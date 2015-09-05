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
import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.ic.ImportManagementIntegrityChecker;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Helper methods used to construct new minor versions of <code>TLLibrary</code> instances and their
 * entity members.
 * 
 * @author S. Livezey
 */
public final class MinorVersionHelper extends AbstractVersionHelper {

    /**
     * Default constructor. NOTE: When working in an environment where a <code>ProjectManager</code>
     * is being used, the other constructor should be used to assign the active project for the
     * helper.
     */
    public MinorVersionHelper() {
    }

    /**
     * Constructor that assigns the active project for an application's <code>ProjectManager</code>.
     * If new libraries are created by this helper, they will be automatically added to the active
     * project that is passed to this constructor.
     * 
     * @param activeProject
     *            the active project to which new libraries will be assigned
     */
    public MinorVersionHelper(Project activeProject) {
        super(activeProject);
    }

    /**
     * @see org.opentravel.schemacompiler.version.AbstractVersionHelper#getLaterMinorVersions(org.opentravel.schemacompiler.model.TLLibrary)
     */
    @Override
    public List<TLLibrary> getLaterMinorVersions(TLLibrary library) throws VersionSchemeException {
        return super.getLaterMinorVersions(library);
    }

    /**
     * Returns the minor version that immediately precedes the version of the given library. If no
     * prior version exists in the library's model, this method will return null.
     * 
     * @param library
     *            the library for which to retrieve the previous minor version
     * @return TLLibrary
     * @throws VersionSchemeException
     *             thrown if the library's version scheme is not recognized
     */
    public TLLibrary getPriorMinorVersion(TLLibrary library) throws VersionSchemeException {
        VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                library.getVersionScheme());
        String baseNamespace = (library == null) ? null : library.getBaseNamespace();
        TLLibrary priorVersion = null;

