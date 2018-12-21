/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemacompiler.validate.impl;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.ValidatorFactory;

/**
 * Static utility methods used for the validation of <code>TLModel</code> elements.
 * 
 * @author S. Livezey
 */
public class TLModelValidator {
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private TLModelValidator() {}
	
    /**
     * Utility method that validates all elements of the given model using the specified rule set
     * from the application context file.
     * 
     * @param model
     *            the model whose members should be validated
     * @param validationRuleSetId
     *            the application context ID of the validation rule set to apply
     * @return ValidationFindings
     */
    public static ValidationFindings validateModel(TLModel model, String validationRuleSetId) {
        ValidatorFactory factory = ValidatorFactory.getInstance(validationRuleSetId,
                new TLModelValidationContext(model));
        Validator<TLLibrary> validator = factory.getValidatorForClass(TLLibrary.class);
        ValidationFindings findings = new ValidationFindings();

        if (validator != null) {
            for (TLLibrary library : model.getUserDefinedLibraries()) {
                findings.addAll(validator.validate(library));
            }
        }
        return findings;
    }

    /**
     * Utility method that validates the given model element. If the object has not yet been
     * assigned to a model, some validation tasks may not function properly.
     * 
     * @param modelElement
     *            the model element to validate
     * @param validationRuleSetId
     *            the application context ID of the validation rule set to apply
     * @return TLModelElement
     */
    public static ValidationFindings validateModelElement(TLModelElement modelElement,
            String validationRuleSetId) {
        ValidatorFactory factory = ValidatorFactory.getInstance(validationRuleSetId,
                new TLModelValidationContext(modelElement.getOwningModel()));
        Validator<TLModelElement> validator = factory.getValidatorForTarget(modelElement);
        ValidationFindings findings = new ValidationFindings();

        if (validator != null) {
            findings.addAll(validator.validate(modelElement));
        }
        return findings;
    }

}
