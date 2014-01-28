/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.version;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.ic.ImportManagementIntegrityChecker;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
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
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Helper methods used to construct new major versions of <code>TLLibrary</code> instances.
 * 
 * @author S. Livezey
 */
public final class MajorVersionHelper extends AbstractVersionHelper {
	
	/**
	 * Default constructor.  NOTE: When working in an environment where a <code>ProjectManager</code>
	 * is being used, the other constructor should be used to assign the active project for the helper.
	 */
	public MajorVersionHelper() {}
	
	/**
	 * Constructor that assigns the active project for an application's <code>ProjectManager</code>.  If
	 * new libraries are created by this helper, they will be automatically added to the active project
	 * that is passed to this constructor.
	 * 
	 * @param activeProject  the active project to which new libraries will be assigned
	 */
	public MajorVersionHelper(Project activeProject) {
		super( activeProject );
	}
	
	/**
	 * Returns the major version that began the library's minor version chain.  If no such major
	 * version exists in the library's model, this method will return null.  If the library passed
	 * to this method is, itself, a major version, the original library will be returned.
	 * 
	 * @param library  the library for which to retrieve the major version
	 * @return TLLibrary
	 * @throws VersionSchemeException  thrown if the library's version scheme is not recognized
	 */
	public TLLibrary getMajorVersion(TLLibrary library) throws VersionSchemeException {
		VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme( library.getVersionScheme() );
		TLLibrary majorVersion = null;
		
		if ((library != null) && (library.getNamespace() != null)) {
			if (versionScheme.isMajorVersion( library.getNamespace() )) {
				majorVersion = library; // simple case: we already have the major version
				
			} else if (library.getOwningModel() != null) {
				List<String> versionChain = versionScheme.getMajorVersionChain( library.getNamespace() );
				String majorVersionNS = versionChain.isEmpty() ? library.getNamespace() : versionChain.get( versionChain.size() - 1 );
				
				for (TLLibrary lib : library.getOwningModel().getUserDefinedLibraries()) {
					if ((lib != library) && isVersionCandidateMatch(lib, library)
							&& majorVersionNS.equals(lib.getNamespace())) {
						majorVersion = lib;
						break;
					}
				}
			}
		}
		return majorVersion;
	}
	
	/**
	 * Creates a new major version of the given library.  The new version will contain a roll-up of all members
	 * from the given library's major version chain.  It does not matter whether the given library is the latest
	 * member of its version chain; this method will locate all chain members and incorporate them into the roll-up.
	 * 
	 * <p>NOTE: The library that is returned by this method is saved to the specified location and added to the
	 * owning model of the original library.
	 * 
	 * @param library  the library from which to construct a new major version
	 * @return TLLibrary
	 * @throws VersionSchemeException  thrown if a later major version already exists for the given library
	 * @throws ValidationException  thrown if one of more of the libraries to be rolled-up contains validation errors
	 * @throws LibrarySaveException  thrown if the new version of the library cannot be saved to the local disk
	 */
	public TLLibrary createNewMajorVersion(TLLibrary library) throws VersionSchemeException, ValidationException, LibrarySaveException {
		return createNewMajorVersion(library, null);
	}
	
