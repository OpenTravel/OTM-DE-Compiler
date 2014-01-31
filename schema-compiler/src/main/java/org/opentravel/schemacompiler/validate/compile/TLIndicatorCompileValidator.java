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

import java.util.List;

import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLIndicatorBaseValidator;
import org.opentravel.schemacompiler.validate.impl.FacetMemberIdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;

/**
 * Validator for the <code>TLIndicator</code> class.
 * 
 * @author S. Livezey
 */
public class TLIndicatorCompileValidator extends TLIndicatorBaseValidator {

    public static final String WARNING_ELEMENTS_NOT_ALLOWED = "ELEMENTS_NOT_ALLOWED";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLIndicator target) {
        TLValidationBuilder dupBuilder = newValidationBuilder(target);
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);

        builder.setProperty("equivalents", target.getEquivalents())
                .setFindingType(FindingType.ERROR).assertNotNull().assertContainsNoNullElements();

        if (target.isPublishAsElement() && (target.getOwner() instanceof TLValueWithAttributes)) {
            builder.addFinding(FindingType.WARNING, "publishAsElement",
                    WARNING_ELEMENTS_NOT_ALLOWED);
        }

        // Check for duplicate names of this attribute
        dupBuilder.setProperty("name", getMembersOfOwner(target)).setFindingType(FindingType.ERROR)
                .assertNoDuplicates(new FacetMemberIdentityResolver());

        if (dupBuilder.isEmpty() && (target.getOwner() instanceof TLFacet)) {
            dupBuilder.setProperty("name-upa", getInheritedMembersOfOwner(target))
                    .setFindingType(FindingType.ERROR)
                    .assertNoDuplicates(new FacetMemberIdentityResolver());
        }

        builder.addFindings(dupBuilder.getFindings());
        return builder.getFindings();
    }

    /**
     * Returns the list of attributes, properties, and indicators defined by the given indicator's
     * owner.
     * 
     * @param target
     *            the target indicator being validated
     * @return List<TLModelElement>
     */
    @SuppressWarnings("unchecked")
    private List<TLModelElement> getMembersOfOwner(TLIndicator target) {
        TLIndicatorOwner indicatorOwner = target.getOwner();
        String cacheKey = indicatorOwner.getNamespace() + ":" + indicatorOwner.getLocalName()
                + ":members";
        List<TLModelElement> members = (List<TLModelElement>) getContextCacheEntry(cacheKey);

        if (members == null) {
            if (indicatorOwner instanceof TLValueWithAttributes) {
                members = ValidatorUtils.getMembers((TLValueWithAttributes) indicatorOwner);

            } else if (indicatorOwner instanceof TLExtensionPointFacet) {
                members = ValidatorUtils.getMembers((TLExtensionPointFacet) indicatorOwner);

            } else { // TLFacet
                members = ValidatorUtils.getMembers((TLFacet) indicatorOwner);
            }
            setContextCacheEntry(cacheKey, members);
        }
        return members;
    }

    /**
     * Returns the list of inherited attributes, properties, and indicators defined by the given
     * attribute' owner.
     * 
     * @param target
     *            the target attribute being validated
     * @return List<TLModelElement>
     */
    @SuppressWarnings("unchecked")
    private List<TLModelElement> getInheritedMembersOfOwner(TLIndicator target) {
        TLIndicatorOwner indicatorOwner = target.getOwner();
        String cacheKey = indicatorOwner.getNamespace() + ":" + indicatorOwner.getLocalName()
                + ":inheritedMembers";
        List<TLModelElement> members = (List<TLModelElement>) getContextCacheEntry(cacheKey);

        if (members == null) {
            members = ValidatorUtils.getInheritedMembers((TLFacet) indicatorOwner);
            setContextCacheEntry(cacheKey, members);
        }
        return members;
    }

}
