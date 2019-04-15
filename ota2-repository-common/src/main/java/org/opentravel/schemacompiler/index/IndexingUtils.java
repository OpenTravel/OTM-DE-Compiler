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

package org.opentravel.schemacompiler.index;

import org.opentravel.ns.ota2.release_v01_00.ReleaseMemberType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionEventType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
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
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.ServiceAssemblyMember;
import org.opentravel.schemacompiler.util.ClassSpecificFunction;
import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationFinding;

import javax.xml.namespace.QName;

/**
 * Static utility methods used during the repository indexing and search result processing.
 */
public class IndexingUtils {

    private static final String META_DATA_SUFFIX = ":meta-data";

    /**
     * Private constructor to prevent instantiation.
     */
    private IndexingUtils() {}

    /**
     * Returns the qualified identity key for the given OTM model entity.
     * 
     * @param entity the named entity for which to return an identity key
     * @return String
     */
    public static String getIdentityKey(NamedEntity entity) {
        return getIdentityKey( entity, true );
    }

    /**
     * Returns the qualified identity key for the given OTM model entity. If the 'isSearchable' flag is true, the
     * entity's index document will be included in the searchable index records.
     * 
     * @param entity the named entity for which to return an identity key
     * @param isSearchable flag indicating whether the resulting index document should be searchable
     * @return String
     */
    public static String getIdentityKey(NamedEntity entity, boolean isSearchable) {
        return getIdentityKey( entity.getNamespace(), entity.getLocalName(), isSearchable );
    }

    /**
     * Returns the qualified identity key for the OTM model entity with the given namespace and local name.
     * 
     * @param entityNS the namespace of the OTM entity
     * @param entityLocalName the local name of the OTM entity
     * @param isSearchable flag indicating whether the resulting index document should be searchable
     * @return String
     */
    public static String getIdentityKey(String entityNS, String entityLocalName, boolean isSearchable) {
        StringBuilder identityKey = new StringBuilder();

        identityKey.append( entityNS ).append( ":" );
        identityKey.append( entityLocalName );
        if (!isSearchable) {
            identityKey.append( META_DATA_SUFFIX );
        }
        return identityKey.toString();
    }

    /**
     * Returns the qualified identity key for the search index term.
     * 
     * @param item the repository item for which to return the indexing key
     * @return String
     */
    public static String getIdentityKey(RepositoryItem item) {
        RepositoryItemType itemType = RepositoryItemType.fromFilename( item.getFilename() );
        StringBuilder identityKey = new StringBuilder();

        switch (itemType) {
            case ASSEMBLY:
                identityKey.append( "OSM:" );
                break;
            case RELEASE:
                identityKey.append( "REL:" );
                break;
            case LIBRARY:
            default:
                identityKey.append( "LIB:" );
                break;
        }
        identityKey.append( item.getNamespace() ).append( ":" );
        identityKey.append( item.getLibraryName() );
        return identityKey.toString();
    }

    /**
     * Returns the qualified identity key of the library associated with the given release member.
     * 
     * @param member the release member for which to return the search index key
     * @return String
     */
    public static String getIdentityKey(ReleaseMemberType member) {
        StringBuilder identityKey = new StringBuilder();

        identityKey.append( "LIB:" );
        identityKey.append( member.getNamespace() ).append( ":" );
        identityKey.append( member.getLibraryName() );
        return identityKey.toString();
    }

    /**
     * Returns the qualified identity key of the release associated with the given service assembly member.
     * 
     * @param member the assembly member for which to return the search index key
     * @return String
     */
    public static String getIdentityKey(ServiceAssemblyMember member) {
        StringBuilder identityKey = new StringBuilder();

        identityKey.append( "REL:" );
        identityKey.append( member.getReleaseItem().getNamespace() ).append( ":" );
        identityKey.append( member.getReleaseItem().getLibraryName() );
        return identityKey.toString();
    }

    /**
     * Returns the qualified identity key for the given library.
     * 
     * @param library the library for which to return the search index key
     * @return String
     */
    public static String getIdentityKey(TLLibrary library) {
        StringBuilder identityKey = new StringBuilder();

        identityKey.append( "LIB:" );
        identityKey.append( library.getNamespace() ).append( ":" );
        identityKey.append( library.getName() );
        return identityKey.toString();
    }

    /**
     * Returns the qualified identity key for the given validation finding.
     * 
     * @param finding the validation finding for which to return the search index key
     * @return String
     */
    public static String getIdentityKey(ValidationFinding finding) {
        QName sourceQName = getQualifiedName( finding.getSource() );
        StringBuilder identityKey = new StringBuilder();

        identityKey.append( "F:" );

        if (sourceQName != null) {
            identityKey.append( sourceQName.getNamespaceURI() ).append( "|" );
            identityKey.append( sourceQName.getLocalPart() ).append( "|" );
        }
        identityKey.append( finding.getSource().getValidationIdentity() ).append( "|" );
        identityKey.append( finding.getMessageKey() );
        return identityKey.toString();
    }

    /**
     * Returns the qualified identity key for the subscription target.
     * 
     * @param subscriptionTarget the subscription target for which to return an identity key
     * @param eventType the type of the subscription event to which the identity key will apply
     * @return String
     */
    public static String getIdentityKey(SubscriptionTarget subscriptionTarget, SubscriptionEventType eventType) {
        StringBuilder identityKey = new StringBuilder();

        identityKey.append( "S:" );
        identityKey.append( subscriptionTarget.getBaseNamespace() );

        if (subscriptionTarget.getLibraryName() != null) {
            identityKey.append( ":" ).append( subscriptionTarget.getLibraryName() );
        }
        if (subscriptionTarget.getVersion() != null) {
            identityKey.append( ":" ).append( subscriptionTarget.getVersion() );
        }
        identityKey.append( ":" ).append( eventType.toString() );
        return identityKey.toString();
    }

