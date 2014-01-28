package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLContextBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLContext</code> class.
 * 
 * @author S. Livezey
 */
public class TLContextSaveValidator extends TLContextBaseValidator {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLContext target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("contextId", target.getContextId()).setFindingType(FindingType.WARNING)
                .assertNotNull();

        builder.setProperty("applicationContext", target.getApplicationContext())
                .setFindingType(FindingType.WARNING).assertNotNull();

        return builder.getFindings();
    }

}
