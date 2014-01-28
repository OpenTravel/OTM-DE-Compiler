/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLExtensionPointFacetBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLExtensionPointFacet</code> class.
 *
 * @author S. Livezey
 */
public class TLExtensionPointFacetSaveValidator extends TLExtensionPointFacetBaseValidator {
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLExtensionPointFacet target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		// No validation checks required for save operations
		
		return builder.getFindings();
	}

}
