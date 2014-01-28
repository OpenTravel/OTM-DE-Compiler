/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEnumValue</code> class.
 *
 * @author S. Livezey
 */
public class TLEnumValueSaveValidator extends TLValidatorBase<TLEnumValue> {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
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
