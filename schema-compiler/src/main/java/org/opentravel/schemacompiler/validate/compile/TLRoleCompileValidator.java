package org.opentravel.schemacompiler.validate.compile;

import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLRoleBaseValidator;
import org.opentravel.schemacompiler.validate.impl.IdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLRole</code> class.
 * 
 * @author S. Livezey
 */
public class TLRoleCompileValidator extends TLRoleBaseValidator {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLRole target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);

        builder.setProperty("name", target.getRoleEnumeration().getRoles())
                .setFindingType(FindingType.ERROR)
                .assertNoDuplicates(new IdentityResolver<TLRole>() {
                    public String getIdentity(TLRole role) {
                        return (role == null) ? null : role.getName();
                    }
                });

        return builder.getFindings();
    }

}
