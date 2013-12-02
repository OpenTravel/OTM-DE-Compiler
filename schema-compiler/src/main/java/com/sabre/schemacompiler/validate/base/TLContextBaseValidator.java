/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.base;

import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLContext</code> class.
 * 
 * @author S. Livezey
 */
public class TLContextBaseValidator extends TLValidatorBase<TLContext> {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateChildren(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLContext target) {
		ValidationFindings findings = new ValidationFindings();
		
		if (target.getDocumentation() != null) {
			Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
			
			findings.addAll( docValidator.validate(target.getDocumentation()) );
		}
		return findings;
	}
	
}
