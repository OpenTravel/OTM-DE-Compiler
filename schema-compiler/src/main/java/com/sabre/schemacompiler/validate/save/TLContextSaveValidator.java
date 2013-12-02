/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.save;

import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLContextBaseValidator;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLContext</code> class.
 *
 * @author S. Livezey
 */
public class TLContextSaveValidator extends TLContextBaseValidator {
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLContext target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("contextId", target.getContextId()).setFindingType(FindingType.WARNING)
			.assertNotNull();
		
		builder.setProperty("applicationContext", target.getApplicationContext()).setFindingType(FindingType.WARNING)
			.assertNotNull();
		
		return builder.getFindings();
	}

}
