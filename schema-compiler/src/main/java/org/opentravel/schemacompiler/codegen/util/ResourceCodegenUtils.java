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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerScheme;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDSimpleType;

/**
 * Shared static methods used during the code generation for <code>TLResource</code> entities.
 * 
 * @author S. Livezey
 */
public class ResourceCodegenUtils {
	
    /** This expression derived/taken from the BNF for URI (RFC2396). */
    private static final String URL_REGEX =
            "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
    //        12            3  4          5       6   7        8 9
    private static final Pattern URL_PATTERN = Pattern.compile( URL_REGEX );
    private static final int SCHEME_GROUP    = 2;
    private static final int AUTHORITY_GROUP = 4;
    private static final int PATH_GROUP      = 5;
    private static final int QUERY_GROUP     = 7;
    
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
	public static List<TLResourceParentRef> getInheritedParentRefs(TLResource resource) {
		List<TLResourceParentRef> parentRefs = new ArrayList<>();
		Set<String> parentResourceNames = new HashSet<>();
		
		for (TLResource extendedResource : getInheritanceHierarchy(resource)) {
			List<TLResourceParentRef> localParentRefs = new ArrayList<>();
			
			for (TLResourceParentRef parentRef : extendedResource.getParentRefs()) {
				if ((parentRef.getParentResource() != null)
						&& !parentResourceNames.contains(parentRef.getParentResource().getName())) {
					localParentRefs.add(parentRef);
					parentResourceNames.add(parentRef.getParentResource().getName());
				}
			}
			parentRefs.addAll(0, localParentRefs);
		}
		Collections.reverse(parentRefs);
		return parentRefs;
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
					String paramName = (param.getFieldRef() == null) ? param.getFieldRefName() : param.getFieldRef().getName();
					
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
	 * Returns the list of member fields from the given facet that are eligible to be
	 * declared as parameters in a resource parameter group.  The resulting list will
	 * include any indicators and simple type attributes and elements that are not
	 * contained within repeating elements of the given facet or its children.
	 * 
	 * @param facet  the facet for which to return a list of eligible member fields
	 * @return List<TLMemberField<TLFacet>>
	 * @throws IllegalArgumentException  thrown if the given facet does not belong
	 *									 to a business object
	 */
	public static List<TLMemberField<?>> getEligibleParameterFields(TLFacet facet) {
		return getParameterFields( facet, false );
	}
	
	/**
	 * Returns the list of all member fields from the given facet, regardless of
	 * whether they eligible to be parameters in a resource parameter group.  This
	 * method is intended only for validation purposes since some members of the
	 * resulting list may not be considered legal parameters.
	 * 
	 * @param facet  the facet for which to return a list of member fields
	 * @return List<TLMemberField<TLFacet>>
	 * @throws IllegalArgumentException  thrown if the given facet does not belong
	 *									 to a business object
	 */
	public static List<TLMemberField<?>> getAllParameterFields(TLFacet facet) {
		return getParameterFields( facet, true );
	}
	
	/**
	 * Returns the list of all member fields from the given facet.
	 * 
	 * @param facet  the facet for which to return a list of member fields
	 * @param includeIneligibleFields  flag indicating whether to include ineligible fields
	 * @return List<TLMemberField<TLFacet>>
	 * @throws IllegalArgumentException  thrown if the given facet does not belong
	 *									 to a business object
	 */
	private static List<TLMemberField<?>> getParameterFields(TLFacet facet, boolean includeIneligibleFields) {
		List<TLMemberField<?>> paramFields = new ArrayList<>();
		List<TLFacet> paramFacets = new ArrayList<>();
		Set<String> fieldNames = new HashSet<>();
		
		findParameterFacets(facet, paramFacets, includeIneligibleFields);
		
		for (TLFacet eligibleFacet : paramFacets) {
			List<TLAttribute> attributeList = PropertyCodegenUtils.getInheritedAttributes(eligibleFacet);
			List<TLProperty> elementList = PropertyCodegenUtils.getInheritedProperties(eligibleFacet);
			List<TLIndicator> indicatorList = PropertyCodegenUtils.getInheritedIndicators(eligibleFacet);
			
			for (TLAttribute attribute : attributeList) {
				if (!fieldNames.contains(attribute.getName())) {
					paramFields.add(attribute);
					fieldNames.add(attribute.getName());
				}
			}
			for (TLProperty element : elementList) {
				if (includeIneligibleFields || (element.getRepeat() == 0)
						|| (element.getRepeat() == 1)) { // Skip repeating elements unless specifically requested
					if (element.getType() instanceof TLValueWithAttributes) {
						TLValueWithAttributes vwa = (TLValueWithAttributes) element.getType();
						List<TLAttribute> vwaAttributes = PropertyCodegenUtils.getInheritedAttributes(vwa);
						List<TLIndicator> vwaIndicators = PropertyCodegenUtils.getInheritedIndicators(vwa);
						
						for (TLAttribute attribute : vwaAttributes) {
							if (!(attribute.getType() instanceof TLValueWithAttributes) &&
									!fieldNames.contains(attribute.getName())) {
								paramFields.add(attribute);
								fieldNames.add(attribute.getName());
							}
						}
						for (TLIndicator indicator : vwaIndicators) {
							if (!fieldNames.contains(indicator.getName())) {
								paramFields.add(indicator);
								fieldNames.add(indicator.getName());
							}
						}
						
					} else if (isEligibleParameterType(element.getType())) {
						QName schemaName = XsdCodegenUtils.getGlobalElementName(element.getType());
						String elementName = (schemaName != null) ? schemaName.getLocalPart() : element.getName();
						
						if (!fieldNames.contains(elementName)) {
							paramFields.add(element);
							fieldNames.add(elementName);
						}
					}
				}
			}
			for (TLIndicator indicator : indicatorList) {
				if (!fieldNames.contains(indicator.getName())) {
					paramFields.add(indicator);
					fieldNames.add(indicator.getName());
				}
			}
		}
		return paramFields;
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
	 * Finds the list of all facets that belong to or are contained within the given one.
	 * 
	 * @param facet  the facet for which to return parameter facets
	 * @param paramFacets  the list to which parameter facets will be appended
	 * @param includeIneligibleFacets  flag indicating whether ineligible facets should
	 *								   be included in the resulting list
	 */
	private static void findParameterFacets(TLFacet facet, List<TLFacet> paramFacets,
			boolean includeIneligibleFacets) {
		if (paramFacets.contains(facet)) {
			return; // avoid circular references
		}
		paramFacets.add(facet);
		
		for (TLProperty element : PropertyCodegenUtils.getInheritedProperties(facet)) {
			if (includeIneligibleFacets || (element.getRepeat() == 0)
					|| (element.getRepeat() == 1)) { // Skip repeating elements unless specifically requested
				TLPropertyType elementType = element.getType();
				
				if (elementType instanceof TLAlias) {
					TLAliasOwner aliasOwner = ((TLAlias) elementType).getOwningEntity();
					
					if (aliasOwner instanceof TLPropertyType) {
						elementType = (TLPropertyType) aliasOwner;
					}
				}
				
				if (elementType instanceof TLFacet) {
					findParameterFacets((TLFacet) elementType, paramFacets, includeIneligibleFacets);
					
				} else if (elementType instanceof TLBusinessObject) {
					TLBusinessObject bo = (TLBusinessObject) elementType;
					
					findParameterFacets(bo.getIdFacet(), paramFacets, includeIneligibleFacets);
					findParameterFacets(bo.getSummaryFacet(), paramFacets, includeIneligibleFacets);
					findParameterFacets(bo.getDetailFacet(), paramFacets, includeIneligibleFacets);
					
					for (TLFacet customFacet : bo.getCustomFacets()) {
						findParameterFacets(customFacet, paramFacets, includeIneligibleFacets);
					}
					
				} else if (elementType instanceof TLCoreObject) {
					TLCoreObject core = (TLCoreObject) elementType;
					
					findParameterFacets(core.getSummaryFacet(), paramFacets, includeIneligibleFacets);
					findParameterFacets(core.getDetailFacet(), paramFacets, includeIneligibleFacets);
					
				} else if (elementType instanceof TLChoiceObject) {
					TLChoiceObject choice = (TLChoiceObject) elementType;
					
					findParameterFacets(choice.getSharedFacet(), paramFacets, includeIneligibleFacets);
					
					for (TLFacet choiceFacet : choice.getChoiceFacets()) {
						findParameterFacets(choiceFacet, paramFacets, includeIneligibleFacets);
					}
				}
			}
		}
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
	 * Returns the list of all actions declared and inherited by the given resource.
	 * 
	 * @param resource  the resource for which to return inherited actions
	 * @return List<QualifiedAction>
	 */
	public static List<QualifiedAction> getQualifiedActions(TLResource resource) {
		List<QualifiedAction> actionList = new ArrayList<>();
		
		for (TLAction action : getInheritedActions( resource )) {
			if (resource.isFirstClass()) {
				// First-class resources can be accessed independently of a parent resource
				actionList.add( new QualifiedAction( null, action ) );
			}
			for (TLResourceParentRef parentRef : getInheritedParentRefs( resource )) {
				buildQualifiedActions( action, parentRef, new ArrayList<TLResourceParentRef>(), actionList );
			}
		}
		return actionList;
	}
	
	/**
	 * Recursive method that constructs all of the valid permutations of qualified actions.
	 * 
	 * @param action  the action to be associated with any qualified action that is created
	 * @param parentRef  the current parent reference for the qualified action that should be considered
	 * @param parentList  the list of child resource parent references that are currently under consideration
	 * @param actionList  the list of qualified actions that have already been created
	 */
	private static void buildQualifiedActions(TLAction action, TLResourceParentRef parentRef,
			List<TLResourceParentRef> parentList, List<QualifiedAction> actionList) {
		TLResource parentResource = parentRef.getParentResource();
		List<TLResourceParentRef> newParentList = new ArrayList<>( parentList );
		
		newParentList.add( parentRef );
		
		if (parentResource.isFirstClass()) {
			actionList.add( new QualifiedAction( newParentList, action ) );
		}
		for (TLResourceParentRef pRef : getInheritedParentRefs( parentResource )) {
			buildQualifiedActions( action, pRef, newParentList, actionList );
		}
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
		List<TLActionResponse> defaultResponses = new ArrayList<>();
		List<TLActionResponse> responses = new ArrayList<>();
		Set<Integer> statusCodes = new HashSet<>();
		String actionId = action.getActionId();
		
		// Start by adding responses from all of the inherited, non-common actions
		for (TLResource extendedResource : resourceHierarchy) {
			for (TLAction inheritedAction : extendedResource.getActions()) {
				List<TLActionResponse> localDefaultResponses = new ArrayList<>();
				List<TLActionResponse> localResponses = new ArrayList<>();
				String inheritedActionId = inheritedAction.getActionId();
				
				if (inheritedAction.isCommonAction() ||
						((inheritedActionId != null) && inheritedActionId.equals( actionId ))) {
					// Note that responses for common actions are included, regardless
					// of whether their action ID's match that of the original.
					for (TLActionResponse response : inheritedAction.getResponses()) {
						if (response.getStatusCodes().isEmpty()) {
							localDefaultResponses.add(response);
							
						} else if (!statusCodes.containsAll(response.getStatusCodes())) {
							localResponses.add(response);
							statusCodes.addAll(response.getStatusCodes());
						}
					}
				}
				if (defaultResponses.isEmpty()) {
					// Default responses are considered to be overridden if any have been
					// detected in extended resources
					defaultResponses = localDefaultResponses;
				}
				responses.addAll(0, localResponses);
			}
		}
		responses.addAll( defaultResponses );
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
	 * Returns the message payload as either the given action facet, the business
	 * object that is referenced by the owning resource, or the core/choice object
	 * that defines the payload.
	 * 
	 * @param request  the request for which to return a payload type
	 * @return NamedEntity
	 */
	public static NamedEntity getPayloadType(TLActionFacet actionFacet) {
		NamedEntity payloadType = actionFacet;
		
		if (actionFacet != null) {
			if (actionFacet.getBasePayload() != null) {
				if (actionFacet.getReferenceType() == TLReferenceType.NONE) {
					payloadType = actionFacet.getBasePayload();
				}
			} else {
				TLResource owningResource = actionFacet.getOwningResource();
				TLBusinessObject referencedBO = (owningResource == null) ? null : owningResource.getBusinessObjectRef();
				
				if (referencedBO != null) {
					int repeatCount = actionFacet.getReferenceRepeat();
					
					if ((repeatCount == 0) || (repeatCount == 1)) {
			    		if (actionFacet.getReferenceFacetName() != null) {
			    			payloadType = ResourceCodegenUtils.getReferencedFacet(
			    					referencedBO, actionFacet.getReferenceFacetName() );
			    		} else {
							payloadType = referencedBO;
			    		}
					}
				}
			}
		}
		return payloadType;
	}
	
	/**
	 * If the given action facet specifies a reference to the resource's business object,
	 * this method will return a ghost-element that can be used to generate schema content
	 * and examples.  If a business object is not referenced, this method will return null.
	 * 
	 * @param actionFacet  the action facet for which to return the business object element
	 * @param owner  the owner of the ghost-property that will be returned
	 * @return TLProperty
	 */
	public static TLProperty createBusinessObjectElement(TLActionFacet actionFacet, TLPropertyOwner owner) {
		TLResource owningResource = actionFacet.getOwningResource();
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
    		boElement.setOwner( owner );
    		boElement.setMandatory( (refType == TLReferenceType.REQUIRED ) );
    		
    		if (actionFacet.getReferenceRepeat() > 1) {
    			boElement.setRepeat( actionFacet.getReferenceRepeat() );
    		}
		}
		return boElement;
	}
	
	/**
	 * Parses the components of the given URL string.
	 * 
	 * @param url  the URL to parse
	 * @return URLComponents
	 * @throws MalformedURLException  thrown if the URL is not valid
	 */
	public static URLComponents parseUrl(String url) throws MalformedURLException {
		Matcher m = URL_PATTERN.matcher( url );
		SwaggerScheme scheme;
		String urlPath;
		
		if (!m.matches()) {
			throw new MalformedURLException("The URL is not valid - " + url);
		}
		urlPath = m.group( PATH_GROUP );
		
		if ((urlPath == null) || (urlPath.length() == 0)) {
			urlPath = "/";
		}
		scheme = SwaggerScheme.fromDisplayValue( m.group( SCHEME_GROUP ) );
		
		return new URLComponents( (scheme == null) ? SwaggerScheme.HTTP : scheme,
				m.group( AUTHORITY_GROUP ), urlPath, m.group( QUERY_GROUP ) );
	}
	
	/**
	 * Encapsulates the components of a parsed URL.
	 */
	public static class URLComponents {
		
		private SwaggerScheme scheme;
		private String authority;
		private String path;
		private String queryString;
		
		/**
		 * Full constructor.
		 * 
		 * @param scheme  the scheme of a parsed URL
		 * @param authority  the authority of a parsed URL
		 * @param path  the path string of a parsed URL
		 * @param queryString  the query string of a parsed URL
		 */
		public URLComponents(SwaggerScheme scheme, String authority, String path, String queryString) {
			this.scheme = scheme;
			this.authority = authority;
			this.path = path;
			this.queryString = queryString;
		}

		/**
		 * Returns the scheme of a parsed URL.
		 *
		 * @return SwaggerScheme
		 */
		public SwaggerScheme getScheme() {
			return scheme;
		}

		/**
		 * Returns the authority of a parsed URL.
		 *
		 * @return String
		 */
		public String getAuthority() {
			return authority;
		}

		/**
		 * Returns the path string of a parsed URL (always begins with a '/';
		 * a no path URL returns "/"). 
		 *
		 * @return String
		 */
		public String getPath() {
			return path;
		}

		/**
		 * Returns the query string of a parsed URL (may be null).
		 *
		 * @return String
		 */
		public String getQueryString() {
			return queryString;
		}
		
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
