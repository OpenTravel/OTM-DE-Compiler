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

package org.opentravel.schemacompiler.validate.assembly;

import org.opentravel.schemacompiler.repository.ServiceAssembly;
import org.opentravel.schemacompiler.repository.ServiceAssemblyItem;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;

/**
 * Validator for the <code>ServiceAssembly</code> class.
 */
public class ServiceAssemblyValidator extends AssemblyValidatorBase<ServiceAssembly> {

    public static final String ERROR_INVALID_URI = "INVALID_URI";
    public static final String ERROR_VERSION_IDENTIFIER = "VERSION_IDENTIFIER";

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#validate(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    public ValidationFindings validate(ServiceAssembly target) {
        Validator<ServiceAssemblyItem> itemValidator =
            getValidatorFactory().getValidatorForClass( ServiceAssemblyItem.class );
        AssemblyValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( "assemblyUrl", target.getAssemblyUrl() ).setFindingType( FindingType.ERROR )
            .assertNotNull();

        builder.setProperty( "baseNamespace", target.getBaseNamespace() ).setFindingType( FindingType.ERROR )
            .assertNotNullOrBlank().assertContainsNoWhitespace();

        if ((target.getBaseNamespace() != null) && !URLUtils.isValidURI( target.getBaseNamespace() )) {
            builder.addFinding( FindingType.ERROR, "baseNamespace", ERROR_INVALID_URI, target.getBaseNamespace() );
        }

        builder.setProperty( "name", target.getName() ).setFindingType( FindingType.ERROR ).assertNotNullOrBlank();

        builder.setProperty( "version", target.getVersion() ).setFindingType( FindingType.ERROR )
            .assertNotNullOrBlank();

        if ((target.getVersion() != null) && !versionScheme.isValidVersionIdentifier( target.getVersion() )) {
            builder.addFinding( FindingType.ERROR, "version", ERROR_VERSION_IDENTIFIER, target.getVersion() );
        }

        for (ServiceAssemblyItem item : target.getAllApis()) {
            builder.addFindings( itemValidator.validate( item ) );
        }
        return builder.getFindings();
    }

}
