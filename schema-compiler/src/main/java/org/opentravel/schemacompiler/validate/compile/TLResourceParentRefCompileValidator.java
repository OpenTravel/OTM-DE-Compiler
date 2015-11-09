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

import java.util.List;

import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLResourceParentRefBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLResourceParentRef</code> class.
 * 
 * @author S. Livezey
 */
public class TLResourceParentRefCompileValidator extends TLResourceParentRefBaseValidator {

    public static final String ERROR_INVALID_ABSTRACT_PARENT = "INVALID_ABSTRACT_PARENT";
    public static final String ERROR_INVALID_PARAM_GROUP     = "INVALID_PARAM_GROUP";
    public static final String ERROR_ID_PARAM_GROUP_REQUIRED = "ID_PARAM_GROUP_REQUIRED";
    
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLResourceParentRef target) {
        TLValidationBuilder builder = newValidationBuilder(target);
        TLResource parentResource = target.getParentResource();
        TLParamGroup parentParamGroup = target.getParentParamGroup();
        
    	builder.setEntityReferenceProperty("parentResource", parentResource,
    			target.getParentResourceName()).setFindingType(FindingType.ERROR)
				.assertNotNull();
    	
    	if ((parentResource != null) && parentResource.isAbstract()) {
        	builder.addFinding( FindingType.ERROR, "parentResource", ERROR_INVALID_ABSTRACT_PARENT );
    	}
    	
    	if (parentParamGroup == null) {
    		String paramGroupName = target.getParentParamGroupName();
    		
            if ((paramGroupName == null) || paramGroupName.equals("")) {
                builder.addFinding( FindingType.ERROR, "parentParamGroup",
                		TLValidationBuilder.MISSING_NAMED_ENTITY_REFERENCE );
            } else {
            	builder.addFinding(FindingType.ERROR, "parentParamGroup",
            			TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE, paramGroupName );
            }
    	} else if (parentResource != null) {
    		List<TLParamGroup> inheritedParamGroup = ResourceCodegenUtils.getInheritedParamGroups( parentResource );
    		
    		if (!inheritedParamGroup.contains( parentParamGroup )) {
            	builder.addFinding( FindingType.ERROR, "parentParamGroup", ERROR_INVALID_PARAM_GROUP,
            			parentParamGroup.getName() );
    		}
    		if (!parentParamGroup.isIdGroup()) {
            	builder.addFinding( FindingType.ERROR, "parentParamGroup", ERROR_ID_PARAM_GROUP_REQUIRED,
            			parentParamGroup.getName() );
    		}
    	}
    	
    	builder.setProperty("pathTemplate", target.getPathTemplate()).setFindingType(FindingType.ERROR)
    			.assertNotNullOrBlank();
    	validatePathTemplate( target.getPathTemplate(), parentParamGroup, builder );
    	
    	return builder.getFindings();
	}
	
}
