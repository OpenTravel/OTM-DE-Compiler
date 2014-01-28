
package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLValueWithAttributes</code> class.
 * 
 * @author S. Livezey
 */
public class TLValueWithAttributesBaseValidator extends TLValidatorBase<TLValueWithAttributes> {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLValueWithAttributes target) {
		Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
		Validator<TLAttribute> attributeValidator = getValidatorFactory().getValidatorForClass(TLAttribute.class);
		Validator<TLIndicator> indicatorValidator = getValidatorFactory().getValidatorForClass(TLIndicator.class);
		ValidationFindings findings = new ValidationFindings();
		
		if (target.getDocumentation() != null) {
			findings.addAll( docValidator.validate(target.getDocumentation()) );
		}
		if (target.getValueDocumentation() != null) {
			findings.addAll( docValidator.validate(target.getValueDocumentation()) );
		}
		if (target.getAttributes() != null) {
			for (TLAttribute attribute : target.getAttributes()) {
				if (attribute != null) {
					findings.addAll( attributeValidator.validate(attribute) );
				}
			}
		}
		if (target.getIndicators() != null) {
			for (TLIndicator indicators : target.getIndicators()) {
				if (indicators != null) {
					findings.addAll( indicatorValidator.validate(indicators) );
				}
			}
		}
		if (target.getEquivalents() != null) {
			Validator<TLEquivalent> equivValidator = getValidatorFactory().getValidatorForClass(TLEquivalent.class);
			
			for (TLEquivalent equiv : target.getEquivalents()) {
				findings.addAll( equivValidator.validate(equiv) );
			}
		}
		if (target.getExamples() != null) {
			Validator<TLExample> exampleValidator = getValidatorFactory().getValidatorForClass(TLExample.class);
			
			for (TLExample example : target.getExamples()) {
				findings.addAll( exampleValidator.validate(example) );
			}
		}
		return findings;
	}
	
}
