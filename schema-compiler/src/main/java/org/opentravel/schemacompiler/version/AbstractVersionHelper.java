
package org.opentravel.schemacompiler.version;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Base class for version helpers used to retrieve related versions of a library or entity from a model
 * or to create new versions.
 * 
 * @author S. Livezey
 */
public abstract class AbstractVersionHelper {
	
	private Project activeProject;
	
	/**
	 * Default constructor.  NOTE: When working in an environment where a <code>ProjectManager</code>
	 * is being used, the other constructor should be used to assign the active project for the helper.
	 */
	public AbstractVersionHelper() {}
	
	/**
	 * Constructor that assigns the active project for an application's <code>ProjectManager</code>.  If
	 * new libraries are created or opened by this helper, they will be automatically added to the active
	 * project that is passed to this constructor.
	 * 
	 * @param activeProject  the active project to which new libraries will be assigned
	 */
	public AbstractVersionHelper(Project activeProject) {
		this.activeProject = activeProject;
	}
	
	/**
	 * Returns the version chain for the given library.  The resulting list includes all dependent
	 * versions starting with the one provided and ending with the major version that started the
	 * chain.
	 * 
	 * @param library  the library for which to return the version chain
	 * @return List<TLLibrary>
	 * @throws VersionSchemeException  thrown if the library's version scheme is not recognized
	 * @throws IllegalStateException  thrown if the given library is not associated with a TLModel instance
	 */
	public List<TLLibrary> getVersionChain(TLLibrary library) throws VersionSchemeException {
		VersionScheme versionScheme = getVersionScheme( library );
		List<TLLibrary> versionChain = new ArrayList<TLLibrary>();
		String currentVersionNS = library.getNamespace();
		
		if (library.getOwningModel() == null) {
			throw new IllegalStateException("The given library is not associated with an owning model instance: "
					+ library.getLibraryUrl().toExternalForm());
		}
		
		// Find all prior patch versions
		while (versionScheme.isPatchVersion(currentVersionNS)) {
			TLLibrary currentVersionLib = findLibrary( currentVersionNS, library.getName(), library.getOwningModel() );
			String currentVersion = versionScheme.getVersionIdentifier( currentVersionNS );
			
			if (currentVersionLib != null) {
				versionChain.add( currentVersionLib );
			}
			currentVersionNS = versionScheme.setVersionIdentifier(
					currentVersionNS, versionScheme.decrementPatchLevel( currentVersion ) );
		}
		
		// Find all prior minor versions
		while (!versionScheme.isMajorVersion(currentVersionNS)) {
			TLLibrary currentVersionLib = findLibrary( currentVersionNS, library.getName(), library.getOwningModel() );
			String currentVersion = versionScheme.getVersionIdentifier( currentVersionNS );
			
			if (currentVersionLib != null) {
				versionChain.add( currentVersionLib );
			}
			currentVersionNS = versionScheme.setVersionIdentifier(
					currentVersionNS, versionScheme.decrementMinorVersion( currentVersion ) );
		}
		
		// Add the major version to the end of the chain
		TLLibrary majorVersionLib = findLibrary( currentVersionNS, library.getName(), library.getOwningModel() );
		
		if (majorVersionLib != null) {
			versionChain.add( majorVersionLib );
		}
		
		return versionChain;
	}
	
	/**
	 * Returns the library from the model with the specified name and namespace, or null if no such
	 * library exists.
	 * 
	 * @param namespace  the namespace of the library to retrieve
	 * @param libraryName  the name of the library to retrieve
	 * @param model  the model from which to retrieve the library
	 * @return TLLibrary
	 */
	private TLLibrary findLibrary(String namespace, String libraryName, TLModel model) {
		AbstractLibrary library = model.getLibrary(namespace, libraryName);
		return (library instanceof TLLibrary) ? (TLLibrary) library : null;
	}
	
	/**
	 * If the given versioned entity is a minor version of an entity defined in a prior version, this
	 * method will return that prior version.
	 * 
	 * @param versionedEntity  the versioned entity for which to return the previous version
	 * @return V
	 * @throws VersionSchemeException  thrown if the entity's version scheme is not recognized
	 */
	@SuppressWarnings("unchecked")
	<V extends Versioned> V getPriorVersionExtension(V versionedEntity) throws VersionSchemeException {
		NamedEntity origEntity = (NamedEntity) versionedEntity;
		NamedEntity extendedEntity = null;
		V extendedVersion = null;
		
		// Identify the extension (if any) based on the type of versioned entity we have
		if (versionedEntity instanceof TLExtensionOwner) {
			TLExtension extension = ((TLExtensionOwner) versionedEntity).getExtension();
			extendedEntity = (extension == null) ? null : extension.getExtendsEntity();
			
		} else if (versionedEntity instanceof TLValueWithAttributes) {
			extendedEntity = ((TLValueWithAttributes) versionedEntity).getParentType();
		}
		
		// Determine whether the extended entity is a minor version of the one that
		// was passed to this method
		if ( (extendedEntity != null) && extendedEntity.getClass().equals(versionedEntity.getClass())
				&& extendedEntity.getLocalName().equals(origEntity.getLocalName()) ) {
			String origBaseNamespace = versionedEntity.getBaseNamespace();
			String extendedBaseNamespace = ((Versioned) extendedEntity).getBaseNamespace();
			
			if ((origBaseNamespace != null) && origBaseNamespace.equals(extendedBaseNamespace)) {
				VersionScheme versionScheme = getVersionScheme(versionedEntity);
				List<String> versionChain = versionScheme.getMajorVersionChain(versionedEntity.getNamespace());
				
				if (versionChain.contains(extendedEntity.getNamespace())) {
					extendedVersion = (V) extendedEntity;
				}
			}
		}
		return extendedVersion;
	}
	
	/**
	 * If the given versioned entity is a minor version of an entity defined in a prior version, this
	 * method will return an ordered list of all the previous versions.  The list is sorted in descending
	 * ordr (i.e. the latest prior version will be the first element in the list).
	 * 
	 * @param versionedEntity  the versioned entity for which to return the previous version
	 * @return List<V>
	 * @throws VersionSchemeException  thrown if the entity's version scheme is not recognized
	 */
	<V extends Versioned> List<V> getAllPriorVersionExtensions(V versionedEntity) throws VersionSchemeException {
		List<V> extendedVersions = new ArrayList<V>();
		V extendedVersion = versionedEntity;
		
		while ((extendedVersion = getPriorVersionExtension(extendedVersion)) != null) {
			extendedVersions.add( extendedVersion );
		}
		Collections.sort(extendedVersions, getVersionScheme(versionedEntity).getComparator(false));
		return extendedVersions;
	}
	
	/**
	 * Returns a list of all libraries that are later minor versions of the given library.
	 * 
	 * @param library  the library for which to return minor versions
	 * @return List<TLLibrary>
	 * @throws VersionSchemeException  thrown if the entity's version scheme is not recognized
	 * @throws IllegalStateException  thrown if the given library is not associated with a TLModel instance
	 */
	List<TLLibrary> getLaterMinorVersions(TLLibrary library) throws VersionSchemeException {
		List<TLLibrary> minorVersionList = new ArrayList<TLLibrary>();
		
		if (library.getOwningModel() == null) {
			throw new IllegalStateException("The given library is not associated with an owning model instance: "
					+ library.getLibraryUrl().toExternalForm());
		}
		
		if ((library != null) && (library.getNamespace() != null)) {
			VersionScheme versionScheme = getVersionScheme( library );
			
			for (TLLibrary lib : library.getOwningModel().getUserDefinedLibraries()) {
				if ((lib == library) || !isVersionCandidateMatch(lib, library)
						|| versionScheme.isPatchVersion(lib.getNamespace())) {
					continue;
				}
				List<String> versionChain = versionScheme.getMajorVersionChain(lib.getNamespace());
				
				// If our original library's namespace occurs later in the version chain
				// than our current one, it must be a later minor version
				if (versionChain.indexOf(library.getNamespace()) > 0) {
					minorVersionList.add( lib );
				}
			}
			Collections.sort(minorVersionList, new LibraryVersionComparator(versionScheme, true));
		}
		return minorVersionList;
	}
	
