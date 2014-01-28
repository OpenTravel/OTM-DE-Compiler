package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLSimpleBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLSimple</code> class.
 * 
 * @author S. Livezey
 */
public class TLSimpleSaveValidator extends TLSimpleBaseValidator {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLSimple target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
                .assertPatternMatch(NAME_XML_PATTERN);

        builder.setProperty("equivalents", target.getEquivalents())
                .setFindingType(FindingType.WARNING).assertNotNull().assertContainsNoNullElements();

        return builder.getFindings();
    }

}
