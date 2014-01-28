
package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLAttribute</code> class.
 * 
 * @author S. Livezey
 */
public class TLAttributeBaseValidator extends TLValidatorBase<TLAttribute> {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLAttribute target) {
		ValidationFindings findings = new ValidationFindings();
		
		if (target.getDocumentation() != null) {
			Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
			
			findings.addAll( docValidator.validate(target.getDocumentation()) );
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
