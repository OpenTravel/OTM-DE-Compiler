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

import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLClosedEnumerationBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLClosedEnumeration</code> class.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumerationCompileValidator extends TLClosedEnumerationBaseValidator {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLClosedEnumeration target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);

        builder.setProperty("values", target.getValues()).setFindingType(FindingType.ERROR)
                .assertNotNull().assertContainsNoNullElements().assertMinimumSize(1);

        checkSchemaNamingConflicts(target, builder);

        checkMajorVersionNamingConflicts(target, builder);

        return builder.getFindings();
    }

}
