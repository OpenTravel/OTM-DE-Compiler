/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLRoleBaseValidator;
import com.sabre.schemacompiler.validate.impl.IdentityResolver;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLRole</code> class.
 *
 * @author S. Livezey
 */
public class TLRoleCompileValidator extends TLRoleBaseValidator {
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLRole target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank()
			.assertPatternMatch(NAME_XML_PATTERN);
		
		builder.setProperty("name", target.getRoleEnumeration().getRoles()).setFindingType(FindingType.ERROR)
			.assertNoDuplicates(
					new IdentityResolver<TLRole>() {
						public String getIdentity(TLRole role) {
							return (role == null) ? null : role.getName();
						}
					});

		return builder.getFindings();
	}

}
