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

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.ic.ImportManagementIntegrityChecker;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler;
import org.opentravel.schemacompiler.version.handlers.VersionHandler;
import org.opentravel.schemacompiler.version.handlers.VersionHandlerFactory;
import org.opentravel.schemacompiler.version.handlers.VersionHandlerMergeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper methods used to construct new major versions of <code>TLLibrary</code> instances.
 * 
 * @author S. Livezey
 */
public final class MajorVersionHelper extends AbstractVersionHelper {

    private static final Logger log = LoggerFactory.getLogger( MajorVersionHelper.class );

    /**
     * Default constructor. NOTE: When working in an environment where a <code>ProjectManager</code> is being used, the
     * other constructor should be used to assign the active project for the helper.
     */
    public MajorVersionHelper() {}

    /**
     * Constructor that assigns the active project for an application's <code>ProjectManager</code>. If new libraries
     * are created by this helper, they will be automatically added to the active project that is passed to this
     * constructor.
     * 
     * @param activeProject the active project to which new libraries will be assigned
     */
    public MajorVersionHelper(Project activeProject) {
        super( activeProject );
    }

    /**
     * Returns the major version that began the library's minor version chain. If no such major version exists in the
     * library's model, this method will return null. If the library passed to this method is, itself, a major version,
     * the original library will be returned.
     * 
     * @param library the library for which to retrieve the major version
     * @return TLLibrary
     * @throws VersionSchemeException thrown if the library's version scheme is not recognized
     */
    public TLLibrary getMajorVersion(TLLibrary library) throws VersionSchemeException {
        VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme( library.getVersionScheme() );
        TLLibrary majorVersion = null;

        if (library.getNamespace() != null) {
            if (versionScheme.isMajorVersion( library.getNamespace() )) {
                majorVersion = library; // simple case: we already have the major version

            } else if (library.getOwningModel() != null) {
                List<String> versionChain = versionScheme.getMajorVersionChain( library.getNamespace() );
                String majorVersionNS =
                    versionChain.isEmpty() ? library.getNamespace() : versionChain.get( versionChain.size() - 1 );

                for (TLLibrary lib : library.getOwningModel().getUserDefinedLibraries()) {
                    if ((lib != library) && isVersionCandidateMatch( lib, library )
                        && majorVersionNS.equals( lib.getNamespace() )) {
                        majorVersion = lib;
                        break;
                    }
                }
            }
        }
        return majorVersion;
    }

    /**
     * Creates a new major version of the given library. The new version will contain a roll-up of all members from the
     * given library's major version chain. It does not matter whether the given library is the latest member of its
     * version chain; this method will locate all chain members and incorporate them into the roll-up.
     * 
     * <p>
     * NOTE: The library that is returned by this method is saved to the specified location and added to the owning
     * model of the original library.
     * 
     * @param library the library from which to construct a new major version
     * @return TLLibrary
     * @throws VersionSchemeException thrown if a later major version already exists for the given library
     * @throws ValidationException thrown if one of more of the libraries to be rolled-up contains validation errors
     * @throws LibrarySaveException thrown if the new version of the library cannot be saved to the local disk
     */
    public TLLibrary createNewMajorVersion(TLLibrary library)
        throws VersionSchemeException, ValidationException, LibrarySaveException {
        return createNewMajorVersion( library, null );
    }

