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

package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLLibraryBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLLibrary</code> class.
 * 
 * @author S. Livezey
 */
public class TLLibrarySaveValidator extends TLLibraryBaseValidator {

    public static final String ERROR_DUPLICATE_LIBRARY_MEMBER_NAME = "DUPLICATE_LIBRARY_MEMBER_NAME";
    public static final String ERROR_DUPLICATE_SERVICE_NAME = "DUPLICATE_SERVICE_NAME";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLLibrary target) {
        TLValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( "libraryUrl", target.getLibraryUrl() ).setFindingType( FindingType.WARNING )
            .assertNotNull();

        builder.setProperty( "name", target.getName() ).setFindingType( FindingType.WARNING )
            .assertPatternMatch( NAME_FILE_PATTERN );

        builder.setProperty( "versionScheme", target.getVersionScheme() ).setFindingType( FindingType.WARNING )
            .assertNotNullOrBlank();

        builder.setProperty( "namespace", target.getNamespace() ).setFindingType( FindingType.WARNING )
            .assertNotNullOrBlank();

        builder.setProperty( "prefix", target.getPrefix() ).setFindingType( FindingType.WARNING )
            .assertNotNullOrBlank();

        builder.setProperty( "includes", target.getIncludes() ).setFindingType( FindingType.WARNING )
            .assertContainsNoNullElements();

        builder.setProperty( "namedMembers", target.getNamedMembers() ).setFindingType( FindingType.WARNING )
            .assertContainsNoNullElements();

        return builder.getFindings();
    }

}
