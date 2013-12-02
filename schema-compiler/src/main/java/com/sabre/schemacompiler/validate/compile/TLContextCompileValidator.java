/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLContextBaseValidator;
import com.sabre.schemacompiler.validate.impl.IdentityResolver;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLContext</code> class.
 *
 * @author S. Livezey
 */
public class TLContextCompileValidator extends TLContextBaseValidator {
	
	public static final String ERROR_INVALID_CONTEXT = "INVALID_CONTEXT";

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLContext target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("contextId", target.getContextId()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank();
		
		builder.setProperty("applicationContext", target.getApplicationContext()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank();
		
		builder.setProperty("applicationContext", target.getOwningLibrary().getContexts()).setFindingType(FindingType.ERROR)
			.assertNoDuplicates(
				new IdentityResolver<TLContext>() {
					public String getIdentity(TLContext entity) {
						return (entity == null) ? null : entity.getApplicationContext();
					}
				});
		
		builder.setProperty("contextId", target.getOwningLibrary().getContexts()).setFindingType(FindingType.ERROR)
			.assertNoDuplicates(
				new IdentityResolver<TLContext>() {
					public String getIdentity(TLContext entity) {
						return (entity == null) ? null : entity.getContextId();
					}
				});
	
		return builder.getFindings();
	}

}
