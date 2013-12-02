/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.OperationType;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLOperationBaseValidator;
import com.sabre.schemacompiler.validate.impl.IdentityResolver;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLOperation</code> class.
 * 
 * @author S. Livezey
 */
public class TLOperationCompileValidator extends TLOperationBaseValidator {

	public static final String ERROR_INVALID_OPERATION         = "INVALID_OPERATION";
	public static final String ERROR_INVALID_VERSION_EXTENSION = "INVALID_VERSION_EXTENSION";
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLOperation target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank()
			.assertPatternMatch(NAME_XML_PATTERN);
		
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		builder.setProperty("name", target.getOwningService().getOperations()).setFindingType(FindingType.ERROR)
			.assertNoDuplicates(
					new IdentityResolver<TLOperation>() {
						public String getIdentity(TLOperation operation) {
							return (operation == null) ? null : operation.getName();
						}
					});

		if (target.getOperationType() == OperationType.INVALID) {
			builder.addFinding(FindingType.ERROR, "operationType", ERROR_INVALID_OPERATION);
		}
		
		if (isInvalidVersionExtension(target)) {
			builder.addFinding(FindingType.ERROR, "versionExtension", ERROR_INVALID_VERSION_EXTENSION);
		}
		
		checkMajorVersionNamingConflicts(target, builder);
		
		return builder.getFindings();
	}

}
