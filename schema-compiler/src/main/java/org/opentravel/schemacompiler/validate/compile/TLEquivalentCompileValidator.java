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

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLEquivalent</code> class.
 * 
 * @author S. Livezey
 */
public class TLEquivalentCompileValidator extends TLValidatorBase<TLEquivalent> {

    private static final String CONTEXT = "context";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLEquivalent target) {
        TLValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( CONTEXT, target.getContext() ).setFindingType( FindingType.ERROR ).assertNotNullOrBlank();

        builder.setProperty( CONTEXT, target.getOwningEntity().getEquivalents() ).setFindingType( FindingType.ERROR )
            .assertNoDuplicates( e -> (e == null) ? null : ((TLEquivalent) e).getContext() );

        // Make sure that the context value is among the declared contexts for the owning library
        if ((target.getContext() != null) && (target.getContext().length() > 0)) {
            AbstractLibrary owningLibrary = target.getOwningLibrary();

            if (owningLibrary instanceof TLLibrary) {
                TLLibrary library = (TLLibrary) owningLibrary;

                if (library.getContext( target.getContext() ) == null) {
                    builder.addFinding( FindingType.ERROR, CONTEXT, TLContextCompileValidator.ERROR_INVALID_CONTEXT,
                        target.getContext() );
                }
            }
        }
        return builder.getFindings();
    }

}
