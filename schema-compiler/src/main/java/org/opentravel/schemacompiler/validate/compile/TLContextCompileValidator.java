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

import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLContextBaseValidator;
import org.opentravel.schemacompiler.validate.impl.IdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLContext</code> class.
 * 
 * @author S. Livezey
 */
public class TLContextCompileValidator extends TLContextBaseValidator {

    public static final String ERROR_INVALID_CONTEXT = "INVALID_CONTEXT";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLContext target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("contextId", target.getContextId()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank();

        builder.setProperty("applicationContext", target.getApplicationContext())
                .setFindingType(FindingType.ERROR).assertNotNullOrBlank();

        builder.setProperty("applicationContext", target.getOwningLibrary().getContexts())
                .setFindingType(FindingType.ERROR)
                .assertNoDuplicates(new IdentityResolver<TLContext>() {
                    public String getIdentity(TLContext entity) {
                        return (entity == null) ? null : entity.getApplicationContext();
                    }
                });

        builder.setProperty("contextId", target.getOwningLibrary().getContexts())
                .setFindingType(FindingType.ERROR)
                .assertNoDuplicates(new IdentityResolver<TLContext>() {
                    public String getIdentity(TLContext entity) {
                        return (entity == null) ? null : entity.getContextId();
                    }
                });

        return builder.getFindings();
    }

}