    /**
     * Creates a new major version of the given library. The new version will contain a roll-up of all members from the
     * given library's major version chain. It does not matter whether the given library is the latest member of its
     * version chain; this method will locate all chain members and incorporate them into the roll-up.
     * 
     * <p>
     * NOTE: The library that is returned by this method is saved to a default location and added to the owning model of
     * the original library.
     * 
     * @param library the library from which to construct a new major version
     * @param libraryFile the file name and location where the new library is to be saved (null for default location)
     * @return TLLibrary
     * @throws VersionSchemeException thrown if a later major version already exists for the given library
     * @throws ValidationException thrown if one of more of the libraries to be rolled-up contains validation errors
     * @throws LibrarySaveException thrown if the new version of the library cannot be saved to the local disk
     */
    public TLLibrary createNewMajorVersion(TLLibrary library, File libraryFile)
        throws VersionSchemeException, ValidationException, LibrarySaveException {
        List<ProjectItem> importedVersions = new ArrayList<>();
        try {
            TLLibrary previousMajorVersion = getMajorVersion( library );

            // First, do some preliminary error checking
            if (isWorkInProcessLibrary( library )) {
                throw new VersionSchemeException(
                    "New versions cannot be created from work-in-process libraries (commit and unlock to proceed)." );
            }
            if (isDuplicateOfPublishedLibrary( library )) {
                throw new VersionSchemeException( "Unable to create the new version"
                    + " - a duplicate of this unmanaged library has already been published." );
            }
            if (previousMajorVersion == null) {
                throw new VersionSchemeException(
                    "Unable to find corresponding major version for library: " + library.getName() );
            }

            // Load all of the related libraries required to build the new version
            VersionScheme versionScheme =
                VersionSchemeFactory.getInstance().getVersionScheme( library.getVersionScheme() );
            List<TLLibrary> minorVersionLibraries = getLaterMinorVersions( previousMajorVersion );
            importedVersions
                .addAll( importLaterMinorVersionsFromRepository( previousMajorVersion, minorVersionLibraries ) );
            minorVersionLibraries.add( 0, previousMajorVersion );
            TLLibrary lastMinorVersion =
                minorVersionLibraries.isEmpty() ? null : minorVersionLibraries.get( minorVersionLibraries.size() - 1 );
            List<TLLibrary> patchLibraries =
                (lastMinorVersion == null) ? new ArrayList<>() : getLaterPatchVersions( lastMinorVersion );
            importedVersions.addAll( importLaterPatchVersionsFromRepository( lastMinorVersion, patchLibraries ) );

            // Validate all of the libraries to be rolled up into the new major version
            ValidationFindings findings = new ValidationFindings();

            for (TLLibrary lib : minorVersionLibraries) {
                findings.addAll( validate( lib ) );
            }
            for (TLLibrary lib : patchLibraries) {
                findings.addAll( validate( lib ) );
            }
            if (findings.hasFinding( FindingType.ERROR )) {
                throw new ValidationException(
                    "Unable to roll-up because the target and/or patch versions contains errors.", findings );
            }

            // Create the new (empty) library version
            String newLibraryVersion = versionScheme
                .incrementMajorVersion( versionScheme.getVersionIdentifier( previousMajorVersion.getNamespace() ) );
            TLLibrary newLibrary = createNewLibrary( previousMajorVersion );

            newLibrary.setNamespace( versionScheme.setVersionIdentifier( library.getNamespace(), newLibraryVersion ) );
            newLibrary.setPrefix( versionScheme.getPrefix( library.getPrefix(), newLibraryVersion ) );

            if (libraryFile == null) {
                libraryFile = getDefaultLibraryFileLocation( newLibrary, library );
            }
            if (libraryFile.exists()) {
                throw new LibrarySaveException(
                    "A file already exists at the new library location: " + libraryFile.getAbsolutePath() );
            }
            newLibrary.setLibraryUrl( URLUtils.toURL( libraryFile ) );
            library.getOwningModel().addLibrary( newLibrary );

            // For any minor versions or patches that exist in the prior minor version, create
            // roll-up entities in the new library version.
            RollupReferenceHandler referenceHandler =
                new RollupReferenceHandler( minorVersionLibraries, patchLibraries );

            for (TLLibrary minorVersionLibrary : minorVersionLibraries) {
                rollupMinorVersionLibrary( newLibrary, minorVersionLibrary, referenceHandler );
            }

            // Roll up any patches that exist for the last minor version
            for (TLLibrary patchLibrary : patchLibraries) {
                rollupPatchLibrary( newLibrary, patchLibrary, referenceHandler );
            }
            referenceHandler.adjustSameLibraryReferences( newLibrary );
            ImportManagementIntegrityChecker.verifyReferencedLibraries( newLibrary );

            new LibraryModelSaver().saveLibrary( newLibrary );
            addToActiveProject( newLibrary );
            return newLibrary;

        } finally {
            removeImportedVersions( importedVersions );
        }
    }

