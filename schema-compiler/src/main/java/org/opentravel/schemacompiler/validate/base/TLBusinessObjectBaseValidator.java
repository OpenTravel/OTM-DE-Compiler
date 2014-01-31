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

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLBusinessObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLBusinessObjectBaseValidator extends TLValidatorBase<TLBusinessObject> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateChildren(TLBusinessObject target) {
        Validator<TLAlias> aliasValidator = getValidatorFactory().getValidatorForClass(
                TLAlias.class);
        Validator<TLFacet> facetValidator = getValidatorFactory().getValidatorForClass(
                TLFacet.class);
        ValidationFindings findings = new ValidationFindings();

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
        if (target.getAliases() != null) {
            for (TLAlias alias : target.getAliases()) {
                findings.addAll(aliasValidator.validate(alias));
            }
        }
        if (target.getEquivalents() != null) {
            Validator<TLEquivalent> equivValidator = getValidatorFactory().getValidatorForClass(
                    TLEquivalent.class);

            for (TLEquivalent equiv : target.getEquivalents()) {
                findings.addAll(equivValidator.validate(equiv));
            }
        }
        if (target.getIdFacet() != null) {
            findings.addAll(facetValidator.validate(target.getIdFacet()));
        }
        if (target.getSummaryFacet() != null) {
            findings.addAll(facetValidator.validate(target.getSummaryFacet()));
        }
        if (target.getDetailFacet() != null) {
            findings.addAll(facetValidator.validate(target.getDetailFacet()));
        }
        for (TLFacet customFacet : target.getCustomFacets()) {
            findings.addAll(facetValidator.validate(customFacet));
        }
        for (TLFacet queryFacet : target.getQueryFacets()) {
            findings.addAll(facetValidator.validate(queryFacet));
        }
        return findings;
    }

}
