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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimpleFacet;

/**
 * Static utility methods that assist with the resolution of <code>TLDocumentation</code>
 * elements and documentation patches within the OTM model.
 */
public class DocumentationUtils {
	
	/**
	 * Returns the path for the given documentation owner within its owning library.
	 * 
	 * @param owner  the documentation owner for which to return a path
	 * @return String
	 */
	public static String getDocumentationPath(TLDocumentationOwner owner) {
		ModelElement currentElement = (ModelElement) owner;
		StringBuilder docPath = new StringBuilder();
		
		while (currentElement != null) {
			if (currentElement instanceof TLService) {
				docPath.insert(0, "@SERVICE");
				currentElement = null;
				
			} else if (currentElement instanceof TLLibraryMember) {
				// All other top-level library members fall into this category
				docPath.insert(0, ((TLLibraryMember) currentElement).getLocalName());
				currentElement = null;
				
			} else if (currentElement instanceof TLSimpleFacet) {
				docPath.insert(0, "|@SIMPLE");
				currentElement = ((TLAbstractFacet) currentElement).getOwningEntity();
				
			} else if (currentElement instanceof TLContextualFacet) {
				TLContextualFacet facet = (TLContextualFacet) currentElement;
				
				docPath.insert(0, "@CONTEXTUAL:" + facet.getLocalName());
				currentElement = null;
				
			} else if (currentElement instanceof TLFacet) {
				TLFacet facet = (TLFacet) currentElement;
				
				docPath.insert(0, "|@FACET:" + facet.getFacetType().toString());
				currentElement = facet.getOwningEntity();
				
			} else if (currentElement instanceof TLListFacet) {
				TLListFacet facet = (TLListFacet) currentElement;
				
				docPath.insert(0, "|@LISTFACET:" + facet.getFacetType().toString());
				currentElement = facet.getOwningEntity();
				
			} else if (currentElement instanceof TLMemberField) {
				TLMemberField<?> field = (TLMemberField<?>) currentElement;
				
				docPath.insert(0, "|" + field.getName());
				currentElement = (ModelElement) field.getOwner();
				
			} else if (currentElement instanceof TLEnumValue) {
				TLEnumValue value = (TLEnumValue) currentElement;
				
				docPath.insert(0, "|" + value.getLiteral());
				currentElement = value.getOwningEnum();
				
			} else if (currentElement instanceof TLRole) {
				TLRole role = (TLRole) currentElement;
				TLRoleEnumeration roleEnum = role.getRoleEnumeration();
				
				docPath.insert(0, "|@ROLE:" + role.getName());
				currentElement = (roleEnum == null) ? null : roleEnum.getOwningEntity();
				
			} else if (currentElement instanceof TLExtension) {
				docPath.insert(0, "|@EXTENSION");
				currentElement = ((TLExtension) currentElement).getOwner();
				
			} else if (currentElement instanceof TLResourceParentRef) {
				TLResourceParentRef parentRef = (TLResourceParentRef) currentElement;
				
				docPath.insert(0, "|@PREF:" + parentRef.getParentResourceName() + "/" + parentRef.getParentParamGroupName());
				currentElement = parentRef.getOwner();
				
			} else if (currentElement instanceof TLParamGroup) {
				TLParamGroup paramGroup = (TLParamGroup) currentElement;
				
				docPath.insert(0, "|@PGRP:" + paramGroup.getName());
				currentElement = paramGroup.getOwner();
				
			} else if (currentElement instanceof TLParameter) {
				TLParameter param = (TLParameter) currentElement;
				TLMemberField<?> fieldRef = param.getFieldRef();
				String fieldName = (fieldRef == null) ? param.getFieldRefName() : fieldRef.getName();
				
				docPath.insert(0, "|" + fieldName);
				currentElement = param.getOwner();
				
			} else if (currentElement instanceof TLAction) {
				TLAction action = (TLAction) currentElement;
				
				docPath.insert(0, "|@ACTION:" + action.getActionId());
				currentElement = action.getOwner();
				
			} else if (currentElement instanceof TLActionRequest) {
				docPath.insert(0, "|@RQ");
				currentElement = ((TLActionRequest) currentElement).getOwner();
				
			} else if (currentElement instanceof TLActionResponse) {
				TLActionResponse response = (TLActionResponse) currentElement;
				
				docPath.insert(0, "|@RS" + getIntegerListIdentity( response.getStatusCodes() ));
				currentElement = response.getOwner();
				
			} else if (currentElement instanceof TLActionFacet) {
				TLActionFacet facet = (TLActionFacet) currentElement;
				
				docPath.insert(0, "|@ACTIONFACET:" + facet.getName());
				currentElement = facet.getOwningResource();
				
			} else if (currentElement instanceof TLOperation) {
				TLOperation op = (TLOperation) currentElement;
				
				docPath.insert(0, "|" + op.getName());
				currentElement = op.getOwningService();
				
			} else if (currentElement instanceof TLContext) {
				TLContext context = (TLContext) currentElement;
				
				docPath.insert(0, "@CONTEXT:" + context.getContextId());
				currentElement = null;
			}
		}
		return docPath.toString();
	}
	
