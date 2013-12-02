/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.XSDSimpleType;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLSimpleFacetBaseValidator;
import com.sabre.schemacompiler.validate.impl.CircularReferenceChecker;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLSimpleFacet</code> class.
 * 
 *
 * @author S. Livezey
 */
public class TLSimpleFacetCompileValidator extends TLSimpleFacetBaseValidator {

	public static final String ERROR_INVALID_SIMPLE_CORE_VERSION = "INVALID_SIMPLE_CORE_VERSION";
	public static final String ERROR_INVALID_CIRCULAR_REFERENCE  = "INVALID_CIRCULAR_REFERENCE";

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLSimpleFacet target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setEntityReferenceProperty("simpleType", target.getSimpleType(), target.getSimpleTypeName())
			.setFindingType(FindingType.ERROR)
				.assertValidEntityReference(TLCoreObject.class, TLSimpleFacet.class,
						TLClosedEnumeration.class, TLSimple.class, XSDSimpleType.class)
			.setFindingType(FindingType.WARNING)
				.assertNotNull()
				.assertNotDeprecated();
		
		checkEmptyValueType(target, target.getSimpleType(), "simpleType", builder);
		
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		// For core objects, make sure the simple facet type did not change if the target
		// belongs to a later version of the core.
		if (target.getOwningEntity() instanceof TLCoreObject) {
			TLCoreObject extendedCore = getExtendedVersion( (TLCoreObject) target.getOwningEntity() );
			
			if ((extendedCore != null) && (extendedCore.getSimpleFacet().getSimpleType() != target.getSimpleType())) {
				builder.addFinding(FindingType.ERROR, "simpleType", ERROR_INVALID_SIMPLE_CORE_VERSION);
			}
		}
		
		if ( CircularReferenceChecker.hasCircularReference(target) ) {
			builder.addFinding(FindingType.ERROR, "simpleType", ERROR_INVALID_CIRCULAR_REFERENCE);
		}
		
		checkSchemaNamingConflicts( target, builder );
		
		return builder.getFindings();
	}
	
}
