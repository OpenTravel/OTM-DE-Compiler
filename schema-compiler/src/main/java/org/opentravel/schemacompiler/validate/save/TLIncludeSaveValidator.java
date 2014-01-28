package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLInclude</code> class.
 * 
 * @author S. Livezey
 */
public class TLIncludeSaveValidator extends TLValidatorBase<TLInclude> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLInclude target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("path", target.getPath()).setFindingType(FindingType.WARNING)
                .assertNotNullOrBlank().assertContainsNoWhitespace();

        return builder.getFindings();
    }

}