	/**
	 * Returns a list of all libraries that are later patch versions of the given library.
	 * 
	 * @param library  the library for which to return patch versions
	 * @return List<TLLibrary>
	 * @throws VersionSchemeException  thrown if the entity's version scheme is not recognized
	 * @throws IllegalStateException  thrown if the given library is not associated with a TLModel instance
	 */
	List<TLLibrary> getLaterPatchVersions(TLLibrary library) throws VersionSchemeException {
		List<TLLibrary> patchVersionList = new ArrayList<TLLibrary>();
		
		if ((library != null) && (library.getNamespace() != null)) {
			VersionScheme versionScheme = getVersionScheme( library );
			String minorVersionNS = null;
			
			if (library.getOwningModel() == null) {
				throw new IllegalStateException("The given library is not associated with an owning model instance: "
						+ library.getLibraryUrl().toExternalForm());
			}
			
			if (!versionScheme.isPatchVersion( library.getNamespace() )) {
				minorVersionNS = library.getNamespace();
			}
			
			for (TLLibrary lib : library.getOwningModel().getUserDefinedLibraries()) {
				if ((lib == library) || !isVersionCandidateMatch(lib, library)
						|| !versionScheme.isPatchVersion(lib.getNamespace())) {
					continue;
				}
				if (minorVersionNS != null) { // original library was a major or minor version
					String adjustedVersion = versionScheme.decrementMinorVersion( versionScheme.incrementMinorVersion( lib.getVersion() ) );
					String adjustedLibNS = versionScheme.setVersionIdentifier(lib.getNamespace(), adjustedVersion);
					
					if (adjustedLibNS.equals(minorVersionNS)) {
						patchVersionList.add( lib );
					}
				} else { // original library was a patch version
					List<String> versionChain = versionScheme.getMajorVersionChain(lib.getNamespace());
					
					// If our original library's namespace occurs later in the version chain
					// than our current one, it must be a later minor version
					if (versionChain.indexOf(library.getNamespace()) > 0) {
						patchVersionList.add( lib );
					}
				}
			}
			Collections.sort(patchVersionList, new LibraryVersionComparator(versionScheme, true));
		}
		return patchVersionList;
	}
	
	/**
	 * If the given <code>TLExtensionPointFacet</code> is a version patch, the <code>Versioned</code> entity
	 * that is being patched will be returned.
	 * 
	 * @param xpFacet  the extension point facet for which to return the associated patched entity
	 * @return Versioned
	 * @throws VersionSchemeException  thrown if the entity's version scheme is not recognized
	 */
	Versioned getPatchedVersion(TLExtensionPointFacet xpFacet) throws VersionSchemeException {
		NamedEntity extendedEntity = (xpFacet.getExtension() == null) ? null : xpFacet.getExtension().getExtendsEntity();
		Versioned patchedVersion = null;
		
		if (extendedEntity instanceof TLFacet) {
			TLFacetOwner extendedFacetOwner = ((TLFacet) extendedEntity).getOwningEntity();
			
			if (extendedFacetOwner instanceof Versioned) {
				VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme( xpFacet.getVersionScheme() );
				List<String> versionChain = versionScheme.getMajorVersionChain(xpFacet.getNamespace());
				
				if (versionChain.indexOf(extendedFacetOwner.getNamespace()) > 0) {
					patchedVersion = (Versioned) extendedFacetOwner;
				}
			}
		}
		return patchedVersion;
	}
	
	/**
	 * Searches all known remote repositories for later minor versions of the given library.  If later minor
	 * versions are identified that are not currently available in the model, they are automatically loaded.  The
	 * newly-loaded libraries are added to the list of existing minor versions, and their project items are returned
	 * in the resulting list.
	 * 
	 * @param library  the library for which to import later minor versions
	 * @param existingLaterMinorVersions  the list of later minor versions that already existed within the model
	 * @return List<ProjectItem>
	 * @throws VersionSchemeException  thrown if an error occurs while attempting to import new versions of the library
	 */
	List<ProjectItem> importLaterMinorVersionsFromRepository(TLLibrary library, List<TLLibrary> existingLaterMinorVersions)
			throws VersionSchemeException {
		List<ProjectItem> importedVersions = new ArrayList<ProjectItem>();
		VersionScheme versionScheme = getVersionScheme( library );
		
		if (activeProject != null) {
			List<RepositoryItem> managedVersions = findAllManagedVersionsNotLoaded( library, versionScheme );
			String libraryMajorVersion = versionScheme.getMajorVersion( library.getVersion() );
			Comparator<Versioned> versionComparator = versionScheme.getComparator( true );
			VersionWrapper libraryWrapper = new VersionWrapper( library );
			List<RepositoryItem> laterMinorVersions = new ArrayList<RepositoryItem>();
			
			// Add all of the new versions as long as they
			// 1) have the same major version,
			// 2) are a minor version of the library, and
			// 3) have a version identifier greater than that of our original library
			for (RepositoryItem itemVersion : managedVersions) {
				String itemMajorVersion = versionScheme.getMajorVersion( itemVersion.getVersion() );
				
				if (!libraryMajorVersion.equals(itemMajorVersion) || !versionScheme.isMinorVersion( itemVersion.getNamespace() )) {
					continue;
				}
				VersionWrapper itemWrapper = new VersionWrapper( itemVersion, library.getVersionScheme() );
				
				if (versionComparator.compare(libraryWrapper, itemWrapper) < 0) {
					laterMinorVersions.add( itemVersion );
				}
			}
			
			// Load all of the repository items and re-sort the original list of later minor versions
			if (!laterMinorVersions.isEmpty()) {
				for (RepositoryItem item : laterMinorVersions) {
					try {
						ProjectItem pItem = activeProject.getProjectManager().addManagedProjectItem(item, activeProject);
						
						if (pItem != null) {
							importedVersions.add( pItem );
							existingLaterMinorVersions.add( (TLLibrary) pItem.getContent() );
						}
					} catch (Exception e) {
						// No action - just skip this repository item and move on to the next one
					}
				}
				Collections.sort(existingLaterMinorVersions, new LibraryVersionComparator(versionScheme, true));
			}
		}
		return importedVersions;
	}
	