	/**
	 * Creates a new major version of the given library.  The new version will contain a roll-up of all members
	 * from the given library's major version chain.  It does not matter whether the given library is the latest
	 * member of its version chain; this method will locate all chain members and incorporate them into the roll-up.
	 * 
	 * <p>NOTE: The library that is returned by this method is saved to a default location and added to the
	 * owning model of the original library.
	 * 
	 * @param library  the library from which to construct a new major version
	 * @param libraryFile  the file name and location where the new library is to be saved (null for default location)
	 * @return TLLibrary
	 * @throws VersionSchemeException  thrown if a later major version already exists for the given library
	 * @throws ValidationException  thrown if one of more of the libraries to be rolled-up contains validation errors
	 * @throws LibrarySaveException  thrown if the new version of the library cannot be saved to the local disk
	 */
	@SuppressWarnings("unchecked")
	public TLLibrary createNewMajorVersion(TLLibrary library, File libraryFile)
			throws VersionSchemeException, ValidationException, LibrarySaveException {
		List<ProjectItem> importedVersions = new ArrayList<ProjectItem>();
		try {
			// First, do some preliminary error checking
			if (isWorkInProcessLibrary(library)) {
				throw new VersionSchemeException("New versions cannot be created from work-in-process libraries (commit and unlock to proceed).");
			}
			if (isDuplicateOfPublishedLibrary(library)) {
				throw new VersionSchemeException("Unable to create the new version - a duplicate of this unmanaged library has already been published.");
			}
			
			// Load all of the related libraries required to build the new version
			VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme( library.getVersionScheme() );
			TLLibrary previousMajorVersion = getMajorVersion( library );
			List<TLLibrary> minorVersionLibraries = getLaterMinorVersions( previousMajorVersion );
			importedVersions.addAll( importLaterMinorVersionsFromRepository( previousMajorVersion, minorVersionLibraries ) );
			minorVersionLibraries.add(0, previousMajorVersion);
			TLLibrary lastMinorVersion = minorVersionLibraries.isEmpty() ? null : minorVersionLibraries.get( minorVersionLibraries.size() - 1 );
			List<TLLibrary> patchLibraries = (lastMinorVersion == null) ? new ArrayList<TLLibrary>() : getLaterPatchVersions( lastMinorVersion );
			importedVersions.addAll( importLaterPatchVersionsFromRepository( lastMinorVersion, patchLibraries ) );
			
			// Validate all of the libraries to be rolled up into the new major version
			ValidationFindings findings = new ValidationFindings();
			
			for (TLLibrary lib : minorVersionLibraries) {
				findings.addAll( validate(lib) );
			}
			for (TLLibrary lib : patchLibraries) {
				findings.addAll( validate(lib) );
			}
			if (findings.hasFinding(FindingType.ERROR)) {
				throw new ValidationException("Unable to roll-up because the target and/or patch versions contains errors.", findings);
			}
			
			// Create the new (empty) library version
			String newLibraryVersion = versionScheme.incrementMajorVersion( versionScheme.getVersionIdentifier(previousMajorVersion.getNamespace()) );
			TLLibrary newLibrary = createNewLibrary( previousMajorVersion );
			
			newLibrary.setNamespace( versionScheme.setVersionIdentifier(library.getNamespace(), newLibraryVersion) );
			newLibrary.setPrefix( versionScheme.getPrefix(library.getPrefix(), newLibraryVersion) );
			
			if (libraryFile == null) {
				libraryFile = getDefaultLibraryFileLocation(newLibrary, library);
			}
			if (libraryFile.exists()) {
				throw new LibrarySaveException("A file already exists at the new library location: " + libraryFile.getAbsolutePath());
			}
			newLibrary.setLibraryUrl( URLUtils.toURL(libraryFile) );
			
			// For any minor versions or patches that exist in the prior minor version, create roll-up
			// entities in the new library version.
			RollupReferenceInfo rollupReferences = new RollupReferenceInfo( minorVersionLibraries, patchLibraries );
			ModelElementCloner cloner = new ModelElementCloner(library.getOwningModel());
			
			for (TLLibrary minorVersionLibrary : minorVersionLibraries) {
				rollupMinorVersionLibrary(newLibrary, minorVersionLibrary, cloner, rollupReferences);
			}
			
			// Roll up any patches that exist for the last minor version
			for (TLLibrary patchLibrary : patchLibraries) {
				rollupPatchLibrary(newLibrary, patchLibrary, cloner, rollupReferences);
			}
			adjustSameLibraryReferences( newLibrary, rollupReferences );
			ImportManagementIntegrityChecker.verifyReferencedLibraries( newLibrary );
			
			new LibraryModelSaver().saveLibrary( newLibrary );
			addToActiveProject( newLibrary );
			return newLibrary;
			
		} finally {
			removeImportedVersions( importedVersions );
		}
	}
	
