/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.base;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLOperation</code> class.
 * 
 * @author S. Livezey
 */
public class TLOperationBaseValidator extends TLValidatorBase<TLOperation> {

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateChildren(com.sabre.schemacompiler.validate.Validatable)
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
