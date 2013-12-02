/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAliasOwner;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.impl.IdentityResolver;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLAlias</code> class.
 *
 * @author S. Livezey
 */
public class TLAliasCompileValidator extends TLValidatorBase<TLAlias> {
	
	public static final String ERROR_ILLEGAL_ALIAS_NAME = "ILLEGAL_ALIAS_NAME";
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLAlias target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		TLAliasOwner owner = target.getOwningEntity();
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank()
			.assertPatternMatch(NAME_XML_PATTERN);
		
		if (owner != null) {
			builder.setProperty("name", target.getOwningEntity().getAliases()).setFindingType(FindingType.ERROR)
				.assertNoDuplicates(
						new IdentityResolver<TLAlias>() {
							public String getIdentity(TLAlias alias) {
								return (alias == null) ? null : alias.getName();
							}
						});
		
			// Add an error if the alias name is the same as that of its owner
			if ((target.getName() != null) && target.getName().equals(target.getOwningEntity().getLocalName())) {
				builder.addFinding(FindingType.ERROR, "name", ERROR_ILLEGAL_ALIAS_NAME, target.getName());
			}
		}
		
		checkSchemaNamingConflicts( target, builder );
		
		return builder.getFindings();
	}

}
