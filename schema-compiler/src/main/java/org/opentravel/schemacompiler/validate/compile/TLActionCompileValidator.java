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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLActionBaseValidator;
import org.opentravel.schemacompiler.validate.impl.IdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLAction</code> class.
 * 
 * @author S. Livezey
 */
public class TLActionCompileValidator extends TLActionBaseValidator {

    public static final String ERROR_MISSING_REQUIRED_REQUEST   = "MISSING_REQUIRED_REQUEST";
    public static final String ERROR_CONFLICTING_STATUS_CODES   = "CONFLICTING_STATUS_CODES";
    public static final String ERROR_MULTIPLE_DEFAULT_RESPONSES = "MULTIPLE_DEFAULT_RESPONSES";
    public static final String WARNING_IGNORING_REQUEST         = "IGNORING_REQUEST";
    
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLAction target) {
        TLActionRequest request = ResourceCodegenUtils.getDeclaredOrInheritedRequest( target );
        List<TLActionResponse> responses = ResourceCodegenUtils.getInheritedResponses( target );
        List<Integer> duplicateResponseCodes = getDuplicateResponseCodes( responses );
        TLValidationBuilder builder = newValidationBuilder(target);
        
        builder.setProperty("actionId", target.getActionId()).setFindingType(FindingType.ERROR)
        		.assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);

        builder.setProperty("actionId", target.getOwner().getActions())
        		.setFindingType(FindingType.ERROR)
        		.assertNoDuplicates(new IdentityResolver<TLAction>() {
        			public String getIdentity(TLAction entity) {
        				return entity.getActionId();
        			}
        		});
        
    	if (target.isCommonAction()) {
    		if (request != null) {
            	builder.addFinding( FindingType.WARNING, "request", WARNING_IGNORING_REQUEST );
    		}
    	} else {
    		if (request == null) {
            	builder.addFinding( FindingType.ERROR, "request", ERROR_MISSING_REQUIRED_REQUEST );
    		}
    	}
    	
        builder.setProperty("responses", ResourceCodegenUtils.getInheritedResponses( target ))
        		.setFindingType(FindingType.ERROR)
        		.assertMinimumSize( 1 );
        
        if (!duplicateResponseCodes.isEmpty()) {
        	builder.addFinding( FindingType.ERROR, "responses", ERROR_CONFLICTING_STATUS_CODES,
        			toCsvString( duplicateResponseCodes ) );
        }
        
        if (getDefaultResponses( responses ).size() > 1) {
        	builder.addFinding( FindingType.ERROR, "responses", ERROR_MULTIPLE_DEFAULT_RESPONSES );
        }
        return builder.getFindings();
	}
	
	/**
	 * Returns the list of duplicate HTTP response codes used across the declared
	 * and inherited responses.
	 * 
	 * @param responseList  the list of declared or inherited responses
	 * @return List<Integer>
	 */
	private List<Integer> getDuplicateResponseCodes(List<TLActionResponse> responseList) {
		Set<Integer> existingCodes = new HashSet<>();
		List<Integer> duplicateCodes = new ArrayList<>();
		
		for (TLActionResponse response : responseList) {
			for (Integer statusCode : response.getStatusCodes()) {
				if (existingCodes.contains( statusCode )) {
					if (!duplicateCodes.contains( statusCode )) {
						duplicateCodes.add( statusCode );
					}
				} else {
					existingCodes.add( statusCode );
				}
			}
		}
		Collections.sort( duplicateCodes );
		return duplicateCodes;
	}
	
	/**
	 * Returns the list of 'default' responses from the list provided.
	 * 
	 * @param responseList  the list of declared or inherited responses
	 * @return List<TLActionResponse>
	 */
	private List<TLActionResponse> getDefaultResponses(List<TLActionResponse> responseList) {
		List<TLActionResponse> defaultResponses = new ArrayList<>();
		
		for (TLActionResponse response : responseList) {
			if (response.getStatusCodes().isEmpty()) {
				defaultResponses.add( response );
			}
		}
		return defaultResponses;
	}
	
}
