/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.otm.repository.reports;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.loader.impl.MultiVersionLibraryModuleLoader;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemacompiler.repository.RepositoryItemHistory;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.ModelReferenceResolver;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans an OTM repository for all libraries and named entities.  Results are saved in
 * the relational database for reporting purposes.
 */
public class RepositoryReportLoader {
	
	public static boolean debug = false;
	
    private static final Logger log = LoggerFactory.getLogger( RepositoryReportLoader.class );
	private static VersionScheme versionScheme;
	
	private DefaultTransformerContext transformContext = new DefaultTransformerContext();
	private TransformerFactory<DefaultTransformerContext> transformerFactory = TransformerFactory
            .getInstance(SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY, transformContext);
	private EntityManagerFactory emFactory;
	private EntityManager entityManager;
	private String namespaceFilter;
	private String repositoryId;
	
	/**
	 * Constructor that specifies the ID of the repository to be processed by
	 * this report loader.
	 * 
	 * @param repositoryId  the ID of the repository to load
	 * @param factory  the JPA entity manager factory to use for all database access
	 */
	public RepositoryReportLoader(String repositoryId, EntityManagerFactory factory) {
		this.repositoryId = repositoryId;
		this.emFactory = factory;
	}
	
	/**
	 * Purges the reporting database and reloads all content from the OTM repository.
	 * 
	 * @throws RepositoryException  thrown if the OTM repository cannot be accessed
	 */
	public void execute() throws RepositoryException {
		try {
			initialize();
			RepositoryManager repositoryManager = RepositoryManager.getDefault();
			Repository repository = repositoryManager.getRepository( repositoryId );
			
			for (String baseNS : repository.listBaseNamespaces()) {
				if (!processNamespace( baseNS )) continue;
				if (debug) log.info("Processing Namespace: " + baseNS);
				EntityDateCalculator dateCalculator = new EntityDateCalculator();
				Map<String,TRLibrary> libraryMasters = new HashMap<>();
				Map<String,TREntity> entityMasters = new HashMap<>();
				List<RepositoryItem> itemList = repository.listItems( baseNS,
						TLLibraryStatus.DRAFT, false, RepositoryItemType.LIBRARY );
				
				for (RepositoryItem item : itemList) {
					if (debug) log.info("  Processing: " + item.getFilename());
					boolean success = false;
					
					try {
						RepositoryItemHistory history = repository.getHistory( item );
						String libraryMasterKey = item.getBaseNamespace() + " | " + item.getLibraryName();
						Date versionCreateDate = getFirstCommitDate( history );
						TRLibrary library = libraryMasters.get( libraryMasterKey );
						
						entityManager.getTransaction().begin();
						
						if (library == null) {
							library = new TRLibrary();
							library.setBaseNamespace( item.getBaseNamespace() );
							library.setLibraryName( item.getLibraryName() );
							library.setCreateDate( versionCreateDate );
							libraryMasters.put( libraryMasterKey, library );
							entityManager.persist( library );
							
						} else if (versionCreateDate.before( library.getCreateDate() )) {
							library.setCreateDate( versionCreateDate );
						}
						
						TRLibraryVersion libraryVersion = new TRLibraryVersion();
						Map<String,TREntityVersion> entityVersions = new HashMap<>();
						
						libraryVersion.setLibrary( library );
						libraryVersion.setVersion( item.getVersion() );
						libraryVersion.setMajorVersion( Integer.parseInt( versionScheme.getMajorVersion( item.getVersion() ) ) );
						libraryVersion.setMinorVersion( Integer.parseInt( versionScheme.getMinorVersion( item.getVersion() ) ) );
						libraryVersion.setPatchVersion( Integer.parseInt( versionScheme.getPatchLevel( item.getVersion() ) ) );
						libraryVersion.setCreateDate( getFirstCommitDate( history ) );
						entityManager.persist( libraryVersion );
						
						for (RepositoryItemCommit commit : history.getCommitHistory()) {
							TLLibrary libraryContent = loadLibraryContent( item, commit.getEffectiveOn() );
							TRLibraryCommit libraryCommit = new TRLibraryCommit();
							
							libraryCommit.setLibraryVersion( libraryVersion );
							libraryCommit.setCommitNumber( commit.getCommitNumber() );
							libraryCommit.setCommitDate( commit.getEffectiveOn() );
							entityManager.persist( libraryCommit );
							
							for (NamedEntity tlEntity : libraryContent.getNamedMembers()) {
								if (isLocalContextualFacet( tlEntity )) continue;
								String entityName = getEntityName( tlEntity );
								String entityMasterKey = libraryMasterKey + " | " + entityName;
								String entityVersionKey = entityMasterKey + " | " + libraryVersion.getVersion();
								TREntity entity = entityMasters.get( entityMasterKey );
								TREntityVersion entityVersion = entityVersions.get( entityVersionKey );
								
								if (entity == null) {
									entity = new TREntity();
									entity.setLibrary( library );
									entity.setEntityName( entityName );
									entity.setEntityType( tlEntity.getClass().getSimpleName() );
									entity.setCreateDate( versionCreateDate );
									entityMasters.put( entityMasterKey, entity );
									entityManager.persist( entity );
								}
								
								if (entityVersion == null) {
									entityVersion = new TREntityVersion();
									entityVersion.setEntity( entity );
									entityVersion.setLibraryVersion( libraryVersion );
									entityVersion.setCreateDate( versionCreateDate );
									entityVersions.put( entityVersionKey, entityVersion );
									entityManager.persist( entityVersion );
								}
								dateCalculator.add( entityVersion, commit.getEffectiveOn() );
							}
						}
						
						entityManager.getTransaction().commit();
						success = true;
						
					} catch (PersistenceException e) {
						log.error("Error committing transaction for library: " + item.getFilename());
						
					} catch (Throwable t) {
						log.error("Error publishing report data for library: " + item.getFilename(), t);
						
					} finally {
						if (!success && entityManager.getTransaction().isActive()) {
							entityManager.getTransaction().rollback();
						}
					}
				}
				
				// Assign deletion dates for all entities in this namespace
				boolean success = false;
				
				try {
					entityManager.getTransaction().begin();
					dateCalculator.assignDates();
					entityManager.getTransaction().commit();
					success = true;
					
				} catch (PersistenceException e) {
					log.error("Error committing entity effective dates for namespace: " + baseNS);
					
				} catch (Throwable t) {
					log.error("Error assigning entity effective dates for namespace: " + baseNS, t);
					
				} finally {
					if (!success && entityManager.getTransaction().isActive()) {
						entityManager.getTransaction().rollback();
					}
				}
			}
			
		} finally {
			shutdown();
		}
	}
	
