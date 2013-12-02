/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.base;

import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLExtensionPointFacet</code> class.
 * 
 * @author S. Livezey
 */
public class TLExtensionPointFacetBaseValidator extends TLValidatorBase<TLExtensionPointFacet> {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateChildren(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLExtensionPointFacet target) {
		Validator<TLAttribute> attributeValidator = getValidatorFactory().getValidatorForClass(TLAttribute.class);
		Validator<TLProperty> elementValidator = getValidatorFactory().getValidatorForClass(TLProperty.class);
		Validator<TLIndicator> indicatorValidator = getValidatorFactory().getValidatorForClass(TLIndicator.class);
		ValidationFindings findings = new ValidationFindings();
		
		if (target.getExtension() != null) {
			Validator<TLExtension> extensionValidator = getValidatorFactory().getValidatorForClass(TLExtension.class);

			findings.addAll( extensionValidator.validate(target.getExtension()) );
		}
		if (target.getAttributes() != null) {
			for (TLAttribute attribute : target.getAttributes()) {
				if (attribute != null) {
					findings.addAll( attributeValidator.validate(attribute) );
				}
			}
		}
		if (target.getElements() != null) {
			for (TLProperty element : target.getElements()) {
				if (element != null) {
					findings.addAll( elementValidator.validate(element) );
				}
			}
		}
		if (target.getIndicators() != null) {
			for (TLIndicator indicator : target.getIndicators()) {
				if (indicator != null) {
					findings.addAll( indicatorValidator.validate(indicator) );
				}
			}
		}
		if (target.getDocumentation() != null) {
			Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
			
			findings.addAll( docValidator.validate(target.getDocumentation()) );
		}
		return findings;
	}
	
}
