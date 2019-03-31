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

package org.opentravel.schemacompiler.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration that provides detailed information about which XML schema facets are applicable to
 * <code>TLAttributeType</code> entities in the resulting schemas produced from an OTM model.
 * 
 * @author S. Livezey
 */
public enum XSDFacetProfile {

    FP_BOOLEAN(false, false, false, true, false, true, false, false, false, false, false, false),
    FP_INT(false, false, false, true, true, true, true, true, true, true, true, true),
    FP_DECIMAL(false, false, false, true, true, true, true, true, true, true, true, true),
    FP_FLOAT(false, false, false, true, true, true, false, false, true, true, true, true),
    FP_STRING(true, true, true, true, true, true, false, false, false, false, false, false),
    FP_DATE(false, false, false, true, true, true, false, false, true, true, true, true),
    FP_DATETIME(false, false, false, true, true, true, false, false, true, true, true, true),
    FP_TIME(false, false, false, true, true, true, false, false, true, true, true, true),
    FP_DURATION(false, false, false, true, true, true, false, false, true, true, true, true),
    FP_LANGUAGE(false, false, false, false, false, false, false, false, false, false, false, false),
    FP_UNKNOWN(false, false, false, false, false, false, false, false, false, false, false, false);

    private static final Map<String,XSDFacetProfile> w3cProfileMap = new HashMap<>();

    private boolean lengthSupported;
    private boolean minLengthSupported;
    private boolean maxLengthSupported;
    private boolean patternSupported;
    private boolean enumerationSupported;
    private boolean whiteSpaceSupported;
    private boolean totalDigitsSupported;
    private boolean fractionDigitsSupported;
    private boolean minInclusiveSupported;
    private boolean maxInclusiveSupported;
    private boolean minExclusiveSupported;
    private boolean maxExclusiveSupported;

    /**
     * Returns the <code>XSDFacetProfile</code> that is appropriate to the XML schema type provided.
     * 
     * @param xmlSchemaType the XML schema type (must be defined in the XML schema namespace)
     * @return XSDFacetProfile
     */
    public static XSDFacetProfile toFacetProfile(String xmlSchemaType) {
        XSDFacetProfile result = w3cProfileMap.get( xmlSchemaType );
        return (result != null) ? result : FP_UNKNOWN;
    }

    /**
     * Full constructor that initializes all of the supported facet boolean values.
     * 
     * @param length value of the 'lengthSupported' property
     * @param minLength value of the 'minLengthSupported' property
     * @param maxLength value of the 'maxLengthSupported' property
     * @param pattern value of the 'patternSupported' property
     * @param enumeration value of the 'enumerationSupported' property
     * @param whiteSpace value of the 'whiteSpaceSupported' property
     * @param totalDigits value of the 'totalDigitsSupported' property
     * @param fractionDigits value of the 'fractionDigitsSupported' property
     * @param minInclusive value of the 'minInclusiveSupported' property
     * @param maxInclusive value of the 'maxInclusiveSupported' property
     * @param minExclusive value of the 'minExclusiveSupported' property
     * @param maxExclusive value of the 'maxExclusiveSupported' property
     */
    @SuppressWarnings("squid:S00107") // Number of params unavoidable since enum constructors are private
    private XSDFacetProfile(boolean length, boolean minLength, boolean maxLength, boolean pattern, boolean enumeration,
        boolean whiteSpace, boolean totalDigits, boolean fractionDigits, boolean minInclusive, boolean maxInclusive,
        boolean minExclusive, boolean maxExclusive) {
        this.lengthSupported = length;
        this.minLengthSupported = minLength;
        this.maxLengthSupported = maxLength;
        this.patternSupported = pattern;
        this.enumerationSupported = enumeration;
        this.whiteSpaceSupported = whiteSpace;
        this.totalDigitsSupported = totalDigits;
        this.fractionDigitsSupported = fractionDigits;
        this.minInclusiveSupported = minInclusive;
        this.maxInclusiveSupported = maxInclusive;
        this.minExclusiveSupported = minExclusive;
        this.maxExclusiveSupported = maxExclusive;
    }