	/**
	 * Rolls up the contents of the given minor version into the new major version.  A roll-up is essentially
	 * a merge of the attributes, properties, and indicators from the facets of the minor version.
	 * 
	 * @param majorVersionTarget  the major version of the entity that will receive any rolled up items
	 * @param minorVersion  the minor version of the entity whose items will be the source of the roll-up
	 * @throws VersionSchemeException  thrown if the given major version is not a later major version and/or
	 *								   the minor version is not a prior minor version
	 * @throws ValidationException  thrown if the rollup cannot be performed because one or more validation
	 *								errors exist in either the source or target entity
	 */
	@SuppressWarnings("unchecked")
	public <V extends Versioned> void rollupMinorVersion(V majorVersionTarget, V minorVersion)
			throws VersionSchemeException, ValidationException {
		if (!(majorVersionTarget.getOwningLibrary() instanceof TLLibrary)
				|| !(minorVersion.getOwningLibrary() instanceof TLLibrary)) {
			throw new VersionSchemeException("Only entities contained within user-defined libraries can be rolled up.");
		}
		RollupReferenceInfo rollupReferences = new RollupReferenceInfo( (TLLibrary) minorVersion.getOwningLibrary() );
		TLLibrary targetLibrary = (TLLibrary) majorVersionTarget.getOwningLibrary();
		
		rollupMinorVersion( majorVersionTarget, minorVersion, rollupReferences, false );
		adjustSameLibraryReferences( targetLibrary, rollupReferences );
		ImportManagementIntegrityChecker.verifyReferencedLibraries( targetLibrary );
	}
	
	/**
	 * Rolls up the contents of the given patch version into the new major version.  A roll-up is essentially
	 * a merge of the attributes, properties, and indicators from the facets of the patch version.
	 * 
	 * @param majorVersionTarget  the major version of the entity that will receive any rolled up items
	 * @param patchVersion  the patch version of the entity whose items will be the source of the roll-up
	 * @throws VersionSchemeException  thrown if the given major version is not a later major version and/or
	 *								   the patch version is not a prior patch version
	 * @throws ValidationException  thrown if the rollup cannot be performed because one or more validation
	 *								errors exist in either the source or target entity
	 */
	@SuppressWarnings("unchecked")
	public void rollupPatchVersion(Versioned majorVersionTarget, TLExtensionPointFacet patchVersion)
			throws VersionSchemeException, ValidationException {
		if (!(majorVersionTarget.getOwningLibrary() instanceof TLLibrary)
				|| !(patchVersion.getOwningLibrary() instanceof TLLibrary)) {
			throw new VersionSchemeException("Only entities contained within user-defined libraries can be rolled up.");
		}
		RollupReferenceInfo rollupReferences = new RollupReferenceInfo( (TLLibrary) patchVersion.getOwningLibrary() );
		TLLibrary targetLibrary = (TLLibrary) majorVersionTarget.getOwningLibrary();
		
		rollupPatchVersion(majorVersionTarget, patchVersion,
				new ModelElementCloner(majorVersionTarget.getOwningModel()), rollupReferences, false);
		adjustSameLibraryReferences( targetLibrary, rollupReferences );
		ImportManagementIntegrityChecker.verifyReferencedLibraries( targetLibrary );
	}

