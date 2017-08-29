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
package org.opentravel.schemacompiler.repository;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.opentravel.ns.ota2.release_v01_00.PrincipalItemsType;
import org.opentravel.ns.ota2.release_v01_00.ReferencedItemsType;
import org.opentravel.ns.ota2.release_v01_00.ReleaseIdentityType;
import org.opentravel.ns.ota2.release_v01_00.ReleaseItemType;
import org.opentravel.ns.ota2.release_v01_00.ReleaseStatus;
import org.opentravel.ns.ota2.release_v01_00.ReleaseType;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.LoaderValidationMessageKeys;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.impl.ReleaseFileUtils;
import org.opentravel.schemacompiler.repository.impl.ReleaseLibraryModuleLoader;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.transform.util.ModelReferenceResolver;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.xml.XMLGregorianCalendarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the loading and saving of OTM releases on the local file system
 * and the OTM repository.
 */
public class ReleaseManager implements LoaderValidationMessageKeys {
	
    private static final Logger log = LoggerFactory.getLogger( ReleaseManager.class );
    
	private ReleaseType release;
	private File releaseFile;
	private TLModel model;
	private RepositoryManager repositoryManager;
	
	/**
	 * Default constructor.
	 * 
	 * @throws RepositoryException  thrown if the underlying repository manager cannot be initialized
	 */
	public ReleaseManager() throws RepositoryException {
		this( RepositoryManager.getDefault() );
	}
	
