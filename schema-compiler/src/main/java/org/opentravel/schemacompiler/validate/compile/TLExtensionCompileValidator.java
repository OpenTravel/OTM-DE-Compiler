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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.CircularReferenceChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLExtension</code> class.
 * 
 * @author S. Livezey
 */
public class TLExtensionCompileValidator extends TLValidatorBase<TLExtension> {

    private static final String EXTENDS_ENTITY = "extendsEntity";

    public static final String ERROR_INVALID_CIRCULAR_EXTENSION = "INVALID_CIRCULAR_EXTENSION";
    public static final String ERROR_INVALID_LOCAL_FACET_EXTENSION = "INVALID_LOCAL_FACET_EXTENSION";
    public static final String ERROR_NESTED_FACET_EXTENSION = "NESTED_FACET_EXTENSION";
    public static final String ERROR_ILLEGAL_EXTENSION = "ILLEGAL_EXTENSION";
    public static final String WARNING_MUST_BE_EXTENSIBLE = "MUST_BE_EXTENSIBLE";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLExtension target) {
        TLValidationBuilder builder = newValidationBuilder( target );
        NamedEntity extendsEntity = target.getExtendsEntity();
        TLExtensionOwner extensionOwner = target.getOwner();

        builder.setEntityReferenceProperty( EXTENDS_ENTITY, target.getExtendsEntity(), target.getExtendsEntityName() )
            .setFindingType( FindingType.ERROR ).assertNotNull().setFindingType( FindingType.WARNING )
            .assertNotDeprecated().assertNotObsolete();

        if (extendsEntity != null) {
            // Assert the correct type of entity reference based on the extension owner's type
            if (extensionOwner instanceof TLBusinessObject) {
                builder.setFindingType( FindingType.ERROR ).assertValidEntityReference( TLBusinessObject.class );

            } else if (extensionOwner instanceof TLCoreObject) {
                builder.setFindingType( FindingType.ERROR ).assertValidEntityReference( TLCoreObject.class );

            } else if (extensionOwner instanceof TLOperation) {
                builder.setFindingType( FindingType.ERROR ).assertValidEntityReference( TLOperation.class );

            } else if (extensionOwner instanceof TLClosedEnumeration) {
                builder.setFindingType( FindingType.ERROR ).assertValidEntityReference( TLClosedEnumeration.class );

            } else if (extensionOwner instanceof TLOpenEnumeration) {
                builder.setFindingType( FindingType.ERROR ).assertValidEntityReference( TLOpenEnumeration.class,
                    TLClosedEnumeration.class );

            } else if (extensionOwner instanceof TLExtensionPointFacet) {
                validateExtensionPointFacetExtension( (TLExtensionPointFacet) extensionOwner, extendsEntity, builder );
            }

            // If the extended entity publishes an extension point, the extension owner must publish
            // one as well.
            if (isExtendableEntity( extendsEntity ) && !isExtendableEntity( (NamedEntity) extensionOwner )) {
                builder.addFinding( FindingType.WARNING, "owner", WARNING_MUST_BE_EXTENSIBLE,
                    extendsEntity.getLocalName() );
            }
        }

        // Assert that the entity being extended is, in fact, marked as being extendable (XP-facets
        // only)
        if ((extensionOwner instanceof TLExtensionPointFacet) && !isExtendableEntity( extendsEntity )) {
            builder.addFinding( FindingType.ERROR, EXTENDS_ENTITY, ERROR_ILLEGAL_EXTENSION,
                target.getExtendsEntityName() );
        }

        if (CircularReferenceChecker.hasCircularExtension( target )) {
            builder.addFinding( FindingType.ERROR, EXTENDS_ENTITY, ERROR_INVALID_CIRCULAR_EXTENSION );
        }

        return builder.getFindings();
    }

    /**
     * Validates an extension that is owned by a <code>TLExtensionPointFacet</code>.
     * 
     * @param epFacet the extension point facet
     * @param extendsEntity the entity that is extended by the facet
     * @param builder validation builder where errors and warnings will be reported
     */
    private void validateExtensionPointFacetExtension(TLExtensionPointFacet epFacet, NamedEntity extendsEntity,
        TLValidationBuilder builder) {
        String extendsEntityNamespace = extendsEntity.getNamespace();
        String localNamespace = epFacet.getNamespace();

        // Extension point facets can only extend facets from another namespace
        builder.assertValidEntityReference( TLFacet.class, TLActionFacet.class );

        if ((extendsEntity instanceof TLFacet) && (localNamespace != null)
            && localNamespace.equals( extendsEntityNamespace )) {
            builder.addFinding( FindingType.ERROR, EXTENDS_ENTITY, ERROR_INVALID_LOCAL_FACET_EXTENSION );
        }

        // Extension point facets cannot extend nested contextual facets
        if (extendsEntity instanceof TLContextualFacet) {
            TLFacetOwner extendsEntityFacetOwner = ((TLContextualFacet) extendsEntity).getOwningEntity();

            if (extendsEntityFacetOwner instanceof TLContextualFacet) {
                builder.addFinding( FindingType.ERROR, EXTENDS_ENTITY, ERROR_NESTED_FACET_EXTENSION );
            }
        }
    }

    /**
     * Returns true if the given entity has been marked as 'extendable' (or marked as not-notExtendable, as the case may
     * be).
     * 
     * @param extendedEntity the named entity to analyze
     * @return boolean
     */
    private boolean isExtendableEntity(NamedEntity extendedEntity) {
        boolean isExtendable = true;

        if (extendedEntity instanceof TLComplexTypeBase) {
            isExtendable = !((TLComplexTypeBase) extendedEntity).isNotExtendable();

        } else if (extendedEntity instanceof TLOperation) {
            isExtendable = !((TLOperation) extendedEntity).isNotExtendable();

        } else if (extendedEntity instanceof TLFacet) {
            TLFacet extendedFacet = (TLFacet) extendedEntity;

            if (extendedFacet.getFacetType().isContextual()) {
                isExtendable = !extendedFacet.isNotExtendable();

            } else {
                TLFacetOwner extendedFacetOwner = extendedFacet.getOwningEntity();

                if (extendedFacetOwner instanceof TLComplexTypeBase) {
                    isExtendable = !((TLComplexTypeBase) extendedFacetOwner).isNotExtendable();
                }
            }
        }
        return isExtendable;
    }

}