    /**
     * Indicates whether the 'length' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isLengthSupported() {
        return lengthSupported;
    }

    /**
     * Indicates whether the 'minLength' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isMinLengthSupported() {
        return minLengthSupported;
    }

    /**
     * Indicates whether the 'maxLength' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isMaxLengthSupported() {
        return maxLengthSupported;
    }

    /**
     * Indicates whether the 'pattern' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isPatternSupported() {
        return patternSupported;
    }

    /**
     * Indicates whether the 'enumeration' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isEnumerationSupported() {
        return enumerationSupported;
    }

    /**
     * Indicates whether the 'whiteSpace' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isWhiteSpaceSupported() {
        return whiteSpaceSupported;
    }

    /**
     * Indicates whether the 'totalDigits' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isTotalDigitsSupported() {
        return totalDigitsSupported;
    }

    /**
     * Indicates whether the 'fractionDigits' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isFractionDigitsSupported() {
        return fractionDigitsSupported;
    }

    /**
     * Indicates whether the 'minInclusive' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isMinInclusiveSupported() {
        return minInclusiveSupported;
    }

    /**
     * Indicates whether the 'maxInclusive' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isMaxInclusiveSupported() {
        return maxInclusiveSupported;
    }

    /**
     * Indicates whether the 'minExclusive' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isMinExclusiveSupported() {
        return minExclusiveSupported;
    }

    /**
     * Indicates whether the 'maxExclusive' XML facet is supported by this profile.
     * 
     * @return boolean
     */
    public boolean isMaxExclusiveSupported() {
        return maxExclusiveSupported;
    }

    /**
     * Initializes the map of W3C type names to facet profiles.
     */
    static {
        try {
            w3cProfileMap.put( "boolean", FP_BOOLEAN );
            w3cProfileMap.put( "byte", FP_INT );
            w3cProfileMap.put( "short", FP_INT );
            w3cProfileMap.put( "int", FP_INT );
            w3cProfileMap.put( "integer", FP_INT );
            w3cProfileMap.put( "long", FP_INT );
            w3cProfileMap.put( "positiveInteger", FP_INT );
            w3cProfileMap.put( "nonPositiveInteger", FP_INT );
            w3cProfileMap.put( "negativeInteger", FP_INT );
            w3cProfileMap.put( "nonNegativeInteger", FP_INT );
            w3cProfileMap.put( "unsignedByte", FP_INT );
            w3cProfileMap.put( "unsignedShort", FP_INT );
            w3cProfileMap.put( "unsignedInt", FP_INT );
            w3cProfileMap.put( "unsignedLong", FP_INT );
            w3cProfileMap.put( "decimal", FP_DECIMAL );
            w3cProfileMap.put( "float", FP_FLOAT );
            w3cProfileMap.put( "double", FP_FLOAT );
            w3cProfileMap.put( "string", FP_STRING );
            w3cProfileMap.put( "normalizedString", FP_STRING );
            w3cProfileMap.put( "token", FP_STRING );
            w3cProfileMap.put( "Name", FP_STRING );
            w3cProfileMap.put( "NCName", FP_STRING );
            w3cProfileMap.put( "NOTATION", FP_STRING );
            w3cProfileMap.put( "NMTOKEN", FP_STRING );
            w3cProfileMap.put( "NMTOKENS", FP_STRING );
            w3cProfileMap.put( "ENTITY", FP_STRING );
            w3cProfileMap.put( "ENTITIES", FP_STRING );
            w3cProfileMap.put( "date", FP_DATE );
            w3cProfileMap.put( "dateTime", FP_DATETIME );
            w3cProfileMap.put( "time", FP_TIME );
            w3cProfileMap.put( "duration", FP_DURATION );
            w3cProfileMap.put( "gYear", FP_DATE );
            w3cProfileMap.put( "gYearMonth", FP_DATE );
            w3cProfileMap.put( "gMonthDay", FP_DATE );
            w3cProfileMap.put( "gMonth", FP_DATE );
            w3cProfileMap.put( "gDay", FP_DATE );
            w3cProfileMap.put( "ID", FP_STRING );
            w3cProfileMap.put( "IDREF", FP_STRING );
            w3cProfileMap.put( "IDREFS", FP_STRING );
            w3cProfileMap.put( "language", FP_LANGUAGE );
            w3cProfileMap.put( "QName", FP_STRING );
            w3cProfileMap.put( "anyURI", FP_STRING );
            w3cProfileMap.put( "hexBinary", FP_STRING );
            w3cProfileMap.put( "base64Binary", FP_STRING );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
