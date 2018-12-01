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
package org.opentravel.schemacompiler.version;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLFolder;
import org.opentravel.schemacompiler.model.TLFolderOwner;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler;
import org.opentravel.schemacompiler.version.handlers.VersionHandler;
import org.opentravel.schemacompiler.version.handlers.VersionHandlerFactory;
import org.opentravel.schemacompiler.version.handlers.VersionHandlerMergeUtils;

/**
 * Base class for version helpers used to retrieve related versions of a library or entity from a
 * model or to create new versions.
 * 
 * @author S. Livezey
 */
public abstract class AbstractVersionHelper {

    private VersionHandlerFactory handlerFactory = new VersionHandlerFactory();
	private Project activeProject;

    /**
     * Default constructor. NOTE: When working in an environment where a <code>ProjectManager</code>
     * is being used, the other constructor should be used to assign the active project for the
     * helper.
     */
    public AbstractVersionHelper() {
    }

    /**
     * Constructor that assigns the active project for an application's <code>ProjectManager</code>.
     * If new libraries are created or opened by this helper, they will be automatically added to
     * the active project that is passed to this constructor.
     * 
     * @param activeProject
     *            the active project to which new libraries will be assigned
     */
    public AbstractVersionHelper(Project activeProject) {
        this.activeProject = activeProject;
    }

    /**
     * Returns the version chain for the given library. The resulting list includes all dependent
     * versions starting with the one provided and ending with the major version that started the
     * chain.
     * 
     * @param library
     *            the library for which to return the version chain
     * @return List<TLLibrary>
     * @throws VersionSchemeException
     *             thrown if the library's version scheme is not recognized
     * @throws IllegalStateException
     *             thrown if the given library is not associated with a TLModel instance
     */
    public List<TLLibrary> getVersionChain(TLLibrary library) throws VersionSchemeException {
        VersionScheme versionScheme = getVersionScheme(library);
        List<TLLibrary> versionChain = new ArrayList<>();
        String currentVersionNS = library.getNamespace();

        if (library.getOwningModel() == null) {
            throw new IllegalStateException(
                    "The given library is not associated with an owning model instance: "
                            + library.getLibraryUrl().toExternalForm());
        }

        // Find all prior patch versions
        while (versionScheme.isPatchVersion(currentVersionNS)) {
            TLLibrary currentVersionLib = findLibrary(currentVersionNS, library.getName(),
                    library.getOwningModel());
            String currentVersion = versionScheme.getVersionIdentifier(currentVersionNS);

            if (currentVersionLib != null) {
                versionChain.add(currentVersionLib);
            }
            currentVersionNS = versionScheme.setVersionIdentifier(currentVersionNS,
                    versionScheme.decrementPatchLevel(currentVersion));
        }

        // Find all prior minor versions
        while (!versionScheme.isMajorVersion(currentVersionNS)) {
            TLLibrary currentVersionLib = findLibrary(currentVersionNS, library.getName(),
                    library.getOwningModel());
            String currentVersion = versionScheme.getVersionIdentifier(currentVersionNS);

            if (currentVersionLib != null) {
                versionChain.add(currentVersionLib);
            }
            currentVersionNS = versionScheme.setVersionIdentifier(currentVersionNS,
                    versionScheme.decrementMinorVersion(currentVersion));
        }

        // Add the major version to the end of the chain
        TLLibrary majorVersionLib = findLibrary(currentVersionNS, library.getName(),
                library.getOwningModel());

        if (majorVersionLib != null) {
            versionChain.add(majorVersionLib);
        }

        return versionChain;
    }
    
    /**
     * Returns the major version namespace of the given library.
     * 
     * @param library  the library for which to return the major version namespace
     * @return String
     * @throws VersionSchemeException
     *             thrown if the library's version scheme is not recognized
     */
    public String getMajorVersionNamespace(AbstractLibrary library) throws VersionSchemeException {
        VersionScheme versionScheme = getVersionScheme( library );
        return versionScheme.getMajorVersionNamespace( library.getNamespace() );
    }
    
    /**
     * Returns the major version namespace of the given versioned entity.
     * 
     * @param versionedEntity  the entity for which to return the major version namespace
     * @return String
     * @throws VersionSchemeException
     *             thrown if the library's version scheme is not recognized
     */
    public String getMajorVersionNamespace(Versioned versionedEntity) throws VersionSchemeException {
    	return (versionedEntity == null) ? null : getMajorVersionNamespace( versionedEntity.getOwningLibrary() );
    }

    /**
     * Returns the library from the model with the specified name and namespace, or null if no such
     * library exists.
     * 
     * @param namespace
     *            the namespace of the library to retrieve
     * @param libraryName
     *            the name of the library to retrieve
     * @param model
     *            the model from which to retrieve the library
     * @return TLLibrary
     */
    private TLLibrary findLibrary(String namespace, String libraryName, TLModel model) {
        AbstractLibrary library = model.getLibrary(namespace, libraryName);
        return (library instanceof TLLibrary) ? (TLLibrary) library : null;
    }
    
