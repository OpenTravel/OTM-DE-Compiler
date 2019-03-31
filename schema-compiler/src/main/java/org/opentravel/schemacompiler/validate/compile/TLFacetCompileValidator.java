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
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLFacetBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;

/**
 * Validator for the <code>TLFacet</code> class.
 * 
 * @author S. Livezey
 */
public class TLFacetCompileValidator extends TLFacetBaseValidator {

    public static final String ERROR_EXTENSIBILITY_NOT_ALLOWED = "EXTENSIBILITY_NOT_ALLOWED";
    public static final String ERROR_CONTEXT_OR_LABEL_REQUIRED = "CONTEXT_OR_LABEL_REQUIRED";
    public static final String ERROR_MULTIPLE_ID_MEMBERS = "MULTIPLE_ID_MEMBERS";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLFacet target) {
        TLValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( "facetType", target.getFacetType() ).setFindingType( FindingType.ERROR ).assertNotNull();

        builder.setProperty( "aliases", target.getAliases() ).setFindingType( FindingType.ERROR ).assertNotNull()
            .assertContainsNoNullElements();

        builder.setProperty( "attributes", target.getAttributes() ).setFindingType( FindingType.ERROR ).assertNotNull()
            .assertContainsNoNullElements();

        builder.setProperty( "elements", target.getElements() ).setFindingType( FindingType.ERROR ).assertNotNull()
            .assertContainsNoNullElements();

        builder.setProperty( "indicators", target.getIndicators() ).setFindingType( FindingType.ERROR ).assertNotNull()
            .assertContainsNoNullElements();

        if (ValidatorUtils.hasMultipleIdMembers( target )) {
            builder.addFinding( FindingType.ERROR, "members", ERROR_MULTIPLE_ID_MEMBERS );
        }

        if (target.getOwningEntity() instanceof TLBusinessObject) {
            validateBusinessObjectFacet( target, builder );
        }

        checkSchemaNamingConflicts( target, builder );

        return builder.getFindings();
    }

    /**
     * Performs specialized checks for facets that are owned by business objects.
     * 
     * @param target the target facet to validate
     * @param builder the validation builder used to collect any findings that are discovered
     */
    private void validateBusinessObjectFacet(TLFacet target, TLValidationBuilder builder) {
        TLBusinessObject businessObject = (TLBusinessObject) target.getOwningEntity();
        TLFacetOwner baseEntity = FacetCodegenUtils.getFacetOwnerExtension( businessObject );
        TLFacetType facetType = target.getFacetType();

        // ID facets must define at least one attribute or property (unless the owning business
        // object is an extension of another one).
        if ((facetType == TLFacetType.ID) && (baseEntity == null)) {
            builder.setProperty( "ID.members", ValidatorUtils.getMembers( target, false ) )
                .setFindingType( FindingType.ERROR ).assertMinimumSize( 1 );
        }
    }

}