        if ((library != null) && (baseNamespace != null) && (library.getOwningModel() != null)
                && !versionScheme.isMajorVersion(library.getNamespace())) {
            String priorMinorVersion;

            // Identify the version (and namespace) of the library we are looking for
            if (versionScheme.isPatchVersion(library.getNamespace())) {
                priorMinorVersion = library.getVersion();
                String priorMinorVersionNS;

                do {
                    priorMinorVersion = versionScheme.decrementPatchLevel(priorMinorVersion);
                    priorMinorVersionNS = versionScheme.setVersionIdentifier(
                            library.getNamespace(), priorMinorVersion);

                } while (versionScheme.isPatchVersion(priorMinorVersionNS));
            } else {
                priorMinorVersion = versionScheme.decrementMinorVersion(library.getVersion());
            }

            // Search the model for a matching library
            for (TLLibrary lib : library.getOwningModel().getUserDefinedLibraries()) {
                if ((lib != library) && isVersionCandidateMatch(lib, library)
                        && baseNamespace.equals(lib.getBaseNamespace())
                        && priorMinorVersion.equals(lib.getVersion())) {
                    priorVersion = lib;
                    break;
                }
            }
        }
        return priorVersion;
    }

    /**
     * @see org.opentravel.schemacompiler.version.AbstractVersionHelper#getPriorVersionExtension(org.opentravel.schemacompiler.version.Versioned)
     */
    @Override
    public <V extends Versioned> V getPriorVersionExtension(V versionedEntity)
            throws VersionSchemeException {
        return super.getPriorVersionExtension(versionedEntity);
    }

    /**
     * @see org.opentravel.schemacompiler.version.AbstractVersionHelper#getAllPriorVersionExtensions(org.opentravel.schemacompiler.version.Versioned)
     */
    @Override
    public <V extends Versioned> List<V> getAllPriorVersionExtensions(V versionedEntity)
            throws VersionSchemeException {
        return super.getAllPriorVersionExtensions(versionedEntity);
    }

    /**
     * Returns a list of all minor versions that extend the given versioned entity.
     * 
     * @param versionedEntity
     *            the versioned entity for which to return minor version extensions
     * @return List<V>
     * @throws VersionSchemeException
     *             thrown if the entity's version scheme is not recognized
     */
    @SuppressWarnings("unchecked")
    public <V extends Versioned> List<V> getLaterMinorVersions(V versionedEntity)
            throws VersionSchemeException {
        List<V> versionExtensions = new ArrayList<V>();

        if (versionedEntity instanceof LibraryElement) {
            LibraryElement versionedMember = (LibraryElement) versionedEntity;

            if (versionedMember.getOwningLibrary() instanceof TLLibrary) {
                List<TLLibrary> minorVersionLibraries = getLaterMinorVersions((TLLibrary) versionedMember
                        .getOwningLibrary());

                for (TLLibrary minorVersionLib : minorVersionLibraries) {
                    NamedEntity minorVersionEntity = null;

                    if (versionedEntity instanceof TLOperation) {
                        TLService minorVersionService = minorVersionLib.getService();

                        if (minorVersionService != null) {
                            minorVersionEntity = minorVersionService
                                    .getOperation(((TLOperation) versionedEntity).getName());
                        }
                    } else {
                        minorVersionEntity = minorVersionLib.getNamedMember(versionedEntity
                                .getLocalName());
                    }

                    // If the names and classes of the two entities match, we have a minor version
                    if ((minorVersionEntity != null)
                            && minorVersionEntity.getClass().equals(versionedEntity.getClass())) {
                        versionExtensions.add((V) minorVersionEntity);
                    }
                }
            }
        }
        return versionExtensions;
    }

    /**
     * Returns the libraries in the current model that are eligible to accept new minor versions for
     * the given versioned entity. In order to be returned by this method, an eligible library must
     * also be writeable by an editor application.
     * 
     * @param versionedEntity
     *            the versioned entity for which to return eligible minor version libraries
     * @return List<TLLibrary>
     * @throws VersionSchemeException
     */
    public List<TLLibrary> getEligibleMinorVersionTargets(Versioned versionedEntity)
            throws VersionSchemeException {
        TLLibrary owningLibrary = getOwningLibrary(versionedEntity);
        List<TLLibrary> minorVersionLibraries = getLaterMinorVersions(owningLibrary);
        List<TLLibrary> eligibleLibraries = new ArrayList<TLLibrary>();

        // Search the patch libraries, and return only those who do not yet contain a patch for the
        // given facet
        for (TLLibrary minorVersionLibrary : minorVersionLibraries) {
            boolean isEligible = true;

            if (versionedEntity instanceof TLOperation) {
                if (minorVersionLibrary.getService() != null) {
                    isEligible = (minorVersionLibrary.getService().getOperation(
                            ((TLOperation) versionedEntity).getName()) == null);
                }
            } else {
                isEligible = (minorVersionLibrary.getNamedMember(versionedEntity.getLocalName()) == null);
            }
            if (isEligible && !isReadOnly(minorVersionLibrary)) {
                eligibleLibraries.add(minorVersionLibrary);
            }
        }
        return eligibleLibraries;
    }

    /**
     * Returns the libraries in the current model that are eligible to accept new minor versions for
     * the given versioned entity.
     * 
     * @param versionedEntity
     *            the versioned entity for which to return eligible minor version libraries
     * @return List<TLLibrary>
     * @throws VersionSchemeException
     */
    public TLLibrary getPreferredMinorVersionTarget(Versioned versionedEntity)
            throws VersionSchemeException {
        List<TLLibrary> eligibleTargets = getEligibleMinorVersionTargets(versionedEntity);
        return eligibleTargets.isEmpty() ? null : eligibleTargets.get(eligibleTargets.size() - 1);
    }

    /**
     * Creates a new minor version of the given library. The new version will contain a roll-up of
     * all members from all patches that exist for the library.
     * 
     * <p>
     * NOTE: The library that is returned by this method is saved to a default location and added to
     * the owning model of the original library.
     * 
     * @param library
     *            the library from which to construct a new minor version
     * @return TLLibrary
     * @throws VersionSchemeException
     *             thrown if a later minor version already exists for the given library
     * @throws ValidationException
     *             thrown if one of more of the libraries to be rolled-up contains validation errors
     * @throws LibrarySaveException
     *             thrown if the new version of the library cannot be saved to the local disk
     */
    public TLLibrary createNewMinorVersion(TLLibrary library) throws VersionSchemeException,
            ValidationException, LibrarySaveException {
        return createNewMinorVersion(library, null);
    }

    /**
     * Creates a new minor version of the given library. The new version will contain a roll-up of
     * all members from all patches that exist for the library.
     * 
     * <p>
     * NOTE: The library that is returned by this method is saved to the specified location and
     * added to the owning model of the original library.
     * 
     * @param library
     *            the library from which to construct a new minor version
     * @param libraryFile
     *            the file name and location where the new library is to be saved (null for default
     *            location)
     * @return TLLibrary
     * @throws VersionSchemeException
     *             thrown if a later minor version already exists for the given library
     * @throws ValidationException
     *             thrown if one of more of the libraries to be rolled-up contains validation errors
     * @throws LibrarySaveException
     *             thrown if the new version of the library cannot be saved to the local disk
     */
    public TLLibrary createNewMinorVersion(TLLibrary library, File libraryFile)
            throws VersionSchemeException, ValidationException, LibrarySaveException {
        List<ProjectItem> importedVersions = new ArrayList<ProjectItem>();
        try {
            VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                    library.getVersionScheme());

            if (isWorkInProcessLibrary(library)) {
                throw new VersionSchemeException(
                        "New versions cannot be created from work-in-process libraries (commit and unlock to proceed).");
            }
            if (isDuplicateOfPublishedLibrary(library)) {
                throw new VersionSchemeException(
                        "Unable to create the new version - a duplicate of this unmanaged library has already been published.");
            }
            if (versionScheme.isPatchVersion(library.getNamespace())) {
                throw new VersionSchemeException(
                        "Cannot create a minor version from a patch-level library.");
            }

            // Create the new (empty) library version
            List<TLLibrary> minorVersions = getLaterMinorVersions(library);
            importedVersions.addAll(importLaterMinorVersionsFromRepository(library, minorVersions));
            TLLibrary latestMinorVersion = minorVersions.isEmpty() ? library : minorVersions
                    .get(minorVersions.size() - 1);
            String newLibraryVersion = versionScheme.incrementMinorVersion(versionScheme
                    .getVersionIdentifier(latestMinorVersion.getNamespace()));
            TLLibrary newLibrary = createNewLibrary(library);

            if (library.getPrefix() != null) {
                newLibrary
                        .setPrefix(versionScheme.getPrefix(library.getPrefix(), newLibraryVersion));
            }
            newLibrary.setNamespace(versionScheme.setVersionIdentifier(library.getNamespace(),
                    newLibraryVersion));

            if (libraryFile == null) {
                libraryFile = getDefaultLibraryFileLocation(newLibrary, library);
            }
            if (libraryFile.exists()) {
                throw new LibrarySaveException(
                        "A file already exists at the new library location: "
                                + libraryFile.getAbsolutePath());
            }
            newLibrary.setLibraryUrl(URLUtils.toURL(libraryFile));
            library.getOwningModel().addLibrary(newLibrary);

            // Validate all of the patch libraries to be rolled-up to ensure no errors exist
            TLLibrary priorMinorVersion = getPriorMinorVersion(newLibrary);
            List<TLLibrary> patchLibraries = getLaterPatchVersions(priorMinorVersion);
            ValidationFindings findings = new ValidationFindings();

            importedVersions.addAll(importLaterPatchVersionsFromRepository(priorMinorVersion,
                    patchLibraries));

            for (TLLibrary lib : patchLibraries) {
                findings.addAll(validate(lib));
            }
            findings.addAll(validate(priorMinorVersion));

            if (findings.hasFinding(FindingType.ERROR)) {
            	for (String message : findings.getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
            		System.out.println(message);
            	}
                library.getOwningModel().removeLibrary(newLibrary);
                throw new ValidationException(
                        "Unable to create the new version because the prior version and/or patch libraries contain errors.",
                        findings);
            }

            // For any patches that exist in the prior minor version, create roll-up
            // entities in the new library version.
            RollupReferenceInfo rollupReferences = new RollupReferenceInfo(library, patchLibraries);
            ModelElementCloner cloner = new ModelElementCloner(library.getOwningModel());

            for (TLLibrary patchLibrary : patchLibraries) {
                rollupPatchLibrary(newLibrary, patchLibrary, cloner, rollupReferences);
            }
            adjustSameLibraryReferences(newLibrary, rollupReferences);
            ImportManagementIntegrityChecker.verifyReferencedLibraries(newLibrary);

            new LibraryModelSaver().saveLibrary(newLibrary);
            addToActiveProject(newLibrary);
            return newLibrary;

        } finally {
            removeImportedVersions(importedVersions);
        }
    }

    /**
     * Creates a new minor version of the given 'versionedEntity' and adds it to the target library
     * verson as a new member. The new version will be a minor version extension of the given
     * versioned entity, and its contents will be a roll-up of any patches that may exist.
     * 
     * @param versionedEntity
     *            the versioned entity from which to create a new minor version
     * @param targetLibraryVersion
     *            the target library that is assigned to the desired minor version number
     * @return V
     * @throws VersionSchemeException
     *             thrown if the target library is not assigned to the entity's major version chain,
     *             or the entity is not the latest minor version in that chain
     * @throws ValidationException
     *             thrown if the versioned entity or one of its roll-up patches contains validation
     *             errors
     */
    @SuppressWarnings("unchecked")
    public <V extends Versioned> V createNewMinorVersion(V versionedEntity,
            TLLibrary targetLibraryVersion) throws VersionSchemeException, ValidationException {
        TLLibrary owningLibrary = getOwningLibrary(versionedEntity);
        List<TLLibrary> minorVersionLibraries = getLaterMinorVersions(owningLibrary);
        List<TLLibrary> patchLibraries = getLaterPatchVersions(owningLibrary);
        int targetVersionIndex = minorVersionLibraries.indexOf(targetLibraryVersion);
        ValidationFindings findings = new ValidationFindings();
        V laterMinorVersion = null;

        // Perform validation checks to ensure we can create the new minor version
        for (TLLibrary lib : patchLibraries) {
            findings.addAll(validate(lib));
        }
        findings.addAll(validate(targetLibraryVersion));

        if (findings.hasFinding(FindingType.ERROR)) {
            throw new ValidationException(
                    "Unable to create the new version because the prior version and/or patch libraries contain errors.",
                    findings);
        }
        if (targetVersionIndex < 0) {
            throw new VersionSchemeException(
                    "The target library cannot contain a minor version of the entity provided.");
        }
        if (isReadOnly(targetLibraryVersion)) {
            throw new VersionSchemeException(
                    "Unable to create the requested minor version because the target library is read-only.");
        }
        if (versionedEntity instanceof TLOperation) {
            if ((targetLibraryVersion.getService() != null)
                    && (targetLibraryVersion.getService().getOperation(
                            ((TLOperation) versionedEntity).getName()) != null)) {
                throw new VersionSchemeException("A minor version of operation '"
                        + versionedEntity.getLocalName()
                        + "' already exists in the specified library's service.");
            }
        } else if (versionedEntity instanceof TLSimple) {
        	if (((TLSimple) versionedEntity).isListTypeInd()) {
        		throw new VersionSchemeException("Minor versions of simple list types are not allowed.");
        	}
        } else {
            if (targetLibraryVersion.getNamedMember(versionedEntity.getLocalName()) != null) {
                throw new VersionSchemeException("A minor version of entity '"
                        + versionedEntity.getLocalName()
                        + "' already exists in the specified library.");
            }
        }

        // Attempt to identify a later minor version than the one we are going to create
        for (int i = targetVersionIndex + 1; i < minorVersionLibraries.size(); i++) {
            TLLibrary laterMinorVersionLibrary = minorVersionLibraries.get(i);

            if (versionedEntity instanceof TLOperation) {
                TLService service = laterMinorVersionLibrary.getService();

                if (service != null) {
                    laterMinorVersion = (V) service.getOperation(((TLOperation) versionedEntity)
                            .getName());
                }
            } else {
                laterMinorVersion = (V) laterMinorVersionLibrary.getNamedMember(versionedEntity
                        .getLocalName());
            }
            if (laterMinorVersion != null) {
                if (isReadOnly(laterMinorVersionLibrary)) {
                    // This read-only check is included for completeness, but it is an edge case
                    // since the later minor version is probably marked as FINAL, even though our
                	// previous version is DRAFT. This would not be an expected configuration since
                	// earlier versions are typically locked for editing before the later versions
                	// of a library.
                    throw new VersionSchemeException(
                            "Unable to create the requested minor version because a later minor version exists that is read-only.");
                }
                break;
            }
        }

        // Create the new minor version
        RollupReferenceInfo rollupReferences = new RollupReferenceInfo(owningLibrary,
                patchLibraries);
        ModelElementCloner cloner = new ModelElementCloner(owningLibrary.getOwningModel());
        V newVersion;

        if (versionedEntity instanceof TLBusinessObject) {
            TLBusinessObject oldBO = (TLBusinessObject) versionedEntity;
            TLBusinessObject newBO = newVersionInstance(oldBO, cloner);

            if (laterMinorVersion != null) {
                setMinorVersionExtension(laterMinorVersion, newBO);
            }
            targetLibraryVersion.addNamedMember(newBO);
            newVersion = (V) newBO;

        } else if (versionedEntity instanceof TLCoreObject) {
            TLCoreObject oldCore = (TLCoreObject) versionedEntity;
            TLCoreObject newCore = newVersionInstance(oldCore, cloner);

            if (laterMinorVersion != null) {
                setMinorVersionExtension(laterMinorVersion, newCore);
            }
            targetLibraryVersion.addNamedMember(newCore);
            newVersion = (V) newCore;

        } else if (versionedEntity instanceof TLOperation) {
            TLService newVersionService = targetLibraryVersion.getService();
            TLOperation oldOp = (TLOperation) versionedEntity;
            TLOperation newOp = newVersionInstance(oldOp, cloner);

            if (newVersionService == null) {
                newVersionService = newVersionInstance(oldOp.getOwningService(), cloner);
                targetLibraryVersion.setService(newVersionService);
            }
            if (laterMinorVersion != null) {
                setMinorVersionExtension(laterMinorVersion, newOp);
            }
            newVersionService.addOperation(newOp);
            newVersion = (V) newOp;

        } else if (versionedEntity instanceof TLValueWithAttributes) {
            TLValueWithAttributes oldVWA = (TLValueWithAttributes) versionedEntity;
            TLValueWithAttributes newVWA = newVersionInstance(oldVWA, cloner);

            if (laterMinorVersion != null) {
                setMinorVersionExtension(laterMinorVersion, newVWA);
            }
            targetLibraryVersion.addNamedMember(newVWA);
            newVersion = (V) newVWA;

        } else if (versionedEntity instanceof TLOpenEnumeration) {
        	TLOpenEnumeration oldEnum = (TLOpenEnumeration) versionedEntity;
        	TLOpenEnumeration newEnum = newVersionInstance(oldEnum, cloner);

            if (laterMinorVersion != null) {
                setMinorVersionExtension(laterMinorVersion, newEnum);
            }
            targetLibraryVersion.addNamedMember(newEnum);
            newVersion = (V) newEnum;

        } else if (versionedEntity instanceof TLClosedEnumeration) {
        	TLClosedEnumeration oldEnum = (TLClosedEnumeration) versionedEntity;
        	TLClosedEnumeration newEnum = newVersionInstance(oldEnum, cloner);

            if (laterMinorVersion != null) {
                setMinorVersionExtension(laterMinorVersion, newEnum);
            }
            targetLibraryVersion.addNamedMember(newEnum);
            newVersion = (V) newEnum;

        } else { // Assume TLSimple
        	TLSimple oldSimple = (TLSimple) versionedEntity;
        	TLSimple newSimple = newVersionInstance(oldSimple, cloner);

            if (laterMinorVersion != null) {
                setMinorVersionExtension(laterMinorVersion, newSimple);
            }
            targetLibraryVersion.addNamedMember(newSimple);
            newVersion = (V) newSimple;
        }

        // Search for patches of the versioned entity to roll up
        for (TLLibrary patchLibrary : patchLibraries) {
            for (TLExtensionPointFacet patch : patchLibrary.getExtensionPointFacetTypes()) {
                if (getPatchedVersion(patch) == versionedEntity) {
                    rollupPatchVersion(newVersion, patch, cloner, rollupReferences, true);
                }
            }
        }
        adjustSameLibraryReferences(targetLibraryVersion, rollupReferences);
        ImportManagementIntegrityChecker.verifyReferencedLibraries(targetLibraryVersion);

        return newVersion;
    }

    /**
     * Rolls up the contents of the given patch version into the new major version. A roll-up is
     * essentially a merge of the attributes, properties, and indicators from the facets of the
     * patch version.
     * 
     * @param minorVersionTarget
     *            the minor version of the entity that will receive any rolled up items
     * @param patchVersion
     *            the patch version of the entity whose items will be the source of the roll-up
     * @throws VersionSchemeException
     *             thrown if the given major version is not a later major version and/or the patch
     *             version is not a prior patch version
     * @throws ValidationException
     *             thrown if the rollup cannot be performed because one or more validation errors
     *             exist in either the source or target entity
     */
    public void rollupPatchVersion(Versioned minorVersionTarget, TLExtensionPointFacet patchVersion)
            throws VersionSchemeException, ValidationException {
        if (!(minorVersionTarget.getOwningLibrary() instanceof TLLibrary)
                || (!(patchVersion.getOwningLibrary() instanceof TLLibrary))) {
            throw new VersionSchemeException(
                    "Only entities contained within user-defined libraries can be rolled up.");
        }
        RollupReferenceInfo rollupReferences = new RollupReferenceInfo(
                (TLLibrary) patchVersion.getOwningLibrary());
        TLLibrary targetLibrary = (TLLibrary) minorVersionTarget.getOwningLibrary();

        rollupPatchVersion(minorVersionTarget, patchVersion, new ModelElementCloner(
                minorVersionTarget.getOwningModel()), rollupReferences, false);
        adjustSameLibraryReferences(targetLibrary, rollupReferences);
        ImportManagementIntegrityChecker.verifyReferencedLibraries(targetLibrary);
    }

}
