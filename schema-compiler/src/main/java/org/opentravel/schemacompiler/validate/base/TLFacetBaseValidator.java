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

import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLFacet</code> class.
 * 
 * @author S. Livezey
 */
public class TLFacetBaseValidator extends TLValidatorBase<TLFacet> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateChildren(TLFacet target) {
        Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(
                TLDocumentation.class);
        Validator<TLAttribute> attributeValidator = getValidatorFactory().getValidatorForClass(
                TLAttribute.class);
        Validator<TLProperty> elementValidator = getValidatorFactory().getValidatorForClass(
                TLProperty.class);
        Validator<TLIndicator> indicatorValidator = getValidatorFactory().getValidatorForClass(
                TLIndicator.class);
        ValidationFindings findings = new ValidationFindings();

        if (target.getDocumentation() != null) {
            findings.addAll(docValidator.validate(target.getDocumentation()));
        }
        if (target.getAttributes() != null) {
            for (TLAttribute attribute : target.getAttributes()) {
                if (attribute != null) {
                    findings.addAll(attributeValidator.validate(attribute));
                }
            }
        }
        if (target.getElements() != null) {
            for (TLProperty element : target.getElements()) {
                if (element != null) {
                    findings.addAll(elementValidator.validate(element));
                }
            }
        }
        if (target.getIndicators() != null) {
            for (TLIndicator indicator : target.getIndicators()) {
                if (indicator != null) {
                    findings.addAll(indicatorValidator.validate(indicator));
                }
            }
        }
        return findings;
    }

}
