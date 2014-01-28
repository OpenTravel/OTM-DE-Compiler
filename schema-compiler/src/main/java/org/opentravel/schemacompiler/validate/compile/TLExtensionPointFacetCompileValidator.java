package org.opentravel.schemacompiler.validate.compile;

import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLExtensionPointFacetBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.PatchVersionHelper;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;

/**
 * Validator for the <code>TLExtensionPointFacet</code> class.
 * 
 * @author S. Livezey
 */
public class TLExtensionPointFacetCompileValidator extends TLExtensionPointFacetBaseValidator {

    public static final String ERROR_MULTIPLE_ID_MEMBERS = "MULTIPLE_ID_MEMBERS";
    public static final String ERROR_ILLEGAL_PATCH1 = "ILLEGAL_PATCH1";
    public static final String ERROR_ILLEGAL_PATCH2 = "ILLEGAL_PATCH2";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLExtensionPointFacet target) {
        TLValidationBuilder builder = newValidationBuilder(target);
        TLExtension extension = target.getExtension();

        if (extension == null) {
            builder.addFinding(FindingType.ERROR, "extendsEntity",
                    TLValidationBuilder.ERROR_NULL_VALUE);
        }

        if (ValidatorUtils.hasMultipleIdMembers(target)) {
            builder.addFinding(FindingType.ERROR, "members", ERROR_MULTIPLE_ID_MEMBERS);
        }

        // Validate versioning rules
        try {
            if ((extension != null) && (extension.getExtendsEntity() != null)
                    && (target.getOwningLibrary() instanceof TLLibrary)) {
                TLLibrary owningLibrary = (TLLibrary) target.getOwningLibrary();
                PatchVersionHelper helper = new PatchVersionHelper();
                VersionScheme vScheme = helper.getVersionScheme(owningLibrary);

                if (vScheme != null) {
                    if (vScheme.isPatchVersion(owningLibrary.getNamespace())) {
                        // Make sure the patch modifies an entity in the minor version library being
                        // patched
                        MinorVersionHelper mvHelper = new MinorVersionHelper();
                        TLLibrary minorVersionLibrary = mvHelper
                                .getPriorMinorVersion(owningLibrary);
                        boolean isLegalPatch = (extension.getExtendsEntity().getOwningLibrary() == minorVersionLibrary);
                        String patchedVersionId = minorVersionLibrary.getVersion();

                        while ((minorVersionLibrary != null) && !isLegalPatch) {
                            minorVersionLibrary = mvHelper
                                    .getPriorMinorVersion(minorVersionLibrary);
                            isLegalPatch = (extension.getExtendsEntity().getOwningLibrary() == minorVersionLibrary);
                        }
                        if (!isLegalPatch) {
                            builder.addFinding(FindingType.ERROR, "extendsEntity",
                                    ERROR_ILLEGAL_PATCH2, extension.getExtendsEntityName(),
                                    patchedVersionId);
                        }
                    } else { // major or minor version library
                        if (helper.getPatchedVersion(target) != null) { // is the patched entity
                                                                        // assigned to the same base
                                                                        // namespace?
                            builder.addFinding(FindingType.ERROR, "extendsEntity",
                                    ERROR_ILLEGAL_PATCH1);
                        }
                    }
                }
            }
        } catch (VersionSchemeException e) {
            // Ignore - Invalid version scheme error will be reported when the owning library is
            // validated
        }

        checkSchemaNamingConflicts(target, builder);

        return builder.getFindings();
    }

}