	/**
	 * Rolls up the contents of the given minor version into the new major version.  A roll-up is essentially
	 * a merge of the attributes, properties, and indicators from the facets of the minor version.
	 * 
	 * @param majorVersionTarget  the major version of the entity that will receive any rolled up items
	 * @param minorVersion  the minor version of the entity whose items will be the source of the roll-up
	 * @param rollupReferences  reference information for the libraries being rolled up
	 * @param skipValidation  internal flag indicating whether entity validation should be skipped
	 * @throws VersionSchemeException  thrown if the given major version is not a later major version and/or
	 *								   the minor version is not a prior minor version
	 * @throws ValidationException  thrown if the rollup cannot be performed because one or more validation
	 *								errors exist in either the source or target entity
	 */
	private <V extends Versioned> void rollupMinorVersion(V majorVersionTarget, V minorVersion,
			RollupReferenceInfo rollupReferences, boolean skipValidation) throws VersionSchemeException, ValidationException {
		// Perform validation checks before rolling up
		if (!skipValidation) {
			ValidationFindings findings = new ValidationFindings();
			findings.addAll( validate(majorVersionTarget) );
			findings.addAll( validate(minorVersion) );
			
			if (findings.hasFinding(FindingType.ERROR)) {
				throw new ValidationException("Unable to roll-up because the target and/or minor versions contains errors.", findings);
			}
			if ((majorVersionTarget.getVersionScheme() == null)
					|| !majorVersionTarget.getVersionScheme().equals(minorVersion.getVersionScheme())) {
				throw new VersionSchemeException("The target and minor versions to be rolled-up are not assigned to the same version scheme.");
			}
			if ((majorVersionTarget.getBaseNamespace() == null)
					|| !majorVersionTarget.getBaseNamespace().equals(minorVersion.getBaseNamespace())) {
				throw new VersionSchemeException("The target and minor versions to be rolled-up are not assigned to the same base namespace.");
			}
			if ( !((NamedEntity) majorVersionTarget).getLocalName().equals( ((NamedEntity) minorVersion).getLocalName() ) ) {
				throw new VersionSchemeException("The minor version provided is not a later version of the target entity because it does not have the same name.");
			}
			if ( !majorVersionTarget.getClass().equals( minorVersion.getClass() ) ) {
				throw new VersionSchemeException("The minor version provided is not a later version of the target entity because they are different entity types.");
			}
			if (isReadOnly(majorVersionTarget.getOwningLibrary())) {
				throw new VersionSchemeException("Unable to roll-up the requested minor version because the target version is in a read-only library.");
			}
			
			// Verify that the minor is, in fact, an extension of one of the previous major versions
			VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme( majorVersionTarget.getVersionScheme() );
			Comparator<Versioned> versionComparator = versionScheme.getComparator(true);
			
			if ( !versionScheme.isMajorVersion(majorVersionTarget.getNamespace())
					|| !versionScheme.isMinorVersion(minorVersion.getNamespace())
					|| (versionComparator.compare(majorVersionTarget, minorVersion) < 0) ) {
				throw new VersionSchemeException("The minor version provided is not a later version of the target entity.");
			}
		}
		
		// Perform the roll-up of all attributes, properties, and indicators of the given minor version
		if (majorVersionTarget instanceof TLValueWithAttributes) {
			ModelElementCloner cloner = new ModelElementCloner(minorVersion.getOwningModel());
			TLValueWithAttributes target = (TLValueWithAttributes) majorVersionTarget;
			TLValueWithAttributes source = (TLValueWithAttributes) minorVersion;
			
			mergeAttributes(target, source.getAttributes(), cloner, rollupReferences);
			mergeIndicators(target, source.getIndicators(), cloner);
			
		} else if (majorVersionTarget instanceof TLFacetOwner) {
			Map<String,TLFacet> targetFacets = new HashMap<String,TLFacet>();
			Map<String,TLFacet> sourceFacets = new HashMap<String,TLFacet>();
			
			// Identify the facets to be merged based on the type of the versioned objects
			if (majorVersionTarget instanceof TLBusinessObject) {
				TLBusinessObject target = (TLBusinessObject) majorVersionTarget;
				TLBusinessObject source = (TLBusinessObject) minorVersion;
				
				addToIdentityFacetMap(target.getSummaryFacet(), targetFacets);
				addToIdentityFacetMap(target.getDetailFacet(), targetFacets);
				addToIdentityFacetMap(source.getSummaryFacet(), sourceFacets);
				addToIdentityFacetMap(source.getDetailFacet(), sourceFacets);
				
				for (TLFacet sourceFacet : source.getCustomFacets()) {
					TLFacet targetFacet = target.getCustomFacet(sourceFacet.getContext(), sourceFacet.getLabel());
					
					if (targetFacet == null) {
						targetFacet = new TLFacet();
						target.addCustomFacet(targetFacet);
					}
					addToIdentityFacetMap(targetFacet, targetFacets);
					addToIdentityFacetMap(sourceFacet, targetFacets);
				}
				
				for (TLFacet sourceFacet : source.getQueryFacets()) {
					TLFacet targetFacet = target.getQueryFacet(sourceFacet.getContext(), sourceFacet.getLabel());
					
					if (targetFacet == null) {
						targetFacet = new TLFacet();
						target.addQueryFacet(targetFacet);
					}
					addToIdentityFacetMap(targetFacet, targetFacets);
					addToIdentityFacetMap(sourceFacet, targetFacets);
				}
				
			} else if (majorVersionTarget instanceof TLCoreObject) {
				TLCoreObject target = (TLCoreObject) majorVersionTarget;
				TLCoreObject source = (TLCoreObject) minorVersion;
				
				addToIdentityFacetMap(target.getSummaryFacet(), targetFacets);
				addToIdentityFacetMap(target.getDetailFacet(), targetFacets);
				addToIdentityFacetMap(source.getSummaryFacet(), sourceFacets);
				addToIdentityFacetMap(source.getDetailFacet(), sourceFacets);
				
			} else if (majorVersionTarget instanceof TLOperation) {
				TLOperation target = (TLOperation) majorVersionTarget;
				TLOperation source = (TLOperation) minorVersion;
				
				addToIdentityFacetMap(target.getRequest(), targetFacets);
				addToIdentityFacetMap(target.getResponse(), targetFacets);
				addToIdentityFacetMap(target.getNotification(), targetFacets);
				addToIdentityFacetMap(source.getRequest(), sourceFacets);
				addToIdentityFacetMap(source.getResponse(), sourceFacets);
				addToIdentityFacetMap(source.getNotification(), sourceFacets);
				
			} else {
				throw new IllegalArgumentException("Unknown versioned entity type: " + majorVersionTarget.getClass().getSimpleName());
			}
			
			// Perform the merge of each facet identified in the source and target versions
			ModelElementCloner cloner = new ModelElementCloner(minorVersion.getOwningModel());
			
			for (String facetIdentity : sourceFacets.keySet()) {
				TLFacet sourceFacet = sourceFacets.get(facetIdentity);
				TLFacet targetFacet = targetFacets.get(facetIdentity);
				
				if (targetFacet.getDocumentation() == null) {
					targetFacet.setDocumentation( cloner.clone(sourceFacet.getDocumentation()) );
				}
				mergeAttributes(targetFacet, sourceFacet.getAttributes(), cloner, rollupReferences);
				mergeProperties(targetFacet, sourceFacet.getElements(), cloner, rollupReferences);
				mergeIndicators(targetFacet, sourceFacet.getIndicators(), cloner);
			}
		} else {
			throw new IllegalArgumentException("Unknown versioned entity type: " + majorVersionTarget.getClass().getSimpleName());
		}
	}
	