	/**
	 * Searches all known remote repositories for later patch versions of the given library.  If later patch
	 * versions are identified that are not currently available in the model, they are automatically loaded.  The
	 * newly-loaded libraries are added to the list of existing patch versions, and their project items are returned
	 * in the resulting list.
	 * 
	 * @param library  the library for which to import later patch versions
	 * @param existingLaterPatchVersions  the list of later patch versions that already existed within the model
	 * @return List<ProjectItem>
	 * @throws VersionSchemeException  thrown if an error occurs while attempting to import new versions of the library
	 */
	List<ProjectItem> importLaterPatchVersionsFromRepository(TLLibrary library, List<TLLibrary> existingLaterPatchVersions)
			throws VersionSchemeException {
		List<ProjectItem> importedVersions = new ArrayList<ProjectItem>();
		VersionScheme versionScheme = getVersionScheme( library );
		
		if (activeProject != null) {
			List<RepositoryItem> managedVersions = findAllManagedVersionsNotLoaded( library, versionScheme );
			String libraryMajorVersion = versionScheme.getMajorVersion( library.getVersion() );
			String libraryMinorVersion = versionScheme.getMinorVersion( library.getVersion() );
			Comparator<Versioned> versionComparator = versionScheme.getComparator( true );
			VersionWrapper libraryWrapper = new VersionWrapper( library );
			List<RepositoryItem> laterPatchVersions = new ArrayList<RepositoryItem>();
			
			// Add all of the new versions as long as they
			// 1) have the same major and minor version,
			// 2) are a patch version of the library, and
			// 3) have a version identifier greater than that of our original library
			for (RepositoryItem itemVersion : managedVersions) {
				String itemMajorVersion = versionScheme.getMajorVersion( itemVersion.getVersion() );
				String itemMinorVersion = versionScheme.getMinorVersion( itemVersion.getVersion() );
				
				if (!libraryMajorVersion.equals(itemMajorVersion) || !libraryMinorVersion.equals(itemMinorVersion)
						|| !versionScheme.isPatchVersion( itemVersion.getNamespace() )) {
					continue;
				}
				VersionWrapper itemWrapper = new VersionWrapper( itemVersion, library.getVersionScheme() );
				
				if (versionComparator.compare(libraryWrapper, itemWrapper) < 0) {
					laterPatchVersions.add( itemVersion );
				}
			}
			
			// Load all of the repository items and re-sort the original list of later patch versions
			if (!laterPatchVersions.isEmpty()) {
				for (RepositoryItem item : laterPatchVersions) {
					try {
						ProjectItem pItem = activeProject.getProjectManager().addManagedProjectItem(item, activeProject);
						
						if (pItem != null) {
							importedVersions.add( pItem );
							existingLaterPatchVersions.add( (TLLibrary) pItem.getContent() );
						}
					} catch (Exception e) {
						// No action - just skip this repository item and move on to the next one
					}
				}
				Collections.sort(existingLaterPatchVersions, new LibraryVersionComparator(versionScheme, true));
			}
		}
		return importedVersions;
	}
	
	/**
	 * Returns a list of repository items that represent all versions of the given library that
	 * exist in a remote repository that have not been loaded into the active project.
	 * 
	 * @param library  the library for which to find non-loaded versions
	 * @param versionScheme  the version scheme of the library
	 * @return List<RepositoryItem>
	 * @throws VersionSchemeException  thrown if the library's version scheme is invalid
	 */
	private List<RepositoryItem> findAllManagedVersionsNotLoaded(TLLibrary library, VersionScheme versionScheme)
			throws VersionSchemeException {
		RepositoryManager repositoryManager = activeProject.getProjectManager().getRepositoryManager();
		List<Repository> repositoriesToSearch = new ArrayList<Repository>();
		List<RepositoryItem> newVersions = new ArrayList<RepositoryItem>();
		List<String> itemKeys = new ArrayList<String>();
		
		repositoriesToSearch.addAll( repositoryManager.listRemoteRepositories() );
		repositoriesToSearch.add( repositoryManager ); // the repository manager instance represents the local repository
		
		for (Repository repository : repositoriesToSearch) {
			try {
				String baseNamespace = library.getBaseNamespace();
				
				if (baseNamespace == null) {
					continue;
				}
				for (RepositoryItem item : repository.listItems(baseNamespace, false, true)) {
					String itemKey = getRepositoryItemKey( item );
					
					if (!itemKeys.contains(itemKey) && item.getBaseNamespace().equals(baseNamespace)) {
						String targetFilename = versionScheme.getDefaultFileHint(item.getNamespace(), library.getName());
						
						if (targetFilename.equals(item.getFilename())) {
							URL itemUrl = repositoryManager.getContentLocation(item);
							
							if (activeProject.getProjectManager().getModel().getLibrary( itemUrl ) == null) {
								newVersions.add( item );
							}
						}
						itemKeys.add( itemKey );
					}
				}
			} catch (RepositoryException e) {
				// No action - skip this repository and move on if not available
			}
		}
		return newVersions;
	}
	
	/**
	 * Returns a unique key to identify the given repository item.  This is necessary just in case multiple
	 * copies of the same item exist in different remote repositories (unlikely, but possible).
	 * 
	 * @param item  the repository item for which to return a key
	 * @return String
	 */
	private String getRepositoryItemKey(RepositoryItem item) {
		StringBuilder key = new StringBuilder();
		
		key.append( item.getNamespace() ).append(":");
		key.append( item.getFilename() ).append(":");
		key.append( item.getVersion() );
		return key.toString();
	}
	
	/**
	 * Removes each of the imported project items from the current active project.
	 * 
	 * @param importedVersions  the list of imported version items to be removed
	 */
	void removeImportedVersions(List<ProjectItem> importedVersions) {
		if ((activeProject != null) && (importedVersions != null)) {
			for (ProjectItem item : importedVersions) {
				activeProject.remove( item );
			}
		}
	}
	
	/**
	 * Rolls up the contents of the patch library into the minor version library provided.
	 * 
	 * @param minorVersionLibrary  the minor version libarary that will receive any new/modified rolled-up entities
	 * @param patchLibrary  the patch library whose contents are to be rolled up
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @param rollupReferences  reference information for the libraries being rolled up
	 * @throws VersionSchemeException  thrown if the library's version scheme is not recognized
	 */
	void rollupPatchLibrary(TLLibrary minorVersionLibrary, TLLibrary patchLibrary, ModelElementCloner cloner,
			RollupReferenceInfo rollupReferences) throws VersionSchemeException {
		for (TLContext context : patchLibrary.getContexts()) {
			if (minorVersionLibrary.getContext(context.getContextId()) == null) {
				minorVersionLibrary.addContext( cloner.clone(context) );
			}
		}
		for (TLSimple simple : patchLibrary.getSimpleTypes()) {
			if (minorVersionLibrary.getNamedMember(simple.getName()) == null) {
				TLSimple clone = cloner.clone(simple);
				
				minorVersionLibrary.addNamedMember( clone );
				captureRollupLibraryReference( clone, rollupReferences );
			}
		}
		for (TLClosedEnumeration closedEnum : patchLibrary.getClosedEnumerationTypes()) {
			if (minorVersionLibrary.getNamedMember(closedEnum.getName()) == null) {
				TLClosedEnumeration clone = cloner.clone(closedEnum);
				
				minorVersionLibrary.addNamedMember( clone );
				captureRollupLibraryReference( clone, rollupReferences );
			}
		}
		for (TLOpenEnumeration openEnum : patchLibrary.getOpenEnumerationTypes()) {
			if (minorVersionLibrary.getNamedMember(openEnum.getName()) == null) {
				TLOpenEnumeration clone = cloner.clone(openEnum);
				
				minorVersionLibrary.addNamedMember( clone );
				captureRollupLibraryReference( clone, rollupReferences );
			}
		}
		for (TLExtensionPointFacet patch : patchLibrary.getExtensionPointFacetTypes()) {
			Versioned patchedEntity = getPatchedVersion( patch );
			
			if (patchedEntity != null) {
				try {
					Versioned newEntityVersion = createOrRetrieveNewEntityVersion( patchedEntity, minorVersionLibrary, cloner );
					rollupPatchVersion(newEntityVersion, patch, cloner, rollupReferences, true);
					
				} catch (ValidationException e) {
					// Not possible since we indicated validation should be skipped
				}
			}
		}
	}
	
