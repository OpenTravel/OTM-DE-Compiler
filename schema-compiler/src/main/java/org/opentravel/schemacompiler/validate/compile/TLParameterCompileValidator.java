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

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLParameterBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLParameter</code> class.
 * 
 * @author S. Livezey
 */
public class TLParameterCompileValidator extends TLParameterBaseValidator{

    public static final String ERROR_INVALID_FIELD_REF     = "INVALID_FIELD_REF";
    public static final String ERROR_INELIGIBLE_FIELD_REF  = "INELIGIBLE_FIELD_REF";
    
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLParameter target) {
        TLValidationBuilder builder = newValidationBuilder(target);
        TLMemberField<?> fieldRef = target.getFieldRef();
        
    	if (fieldRef == null) {
    		String fieldRefName = target.getFieldRefName();
    		
            if ((fieldRefName == null) || fieldRefName.equals("")) {
                builder.addFinding( FindingType.ERROR, "fieldRef",
                		TLValidationBuilder.MISSING_NAMED_ENTITY_REFERENCE );
            } else {
            	builder.addFinding(FindingType.ERROR, "fieldRef",
            			TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE, fieldRefName );
            }
            
    	} else {
    		TLFacet facetRef;
    		
    		if ((target.getOwner() != null) && ((facetRef = target.getOwner().getFacetRef()) != null)) {
    			List<TLMemberField<?>> eligibleFields = ResourceCodegenUtils.getEligibleParameterFields( facetRef );
    			
    			if (!isFacetField( fieldRef, facetRef )) {
    	        	builder.addFinding( FindingType.ERROR, "fieldRef", ERROR_INVALID_FIELD_REF );
    			}
    			if (!eligibleFields.contains( fieldRef )) {
    	        	builder.addFinding( FindingType.ERROR, "fieldRef", ERROR_INELIGIBLE_FIELD_REF );
    			}
    		}
    	}
    	
        return builder.getFindings();
	}
	
	/**
	 * Returns true if the given field is declared or inherited by the facet provided.
	 * 
	 * @param fieldRef  the field reference of the parameter being validated
	 * @param facetRef  the facet referenced by the owning parameter group
	 * @return boolean
	 */
	private boolean isFacetField(TLMemberField<?> fieldRef, TLFacet facetRef) {
		boolean isValid = false;
		
		for (TLAttribute field : PropertyCodegenUtils.getInheritedAttributes( facetRef )) {
			if (isValid = (fieldRef == field)) {
				break;
			}
		}
		if (!isValid) {
			for (TLProperty field : PropertyCodegenUtils.getInheritedProperties( facetRef )) {
				if (isValid = (fieldRef == field)) {
					break;
				}
			}
		}
		if (!isValid) {
			for (TLIndicator field : PropertyCodegenUtils.getInheritedIndicators( facetRef )) {
				if (isValid = (fieldRef == field)) {
					break;
				}
			}
		}
		return isValid;
	}
	
}