    /**
     * Rolls up the contents of the given minor version into the new major version. A roll-up is essentially a merge of
     * the attributes, properties, and indicators from the facets of the minor version.
     * 
     * @param majorVersionTarget the major version of the entity that will receive any rolled up items
     * @param minorVersion the minor version of the entity whose items will be the source of the roll-up
     * @param <V> the type of the versioned object to be rolled up
     * @throws VersionSchemeException thrown if the given major version is not a later major version and/or the minor
     *         version is not a prior minor version
     * @throws ValidationException thrown if the rollup cannot be performed because one or more validation errors exist
     *         in either the source or target entity
     */
    public <V extends Versioned> void rollupMinorVersion(V majorVersionTarget, V minorVersion)
        throws VersionSchemeException, ValidationException {
        if (!(majorVersionTarget.getOwningLibrary() instanceof TLLibrary)
            || !(minorVersion.getOwningLibrary() instanceof TLLibrary)) {
            throw new VersionSchemeException(
                "Only entities contained within user-defined libraries can be rolled up." );
        }
        validateMinorVersionRollup( majorVersionTarget, minorVersion );

        RollupReferenceHandler referenceHandler =
            new RollupReferenceHandler( (TLLibrary) minorVersion.getOwningLibrary() );
        TLLibrary targetLibrary = (TLLibrary) majorVersionTarget.getOwningLibrary();
        VersionHandler<V> handler = getVersionHandler( minorVersion );

        handler.rollupMinorVersion( minorVersion, majorVersionTarget, referenceHandler );
        ImportManagementIntegrityChecker.verifyReferencedLibraries( targetLibrary );
    }

    /**
     * Rolls up the contents of the given patch version into the new major version. A roll-up is essentially a merge of
     * the attributes, properties, and indicators from the facets of the patch version.
     * 
     * @param majorVersionTarget the major version of the entity that will receive any rolled up items
     * @param patchVersion the patch version of the entity whose items will be the source of the roll-up
     * @throws VersionSchemeException thrown if the given major version is not a later major version and/or the patch
     *         version is not a prior patch version
     * @throws ValidationException thrown if the rollup cannot be performed because one or more validation errors exist
     *         in either the source or target entity
     */
    public void rollupPatchVersion(Versioned majorVersionTarget, TLExtensionPointFacet patchVersion)
        throws VersionSchemeException, ValidationException {
        if (!(majorVersionTarget.getOwningLibrary() instanceof TLLibrary)
            || !(patchVersion.getOwningLibrary() instanceof TLLibrary)) {
            throw new VersionSchemeException(
                "Only entities contained within user-defined libraries can be rolled up." );
        }
        RollupReferenceHandler referenceHandler =
            new RollupReferenceHandler( (TLLibrary) patchVersion.getOwningLibrary() );
        TLLibrary targetLibrary = (TLLibrary) majorVersionTarget.getOwningLibrary();

        rollupPatchVersion( majorVersionTarget, patchVersion, referenceHandler, false );
        referenceHandler.adjustSameLibraryReferences( targetLibrary );
        ImportManagementIntegrityChecker.verifyReferencedLibraries( targetLibrary );
    }