	/**
	 * Constructs a new empty <code>TLLibrary</code> instance using the given one as a basis.  The only
	 * fields that are not initialized on the new library are it's namespace, prefix, and URL assignments.
	 * In addition to those initial field values, all of the contexts in the given library are cloned
	 * for the new instance.
	 * 
	 * @param library  the library to be used as a basis for the new one
	 * @return TLLibrary
	 */
	TLLibrary createNewLibrary(TLLibrary library) {
		TLLibrary newLibrary = new TLLibrary();
		
		newLibrary.setName( library.getName() );
		newLibrary.setComments( library.getComments() );
		newLibrary.setStatus( TLLibraryStatus.DRAFT );
		
		for (TLContext context : library.getContexts()) {
			newLibrary.addContext( (TLContext) context.cloneElement() );
		}
		return newLibrary;
	}
	
	/**
	 * Constructs a new copy of the given entity with the same local name, but does
	 * not populate any of the contents (elements or attributes).  The new entity is
	 * added to the target library as a new named member.
	 * 
	 * @param originalEntity  the original entity from which the copy should be constructed
	 * @param targetLibrary  the target library to which the new entity version will be assigned
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @return Versioned
	 */
	Versioned createOrRetrieveNewEntityVersion(Versioned originalEntity, TLLibrary targetLibrary, ModelElementCloner cloner) {
		Versioned newEntityVersion;
		try {
			// Attempt to find an existing entity that matches the original
			if (originalEntity instanceof TLOperation) {
				TLService service = targetLibrary.getService();
				newEntityVersion = (service == null) ? null : service.getOperation( ((TLOperation) originalEntity).getName() );
				
			} else {
				newEntityVersion = (Versioned) targetLibrary.getNamedMember( originalEntity.getLocalName() );
			}
			
			// If a matching entity does not already exist, create an empty instance
			// with the same name.
			if (newEntityVersion == null) {
				if (originalEntity instanceof TLBusinessObject) {
					TLBusinessObject newBO = newVersionInstance( (TLBusinessObject) originalEntity, cloner );
					TLExtension ext = new TLExtension();
					ext.setExtendsEntity(originalEntity);
					newBO.setExtension(ext);
					
					targetLibrary.addNamedMember( newBO );
					newEntityVersion = newBO;
					
				} else if (originalEntity instanceof TLCoreObject) {
					TLCoreObject newCore = newVersionInstance( (TLCoreObject) originalEntity, cloner );
					TLExtension ext = new TLExtension();
					ext.setExtendsEntity(originalEntity);
					newCore.setExtension(ext);
					
					targetLibrary.addNamedMember( newCore );
					newEntityVersion = newCore;
					
				} else if (originalEntity instanceof TLOperation) {
					TLService newVersionService = targetLibrary.getService();
					TLOperation oldOp = (TLOperation) originalEntity;
					TLOperation newOp = newVersionInstance( oldOp, cloner );
					
					if (newVersionService == null) {
						newVersionService = newVersionInstance( oldOp.getOwningService(), cloner );
						targetLibrary.setService( newVersionService );
					}
					newVersionService.addOperation( newOp );
					newEntityVersion = newOp;
					
				} else if (originalEntity instanceof TLValueWithAttributes) {
					TLValueWithAttributes newVWA = newVersionInstance( (TLValueWithAttributes) originalEntity, cloner );
					
					targetLibrary.addNamedMember( newVWA );
					newEntityVersion = newVWA;
				}
			}
		} catch (ClassCastException e) {
			newEntityVersion = null;
		}
		return newEntityVersion;
	}
	
	/**
	 * Rolls up the contents of the given patch version into the new major/minor version.  A roll-up is essentially
	 * a merge of the attributes, properties, and indicators from the facets of the patch version.
	 * 
	 * @param majorOrMinorVersionTarget  the major/minor version of the entity that will receive any rolled up items
	 * @param patchVersion  the patch version of the entity whose items will be the source of the roll-up
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @param rollupReferences  reference information for the libraries being rolled up
	 * @param skipValidation  internal flag indicating whether entity validation should be skipped
	 * @throws VersionSchemeException  thrown if the given major version is not a later major version and/or
	 *								   the patch version is not a prior patch version
	 * @throws ValidationException  thrown if the rollup cannot be performed because one or more validation
	 *								errors exist in either the source or target entity
	 */
	void rollupPatchVersion(Versioned majorOrMinorVersionTarget, TLExtensionPointFacet patchVersion,
			ModelElementCloner cloner,RollupReferenceInfo rollupReferences, boolean skipValidation)
					throws VersionSchemeException, ValidationException {
		// Perform validation checks before rolling up
		if (!skipValidation) {
			ValidationFindings findings = new ValidationFindings();
			findings.addAll( validate(majorOrMinorVersionTarget) );
			findings.addAll( validate(patchVersion) );
			
			if (findings.hasFinding(FindingType.ERROR)) {
				throw new ValidationException("Unable to roll-up because the target and/or patch versions contains errors.", findings);
			}
			if ((majorOrMinorVersionTarget.getVersionScheme() == null)
					|| !majorOrMinorVersionTarget.getVersionScheme().equals(patchVersion.getVersionScheme())) {
				throw new VersionSchemeException("The target and patch versions to be rolled-up are not assigned to the same version scheme.");
			}
			if ((majorOrMinorVersionTarget.getBaseNamespace() == null)
					|| !majorOrMinorVersionTarget.getBaseNamespace().equals(patchVersion.getBaseNamespace())) {
				throw new VersionSchemeException("The target and patch versions to be rolled-up are not assigned to the same base namespace.");
			}
			if (isReadOnly(majorOrMinorVersionTarget.getOwningLibrary())) {
				throw new VersionSchemeException("Unable to roll-up the requested patch because the target version is in a read-only library.");
			}
			
			// Verify that the patch is, in fact, a patch of one of its prior minor versions
			List<Versioned> priorMajorOrMinorVersions = getAllPriorVersionExtensions(majorOrMinorVersionTarget);
			Versioned patchedVersion = getPatchedVersion( patchVersion );
			
			if ((patchedVersion == null) || !priorMajorOrMinorVersions.contains(patchedVersion)) {
				throw new VersionSchemeException("Unable to roll-up because the patch provided does not apply to a prior version of the given target.");
			}
		}
		
		// Perform the roll-up of attributes, properties, and indicators
		TLFacetType patchedFacetType = ((TLFacet) patchVersion.getExtension().getExtendsEntity()).getFacetType();
		TLFacet targetFacet = FacetCodegenUtils.getFacetOfType((TLFacetOwner) majorOrMinorVersionTarget, patchedFacetType);
		
		if (targetFacet != null) {
			mergeAttributes(targetFacet, patchVersion.getAttributes(), cloner, rollupReferences);
			mergeProperties(targetFacet, patchVersion.getElements(), cloner, rollupReferences);
			mergeIndicators(targetFacet, patchVersion.getIndicators(), cloner);
		}
	}
	
	/**
	 * Adds the given library to the active project.  If no active project is assigned, this method
	 * will take no action.
	 * 
	 * @param library  the library to be added to the active project
	 */
	void addToActiveProject(TLLibrary library) {
		if (activeProject != null) {
			try {
				activeProject.getProjectManager().addUnmanagedProjectItem(library, activeProject);
				
			} catch (RepositoryException e) {
				throw new IllegalArgumentException("Unable to add the new library to the active project.", e);
			}
		}
	}
	
