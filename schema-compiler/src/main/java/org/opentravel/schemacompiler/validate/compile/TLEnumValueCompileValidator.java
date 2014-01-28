package org.opentravel.schemacompiler.validate.compile;

import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.IdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEnumValue</code> class.
 * 
 * @author S. Livezey
 */
public class TLEnumValueCompileValidator extends TLValidatorBase<TLEnumValue> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateChildren(TLEnumValue target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("literal", target.getLiteral()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertMaximumLength(80);

        builder.setProperty("literal", target.getOwningEnum().getValues())
                .setFindingType(FindingType.ERROR)
                .assertNoDuplicates(new IdentityResolver<TLEnumValue>() {
                    public String getIdentity(TLEnumValue enumValue) {
                        return (enumValue == null) ? null : enumValue.getLiteral();
                    }
                });

        builder.setProperty("equivalents", target.getEquivalents())
                .setFindingType(FindingType.ERROR).assertNotNull().assertContainsNoNullElements();

        return builder.getFindings();
    }

}
