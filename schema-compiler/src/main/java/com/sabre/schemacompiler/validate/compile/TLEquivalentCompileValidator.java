/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.impl.IdentityResolver;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEquivalent</code> class.
 *
 * @author S. Livezey
 */
public class TLEquivalentCompileValidator extends TLValidatorBase<TLEquivalent> {
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLEquivalent target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("context", target.getContext()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank();
		
		/*
		Commented out until better editing capabilities are available in the GUI for equivalents
		
		builder.setProperty("description", target.getDescription()).setFindingType(FindingType.WARNING)
			.assertNotNullOrBlank();
		 */
		
		builder.setProperty("context", target.getOwningEntity().getEquivalents()).setFindingType(FindingType.ERROR)
			.assertNoDuplicates(
					new IdentityResolver<TLEquivalent>() {
						public String getIdentity(TLEquivalent entity) {
							return (entity == null) ? null : entity.getContext();
						}
					});
		
		// Make sure that the context value is among the declared contexts for the owning library
		if ((target.getContext() != null) && (target.getContext().length() > 0)) {
			AbstractLibrary owningLibrary = target.getOwningLibrary();
			
			if (owningLibrary instanceof TLLibrary) {
				TLLibrary library = (TLLibrary) owningLibrary;
				
				if (library.getContext(target.getContext()) == null) {
					builder.addFinding(FindingType.ERROR, "context",
							TLContextCompileValidator.ERROR_INVALID_CONTEXT, target.getContext());
				}
			}
		}
		return builder.getFindings();
	}

}
