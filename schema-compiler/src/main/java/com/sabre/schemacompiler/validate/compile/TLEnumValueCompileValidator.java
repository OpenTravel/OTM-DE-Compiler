/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.impl.IdentityResolver;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEnumValue</code> class.
 *
 * @author S. Livezey
 */
public class TLEnumValueCompileValidator extends TLValidatorBase<TLEnumValue> {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateChildren(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLEnumValue target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("literal", target.getLiteral()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank()
			.assertMaximumLength(80);
		
		builder.setProperty("literal", target.getOwningEnum().getValues()).setFindingType(FindingType.ERROR)
			.assertNoDuplicates(
					new IdentityResolver<TLEnumValue>() {
						public String getIdentity(TLEnumValue enumValue) {
							return (enumValue == null) ? null : enumValue.getLiteral();
						}
					});
	
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		return builder.getFindings();
	}

}
