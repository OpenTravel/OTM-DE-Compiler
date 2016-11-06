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

import javax.xml.namespace.QName;

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
import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationFinding;

/**
 * Static utility methods used during the repository indexing and search result processing.
 */
public class IndexingUtils {

	/**
	 * Returns the qualified identity key for the given OTM model entity.
	 * 
	 * @param entity  the named entity for which to return an identity key
	 * @return String
	 */
	public static String getIdentityKey(NamedEntity entity) {
		return getIdentityKey( entity, true );
	}

	/**
	 * Returns the qualified identity key for the given OTM model entity.  If the
	 * 'isSearchable' flag is true, the entity's index document will be included in
	 * the searchable index records.
	 * 
	 * @param entity  the named entity for which to return an identity key
	 * @param isSearchable  flag indicating whether the resulting index document should be searchable
	 * @return String
	 */
	public static String getIdentityKey(NamedEntity entity, boolean isSearchable) {
		return getIdentityKey( entity.getNamespace(), entity.getLocalName(), isSearchable );
	}
	
	/**
	 * Returns the qualified identity key for the OTM model entity with the given namespace
	 * and local name.
	 * 
	 * @param entityNS  the namespace of the OTM entity
	 * @param entityLocalName  the local name of the OTM entity
	 * @param isSearchable  flag indicating whether the resulting index document should be searchable
	 * @return String
	 */
	public static String getIdentityKey(String entityNS, String entityLocalName, boolean isSearchable) {
		StringBuilder identityKey = new StringBuilder();
		
		identityKey.append( entityNS ).append(":");
		identityKey.append( entityLocalName );
		if (!isSearchable) {
			identityKey.append(":meta-data");
		}
		return identityKey.toString();
	}

	/**
	 * Returns the searchable variant of the given identity key.  If the key is already
	 * searchable, the original string is returned.
	 * 
	 * @param identityKey  the identity key to process
	 * @return String
	 */
	public static String getSearchableIdentityKey(String identityKey) {
		String key = identityKey;
		
		if (key.endsWith(":meta-data")) {
			key = key.substring( 0, key.length() - 10 );
		}
		return key;
	}

	/**
	 * Returns the non-searchable variant of the given identity key.  If the key is already
	 * non-searchable, the original string is returned.
	 * 
	 * @param identityKey  the identity key to process
	 * @return String
	 */
	public static String getNonSearchableIdentityKey(String identityKey) {
		String key = identityKey;
		
		if (!key.endsWith(":meta-data")) {
			key += ":meta-data";
		}
		return key;
	}

	/**
	 * Returns the qualified identity key for the search index term.
	 * 
	 * @return String
	 */
	public static String getIdentityKey(RepositoryItem item) {
		StringBuilder identityKey = new StringBuilder();
		
		identityKey.append("LIB:");
		identityKey.append( item.getNamespace() ).append(":");
		identityKey.append( item.getLibraryName() );
		return identityKey.toString();
	}

	/**
	 * Returns the qualified identity key for the given library.
	 * 
	 * @return String
	 */
	public static String getIdentityKey(TLLibrary library) {
		StringBuilder identityKey = new StringBuilder();
		
		identityKey.append("LIB:");
		identityKey.append( library.getNamespace() ).append(":");
		identityKey.append( library.getName() );
		return identityKey.toString();
	}
	
	/**
	 * Returns the qualified identity key for the given validation finding.
	 * 
	 * @return String
	 */
	public static String getIdentityKey(ValidationFinding finding) {
		QName sourceQName = getQualifiedName( finding.getSource() );
		StringBuilder identityKey = new StringBuilder();
		
		identityKey.append("F:");
		identityKey.append( sourceQName.getNamespaceURI() ).append("|");
		identityKey.append( sourceQName.getLocalPart() ).append("|");
		identityKey.append( finding.getSource().getValidationIdentity() ).append("|");
		identityKey.append( finding.getMessageKey() );
		return identityKey.toString();
	}
	
	/**
	 * Returns the qualified name of the library or named entity that is the target of
	 * a validation finding.
	 * 
	 * @param validatable  the source object for which to return a qualified name
	 * @return QName
	 */
	public static QName getQualifiedName(Validatable validatable) {
		TLModelElement targetObject = getTargetEntity( validatable );
		QName objName;
		
		if (targetObject instanceof TLLibrary) {
			TLLibrary library = (TLLibrary) targetObject;
			objName = new QName( library.getNamespace(), library.getName() );
			
		} else {
			NamedEntity entity = (NamedEntity) targetObject;
			objName = new QName( entity.getNamespace(), entity.getLocalName() );
		}
		return objName;
	}
	
