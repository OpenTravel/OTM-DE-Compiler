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
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLValueWithAttributes</code> class.
 * 
 * @author S. Livezey
 */
public class TLValueWithAttributesBaseValidator extends TLValidatorBase<TLValueWithAttributes> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateChildren(TLValueWithAttributes target) {
        Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass( TLDocumentation.class );
        Validator<TLAttribute> attributeValidator = getValidatorFactory().getValidatorForClass( TLAttribute.class );
        Validator<TLIndicator> indicatorValidator = getValidatorFactory().getValidatorForClass( TLIndicator.class );
        Validator<TLEquivalent> equivValidator = getValidatorFactory().getValidatorForClass( TLEquivalent.class );
        Validator<TLExample> exampleValidator = getValidatorFactory().getValidatorForClass( TLExample.class );
        ValidationFindings findings = new ValidationFindings();

        if (target.getDocumentation() != null) {
            findings.addAll( docValidator.validate( target.getDocumentation() ) );
        }
        if (target.getValueDocumentation() != null) {
            findings.addAll( docValidator.validate( target.getValueDocumentation() ) );
        }

        for (TLAttribute attribute : target.getAttributes()) {
            findings.addAll( attributeValidator.validate( attribute ) );
        }
        for (TLIndicator indicators : target.getIndicators()) {
            findings.addAll( indicatorValidator.validate( indicators ) );
        }
        for (TLEquivalent equiv : target.getEquivalents()) {
            findings.addAll( equivValidator.validate( equiv ) );
        }

        for (TLExample example : target.getExamples()) {
            findings.addAll( exampleValidator.validate( example ) );
        }
        return findings;
    }

}
