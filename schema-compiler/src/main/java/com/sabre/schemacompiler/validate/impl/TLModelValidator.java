/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.impl;

import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.ValidatorFactory;

/**
 * Static utility methods used for the validation of <code>TLModel</code> elements.
 *
 * @author S. Livezey
 */
public class TLModelValidator {
	
	/**
	 * Utility method that validates all elements of the given model using the specified rule set
	 * from the application context file.
	 * 
	 * @param model  the model whose members should be validated
	 * @param validationRuleSetId  the application context ID of the validation rule set to apply
	 * @return ValidationFindings
	 */
	public static ValidationFindings validateModel(TLModel model, String validationRuleSetId) {
		ValidatorFactory factory = ValidatorFactory.getInstance(validationRuleSetId, new TLModelValidationContext(model));
		Validator<TLLibrary> validator = factory.getValidatorForClass(TLLibrary.class);
		ValidationFindings findings = new ValidationFindings();
		
		if (validator != null) {
			for (TLLibrary library : model.getUserDefinedLibraries()) {
				findings.addAll( validator.validate(library) );
			}
		}
		return findings;
	}
	
	/**
	 * Utility method that validates the given model element.  If the object has not yet been assigned
	 * to a model, some validation tasks may not function properly.
	 * 
	 * @param modelElement  the model element to validate
	 * @param validationRuleSetId  the application context ID of the validation rule set to apply
	 * @return TLModelElement
	 */
	public static ValidationFindings validateModelElement(TLModelElement modelElement, String validationRuleSetId) {
		ValidatorFactory factory = ValidatorFactory.getInstance(validationRuleSetId,
				new TLModelValidationContext(modelElement.getOwningModel()));
		Validator<TLModelElement> validator = factory.getValidatorForTarget(modelElement);
		ValidationFindings findings = new ValidationFindings();
		
		if (validator != null) {
			findings.addAll( validator.validate(modelElement) );
		}
		return findings;
	}
	
}
