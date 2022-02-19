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

package org.opentravel.repocommon.index.builder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.opentravel.repocommon.index.IndexingTerms;
import org.opentravel.repocommon.index.IndexingUtils;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Index builder that constructs search index documents for OTM named entities.
 *
 * @param <T> the type of the OTM named entity for which to construct a search index document
 */
public class EntityIndexBuilder<T extends NamedEntity> extends IndexBuilder<T> {

    private static Logger log = LogManager.getLogger( EntityIndexBuilder.class );

    private boolean latestVersion;
    private boolean latestVersionAtUnderReview;
    private boolean latestVersionAtFinal;
    private boolean latestVersionAtObsolete;
    private String lockedByUser;
    private Map<String,String> prefixMappings = new HashMap<>();
    private Set<String> referenceIdentityKeys = new HashSet<>();
    private Set<String> referencedEntityKeys = new HashSet<>();
    private String extendsEntityKey;

    /**
     * @see org.opentravel.repocommon.index.builder.IndexBuilder#createIndex()
     */
    @Override
    public void createIndex() {
        NamedEntity sourceObject = getSourceObject();
        TLLibrary owningLibrary = (TLLibrary) sourceObject.getOwningLibrary();

        try {
            log.debug( "Indexing Model Entity: " + sourceObject.getLocalName() );

            // Recursively gather keywords and references
            ModelNavigator.navigate( sourceObject, new KeywordAndReferenceVisitor() );

            // Build and store the index document for the entity
            Boolean searchIndexInd = isSearchIndexEntity( sourceObject );
            String identityKey = IndexingUtils.getIdentityKey( sourceObject, searchIndexInd );
            String owningLibraryIdentity = IndexingUtils.getIdentityKey( owningLibrary );
            String entityDescription = getEntityDescription( sourceObject );
            String entityContent = IndexContentHelper.marshallEntity( sourceObject );
            Field.Store nonStoreField = searchIndexInd ? Field.Store.NO : Field.Store.YES;
            Document indexDoc = new Document();

            indexDoc.add( new StringField( IndexingTerms.IDENTITY_FIELD, identityKey, Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.SEARCH_INDEX_FIELD, searchIndexInd + "", Field.Store.NO ) );
            indexDoc
                .add( new StringField( IndexingTerms.OWNING_LIBRARY_FIELD, owningLibraryIdentity, Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.ENTITY_TYPE_FIELD, sourceObject.getClass().getName(),
                Field.Store.YES ) );
            indexDoc.add(
                new StringField( IndexingTerms.ENTITY_NAME_FIELD, getEntityName( sourceObject ), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.ENTITY_LOCAL_NAME_FIELD, sourceObject.getLocalName(),
                Field.Store.YES ) );
            indexDoc.add(
                new StringField( IndexingTerms.ENTITY_NAMESPACE_FIELD, sourceObject.getNamespace(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.VERSION_FIELD, owningLibrary.getVersion(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_FIELD, latestVersion + "", nonStoreField ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_UNDER_REVIEW_FIELD,
                latestVersionAtUnderReview + "", nonStoreField ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_FINAL_FIELD, latestVersionAtFinal + "",
                nonStoreField ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_OBSOLETE_FIELD, latestVersionAtObsolete + "",
                nonStoreField ) );
            indexDoc.add( new TextField( IndexingTerms.KEYWORDS_FIELD, getFreeTextSearchContent(), nonStoreField ) );

            if (entityDescription != null) {
                indexDoc.add(
                    new StringField( IndexingTerms.ENTITY_DESCRIPTION_FIELD, entityDescription, Field.Store.YES ) );
            }
            if (owningLibrary.getStatus() != null) {
                indexDoc.add( new StringField( IndexingTerms.STATUS_FIELD, owningLibrary.getStatus().toString(),
                    Field.Store.YES ) );
            }
            if (lockedByUser != null) {
                indexDoc.add( new StringField( IndexingTerms.LOCKED_BY_USER_FIELD, lockedByUser, Field.Store.YES ) );
            }
            if (extendsEntityKey != null) {
                indexDoc
                    .add( new StringField( IndexingTerms.EXTENDS_ENTITY_FIELD, extendsEntityKey, Field.Store.YES ) );
            }
            for (String facetOwnerKey : getFacetOwnerKeys( sourceObject )) {
                indexDoc.add( new StringField( IndexingTerms.FACET_OWNER_FIELD, facetOwnerKey, Field.Store.YES ) );
            }
            if (entityContent != null) {
                indexDoc.add( new StoredField( IndexingTerms.CONTENT_DATA_FIELD, new BytesRef( entityContent ) ) );
            }
            for (String key : referenceIdentityKeys) {
                indexDoc.add( new StringField( IndexingTerms.REFERENCE_IDENTITY_FIELD, key, Field.Store.YES ) );
            }
            for (String key : referencedEntityKeys) {
                indexDoc.add( new StringField( IndexingTerms.REFERENCED_ENTITY_FIELD, key, nonStoreField ) );
            }
            getIndexWriter().updateDocument( new Term( IndexingTerms.IDENTITY_FIELD, identityKey ), indexDoc );

            // If the entity is a facet owner, add it and all of its parent facet owners (if any) to the list of facet
            // owners to be post-processed for their contextual facet content
            NamedEntity entity = sourceObject;

            while (entity instanceof TLContextualFacet) {
                TLContextualFacet ctxFacet = (TLContextualFacet) entity;
                String facetIdentity = IndexingUtils.getIdentityKey( ctxFacet, false );

                getFactory().getFacetService().addFacetOwnerID( facetIdentity );
                entity = ctxFacet.getOwningEntity();
            }

            if ((entity instanceof TLBusinessObject) || (entity instanceof TLChoiceObject)) {
                getFactory().getFacetService().addFacetOwnerID( IndexingUtils.getIdentityKey( entity ) );
            }

        } catch (IOException | RepositoryException e) {
            log.warn( "Error indexing model entity: " + sourceObject.getLocalName(), e );
        }
    }

    @Override
    public void deleteIndex() {
        NamedEntity sourceObject = getSourceObject();
        try {
            IndexWriter indexWriter = getIndexWriter();

            indexWriter.deleteDocuments(
                new Term( IndexingTerms.IDENTITY_FIELD, IndexingUtils.getIdentityKey( sourceObject ) ) );

            if (!isSearchIndexEntity( sourceObject )) {
                indexWriter.deleteDocuments(
                    new Term( IndexingTerms.IDENTITY_FIELD, IndexingUtils.getIdentityKey( sourceObject, false ) ) );
            }

        } catch (IOException e) {
            log.warn( "Error indexing model entity: " + sourceObject.getLocalName(), e );
        }
    }

    /**
     * @see org.opentravel.repocommon.index.builder.IndexBuilder#setSourceObject(java.lang.Object)
     */
    @Override
    public void setSourceObject(T sourceObject) {
        super.setSourceObject( sourceObject );
        prefixMappings.clear();

        if (sourceObject.getOwningLibrary() instanceof TLLibrary) {
            TLLibrary library = (TLLibrary) sourceObject.getOwningLibrary();

            for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
                if ((nsImport.getNamespace() != null) && (nsImport.getPrefix() != null)) {
                    prefixMappings.put( nsImport.getPrefix(), nsImport.getNamespace() );
                }
            }
        }
    }

    /**
     * Returns the local name of the given entity as it is to be saved in the search index.
     * 
     * @param entity the source object for which to return the local name
     * @return String
     */
    private String getEntityName(NamedEntity entity) {
        String entityName;

        if (entity instanceof TLOperation) {
            entityName = ((TLOperation) entity).getName();

        } else if (entity instanceof TLContextualFacet) {
            TLContextualFacet facet = (TLContextualFacet) entity;
            TLFacetType facetType = facet.getFacetType();

            entityName = (facetType == null) ? facet.getName() : facetType.getIdentityName( facet.getName() );

            if (entityName == null) {
                entityName = "UNKNOWN";
            }

        } else {
            entityName = entity.getLocalName();
        }
        return entityName;
    }

    /**
     * Returns the free-text description of the entity or null if a description has not been provided.
     * 
     * @param entity the entity for which to return the description
     * @return String
     */
    private String getEntityDescription(NamedEntity entity) {
        String description = null;

        if (entity instanceof TLDocumentationOwner) {
            TLDocumentation doc = DocumentationFinder.getDocumentation( (TLDocumentationOwner) entity );

            if (doc != null) {
                description = doc.getDescription();
            }
        }
        return description;
    }

    /**
     * Returns a boolean value indicating whether the entity's document should be saved directly to the main search
     * index or to the contextual facet meta-data index.
     * 
     * @param entity the entity for which to return the searchIndexInd flag
     * @return Boolean
     */
    private Boolean isSearchIndexEntity(NamedEntity entity) {
        return !((entity instanceof TLContextualFacet) || (entity instanceof TLBusinessObject)
            || (entity instanceof TLChoiceObject));
    }

    /**
     * If the given entity is a contextual facet, all of its parent owning facets will be returned in the resulting
     * list.
     * 
     * @param entity the entity for which to capture facet owner keys
     * @return List&lt;String&gt;
     */
    protected List<String> getFacetOwnerKeys(NamedEntity entity) {
        List<String> ownerKeys = new ArrayList<>();

        while (entity instanceof TLContextualFacet) {
            TLContextualFacet facet = (TLContextualFacet) entity;
            NamedEntity facetOwner = facet.getOwningEntity();

            if (facetOwner != null) {
                ownerKeys.add( IndexingUtils.getIdentityKey( facetOwner ) );
            }
            entity = facetOwner;
        }
        return ownerKeys;
    }

    /**
     * Adds a reference identity that may be either the source object itself or one of its <code>NamedEntity</code>
     * child objects such as a facet or alias.
     * 
     * @param referencableEntity the referencable entity
     */
    protected void addReferenceIdentity(NamedEntity referencableEntity) {
        String identityKey = null;

        // Handle special case for facet aliases
        if (referencableEntity instanceof TLAlias) {
            TLAlias alias = (TLAlias) referencableEntity;

            if (alias.getOwningEntity() instanceof TLFacet) {
                identityKey =
                    alias.getNamespace() + ":" + alias.getLocalName() + "_" + alias.getOwningEntity().getLocalName();
            }
        }

        // Normal case...
        if (identityKey == null) {
            identityKey = IndexingUtils.getIdentityKey( referencableEntity );
        }
        referenceIdentityKeys.add( identityKey );
    }

    /**
     * Resolves the given type reference string and returns the identity key.
     * 
     * @param typeReference the type reference string to resolve
     * @return String
     */
    protected String resolveTypeReference(String typeReference) {
        String identityKey = null;

        if ((typeReference != null) && (typeReference.length() > 0)) {
            String[] refParts = typeReference.split( "\\:" );
            String prefix = null;
            String namespace = null;
            String localName = null;

            if (refParts.length == 1) {
                localName = refParts[0];

            } else if (refParts.length == 2) {
                prefix = refParts[0];
                localName = refParts[1];
            }
            namespace = (prefix == null) ? getSourceObject().getNamespace() : prefixMappings.get( prefix );

            if ((namespace != null) && (localName != null) && (localName.length() > 0)) {
                identityKey = namespace + ":" + localName;
                addFreeTextKeywords( localName );
            }
        }
        return identityKey;
    }

    /**
     * Adds the identity key for the given type reference to the list of referenced entities.
     * 
     * @param typeReference the type reference string to resolve
     */
    protected void addEntityReference(NamedEntity entityRef) {
        String identityKey = IndexingUtils.getIdentityKey( entityRef );

        if (identityKey != null) {
            referencedEntityKeys.add( identityKey );
        }
    }

    /**
     * Adds the identity key for the given type reference to the list of referenced entities.
     * 
     * @param typeReference the type reference string to resolve
     */
    protected void addEntityReference(String typeReference) {
        String identityKey = resolveTypeReference( typeReference );

        if (identityKey != null) {
            referencedEntityKeys.add( identityKey );
        }
    }

    /**
     * Returns the flag value indicating whether the owning library is the latest in its version chain.
     *
     * @return boolean
     */
    public boolean isLatestVersion() {
        return latestVersion;
    }

    /**
     * Assigns the flag value indicating whether the owning library is the latest in its version chain.
     *
     * @param latestVersion the flag value to assign
     */
    public void setLatestVersion(boolean latestVersion) {
        this.latestVersion = latestVersion;
    }

    /**
     * Returns the flag value indicating whether the owning library is the latest in its version chain at the
     * <code>UNDER_REVIEW</code> state.
     *
     * @return boolean
     */
    public boolean isLatestVersionAtUnderReview() {
        return latestVersionAtUnderReview;
    }

    /**
     * Assigns the flag value indicating whether the owning library is the latest in its version chain at the
     * <code>UNDER_REVIEW</code> state.
     *
     * @param latestVersionAtUnderReview the flag value to assign
     */
    public void setLatestVersionAtUnderReview(boolean latestVersionAtUnderReview) {
        this.latestVersionAtUnderReview = latestVersionAtUnderReview;
    }

    /**
     * Returns the flag value indicating whether the owning library is the latest in its version chain at the
     * <code>FINAL</code> state.
     *
     * @return boolean
     */
    public boolean isLatestVersionAtFinal() {
        return latestVersionAtFinal;
    }

    /**
     * Assigns the flag value indicating whether the owning library is the latest in its version chain at the
     * <code>FINAL</code> state.
     *
     * @param latestVersionAtFinal the flag value to assign
     */
    public void setLatestVersionAtFinal(boolean latestVersionAtFinal) {
        this.latestVersionAtFinal = latestVersionAtFinal;
    }

    /**
     * Returns the flag value indicating whether the owning library is the latest in its version chain at the
     * <code>OBSOLETE</code> state.
     *
     * @return boolean
     */
    public boolean isLatestVersionAtObsolete() {
        return latestVersionAtObsolete;
    }

    /**
     * Assigns the flag value indicating whether the owning library is the latest in its version chain at the
     * <code>OBSOLETE</code> state.
     *
     * @param latestVersionAtObsolete the flag value to assign
     */
    public void setLatestVersionAtObsolete(boolean latestVersionAtObsolete) {
        this.latestVersionAtObsolete = latestVersionAtObsolete;
    }

    /**
     * Returns the ID of the user (if any) that owns the lock on the entity's owning library.
     *
     * @return String
     */
    public String getLockedByUser() {
        return lockedByUser;
    }

    /**
     * Assigns the ID of the user (if any) that owns the lock on the entity's owning library.
     *
     * @param lockedByUser the user ID to assign
     */
    public void setLockedByUser(String lockedByUser) {
        this.lockedByUser = lockedByUser;
    }

    /**
     * Visitor used to collect keywords from the entity that is assigned to this index builder.
     */
    private class KeywordAndReferenceVisitor extends ModelElementVisitorAdapter {

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
            addReferenceIdentity( simple );
            addFreeTextKeywords( simple.getName() );
            addFreeTextKeywords( simple.getPattern() );
            addFreeTextKeywords( simple.getMinLength() + "" );
            addFreeTextKeywords( simple.getMaxLength() + "" );
            addFreeTextKeywords( simple.getFractionDigits() + "" );
            addFreeTextKeywords( simple.getTotalDigits() + "" );
            addFreeTextKeywords( simple.getMinInclusive() );
            addFreeTextKeywords( simple.getMaxInclusive() );
            addFreeTextKeywords( simple.getMinExclusive() );
            addFreeTextKeywords( simple.getMaxExclusive() );
            addEntityReference( simple.getParentTypeName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes vwa) {
            addReferenceIdentity( vwa );
            addFreeTextKeywords( vwa.getName() );
            addEntityReference( vwa.getParentTypeName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(org.opentravel.schemacompiler.model.TLClosedEnumeration)
         */
        @Override
        public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
            addReferenceIdentity( enumeration );
            addFreeTextKeywords( enumeration.getName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
         */
        @Override
        public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
            addReferenceIdentity( enumeration );
            addFreeTextKeywords( enumeration.getName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitEnumValue(org.opentravel.schemacompiler.model.TLEnumValue)
         */
        @Override
        public boolean visitEnumValue(TLEnumValue enumValue) {
            addFreeTextKeywords( enumValue.getLiteral() );
            addFreeTextKeywords( enumValue.getLabel() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitChoiceObject(org.opentravel.schemacompiler.model.TLChoiceObject)
         */
        @Override
        public boolean visitChoiceObject(TLChoiceObject choiceObject) {
            addReferenceIdentity( choiceObject );
            addFreeTextKeywords( choiceObject.getName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(org.opentravel.schemacompiler.model.TLCoreObject)
         */
        @Override
        public boolean visitCoreObject(TLCoreObject coreObject) {
            addReferenceIdentity( coreObject );
            addFreeTextKeywords( coreObject.getName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitRole(org.opentravel.schemacompiler.model.TLRole)
         */
        @Override
        public boolean visitRole(TLRole role) {
            addFreeTextKeywords( role.getName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(org.opentravel.schemacompiler.model.TLBusinessObject)
         */
        @Override
        public boolean visitBusinessObject(TLBusinessObject businessObject) {
            addReferenceIdentity( businessObject );
            addFreeTextKeywords( businessObject.getName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(org.opentravel.schemacompiler.model.TLOperation)
         */
        @Override
        public boolean visitOperation(TLOperation operation) {
            addReferenceIdentity( operation );
            addFreeTextKeywords( operation.getOwningService().getName() );
            addFreeTextKeywords( operation.getName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
         */
        @Override
        public boolean visitResource(TLResource resource) {
            addReferenceIdentity( resource );
            addFreeTextKeywords( resource.getName() );
            addFreeTextKeywords( resource.getBasePath() );
            addEntityReference( resource.getBusinessObjectRefName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
         */
        @Override
        public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
            addFreeTextKeywords( parentRef.getParentParamGroupName() );
            addFreeTextKeywords( parentRef.getPathTemplate() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
         */
        @Override
        public boolean visitParamGroup(TLParamGroup paramGroup) {
            addFreeTextKeywords( paramGroup.getName() );
            addFreeTextKeywords( paramGroup.getFacetRefName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParameter(org.opentravel.schemacompiler.model.TLParameter)
         */
        @Override
        public boolean visitParameter(TLParameter parameter) {
            addFreeTextKeywords( parameter.getFieldRefName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAction(org.opentravel.schemacompiler.model.TLAction)
         */
        @Override
        public boolean visitAction(TLAction action) {
            addFreeTextKeywords( action.getActionId() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionRequest(org.opentravel.schemacompiler.model.TLActionRequest)
         */
        @Override
        public boolean visitActionRequest(TLActionRequest actionRequest) {
            addFreeTextKeywords( actionRequest.getParamGroupName() );
            addFreeTextKeywords( actionRequest.getPathTemplate() );
            addEntityReference( actionRequest.getPayloadTypeName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
         */
        @Override
        public boolean visitActionResponse(TLActionResponse actionResponse) {
            for (Integer statusCode : actionResponse.getStatusCodes()) {
                addFreeTextKeywords( statusCode.toString() );
            }
            addEntityReference( actionResponse.getPayloadTypeName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
         */
        @Override
        public boolean visitExtension(TLExtension extension) {
            addEntityReference( extension.getExtendsEntityName() );
            extendsEntityKey = resolveTypeReference( extension.getExtendsEntityName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
         */
        @Override
        public boolean visitFacet(TLFacet facet) {
            addReferenceIdentity( facet );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitContextualFacet(org.opentravel.schemacompiler.model.TLContextualFacet)
         */
        @Override
        public boolean visitContextualFacet(TLContextualFacet facet) {
            addReferenceIdentity( facet );
            addFreeTextKeywords( facet.getName() );

            // If the source object being indexed is this contextual facet, we need to capture the owning
            // entity's name to include it in the searchable terms of the index document. We will also
            // change the owning entity name to that of the owner's search index ID; this will help us to
            // reassemble the contextual facet structure(s) when the owner's content is retrieved from the
            // search index.
            if (facet == getSourceObject()) {
                TLFacetOwner facetOwner = facet.getOwningEntity();

                facet.setFacetNamespace( facet.getOwningLibrary().getNamespace() );

                if (facetOwner != null) {
                    extendsEntityKey = IndexingUtils.getIdentityKey( facetOwner, true );
                    addEntityReference( facetOwner );
                    addReferenceIdentity( facetOwner );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitListFacet(org.opentravel.schemacompiler.model.TLListFacet)
         */
        @Override
        public boolean visitListFacet(TLListFacet listFacet) {
            addReferenceIdentity( listFacet );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionFacet(org.opentravel.schemacompiler.model.TLActionFacet)
         */
        @Override
        public boolean visitActionFacet(TLActionFacet facet) {
            addReferenceIdentity( facet );
            addFreeTextKeywords( facet.getName() );
            addEntityReference( facet.getBasePayloadName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            addReferenceIdentity( simpleFacet );
            addEntityReference( simpleFacet.getSimpleTypeName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAlias(org.opentravel.schemacompiler.model.TLAlias)
         */
        @Override
        public boolean visitAlias(TLAlias alias) {
            addReferenceIdentity( alias );
            addFreeTextKeywords( alias.getName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
         */
        @Override
        public boolean visitAttribute(TLAttribute attribute) {
            addFreeTextKeywords( attribute.getName() );
            addEntityReference( attribute.getTypeName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            addFreeTextKeywords( element.getName() );
            addEntityReference( element.getTypeName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitIndicator(org.opentravel.schemacompiler.model.TLIndicator)
         */
        @Override
        public boolean visitIndicator(TLIndicator indicator) {
            addFreeTextKeywords( indicator.getName() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
         */
        @Override
        public boolean visitEquivalent(TLEquivalent equivalent) {
            addFreeTextKeywords( equivalent.getDescription() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExample(org.opentravel.schemacompiler.model.TLExample)
         */
        @Override
        public boolean visitExample(TLExample example) {
            addFreeTextKeywords( example.getValue() );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
         */
        @Override
        public boolean visitDocumentation(TLDocumentation documentation) {
            addFreeTextKeywords( documentation.getDescription() );

            for (TLDocumentationItem docItem : documentation.getDeprecations()) {
                addFreeTextKeywords( docItem.getText() );
            }
            for (TLDocumentationItem docItem : documentation.getReferences()) {
                addFreeTextKeywords( docItem.getText() );
            }
            for (TLDocumentationItem docItem : documentation.getImplementers()) {
                addFreeTextKeywords( docItem.getText() );
            }
            for (TLDocumentationItem docItem : documentation.getMoreInfos()) {
                addFreeTextKeywords( docItem.getText() );
            }
            for (TLDocumentationItem docItem : documentation.getOtherDocs()) {
                addFreeTextKeywords( docItem.getText() );
            }
            return true;
        }

    }

}