    /**
     * Returns the searchable variant of the given identity key. If the key is already searchable, the original string
     * is returned.
     * 
     * @param identityKey the identity key to process
     * @return String
     */
    public static String getSearchableIdentityKey(String identityKey) {
        String key = identityKey;

        if (key.endsWith( META_DATA_SUFFIX )) {
            key = key.substring( 0, key.length() - 10 );
        }
        return key;
    }

    /**
     * Returns the non-searchable variant of the given identity key. If the key is already non-searchable, the original
     * string is returned.
     * 
     * @param identityKey the identity key to process
     * @return String
     */
    public static String getNonSearchableIdentityKey(String identityKey) {
        String key = identityKey;

        if (!key.endsWith( META_DATA_SUFFIX )) {
            key += META_DATA_SUFFIX;
        }
        return key;
    }

    /**
     * Returns the qualified name of the library or named entity that is the target of a validation finding.
     * 
     * @param validatable the source object for which to return a qualified name
     * @return QName
     */
    public static QName getQualifiedName(Validatable validatable) {
        TLModelElement targetObject = getTargetEntity( validatable );
        QName objName = null;

        if (targetObject instanceof TLLibrary) {
            TLLibrary library = (TLLibrary) targetObject;
            objName = new QName( library.getNamespace(), library.getName() );

        } else if (targetObject instanceof NamedEntity) {
            NamedEntity entity = (NamedEntity) targetObject;
            objName = new QName( entity.getNamespace(), entity.getLocalName() );
        }
        return objName;
    }

    private static ClassSpecificFunction<TLModelElement> targetEntityFunction =
        new ClassSpecificFunction<TLModelElement>().addFunction( TLLibrary.class, v -> v )
            .addFunction( TLContext.class, v -> v.getOwningLibrary() )
            .addFunction( TLInclude.class, v -> v.getOwningLibrary() )
            .addFunction( TLNamespaceImport.class, v -> v.getOwningLibrary() )
            .addFunction( TLService.class, v -> v.getOwningLibrary() ).addFunction( TLSimple.class, v -> v )
            .addFunction( TLValueWithAttributes.class, v -> v ).addFunction( TLClosedEnumeration.class, v -> v )
            .addFunction( TLOpenEnumeration.class, v -> v ).addFunction( TLChoiceObject.class, v -> v )
            .addFunction( TLCoreObject.class, v -> v ).addFunction( TLBusinessObject.class, v -> v )
            .addFunction( TLResource.class, v -> v ).addFunction( TLOperation.class, v -> v )
            .addFunction( TLExtensionPointFacet.class, v -> v )
            .addFunction( TLFacet.class, v -> getTargetEntity( v.getOwningEntity() ) )
            .addFunction( TLActionFacet.class, v -> getTargetEntity( v.getOwningResource() ) )
            .addFunction( TLSimpleFacet.class, v -> getTargetEntity( v.getOwningEntity() ) )
            .addFunction( TLListFacet.class, v -> getTargetEntity( v.getOwningEntity() ) )
            .addFunction( TLAlias.class, v -> getTargetEntity( v.getOwningEntity() ) )
            .addFunction( TLParamGroup.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLParameter.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLResourceParentRef.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLAction.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLActionRequest.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLActionResponse.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLExtension.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLDocumentation.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLEquivalent.class, v -> getTargetEntity( v.getOwningEntity() ) )
            .addFunction( TLExample.class, v -> getTargetEntity( v.getOwningEntity() ) )
            .addFunction( TLAttribute.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLProperty.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLIndicator.class, v -> getTargetEntity( v.getOwner() ) )
            .addFunction( TLEnumValue.class, v -> getTargetEntity( v.getOwningEnum() ) )
            .addFunction( TLRoleEnumeration.class, v -> getTargetEntity( v.getOwningEntity() ) )
            .addFunction( TLRole.class, v -> getTargetEntity( v.getRoleEnumeration() ) );

    /**
     * Returns the entity or library that will be the indexing target for the source object of a validation finding. For
     * EXAMPLE, the owning entity of an attribute finding would be the core object that declared the attribute.
     * 
     * @param validatable the object that is the source of a validation finding
     * @return TLModelElement
     */
    public static TLModelElement getTargetEntity(Validatable validatable) {
        TLModelElement targetEntity = null;

        if (targetEntityFunction.canApply( validatable )) {
            targetEntity = targetEntityFunction.apply( validatable );
        }
        return targetEntity;
    }

    /**
     * Returns the qualified name of the given facets owning entity.
     * 
     * @param facet the contextual facet for which to return the owner's qualified name
     * @return QName
     */
    public static QName getContextualFacetOwnerQName(TLContextualFacet facet) {
        String ownerIndexId = facet.getOwningEntityName();
        int delimIdx = ownerIndexId.lastIndexOf( ':' );
        QName ownerName = null;

        if (delimIdx >= 0) {
            ownerName = new QName( ownerIndexId.substring( 0, delimIdx ), ownerIndexId.substring( delimIdx + 1 ) );
        }
        return ownerName;
    }

}
