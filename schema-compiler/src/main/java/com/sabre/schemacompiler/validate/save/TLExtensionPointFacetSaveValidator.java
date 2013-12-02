/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.save;

import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLExtensionPointFacetBaseValidator;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLExtensionPointFacet</code> class.
 *
 * @author S. Livezey
 */
public class TLExtensionPointFacetSaveValidator extends TLExtensionPointFacetBaseValidator {
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLExtensionPointFacet target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		// No validation checks required for save operations
		
		return builder.getFindings();
	}

}
