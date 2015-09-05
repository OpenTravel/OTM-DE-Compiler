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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.XSDFacetProfile;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLSimpleBaseValidator;
import org.opentravel.schemacompiler.validate.impl.CircularReferenceChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Validator for the <code>TLSimple</code> class.
 * 
 * @author S. Livezey
 */
public class TLSimpleCompileValidator extends TLSimpleBaseValidator {

    public static final String ERROR_INVALID_LIST_OF_LISTS = "INVALID_LIST_OF_LISTS";
    public static final String ERROR_INVALID_PATTERN = "INVALID_PATTERN";
    public static final String ERROR_INVALID_RESTRICTION = "INVALID_RESTRICTION";
    public static final String ERROR_RESTRICTION_NOT_APPLICABLE = "RESTRICTION_NOT_APPLICABLE";
    public static final String ERROR_INVALID_CIRCULAR_REFERENCE = "INVALID_CIRCULAR_REFERENCE";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLSimple target) {
        String minInclusive = trimString(target.getMinInclusive());
        String maxInclusive = trimString(target.getMaxInclusive());
        String minExclusive = trimString(target.getMinExclusive());
        String maxExclusive = trimString(target.getMaxExclusive());
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);

        builder.setEntityReferenceProperty("parentType", target.getParentType(),
                target.getParentTypeName()).setFindingType(FindingType.ERROR).assertNotNull()
                .setFindingType(FindingType.WARNING).assertNotDeprecated();
        
        // Validate allowable parent types (closed enumerations are allowed for list types
        if (!target.isListTypeInd()) {
        	builder.setFindingType(FindingType.ERROR).assertValidEntityReference(
        			TLSimple.class, XSDSimpleType.class);
        	
        } else {
        	builder.setFindingType(FindingType.ERROR).assertValidEntityReference(
        			TLSimple.class, XSDSimpleType.class, TLClosedEnumeration.class);
        }
        
        if (target.isListTypeInd() && isSimpleListType(target.getParentType())) {
            builder.addFinding(FindingType.ERROR, "listTypeInd", ERROR_INVALID_LIST_OF_LISTS);
        }

        if (!target.isListTypeInd()) {
            // Validate restriction parameters if the target is not a simple-list
            if (target.getMinLength() >= 0) {
                builder.setProperty("minLength", target.getMinLength())
                        .setFindingType(FindingType.ERROR).assertGreaterThanOrEqual(0);
            }

            if (target.getMaxLength() >= 0) {
                builder.setProperty("maxLength", target.getMaxLength())
                        .setFindingType(FindingType.ERROR)
                        .assertGreaterThanOrEqual(Math.max(0, target.getMinLength()));
            }

            if ((target.getPattern() != null) && (target.getPattern().length() > 0)) {
                try {
                    Pattern.compile(target.getPattern());

                } catch (PatternSyntaxException e) {
                    builder.addFinding(FindingType.ERROR, "pattern", ERROR_INVALID_PATTERN,
                            target.getPattern());
                }
            }

            if ((minInclusive != null) && (minExclusive != null)) {
                builder.addFinding(FindingType.ERROR, "minInclusive", ERROR_INVALID_RESTRICTION);
            }

            if ((maxInclusive != null) && (maxExclusive != null)) {
                builder.addFinding(FindingType.ERROR, "maxInclusive", ERROR_INVALID_RESTRICTION);
            }
            
            // Validate that any constraints are valid according to the facet profile
            XSDFacetProfile facetProfile = target.getXSDFacetProfile();
            
            if ((target.getMinLength() > 0) && !facetProfile.isMinLengthSupported()) {
                builder.addFinding(FindingType.ERROR, "constraintFacet", ERROR_RESTRICTION_NOT_APPLICABLE, "minLength");
            }
            if ((target.getMaxLength() > 0) && !facetProfile.isMaxLengthSupported()) {
                builder.addFinding(FindingType.ERROR, "constraintFacet", ERROR_RESTRICTION_NOT_APPLICABLE, "maxLength");
            }
            if ((target.getPattern() != null) && !facetProfile.isPatternSupported()) {
                builder.addFinding(FindingType.ERROR, "constraintFacet", ERROR_RESTRICTION_NOT_APPLICABLE, "pattern");
            }
            if ((target.getFractionDigits() > 0) && !facetProfile.isFractionDigitsSupported()) {
                builder.addFinding(FindingType.ERROR, "constraintFacet", ERROR_RESTRICTION_NOT_APPLICABLE, "fractionDigits");
            }
            if ((target.getTotalDigits() > 0) && !facetProfile.isTotalDigitsSupported()) {
                builder.addFinding(FindingType.ERROR, "constraintFacet", ERROR_RESTRICTION_NOT_APPLICABLE, "totalDigits");
            }
            if ((target.getMinInclusive() != null) && !facetProfile.isMinInclusiveSupported()) {
                builder.addFinding(FindingType.ERROR, "constraintFacet", ERROR_RESTRICTION_NOT_APPLICABLE, "minInclusive");
            }
            if ((target.getMaxInclusive() != null) && !facetProfile.isMaxInclusiveSupported()) {
                builder.addFinding(FindingType.ERROR, "constraintFacet", ERROR_RESTRICTION_NOT_APPLICABLE, "maxInclusive");
            }
            if ((target.getMinExclusive() != null) && !facetProfile.isMinExclusiveSupported()) {
                builder.addFinding(FindingType.ERROR, "constraintFacet", ERROR_RESTRICTION_NOT_APPLICABLE, "minExclusive");
            }
            if ((target.getMaxExclusive() != null) && !facetProfile.isMaxExclusiveSupported()) {
                builder.addFinding(FindingType.ERROR, "constraintFacet", ERROR_RESTRICTION_NOT_APPLICABLE, "maxExclusive");
            }

        } else {
            // Warn if restriction value(s) are provided for a simple-list type
            builder.setProperty("minLength", target.getMinLength())
                    .setFindingType(FindingType.WARNING).assertLessThanOrEqual(0);
            builder.setProperty("maxLength", target.getMaxLength())
                    .setFindingType(FindingType.WARNING).assertLessThanOrEqual(0);
            builder.setProperty("pattern", target.getPattern()).setFindingType(FindingType.WARNING)
                    .assertNullOrBlank();
            builder.setProperty("minInclusive", target.getMinInclusive())
                    .setFindingType(FindingType.WARNING).assertNullOrBlank();
            builder.setProperty("maxInclusive", target.getMaxInclusive())
                    .setFindingType(FindingType.WARNING).assertNullOrBlank();
            builder.setProperty("minExclusive", target.getMinExclusive())
                    .setFindingType(FindingType.WARNING).assertNullOrBlank();
            builder.setProperty("maxExclusive", target.getMaxExclusive())
                    .setFindingType(FindingType.WARNING).assertNullOrBlank();
            builder.setProperty("fractionDigits", target.getFractionDigits())
                    .setFindingType(FindingType.WARNING).assertLessThanOrEqual(0);
            builder.setProperty("totalDigits", target.getTotalDigits())
                    .setFindingType(FindingType.WARNING).assertLessThanOrEqual(0);
        }

        checkEmptyValueType(target, target.getParentType(), "parentType", builder);

        builder.setProperty("equivalents", target.getEquivalents())
                .setFindingType(FindingType.ERROR).assertNotNull().assertContainsNoNullElements();

        if (CircularReferenceChecker.hasCircularReference(target)) {
            builder.addFinding(FindingType.ERROR, "parentType", ERROR_INVALID_CIRCULAR_REFERENCE);
        }

        checkSchemaNamingConflicts(target, builder);
        validateVersioningRules(target, builder);

        return builder.getFindings();
    }

    /**
     * Returns true if the given entity represents a simple-list data type declaration.
     * 
     * @param parentType
     *            the parent type to analyze
     * @return boolean
     */
    private boolean isSimpleListType(TLAttributeType parentType) {
        boolean result = false;

        if (parentType instanceof TLSimple) {
            result = ((TLSimple) parentType).isListTypeInd();

        } else if (parentType instanceof XSDSimpleType) {
            TopLevelSimpleType jaxbType = ((XSDSimpleType) parentType).getJaxbType();
            result = (jaxbType != null) && (jaxbType.getList() != null);
        }
        return result;
    }

}
