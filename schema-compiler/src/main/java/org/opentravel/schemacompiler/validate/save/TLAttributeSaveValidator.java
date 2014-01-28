/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLAttributeBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLAttribute</code> class.
 * 
 * @author S. Livezey
 */
public class TLAttributeSaveValidator extends TLAttributeBaseValidator {

	public static final String ERROR_NON_SIMPLE_CORE_AS_ATTRIBUTE = "NON_SIMPLE_CORE_AS_ATTRIBUTE";
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLAttribute target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
			.assertPatternMatch(NAME_XML_PATTERN);
		
		return builder.getFindings();
	}

}
