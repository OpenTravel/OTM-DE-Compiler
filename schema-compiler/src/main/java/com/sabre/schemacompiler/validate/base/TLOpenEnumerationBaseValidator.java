/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.base;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLOpenEnumeration</code> class.
 * 
 * @author S. Livezey
 */
public class TLOpenEnumerationBaseValidator extends TLValidatorBase<TLOpenEnumeration> {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateChildren(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLOpenEnumeration target) {
		Validator<TLEnumValue> enumValueValidator = getValidatorFactory().getValidatorForClass(TLEnumValue.class);
		ValidationFindings findings = new ValidationFindings();
		
		for (TLEnumValue enumValue : target.getValues()) {
			findings.addAll( enumValueValidator.validate(enumValue) );
		}
		
		if (target.getDocumentation() != null) {
			Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
			
			findings.addAll( docValidator.validate(target.getDocumentation()) );
		}
		return findings;
	}
	
}
