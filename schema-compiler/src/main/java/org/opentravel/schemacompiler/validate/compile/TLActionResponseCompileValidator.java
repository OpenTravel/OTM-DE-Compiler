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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLActionResponseBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validator for the <code>TLActionResponse</code> class.
 * 
 * @author S. Livezey
 */
public class TLActionResponseCompileValidator extends TLActionResponseBaseValidator {

    private static final String PAYLOAD_TYPE = "payloadType";
    private static final String MIME_TYPES = "mimeTypes";

    public static final String ERROR_INVALID_STATUS_CODES = "INVALID_STATUS_CODES";
    public static final String ERROR_INVALID_ACTION_FACET_REF = "INVALID_ACTION_FACET_REF";
    public static final String ERROR_INVALID_RESPONSE_PAYLOAD = "INVALID_RESPONSE_PAYLOAD";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLActionResponse target) {
        TLResource owningResource = (target.getOwner() == null) ? null : target.getOwner().getOwner();
        List<Integer> invalidStatusCodes = getInvalidStatusCodes( target );
        TLValidationBuilder builder = newValidationBuilder( target );

        if (!invalidStatusCodes.isEmpty()) {
            builder.addFinding( FindingType.ERROR, "statusCodes", ERROR_INVALID_STATUS_CODES,
                toCsvString( invalidStatusCodes ) );
        }

        if (target.getPayloadType() != null) {
            NamedEntity payloadType = target.getPayloadType();

            if (payloadType instanceof TLActionFacet) {
                if ((owningResource != null)
                    && !isDeclaredOrInheritedFacet( owningResource, (TLActionFacet) payloadType )) {
                    builder.addFinding( FindingType.ERROR, PAYLOAD_TYPE, ERROR_INVALID_ACTION_FACET_REF,
                        payloadType.getLocalName() );
                }
            } else if (!isValidResponsePayload( payloadType )) {
                builder.addFinding( FindingType.ERROR, PAYLOAD_TYPE, ERROR_INVALID_RESPONSE_PAYLOAD,
                    payloadType.getLocalName() );
            }
            builder.setProperty( MIME_TYPES, target.getMimeTypes() ).setFindingType( FindingType.ERROR )
                .assertMinimumSize( 1 );

        } else {
            String payloadTypeName = target.getPayloadTypeName();

            if ((payloadTypeName != null) && (payloadTypeName.length() > 0)) {
                builder.addFinding( FindingType.ERROR, PAYLOAD_TYPE,
                    TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE, payloadTypeName );
                builder.setProperty( MIME_TYPES, target.getMimeTypes() ).setFindingType( FindingType.ERROR )
                    .assertMinimumSize( 1 );
            } else {
                builder.setProperty( MIME_TYPES, target.getMimeTypes() ).setFindingType( FindingType.WARNING )
                    .assertMaximumSize( 0 );
            }
        }

        return builder.getFindings();
    }

    /**
     * Returns a list of invalid status codes from the given response.
     * 
     * @param target the action response being validated
     * @return List&lt;Integer&gt;
     */
    private List<Integer> getInvalidStatusCodes(TLActionResponse target) {
        List<Integer> invalidCodes = new ArrayList<>();

        for (Integer statusCode : target.getStatusCodes()) {
            if ((statusCode < 100) || (statusCode > 599)) {
                invalidCodes.add( statusCode );
            }
        }
        Collections.sort( invalidCodes );
        return invalidCodes;
    }

    /**
     * Returns true if the given entity is a valid reference to a core object, one of its aliases, or one of its
     * non-simple facets.
     * 
     * @param payloadType the payload type to validate
     * @return boolean
     */
    private boolean isValidResponsePayload(NamedEntity payloadType) {
        if (payloadType instanceof TLAlias) {
            payloadType = ((TLAlias) payloadType).getOwningEntity();
        }
        if (payloadType instanceof TLFacet) {
            payloadType = ((TLFacet) payloadType).getOwningEntity();
        }
        return (payloadType instanceof TLCoreObject) || (payloadType instanceof TLChoiceObject);
    }

}
