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

import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLActionFacetBaseValidator;
import org.opentravel.schemacompiler.validate.impl.IdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLActionFacet</code> class.
 * 
 * @author S. Livezey
 */
public class TLActionFacetCompileValidator extends TLActionFacetBaseValidator{

    public static final String ERROR_NOT_ALLOWED_FOR_ABSTRACT = "NOT_ALLOWED_FOR_ABSTRACT";
    public static final String ERROR_INVALID_REFERENCE_TYPE   = "INVALID_REFERENCE_TYPE";
    public static final String ERROR_INVALID_FACET_REFERENCE  = "INVALID_FACET_REFERENCE";
    
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLActionFacet target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		String referenceFacetName = target.getReferenceFacetName();
		NamedEntity basePayload = target.getBasePayload();
		TLResource owner = target.getOwningResource();
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
				.assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);
		
        builder.setProperty("name", owner.getActionFacets())
        		.setFindingType(FindingType.ERROR)
        		.assertNoDuplicates(new IdentityResolver<TLActionFacet>() {
        			public String getIdentity(TLActionFacet entity) {
        				return entity.getName();
        			}
        		});
        
		builder.setProperty("referenceType", target.getReferenceType())
				.setFindingType(FindingType.ERROR)
				.assertNotNull();
        
		if ((target.getReferenceType() != null) && (owner != null) && owner.isAbstract()) {
			if ((referenceFacetName != null) && (referenceFacetName.length() > 0)) {
	        	builder.addFinding( FindingType.ERROR, "referenceFacetName", ERROR_NOT_ALLOWED_FOR_ABSTRACT );
			}
		}
		
		if (target.getReferenceType() == TLReferenceType.NONE) {
			builder.setProperty("referenceFacetName", target.getReferenceFacetName())
					.setFindingType(FindingType.WARNING)
					.assertNullOrBlank();
			builder.setProperty("referenceRepeat", target.getReferenceRepeat())
					.setFindingType(FindingType.WARNING)
					.assertLessThanOrEqual(1);
			
			if (target.getBasePayload() == null) {
	        	builder.addFinding( FindingType.ERROR, "referenceType", ERROR_INVALID_REFERENCE_TYPE );
			}
			
		} else if (target.getReferenceType() != null) {
			if (!owner.isAbstract() && (owner.getBusinessObjectRef() != null)
					&& (referenceFacetName != null)
					&& (ResourceCodegenUtils.getReferencedFacet(
							owner.getBusinessObjectRef(), referenceFacetName) == null)) {
	        	builder.addFinding( FindingType.ERROR, "referenceFacetName", ERROR_INVALID_FACET_REFERENCE,
	        			target.getReferenceFacetName(), owner.getBusinessObjectRef().getLocalName() );
			}
		}
		
		if (basePayload != null) {
			NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( target );
			
	        builder.setEntityReferenceProperty("basePayload", basePayload,
	                target.getBasePayloadName()).setFindingType(FindingType.ERROR)
	                .assertValidEntityReference(TLCoreObject.class, TLChoiceObject.class)
	                .setFindingType(FindingType.WARNING)
	                .assertNotNull().assertNotDeprecated().assertNotObsolete();
			
	        // If the payload type is not an existing core/choice object, make sure the
	        // action facet's name does not conflict with another entity
	        if (payloadType == target) {
	    		checkSchemaNamingConflicts(target, builder);
	        }
	        
		} else {
			String basePayloadName = target.getBasePayloadName();
			
			if ((basePayloadName != null) && !basePayloadName.equals("")) {
				builder.addFinding( FindingType.ERROR, "basePayload",
						TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE, basePayloadName );
			}
		}
		return builder.getFindings();
	}

}
