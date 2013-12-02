/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.base;

import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLBusinessObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLBusinessObjectBaseValidator extends TLValidatorBase<TLBusinessObject> {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateChildren(com.sabre.schemacompiler.validate.Validatable)
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