    /**
     * Performs a compiler validation check on both entity versions to ensure that a minor version rollup can be
     * executed successfully. In addition to the normal validation checks, a number of other checks are performed to
     * ensure that the version schemes of the two entities are properly aligned.
     * 
     * @param majorVersionTarget the major version of the entity that will receive any rolled up items
     * @param minorVersion the minor version of the entity whose items will be the source of the roll-up
     * @throws VersionSchemeException thrown if the given major version is not a later major version and/or the patch
     *         version is not a prior patch version
     * @throws ValidationException thrown if the rollup cannot be performed because one or more validation errors exist
     *         in either the major or minor version entity
     */
    private <V extends Versioned> void validateMinorVersionRollup(V majorVersionTarget, V minorVersion)
        throws VersionSchemeException, ValidationException {
        ValidationFindings findings = new ValidationFindings();
        findings.addAll( validate( majorVersionTarget ) );
        findings.addAll( validate( minorVersion ) );

        if (findings.hasFinding( FindingType.ERROR )) {
            throw new ValidationException(
                "Unable to roll-up because the target and/or minor versions contains errors.", findings );
        }
        if ((majorVersionTarget.getVersionScheme() == null)
            || !majorVersionTarget.getVersionScheme().equals( minorVersion.getVersionScheme() )) {
            throw new VersionSchemeException(
                "The target and minor versions to be rolled-up are not assigned to the same version scheme." );
        }
        if ((majorVersionTarget.getBaseNamespace() == null)
            || !majorVersionTarget.getBaseNamespace().equals( minorVersion.getBaseNamespace() )) {
            throw new VersionSchemeException(
                "The target and minor versions to be rolled-up are not assigned to the same base namespace." );
        }
        if (!majorVersionTarget.getLocalName().equals( minorVersion.getLocalName() )) {
            throw new VersionSchemeException( "The minor version provided is not a later version"
                + " of the target entity because it does not have the same name." );
        }
        if (!majorVersionTarget.getClass().equals( minorVersion.getClass() )) {
            throw new VersionSchemeException( "The minor version provided is not a later version"
                + " of the target entity because they are different entity types." );
        }
        if (isReadOnly( majorVersionTarget.getOwningLibrary() )) {
            throw new VersionSchemeException(
                "Unable to roll-up the requested minor version because the target version is in a read-only library." );
        }

        // Verify that the minor is, in fact, an extension of one of the previous major versions
        VersionScheme versionScheme =
            VersionSchemeFactory.getInstance().getVersionScheme( majorVersionTarget.getVersionScheme() );
        Comparator<Versioned> versionComparator = versionScheme.getComparator( true );

        if (!versionScheme.isMajorVersion( majorVersionTarget.getNamespace() )
            || !versionScheme.isMinorVersion( minorVersion.getNamespace() )
            || (versionComparator.compare( majorVersionTarget, minorVersion ) < 0)) {
            throw new VersionSchemeException(
                "The minor version provided is not a later version of the target entity." );
        }
    }

    /**
     * Rolls up the contents of the minor version library into the major version library provided.
     * 
     * @param majorVersionLibrary the major version libarary that will receive any new/modified rolled-up entities
     * @param minorVersionLibrary the minor version library whose contents are to be rolled up
     * @param cloner the cloner to use when creating copies of model elements
     * @param referenceHandler handler that stores reference information for the libraries being rolled up
     * @throws VersionSchemeException thrown if the library's version scheme is not recognized
     */
    private void rollupMinorVersionLibrary(TLLibrary majorVersionLibrary, TLLibrary minorVersionLibrary,
        RollupReferenceHandler referenceHandler) throws VersionSchemeException {
        // Start by rolling up the contexts and the folder structure
        for (TLContext context : minorVersionLibrary.getContexts()) {
            if (majorVersionLibrary.getContext( context.getContextId() ) == null) {
                ModelElementCloner cloner = getCloner( minorVersionLibrary.getOwningModel() );
                majorVersionLibrary.addContext( cloner.clone( context ) );
            }
        }
        copyLibraryFolders( minorVersionLibrary, majorVersionLibrary );

        // Collect the list of all versioned entities from the minor version library
        List<TLContextualFacet> nonLocalFacets = new ArrayList<>();
        List<Versioned> minorVersionList = new ArrayList<>();

        collectVersionedEntities( minorVersionLibrary, nonLocalFacets, minorVersionList );
        rollupNonLocalFacets( nonLocalFacets, majorVersionLibrary, referenceHandler );

        // Roll up all of the entities we just collected to the major version library
        for (Versioned minorVersion : minorVersionList) {
            Versioned majorVersion = getVersionHandler( minorVersion ).rollupMinorVersion( minorVersion,
                majorVersionLibrary, referenceHandler );

            if (majorVersion instanceof LibraryMember) {
                assignTargetFolder( (LibraryMember) minorVersion, (LibraryMember) majorVersion );
            }
        }
    }

