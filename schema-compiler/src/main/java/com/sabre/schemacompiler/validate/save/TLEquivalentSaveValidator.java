/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.save;

import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEquivalent</code> class.
 *
 * @author S. Livezey
 */
public class TLEquivalentSaveValidator extends TLValidatorBase<TLEquivalent> {
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLEquivalent target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("context", target.getContext()).setFindingType(FindingType.WARNING)
			.assertNotNull();
		
		return builder.getFindings();
	}

}
