
package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLBusinessObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLBusinessObjectBaseValidator extends TLValidatorBase<TLBusinessObject> {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLBusinessObject target) {
		Validator<TLAlias> aliasValidator = getValidatorFactory().getValidatorForClass(TLAlias.class);
		Validator<TLFacet> facetValidator = getValidatorFactory().getValidatorForClass(TLFacet.class);
		ValidationFindings findings = new ValidationFindings();
		
		if (target.getExtension() != null) {
			Validator<TLExtension> extensionValidator = getValidatorFactory().getValidatorForClass(TLExtension.class);

			findings.addAll( extensionValidator.validate(target.getExtension()) );
		}
		if (target.getDocumentation() != null) {
			Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
			
			findings.addAll( docValidator.validate(target.getDocumentation()) );
		}
		if (target.getAliases() != null) {
			for (TLAlias alias : target.getAliases()) {
				findings.addAll( aliasValidator.validate(alias) );
			}
		}
		if (target.getEquivalents() != null) {
			Validator<TLEquivalent> equivValidator = getValidatorFactory().getValidatorForClass(TLEquivalent.class);
			
			for (TLEquivalent equiv : target.getEquivalents()) {
				findings.addAll( equivValidator.validate(equiv) );
			}
		}
		if (target.getIdFacet() != null) {
			findings.addAll( facetValidator.validate(target.getIdFacet()) );
		}
		if (target.getSummaryFacet() != null) {
			findings.addAll( facetValidator.validate(target.getSummaryFacet()) );
		}
		if (target.getDetailFacet() != null) {
			findings.addAll( facetValidator.validate(target.getDetailFacet()) );
		}
		for (TLFacet customFacet : target.getCustomFacets()) {
			findings.addAll( facetValidator.validate(customFacet) );
		}
		for (TLFacet queryFacet : target.getQueryFacets()) {
			findings.addAll( facetValidator.validate(queryFacet) );
		}
		return findings;
	}
	
}
