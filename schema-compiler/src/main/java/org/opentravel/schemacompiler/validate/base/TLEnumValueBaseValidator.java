package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEnumValue</code> class.
 * 
 * @author S. Livezey
 */
public class TLEnumValueBaseValidator extends TLValidatorBase<TLEnumValue> {

    public static final String ERROR_DUPLICATE_ENUM_LITERAL = "DUPLICATE_ENUM_LITERAL";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLEnumValue target) {
        Validator<TLEquivalent> equivValidator = getValidatorFactory().getValidatorForClass(
                TLEquivalent.class);
        Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(
                TLDocumentation.class);
        TLValidationBuilder builder = newValidationBuilder(target);

        for (TLEquivalent equiv : target.getEquivalents()) {
            builder.addFindings(equivValidator.validate(equiv));
        }

        if (target.getDocumentation() != null) {
            builder.addFindings(docValidator.validate(target.getDocumentation()));
        }

        return builder.getFindings();
    }

}