	/**
	 * Rolls up the contents of the minor version library into the major version library provided.
	 * 
	 * @param majorVersionLibrary  the major version libarary that will receive any new/modified rolled-up entities
	 * @param minorVersionLibrary  the minor version library whose contents are to be rolled up
	 * @param cloner  the cloner to use when creating copies of model elements
	 * @param rollupReferences  reference information for the libraries being rolled up
	 * @throws VersionSchemeException  thrown if the library's version scheme is not recognized
	 */
	private void rollupMinorVersionLibrary(TLLibrary majorVersionLibrary, TLLibrary minorVersionLibrary,
			ModelElementCloner cloner, RollupReferenceInfo rollupReferences) throws VersionSchemeException {
		for (TLContext context : minorVersionLibrary.getContexts()) {
			if (majorVersionLibrary.getContext(context.getContextId()) == null) {
				majorVersionLibrary.addContext( cloner.clone(context) );
			}
		}
		for (TLSimple simple : minorVersionLibrary.getSimpleTypes()) {
			if (majorVersionLibrary.getNamedMember(simple.getName()) == null) {
				TLSimple clone = cloner.clone(simple);
				
				majorVersionLibrary.addNamedMember( clone );
				captureRollupLibraryReference( clone, rollupReferences );
			}
		}
		for (TLClosedEnumeration closedEnum : minorVersionLibrary.getClosedEnumerationTypes()) {
			if (majorVersionLibrary.getNamedMember(closedEnum.getName()) == null) {
				TLClosedEnumeration clone = cloner.clone(closedEnum);
				
				majorVersionLibrary.addNamedMember( clone );
				captureRollupLibraryReference( clone, rollupReferences );
			}
		}
		for (TLOpenEnumeration openEnum : minorVersionLibrary.getOpenEnumerationTypes()) {
			if (majorVersionLibrary.getNamedMember(openEnum.getName()) == null) {
				TLOpenEnumeration clone = cloner.clone(openEnum);
				
				majorVersionLibrary.addNamedMember( clone );
				captureRollupLibraryReference( clone, rollupReferences );
			}
		}
		for (TLValueWithAttributes vwa : minorVersionLibrary.getValueWithAttributesTypes()) {
			LibraryMember majorVersionEntity;
			
			if ((majorVersionEntity = majorVersionLibrary.getNamedMember(vwa.getName())) == null) {
				TLValueWithAttributes clone = cloner.clone(vwa);
				
				majorVersionLibrary.addNamedMember( clone );
				captureRollupLibraryReference( clone, rollupReferences );
				
			} else if (majorVersionEntity instanceof TLValueWithAttributes) {
				try {
					rollupMinorVersion((Versioned) majorVersionEntity, vwa, rollupReferences, true);
					
				} catch (ValidationException e) {
					// Should never happen since we indicate that validation should be skipped
				}
			}
		}
		for (TLCoreObject coreObject : minorVersionLibrary.getCoreObjectTypes()) {
			LibraryMember majorVersionEntity;
			
			if ((majorVersionEntity = majorVersionLibrary.getNamedMember(coreObject.getName())) == null) {
				TLCoreObject clone = cloner.clone(coreObject);
				
				majorVersionLibrary.addNamedMember( clone );
				captureRollupLibraryReference( clone, rollupReferences );
				
			} else if (majorVersionEntity instanceof TLCoreObject) {
				try {
					rollupMinorVersion((Versioned) majorVersionEntity, coreObject, rollupReferences, true);
					
				} catch (ValidationException e) {
					// Should never happen since we indicate that validation should be skipped
				}
			}
		}
		for (TLBusinessObject businessObject : minorVersionLibrary.getBusinessObjectTypes()) {
			LibraryMember majorVersionEntity;
			
			if ((majorVersionEntity = majorVersionLibrary.getNamedMember(businessObject.getName())) == null) {
				TLBusinessObject clone = cloner.clone(businessObject);
				
				majorVersionLibrary.addNamedMember( clone );
				captureRollupLibraryReference( clone, rollupReferences );
				
			} else if (majorVersionEntity instanceof TLBusinessObject) {
				try {
					rollupMinorVersion((Versioned) majorVersionEntity, businessObject, rollupReferences, true);
					
				} catch (ValidationException e) {
					// Should never happen since we indicate that validation should be skipped
				}
			}
		}
		if (minorVersionLibrary.getService() != null) {
			TLService majorVersionService = majorVersionLibrary.getService();
			
			if (majorVersionService == null) {
				majorVersionService = new TLService();
				majorVersionService.setName( minorVersionLibrary.getService().getName() );
				majorVersionService.setDocumentation( cloner.clone(minorVersionLibrary.getService().getDocumentation()) );
				majorVersionLibrary.setService( majorVersionService );
			}
			for (TLOperation operation : minorVersionLibrary.getService().getOperations()) {
				TLOperation majorVersionOp;
				
				if ((majorVersionOp = majorVersionService.getOperation(operation.getName())) == null) {
					TLOperation clone = cloner.clone(operation);
					
					majorVersionService.addOperation( clone );
					captureRollupLibraryReference( clone, rollupReferences );
					
				} else {
					try {
						rollupMinorVersion((Versioned) majorVersionOp, operation, rollupReferences, true);
						
					} catch (ValidationException e) {
						// Should never happen since we indicate that validation should be skipped
					}
				}
			}
		}
	}
	
	/**
	 * Adds the given facet to the identity map, using the full identity string of the facet
	 * as the key.
	 * 
	 * @param facet  the facet instance to add
	 * @param identityFacetMap  the map that will receive the facet
	 */
	private void addToIdentityFacetMap(TLFacet facet, Map<String,TLFacet> identityFacetMap) {
		if (facet != null) {
			identityFacetMap.put(facet.getFacetType().getIdentityName(facet.getContext(), facet.getLabel()), facet);
		}
	}
	
}
