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

import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationBuilder;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLIndicatorBaseValidator;
import org.opentravel.schemacompiler.validate.impl.DuplicateFieldChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.UPAViolationChecker;

/**
 * Validator for the <code>TLIndicator</code> class.
 * 
 * @author S. Livezey
 */
public class TLIndicatorCompileValidator extends TLIndicatorBaseValidator {

    public static final String WARNING_ELEMENTS_NOT_ALLOWED = "ELEMENTS_NOT_ALLOWED";
    public static final String ERROR_UPA_VIOLATION = "UPA_VIOLATION";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLIndicator target) {
        TLValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( "name", target.getName() ).setFindingType( FindingType.ERROR ).assertNotNullOrBlank()
            .assertPatternMatch( NAME_XML_PATTERN );

        builder.setProperty( "equivalents", target.getEquivalents() ).setFindingType( FindingType.ERROR )
            .assertNotNull().assertContainsNoNullElements();

        // Check for duplicate names of this indicator
        if (target.getName() != null) {
            DuplicateFieldChecker dupChecker = getDuplicateFieldChecker( target );

            if (dupChecker.isDuplicateName( target )) {
                builder.addFinding( FindingType.ERROR, "name", ValidationBuilder.ERROR_DUPLICATE_ELEMENT,
                    target.getName() );
            }
        }

        if (target.isPublishAsElement()) {
            if (target.getOwner() instanceof TLValueWithAttributes) {
                builder.addFinding( FindingType.WARNING, "publishAsElement", WARNING_ELEMENTS_NOT_ALLOWED );

            } else {
                // Check for UPA violations
                if (target.getName() != null) {
                    UPAViolationChecker upaChecker = getUPAViolationChecker( target );

                    if (upaChecker.isUPAViolation( target )) {
                        builder.addFinding( FindingType.ERROR, "name", ERROR_UPA_VIOLATION, target.getName() );
                    }
                }
            }
        }

        return builder.getFindings();
    }

    /**
     * Returns a <code>DuplicateFieldChecker</code> that can be used to identify duplicate field names within the
     * elements of the declaring facet.
     * 
     * @param target the target indicator being validated
     * @return DuplicateFieldChecker
     */
    private DuplicateFieldChecker getDuplicateFieldChecker(TLIndicator target) {
        TLIndicatorOwner indicatorOwner = target.getOwner();
        String cacheKey = indicatorOwner.getNamespace() + ":" + indicatorOwner.getLocalName() + ":dupChecker";
        DuplicateFieldChecker checker = (DuplicateFieldChecker) getContextCacheEntry( cacheKey );

        if (checker == null) {
            checker = new DuplicateFieldChecker( indicatorOwner );
            setContextCacheEntry( cacheKey, checker );
        }
        return checker;
    }

    /**
     * Returns a <code>UPAViolationChecker</code> that can be used to identify UPA violations that occur with preceding
     * elements of the declaring facet.
     * 
     * @param target the target indicator being validated
     * @return UPAViolationChecker
     */
    private UPAViolationChecker getUPAViolationChecker(TLIndicator target) {
        TLIndicatorOwner indicatorOwner = target.getOwner();
        String cacheKey = indicatorOwner.getNamespace() + ":" + indicatorOwner.getLocalName() + ":upaChecker";
        UPAViolationChecker checker = (UPAViolationChecker) getContextCacheEntry( cacheKey );

        if (checker == null) {
            checker = new UPAViolationChecker( indicatorOwner );
            setContextCacheEntry( cacheKey, checker );
        }
        return checker;
    }

}
