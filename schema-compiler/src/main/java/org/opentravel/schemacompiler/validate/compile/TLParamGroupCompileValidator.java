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

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLParamGroupBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLParamGroup</code> class.
 * 
 * @author S. Livezey
 */
public class TLParamGroupCompileValidator extends TLParamGroupBaseValidator{

    public static final String ERROR_INVALID_FACET_REF = "INVALID_FACET_REF";
    public static final String ERROR_INVALID_ID_GROUP  = "INVALID_ID_GROUP";
    
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLParamGroup target) {
        TLValidationBuilder builder = newValidationBuilder(target);
        TLFacet facetRef = target.getFacetRef();
        TLBusinessObject boRef;
        
        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
        		.assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);
        
        builder.setProperty("name", target.getOwner().getParamGroups())
        		.setFindingType(FindingType.ERROR)
        		.assertNoDuplicates( e -> ((TLParamGroup) e).getName() );
        
    	builder.setEntityReferenceProperty("facetRef", facetRef, target.getFacetRefName())
    			.setFindingType(FindingType.ERROR)
    			.assertNotNull();
        
    	if ((facetRef != null) && (target.getOwner() != null)
    			&& ((boRef = target.getOwner().getBusinessObjectRef()) != null)
    			&& !isDeclaredOrInheritedFacet(boRef, facetRef, boRef)) {
        	builder.addFinding( FindingType.ERROR, "facetRef", ERROR_INVALID_FACET_REF, boRef.getLocalName() );
    	}
    	
    	if (target.isIdGroup() && (facetRef != null) && (facetRef.getFacetType() == TLFacetType.QUERY)) {
        	builder.addFinding( FindingType.ERROR, "idGroup", ERROR_INVALID_ID_GROUP );
    	}
    	
        builder.setProperty("parameters", ResourceCodegenUtils.getInheritedParameters( target ))
        		.setFindingType(FindingType.ERROR)
				.assertMinimumSize( 1 );
        
        return builder.getFindings();
	}
	
	/**
	 * Returns true if the given facet reference is declared or inherited by the
	 * business object provided.
	 * 
	 * @param facetOwner  the candidate facet owner to check for the 'facetRef'
	 * @param facetRef  the facet reference of the parameter group being validated
	 * @param boRef  the business object reference from the owning resource
	 * @return boolean
	 */
	private boolean isDeclaredOrInheritedFacet(TLFacetOwner facetOwner, TLFacet facetRef, TLBusinessObject boRef) {
		boolean isValid = false;
		
		while (!isValid && (facetOwner != null)) {
			List<TLFacet> boFacets = FacetCodegenUtils.getAllFacetsOfType( facetOwner, facetRef.getFacetType() );
			
			for (TLFacet boFacet : boFacets) {
				if (facetRef == boFacet) {
					isValid = true;
					
				} else if (boFacet instanceof TLContextualFacet){
					isValid = isDeclaredOrInheritedFacet( (TLContextualFacet) boFacet, facetRef, boRef );
				}
				if (isValid) break;
			}
			if (!isValid) {
				TLFacetOwner ownerExtension = FacetCodegenUtils.getFacetOwnerExtension( facetOwner );
				
				if (ownerExtension instanceof TLBusinessObject) {
					facetOwner = ownerExtension;
				} else {
					facetOwner = null;
				}
			}
		}
		return isValid;
	}
	
}
