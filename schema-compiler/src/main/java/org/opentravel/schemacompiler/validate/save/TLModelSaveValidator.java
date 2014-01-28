package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.ValidatorFactory;
import org.opentravel.schemacompiler.validate.impl.TLModelValidator;

/**
 * Static utility methods used for the validation of <code>TLModel</code> elements prior to library
 * save operations.
 * 
 * @author S. Livezey
 */
public class TLModelSaveValidator {

    /**
     * Utility method that validates all elements of the given model using the default rule set for
     * library saves.
     * 
     * @param model
     *            the model whose members should be validated
     * @return ValidationFindings
     */
    public static ValidationFindings validateModel(TLModel model) {
        return TLModelValidator.validateModel(model, ValidatorFactory.SAVE_RULE_SET_ID);
    }

    /**
     * Utility method that validates the given model element using the specified rule set from the
     * application context file. If the object has not yet been assigned to a model, some validation
     * tasks may not function properly.
     * 
     * @param modelElement
     *            the model element to validate
     * @return TLModelElement
     */
    public static ValidationFindings validateModelElement(TLModelElement modelElement) {
        return TLModelValidator.validateModelElement(modelElement,
                ValidatorFactory.SAVE_RULE_SET_ID);
    }

}
