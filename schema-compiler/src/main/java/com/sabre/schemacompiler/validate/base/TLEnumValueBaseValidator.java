/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.base;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEnumValue</code> class.
 *
 * @author S. Livezey
 */
public class TLEnumValueBaseValidator extends TLValidatorBase<TLEnumValue> {

	public static final String ERROR_DUPLICATE_ENUM_LITERAL = "DUPLICATE_ENUM_LITERAL";
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLEnumValue target) {
		Validator<TLEquivalent> equivValidator = getValidatorFactory().getValidatorForClass(TLEquivalent.class);
		Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
		TLValidationBuilder builder = newValidationBuilder(target);
		
		for (TLEquivalent equiv : target.getEquivalents()) {
			builder.addFindings( equivValidator.validate(equiv) );
		}
		
		if (target.getDocumentation() != null) {
			builder.addFindings( docValidator.validate(target.getDocumentation()) );
		}
		
		return builder.getFindings();
	}
	
}