	/**
	 * Returns true if the given library is unmanaged (i.e. not under repository control), <u>and</u> a
	 * managaged library with the same base namespace, version, and library name has already been published
	 * to a repository.
	 * 
	 * @param library  the library to analyze
	 * @return boolean
	 */
	boolean isDuplicateOfPublishedLibrary(TLLibrary library) throws VersionSchemeException {
		boolean isDuplicate = false;
		
		if (activeProject != null) {
			ProjectManager projectManager = activeProject.getProjectManager();
			ProjectItem item = projectManager.getProjectItem( library );
			VersionScheme versionScheme = getVersionScheme( library);
			
			if ((versionScheme != null) && (item != null) && ((item.getState() == RepositoryItemState.UNMANAGED)
					|| !projectManager.isRepositoryUrl(library.getLibraryUrl()))) {
				try {
					String managedFilename = versionScheme.getDefaultFileHint( library.getNamespace(), library.getName() );
					RepositoryItem rItem = projectManager.getRepositoryManager().getRepositoryItem(
							library.getBaseNamespace(), managedFilename, library.getVersion() );
					
					isDuplicate = (rItem != null);
					
				} catch (RepositoryException e) {
					// No Error - If the content was not found, that means that a duplicate published
					// item does not exist.
				}
			}
		}
		return isDuplicate;
	}
	
	/**
	 * Returns true if the given library is a work-in-process (WIP) copy of a managed repository
	 * item.
	 * 
	 * @param library  the library to analyze
	 * @return boolean
	 */
	boolean isWorkInProcessLibrary(TLLibrary library) {
		boolean isWIP = false;
		
		if (activeProject != null) {
			ProjectManager projectManager = activeProject.getProjectManager();
			ProjectItem item = projectManager.getProjectItem( library );
			
			if (item != null) {
				isWIP = (item.getState() == RepositoryItemState.MANAGED_WIP);
			}
		}
		return isWIP;
	}
	
	/**
	 * Returns true if the given library is read-only.
	 * 
	 * @param library  the library to analyze
	 * @return boolean
	 */
	boolean isReadOnly(AbstractLibrary library) {
		ProjectItem item = (activeProject == null) ? null : activeProject.getProjectManager().getProjectItem( library );
		boolean readOnly = (item != null) ? item.isReadOnly() : library.isReadOnly();
		
		return readOnly;
	}
	
	/**
	 * Returns true if the two libraries have the same name, are assigned to the same base namespace,
	 * and use the same version scheme.
	 * 
	 * @param library1  the first library to compare
	 * @param library2  the second library to compare
	 * @return boolean
	 */
	boolean isVersionCandidateMatch(TLLibrary library1, TLLibrary library2) {
		return (library1.getName() != null) && (library1.getNamespace() != null) && (library1.getVersionScheme() != null)
				&& library1.getName().equals(library2.getName())
				&& library1.getVersionScheme().equals(library2.getVersionScheme())
				&& library1.getBaseNamespace().equals(library2.getBaseNamespace());
	}
	
	/**
	 * Returns the <code>VersionScheme</code> associated with the given versioned entity.
	 * 
	 * @param versioned  the versioned entity for which to return the scheme
	 * @return VersionScheme
	 * @throws VersionSchemeException  thrown if the entity's version scheme is not recognized
	 */
	public VersionScheme getVersionScheme(Versioned versioned) throws VersionSchemeException {
		VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
		return (versioned == null) ? null : factory.getVersionScheme( versioned.getVersionScheme() );
	}
	
	/**
	 * Returns the <code>VersionScheme</code> associated with the given library.
	 * 
	 * @param library  the library for which to return the scheme
	 * @return VersionScheme
	 * @throws VersionSchemeException  thrown if the library's version scheme is not recognized
	 */
	public VersionScheme getVersionScheme(TLLibrary library) throws VersionSchemeException {
		return (library == null) ? null :
			VersionSchemeFactory.getInstance().getVersionScheme( library.getVersionScheme() );
	}
	
	/**
	 * Returns the owning library of the given versioned entity.
	 * 
	 * @param versionedEntity  the versioned entity for which to return the library
	 * @return TLLibrary
	 * @throws IllegalArgumentException  thrown if the entity is not a member of a user-defined <code>TLLibrary</code>
	 */
	TLLibrary getOwningLibrary(Versioned versionedEntity) {
		TLLibrary library;
		
		if (versionedEntity == null) {
			library = null;
			
		} else if (versionedEntity instanceof LibraryElement) {
			AbstractLibrary lib = ((LibraryElement) versionedEntity).getOwningLibrary();
			
			if (lib instanceof TLLibrary) {
				library = (TLLibrary) lib;
				
			} else {
				throw new IllegalArgumentException("The versioned entity does not belong to a user-defined library: "
						+ versionedEntity.getClass().getSimpleName());
			}
		} else {
			throw new IllegalArgumentException("The versioned entity type is not a library member: "
					+ versionedEntity.getClass().getSimpleName());
		}
		return library;
	}
	
	/**
	 * Calculates the folder location and filename of the new library.  This method
	 * assumes that the folder location of the new file will be the same as that of the
	 * original library file.
	 * 
	 * @param newVersionLibrary  the new library version
	 * @param originalLibraryVersion  the original library version that will supply the folder location
	 * @return File
	 * @throws VersionSchemeException  thrown if the library's version scheme is not recognized
	 */
	File getDefaultLibraryFileLocation(TLLibrary newVersionLibrary, TLLibrary originalLibraryVersion) throws VersionSchemeException {
		VersionScheme versionScheme = getVersionScheme( originalLibraryVersion );
		String newVersionFilename = versionScheme.getDefaultFileHint( newVersionLibrary.getNamespace(), newVersionLibrary.getName() );
		File originalFileLocation = URLUtils.toFile( originalLibraryVersion.getLibraryUrl() );
		File targetFolder;
		
		if ((activeProject != null) && activeProject.getProjectManager().isRepositoryUrl(originalLibraryVersion.getLibraryUrl())) {
			targetFolder = activeProject.getProjectFile().getParentFile();
			
		} else {
			targetFolder = originalFileLocation.getParentFile();
		}
		return new File(targetFolder, newVersionFilename);
	}
	
	/**
	 * Validates the given library and returns the validation findings.
	 * 
	 * @param library  the library to validate
	 * @return ValidationFindings
	 */
	ValidationFindings validate(TLLibrary library) {
		return TLModelCompileValidator.validateModelElement(library, false);
	}
	
	/**
	 * Validates the given versioned entity and returns the validation findings.
	 * 
	 * @param versionedEntity  the versioned entity to validate
	 * @return ValidationFindings
	 */
	ValidationFindings validate(Versioned versionedEntity) {
		ValidationFindings findings = new ValidationFindings();
		
		if (versionedEntity instanceof TLModelElement) {
			findings.addAll( TLModelCompileValidator.validateModelElement((TLModelElement) versionedEntity, false) );
		}
		return findings;
	}
	
	/**
	 * Validates the given extension point facet and returns the validation findings.
	 * 
	 * @param xpFacet  the extension point facet entity to validate
	 * @return ValidationFindings
	 */
	ValidationFindings validate(TLExtensionPointFacet xpFacet) {
		ValidationFindings findings = new ValidationFindings();
		
		findings.addAll( TLModelCompileValidator.validateModelElement( xpFacet, false ) );
		return findings;
	}
	
