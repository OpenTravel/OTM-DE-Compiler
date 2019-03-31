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
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLContextualFacetBaseValidator;
import org.opentravel.schemacompiler.validate.impl.CircularReferenceChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for the <code>TLContextualFacet</code> class.
 *
 * @author S. Livezey
 */
public class TLContextualFacetCompileValidator extends TLContextualFacetBaseValidator {

    private static final String OWNING_ENTITY = "owningEntity";

    public static final String ERROR_INVALID_CIRCULAR_REFERENCE = "INVALID_CIRCULAR_REFERENCE";
    public static final String ERROR_INVALID_FACET_TYPE = "INVALID_FACET_TYPE";
    public static final String ERROR_OTM_16_OWNING_LIBRARY_MISMATCH = "OTM_16_OWNING_LIBRARY_MISMATCH";
    public static final String ERROR_OTM_16_INVALID_OWNER = "OTM_16_INVALID_OWNER";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLContextualFacet target) {
        boolean specialCaseQueryFacet =
            (target.getFacetType() == TLFacetType.QUERY) && !(target.getOwningEntity() instanceof TLContextualFacet);
        TLValidationBuilder builder = newValidationBuilder( target );
        TLFacetOwner owningEntity = target.getOwningEntity();

        builder.setEntityReferenceProperty( OWNING_ENTITY, target.getOwningEntity(), target.getOwningEntityName() )
            .setFindingType( FindingType.ERROR ).assertNotNull()
            .assertValidEntityReference( TLBusinessObject.class, TLChoiceObject.class, TLContextualFacet.class )
            .setFindingType( FindingType.WARNING ).assertNotDeprecated().assertNotObsolete();

        if (CircularReferenceChecker.hasCircularReference( target )) {
            builder.addFinding( FindingType.ERROR, OWNING_ENTITY, ERROR_INVALID_CIRCULAR_REFERENCE );
        }

        if (!specialCaseQueryFacet) {
            builder.setProperty( "facetName", target.getName() ).setFindingType( FindingType.ERROR )
                .assertNotNullOrBlank().assertPatternMatch( NAME_XML_PATTERN );
        }

        validateFacetType( target, owningEntity, builder );

        builder.setProperty( "aliases", target.getAliases() ).setFindingType( FindingType.ERROR ).assertNotNull()
            .assertContainsNoNullElements();

        builder.setProperty( "attributes", target.getAttributes() ).setFindingType( FindingType.ERROR ).assertNotNull()
            .assertContainsNoNullElements();

        builder.setProperty( "elements", target.getElements() ).setFindingType( FindingType.ERROR ).assertNotNull()
            .assertContainsNoNullElements();

        builder.setProperty( "indicators", target.getIndicators() ).setFindingType( FindingType.ERROR ).assertNotNull()
            .assertContainsNoNullElements();

        if (ValidatorUtils.hasMultipleIdMembers( target )) {
            builder.addFinding( FindingType.ERROR, "members", TLFacetCompileValidator.ERROR_MULTIPLE_ID_MEMBERS );
        }

        builder.setProperty( "members", ValidatorUtils.getMembers( target ) ).setFindingType( FindingType.WARNING )
            .assertMinimumSize( 1 );

        checkSchemaNamingConflicts( target, builder );

        if (!OTM16Upgrade.otm16Enabled) {

            if ((owningEntity != null) && (target.getOwningLibrary() != owningEntity.getOwningLibrary())) {
                builder.addFinding( FindingType.ERROR, "owningLibrary", ERROR_OTM_16_OWNING_LIBRARY_MISMATCH );
            }

            if (owningEntity instanceof TLContextualFacet) {
                builder.addFinding( FindingType.ERROR, OWNING_ENTITY, ERROR_OTM_16_INVALID_OWNER );
            }
        }
        return builder.getFindings();
    }

    /**
     * Validates the type of the contextual facet.
     * 
     * @param target the target contextual facet being validated
     * @param owningEntity the entity that owns the contextual facet
     * @param builder the validation builder where errors and warnings will be reported
     */
    private void validateFacetType(TLContextualFacet target, TLFacetOwner owningEntity, TLValidationBuilder builder) {
        TLFacetType impliedType = getImpliedFacetType( target );

        builder.setProperty( "facetType", target.getFacetType() ).setFindingType( FindingType.ERROR ).assertNotNull();

        if ((impliedType != null) && (impliedType != target.getFacetType())) {
            if (owningEntity instanceof TLBusinessObject) {
                builder.addFinding( FindingType.ERROR, "businessObject.findingType", ERROR_INVALID_FACET_TYPE,
                    target.getFacetType().getIdentityName(), impliedType.getIdentityName() );

            } else if (owningEntity instanceof TLChoiceObject) {
                builder.addFinding( FindingType.ERROR, "choiceObject.findingType", ERROR_INVALID_FACET_TYPE,
                    target.getFacetType().getIdentityName() );

            } else if (owningEntity instanceof TLContextualFacet) {
                builder.addFinding( FindingType.ERROR, "contextualFacet.findingType", ERROR_INVALID_FACET_TYPE,
                    target.getFacetType().getIdentityName(), impliedType.getIdentityName() );
            }
        }

        if (impliedType != null) {
            builder.setProperty( "identity", getSiblingFacets( target, impliedType ) )
                .setFindingType( FindingType.ERROR ).assertNoDuplicates(
                    e -> ((TLContextualFacet) e).getFacetType().getIdentityName( ((TLContextualFacet) e).getName() ) );
        }
    }

    /**
     * Returns the implied facet type as denoted by the facet's position within its owning entity. For EXAMPLE, a
     * contextual facet that is located within a business object's list of query facets would return <code>QUERY</code>
     * regardless of the facet's assinged type. If the implied facet type cannot be identified, this method will return
     * null.
     * 
     * @param target the contextual facet being validated
     * @return TLFacetType
     */
    private TLFacetType getImpliedFacetType(TLContextualFacet target) {
        TLFacetOwner owningEntity = target.getOwningEntity();
        TLFacetType impliedType = null;

        if (owningEntity instanceof TLBusinessObject) {
            TLBusinessObject owner = (TLBusinessObject) owningEntity;

            if (owner.getCustomFacets().contains( target )) {
                impliedType = TLFacetType.CUSTOM;

            } else if (owner.getQueryFacets().contains( target )) {
                impliedType = TLFacetType.QUERY;

            } else if (owner.getUpdateFacets().contains( target )) {
                impliedType = TLFacetType.UPDATE;

            } else {
                // Last Resort: Call it a custom facet if not found in any of the collections
                impliedType = TLFacetType.CUSTOM;
            }

        } else if (owningEntity instanceof TLChoiceObject) {
            impliedType = TLFacetType.CHOICE;

        } else if (owningEntity instanceof TLContextualFacet) {
            impliedType = ((TLContextualFacet) owningEntity).getFacetType();
        }
        return impliedType;
    }

    /**
     * Returns the list of all facets of the same type from the owner of the given facet. The list that is returned may
     * contain the target facet that is passed to this method.
     * 
     * @param target the contextual facet being validated
     * @param impliedFacetType the implied type of the contextual facet
     * @return List&lt;TLContextualFacet&gt;
     */
    private List<TLContextualFacet> getSiblingFacets(TLContextualFacet target, TLFacetType impliedFacetType) {
        TLFacetOwner owningEntity = target.getOwningEntity();
        List<TLContextualFacet> siblings = new ArrayList<>();

        for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType( owningEntity, impliedFacetType )) {
            if (facet instanceof TLContextualFacet) {
                siblings.add( (TLContextualFacet) facet );
            }
        }
        return siblings;
    }

}
