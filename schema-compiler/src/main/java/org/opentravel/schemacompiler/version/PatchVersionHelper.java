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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Helper methods used to construct new patch versions of <code>TLLibrary</code> instances and their entity members.
 * 
 * @author S. Livezey
 */
public final class PatchVersionHelper extends AbstractVersionHelper {
	
	/**
	 * Default constructor. NOTE: When working in an environment where a <code>ProjectManager</code> is being used, the
	 * other constructor should be used to assign the active project for the helper.
	 */
	public PatchVersionHelper() {
	}
	
	/**
	 * Constructor that assigns the active project for an application's <code>ProjectManager</code>. If new libraries
	 * are created by this helper, they will be automatically added to the active project that is passed to this
	 * constructor.
	 * 
	 * @param activeProject the active project to which new libraries will be assigned
	 */
	public PatchVersionHelper(Project activeProject) {
		super( activeProject );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.AbstractVersionHelper#getLaterPatchVersions(org.opentravel.schemacompiler.model.TLLibrary)
	 */
	@Override
	public List<TLLibrary> getLaterPatchVersions(TLLibrary library) throws VersionSchemeException {
		return super.getLaterPatchVersions( library );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.AbstractVersionHelper#getPatchedVersion(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
	 */
	@Override
	public Versioned getPatchedVersion(TLExtensionPointFacet xpFacet) throws VersionSchemeException {
		return super.getPatchedVersion( xpFacet );
	}
	
	/**
	 * Returns the list of all extension point facets that reference the given versioned entity from a library whose
	 * version is a patch of the given entity.
	 * 
	 * @param versionedEntity the versioned entity for which to return patches
	 * @return List<TLExtensionPointFacet>
	 * @throws VersionSchemeException thrown if the entity's version scheme is not recognized
	 */
	public List<TLExtensionPointFacet> getLaterPatchVersions(Versioned versionedEntity) throws VersionSchemeException {
		List<TLExtensionPointFacet> patchList = new ArrayList<>();
		
		if (!(versionedEntity instanceof LibraryElement)) {
			return patchList;
		}
		LibraryElement versionedMember = versionedEntity;
		
		if (versionedMember.getOwningLibrary() instanceof TLLibrary) {
			List<TLLibrary> patchVersionLibraries = getLaterPatchVersions(
					(TLLibrary) versionedMember.getOwningLibrary() );
			
			if (!patchVersionLibraries.isEmpty()) {
				addPatchingFacets( versionedEntity, patchVersionLibraries, patchList );
			}
		}
		return patchList;
	}

	/**
	 * Adds all of the extension point facets from the list of patch version libraries that
	 * extend the given versioned entity.
	 * 
	 * @param versionedEntity  the versioned entity for which to return extension point patches
	 * @param patchVersionLibraries  the list of patch version libraries to search
	 * @param patchList  the list of extension point facets that patch the given entity
	 */
	private void addPatchingFacets(Versioned versionedEntity, List<TLLibrary> patchVersionLibraries,
			List<TLExtensionPointFacet> patchList) {
		// Start by identifying the possible facets to which an extension point facet
		// can refer
		Set<TLPatchableFacet> entityFacets = new HashSet<>(
				getVersionHandler( versionedEntity ).getPatchableFacets( versionedEntity ) );
		
		// Search the extension points to determine if any of them reference a facet of
		// our original entity
		for (TLLibrary patchVersionLib : patchVersionLibraries) {
			for (TLExtensionPointFacet xpFacet : patchVersionLib.getExtensionPointFacetTypes()) {
				NamedEntity xpExtendedEntity = (xpFacet.getExtension() == null) ? null
						: xpFacet.getExtension().getExtendsEntity();
				
				if (entityFacets.contains( xpExtendedEntity )) {
					patchList.add( xpFacet );
				}
			}
		}
	}
	
	/**
	 * Returns the libraries in the current model that are eligible to accept new patches for the given versioned entity
	 * facet.
	 * 
	 * @param versionedEntityFacet the versioned entity facet for which to return eligible patch libraries
	 * @return List<TLLibrary>
	 * @throws VersionSchemeException
	 */
	public List<TLLibrary> getEligiblePatchVersionTargets(TLPatchableFacet versionedEntityFacet)
			throws VersionSchemeException {
		TLLibrary owningLibrary = getOwningLibrary( (Versioned) versionedEntityFacet.getOwningEntity() );
		List<TLLibrary> patchLibraries = getLaterPatchVersions( owningLibrary );
		List<TLLibrary> eligibleLibraries = new ArrayList<>();
		
		// Search the patch libraries, and return only those who do not yet contain a patch for the
		// given facet
		for (TLLibrary patchLibrary : patchLibraries) {
			boolean isEligible = true;
			
			for (TLExtensionPointFacet patch : patchLibrary.getExtensionPointFacetTypes()) {
				if ((patch.getExtension() != null)
						&& (patch.getExtension().getExtendsEntity() == versionedEntityFacet)) {
					isEligible = false;
					break;
				}
			}
			if (isEligible && !isReadOnly( patchLibrary )) {
				eligibleLibraries.add( patchLibrary );
			}
		}
		return eligibleLibraries;
	}
	
	/**
	 * Returns the libraries in the current model that are eligible to accept new patches for the given versioned entity
	 * facet.
	 * 
	 * @param versionedEntityFacet the versioned entity facet for which to return eligible patch libraries
	 * @return List<TLLibrary>
	 * @throws VersionSchemeException
	 */
	public TLLibrary getPreferredPatchVersionTarget(TLPatchableFacet versionedEntityFacet)
			throws VersionSchemeException {
		List<TLLibrary> eligibleTargets = getEligiblePatchVersionTargets( versionedEntityFacet );
		return eligibleTargets.isEmpty() ? null : eligibleTargets.get( eligibleTargets.size() - 1 );
	}
	
	/**
	 * Creates a new patch version of the given library. The library that is passed to this method must be a major or
	 * minor version intance. The resulting patch number of the resulting patch library will be one greater than the
	 * latest known patch for the library that is passed to this method.
	 * 
	 * <p>
	 * NOTE: The library that is returned by this method is saved to a default location and added to the owning model of
	 * the original library.
	 * 
	 * @param library the library for which to construct a new patch version
	 * @return TLLibrary
	 * @throws VersionSchemeException thrown if the library's version scheme is not recognized
	 * @throws LibrarySaveException thrown if the new version of the library cannot be saved to the local disk
	 */
	public TLLibrary createNewPatchVersion(TLLibrary library) throws VersionSchemeException, LibrarySaveException {
		return createNewPatchVersion( library, null );
	}
	
	/**
	 * Creates a new patch version of the given library. The library that is passed to this method must be a major or
	 * minor version intance. The resulting patch number of the resulting patch library will be one greater than the
	 * latest known patch for the library that is passed to this method.
	 * 
	 * <p>
	 * NOTE: The library that is returned by this method is saved to the specified location and added to the owning
	 * model of the original library.
	 * 
	 * @param library the library for which to construct a new patch version
	 * @param libraryFile the file name and location where the new library is to be saved (null for default location)
	 * @return TLLibrary
	 * @throws VersionSchemeException thrown if the library's version scheme is not recognized
	 */
	public TLLibrary createNewPatchVersion(TLLibrary library, File libraryFile)
			throws VersionSchemeException, LibrarySaveException {
		List<ProjectItem> importedVersions = new ArrayList<>();
		try {
			// First, do some preliminary error checking
			if (isWorkInProcessLibrary( library )) {
				throw new VersionSchemeException(
						"New versions cannot be created from work-in-process libraries (commit and unlock to proceed)." );
			}
			if (isDuplicateOfPublishedLibrary( library )) {
				throw new VersionSchemeException(
						"Unable to create the new version - a duplicate of this unmanaged library has already been published." );
			}
			
			// Load all of the related libraries required to build and/or validate the new version
			VersionScheme versionScheme = getVersionScheme( library );
			List<TLLibrary> patchVersions = getLaterPatchVersions( library );
			importedVersions.addAll( importLaterPatchVersionsFromRepository( library, patchVersions ) );
			TLLibrary latestPatch = patchVersions.isEmpty() ? null : patchVersions.get( patchVersions.size() - 1 );
			TLLibrary newLibrary = createNewLibrary( library );
			TLModel model = library.getOwningModel();
			String newLibraryVersion;
			
			if (latestPatch != null) {
				newLibraryVersion = versionScheme.incrementPatchLevel( latestPatch.getVersion() );
			} else {
				newLibraryVersion = versionScheme.incrementPatchLevel( library.getVersion() );
			}
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
			model.addLibrary( newLibrary );
			new LibraryModelSaver().saveLibrary( newLibrary );
			addToActiveProject( newLibrary );
			return newLibrary;
			
		} finally {
			removeImportedVersions( importedVersions );
		}
	}
	
	/**
	 * Creates a new <code>TLExtensionPointFacet</code> that is a patch for the given versioned entity. The contents of
	 * the patch returned by this method will be empty.
	 * 
	 * @param versionedEntity the versioned entity for which to create a patch
	 * @param facetToPatch the facet to be patched from the versioned entity
	 * @param targetLibraryVersion the target library that is assigned to the desired patch version number
	 * @return TLExtensionPointFacet
	 * @throws VersionSchemeException thrown if the target library is not a patch version of the given entity, or the
	 *             facet to be patched is not owned by the versioned entity
	 */
	public TLExtensionPointFacet createNewPatch(TLPatchableFacet facetToPatch, TLLibrary targetLibraryVersion)
			throws VersionSchemeException {
		TLLibrary owningLibrary = getOwningLibrary( (Versioned) facetToPatch.getOwningEntity() );
		List<TLLibrary> patchLibraries = getLaterPatchVersions( owningLibrary );
		
		if (patchLibraries.indexOf( targetLibraryVersion ) < 0) {
			throw new VersionSchemeException(
					"The target library cannot contain a patch for the versioned entity provided." );
		}
		if (isReadOnly( targetLibraryVersion )) {
			throw new VersionSchemeException(
					"Unable to create the requested patch because the target library is read-only." );
		}
		TLExtensionPointFacet patchEntity = new TLExtensionPointFacet();
		TLExtension extension = new TLExtension();
		
		targetLibraryVersion.addNamedMember( patchEntity );
		patchEntity.setExtension( extension );
		extension.setExtendsEntity( facetToPatch );
		return patchEntity;
	}
	
}
