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

import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLBusinessObjectBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLBusinessObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLBusinessObjectCompileValidator extends TLBusinessObjectBaseValidator {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLBusinessObject target) {
        TLValidationBuilder builder = newValidationBuilder( target );
        ValidationFindings findings;

        builder.setProperty( "name", target.getName() ).setFindingType( FindingType.ERROR ).assertNotNullOrBlank()
            .assertPatternMatch( NAME_XML_PATTERN );

        builder.setProperty( "aliases", target.getAliases() ).setFindingType( FindingType.ERROR ).assertNotNull()
            .assertContainsNoNullElements();

        builder.setProperty( "idFacet", target.getIdFacet() ).setFindingType( FindingType.ERROR ).assertNotNull();

        builder.setProperty( "equivalents", target.getEquivalents() ).setFindingType( FindingType.ERROR )
            .assertNotNull().assertContainsNoNullElements();

        checkSchemaNamingConflicts( target, builder );
        validateVersioningRules( target, builder );

        findings = builder.getFindings();
        findings.addAll( validateContextualFacetLibraryOwnership( target.getCustomFacets() ) );
        findings.addAll( validateContextualFacetLibraryOwnership( target.getQueryFacets() ) );
        findings.addAll( validateContextualFacetLibraryOwnership( target.getUpdateFacets() ) );

        return findings;
    }

}