	/**
	 * Returns the documentation owner at the specified path within the given OTM
	 * library.
	 * 
	 * @param docPath  the path of the documentation owner to return
	 * @param library  the OTM library from which to retrieve the documentation owner
	 * @return TLDocumentationOwner
	 */
	public static TLDocumentationOwner getDocumentationOwner(String docPath, TLLibrary library) {
		String[] _pathParts = ((docPath == null) || (docPath.length() <= 1)) ? new String[0] : docPath.split("\\|");
		List<String> pathParts = new ArrayList<>( Arrays.asList( _pathParts ) );
		ModelElement currentElement = (ModelElement) library;
		TLDocumentationOwner docOwner = null;
		
		while ((docOwner == null) && (currentElement != null) && !pathParts.isEmpty()) {
			String pathPart = pathParts.remove( 0 );
			
			if (currentElement instanceof TLLibrary) {
				if (pathPart.equals("@SERVICE")) {
					currentElement = ((TLLibrary) currentElement).getService();
					if (pathParts.isEmpty()) docOwner = (TLDocumentationOwner) currentElement;
				} else if (pathPart.startsWith("@CONTEXT:")) {
					docOwner = ((TLLibrary) currentElement).getContext( pathPart.substring( 9 ) );
				} else if (pathPart.startsWith("@CONTEXTUAL:")) {
					currentElement = (TLContextualFacet) ((TLLibrary) currentElement).getNamedMember( pathPart.substring( 12 ) );
					if (pathParts.isEmpty()) docOwner = (TLDocumentationOwner) currentElement;
				} else {
					currentElement = ((TLLibrary) currentElement).getNamedMember( pathPart );
					if (pathParts.isEmpty()) docOwner = (TLDocumentationOwner) currentElement;
				}
				
			} else if (currentElement instanceof TLMemberFieldOwner) {
				docOwner = (TLDocumentationOwner) ((TLMemberFieldOwner) currentElement).getMemberField( pathPart );
				
			} else if (currentElement instanceof TLAbstractEnumeration) {
				for (TLEnumValue value : ((TLAbstractEnumeration) currentElement).getValues()) {
					if (pathPart.equals( value.getLiteral())) {
						docOwner = value;
						break;
					}
				}
				currentElement = null;
				
			} else if (currentElement instanceof TLService) {
				currentElement = ((TLService) currentElement).getOperation( pathPart );
				if (pathParts.isEmpty()) docOwner = (TLDocumentationOwner) currentElement;
				
			} else if (currentElement instanceof TLResource) {
				if (pathPart.startsWith("@PREF:")) {
					currentElement = ((TLResource) currentElement).getParentRef( pathPart.substring( 6 ) );
				} else if (pathPart.startsWith("@PGRP:")) {
					currentElement = ((TLResource) currentElement).getParamGroup( pathPart.substring( 6 ) );
				} else if (pathPart.startsWith("@ACTION:")) {
					currentElement = ((TLResource) currentElement).getAction( pathPart.substring( 8 ) );
				} else if (pathPart.startsWith("@ACTIONFACET:")) {
					currentElement = ((TLResource) currentElement).getActionFacet( pathPart.substring( 13 ) );
				} else {
					currentElement = null;
				}
				if (pathParts.isEmpty()) docOwner = (TLDocumentationOwner) currentElement;
				
			} else if (currentElement instanceof TLParamGroup) {
				docOwner = ((TLParamGroup) currentElement).getParameter( pathPart );
				
			} else if (currentElement instanceof TLAction) {
				if (pathPart.equals("@RQ")) {
					docOwner = ((TLAction) currentElement).getRequest();
				} else if (pathPart.startsWith("@RS")) {
					String responseId = pathPart.substring( 3 );
					
					for (TLActionResponse response : ((TLAction) currentElement).getResponses()) {
						if (responseId.equals( getIntegerListIdentity( response.getStatusCodes() ))) {
							docOwner = response;
							break;
						}
					}
				}
				
			} else if (pathPart.startsWith("@EXTENSION")) {
				if (currentElement instanceof TLExtensionOwner) {
					docOwner = ((TLExtensionOwner) currentElement).getExtension();
				} else {
					currentElement = null;
				}
				
			} else if (pathPart.startsWith("@SIMPLE")) {
				if (currentElement instanceof TLCoreObject) {
					docOwner = ((TLCoreObject) currentElement).getSimpleFacet();
				} else {
					currentElement = null;
				}
				
			/*
			} else if (pathPart.startsWith("@CONTEXTUAL:")) {
				String[] facetParts = pathPart.substring( 12 ).split( ":" );
				TLFacetType facetType = getFacetType( facetParts[0] );
				String facetName = (facetParts.length >= 2) ? facetParts[1] : null;
				
				if (currentElement instanceof TLFacetOwner) {
					currentElement = FacetCodegenUtils.getFacetOfType( (TLBusinessObject) currentElement, facetType, facetName );
					if (pathParts.isEmpty()) docOwner = (TLDocumentationOwner) currentElement;
				} else {
					currentElement = null;
				}
			*/
				
			} else if (pathPart.startsWith("@FACET:")) {
				TLFacetType facetType = getFacetType( pathPart.substring( 7 ) );
				
				if (currentElement instanceof TLFacetOwner) {
					currentElement = FacetCodegenUtils.getFacetOfType( (TLFacetOwner) currentElement, facetType );
					if (pathParts.isEmpty()) docOwner = (TLDocumentationOwner) currentElement;
				} else {
					currentElement = null;
				}
				
			} else if (pathPart.startsWith("@LISTFACET:")) {
				TLFacetType facetType = getFacetType( pathPart.substring( 11 ) );
				
				if (currentElement instanceof TLCoreObject) {
					if (facetType == TLFacetType.SUMMARY) {
						docOwner = ((TLCoreObject) currentElement).getSummaryFacet();
					} else if (facetType == TLFacetType.DETAIL) {
						docOwner = ((TLCoreObject) currentElement).getDetailFacet();
					} else {
						currentElement = null;
					}
				} else {
					currentElement = null;
				}
				
			} else if (pathPart.startsWith("@ROLE:")) {
				String roleName = pathPart.substring( 6 );
				
				for (TLRole role : ((TLCoreObject) currentElement).getRoleEnumeration().getRoles()) {
					if (roleName.equals( role.getName() )) {
						currentElement = role;
						if (pathParts.isEmpty()) docOwner = (TLDocumentationOwner) currentElement;
						break;
					}
				}
				
			} else {
				currentElement = null;
			}
		}
		return pathParts.isEmpty() ? docOwner : null;
	}
	
	/**
	 * Returns the identity string for the given integer list.
	 * 
	 * @param intList  the integer list for which to return an identity
	 * @return String
	 */
	private static String getIntegerListIdentity(List<Integer> intList) {
		StringBuilder identity = new StringBuilder();
		List<Integer> _list = new ArrayList<>( intList );
		
		Collections.sort( _list );
		
		for (Integer value : _list) {
			identity.append(":" + value);
		}
		return identity.toString();
	}
	
	/**
	 * Returns the facet type indicated by the given string or null if no such facet type
	 * exists.
	 * 
	 * @param facetTypeStr  the string representation of the facet type to return
	 * @return TLFacetType
	 */
	private static TLFacetType getFacetType(String facetTypeStr) {
		TLFacetType facetType = null;
		
		try {
			facetType = TLFacetType.valueOf( facetTypeStr );
			
		} catch (IllegalArgumentException e) {
			// Ignore and return null
		}
		return facetType;
	}
	
}
