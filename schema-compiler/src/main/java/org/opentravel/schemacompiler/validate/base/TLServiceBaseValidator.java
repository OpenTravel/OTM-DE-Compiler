package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLService</code> class.
 * 
 * @author S. Livezey
 */
public class TLServiceBaseValidator extends TLValidatorBase<TLService> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateChildren(TLService target) {
        Validator<TLOperation> operationValidator = getValidatorFactory().getValidatorForClass(
                TLOperation.class);
        ValidationFindings findings = new ValidationFindings();

        if (target.getDocumentation() != null) {
            Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(
                    TLDocumentation.class);

            findings.addAll(docValidator.validate(target.getDocumentation()));
        }
        if (target.getEquivalents() != null) {
            Validator<TLEquivalent> equivValidator = getValidatorFactory().getValidatorForClass(
                    TLEquivalent.class);

            for (TLEquivalent equiv : target.getEquivalents()) {
                findings.addAll(equivValidator.validate(equiv));
            }
        }
        for (TLOperation operation : target.getOperations()) {
            findings.addAll(operationValidator.validate(operation));
        }
        return findings;
    }

}