	/**
	 * Constructor that supplies a pre-configured repository manager.
	 * 
	 * @param repositoryManager  the repository manager to use when accessing remote content
	 */
	public ReleaseManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
		this.model = new TLModel();
		new ProjectManager( model, false, repositoryManager );
	}
	
	/**
	 * Creates a new release for this manager instance.  The version of the new release will
	 * be automatically set to "1.0.0".
	 * 
	 * @param baseNamespace  the base namespace of the new release
	 * @param name  the name of the new release
	 * @param folderLocation  the folder location where the release file should be saved
	 * @throws LibrarySaveException  thrown if an error occurs while saving the release file
	 */
	public void createNewRelease(String baseNamespace, String name, File folderLocation)
			throws LibrarySaveException {
		if ((baseNamespace == null) || (name == null)
				|| baseNamespace.trim().equals("") || name.trim().equals("")) {
			throw new IllegalArgumentException("The namespace and name of a new release cannot be null or blank.");
		}
		ReleaseIdentityType releaseId = new ReleaseIdentityType();
		String releaseFilename = name.replaceAll( "\\s", "_" ) + "_1_0_0.otr";
		
		this.releaseFile = new File( folderLocation, releaseFilename );
		this.model.clearModel();
		this.release = new ReleaseType();
		
		releaseId.setBaseNamespace( baseNamespace );
		releaseId.setName( name );
		releaseId.setFilename( releaseFilename );
		releaseId.setVersion( "1.0.0" );
		release.setReleaseIdentity( releaseId );
		release.setStatus( ReleaseStatus.DRAFT );
		release.setPrincipalItems( new PrincipalItemsType() );
		release.setReferencedItems( new ReferencedItemsType() );
		saveRelease();
	}
	
	/**
	 * Loads the contents of a release from the specified file on the local file system.
	 * 
	 * @param releaseFile  the OTM release file to load
	 * @return ValidationFindings
	 * @throws RepositoryException  thrown if an error occurs that prevents the release from being loaded
	 */
	public ValidationFindings loadRelease(File releaseFile) throws RepositoryException {
		try {
			ValidationFindings findings = new ValidationFindings();
			
			// Clear any existing release & model info so we will not be left in an inconsistent
			// state if something goes wrong during the load.
			this.release = null;
			this.releaseFile = null;
			this.model = null;
			
			this.release = ReleaseFileUtils.loadReleaseFile( releaseFile, findings );
			loadReleaseModel( findings );
			this.releaseFile = releaseFile;
			return findings;
			
		} catch (LibraryLoaderException e) {
			throw new RepositoryException("Unexpected error loading release content.", e);
		}
	}
	
	/**
	 * Saves the content of the current release.
	 * 
	 * @throws LibrarySaveException  thrown if an error occurs while saving the release file
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
	 */
	public void saveRelease() throws LibrarySaveException {
		checkModificationAllowed();
		ReleaseFileUtils.saveReleaseFile( release, releaseFile );
	}
	
	/**
	 * Returns true if the given repository item is among the principal items
	 * of the current release.
	 * 
	 * @param item  the repository item to check
	 * @return boolean
	 */
	public boolean isPrincipalItem(RepositoryItem item) {
		return (findReleaseItem( item, true, false ) != null);
	}
	
	/**
	 * Adds a principal item to the current release.  If the given repository
	 * item is already a principal member, this method has no effect.  If the
	 * item is currently a referenced item, it will be moved to the set of
	 * principal members.
	 * 
	 * @param item  the repository item to be added
	 * @return ReleaseItemType
	 * @throws RepositoryException  thrown if the repository item cannot be located
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
	 */
	public ReleaseItemType addPrincipalItem(RepositoryItem item) {
		return addPrincipalItem( item,
				(release == null) ? null :
					XMLGregorianCalendarConverter.toJavaDate( release.getDefaultEffectiveDate() ) );
	}
	
	/**
	 * Adds a principal item to the current release and assigns the given effective
	 * date.  If the given repository item is already a principal member, this method
	 * has no effect beyond assigning the effective date.  If the item is currently a
	 * referenced item, it will be moved to the set of principal members.
	 * 
	 * @param item  the repository item to be added
	 * @param effectiveDate  the effective date to apply for the item (null for latest commit)
	 * @return ReleaseItemType
	 * @throws RepositoryException  thrown if the repository item cannot be located
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
	 */
	public ReleaseItemType addPrincipalItem(RepositoryItem item, Date effectiveDate) {
		ReleaseItemType releaseItem = findReleaseItem( item, true, false );
		checkModificationAllowed();
		
		if (releaseItem == null) {
			releaseItem = findReleaseItem( item, false, true );
			
			if (releaseItem != null) { // move from referenced to principal list
				release.getReferencedItems().getReleaseItem().remove( releaseItem );
				
			} else { // brand new item for this release
				releaseItem = new ReleaseItemType();
				releaseItem.setRepositoryID( item.getRepository().getId() );
				releaseItem.setBaseNamespace( item.getBaseNamespace() );
				releaseItem.setFilename( item.getFilename() );
				releaseItem.setVersion( item.getVersion() );
			}
			release.getPrincipalItems().getReleaseItem().add( releaseItem );
			setEffectiveDate( releaseItem, effectiveDate );
		}
		return releaseItem;
	}
	
	/**
	 * Removes the given repository item from the list of principal items in the current
	 * release.  If no such repository item is in the principal list, this method has
	 * no effect.
	 * 
	 * @param item  the repository item to remove from this release
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
	 */
	public void removePrincipalItem(RepositoryItem item) {
		ReleaseItemType releaseItem = findReleaseItem( item, true, true );
		
		if (releaseItem != null) {
			release.getPrincipalItems().getReleaseItem().remove( releaseItem );
			release.getReferencedItems().getReleaseItem().remove( releaseItem );
		}
	}
	
	/**
	 * Returns true if the given repository item is among the referenced items
	 * of the current release.
	 * 
	 * @param item  the repository item to check
	 * @return boolean
	 */
	public boolean isReferencedItem(RepositoryItem item) {
		return (findReleaseItem( item, false, true ) != null);
	}
	
	/**
	 * Returns the effective date for the given repository item.  If the item is not
	 * a member of the current release or no effective date is assigned, this method
	 * will return null.
	 * 
	 * @param item  the repository item for which to return an effective date
	 * @return Date
	 */
	public Date getEffectiveDate(RepositoryItem item) {
		ReleaseItemType releaseItem = findReleaseItem( item, true, true );
		
		return getEffectiveDate( releaseItem );
	}
	
	/**
	 * Assigns the effective date of the given repository item in the current release.  The
	 * item may be a principal or referenced item.
	 * 
	 * @param item  the repository item for which to set the effective date
	 * @param effectiveDate  the effective date to assign to the item (null for latest commit)
	 */
	public void setEffectiveDate(RepositoryItem item, Date effectiveDate) {
		setEffectiveDate( findReleaseItem( item, true, true ), effectiveDate );
	}
	
	/**
	 * Assigns the effective date of the given release item.
	 * 
	 * @param item  the release item for which to set the effective date
	 * @param effectiveDate  the effective date to assign to the item
	 */
	public void setEffectiveDate(ReleaseItemType item, Date effectiveDate) {
		if (item != null) {
			item.setEffectiveDate(
					XMLGregorianCalendarConverter.toXMLGregorianCalendar( effectiveDate ) );
		}
	}
	
	/**
	 * Assigns the effective date that will be used for all new items (principal and
	 * referenced) for which an effective date is not otherwise specified.
	 * 
	 * @param effectiveDate  the default effective date for the current release
	 */
	public void setDefaultEffectiveDate(Date effectiveDate) {
		release.setDefaultEffectiveDate(
				XMLGregorianCalendarConverter.toXMLGregorianCalendar( effectiveDate ) );
	}
	
	/**
	 * Returns the repository item associated with the given repository URL or null
	 * if the given URL is not for a managed library.
	 * 
	 * @param libraryUrl  the library URL for which to return a repository item
	 * @return RepositoryItem
	 * @throws RepositoryException  thrown if an error occurs while retrieving the repository item
	 */
	public RepositoryItem getRepositoryItem(URL libraryUrl) throws RepositoryException {
		File libraryFile = URLUtils.isFileURL( libraryUrl ) ? URLUtils.toFile( libraryUrl ) : null;
		RepositoryItem item = null;
		
		if (isRepositoryFile( libraryFile )) {
			item = repositoryManager.getRepositoryItem( libraryFile );
		}
		return item;
	}
	
	/**
	 * Returns the release that was loaded by this manager.
	 * 
	 * @return ReleaseType
	 */
	public ReleaseType getRelease() {
		return release;
	}
	
	/**
	 * Returns the model that was created from one of the 'loadRelease()' methods.
	 * 
	 * @return TLModel
	 */
	public TLModel getModel() {
		return model;
	}
	
	/**
	 * Returns the repository manager associated with this release manager.
	 *
	 * @return RepositoryManager
	 */
	public RepositoryManager getRepositoryManager() {
		return repositoryManager;
	}

	/**
	 * Loads or reloads the model to incorporate changes to effective dates and principal
	 * release items.  During the re-load, the list of referenced libraries for the
	 * release is updated.  Referenced items that are no longer required are removed
	 * and new dependencies are added using the default effective date for the release.
	 * 
	 * @param findings  the validation findings where any load-time errors or warnings
	 *					should be reported
	 * @throws RepositoryException  thrown if a remote repository cannot be accessed
	 * @throws LibraryLoaderException  thrown if an exception occurs during the load process
	 */
	public void loadReleaseModel(ValidationFindings findings)
			throws RepositoryException, LibraryLoaderException {
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>( model );
		ReleaseLibraryModuleLoader moduleLoader = new ReleaseLibraryModuleLoader( this, modelLoader.getModuleLoader() );
		
		model.clearModel();
		modelLoader.getNamespaceResolver().setModel( model );
		modelLoader.setModuleLoader( moduleLoader );
		modelLoader.setResolveModelReferences( false );
		
		// Only force-load the principal items; referenced items will be refreshed
		for (ReleaseItemType principalItem : release.getPrincipalItems().getReleaseItem()) {
			RepositoryItem repoItem = getRepositoryItem( principalItem );
			Date effectiveDate = getEffectiveDate( principalItem );
			LibraryInputSource<InputStream> inputSource =
					repositoryManager.getHistoricalContentSource( repoItem, effectiveDate );
			
			modelLoader.loadLibraryModel( inputSource );
		}
		ModelReferenceResolver.resolveReferences( model );
		moduleLoader.updateReferencedItems( release );
		validateModel( findings );
	}
	
    /**
     * Validates the model and capturing any errors/warnings that exist.
     * 
     * @param findings  the validation findings where any errors or warnings should be reported
     */
    private void validateModel(ValidationFindings findings) {
        for (TLLibrary library : model.getUserDefinedLibraries()) {
            try {
                findings.addAll( TLModelCompileValidator.validateModelElement( library, false ) );

            } catch (Throwable t) {
                findings.addFinding( FindingType.ERROR, library,
                        ERROR_UNKNOWN_EXCEPTION_DURING_VALIDATION, library.getName(),
                        ExceptionUtils.getExceptionClass( t ).getSimpleName(),
                        ExceptionUtils.getExceptionMessage( t ) );
                log.debug("Unexpected exception validating liberary module: " + library.getName(), t);
            }
        }
    }
    
    /**
     * Checks to see whether modification of the current release is allowed and throws
     * an appropriate exception if it is not.
     * 
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
     */
    private void checkModificationAllowed() {
		if (release == null) {
			throw new IllegalStateException("Unable to add - no release has been created or loaded.");
		}
		if ((releaseFile != null) && isRepositoryFile( releaseFile )) {
			throw new UnsupportedOperationException("Releases published to an OTM repository cannot be modified.");
		}
    }

    /**
     * Returns a repository item that matches all aspects of the given release
     * item except for the effective date specification.
     * 
     * @param releaseItem  the release item from which to create the repository item
     * @return RepositoryItem
     */
    private RepositoryItem getRepositoryItem(ReleaseItemType releaseItem) {
    	RepositoryItemImpl repoItem = new RepositoryItemImpl();
    	
    	repoItem.setRepository( repositoryManager.getRepository( releaseItem.getRepositoryID() ) );
    	repoItem.setBaseNamespace( releaseItem.getBaseNamespace() );
    	repoItem.setFilename( releaseItem.getFilename() );
    	repoItem.setVersion( releaseItem.getVersion() );
    	return repoItem;
    }
    
    /**
     * Returns the release item from the current release that corresponds to the
     * given repository item.
     * 
     * @param repoItem  the repository item for which to return the corresponding release item
     * @param includePrincipalItems  flag indicating whether principal items should be considered
     * @param includeReferencedItems  flag indicating whether referenced items should be considered
     * @return ReleaseItemType
     */
    private ReleaseItemType findReleaseItem(RepositoryItem repoItem, boolean includePrincipalItems,
    		boolean includeReferencedItems) {
    	ReleaseItemType releaseItem = null;
    	
    	if (release != null) {
    		List<ReleaseItemType> allItems = new ArrayList<>();
    		
    		if (includePrincipalItems) {
        		allItems.addAll( release.getPrincipalItems().getReleaseItem() );
    		}
    		if (includeReferencedItems) {
        		allItems.addAll( release.getReferencedItems().getReleaseItem() );
    		}
    		
    		for (ReleaseItemType item : allItems) {
    			if (isEquivalent( item, repoItem )) {
    				releaseItem = item;
    				break;
    			}
    		}
    	}
    	return releaseItem;
    }
    
    /**
     * Returns true if the given release item is equivalent to the repository item.
     * 
     * @param releaseItem  the release item to compare
     * @param repoItem  the repository item to compare
     * @return boolean
     */
    private boolean isEquivalent(ReleaseItemType releaseItem, RepositoryItem repoItem) {
    	boolean result = false;
    	
    	if ((releaseItem != null) && (repoItem != null)) {
        	String releaseNS = releaseItem.getBaseNamespace();
        	String releaseFilename = releaseItem.getFilename();
        	String releaseVersion = releaseItem.getVersion();
    		
        	result = (releaseNS != null) && (releaseFilename != null) && (releaseVersion != null)
        			&& releaseNS.equals( repoItem.getBaseNamespace() )
        			&& releaseFilename.equals( repoItem.getFilename() )
        			&& releaseVersion.equals( repoItem.getVersion() );
    	}
    	return result;
    }
    
    /**
     * Returns true if the given file references a location in the user's local
     * repository -- either as a locally-managed item or a local copy of a
     * remotely-managed library or release.
     * 
     * @param file  the release or library file to analyze
     * @return boolean
     */
    private boolean isRepositoryFile(File file) {
        boolean result = false;
        
        if (file != null) {
            File repositoryLocation = repositoryManager.getRepositoryLocation();
            File libraryFolder = file.getParentFile();
            
            while (!result && (libraryFolder != null)) {
                result = libraryFolder.equals(repositoryLocation);
                libraryFolder = libraryFolder.getParentFile();
            }
        }
        return result;
    }
    
    /**
     * Returns the effective date of the given release item as a Java date
     * instance.  This is a convenience method for translating the
     * <code>XMLGregorianCalendar</code> date value.
     * 
     * @param releaseItem  the release item for which to return an effective date
     * @return Date
     */
    public static Date getEffectiveDate(ReleaseItemType releaseItem) {
    	XMLGregorianCalendar jaxbDate = (releaseItem == null) ? null : releaseItem.getEffectiveDate();
    	Date effectiveDate = null;
    	
    	if (jaxbDate != null) {
    		effectiveDate = XMLGregorianCalendarConverter.toJavaDate( jaxbDate );
    	}
    	return effectiveDate;
    }
    
}