    /**
     * Collect all versioned entities from the minor version library.
     * 
     * @param minorVersionLibrary the minor version library from which to collect entities
     * @param nonLocalFacets the list of non-local contextual facets being created
     * @param minorVersionList the list of other minor version entities
     */
    private void collectVersionedEntities(TLLibrary minorVersionLibrary, List<TLContextualFacet> nonLocalFacets,
        List<Versioned> minorVersionList) {
        for (LibraryMember member : minorVersionLibrary.getNamedMembers()) {
            if (member instanceof Versioned) {
                minorVersionList.add( (Versioned) member );

            } else if (member instanceof TLContextualFacet) {
                TLContextualFacet ctxFacet = (TLContextualFacet) member;

                if (!ctxFacet.isLocalFacet()) {
                    nonLocalFacets.add( ctxFacet );
                }

            }
        }
        if (minorVersionLibrary.getService() != null) {
            for (TLOperation operation : minorVersionLibrary.getService().getOperations()) {
                minorVersionList.add( operation );
            }
        }
    }

    /**
     * Returns the list all non-local contextual facets that are assigned to the business and choice objects of the
     * given library. The resulting list will contain non-local facets that are directly owned by the library's
     * busines/choice objects as well as any non-local child facets of those contextual facets.
     * 
     * @param library the library for which to return non-local facets of business/choice objects
     * @return List&lt;TLContextualFacet&gt;
     */
    public List<TLContextualFacet> getNonLocalEntityFacets(TLLibrary library) {
        List<TLContextualFacet> nonLocalFacets = new ArrayList<>();

        for (TLBusinessObject entity : library.getBusinessObjectTypes()) {
            findNonLocalFacets( entity, TLFacetType.CUSTOM, library, nonLocalFacets, new HashSet<TLFacetOwner>() );
            findNonLocalFacets( entity, TLFacetType.QUERY, library, nonLocalFacets, new HashSet<TLFacetOwner>() );
            findNonLocalFacets( entity, TLFacetType.UPDATE, library, nonLocalFacets, new HashSet<TLFacetOwner>() );
        }
        for (TLChoiceObject entity : library.getChoiceObjectTypes()) {
            findNonLocalFacets( entity, TLFacetType.CHOICE, library, nonLocalFacets, new HashSet<TLFacetOwner>() );
        }
        return nonLocalFacets;
    }

    /**
     * Recursive routine that searches for all non-local contextual facets within the hierarchy of the given facet
     * owner.
     * 
     * @param owner the owner for which to find non-local contextual facets
     * @param facetType the type of non-local contextual facets to retrieve
     * @param localLibrary the library that should be considered the "local" owner
     * @param localFacets the list of local contextual facets that have been collected
     * @param visitedOwners the collection of facet owners that have already been visited
     */
    private void findNonLocalFacets(TLFacetOwner owner, TLFacetType facetType, AbstractLibrary localLibrary,
        List<TLContextualFacet> localFacets, Set<TLFacetOwner> visitedOwners) {
        if (!visitedOwners.contains( owner )) {
            visitedOwners.add( owner );

            for (TLFacet f : FacetCodegenUtils.getAllFacetsOfType( owner, facetType )) {
                if (f instanceof TLContextualFacet) {
                    TLContextualFacet facet = (TLContextualFacet) f;

                    if (facet.getOwningLibrary() != localLibrary) {
                        localFacets.add( facet );
                    }
                    findNonLocalFacets( facet, facetType, localLibrary, localFacets, visitedOwners );
                }
            }
        }
    }

