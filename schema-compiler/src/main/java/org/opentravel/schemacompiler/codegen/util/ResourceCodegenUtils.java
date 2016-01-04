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
package org.opentravel.schemacompiler.codegen.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.XSDSimpleType;

/**
 * Shared static methods used during the code generation for <code>TLResource</code> entities.
 * 
 * @author S. Livezey
 */
public class ResourceCodegenUtils {
	
	private static final Set<Class<?>> eligibleParamTypes;
	
	/**
	 * Returns the resource from which the given one extends.  If the given resource
	 * does not extend another, this method will return null.
	 * 
	 * @param resource  the resource for whict to return an extension
	 * @return TLResource
	 */
	public static TLResource getExtendedResource(TLResource resource) {
		TLExtension extension = (resource == null) ? null : resource.getExtension();
		TLResource extendedResource = null;
		
		if ((extension != null) && (extension.getExtendsEntity() instanceof TLResource)) {
			extendedResource = (TLResource) extension.getExtendsEntity();
		}
		return extendedResource;
	}
	
	/**
	 * Returns the list of all parameter groups declared and inherited by the given
	 * resource.
	 * 
	 * @param resource  the resource for which to return inherited parameter groups
	 * @return List<TLParamGroup>
	 */
	public static List<TLParamGroup> getInheritedParamGroups(TLResource resource) {
		List<TLParamGroup> paramGroups = new ArrayList<>();
		Set<String> paramGroupNames = new HashSet<>();
		
		for (TLResource extendedResource : getInheritanceHierarchy(resource)) {
			List<TLParamGroup> localParamGroups = new ArrayList<>();
			
			for (TLParamGroup paramGroup : extendedResource.getParamGroups()) {
				if (!paramGroupNames.contains(paramGroup.getName())) {
					localParamGroups.add(paramGroup);
					paramGroupNames.add(paramGroup.getName());
				}
			}
			paramGroups.addAll(0, localParamGroups);
		}
		Collections.reverse(paramGroups);
		return paramGroups;
	}
	
	/**
	 * Returns the list of parameters declared and inherited by the given parameter
	 * group.
	 * 
	 * @param paramGroup  the parameter group for which to return inherited parameters
	 * @return List<TLParameter>
	 */
	public static List<TLParameter> getInheritedParameters(TLParamGroup paramGroup) {
		List<TLParameter> parameters = new ArrayList<>();
		String paramGroupName = paramGroup.getName();
		Set<String> parameterNames = new HashSet<>();
		
		for (TLResource extendedResource : getInheritanceHierarchy(paramGroup.getOwner())) {
			TLParamGroup extendedGroup = extendedResource.getParamGroup(paramGroupName);
			
			if (extendedGroup != null) {
				List<TLParameter> localParameters = new ArrayList<>();
				
				for (TLParameter param : extendedGroup.getParameters()) {
					String paramName = param.getFieldRefName();
					
					if ((paramName != null) && !parameterNames.contains(paramName)) {
						localParameters.add(param);
						parameterNames.add(paramName);
					}
				}
				parameters.addAll(0, localParameters);
			}
		}
		return parameters;
	}
	
	/**
	 * Returns true if the given entity type is a valid type to assign for a
	 * <code>TLParameter</code> field.
	 * 
	 * @param entityType  the attribute or property type to analyze
	 * @return boolean
	 */
	public static boolean isEligibleParameterType(NamedEntity entityType) {
		return (entityType != null) && eligibleParamTypes.contains(entityType.getClass());
	}
	
	/**
	 * Returns the list of all actions declared and inherited by the given resource.
	 * 
	 * @param resource  the resource for which to return inherited actions
	 * @return List<TLAction>
	 */
	public static List<TLAction> getInheritedActions(TLResource resource) {
		List<TLAction> actions = new ArrayList<>();
		Set<String> actionIds = new HashSet<>();
		
		for (TLResource extendedResource : getInheritanceHierarchy(resource)) {
			List<TLAction> localActions = new ArrayList<>();
			
			for (TLAction action : extendedResource.getActions()) {
				if (!actionIds.contains(action.getActionId())) {
					localActions.add(action);
					actionIds.add(action.getActionId());
				}
			}
			actions.addAll(0, localActions);
		}
		Collections.reverse(actions);
		return actions;
	}
	
	/**
	 * Returns the request (if any) that is declared or inherited by the given
	 * resource action.
	 * 
	 * @param action  the action for which to return a request
	 * @return TLActionRequest
	 */
	public static TLActionRequest getDeclaredOrInheritedRequest(TLAction action) {
		String actionId = action.getActionId();
		TLActionRequest request = null;
		
		if (actionId != null) {
			for (TLResource extendedResource : getInheritanceHierarchy(action.getOwner())) {
				TLAction extendedAction = extendedResource.getAction(actionId);
				
				if (extendedAction != null) {
					if ((request = extendedAction.getRequest()) != null) {
						break;
					}
				}
			}
		} else {
			// If the action ID is null, only return the request from the given action.  Do
			// not look for inherited requests.
			request = action.getRequest();
		}
		return request;
	}
	