    /**
     * Returns a version handler for the given versioned entity.
     * 
     * @param versionedEntity  the versioned entity for which to return a handler
     * @return VersionHandler<V>
     */
    <V extends Versioned> VersionHandler<V> getVersionHandler(V versionedEntity) {
    	return handlerFactory.getHandler( versionedEntity );
    }

    /**
     * Returns a list of all libraries that are later minor versions of the given library.
     * 
     * @param library
     *            the library for which to return minor versions
     * @return List<TLLibrary>
     * @throws VersionSchemeException
     *             thrown if the entity's version scheme is not recognized
     * @throws IllegalStateException
     *             thrown if the given library is not associated with a TLModel instance
     */
    List<TLLibrary> getLaterMinorVersions(TLLibrary library) throws VersionSchemeException {
        List<TLLibrary> minorVersionList = new ArrayList<>();

        if (library.getOwningModel() == null) {
            throw new IllegalStateException(
                    "The given library is not associated with an owning model instance: "
                            + library.getLibraryUrl().toExternalForm());
        }

        if (library.getNamespace() != null) {
            VersionScheme versionScheme = getVersionScheme(library);

            for (TLLibrary lib : library.getOwningModel().getUserDefinedLibraries()) {
                if ((lib == library) || !isVersionCandidateMatch(lib, library)
                        || versionScheme.isPatchVersion(lib.getNamespace())) {
                    continue;
                }
                List<String> versionChain = versionScheme.getMajorVersionChain(lib.getNamespace());

                // If our original library's namespace occurs later in the version chain
                // than our current one, it must be a later minor version
                if (versionChain.indexOf(library.getNamespace()) > 0) {
                    minorVersionList.add(lib);
                }
            }
            Collections.sort(minorVersionList, new LibraryVersionComparator(versionScheme, true));
        }
        return minorVersionList;
    }

    /**
     * Returns a list of all libraries that are later patch versions of the given library.
     * 
     * @param library
     *            the library for which to return patch versions
     * @return List<TLLibrary>
     * @throws VersionSchemeException
     *             thrown if the entity's version scheme is not recognized
     * @throws IllegalStateException
     *             thrown if the given library is not associated with a TLModel instance
     */
    List<TLLibrary> getLaterPatchVersions(TLLibrary library) throws VersionSchemeException {
        List<TLLibrary> patchVersionList = new ArrayList<>();

        if ((library != null) && (library.getNamespace() != null)) {
            VersionScheme versionScheme = getVersionScheme(library);
            String minorVersionNS = null;

            if (library.getOwningModel() == null) {
                throw new IllegalStateException(
                        "The given library is not associated with an owning model instance: "
                                + library.getLibraryUrl().toExternalForm());
            }

            if (!versionScheme.isPatchVersion(library.getNamespace())) {
                minorVersionNS = library.getNamespace();
            }

            for (TLLibrary lib : library.getOwningModel().getUserDefinedLibraries()) {
                if ((lib == library) || !isVersionCandidateMatch(lib, library)
                        || !versionScheme.isPatchVersion(lib.getNamespace())) {
                    continue;
                }
                if (minorVersionNS != null) { // original library was a major or minor version
                    String adjustedVersion = versionScheme.decrementMinorVersion(versionScheme
                            .incrementMinorVersion(lib.getVersion()));
                    String adjustedLibNS = versionScheme.setVersionIdentifier(lib.getNamespace(),
                            adjustedVersion);

                    if (adjustedLibNS.equals(minorVersionNS)) {
                        patchVersionList.add(lib);
                    }
                } else { // original library was a patch version
                    List<String> versionChain = versionScheme.getMajorVersionChain(lib
                            .getNamespace());

                    // If our original library's namespace occurs later in the version chain
                    // than our current one, it must be a later minor version
                    if (versionChain.indexOf(library.getNamespace()) > 0) {
                        patchVersionList.add(lib);
                    }
                }
            }
            Collections.sort(patchVersionList, new LibraryVersionComparator(versionScheme, true));
        }
        return patchVersionList;
    }

