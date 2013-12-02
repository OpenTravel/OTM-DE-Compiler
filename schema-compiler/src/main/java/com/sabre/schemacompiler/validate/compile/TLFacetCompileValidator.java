/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.codegen.util.FacetCodegenUtils;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLFacetOwner;
import com.sabre.schemacompiler.model.TLFacetType;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLFacetBaseValidator;
import com.sabre.schemacompiler.validate.impl.IdentityResolver;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.ValidatorUtils;

/**
 * Validator for the <code>TLFacet</code> class.
 * 
 * @author S. Livezey
 */
public class TLFacetCompileValidator extends TLFacetBaseValidator {
	
	public static final String ERROR_EXTENSIBILITY_NOT_ALLOWED = "EXTENSIBILITY_NOT_ALLOWED";
	public static final String ERROR_CONTEXT_OR_LABEL_REQUIRED = "CONTEXT_OR_LABEL_REQUIRED";
	public static final String ERROR_MULTIPLE_ID_MEMBERS       = "MULTIPLE_ID_MEMBERS";
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLFacet target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("facetType", target.getFacetType()).setFindingType(FindingType.ERROR)
			.assertNotNull();
	
		builder.setProperty("aliases", target.getAliases()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		builder.setProperty("attributes", target.getAttributes()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
	
		builder.setProperty("elements", target.getElements()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
	
		builder.setProperty("indicators", target.getIndicators()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		if (ValidatorUtils.hasMultipleIdMembers(target)) {
			builder.addFinding(FindingType.ERROR, "members", ERROR_MULTIPLE_ID_MEMBERS);
		}
		
		if (target.getOwningEntity() instanceof TLBusinessObject) {
			validateBusinessObjectFacet(target, builder);
			
		} else if (target.getOwningEntity() instanceof TLCoreObject) {
			validateCoreObjectFacet(target, builder);
		}
		
		// Make sure that the context value is among the declared contexts for the owning library
		if ((target.getContext() != null) && (target.getContext().length() > 0)) {
			AbstractLibrary owningLibrary = target.getOwningLibrary();
			
			if (owningLibrary instanceof TLLibrary) {
				TLLibrary library = (TLLibrary) owningLibrary;
				
				if (library.getContext(target.getContext()) == null) {
					builder.addFinding(FindingType.ERROR, "context",
							TLContextCompileValidator.ERROR_INVALID_CONTEXT, target.getContext());
				}
			}
		}
		
		checkSchemaNamingConflicts( target, builder );
		
		return builder.getFindings();
	}
	
	/**
	 * Performs specialized checks for facets that are owned by business objects.
	 * 
	 * @param target  the target facet to validate
	 * @param builder  the validation builder used to collect any findings that are discovered
	 */
	private void validateBusinessObjectFacet(TLFacet target, TLValidationBuilder builder) {
		TLBusinessObject businessObject = (TLBusinessObject) target.getOwningEntity();
		TLFacetOwner baseEntity = FacetCodegenUtils.getFacetOwnerExtension(businessObject);
		TLFacetType facetType = target.getFacetType();
		
		// ID facets must define at least one attribute or property (unless the owning business object
		// is an extension of another one).
		if ((facetType == TLFacetType.ID) && (baseEntity == null)) {
			builder.setProperty("ID.members", ValidatorUtils.getMembers(target, false)).setFindingType(FindingType.ERROR)
				.assertMinimumSize(1);
		}
		
		// Enforce special rules for contextual facets
		if (facetType.isContextual()) {
			
			builder.setProperty("context", target.getContext()).setFindingType(FindingType.ERROR)
				.assertPatternMatch(NAME_XML_PATTERN);
			
			builder.setProperty("label", target.getLabel()).setFindingType(FindingType.ERROR)
				.assertPatternMatch(NAME_XML_PATTERN);
			
			// Query facets allow nulls for both context and query; non-query contextual facets require
			// one or both fields to have a value
			if ((facetType != TLFacetType.QUERY)
					&& (trimString(target.getContext()) == null) && (trimString(target.getLabel()) == null)) {
				builder.addFinding(FindingType.ERROR, "contextLabel", ERROR_CONTEXT_OR_LABEL_REQUIRED);
			}
			
			builder.setProperty(facetType.getIdentityName() + ".members", ValidatorUtils.getInheritedMembers(target))
				.setFindingType(FindingType.WARNING)
				.assertMinimumSize(1);
			
		} else { // Enforce special rules for non-contextual facets
			
			builder.setProperty("context", target.getContext()).setFindingType(FindingType.WARNING)
				.assertNullOrBlank();
			
			builder.setProperty("label", target.getLabel()).setFindingType(FindingType.WARNING)
				.assertNullOrBlank();
		}
		
		// Check the uniqueness of the facet's identity for custom/query facets
		if (facetType == TLFacetType.CUSTOM) {
			builder.setProperty("identity", businessObject.getCustomFacets()).setFindingType(FindingType.ERROR)
				.assertNoDuplicates(new IdentityResolver<TLFacet>() {

					@Override
					public String getIdentity(TLFacet entity) {
						return entity.getFacetType().getIdentityName(entity.getContext(), entity.getLabel());
					}
					
				});
			
		} else if (facetType == TLFacetType.QUERY) {
			builder.setProperty("identity", businessObject.getQueryFacets()).setFindingType(FindingType.ERROR)
				.assertNoDuplicates(new IdentityResolver<TLFacet>() {
					
					@Override
					public String getIdentity(TLFacet entity) {
						return entity.getFacetType().getIdentityName(entity.getContext(), entity.getLabel());
					}
					
				});
		}
	}
	
	/**
	 * Performs specialized checks for facets that are owned by business objects.
	 * 
	 * @param target  the target facet to validate
	 * @param builder  the validation builder used to collect any findings that are discovered
	 */
	private void validateCoreObjectFacet(TLFacet target, TLValidationBuilder builder) {
		// No business rules for core object facets at this time
	}
	
}
