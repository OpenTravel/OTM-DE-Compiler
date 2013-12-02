/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLClosedEnumerationBaseValidator;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLClosedEnumeration</code> class.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumerationCompileValidator extends TLClosedEnumerationBaseValidator {
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLClosedEnumeration target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank()
			.assertPatternMatch(NAME_XML_PATTERN);
	
		builder.setProperty("values", target.getValues()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements()
			.assertMinimumSize(1);
		
		checkSchemaNamingConflicts( target, builder );
		
		checkMajorVersionNamingConflicts(target, builder);
		
		return builder.getFindings();
	}

}
