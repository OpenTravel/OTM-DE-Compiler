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

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLCoreObjectBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLCoreObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLCoreObjectCompileValidator extends TLCoreObjectBaseValidator {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLCoreObject target) {
        TLValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( "name", target.getName() ).setFindingType( FindingType.ERROR ).assertNotNullOrBlank()
            .assertPatternMatch( NAME_XML_PATTERN );

        builder.setProperty( "roles", target.getRoleEnumeration().getRoles() ).setFindingType( FindingType.ERROR )
            .assertNotNull().assertContainsNoNullElements();

        builder.setProperty( "equivalents", target.getEquivalents() ).setFindingType( FindingType.ERROR )
            .assertNotNull().assertContainsNoNullElements();

        if (!target.getSummaryFacet().declaresContent()) {
            TLFacetOwner baseEntity = FacetCodegenUtils.getFacetOwnerExtension( target );

            if (baseEntity == null) {
                builder.addFinding( FindingType.ERROR, "summaryFacet", TLValidationBuilder.ERROR_UNDER_MINIMUM_SIZE );
            }
        }

        checkSchemaNamingConflicts( target, builder );
        validateVersioningRules( target, builder );

        return builder.getFindings();
    }

}