    /**
     * If the given <code>TLExtensionPointFacet</code> is a version patch, the
     * <code>Versioned</code> entity that is being patched will be returned.
     * 
     * @param xpFacet
     *            the extension point facet for which to return the associated patched entity
     * @return Versioned
     * @throws VersionSchemeException
     *             thrown if the entity's version scheme is not recognized
     */
    Versioned getPatchedVersion(TLExtensionPointFacet xpFacet) throws VersionSchemeException {
        NamedEntity extendedEntity = (xpFacet.getExtension() == null) ? null : xpFacet
                .getExtension().getExtendsEntity();
        Versioned patchedVersion = null;
        
        if (extendedEntity instanceof TLFacet) {
        	TLFacetOwner extendedFacetOwner = ((TLFacet) extendedEntity).getOwningEntity();
            
            if (extendedFacetOwner instanceof Versioned) {
                VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                        xpFacet.getVersionScheme());
                List<String> versionChain = versionScheme.getMajorVersionChain(xpFacet.getNamespace());

                if (versionChain.indexOf(extendedFacetOwner.getNamespace()) > 0) {
                    patchedVersion = (Versioned) extendedFacetOwner;
                }
            }
        }
        return patchedVersion;
    }

    /**
     * Searches all known remote repositories for later minor versions of the given library. If
     * later minor versions are identified that are not currently available in the model, they are
     * automatically loaded. The newly-loaded libraries are added to the list of existing minor
     * versions, and their project items are returned in the resulting list.
     * 
     * @param library
     *            the library for which to import later minor versions
     * @param existingLaterMinorVersions
     *            the list of later minor versions that already existed within the model
     * @return List<ProjectItem>
     * @throws VersionSchemeException
     *             thrown if an error occurs while attempting to import new versions of the library
     */
    List<ProjectItem> importLaterMinorVersionsFromRepository(TLLibrary library,
            List<TLLibrary> existingLaterMinorVersions) throws VersionSchemeException {
        List<ProjectItem> importedVersions = new ArrayList<>();
        VersionScheme versionScheme = getVersionScheme(library);

        if (activeProject != null) {
            List<RepositoryItem> managedVersions = findAllManagedVersionsNotLoaded(library,
                    versionScheme);
            String libraryMajorVersion = versionScheme.getMajorVersion(library.getVersion());
            Comparator<Versioned> versionComparator = versionScheme.getComparator(true);
            VersionWrapper libraryWrapper = new VersionWrapper(library);
            List<RepositoryItem> laterMinorVersions = new ArrayList<>();

            // Add all of the new versions as long as they
            // 1) have the same major version,
            // 2) are a minor version of the library, and
            // 3) have a version identifier greater than that of our original library
            for (RepositoryItem itemVersion : managedVersions) {
                String itemMajorVersion = versionScheme.getMajorVersion(itemVersion.getVersion());

                if (!libraryMajorVersion.equals(itemMajorVersion)
                        || !versionScheme.isMinorVersion(itemVersion.getNamespace())) {
                    continue;
                }
                VersionWrapper itemWrapper = new VersionWrapper(itemVersion,
                        library.getVersionScheme());

                if (versionComparator.compare(libraryWrapper, itemWrapper) < 0) {
                    laterMinorVersions.add(itemVersion);
                }
            }

            // Load all of the repository items and re-sort the original list of later minor
            // versions
            if (!laterMinorVersions.isEmpty()) {
                for (RepositoryItem item : laterMinorVersions) {
                    try {
                        ProjectItem pItem = activeProject.getProjectManager()
                                .addManagedProjectItem(item, activeProject);

                        if (pItem != null) {
                            importedVersions.add(pItem);
                            existingLaterMinorVersions.add((TLLibrary) pItem.getContent());
                        }
                    } catch (Exception e) {
                        // No action - just skip this repository item and move on to the next one
                    }
                }
                Collections.sort(existingLaterMinorVersions, new LibraryVersionComparator(
                        versionScheme, true));
            }
        }
        return importedVersions;
    }

    /**
     * Searches all known remote repositories for later patch versions of the given library. If
     * later patch versions are identified that are not currently available in the model, they are
     * automatically loaded. The newly-loaded libraries are added to the list of existing patch
     * versions, and their project items are returned in the resulting list.
     * 
     * @param library
     *            the library for which to import later patch versions
     * @param existingLaterPatchVersions
     *            the list of later patch versions that already existed within the model
     * @return List<ProjectItem>
     * @throws VersionSchemeException
     *             thrown if an error occurs while attempting to import new versions of the library
     */
    List<ProjectItem> importLaterPatchVersionsFromRepository(TLLibrary library,
            List<TLLibrary> existingLaterPatchVersions) throws VersionSchemeException {
        List<ProjectItem> importedVersions = new ArrayList<>();
        VersionScheme versionScheme = getVersionScheme(library);

        if (activeProject != null) {
            List<RepositoryItem> managedVersions = findAllManagedVersionsNotLoaded(library,
                    versionScheme);
            String libraryMajorVersion = versionScheme.getMajorVersion(library.getVersion());
            String libraryMinorVersion = versionScheme.getMinorVersion(library.getVersion());
            Comparator<Versioned> versionComparator = versionScheme.getComparator(true);
            VersionWrapper libraryWrapper = new VersionWrapper(library);
            List<RepositoryItem> laterPatchVersions = new ArrayList<>();

            // Add all of the new versions as long as they
            // 1) have the same major and minor version,
            // 2) are a patch version of the library, and
            // 3) have a version identifier greater than that of our original library
            for (RepositoryItem itemVersion : managedVersions) {
                String itemMajorVersion = versionScheme.getMajorVersion(itemVersion.getVersion());
                String itemMinorVersion = versionScheme.getMinorVersion(itemVersion.getVersion());

                if (!libraryMajorVersion.equals(itemMajorVersion)
                        || !libraryMinorVersion.equals(itemMinorVersion)
                        || !versionScheme.isPatchVersion(itemVersion.getNamespace())) {
                    continue;
                }
                VersionWrapper itemWrapper = new VersionWrapper(itemVersion,
                        library.getVersionScheme());

                if (versionComparator.compare(libraryWrapper, itemWrapper) < 0) {
                    laterPatchVersions.add(itemVersion);
                }
            }

            // Load all of the repository items and re-sort the original list of later patch
            // versions
            if (!laterPatchVersions.isEmpty()) {
                for (RepositoryItem item : laterPatchVersions) {
                    try {
                        ProjectItem pItem = activeProject.getProjectManager()
                                .addManagedProjectItem(item, activeProject);

                        if (pItem != null) {
                            importedVersions.add(pItem);
                            existingLaterPatchVersions.add((TLLibrary) pItem.getContent());
                        }
                    } catch (Exception e) {
                        // No action - just skip this repository item and move on to the next one
                    }
                }
                Collections.sort(existingLaterPatchVersions, new LibraryVersionComparator(
                        versionScheme, true));
            }
        }
        return importedVersions;
    }

    /**
     * Returns a list of repository items that represent all versions of the given library that
     * exist in a remote repository that have not been loaded into the active project.
     * 
     * @param library
     *            the library for which to find non-loaded versions
     * @param versionScheme
     *            the version scheme of the library
     * @return List<RepositoryItem>
     */
    private List<RepositoryItem> findAllManagedVersionsNotLoaded(TLLibrary library,
            VersionScheme versionScheme) {
        RepositoryManager repositoryManager = activeProject.getProjectManager()
                .getRepositoryManager();
        List<Repository> repositoriesToSearch = new ArrayList<>();
        List<RepositoryItem> newVersions = new ArrayList<>();
        List<String> itemKeys = new ArrayList<>();

        repositoriesToSearch.addAll(repositoryManager.listRemoteRepositories());
        repositoriesToSearch.add(repositoryManager); // the repository manager instance represents
                                                     // the local repository

        for (Repository repository : repositoriesToSearch) {
            try {
                String baseNamespace = library.getBaseNamespace();

                if (baseNamespace == null) {
                    continue;
                }
                for (RepositoryItem item : repository.listItems(baseNamespace, TLLibraryStatus.DRAFT, false)) {
                    String itemKey = getRepositoryItemKey(item);

                    if (!itemKeys.contains(itemKey)
                            && item.getBaseNamespace().equals(baseNamespace)) {
                        String targetFilename = versionScheme.getDefaultFileHint(
                                item.getNamespace(), library.getName());

                        if (targetFilename.equals(item.getFilename())) {
                            URL itemUrl = repositoryManager.getContentLocation(item);

                            if (activeProject.getProjectManager().getModel().getLibrary(itemUrl) == null) {
                                newVersions.add(item);
                            }
                        }
                        itemKeys.add(itemKey);
                    }
                }
            } catch (RepositoryException e) {
                // No action - skip this repository and move on if not available
            }
        }
        return newVersions;
    }

    /**
     * Returns a unique key to identify the given repository item. This is necessary just in case
     * multiple copies of the same item exist in different remote repositories (unlikely, but
     * possible).
     * 
     * @param item
     *            the repository item for which to return a key
     * @return String
     */
    private String getRepositoryItemKey(RepositoryItem item) {
        StringBuilder key = new StringBuilder();

        key.append(item.getNamespace()).append(":");
        key.append(item.getFilename()).append(":");
        key.append(item.getVersion());
        return key.toString();
    }

    /**
     * Removes each of the imported project items from the current active project.
     * 
     * @param importedVersions
     *            the list of imported version items to be removed
     */
    void removeImportedVersions(List<ProjectItem> importedVersions) {
        if ((activeProject != null) && (importedVersions != null)) {
            for (ProjectItem item : importedVersions) {
                activeProject.remove(item);
            }
        }
    }

    /**
     * Rolls up the contents of the patch library into the minor version library provided.
     * 
     * @param minorVersionLibrary
     *            the minor version libarary that will receive any new/modified rolled-up entities
     * @param patchLibrary
     *            the patch library whose contents are to be rolled up
     * @param referenceHandler
     *            handler that stores reference information for the libraries being rolled up
     * @throws VersionSchemeException
     *             thrown if the library's version scheme is not recognized
     */
    void rollupPatchLibrary(TLLibrary minorVersionLibrary, TLLibrary patchLibrary,
            RollupReferenceHandler referenceHandler) throws VersionSchemeException {
        for (TLContext context : patchLibrary.getContexts()) {
            if (minorVersionLibrary.getContext(context.getContextId()) == null) {
            	ModelElementCloner cloner = getCloner( patchLibrary.getOwningModel() );
                minorVersionLibrary.addContext(cloner.clone(context));
            }
        }
        for (TLExtensionPointFacet patch : patchLibrary.getExtensionPointFacetTypes()) {
            Versioned patchedEntity = getPatchedVersion(patch);

            if (patchedEntity != null) {
                try {
                	VersionHandler<Versioned> handler = getVersionHandler( patchedEntity );
                	Versioned newEntityVersion = handler.createOrRetrieveNewVersion( patchedEntity, minorVersionLibrary );
                	
                    rollupPatchVersion(newEntityVersion, patch, referenceHandler, true);

                } catch (ValidationException e) {
                    // Not possible since we indicated validation should be skipped
                }
            }
        }
    }

    /**
     * Constructs a new empty <code>TLLibrary</code> instance using the given one as a basis. The
     * only fields that are not initialized on the new library are it's namespace, prefix, and URL
     * assignments. In addition to those initial field values, all of the contexts in the given
     * library are cloned for the new instance.
     * 
     * @param library
     *            the library to be used as a basis for the new one
     * @return TLLibrary
     */
    TLLibrary createNewLibrary(TLLibrary library) {
        TLLibrary newLibrary = new TLLibrary();

        newLibrary.setName(library.getName());
        newLibrary.setComments(library.getComments());
        newLibrary.setStatus(TLLibraryStatus.DRAFT);

        for (TLContext context : library.getContexts()) {
            newLibrary.addContext((TLContext) context.cloneElement());
        }
        return newLibrary;
    }

    /**
     * Rolls up the contents of the given patch version into the new major/minor version. A roll-up
     * is essentially a merge of the attributes, properties, and indicators from the facets of the
     * patch version.
     * 
     * @param majorOrMinorVersionTarget
     *            the major/minor version of the entity that will receive any rolled up items
     * @param patchVersion
     *            the patch version of the entity whose items will be the source of the roll-up
     * @param referenceHandler
     *            handler that stores reference information for the libraries being rolled up
     * @param skipValidation
     *            internal flag indicating whether entity validation should be skipped
     * @throws VersionSchemeException
     *             thrown if the given major version is not a later major version and/or the patch
     *             version is not a prior patch version
     * @throws ValidationException
     *             thrown if the rollup cannot be performed because one or more validation errors
     *             exist in either the source or target entity
     */
    void rollupPatchVersion(Versioned majorOrMinorVersionTarget, TLExtensionPointFacet patchVersion,
    		RollupReferenceHandler referenceHandler, boolean skipValidation)
            throws VersionSchemeException, ValidationException {
        // Perform validation checks before rolling up
        if (!skipValidation) {
            ValidationFindings findings = new ValidationFindings();
            findings.addAll(validate(majorOrMinorVersionTarget));
            findings.addAll(validate(patchVersion));

            if (findings.hasFinding(FindingType.ERROR)) {
                throw new ValidationException(
                        "Unable to roll-up because the target and/or patch versions contains errors.",
                        findings);
            }
            if ((majorOrMinorVersionTarget.getVersionScheme() == null)
                    || !majorOrMinorVersionTarget.getVersionScheme().equals(
                            patchVersion.getVersionScheme())) {
                throw new VersionSchemeException(
                        "The target and patch versions to be rolled-up are not assigned to the same version scheme.");
            }
            if ((majorOrMinorVersionTarget.getBaseNamespace() == null)
                    || !majorOrMinorVersionTarget.getBaseNamespace().equals(
                            patchVersion.getBaseNamespace())) {
                throw new VersionSchemeException(
                        "The target and patch versions to be rolled-up are not assigned to the same base namespace.");
            }
            if (isReadOnly(majorOrMinorVersionTarget.getOwningLibrary())) {
                throw new VersionSchemeException(
                        "Unable to roll-up the requested patch because the target version is in a read-only library.");
            }

            // Verify that the patch is, in fact, a patch of one of its prior minor versions
            VersionHandler<Versioned> handler = getVersionHandler( majorOrMinorVersionTarget );
            List<Versioned> priorMajorOrMinorVersions = handler.getAllVersionExtensions( majorOrMinorVersionTarget );
            Versioned patchedVersion = getPatchedVersion(patchVersion);

            if ((patchedVersion == null) || !priorMajorOrMinorVersions.contains(patchedVersion)) {
                throw new VersionSchemeException(
                        "Unable to roll-up because the patch provided does not apply to a prior version of the given target.");
            }
        }

        // Perform the roll-up of attributes, properties, and indicators
        NamedEntity extendedEntity = patchVersion.getExtension().getExtendsEntity();
        TLMemberFieldOwner targetFacet = null;
        
        if (extendedEntity instanceof TLFacet) {
            TLFacetType patchedFacetType = ((TLFacet) extendedEntity).getFacetType();
            
            // Skip contextual facets since their versions will be created independently
            // of their owners
            if (!patchedFacetType.isContextual()) {
                targetFacet = FacetCodegenUtils.getFacetOfType(
                		(TLFacetOwner) majorOrMinorVersionTarget, patchedFacetType);
                
            } else {
            	if (!OTM16Upgrade.otm16Enabled) {
                	TLContextualFacet patchedFacet = (TLContextualFacet) extendedEntity;
                	
                    targetFacet = FacetCodegenUtils.getFacetOfType((TLFacetOwner) majorOrMinorVersionTarget,
                    		patchedFacetType, patchedFacet.getName());
                	
                	// If a matching contextual facet does not yet exist, create one automatically
                    if (targetFacet == null) {
                		TLContextualFacet contextualFacet = new TLContextualFacet();
                		
                		contextualFacet.setName( patchedFacet.getName() );
                		
                    	if (majorOrMinorVersionTarget instanceof TLBusinessObject) {
                    		if (patchedFacet.getFacetType() == TLFacetType.CUSTOM) {
                    			((TLBusinessObject) majorOrMinorVersionTarget).addCustomFacet( contextualFacet );
                        		targetFacet = contextualFacet;
                        		
                    		} else {
                    			((TLBusinessObject) majorOrMinorVersionTarget).addQueryFacet( contextualFacet );
                        		targetFacet = contextualFacet;
                    		}
                    		
                    	} else if (majorOrMinorVersionTarget instanceof TLChoiceObject) {
                			((TLChoiceObject) majorOrMinorVersionTarget).addChoiceFacet( contextualFacet );
                    		targetFacet = contextualFacet;
                    		

                    	} else {
                    		// At this time, only business objects have contextual facets
                    	}
                    }
            	}
            }
        }

        if (targetFacet != null) {
        	VersionHandlerMergeUtils mergeUtils = new VersionHandlerMergeUtils( handlerFactory );
        	
        	mergeUtils.mergeAttributes((TLAttributeOwner) targetFacet, patchVersion.getAttributes(), referenceHandler);
        	mergeUtils.mergeProperties((TLPropertyOwner) targetFacet, patchVersion.getElements(), referenceHandler);
        	mergeUtils.mergeIndicators((TLIndicatorOwner) targetFacet, patchVersion.getIndicators());
        }
    }
    
    /**
     * Replicates all folders that exist in the source library for the target library.  Only
     * the folders themselves are replicated by this routine, no members are assigned within
     * the structure.
     * 
     * @param sourceLibrary  the source library from which to replicate the folder structure
     * @param targetLibrary  the library for which the new folder structure will be created
     */
    void copyLibraryFolders(TLLibrary sourceLibrary, TLLibrary targetLibrary) {
    	copyFolders( sourceLibrary, targetLibrary, targetLibrary );
    }
    
    /**
     * Recursive routine to replicate all folders that exist in the source folder owner for
     * the target owner.
     * 
     * @param sourceOwner  the source folder owner from which to replicate the structure
     * @param targetOwner  the folder owner for which the new structure will be created
     * @param targetLibrary  the library within which the new folder structure will be created
     */
    private void copyFolders(TLFolderOwner sourceOwner, TLFolderOwner targetOwner, TLLibrary targetLibrary) {
    	for (TLFolder sourceFolder : sourceOwner.getFolders()) {
    		TLFolder targetFolder = targetOwner.getFolder( sourceFolder.getName() );
    		
    		if (targetFolder == null) {
    			targetFolder = new TLFolder( sourceFolder.getName(), targetLibrary );
    			targetOwner.addFolder( targetFolder );
    		}
    		copyFolders( sourceFolder, targetFolder, targetLibrary );
    	}
    }
    
    /**
     * Assigns the target entity to the same folder path in its owning library as the
     * source entity occupies in its owner.  If the source entity is not assigned to a
     * folder or the target folder does not exist, this method will return without
     * action.  If the target folder is successfully assigned, this method will return
     * true; false otherwise.
     * 
     * @param sourceEntity  the source entity from which to derive the target folder path
     * @param targetEntity  the target entity to assign to a folder
     */
    boolean assignTargetFolder(LibraryMember sourceEntity, LibraryMember targetEntity) {
    	List<String> sourcePath = new ArrayList<>();
    	List<String> targetPath = new ArrayList<>();
    	boolean result = false;
    	
    	findFolderPath( sourceEntity, (TLLibrary) sourceEntity.getOwningLibrary(), sourcePath );
    	findFolderPath( targetEntity, (TLLibrary) targetEntity.getOwningLibrary(), targetPath );
    	
    	if (!sourcePath.isEmpty() && targetPath.isEmpty()) {
    		TLFolderOwner folderOwner = (TLFolderOwner) targetEntity.getOwningLibrary();
    		TLFolder targetFolder = null;
    		
    		for (String folderName : sourcePath) {
    			folderOwner = targetFolder = folderOwner.getFolder( folderName );
    			if (targetFolder == null) break; // give up - the target folder does not exist
    		}
    		
    		if (targetFolder != null) {
    			targetFolder.addEntity( targetEntity );
    			result = true;
    		}
    	}
    	return result;
    }
    
    /**
     * Returns the folder path for the entity within its owning library.  If the
     * entity is not assigned to a folder, the folder path list will remain empty
     * after this method call.
     * 
     * @param entity  the entity for which to retreive the folder path
     * @return List<String>
     */
    private boolean findFolderPath(LibraryMember entity, TLFolderOwner folderOwner, List<String> folderPath) {
    	boolean result = false;
    	
    	for (TLFolder folder : folderOwner.getFolders()) {
    		if (folder.getEntities().contains( entity ) || findFolderPath( entity, folder, folderPath )) {
    			folderPath.add( 0, folder.getName() );
    			result = true;
    			break;
    		}
    	}
    	return result;
    }
    
    /**
     * Adds the given library to the active project. If no active project is assigned, this method
     * will take no action.
     * 
     * @param library
     *            the library to be added to the active project
     */
    void addToActiveProject(TLLibrary library) {
        if (activeProject != null) {
            try {
                activeProject.getProjectManager().addUnmanagedProjectItem(library, activeProject);

            } catch (RepositoryException e) {
                throw new IllegalArgumentException(
                        "Unable to add the new library to the active project.", e);
            }
        }
    }

    /**
     * Returns true if the given library is unmanaged (i.e. not under repository control),
     * <u>and</u> a managaged library with the same base namespace, version, and library name has
     * already been published to a repository.
     * 
     * @param library
     *            the library to analyze
     * @return boolean
     */
    boolean isDuplicateOfPublishedLibrary(TLLibrary library) throws VersionSchemeException {
        boolean isDuplicate = false;

        if (activeProject != null) {
            ProjectManager projectManager = activeProject.getProjectManager();
            ProjectItem item = projectManager.getProjectItem(library);
            VersionScheme versionScheme = getVersionScheme(library);

            if ((versionScheme != null)
                    && (item != null)
                    && ((item.getState() == RepositoryItemState.UNMANAGED) || !projectManager
                            .isRepositoryUrl(library.getLibraryUrl()))) {
                try {
                    String managedFilename = versionScheme.getDefaultFileHint(
                            library.getNamespace(), library.getName());
                    RepositoryItem rItem = projectManager.getRepositoryManager().getRepositoryItem(
                            library.getBaseNamespace(), managedFilename, library.getVersion());

                    isDuplicate = (rItem != null);

                } catch (RepositoryException e) {
                    // No Error - If the content was not found, that means that a duplicate
                    // published
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
     * @param library
     *            the library to analyze
     * @return boolean
     */
    boolean isWorkInProcessLibrary(TLLibrary library) {
        boolean isWIP = false;

        if (activeProject != null) {
            ProjectManager projectManager = activeProject.getProjectManager();
            ProjectItem item = projectManager.getProjectItem(library);

            if (item != null) {
                isWIP = (item.getState() == RepositoryItemState.MANAGED_WIP);
            }
        }
        return isWIP;
    }

    /**
     * Returns true if the given library is read-only.
     * 
     * @param library
     *            the library to analyze
     * @return boolean
     */
    boolean isReadOnly(AbstractLibrary library) {
        ProjectItem item = (activeProject == null) ? null : activeProject.getProjectManager()
                .getProjectItem(library);
        
        return (item != null) ? item.isReadOnly() : library.isReadOnly();
    }

    /**
     * Returns true if the two libraries have the same name, are assigned to the same base
     * namespace, and use the same version scheme.
     * 
     * @param library1
     *            the first library to compare
     * @param library2
     *            the second library to compare
     * @return boolean
     */
    boolean isVersionCandidateMatch(TLLibrary library1, TLLibrary library2) {
        return (library1.getName() != null) && (library1.getNamespace() != null)
                && (library1.getVersionScheme() != null)
                && library1.getName().equals(library2.getName())
                && library1.getVersionScheme().equals(library2.getVersionScheme())
                && library1.getBaseNamespace().equals(library2.getBaseNamespace());
    }

    /**
     * Returns the <code>VersionScheme</code> associated with the given versioned entity.
     * 
     * @param versioned
     *            the versioned entity for which to return the scheme
     * @return VersionScheme
     * @throws VersionSchemeException
     *             thrown if the entity's version scheme is not recognized
     */
    public VersionScheme getVersionScheme(Versioned versioned) throws VersionSchemeException {
        VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
        return (versioned == null) ? null : factory.getVersionScheme(versioned.getVersionScheme());
    }

    /**
     * Returns the <code>VersionScheme</code> associated with the given library.
     * 
     * @param library
     *            the library for which to return the scheme
     * @return VersionScheme
     * @throws VersionSchemeException
     *             thrown if the library's version scheme is not recognized
     */
    public VersionScheme getVersionScheme(AbstractLibrary library) throws VersionSchemeException {
        return (library == null) ? null : VersionSchemeFactory.getInstance().getVersionScheme(
                library.getVersionScheme());
    }

    /**
     * Returns the owning library of the given versioned entity.
     * 
     * @param versionedEntity
     *            the versioned entity for which to return the library
     * @return TLLibrary
     * @throws IllegalArgumentException
     *             thrown if the entity is not a member of a user-defined <code>TLLibrary</code>
     */
    TLLibrary getOwningLibrary(Versioned versionedEntity) {
        TLLibrary library;

        if (versionedEntity == null) {
            library = null;

        } else if (versionedEntity instanceof LibraryElement) {
            AbstractLibrary lib = versionedEntity.getOwningLibrary();

            if (lib instanceof TLLibrary) {
                library = (TLLibrary) lib;

            } else {
                throw new IllegalArgumentException(
                        "The versioned entity does not belong to a user-defined library: "
                                + versionedEntity.getClass().getSimpleName());
            }
        } else {
            throw new IllegalArgumentException(
                    "The versioned entity type is not a library member: "
                            + versionedEntity.getClass().getSimpleName());
        }
        return library;
    }

    /**
     * Calculates the folder location and filename of the new library. This method assumes that the
     * folder location of the new file will be the same as that of the original library file.
     * 
     * @param newVersionLibrary
     *            the new library version
     * @param originalLibraryVersion
     *            the original library version that will supply the folder location
     * @return File
     * @throws VersionSchemeException
     *             thrown if the library's version scheme is not recognized
     */
    File getDefaultLibraryFileLocation(TLLibrary newVersionLibrary, TLLibrary originalLibraryVersion)
            throws VersionSchemeException {
        VersionScheme versionScheme = getVersionScheme(originalLibraryVersion);
        String newVersionFilename = versionScheme.getDefaultFileHint(
                newVersionLibrary.getNamespace(), newVersionLibrary.getName());
        File originalFileLocation = URLUtils.toFile(originalLibraryVersion.getLibraryUrl());
        File targetFolder;

        if ((activeProject != null)
                && activeProject.getProjectManager().isRepositoryUrl(
                        originalLibraryVersion.getLibraryUrl())) {
            targetFolder = activeProject.getProjectFile().getParentFile();

        } else {
            targetFolder = originalFileLocation.getParentFile();
        }
        return new File(targetFolder, newVersionFilename);
    }

    /**
     * Validates the given library and returns the validation findings.
     * 
     * @param library
     *            the library to validate
     * @return ValidationFindings
     */
    ValidationFindings validate(TLLibrary library) {
        return TLModelCompileValidator.validateModelElement(library, false);
    }

    /**
     * Validates the given versioned entity and returns the validation findings.
     * 
     * @param versionedEntity
     *            the versioned entity to validate
     * @return ValidationFindings
     */
    ValidationFindings validate(Versioned versionedEntity) {
        ValidationFindings findings = new ValidationFindings();

        if (versionedEntity instanceof TLModelElement) {
            findings.addAll(TLModelCompileValidator.validateModelElement(
                    (TLModelElement) versionedEntity, false));
        }
        return findings;
    }

    /**
     * Validates the given extension point facet and returns the validation findings.
     * 
     * @param xpFacet
     *            the extension point facet entity to validate
     * @return ValidationFindings
     */
    ValidationFindings validate(TLExtensionPointFacet xpFacet) {
        ValidationFindings findings = new ValidationFindings();

        findings.addAll(TLModelCompileValidator.validateModelElement(xpFacet, false));
        return findings;
    }
    
    /**
     * Returns a cloner that can produced deep copies of entities from the given model.
     * 
     * @param model  the model instance for which to return a cloner
     * @return ModelElementCloner
     */
    ModelElementCloner getCloner(TLModel model) {
    	return handlerFactory.getCloner( model );
    }

    /**
     * Implements the <code>Versioned</code> interface for the purpose of comparing the versions of
     * libraries and/or repository items.
     */
    private class VersionWrapper implements Versioned {

        private TLLibrary library;
        private RepositoryItem item;
        private String versionScheme;

        /**
         * Constructor used to wrap a <code>TLLibrary</code> instance.
         * 
         * @param library
         *            the library instance to wrap
         */
        public VersionWrapper(TLLibrary library) {
            this.library = library;
        }

        /**
         * Constructor used to wrap a <code>RepositoryItem</code> instance.
         * 
         * @param item
         *            the repository item instance to wrap
         * @param versionScheme
         *            the version scheme to use for the repository item
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
            return (library != null) && (otherVersionedItem != null) &&
            		library.isLaterVersion(otherVersionedItem.getOwningLibrary());
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
		 * @see org.opentravel.schemacompiler.model.ModelElement#addListener(org.opentravel.schemacompiler.event.ModelElementListener)
		 */
		@Override
		public void addListener(ModelElementListener listener) {
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#removeListener(org.opentravel.schemacompiler.event.ModelElementListener)
		 */
		@Override
		public void removeListener(ModelElementListener listener) {
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#getListeners()
		 */
		@Override
		public Collection<ModelElementListener> getListeners() {
			return Collections.emptyList();
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