	/**
	 * Returns the entity or library that will be the indexing target for the source
	 * object of a validation finding.  For example, the owning entity of an attribute
	 * finding would be the core object that declared the attribute.
	 * 
	 * @param validatable  the object that is the source of a validation finding
	 * @return TLModelElement
	 */
	public static TLModelElement getTargetEntity(Validatable validatable) {
		TLModelElement targetEntity = null;
		
		if (validatable instanceof TLLibrary) {
        	targetEntity = (TLLibrary) validatable;
			
        } else if (validatable instanceof TLContext) {
        	targetEntity = ((TLContext) validatable).getOwningLibrary();

        } else if (validatable instanceof TLInclude) {
        	targetEntity = ((TLInclude) validatable).getOwningLibrary();

        } else if (validatable instanceof TLNamespaceImport) {
        	targetEntity = ((TLNamespaceImport) validatable).getOwningLibrary();

        } else if (validatable instanceof TLService) {
        	targetEntity = ((TLService) validatable).getOwningLibrary();

		} else if (validatable instanceof TLSimple) {
        	targetEntity = (TLSimple) validatable;

        } else if (validatable instanceof TLValueWithAttributes) {
        	targetEntity = (TLValueWithAttributes) validatable;

        } else if (validatable instanceof TLClosedEnumeration) {
        	targetEntity = (TLClosedEnumeration) validatable;

        } else if (validatable instanceof TLOpenEnumeration) {
        	targetEntity = (TLOpenEnumeration) validatable;

        } else if (validatable instanceof TLChoiceObject) {
        	targetEntity = (TLChoiceObject) validatable;

        } else if (validatable instanceof TLCoreObject) {
        	targetEntity = (TLCoreObject) validatable;

        } else if (validatable instanceof TLBusinessObject) {
        	targetEntity = (TLBusinessObject) validatable;

        } else if (validatable instanceof TLResource) {
        	targetEntity = (TLResource) validatable;

        } else if (validatable instanceof TLOperation) {
        	targetEntity = (TLOperation) validatable;

        } else if (validatable instanceof TLExtensionPointFacet) {
        	targetEntity = (TLExtensionPointFacet) validatable;

        } else if (validatable instanceof TLFacet) {
        	targetEntity = getTargetEntity( ((TLFacet) validatable).getOwningEntity() );

        } else if (validatable instanceof TLActionFacet) {
        	targetEntity = getTargetEntity( ((TLActionFacet) validatable).getOwningResource() );

        } else if (validatable instanceof TLSimpleFacet) {
        	targetEntity = getTargetEntity( ((TLSimpleFacet) validatable).getOwningEntity() );

        } else if (validatable instanceof TLListFacet) {
        	targetEntity = getTargetEntity( ((TLListFacet) validatable).getOwningEntity() );

        } else if (validatable instanceof TLAlias) {
        	targetEntity = getTargetEntity( ((TLAlias) validatable).getOwningEntity() );

        } else if (validatable instanceof TLParamGroup) {
        	targetEntity = getTargetEntity( ((TLParamGroup) validatable).getOwner() );

        } else if (validatable instanceof TLParameter) {
        	targetEntity = getTargetEntity( ((TLParameter) validatable).getOwner() );

        } else if (validatable instanceof TLResourceParentRef) {
        	targetEntity = getTargetEntity( ((TLResourceParentRef) validatable).getOwner() );

        } else if (validatable instanceof TLAction) {
        	targetEntity = getTargetEntity( ((TLAction) validatable).getOwner() );

        } else if (validatable instanceof TLActionRequest) {
        	targetEntity = getTargetEntity( ((TLActionRequest) validatable).getOwner() );

        } else if (validatable instanceof TLActionResponse) {
        	targetEntity = getTargetEntity( ((TLActionResponse) validatable).getOwner() );

        } else if (validatable instanceof TLExtension) {
        	targetEntity = getTargetEntity( ((TLExtension) validatable).getOwner() );

        } else if (validatable instanceof TLDocumentation) {
        	targetEntity = getTargetEntity( ((TLDocumentation) validatable).getOwner() );

        } else if (validatable instanceof TLEquivalent) {
        	targetEntity = getTargetEntity( ((TLEquivalent) validatable).getOwningEntity() );

        } else if (validatable instanceof TLExample) {
        	targetEntity = getTargetEntity( ((TLExample) validatable).getOwningEntity() );

        } else if (validatable instanceof TLAttribute) {
        	targetEntity = getTargetEntity( ((TLAttribute) validatable).getOwner() );

        } else if (validatable instanceof TLProperty) {
        	targetEntity = getTargetEntity( ((TLProperty) validatable).getOwner() );

        } else if (validatable instanceof TLIndicator) {
        	targetEntity = getTargetEntity( ((TLIndicator) validatable).getOwner() );

        } else if (validatable instanceof TLEnumValue) {
        	targetEntity = getTargetEntity( ((TLEnumValue) validatable).getOwningEnum() );

        } else if (validatable instanceof TLRoleEnumeration) {
        	targetEntity = getTargetEntity( ((TLRoleEnumeration) validatable).getOwningEntity() );

        } else if (validatable instanceof TLRole) {
        	targetEntity = getTargetEntity( ((TLRole) validatable).getRoleEnumeration() );
        }
        return targetEntity;
	}
	
}
