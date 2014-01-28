
package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLCoreObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLCoreObjectBaseValidator extends TLValidatorBase<TLCoreObject> {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLCoreObject target) {
		Validator<TLAlias> aliasValidator = getValidatorFactory().getValidatorForClass(TLAlias.class);
		Validator<TLSimpleFacet> simpleFacetValidator = getValidatorFactory().getValidatorForClass(TLSimpleFacet.class);
		Validator<TLFacet> facetValidator = getValidatorFactory().getValidatorForClass(TLFacet.class);
		Validator<TLRole> roleValidator = getValidatorFactory().getValidatorForClass(TLRole.class);
		ValidationFindings findings = new ValidationFindings();
		
		if (target.getAliases() != null) {
			for (TLAlias alias : target.getAliases()) {
				findings.addAll( aliasValidator.validate(alias) );
			}
		}
		if (target.getExtension() != null) {
			Validator<TLExtension> extensionValidator = getValidatorFactory().getValidatorForClass(TLExtension.class);

			findings.addAll( extensionValidator.validate(target.getExtension()) );
		}
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
		if (target.getSimpleFacet() != null) {
			findings.addAll( simpleFacetValidator.validate(target.getSimpleFacet()) );
		}
		if (target.getSummaryFacet() != null) {
			findings.addAll( facetValidator.validate(target.getSummaryFacet()) );
		}
		if (target.getDetailFacet() != null) {
			findings.addAll( facetValidator.validate(target.getDetailFacet()) );
		}
		if (target.getRoleEnumeration().getRoles() != null) {
			for (TLRole role : target.getRoleEnumeration().getRoles()) {
				findings.addAll( roleValidator.validate(role) );
			}
		}
		return findings;
	}
	
}
