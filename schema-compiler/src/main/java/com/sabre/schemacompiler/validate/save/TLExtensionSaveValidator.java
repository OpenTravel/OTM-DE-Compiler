/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.save;

import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLExtension</code> class.
 *
 * @author S. Livezey
 */
public class TLExtensionSaveValidator extends TLValidatorBase<TLExtension> {
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLExtension target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		// No validation checks required for save operations
		
		return builder.getFindings();
	}

}
