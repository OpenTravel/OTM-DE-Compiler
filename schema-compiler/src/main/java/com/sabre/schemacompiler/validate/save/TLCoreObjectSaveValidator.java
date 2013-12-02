/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.save;

import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLCoreObjectBaseValidator;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLCoreObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLCoreObjectSaveValidator extends TLCoreObjectBaseValidator {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLCoreObject target) {
		TLValidationBuilder builder = newValidationBuilder(target).addFindings( super.validateFields(target) );
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
			.assertPatternMatch(NAME_XML_PATTERN);
	
		return builder.getFindings();
	}
	
}
