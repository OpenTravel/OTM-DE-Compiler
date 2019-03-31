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

package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLAction</code> class.
 * 
 * @author S. Livezey
 */
public class TLActionBaseValidator extends TLValidatorBase<TLAction> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateChildren(TLAction target) {
        Validator<TLActionResponse> responseValidator =
            getValidatorFactory().getValidatorForClass( TLActionResponse.class );
        ValidationFindings findings = new ValidationFindings();

        if (target.getDocumentation() != null) {
            Validator<TLDocumentation> docValidator =
                getValidatorFactory().getValidatorForClass( TLDocumentation.class );

            findings.addAll( docValidator.validate( target.getDocumentation() ) );
        }

        if (target.getRequest() != null) {
            Validator<TLActionRequest> requestValidator =
                getValidatorFactory().getValidatorForClass( TLActionRequest.class );

            findings.addAll( requestValidator.validate( target.getRequest() ) );
        }
        for (TLActionResponse response : target.getResponses()) {
            findings.addAll( responseValidator.validate( response ) );
        }
        return findings;
    }

}
