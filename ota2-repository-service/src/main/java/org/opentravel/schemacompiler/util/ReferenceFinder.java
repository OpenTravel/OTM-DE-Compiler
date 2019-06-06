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

package org.opentravel.schemacompiler.util;

import org.opentravel.schemacompiler.index.EntitySearchResult;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.RepositoryException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

/**
 * Utility class that scans a <code>NamedEntity</code> to locate all of the entity reference strings.
 * 
 * @author S. Livezey
 */
public class ReferenceFinder {

    private Map<String,String> referencesToIndexIds = new HashMap<>();
    private EntityNameResolver nameResolver;

    /**
     * Constructor that specifies the named entity to scan for references.
     * 
     * @param entity the entity to scan for references
     * @param indexLibrary the source library from which all references are relative
     */
    public ReferenceFinder(EntitySearchResult entity, LibrarySearchResult indexLibrary) {
        this.nameResolver = new EntityNameResolver( indexLibrary );
        scanEntity( entity.getItemContent() );

        // In addition to model identity names, also add all of the known search index ID's for the entity
        entity.getReferenceIdentityIds().forEach( id -> referencesToIndexIds.put( id, id ) );
    }

    /**
     * Constructs a map of entity name references to the associated <code>EntitySearchResult</code> from the free-text
     * search index.
     * 
     * @param searchService the free-text search service to use for reference resolution
     * @return Map&lt;String,EntitySearchResult&gt;
     * @throws RepositoryException thrown if an error occurs while performing the search
     */
    public Map<String,EntitySearchResult> buildEntityReferenceMap(FreeTextSearchService searchService)
        throws RepositoryException {
        Collection<String> referenceIds = referencesToIndexIds.values();
        List<EntitySearchResult> entityList = searchService.getEntitiesByReferenceIdentity( false,
            referenceIds.toArray( new String[referenceIds.size()] ) );
        Map<String,EntitySearchResult> entitiesByReferenceId = new HashMap<>();
        Map<String,EntitySearchResult> entitiesByReference = new HashMap<>();

        // Start by building a map for each reference identity to the entity itself
        for (EntitySearchResult indexEntity : entityList) {
            for (String referenceId : indexEntity.getReferenceIdentityIds()) {
                entitiesByReferenceId.put( referenceId, indexEntity );
            }
        }

        // Build the map of entities mapped to the original type reference names
        for (Entry<String,String> entry : referencesToIndexIds.entrySet()) {
            String entityRef = entry.getKey();
            String referenceIndexId = entry.getValue();
            EntitySearchResult indexEntity = entitiesByReferenceId.get( referenceIndexId );

            if (indexEntity != null) {
                entitiesByReference.put( entityRef, indexEntity );
            }
        }

        // Add
        return entitiesByReference;
    }

