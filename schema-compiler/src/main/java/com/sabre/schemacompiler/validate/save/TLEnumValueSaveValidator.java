/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.save;

import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEnumValue</code> class.
 *
 * @author S. Livezey
 */
public class TLEnumValueSaveValidator extends TLValidatorBase<TLEnumValue> {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateChildren(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLEnumValue target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("literal", target.getLiteral()).setFindingType(FindingType.WARNING)
			.assertMaximumLength(80);
		
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.WARNING)
			.assertContainsNoNullElements();
	
		return builder.getFindings();
	}

}
