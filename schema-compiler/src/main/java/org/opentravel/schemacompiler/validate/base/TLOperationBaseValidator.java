
package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLOperation</code> class.
 * 
 * @author S. Livezey
 */
public class TLOperationBaseValidator extends TLValidatorBase<TLOperation> {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLOperation target) {
		Validator<TLFacet> facetValidator = getValidatorFactory().getValidatorForClass(TLFacet.class);
		Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
		Validator<TLExtension> extensionValidator = getValidatorFactory().getValidatorForClass(TLExtension.class);
		ValidationFindings findings = new ValidationFindings();
		
		if (target.getExtension() != null) {
			findings.addAll( extensionValidator.validate(target.getExtension()) );
		}
		if (target.getDocumentation() != null) {
			findings.addAll( docValidator.validate(target.getDocumentation()) );
		}
		if (target.getEquivalents() != null) {
			Validator<TLEquivalent> equivValidator = getValidatorFactory().getValidatorForClass(TLEquivalent.class);
			
			for (TLEquivalent equiv : target.getEquivalents()) {
				findings.addAll( equivValidator.validate(equiv) );
			}
		}
		if (target.getRequest() != null) {
			findings.addAll( facetValidator.validate(target.getRequest()) );
		}
		if (target.getResponse() != null) {
			findings.addAll( facetValidator.validate(target.getResponse()) );
		}
		if (target.getNotification() != null) {
			findings.addAll( facetValidator.validate(target.getNotification()) );
		}
		return findings;
	}
	
}
