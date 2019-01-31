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
    
    private static final String CONSTRAINT_FACET = "constraintFacet";
    private static final String PATTERN = "pattern";
    private static final String PARENT_TYPE = "parentType";
    private static final String MIN_LENGTH = "minLength";
    private static final String MAX_LENGTH = "maxLength";
    private static final String MIN_INCLUSIVE = "minInclusive";
    private static final String MAX_INCLUSIVE = "maxInclusive";
    
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
        TLValidationBuilder builder = newValidationBuilder( target );
        
        builder.setProperty( "name", target.getName() ).setFindingType( FindingType.ERROR ).assertNotNullOrBlank()
            .assertPatternMatch( NAME_XML_PATTERN );
        
        builder.setEntityReferenceProperty( PARENT_TYPE, target.getParentType(), target.getParentTypeName() )
            .setFindingType( FindingType.ERROR ).assertNotNull().setFindingType( FindingType.WARNING )
            .assertNotDeprecated().assertNotObsolete();
        
        // Validate allowable parent types (closed enumerations are allowed for list types
        if (!target.isListTypeInd()) {
            builder.setFindingType( FindingType.ERROR ).assertValidEntityReference( TLSimple.class,
                    XSDSimpleType.class );
            
        } else {
            builder.setFindingType( FindingType.ERROR ).assertValidEntityReference( TLSimple.class, XSDSimpleType.class,
                    TLClosedEnumeration.class );
        }
        
        if (target.isListTypeInd() && isSimpleListType( target.getParentType() )) {
            builder.addFinding( FindingType.ERROR, "listTypeInd", ERROR_INVALID_LIST_OF_LISTS );
        }
        
        if (!target.isListTypeInd()) {
            validateConstraints( target, builder );
            
        } else {
            // Warn if restriction value(s) are provided for a simple-list type
            builder.setProperty( MIN_LENGTH, target.getMinLength() ).setFindingType( FindingType.WARNING )
                .assertLessThanOrEqual( 0 );
            builder.setProperty( MAX_LENGTH, target.getMaxLength() ).setFindingType( FindingType.WARNING )
                .assertLessThanOrEqual( 0 );
            builder.setProperty( PATTERN, target.getPattern() ).setFindingType( FindingType.WARNING )
                .assertNullOrBlank();
            builder.setProperty( MIN_INCLUSIVE, target.getMinInclusive() ).setFindingType( FindingType.WARNING )
                .assertNullOrBlank();
            builder.setProperty( MAX_INCLUSIVE, target.getMaxInclusive() ).setFindingType( FindingType.WARNING )
                .assertNullOrBlank();
            builder.setProperty( "minExclusive", target.getMinExclusive() ).setFindingType( FindingType.WARNING )
                .assertNullOrBlank();
            builder.setProperty( "maxExclusive", target.getMaxExclusive() ).setFindingType( FindingType.WARNING )
                .assertNullOrBlank();
            builder.setProperty( "fractionDigits", target.getFractionDigits() ).setFindingType( FindingType.WARNING )
                .assertLessThanOrEqual( 0 );
            builder.setProperty( "totalDigits", target.getTotalDigits() ).setFindingType( FindingType.WARNING )
                .assertLessThanOrEqual( 0 );
        }
        
        checkEmptyValueType( target, target.getParentType(), PARENT_TYPE, builder );
        
        builder.setProperty( "equivalents", target.getEquivalents() ).setFindingType( FindingType.ERROR )
            .assertNotNull().assertContainsNoNullElements();
        
        if (CircularReferenceChecker.hasCircularReference( target )) {
            builder.addFinding( FindingType.ERROR, PARENT_TYPE, ERROR_INVALID_CIRCULAR_REFERENCE );
        }
        
        checkSchemaNamingConflicts( target, builder );
        validateVersioningRules( target, builder );
        
        return builder.getFindings();
    }
    
    /**
     * Validates the simple type constraint fields.
     * 
     * @param target the simple type being validated
     * @param builder validation builder where errors and warnings will be reported
     */
    private void validateConstraints(TLSimple target, TLValidationBuilder builder) {
        String minInclusive = trimString( target.getMinInclusive() );
        String maxInclusive = trimString( target.getMaxInclusive() );
        String minExclusive = trimString( target.getMinExclusive() );
        String maxExclusive = trimString( target.getMaxExclusive() );
        
        // Validate restriction parameters if the target is not a simple-list
        if (target.getMinLength() >= 0) {
            builder.setProperty( MIN_LENGTH, target.getMinLength() ).setFindingType( FindingType.ERROR )
                .assertGreaterThanOrEqual( 0 );
        }
        
        if (target.getMaxLength() >= 0) {
            builder.setProperty( MAX_LENGTH, target.getMaxLength() ).setFindingType( FindingType.ERROR )
                .assertGreaterThanOrEqual( Math.max( 0, target.getMinLength() ) );
        }
        
        if ((target.getPattern() != null) && (target.getPattern().length() > 0)) {
            try {
                Pattern.compile( target.getPattern() );
                
            } catch (PatternSyntaxException e) {
                builder.addFinding( FindingType.ERROR, PATTERN, ERROR_INVALID_PATTERN, target.getPattern() );
            }
        }
        
        if ((minInclusive != null) && (minExclusive != null)) {
            builder.addFinding( FindingType.ERROR, MIN_INCLUSIVE, ERROR_INVALID_RESTRICTION );
        }
        
        if ((maxInclusive != null) && (maxExclusive != null)) {
            builder.addFinding( FindingType.ERROR, MAX_INCLUSIVE, ERROR_INVALID_RESTRICTION );
        }
        
        validateFacetProfileConstraints( target, builder );
    }
    
    /**
     * Verify that all constraints are valid according to the facet profile.
     * 
     * @param target the simple type being validated
     * @param builder validation builder where errors and warnings will be reported
     */
    private void validateFacetProfileConstraints(TLSimple target, TLValidationBuilder builder) {
        XSDFacetProfile facetProfile = target.getXSDFacetProfile();
        
        if (notApplicable( target.getMinLength(), facetProfile.isMinLengthSupported() )) {
            builder.addFinding( FindingType.ERROR, CONSTRAINT_FACET, ERROR_RESTRICTION_NOT_APPLICABLE, MIN_LENGTH );
        }
        if (notApplicable( target.getMaxLength(), facetProfile.isMaxLengthSupported() )) {
            builder.addFinding( FindingType.ERROR, CONSTRAINT_FACET, ERROR_RESTRICTION_NOT_APPLICABLE, MAX_LENGTH );
        }
        if (notApplicable( target.getPattern(), facetProfile.isPatternSupported() )) {
            builder.addFinding( FindingType.ERROR, CONSTRAINT_FACET, ERROR_RESTRICTION_NOT_APPLICABLE, PATTERN );
        }
        if (notApplicable( target.getFractionDigits(), facetProfile.isFractionDigitsSupported() )) {
            builder.addFinding( FindingType.ERROR, CONSTRAINT_FACET, ERROR_RESTRICTION_NOT_APPLICABLE,
                    "fractionDigits" );
        }
        if (notApplicable( target.getTotalDigits(), facetProfile.isTotalDigitsSupported() )) {
            builder.addFinding( FindingType.ERROR, CONSTRAINT_FACET, ERROR_RESTRICTION_NOT_APPLICABLE, "totalDigits" );
        }
        if (notApplicable( target.getMinInclusive(), facetProfile.isMinInclusiveSupported() )) {
            builder.addFinding( FindingType.ERROR, CONSTRAINT_FACET, ERROR_RESTRICTION_NOT_APPLICABLE, MIN_INCLUSIVE );
        }
        if (notApplicable( target.getMaxInclusive(), facetProfile.isMaxInclusiveSupported() )) {
            builder.addFinding( FindingType.ERROR, CONSTRAINT_FACET, ERROR_RESTRICTION_NOT_APPLICABLE, MAX_INCLUSIVE );
        }
        if (notApplicable( target.getMinExclusive(), facetProfile.isMinExclusiveSupported() )) {
            builder.addFinding( FindingType.ERROR, CONSTRAINT_FACET, ERROR_RESTRICTION_NOT_APPLICABLE, "minExclusive" );
        }
        if (notApplicable( target.getMaxExclusive(), facetProfile.isMaxExclusiveSupported() )) {
            builder.addFinding( FindingType.ERROR, CONSTRAINT_FACET, ERROR_RESTRICTION_NOT_APPLICABLE, "maxExclusive" );
        }
    }
    
    /**
     * Returns true if the given constraint value is present, but the constraint is not applicable.
     * 
     * @param constraintValue the constraint value to evaluate
     * @param constraintSupported flag indicating if the constraint is supported
     * @return boolean
     */
    private boolean notApplicable(int constraintValue, boolean constraintSupported) {
        return !constraintSupported && (constraintValue > 0);
    }
    
    /**
     * Returns true if the given constraint value is present, but the constraint is not applicable.
     * 
     * @param constraintValue the constraint value to evaluate
     * @param constraintSupported flag indicating if the constraint is supported
     * @return boolean
     */
    private boolean notApplicable(String constraintValue, boolean constraintSupported) {
        return !constraintSupported && (constraintValue != null);
    }
    
    /**
     * Returns true if the given entity represents a simple-list data type declaration.
     * 
     * @param parentType the parent type to analyze
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