	/**
	 * Constructs a new version of the given entity that is an exact copy except that the
	 * facets do not contain any attributes, elements, or indicators.
	 * 
	 * @param oldVersion  the old version from which to construct the new version instance
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @return TLBusinessObject
	 */
	TLBusinessObject newVersionInstance(TLBusinessObject oldVersion, ModelElementCloner cloner) {
		TLBusinessObject newVersion = new TLBusinessObject();
		
		newVersion.setName( oldVersion.getName() );
		newVersion.setDocumentation( cloner.clone(oldVersion.getDocumentation()) );
		
		for (TLEquivalent equivalent : oldVersion.getEquivalents()) {
			newVersion.addEquivalent( cloner.clone(equivalent) );
		}
		return newVersion;
	}
	
	/**
	 * Constructs a new version of the given entity that is an exact copy except that the
	 * facets do not contain any attributes, elements, or indicators.
	 * 
	 * @param oldVersion  the old version from which to construct the new version instance
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @return TLCoreObject
	 */
	TLCoreObject newVersionInstance(TLCoreObject oldVersion, ModelElementCloner cloner) {
		TLCoreObject newVersion = new TLCoreObject();
		
		newVersion.setName( oldVersion.getName() );
		newVersion.setDocumentation( cloner.clone(oldVersion.getDocumentation()) );
		newVersion.setSimpleFacet( cloner.clone(oldVersion.getSimpleFacet()) );
		
		for (TLEquivalent equivalent : oldVersion.getEquivalents()) {
			newVersion.addEquivalent( cloner.clone(equivalent) );
		}
		return newVersion;
	}
	
	/**
	 * Constructs a new version of the given service that is an exact copy except that it does
	 * not contain any operations.
	 * 
	 * @param oldVersion  the old service from which to construct the new instance
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @return TLService
	 */
	TLService newVersionInstance(TLService oldVersion, ModelElementCloner cloner) {
		TLService newVersion = new TLService();
		
		newVersion.setName( oldVersion.getName() );
		newVersion.setDocumentation( cloner.clone(oldVersion.getDocumentation()) );
		return newVersion;
	}
	
	/**
	 * Constructs a new version of the given entity that is an exact copy except that the
	 * facets do not contain any attributes, elements, or indicators.
	 * 
	 * @param oldVersion  the old version from which to construct the new version instance
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @return TLOperation
	 */
	TLOperation newVersionInstance(TLOperation oldVersion, ModelElementCloner cloner) {
		TLOperation newVersion = new TLOperation();
		
		newVersion.setName( oldVersion.getName() );
		newVersion.setDocumentation( cloner.clone(oldVersion.getDocumentation()) );
		
		for (TLEquivalent equivalent : oldVersion.getEquivalents()) {
			newVersion.addEquivalent( cloner.clone(equivalent) );
		}
		return newVersion;
	}
	
	/**
	 * Constructs a new version of the given entity that is an exact copy except that the
	 * facets do not contain any attributes or indicators.
	 * 
	 * @param oldVersion  the old version from which to construct the new version instance
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @return TLValueWithAttributes
	 */
	TLValueWithAttributes newVersionInstance(TLValueWithAttributes oldVersion, ModelElementCloner cloner) {
		TLValueWithAttributes newVersion = new TLValueWithAttributes();
		
		newVersion.setName( oldVersion.getName() );
		newVersion.setDocumentation( cloner.clone(oldVersion.getDocumentation()) );
		
		for (TLEquivalent equivalent : oldVersion.getEquivalents()) {
			newVersion.addEquivalent( cloner.clone(equivalent) );
		}
		for (TLExample example : oldVersion.getExamples()) {
			newVersion.addExample( cloner.clone(example) );
		}
		return newVersion;
	}
	
	/**
	 * Merges the contents of the given list into the specified target entity.  If any attributes with
	 * the same name already exist in the target, the merge item(s) will be ignored.
	 * 
	 * @param target  the entity that will receive new attributes from the merge
	 * @param attributesToMerge  the list of attributes to merge into the target
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @param rollupReferences  reference information for the libraries being rolled up
	 */
	void mergeAttributes(TLAttributeOwner target, List<TLAttribute> attributesToMerge, ModelElementCloner cloner,
			RollupReferenceInfo rollupReferences) {
		Set<String> existingAttributeNames = new HashSet<String>();
		List<TLAttribute> existingAttributes = null;
		
		if (target instanceof TLValueWithAttributes) {
			existingAttributes = PropertyCodegenUtils.getInheritedAttributes((TLValueWithAttributes) target);
		} else if (target instanceof TLFacet) {
			existingAttributes = PropertyCodegenUtils.getInheritedAttributes((TLFacet) target);
		}
		if (existingAttributes != null) {
			for (TLAttribute attr : existingAttributes) {
				existingAttributeNames.add( attr.getName() );
			}
		}
		
		for (TLAttribute sourceAttribute : attributesToMerge) {
			if (!existingAttributeNames.contains(sourceAttribute.getName())) {
				TLAttribute clone = cloner.clone( sourceAttribute );
				
				target.addAttribute( clone );
				captureRollupLibraryReference( clone, rollupReferences );
			}
		}
	}
	
	/**
	 * Merges the contents of the given list into the specified target entity.  If any properties with
	 * the same name already exist in the target (or, in the case of complex types, properties from the
	 * same substitution group), the merge item(s) will be ignored.
	 * 
	 * @param target  the entity that will receive new properties from the merge
	 * @param propertiesToMerge  the list of properties to merge into the target
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @param rollupReferences  reference information for the libraries being rolled up
	 */
	void mergeProperties(TLPropertyOwner target, List<TLProperty> propertiesToMerge, ModelElementCloner cloner,
			RollupReferenceInfo rollupReferences) {
		Set<NamedEntity> existingSubstitutionGroups = new HashSet<NamedEntity>();
		Set<String> existingPropertyNames = new HashSet<String>();
		
		if (target instanceof TLFacet) {
			List<TLProperty> existingProperties = PropertyCodegenUtils.getInheritedProperties((TLFacet) target);
			
			for (TLProperty property : existingProperties) {
				TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(target, property.getType() );
				NamedEntity substitutionGroup = PropertyCodegenUtils.getInheritanceRoot( propertyType );
				
				if (substitutionGroup != null) {
					existingSubstitutionGroups.add( substitutionGroup );
				}
				if (property.getName() != null) {
					existingPropertyNames.add( property.getName() );
				}
			}
		}
		
		for (TLProperty sourceProperty : propertiesToMerge) {
			if (!existingPropertyNames.contains(sourceProperty.getName())) {
				TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(target, sourceProperty.getType() );
				NamedEntity substitutionGroup = PropertyCodegenUtils.getInheritanceRoot( propertyType );
				
				if (!existingSubstitutionGroups.contains(substitutionGroup)) {
					TLProperty clone = cloner.clone(sourceProperty);
					
					target.addElement( clone );
					captureRollupLibraryReference( clone, rollupReferences );
				}
			}
		}
	}
	
