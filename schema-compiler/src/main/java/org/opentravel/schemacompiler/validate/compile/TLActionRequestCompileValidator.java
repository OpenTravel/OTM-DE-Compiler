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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLActionRequestBaseValidator;
import org.opentravel.schemacompiler.validate.impl.ResourceUrlValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLActionRequest</code> class.
 * 
 * @author S. Livezey
 */
public class TLActionRequestCompileValidator extends TLActionRequestBaseValidator {

    public static final String ERROR_PARAM_GROUP_REQUIRED      = "PARAM_GROUP_REQUIRED";
    public static final String ERROR_INVALID_PARAM_GROUP       = "INVALID_PARAM_GROUP";
    public static final String ERROR_CONFLICTING_PATH_TEMPLATE = "CONFLICTING_PATH_TEMPLATE";
    public static final String ERROR_GET_REQUEST_PAYLOAD       = "GET_REQUEST_PAYLOAD";
    public static final String ERROR_INVALID_ACTION_FACET_REF  = "INVALID_ACTION_FACET_REF";
    public static final String WARNING_PATCH_PARTIAL_SUPPORT   = "PATCH_PARTIAL_SUPPORT";
    
	private static ResourceUrlValidator urlValidator = new ResourceUrlValidator( true );
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLActionRequest target) {
        TLResource owningResource = (target.getOwner() == null) ? null : target.getOwner().getOwner();
        TLParamGroup paramGroup = target.getParamGroup();
        TLValidationBuilder builder = newValidationBuilder(target);
        
        builder.setProperty("httpMethod", target.getHttpMethod())
        		.setFindingType(FindingType.ERROR).assertNotNull();
        
        if (target.getHttpMethod() == TLHttpMethod.PATCH) {
        	builder.addFinding( FindingType.WARNING, "httpMethod", WARNING_PATCH_PARTIAL_SUPPORT );
        }
        
    	if (paramGroup == null) {
    		String paramGroupName = target.getParamGroupName();
    		
            if ((paramGroupName != null) && !paramGroupName.equals("")) {
            	builder.addFinding(FindingType.ERROR, "paramGroup",
            			TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE, paramGroupName );
            	
            } else if ((target.getPathTemplate() != null) &&
                	!urlValidator.getPathParameters( target.getPathTemplate() ).isEmpty()) {
               	builder.addFinding( FindingType.ERROR, "paramGroup", ERROR_PARAM_GROUP_REQUIRED );
            }
            
    	} else if (owningResource != null) {
    		List<TLParamGroup> inheritedParamGroup =
    				ResourceCodegenUtils.getInheritedParamGroups( owningResource );
    		
    		if (!inheritedParamGroup.contains( paramGroup )) {
            	builder.addFinding( FindingType.ERROR, "paramGroup", ERROR_INVALID_PARAM_GROUP,
            			paramGroup.getName() );
    		}
    	}
    	
    	builder.setProperty("pathTemplate", target.getPathTemplate()).setFindingType(FindingType.ERROR)
    			.assertNotNullOrBlank();
    	validatePathTemplate( target.getPathTemplate(), target.getParamGroup(), builder );
    	checkConflictingPathTemplate( target, builder );
    	
    	if (target.getPayloadType() != null) {
    		TLActionFacet payloadType = target.getPayloadType();
    		
        	if (target.getHttpMethod() == TLHttpMethod.GET) {
            	builder.addFinding( FindingType.ERROR, "payloadType", ERROR_GET_REQUEST_PAYLOAD );
        	}
        	if (owningResource != null) {
        		if (!owningResource.getActionFacets().contains( payloadType ) &&
        				!FacetCodegenUtils.findGhostFacets( owningResource ).contains( payloadType )) {
                	builder.addFinding( FindingType.ERROR, "payloadType", ERROR_INVALID_ACTION_FACET_REF,
                			payloadType.getName() );
        		}
        	}
            builder.setProperty("mimeTypes", target.getMimeTypes()).setFindingType(FindingType.ERROR)
            		.assertMinimumSize( 1 );
        	
    	} else {
    		String payloadTypeName = target.getPayloadTypeName();
    		
    		if ((payloadTypeName != null) && (payloadTypeName.length() > 0)) {
            	builder.addFinding(FindingType.ERROR, "payloadType",
            			TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE, payloadTypeName );
                builder.setProperty("mimeTypes", target.getMimeTypes()).setFindingType(FindingType.ERROR)
                		.assertMinimumSize( 1 );
    		} else {
                builder.setProperty("mimeTypes", target.getMimeTypes()).setFindingType(FindingType.WARNING)
                		.assertMaximumSize( 0 );
    		}
    	}
    	
		return builder.getFindings();
	}
	
	/**
	 * Determines whether the path template for the target request conflicts with any
	 * of the action requests declared by the owning resource.  Two path templates will
	 * only be considered conflicting if they also have the same HTTP method assigned.
	 * 
	 * @param target  the action request being validated
	 * @param builder  the validation builder to which any findings will be posted
	 */
	private void checkConflictingPathTemplate(TLActionRequest target, TLValidationBuilder builder) {
        TLResource owningResource = (target.getOwner() == null) ? null : target.getOwner().getOwner();
        String targetTestPath = buildTestPath( target.getPathTemplate() );
        TLHttpMethod targetMethod = target.getHttpMethod();
        
        if ((owningResource != null) && (targetTestPath != null)) {
            List<TLActionRequest> resourceRequests = getResourceRequests( owningResource );
        	
            for (TLActionRequest request : resourceRequests) {
            	if ((request == target) || (request.getHttpMethod() != targetMethod)) {
            		continue; // skip requests that cannot create a conflict
            	}
            	String testPath = buildTestPath( request.getPathTemplate() );
            	
            	if (targetTestPath.equals( testPath )) {
                	builder.addFinding( FindingType.ERROR, "pathTemplate", ERROR_CONFLICTING_PATH_TEMPLATE,
                			target.getPathTemplate(), target.getHttpMethod() );
            	}
            }
        }
	}
	
	/**
	 * Returns the list of actions declared or inherited by the given resource.
	 * 
	 * @param resource  the resource for which to return the associated actions
	 * @return List<TLActionRequest>
	 */
	@SuppressWarnings("unchecked")
	private List<TLActionRequest> getResourceRequests(TLResource resource) {
        String cacheKey = resource.getNamespace() + ":" + resource.getLocalName() + ":resourceRequests";
		List<TLActionRequest> requestList = (List<TLActionRequest>) getContextCacheEntry( cacheKey );
		
		if (requestList == null) {
			requestList = new ArrayList<>();
			
			for (TLAction action : ResourceCodegenUtils.getInheritedActions( resource )) {
				TLActionRequest request = ResourceCodegenUtils.getDeclaredOrInheritedRequest( action );
				
				if (request != null) {
					requestList.add( request );
				}
			}
			setContextCacheEntry( cacheKey, requestList );
		}
		return requestList;
	}
	
}
