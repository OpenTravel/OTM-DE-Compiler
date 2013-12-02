/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.save;

import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLPropertyBaseValidator;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLProperty</code> class.
 * 
 * @author S. Livezey
 */
public class TLPropertySaveValidator extends TLPropertyBaseValidator {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLProperty target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
			.assertPatternMatch(NAME_XML_PATTERN);
		
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();

		return builder.getFindings();
	}
	
}