	/**
	 * Merges the contents of the given list into the specified target entity.  If any indicators with
	 * the same name already exist in the target, the merge item(s) will be ignored.
	 * 
	 * @param target  the entity that will receive new indicators from the merge
	 * @param indicatorsToMerge  the list of indicators to merge into the target
	 * @param cloner  the cloner to use when creating copies of model elements
	 */
	void mergeIndicators(TLIndicatorOwner target, List<TLIndicator> indicatorsToMerge, ModelElementCloner cloner) {
		Set<String> existingIndicatorNames = new HashSet<String>();
		List<TLIndicator> existingIndicators = null;
		
		if (target instanceof TLValueWithAttributes) {
			existingIndicators = PropertyCodegenUtils.getInheritedIndicators((TLValueWithAttributes) target);
		} else if (target instanceof TLFacet) {
			existingIndicators = PropertyCodegenUtils.getInheritedIndicators((TLFacet) target);
		}
		if (existingIndicators != null) {
			for (TLIndicator indicator : existingIndicators) {
				existingIndicatorNames.add( indicator.getName() );
			}
		}
		
		for (TLIndicator sourceIndicator : indicatorsToMerge) {
			if (!existingIndicatorNames.contains(sourceIndicator.getName())) {
				target.addIndicator( cloner.clone(sourceIndicator) );
			}
		}
	}
	
	/**
	 * If the new entity contains a reference to another entity within its owning library, that reference is captured
	 * in the map provided so that it may be adjusted in the owning library of the new entity.
	 * 
	 * @param oldEntity  the original entity from which the new copy was created
	 * @param newEntity the new copy of the library element
	 * @param rollupReferences  reference information for the libraries being rolled up
	 */
	void captureRollupLibraryReference(LibraryElement newEntity, RollupReferenceInfo rollupReferences) {
		if (newEntity == null) {
			return; // simple case - nothing to capture
		}
		if (newEntity instanceof TLAttribute) {
			TLAttribute attribute = (TLAttribute) newEntity;
			
			if ((attribute.getType() != null)
					&& rollupReferences.isRollupLibrary( attribute.getType().getOwningLibrary() )) {
				rollupReferences.getReferences().put(attribute, attribute.getType());
			}
			
		} else if (newEntity instanceof TLProperty) {
			TLProperty element = (TLProperty) newEntity;
			
			if ((element.getType() != null)
					&& rollupReferences.isRollupLibrary( element.getType().getOwningLibrary() )) {
				rollupReferences.getReferences().put(element, element.getType());
			}
			
		} else if (newEntity instanceof TLExtension) {
			TLExtension extension = (TLExtension) newEntity;
			
			if ((extension.getExtendsEntity() != null)
					&& rollupReferences.isRollupLibrary( extension.getExtendsEntity().getOwningLibrary() )) {
				rollupReferences.getReferences().put(extension, extension.getExtendsEntity());
			}
			
		} else if (newEntity instanceof TLValueWithAttributes) {
			TLValueWithAttributes newVWA = (TLValueWithAttributes) newEntity;
			
			if ((newVWA.getParentType() != null)
					&& rollupReferences.isRollupLibrary( newVWA.getParentType().getOwningLibrary() )) {
				rollupReferences.getReferences().put(newVWA, newVWA.getParentType());
			}
			for (TLAttribute newAttribute : newVWA.getAttributes()) {
				captureRollupLibraryReference(newVWA.getAttribute(newAttribute.getName()), rollupReferences);
			}
			
		} else if (newEntity instanceof TLSimple) {
			TLSimple simple = (TLSimple) newEntity;
			
			if ((simple.getParentType() != null)
					&& rollupReferences.isRollupLibrary( simple.getParentType().getOwningLibrary() )) {
				rollupReferences.getReferences().put(simple, simple.getParentType());
			}
			
		} else if (newEntity instanceof TLSimpleFacet) {
			TLSimpleFacet simpleFacet = (TLSimpleFacet) newEntity;
			
			if ((simpleFacet.getSimpleType() != null)
					&& rollupReferences.isRollupLibrary( simpleFacet.getSimpleType().getOwningLibrary() )) {
				rollupReferences.getReferences().put(simpleFacet, simpleFacet.getSimpleType());
			}
			
		} else if (newEntity instanceof TLBusinessObject) {
			TLBusinessObject newBO = (TLBusinessObject) newEntity;
			
			captureRollupLibraryReference(newBO.getExtension(), rollupReferences);
			captureRollupLibraryReference(newBO.getIdFacet(), rollupReferences);
			captureRollupLibraryReference(newBO.getSummaryFacet(), rollupReferences);
			captureRollupLibraryReference(newBO.getDetailFacet(), rollupReferences);
			
			for (TLFacet newFacet : newBO.getCustomFacets()) {
				captureRollupLibraryReference(newFacet, rollupReferences);
			}
			for (TLFacet newFacet : newBO.getQueryFacets()) {
				captureRollupLibraryReference(newFacet, rollupReferences);
			}
			
		} else if (newEntity instanceof TLCoreObject) {
			TLCoreObject newCore = (TLCoreObject) newEntity;
			
			captureRollupLibraryReference(newCore.getExtension(), rollupReferences);
			captureRollupLibraryReference(newCore.getSimpleFacet(), rollupReferences);
			captureRollupLibraryReference(newCore.getSummaryFacet(), rollupReferences);
			captureRollupLibraryReference(newCore.getDetailFacet(), rollupReferences);
			
		} else if (newEntity instanceof TLOperation) {
			TLOperation newOp = (TLOperation) newEntity;
			
			captureRollupLibraryReference(newOp.getExtension(), rollupReferences);
			captureRollupLibraryReference(newOp.getRequest(), rollupReferences);
			captureRollupLibraryReference(newOp.getResponse(), rollupReferences);
			captureRollupLibraryReference(newOp.getNotification(), rollupReferences);
			
		} else if (newEntity instanceof TLFacet) {
			TLFacet newFacet = (TLFacet) newEntity;
			
			for (TLAttribute newAttribute : newFacet.getAttributes()) {
				captureRollupLibraryReference(newAttribute, rollupReferences);
			}
			for (TLProperty newElement : newFacet.getElements()) {
				captureRollupLibraryReference(newElement, rollupReferences);
			}
		}
	}
	
	/**
	 * If the given map contains any recorded entities that contained same-library references, the copied references
	 * are adjusted to same-named entities in the new owning library (assuming a same-name match can be found).
	 * 
	 * @param newLibrary  the new library whose internal references are to be adjusted
	 * @param rollupReferences  reference information for the libraries being rolled up
	 */
	void adjustSameLibraryReferences(TLLibrary newLibrary, RollupReferenceInfo rollupReferences) {
		ModelNavigator.navigate( newLibrary,
				new RollupReferenceAdjustmentVisitor(newLibrary, rollupReferences) );
	}
	
	/**
	 * Encapsulates all of the information needed to identify and process entity references within
	 * the same set of libraries that are being rolled up.
	 */
	protected class RollupReferenceInfo {
		
		private Map<LibraryElement,NamedEntity> rollupReferences = new HashMap<LibraryElement,NamedEntity>();
		private Set<TLLibrary> rollupLibraries = new HashSet<TLLibrary>();
		
		/**
		 * Constructor that specifies a single library that is being rolled up as part of the
		 * current operation.
		 * 
		 * @param rollupLibrary  the library being rolled up
		 */
		public RollupReferenceInfo(TLLibrary rollupLibrary, Collection<TLLibrary>... rollupLibraries) {
			this( rollupLibraries );
			
			if (rollupLibrary != null) {
				this.rollupLibraries.add( rollupLibrary );
			}
		}
		
