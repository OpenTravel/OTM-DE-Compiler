/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.TLInclude;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLInclude</code> class.
 * 
 * @author S. Livezey
 */
public class TLIncludeCompileValidator extends TLValidatorBase<TLInclude> {
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLInclude target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("path", target.getPath()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank()
			.assertContainsNoWhitespace();
		
		return builder.getFindings();
	}

}
