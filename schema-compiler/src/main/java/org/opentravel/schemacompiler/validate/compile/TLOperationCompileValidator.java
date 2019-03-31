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

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.OperationType;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLOperationBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLOperation</code> class.
 * 
 * @author S. Livezey
 */
public class TLOperationCompileValidator extends TLOperationBaseValidator {

    public static final String ERROR_INVALID_OPERATION = "INVALID_OPERATION";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLOperation target) {
        TLValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( "name", target.getName() ).setFindingType( FindingType.ERROR ).assertNotNullOrBlank()
            .assertPatternMatch( NAME_XML_PATTERN );

        builder.setProperty( "equivalents", target.getEquivalents() ).setFindingType( FindingType.ERROR )
            .assertNotNull().assertContainsNoNullElements();

        builder.setProperty( "name", target.getOwningService().getOperations() ).setFindingType( FindingType.ERROR )
            .assertNoDuplicates( e -> (e == null) ? null : ((TLOperation) e).getName() );

        if (FacetCodegenUtils.getOperationType( target ) == OperationType.INVALID) {
            builder.addFinding( FindingType.ERROR, "operationType", ERROR_INVALID_OPERATION );
        }

        validateVersioningRules( target, builder );

        return builder.getFindings();
    }

}