	/**
	 * Assigns the namespace to be processed by this loader (null for all).  This method
	 * is typically used only for testing purposes.
	 * 
	 * @param nsFilter  the namespace filter to assign
	 */
	public void setNamespaceFilter(String nsFilter) {
		this.namespaceFilter = nsFilter;
	}
	
	/**
	 * Returns true if the given namespace should be processed by this loader.
	 * 
	 * @param ns  the namespace to check for processing
	 * @return boolean
	 */
	private boolean processNamespace(String ns) {
		boolean result = true;
		
		if (namespaceFilter != null) {
			String testNS = ns.endsWith("/") ? ns : (ns + "/");
			result = testNS.startsWith( namespaceFilter );
		}
		return result;
	}
	
	/**
	 * Returns true if the given entity is a contextual facet that is considered to be
	 * local to its own library.
	 * 
	 * @param entity  the entity to analyze
	 * @return boolean
	 */
	public boolean isLocalContextualFacet(NamedEntity entity) {
		boolean result = false;
		
		if (entity instanceof TLContextualFacet) {
			result = (((TLContextualFacet) entity).getOwningEntity() != null);
		}
		return result;
	}
	
	/**
	 * Returns the local name of the given named entity.
	 * 
	 * @param entity  the entity for which to return a local name
	 * @return String
	 */
	public String getEntityName(NamedEntity entity) {
		String localName;
		
		if (entity instanceof TLContextualFacet) {
			TLContextualFacet facet = (TLContextualFacet) entity;
			String ownerName = facet.getOwningEntityName();
			int delimeterIdx = (ownerName == null) ? -1 : ownerName.indexOf(':');
			
			if (delimeterIdx >= 0) {
				ownerName = ownerName.substring( delimeterIdx + 1 );
			}
			localName = ownerName + "_" + facet.getName();
			
		} else {
			localName = entity.getLocalName();
		}
		return localName;
	}
	
