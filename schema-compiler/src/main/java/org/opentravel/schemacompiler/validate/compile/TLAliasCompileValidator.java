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

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLAlias</code> class.
 * 
 * @author S. Livezey
 */
public class TLAliasCompileValidator extends TLValidatorBase<TLAlias> {

    public static final String ERROR_ILLEGAL_ALIAS_NAME = "ILLEGAL_ALIAS_NAME";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLAlias target) {
        TLValidationBuilder builder = newValidationBuilder(target);
        TLAliasOwner owner = target.getOwningEntity();

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);

        if (owner != null) {
            builder.setProperty("name", target.getOwningEntity().getAliases())
                    .setFindingType(FindingType.ERROR)
                    .assertNoDuplicates( e -> (e == null) ? null : ((TLAlias) e).getName() );

            // Add an error if the alias name is the same as that of its owner
            if ((target.getName() != null)
                    && target.getName().equals(target.getOwningEntity().getLocalName())) {
                builder.addFinding(FindingType.ERROR, "name", ERROR_ILLEGAL_ALIAS_NAME,
                        target.getName());
            }
        }

        checkSchemaNamingConflicts(target, builder);

        return builder.getFindings();
    }

}