		/**
		 * Constructor that specifies the collection(s) of libraries that are being rolled up as
		 * part of the current operation.
		 * 
		 * @param rollupLibraries  the collection(s) of libraries being rolled up
		 */
		public RollupReferenceInfo(Collection<TLLibrary>... rollupLibraries) {
			for (Collection<TLLibrary> libraries : rollupLibraries) {
				if (libraries != null) {
					this.rollupLibraries.addAll( libraries );
				}
			}
		}
		
		/**
		 * Returns the map of references that have been detected to entities contained within
		 * one of the 'rollupLibraries' members.
		 * 
		 * @return Map<LibraryElement,NamedEntity>
		 */
		public Map<LibraryElement,NamedEntity> getReferences() {
			return rollupReferences;
		}
		
		/**
		 * Returns true if the given library is a member of the rollup collection currently being processed.
		 * 
		 * @param library  the library to analyze
		 * @return boolean
		 */
		public boolean isRollupLibrary(AbstractLibrary library) {
			return rollupLibraries.contains( library );
		}
		
	}
	
	/**
	 * Visitor that adjusts references to same-named entities in the new owning library if a
	 * same-name match can be found.
	 */
	private class RollupReferenceAdjustmentVisitor extends ModelElementVisitorAdapter {
		
		private RollupReferenceInfo rollupReferences;
		private SymbolTable symbols;
		
		/**
		 * Constructor that assigns the map of roll-up references that were captured during the entity
		 * cloning process.
		 * 
		 * @param newLibrary  the new library that owns all references to be adjusted
		 * @param rollupReferences  reference information for the libraries being rolled up
		 */
		public RollupReferenceAdjustmentVisitor(TLLibrary newLibrary, RollupReferenceInfo rollupReferences) {
			this.rollupReferences = rollupReferences;
			this.symbols = SymbolTableFactory.newSymbolTableFromEntity( newLibrary );
		}
		
		/**
		 * Searches the given library for an named entity member with the same name as the original entity provided.
		 * 
		 * @param originalEntity  the original entity whose local name should be used in the search
		 * @param library  the library to search
		 * @return NamedEntity
		 */
		private NamedEntity findSameNameEntity(NamedEntity originalEntity, TLLibrary library) {
			Object entity = symbols.getEntity( library.getNamespace(), originalEntity.getLocalName() );
			NamedEntity sameNameEntity = null;
			
			if (entity instanceof NamedEntity) {
				NamedEntity namedEntity = (NamedEntity) entity;
				
				if (namedEntity.getOwningLibrary() == library) {
					sameNameEntity = namedEntity;
				}
			}
			return sameNameEntity;
		}
		
		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			if (rollupReferences.getReferences().containsKey(simple)) {
				NamedEntity sameNameEntity = findSameNameEntity(
						rollupReferences.getReferences().get(simple), (TLLibrary) simple.getOwningLibrary());
				
				if (sameNameEntity instanceof TLAttributeType) {
					simple.setParentType( (TLAttributeType) sameNameEntity );
				}
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
		 */
		@Override
		public boolean visitValueWithAttributes(TLValueWithAttributes vwa) {
			if (rollupReferences.getReferences().containsKey(vwa)) {
				NamedEntity sameNameEntity = findSameNameEntity(
						rollupReferences.getReferences().get(vwa), (TLLibrary) vwa.getOwningLibrary());
				
				if (sameNameEntity instanceof TLAttributeType) {
					vwa.setParentType( (TLAttributeType) sameNameEntity );
				}
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
		 */
		@Override
		public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
			if (rollupReferences.getReferences().containsKey(simpleFacet)) {
				NamedEntity sameNameEntity = findSameNameEntity(
						rollupReferences.getReferences().get(simpleFacet), (TLLibrary) simpleFacet.getOwningLibrary());
				
				if (sameNameEntity instanceof TLAttributeType) {
					simpleFacet.setSimpleType( (TLAttributeType) sameNameEntity );
				}
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
		 */
		@Override
		public boolean visitAttribute(TLAttribute attribute) {
			if (rollupReferences.getReferences().containsKey(attribute)) {
				NamedEntity sameNameEntity = findSameNameEntity(
						rollupReferences.getReferences().get(attribute), (TLLibrary) attribute.getOwningLibrary());
				
				if (sameNameEntity instanceof TLAttributeType) {
					attribute.setType( (TLAttributeType) sameNameEntity );
				}
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
		 */
		@Override
		public boolean visitElement(TLProperty element) {
			if (rollupReferences.getReferences().containsKey(element)) {
				NamedEntity sameNameEntity = findSameNameEntity(
						rollupReferences.getReferences().get(element), (TLLibrary) element.getOwningLibrary());
				
				if (sameNameEntity instanceof TLPropertyType) {
					element.setType( (TLPropertyType) sameNameEntity );
				}
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
		 */
		@Override
		public boolean visitExtension(TLExtension extension) {
			if (rollupReferences.getReferences().containsKey(extension)) {
				NamedEntity sameNameEntity = findSameNameEntity(
						rollupReferences.getReferences().get(extension), (TLLibrary) extension.getOwningLibrary());
				
				if (sameNameEntity != null) {
					extension.setExtendsEntity( sameNameEntity );
				}
			}
			return true;
		}
		
	}
	
	/**
	 * Implements the <code>Versioned</code> interface for the purpose of comparing the versions of libraries
	 * and/or repository items.
	 */
	private class VersionWrapper implements Versioned {
		
		private TLLibrary library;
		private RepositoryItem item;
		private String versionScheme;
		
		/**
		 * Constructor used to wrap a <code>TLLibrary</code> instance.
		 * 
		 * @param library  the library instance to wrap
		 */
		public VersionWrapper(TLLibrary library) {
			this.library = library;
		}
		
		/**
		 * Constructor used to wrap a <code>RepositoryItem</code> instance.
		 * 
		 * @param item  the repository item instance to wrap
		 * @param versionScheme  the version scheme to use for the repository item
		 */
		public VersionWrapper(RepositoryItem item, String versionScheme) {
			this.versionScheme = versionScheme;
			this.item = item;
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getVersion()
		 */
		@Override
		public String getVersion() {
			return (library != null) ? library.getVersion() : item.getVersion();
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getVersionScheme()
		 */
		@Override
		public String getVersionScheme() {
			return (library != null) ? library.getVersionScheme() : versionScheme;
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getNamespace()
		 */
		@Override
		public String getNamespace() {
			return (library != null) ? library.getNamespace() : item.getNamespace();
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getBaseNamespace()
		 */
		@Override
		public String getBaseNamespace() {
			return (library != null) ? library.getBaseNamespace() : item.getBaseNamespace();
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#isLaterVersion(org.opentravel.schemacompiler.version.Versioned)
		 */
		@Override
		public boolean isLaterVersion(Versioned otherVersionedItem) {
			return ((library == null) || (otherVersionedItem == null)) ?
					false : library.isLaterVersion(otherVersionedItem.getOwningLibrary());
		}

		/**
		 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
		 */
		@Override
		public String getLocalName() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
		 */
		@Override
		public AbstractLibrary getOwningLibrary() {
			return (library != null) ? library : null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
		 */
		@Override
		public TLModel getOwningModel() {
			return (library != null) ? library.getOwningModel() : null;
		}

		/**
		 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
		 */
		@Override
		public String getValidationIdentity() {
			return (library != null) ? library.getValidationIdentity() : null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement()
		 */
		@Override
		public LibraryElement cloneElement() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement(org.opentravel.schemacompiler.model.AbstractLibrary)
		 */
		@Override
		public LibraryElement cloneElement(AbstractLibrary namingContext) {
			return null;
		}
		
	}
	
}
