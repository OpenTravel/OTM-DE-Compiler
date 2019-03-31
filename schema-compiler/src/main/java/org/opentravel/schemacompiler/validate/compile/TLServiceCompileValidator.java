/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.validate.compile;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLServiceBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;

/**
 * Validator for the <code>TLService</code> class.
 * 
 * @author S. Livezey
 */
public class TLServiceCompileValidator extends TLServiceBaseValidator {

    public static final String ERROR_ILLEGAL_SERVICE_VERSION = "ILLEGAL_SERVICE_VERSION";
    public static final String ERROR_ILLEGAL_PATCH = "ILLEGAL_PATCH";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLService target) {
        TLValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( "name", target.getName() ).setFindingType( FindingType.ERROR ).assertNotNullOrBlank()
            .assertPatternMatch( NAME_XML_PATTERN );

        builder.setProperty( "equivalents", target.getEquivalents() ).setFindingType( FindingType.ERROR )
            .assertNotNull().assertContainsNoNullElements();

        builder.setProperty( "operations", target.getOperations() ).setFindingType( FindingType.ERROR )
            .assertMinimumSize( 1 );

        // Validate versioning rules
        try {
            if ((target.getOwningLibrary() instanceof TLLibrary) && (target.getName() != null)) {
                TLLibrary owningLibrary = (TLLibrary) target.getOwningLibrary();
                MinorVersionHelper helper = new MinorVersionHelper();
                VersionScheme vScheme = helper.getVersionScheme( owningLibrary );

                if ((vScheme != null) && vScheme.isPatchVersion( owningLibrary.getNamespace() )) {
                    builder.addFinding( FindingType.ERROR, "name", ERROR_ILLEGAL_PATCH );

                } else {
                    TLLibrary previousLibraryVersion = helper.getPriorMinorVersion( owningLibrary );
                    boolean hasError = false;

                    while (!hasError && (previousLibraryVersion != null)) {
                        TLService previousServiceVersion = previousLibraryVersion.getService();

                        hasError = (previousServiceVersion != null)
                            && !target.getName().equals( previousServiceVersion.getName() );
                        previousLibraryVersion = helper.getPriorMinorVersion( previousLibraryVersion );
                    }
                    if (hasError) {
                        builder.addFinding( FindingType.ERROR, "name", ERROR_ILLEGAL_SERVICE_VERSION,
                            target.getName() );
                    }
                }
            }
        } catch (VersionSchemeException e) {
            // Ignore - Invalid version scheme error will be reported when the owning library is
            // validated
        }

        return builder.getFindings();
    }

}
