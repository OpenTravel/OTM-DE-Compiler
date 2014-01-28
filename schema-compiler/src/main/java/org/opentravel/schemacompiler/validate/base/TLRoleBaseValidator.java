package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLRole</code> class.
 * 
 * @author S. Livezey
 */
public class TLRoleBaseValidator extends TLValidatorBase<TLRole> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateChildren(TLRole target) {
        ValidationFindings findings = new ValidationFindings();

        if (target.getDocumentation() != null) {
            Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(
                    TLDocumentation.class);

            findings.addAll(docValidator.validate(target.getDocumentation()));
        }
        return findings;
    }

}