	/**
	 * Loads the content of the library using the given commit date.
	 * 
	 * @param libraryItem  the repository item for the library to be loaded
	 * @param commitDate  the commit date for the library's content to be loaded
	 * @return TLLibrary
	 * @throws SchemaCompilerException  thrown if the library's historical content cannot be loaded
	 */
	@SuppressWarnings("unchecked")
	public TLLibrary loadLibraryContent(RepositoryItem libraryItem, Date commitDate) throws SchemaCompilerException {
		LibraryInputSource<InputStream> contentSource = libraryItem.getRepository()
				.getHistoricalContentSource( libraryItem, commitDate );
		LibraryModuleLoader<InputStream> moduleLoader = new MultiVersionLibraryModuleLoader();
		ValidationFindings validationFindings = new ValidationFindings();
		LibraryModuleInfo<?> moduleInfo = moduleLoader.loadLibrary( contentSource, validationFindings );
		TLLibrary library = null;
		
		if ((moduleInfo != null) && !validationFindings.hasFinding( FindingType.ERROR )) {
			Object jaxbLibrary = moduleInfo.getJaxbArtifact();
	        ObjectTransformer<Object,TLLibrary,DefaultTransformerContext> transformer =
	        		(ObjectTransformer<Object, TLLibrary, DefaultTransformerContext>)
	        		transformerFactory.getTransformer( jaxbLibrary.getClass(), TLLibrary.class);
	        TLModel model = new TLModel();
	        
	        library = transformer.transform( jaxbLibrary );
	        library.setLibraryUrl( contentSource.getLibraryURL() );
	        model.addLibrary( library );
	        ModelReferenceResolver.resolveReferences( model );
	        
		} else {
			log.error("Invalid library content: " + libraryItem.getFilename() + " / commitDate=" + commitDate);
		}
		return library;
	}
	
	/**
	 * Returns the first commit date in the given repository item's history.
	 * 
	 * @param itemHistory  the repository item history to analyze
	 * @return Date
	 */
	public Date getFirstCommitDate(RepositoryItemHistory itemHistory) {
		Date firstCommit = null;
		
		for (RepositoryItemCommit commit : itemHistory.getCommitHistory()) {
			if ((firstCommit == null) || commit.getEffectiveOn().before( firstCommit )) {
				firstCommit = commit.getEffectiveOn();
			}
		}
		return firstCommit;
	}
	
	/**
	 * Deletes the contents of the entire reporting database.
	 * 
	 * @throws PersistenceException  thrown if the database content cannot be deleted
	 */
	@SuppressWarnings("unchecked")
	public <T> void cleanDatabase() throws PersistenceException {
		boolean success = false;
		
		try {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			List<Class<?>> deleteTypes = Arrays.asList( TREntityVersion.class, TREntity.class,
					TRLibraryCommit.class, TRLibraryVersion.class, TRLibrary.class );
			
			entityManager.getTransaction().begin();
			
			for (Class<?> entityType : deleteTypes) {
				CriteriaDelete<T> delete = builder.createCriteriaDelete( (Class<T>) entityType );
				
				delete.from( (Class<T>) entityType );
				entityManager.createQuery( delete ).executeUpdate();
			}
			entityManager.getTransaction().commit();
			success = true;
			
		} finally {
			if (!success && entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
		}
	}
	
	/**
	 * Initializes the JPA entity manager and purges all existing content from the reporting database.
	 */
	public void initialize() {
		entityManager = emFactory.createEntityManager();
		cleanDatabase();
	}
	
	/**
	 * Shuts down the JPA entity manager.
	 */
	public void shutdown() {
		if ((entityManager != null) && entityManager.isOpen()) {
			entityManager.close();
		}
	}
	
	/**
	 * Initializes the OTM schema compiler environment.
	 */
	static {
		try {
			VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
			
			versionScheme = factory.getVersionScheme( factory.getDefaultVersionScheme() );
			OTM16Upgrade.otm16Enabled = true;
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
