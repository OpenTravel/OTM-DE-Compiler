package org.opentravel.schemacompiler.validate.compile;

import org.opentravel.schemacompiler.model.OperationType;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLOperationBaseValidator;
import org.opentravel.schemacompiler.validate.impl.IdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLOperation</code> class.
 * 
 * @author S. Livezey
 */
public class TLOperationCompileValidator extends TLOperationBaseValidator {

    public static final String ERROR_INVALID_OPERATION = "INVALID_OPERATION";
    public static final String ERROR_INVALID_VERSION_EXTENSION = "INVALID_VERSION_EXTENSION";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLOperation target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);

        builder.setProperty("equivalents", target.getEquivalents())
                .setFindingType(FindingType.ERROR).assertNotNull().assertContainsNoNullElements();

        builder.setProperty("name", target.getOwningService().getOperations())
                .setFindingType(FindingType.ERROR)
                .assertNoDuplicates(new IdentityResolver<TLOperation>() {
                    public String getIdentity(TLOperation operation) {
                        return (operation == null) ? null : operation.getName();
                    }
                });

        if (target.getOperationType() == OperationType.INVALID) {
            builder.addFinding(FindingType.ERROR, "operationType", ERROR_INVALID_OPERATION);
        }

        if (isInvalidVersionExtension(target)) {
            builder.addFinding(FindingType.ERROR, "versionExtension",
                    ERROR_INVALID_VERSION_EXTENSION);
        }

        checkMajorVersionNamingConflicts(target, builder);

        return builder.getFindings();
    }

}
