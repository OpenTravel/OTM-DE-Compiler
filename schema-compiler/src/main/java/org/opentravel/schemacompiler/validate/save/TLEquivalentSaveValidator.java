package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEquivalent</code> class.
 * 
 * @author S. Livezey
 */
public class TLEquivalentSaveValidator extends TLValidatorBase<TLEquivalent> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLEquivalent target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("context", target.getContext()).setFindingType(FindingType.WARNING)
                .assertNotNull();

        return builder.getFindings();
    }

}
