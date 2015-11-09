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

import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLResourceBaseValidator;
import org.opentravel.schemacompiler.validate.impl.ResourceUrlValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLResource</code> class.
 * 
 * @author S. Livezey
 */
public class TLResourceCompileValidator extends TLResourceBaseValidator {

    public static final String ERROR_INVALID_BASE_PATH        = "INVALID_BASE_PATH";
    public static final String ERROR_INVALID_BUSINESS_OBJ_NS  = "INVALID_BUSINESS_OBJ_NS";
    public static final String ERROR_PARAM_GROUPS_NOT_ALLOWED = "PARAM_GROUPS_NOT_ALLOWED";
    public static final String ERROR_MULTIPLE_COMMON_ACTIONS  = "MULTIPLE_COMMON_ACTIONS";
    
	private static ResourceUrlValidator urlValidator = new ResourceUrlValidator();
	
    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLResource target) {
        TLValidationBuilder builder = newValidationBuilder(target);
        
        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
        		.assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);
        
        // Different rules for abstract and non-abstract resources
        if (target.isAbstract()) {
        	builder.setProperty("basePath", target.getBasePath()).setFindingType(FindingType.ERROR)
            		.assertNullOrBlank();
        	builder.setProperty("businessObjectRef", target.getBusinessObjectRef()).setFindingType(FindingType.ERROR)
        			.assertNull();
        	
        	if (target.getParamGroups().size() > 0) {
            	builder.addFinding( FindingType.ERROR, "paramGroups", ERROR_PARAM_GROUPS_NOT_ALLOWED );
        	}
        	
        } else {
        	TLBusinessObject businessObjectRef = target.getBusinessObjectRef();
        	String basePath = target.getBasePath();
        	
        	builder.setProperty("basePath", basePath).setFindingType(FindingType.ERROR)
        			.assertNotNullOrBlank();
        	
        	if ((basePath != null) &&
        			!(urlValidator.isValid( basePath ) || urlValidator.isValidPath( basePath ))) {
            	builder.addFinding( FindingType.ERROR, "basePath", ERROR_INVALID_BASE_PATH, basePath );
        	}
        	builder.setEntityReferenceProperty("businessObjectRef", businessObjectRef, target.getBusinessObjectRefName())
        			.setFindingType(FindingType.ERROR)
        			.assertNotNull();
        	
        	if ((businessObjectRef != null) && (target.getNamespace() != null)
        			&& !target.getNamespace().equals(businessObjectRef.getNamespace())) {
            	builder.addFinding( FindingType.ERROR, "businessObjectRef", ERROR_INVALID_BUSINESS_OBJ_NS );
        	}
        }
        
        if (countCommonActions( target ) > 1) {
        	builder.addFinding( FindingType.ERROR, "actions", ERROR_MULTIPLE_COMMON_ACTIONS );
        }
        
        checkSchemaNamingConflicts(target, builder);
        validateVersioningRules(target, builder);
        
        return builder.getFindings();
    }
    
    /**
     * Returns the number of common actions declared for the resource.
     * 
     * @param target  the target resource being validated
     * @return int
     */
    private int countCommonActions(TLResource target) {
    	int commonCount = 0;
    	
    	for (TLAction action : target.getActions()) {
    		if (action.isCommonAction()) {
    			commonCount++;
    		}
    	}
    	return commonCount;
    }
    
}
