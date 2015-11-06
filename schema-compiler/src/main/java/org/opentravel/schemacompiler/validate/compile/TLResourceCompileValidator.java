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

    public static final String ERROR_INVALID_BASE_PATH = "INVALID_BASE_PATH";
    
	private static ResourceUrlValidator urlValidator = new ResourceUrlValidator();
	
    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLResource target) {
        TLValidationBuilder builder = newValidationBuilder(target);
        
        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
        		.assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);
        
        if (target.isAbstract()) {
        	builder.setProperty("basePath", target.getBasePath()).setFindingType(FindingType.ERROR)
            		.assertNullOrBlank();
        	
        } else {
        	String basePath = target.getBasePath();
        	
        	builder.setProperty("basePath", basePath).setFindingType(FindingType.ERROR)
        			.assertNotNullOrBlank();
        	
        	if ((basePath != null) &&
        			!(urlValidator.isValid( basePath ) || urlValidator.isValidPath( basePath ))) {
            	builder.addFinding( FindingType.ERROR, "basePath", ERROR_INVALID_BASE_PATH, basePath );
        	}
        }
        		
        checkSchemaNamingConflicts(target, builder);
        validateVersioningRules(target, builder);
        
        return builder.getFindings();
    }

}