    /**
     * Scans the given <code>NamedEntity</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(NamedEntity entity) {
        if (entity instanceof TLBusinessObject) {
            scanEntity( (TLBusinessObject) entity );

        } else if (entity instanceof TLChoiceObject) {
            scanEntity( (TLChoiceObject) entity );

        } else if (entity instanceof TLCoreObject) {
            scanEntity( (TLCoreObject) entity );

        } else if (entity instanceof TLContextualFacet) {
            scanEntity( (TLContextualFacet) entity );

        } else if (entity instanceof TLValueWithAttributes) {
            scanEntity( (TLValueWithAttributes) entity );

        } else if (entity instanceof TLAbstractEnumeration) {
            scanEntity( (TLAbstractEnumeration) entity );

        } else if (entity instanceof TLSimple) {
            scanEntity( (TLSimple) entity );

        } else if (entity instanceof TLOperation) {
            scanEntity( (TLOperation) entity );

        } else if (entity instanceof TLResource) {
            scanEntity( (TLResource) entity );
        }
    }

    /**
     * Scans the given <code>TLBusinessObject</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(TLBusinessObject entity) {
        scanExtension( entity.getExtension() );
        scanFacet( entity.getIdFacet() );
        scanFacet( entity.getSummaryFacet() );
        scanFacet( entity.getDetailFacet() );
        scanContextualFacets( entity.getCustomFacets() );
        scanContextualFacets( entity.getQueryFacets() );
        scanContextualFacets( entity.getUpdateFacets() );
    }

    /**
     * Scans the given <code>TLChoiceObject</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(TLChoiceObject entity) {
        scanExtension( entity.getExtension() );
        scanFacet( entity.getSharedFacet() );
        scanContextualFacets( entity.getChoiceFacets() );
    }

    /**
     * Scans the given <code>TLCoreObject</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(TLCoreObject entity) {
        TLSimpleFacet simpleFacet = entity.getSimpleFacet();

        if (simpleFacet != null) {
            addQualifiedName( simpleFacet.getSimpleTypeName() );
        }
        scanExtension( entity.getExtension() );
        scanFacet( entity.getSummaryFacet() );
        scanFacet( entity.getDetailFacet() );
    }

    /**
     * Scans the given <code>TLContextualFacet</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(TLContextualFacet entity) {
        scanContextualFacets( Arrays.asList( (TLContextualFacet) entity ) );
    }

    /**
     * Scans the given <code>TLValueWithAttributes</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(TLValueWithAttributes entity) {
        addQualifiedName( entity.getParentTypeName() );

        for (TLAttribute attribute : entity.getAttributes()) {
            scanAttribute( attribute );
        }
    }

    /**
     * Scans the given <code>TLAbstractEnumeration</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(TLAbstractEnumeration entity) {
        scanExtension( entity.getExtension() );
    }

    /**
     * Scans the given <code>TLSimple</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(TLSimple entity) {
        addQualifiedName( entity.getParentTypeName() );
    }

    /**
     * Scans the given <code>TLOperation</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(TLOperation entity) {
        scanExtension( entity.getExtension() );
        scanFacet( entity.getRequest() );
        scanFacet( entity.getResponse() );
        scanFacet( entity.getNotification() );
    }

    /**
     * Scans the given <code>TLResource</code> for type references.
     * 
     * @param entity the entity to scan for references
     */
    private void scanEntity(TLResource entity) {
        scanExtension( entity.getExtension() );
        addQualifiedName( entity.getBusinessObjectRefName() );
        entity.getActionFacets().forEach( f -> addQualifiedName( f.getBasePayloadName() ) );
    }

    /**
     * Scans the given <code>TLExtension</code> for type references.
     * 
     * @param extension the extension to scan for references
     */
    private void scanExtension(TLExtension extension) {
        if (extension != null) {
            addQualifiedName( extension.getExtendsEntityName() );
        }
    }

    /**
     * Scans the given <code>TLFacet</code> for type references.
     * 
     * @param facet the facet to scan for references
     */
    private void scanFacet(TLFacet facet) {
        if (facet != null) {
            for (TLAttribute attribute : facet.getAttributes()) {
                scanAttribute( attribute );
            }
            for (TLProperty element : facet.getElements()) {
                scanElement( element );
            }
        }
    }

    /**
     * Scans the given list of <code>TLContextualFacet</code>s for type references.
     * 
     * @param facetList the list of facets to scan for references
     */
    private void scanContextualFacets(List<TLContextualFacet> facetList) {
        if (facetList != null) {
            for (TLContextualFacet facet : facetList) {
                addQualifiedName( facet.getOwningEntityName() );
                scanFacet( facet );
                scanContextualFacets( facet.getChildFacets() );
            }
        }
    }

    /**
     * Scans the given <code>TLAttribute</code> for type references.
     * 
     * @param attribute the attribute to scan for references
     */
    private void scanAttribute(TLAttribute attribute) {
        if (attribute != null) {
            addQualifiedName( attribute.getTypeName() );
        }
    }

    /**
     * Scans the given <code>TLProperty</code> for type references.
     * 
     * @param element the element to scan for references
     */
    private void scanElement(TLProperty element) {
        if (element != null) {
            addQualifiedName( element.getTypeName() );
        }
    }

    /**
     * Resolves the qualified name for the given reference and adds it to the collection for this finder.
     * 
     * @param entityRef the entity name reference to process
     */
    private void addQualifiedName(String entityRef) {
        if ((entityRef != null) && !referencesToIndexIds.containsKey( entityRef )) {
            QName ref = nameResolver.getQualifiedName( entityRef );

            if (ref != null) {
                referencesToIndexIds.put( entityRef,
                    IndexingUtils.getIdentityKey( ref.getNamespaceURI(), ref.getLocalPart(), true ) );
            }
        }
    }

}
