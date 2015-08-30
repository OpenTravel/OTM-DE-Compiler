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

import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLClosedEnumeration</code> class.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumerationBaseValidator extends TLValidatorBase<TLClosedEnumeration> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateChildren(TLClosedEnumeration target) {
        Validator<TLEnumValue> enumValueValidator = getValidatorFactory().getValidatorForClass(
                TLEnumValue.class);
        ValidationFindings findings = new ValidationFindings();

        for (TLEnumValue enumValue : target.getValues()) {
            findings.addAll(enumValueValidator.validate(enumValue));
        }

        if (target.getExtension() != null) {
            Validator<TLExtension> extensionValidator = getValidatorFactory().getValidatorForClass(
                    TLExtension.class);

            findings.addAll(extensionValidator.validate(target.getExtension()));
        }
        
        if (target.getDocumentation() != null) {
            Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(
                    TLDocumentation.class);

            findings.addAll(docValidator.validate(target.getDocumentation()));
        }
        return findings;
    }

}
