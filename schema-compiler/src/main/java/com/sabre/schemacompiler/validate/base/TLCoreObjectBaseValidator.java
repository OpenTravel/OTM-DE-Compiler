/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.base;

import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLCoreObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLCoreObjectBaseValidator extends TLValidatorBase<TLCoreObject> {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateChildren(com.sabre.schemacompiler.validate.Validatable)
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