	/**
	 * Returns the list of responses that are declared or inherited by the given
	 * resource action.
	 * 
	 * @param action  the action for which to return inherited responses
	 * @return List<TLActionResponse>
	 */
	public static List<TLActionResponse> getInheritedResponses(TLAction action) {
		List<TLResource> resourceHierarchy = getInheritanceHierarchy(action.getOwner());
		List<TLActionResponse> responses = new ArrayList<>();
		Set<Integer> statusCodes = new HashSet<>();
		String actionId = action.getActionId();
		
		// Start by adding responses from all of the inherited, non-common actions
		for (TLResource extendedResource : resourceHierarchy) {
			TLAction extendedAction = extendedResource.getAction(actionId);
			
			if ((extendedAction != null) && !extendedAction.isCommonAction()) {
				List<TLActionResponse> localResponses = new ArrayList<>();
				
				for (TLActionResponse response : extendedAction.getResponses()) {
					if (!statusCodes.containsAll(response.getStatusCodes())) {
						localResponses.add(response);
						statusCodes.addAll(response.getStatusCodes());
					}
				}
				responses.addAll(0, localResponses);
			}
		}
		
		// Finish by incorporating responses from all of the inherited common
		// actions.  Note that common actions are included, regardless of whether
		// their action ID's match that of the original.
		for (TLResource extendedResource : resourceHierarchy) {
			for (TLAction inheritedAction : extendedResource.getActions()) {
				if (inheritedAction.isCommonAction()) {
					responses.addAll( inheritedAction.getResponses() );
				}
			}
		}
		return responses;
	}
	
	/**
	 * Returns the inheritance hierarchy for the given resource starting with the
	 * given resource and ending with the highest-level ancestor.
	 * 
	 * @param resource  the resource for which to return the hierarchy
	 * @return List<TLResource>
	 */
	public static List<TLResource> getInheritanceHierarchy(TLResource resource) {
		List<TLResource> hierarchyList = new ArrayList<>();
		TLResource extendedResource = resource;
		
		if (resource != null) {
			while ((extendedResource != null) && !hierarchyList.contains(extendedResource)) {
				hierarchyList.add(extendedResource);
				extendedResource = getExtendedResource(extendedResource);
			}
		}
		return hierarchyList;
	}
	
	/**
	 * Returns a valid name that may be used by an action facet to reference the given facet.
	 * 
	 * @param facet  the facet to be referenced by an action facet
	 * @return String
	 */
	public static String getActionFacetReferenceName(TLFacet facet) {
		return facet.getFacetType().getIdentityName( facet.getContext(), facet.getLabel() );
	}
	
	/**
	 * Returns the facet from the given business object that matches the reference facet name
	 * provided.  If no matching facet exists for the business object, this method will return
	 * null.
	 * 
	 * @param businessObject  the business object from which to return a matching facet
	 * @param referenceFacetName  the action facet reference name that must be matched by the returned facet
	 * @return TLFacet
	 */
	public static TLFacet getReferencedFacet(TLBusinessObject businessObject, String referenceFacetName) {
		List<TLFacet> boFacets = new ArrayList<>();
		TLFacet referencedFacet = null;
		
		boFacets.add( businessObject.getIdFacet() );
		boFacets.add( businessObject.getSummaryFacet() );
		boFacets.add( businessObject.getDetailFacet() );
		boFacets.addAll( businessObject.getCustomFacets() );
		boFacets.addAll( businessObject.getQueryFacets() );
		
		for (TLFacet boFacet : boFacets) {
			String boFacetReferenceName = getActionFacetReferenceName( boFacet );
			
			if (boFacetReferenceName.equals( referenceFacetName )) {
				referencedFacet = boFacet;
				break;
			}
		}
		return referencedFacet;
	}
	
	/**
	 * If the given action facet specifies a reference to the resource's business object,
	 * this method will return a ghost-element that can be used to generate schema content
	 * and examples.  If a business object is not referenced, this method will return null.
	 * 
	 * @param actionFacet  the action facet for which to return the business object element
	 * @return TLProperty
	 */
	public static TLProperty getBusinessObjectElement(TLActionFacet actionFacet) {
		TLResource owningResource = (TLResource) actionFacet.getOwningEntity();
		TLBusinessObject referencedBO = (owningResource == null) ? null : owningResource.getBusinessObjectRef();
    	TLReferenceType refType = actionFacet.getReferenceType();
		TLProperty boElement = null;
		
		if ((referencedBO != null)
				&& ((refType == TLReferenceType.OPTIONAL) || (refType == TLReferenceType.REQUIRED))) {
    		TLPropertyType elementType = referencedBO;
    		
    		boElement = new TLProperty();
    		
    		if (actionFacet.getReferenceFacetName() != null) {
    			elementType = ResourceCodegenUtils.getReferencedFacet(
    					referencedBO, actionFacet.getReferenceFacetName() );
    		}
    		boElement.setName( elementType.getLocalName() );
    		boElement.setType( elementType );
    		boElement.setMandatory( (refType == TLReferenceType.REQUIRED ) );
    		
    		if (actionFacet.getReferenceRepeat() > 1) {
    			boElement.setRepeat( actionFacet.getReferenceRepeat() );
    		}
		}
		return boElement;
	}
	
	/**
	 * Initializes the list of eligible simple types for <code>TLParameter</code> definitions.
	 */
	static {
		try {
			Set<Class<?>> paramTypes = new HashSet<>();
			
			paramTypes.add( TLSimple.class );
			paramTypes.add( TLSimpleFacet.class );
			paramTypes.add( TLClosedEnumeration.class );
			paramTypes.add( TLRoleEnumeration.class );
			paramTypes.add( XSDSimpleType.class );
			eligibleParamTypes = Collections.unmodifiableSet(paramTypes);
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
