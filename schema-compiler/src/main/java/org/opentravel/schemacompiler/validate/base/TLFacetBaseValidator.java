
package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLFacet</code> class.
 * 
 * @author S. Livezey
 */
public class TLFacetBaseValidator extends TLValidatorBase<TLFacet> {
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLFacet target) {
		Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
		Validator<TLAttribute> attributeValidator = getValidatorFactory().getValidatorForClass(TLAttribute.class);
		Validator<TLProperty> elementValidator = getValidatorFactory().getValidatorForClass(TLProperty.class);
		Validator<TLIndicator> indicatorValidator = getValidatorFactory().getValidatorForClass(TLIndicator.class);
		ValidationFindings findings = new ValidationFindings();
		
		if (target.getDocumentation() != null) {
			findings.addAll( docValidator.validate(target.getDocumentation()) );
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
		return findings;
	}
	
}
