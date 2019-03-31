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

package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

import java.util.List;

/**
 * Validator for the <code>TLDocumentation</code> class.
 * 
 * @author S. Livezey
 */
public class TLDocumentationSaveValidator extends TLValidatorBase<TLDocumentation> {

    private static final int MAX_DESCRIPTION_LENGTH = 10000;

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLDocumentation target) {
        TLValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( "DESCRIPTION", target.getDescription() ).setFindingType( FindingType.WARNING )
            .assertMaximumLength( MAX_DESCRIPTION_LENGTH );

        builder.setProperty( "deprecations", target.getDeprecations() ).setFindingType( FindingType.WARNING )
            .assertContainsNoNullElements();

        builder.setProperty( "references", target.getReferences() ).setFindingType( FindingType.WARNING )
            .assertContainsNoNullElements();

        builder.setProperty( "implementers", target.getImplementers() ).setFindingType( FindingType.WARNING )
            .assertContainsNoNullElements();

        builder.setProperty( "moreInfos", target.getMoreInfos() ).setFindingType( FindingType.WARNING )
            .assertContainsNoNullElements();

        builder.setProperty( "otherDocs", target.getOtherDocs() ).setFindingType( FindingType.WARNING )
            .assertContainsNoNullElements();

        validateMaxLength( target.getDeprecations(), "deprecations.text", builder );
        validateMaxLength( target.getImplementers(), "implementers.text", builder );
        validateMaxLength( target.getOtherDocs(), "otherDocs.text", builder );

        for (TLAdditionalDocumentationItem otherDoc : target.getOtherDocs()) {
            if (otherDoc != null) {
                builder.setProperty( "otherDocs.context", otherDoc.getContext() ).setFindingType( FindingType.WARNING )
                    .assertNotNullOrBlank();
            }
        }
        return builder.getFindings();
    }

    /**
     * Performs validation checks to ensure all of the documentation values are less than or equal to the maximum
     * allowable length.
     * 
     * @param docItems the documentation items to validate
     * @param propertyName the name of the property being validated
     * @param builder the validation builder
     */
    private void validateMaxLength(List<? extends TLDocumentationItem> docItems, String propertyName,
        TLValidationBuilder builder) {
        boolean exceedsMaxLength = false;
        int actualLength = 0;

        for (TLDocumentationItem docItem : docItems) {
            if ((docItem.getText() != null) && (docItem.getText().length() > MAX_DESCRIPTION_LENGTH)) {
                exceedsMaxLength = true;
                actualLength = docItem.getText().length();
                break;
            }
        }
        if (exceedsMaxLength) {
            builder.addFinding( FindingType.WARNING, propertyName, TLValidationBuilder.ERROR_EXCEEDS_MAXIMUM_LENGTH,
                actualLength, MAX_DESCRIPTION_LENGTH );
        }
    }

}