    /**
     * Rolls up the given list of non-local contextual facets.
     * 
     * @param sourceFacets the source facets to be rolled up
     * @param targetLibrary the target owning library for the contextual facet
     * @return List&lt;TLContextualFacet&gt;
     */
    private List<TLContextualFacet> rollupNonLocalFacets(List<TLContextualFacet> sourceFacets, TLLibrary targetLibrary,
        RollupReferenceHandler referenceHandler) {
        List<TLContextualFacet> targetFacets = new ArrayList<>();
        List<TLContextualFacet> remainingFacets = new ArrayList<>( sourceFacets );
        int originalFacetCount = -1;

        while (!remainingFacets.isEmpty() && (remainingFacets.size() != originalFacetCount)) {

            // We don't know what order the source facets came in, so some of the owners may not yet
            // exist. For this reason, we need to continue iterating through the list until we do
            // not find a facet that can be processed
            originalFacetCount = remainingFacets.size();

            for (TLContextualFacet sourceFacet : sourceFacets) {
                TLFacetOwner sourceFacetOwner = sourceFacet.getOwningEntity();
                TLContextualFacet targetFacet =
                    rollupNonLocalFacet( sourceFacet, sourceFacetOwner, targetLibrary, referenceHandler );

                targetFacets.add( targetFacet );
                remainingFacets.remove( sourceFacet );
            }
        }
        return targetFacets;
    }

    /**
     * For non-local contextual facets, this method will roll-up all fields from the original facet's minor version
     * chain and assign the new facet to the target owner.
     * 
     * @param sourceFacet the original (source) facet to be rolled-up
     * @param targetOwner the owner of the new rolled-up facet that will be created
     * @param targetLibrary the target library that will own the new rolled-up facet
     * @param referenceHandler handler that stores reference information for the libraries being rolled up
     * @return TLContextualFacet
     */
    private TLContextualFacet rollupNonLocalFacet(TLContextualFacet sourceFacet, TLFacetOwner targetOwner,
        TLLibrary targetLibrary, RollupReferenceHandler referenceHandler) {
        VersionHandlerMergeUtils mergeUtils = new VersionHandlerMergeUtils( new VersionHandlerFactory() );
        Map<String,TLFacet> targetFacets = new HashMap<>();
        Map<String,TLFacet> sourceFacets = new HashMap<>();
        TLContextualFacet targetFacet = targetLibrary.getContextualFacetType( sourceFacet.getLocalName() );

        if (targetFacet == null) {
            targetFacet = new TLContextualFacet();
            targetFacet.setName( sourceFacet.getName() );
            targetFacet.setFacetType( sourceFacet.getFacetType() );
            addFacetToOwner( targetOwner, targetFacet );
            targetLibrary.addNamedMember( targetFacet );
        }
        mergeUtils.addToIdentityFacetMap( targetFacet, targetFacets );
        mergeUtils.addToIdentityFacetMap( sourceFacet, sourceFacets );
        mergeUtils.mergeFacets( targetFacets, sourceFacets, referenceHandler );
        return targetFacet;
    }

    /**
     * Adds the given facet to its owner based on the owner's class and the facet type.
     * 
     * @param owner the owner to which the facet will be added
     * @param facet the facet to be added
     */
    private void addFacetToOwner(TLFacetOwner owner, TLContextualFacet facet) {
        boolean facetAdded = true;

        if (owner instanceof TLBusinessObject) {
            TLBusinessObject bo = (TLBusinessObject) owner;

            switch (facet.getFacetType()) {
                case CUSTOM:
                    bo.addCustomFacet( facet );
                    break;
                case QUERY:
                    bo.addQueryFacet( facet );
                    break;
                case UPDATE:
                    bo.addUpdateFacet( facet );
                    break;
                default:
                    facetAdded = false;
            }

        } else if (owner instanceof TLChoiceObject) {
            if (facet.getFacetType() == TLFacetType.CHOICE) {
                ((TLChoiceObject) owner).addChoiceFacet( facet );

            } else {
                facetAdded = false;
            }

        } else if (owner instanceof TLContextualFacet) {
            ((TLContextualFacet) owner).addChildFacet( facet );

        } else {
            facetAdded = false;
        }

        if (!facetAdded) {
            log.warn( "Unable to add: Incompatible facet owner for the contextual facet type." );
        }
    }

}
