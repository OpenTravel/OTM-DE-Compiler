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

package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.util.ClassSpecificFunction;

import java.util.HashSet;
import java.util.Set;

/**
 * Locates an appropriate <code>TLDocumentation</code> instance for a model element. If the entity does not declare its
 * own documentation, a search algorithm will attempt to locate an appropriate alternative documentation item (e.g. from
 * the simple type assigned to an attribute).
 * 
 * @author S. Livezey
 */
public class DocumentationFinder {

    /**
     * Private constructor to prevent instantiation.
     */
    private DocumentationFinder() {}

    /**
     * Returns an appropriate documentation item for the given entity. If no documentation can be located, this method
     * will return null.
     * 
     * @param entity the entity for which to return a documentation item
     * @return TLDocumentation
     */
    public static TLDocumentation getDocumentation(TLDocumentationOwner entity) {
        return getDocumentation( entity, new HashSet<String>() );
    }

    /**
     * Recursive method used to search for documentation. This method includes protection from infinite loops due to
     * circular references.
     * 
     * @param entity the entity for which to return a documentation item
     * @param visitedEntities the list of visited entity names
     * @return TLDocumentation
     */
    private static TLDocumentation getDocumentation(TLDocumentationOwner entity, Set<String> visitedEntities) {
        TLDocumentation doc = entity.getDocumentation();

        // Not found if documentation is null or empty
        if ((doc != null) && doc.isEmpty()) {
            doc = null;
        }

        if (doc == null) {
            TLDocumentationOwner nextEntity = null;

            if (docOwnerFunction.canApply( doc )) {
                nextEntity = docOwnerFunction.apply( doc );
            }

            if (nextEntity != null) {
                String entityId = getEntityId( nextEntity );

                if (!visitedEntities.contains( entityId )) {
                    visitedEntities.add( entityId );
                    doc = getDocumentation( nextEntity, visitedEntities );
                }
            }
        }
        return doc;
    }

    /**
     * Returns true if the entity's documentation includes one or more deprecation notices.
     * 
     * @param entity the entity to check for deprecation
     * @return boolean
     */
    public static boolean isDeprecated(TLDocumentationOwner entity) {
        TLDocumentation doc = entity.getDocumentation();
        boolean deprecated = false;

        if ((doc != null) && (doc.getDeprecations() != null)) {
            for (TLDocumentationItem deprecationItem : doc.getDeprecations()) {
                String deprecationText = (deprecationItem == null) ? null : deprecationItem.getText().trim();

                if ((deprecationText != null) && (deprecationText.length() > 0)) {
                    deprecated = true;
                    break;
                }
            }
        }
        return deprecated;
    }

    private static ClassSpecificFunction<TLDocumentationOwner> docOwnerFunction =
        new ClassSpecificFunction<TLDocumentationOwner>()
            .addFunction( TLSimple.class, e -> nextDocOwner( e.getParentType() ) )
            .addFunction( TLAbstractEnumeration.class, e -> nextDocOwner( e.getExtension() ) )
            .addFunction( TLValueWithAttributes.class, e -> nextDocOwner( e.getParentType() ) )
            .addFunction( TLComplexTypeBase.class, e -> nextDocOwner( e.getExtension() ) )
            .addFunction( TLOperation.class, e -> nextDocOwner( e.getExtension() ) )
            .addFunction( TLResource.class, DocumentationFinder::nextDocOwner )
            .addFunction( TLFacet.class, e -> nextDocOwner( e.getOwningEntity() ) )
            .addFunction( TLListFacet.class, e -> nextDocOwner( e.getItemFacet() ) )
            .addFunction( TLSimpleFacet.class, e -> nextDocOwner( e.getSimpleType() ) )
            .addFunction( TLAttribute.class, e -> nextDocOwner( e.getType() ) )
            .addFunction( TLProperty.class, e -> nextDocOwner( e.getType() ) )
            .addFunction( TLParameter.class, e -> nextDocOwner( e.getFieldRef() ) );

    /**
     * Returns the next documentation owner in the navigation cycle.
     * 
     * @param candidate the next candidate documentation owner to check
     * @return TLDocumentationOwner
     */
    private static TLDocumentationOwner nextDocOwner(ModelElement candidate) {
        TLDocumentationOwner nextOwner = null;

        if (candidate instanceof TLResource) {
            TLResource resource = (TLResource) candidate;

            if (resource.getBusinessObjectRef() != null) {
                nextOwner = resource.getBusinessObjectRef();

            } else {
                nextOwner = nextDocOwner( resource.getExtension() );
            }

        } else if (candidate instanceof TLExtension) {
            TLExtension extension = (TLExtension) candidate;

            if (extension.getExtendsEntity() instanceof TLDocumentationOwner) {
                nextOwner = (TLDocumentationOwner) extension.getExtendsEntity();
            }

        } else if (candidate instanceof TLDocumentationOwner) {
            nextOwner = (TLDocumentationOwner) candidate;
        }
        return nextOwner;
    }

    /**
     * Returns a unique ID for the given model entity.
     * 
     * @param entity the entity for which to return a unique ID
     * @return String
     */
    private static String getEntityId(TLDocumentationOwner entity) {
        StringBuilder entityId = new StringBuilder();

        if (entity instanceof NamedEntity) {
            NamedEntity e = (NamedEntity) entity;
            entityId.append( e.getNamespace() ).append( ":" ).append( e.getLocalName() );

        } else if (entity instanceof TLAttribute) {
            TLAttribute e = (TLAttribute) entity;
            entityId.append( getEntityId( (TLDocumentationOwner) e.getOwner() ) ).append( ":" ).append( e.getName() );

        } else if (entity instanceof TLProperty) {
            TLProperty e = (TLProperty) entity;
            entityId.append( getEntityId( (TLDocumentationOwner) e.getOwner() ) ).append( ":" ).append( e.getName() );

        } else {
            entityId.append( "unknown" );
        }
        return entityId.toString();
    }

}
