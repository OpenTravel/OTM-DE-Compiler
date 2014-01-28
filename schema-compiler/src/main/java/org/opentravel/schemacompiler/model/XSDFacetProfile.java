package org.opentravel.schemacompiler.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Enumeration that provides detailed information about which XML schema facets are applicable to
 * <code>TLAttributeType</code> entities in the resulting schemas produced from an OTM model.
 * 
 * @author S. Livezey
 */
public enum XSDFacetProfile {

    FP_String(true, true, true, true, true, true, false, false, false, false, false, false), FP_Boolean(
            false, false, false, true, false, true, false, false, false, false, false, false), FP_Decimal(
            false, false, false, true, true, true, true, true, true, true, true, true), FP_FloatingPoint(
            false, false, false, true, true, true, false, false, true, true, true, true), FP_Integer(
            false, false, false, true, true, true, true, true, true, true, true, true), FP_DateTime(
            false, false, false, true, true, true, false, false, true, true, true, true);

    private static final Set<String> stringTypes = new HashSet<String>(Arrays.asList(new String[] {
            "string", "hexBinary", "base64Binary", "anyURI", "QName", "NOTATION",
            "normalizedString", "token", "language", "NMTOKEN", "NMTOKENS", "Name", "NCName", "ID",
            "IDREF", "IDREFS", "ENTITY", "ENTITIES" }));;
    private static final Set<String> booleanTypes = new HashSet<String>(
            Arrays.asList(new String[] { "boolean" }));;
    private static final Set<String> decimalTypes = new HashSet<String>(
            Arrays.asList(new String[] { "decimal" }));;
    private static final Set<String> floatingPointTypes = new HashSet<String>(
            Arrays.asList(new String[] { "float", "double" }));;
    private static final Set<String> integerTypes = new HashSet<String>(Arrays.asList(new String[] {
            "integer", "nonPositiveInteger", "negativeInteger", "long", "int", "short", "byte",
            "nonNegativeInteger", "positiveInteger", "unsignedLong", "unsignedInt",
            "unsignedShort", "unsignedByte" }));;
    private static final Set<String> dateTimeTypes = new HashSet<String>(
            Arrays.asList(new String[] { "dateTime", "date", "time", "duration", "gYear",
                    "gYearMonth", "gMonthDay", "gMonth", "gDay" }));;

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
     * @param xmlSchemaType
     *            the XML schema type (must be defined in the XML schema namespace)
     * @return XSDFacetProfile
     */
    public static XSDFacetProfile toFacetProfile(String xmlSchemaType) {
        XSDFacetProfile result = null;

        if (stringTypes.contains(xmlSchemaType)) {
            result = FP_String;

        } else if (booleanTypes.contains(xmlSchemaType)) {
            result = FP_Boolean;

        } else if (decimalTypes.contains(xmlSchemaType)) {
            result = FP_Decimal;

        } else if (floatingPointTypes.contains(xmlSchemaType)) {
            result = FP_FloatingPoint;

        } else if (integerTypes.contains(xmlSchemaType)) {
            result = FP_Integer;

        } else if (dateTimeTypes.contains(xmlSchemaType)) {
            result = FP_DateTime;

        }
        return result;
    }

    /**
     * Full constructor that initializes all of the supported facet boolean values.
     * 
     * @param length
     *            value of the 'lengthSupported' property
     * @param minLength
     *            value of the 'minLengthSupported' property
     * @param maxLength
     *            value of the 'maxLengthSupported' property
     * @param pattern
     *            value of the 'patternSupported' property
     * @param enumeration
     *            value of the 'enumerationSupported' property
     * @param whiteSpace
     *            value of the 'whiteSpaceSupported' property
     * @param totalDigits
     *            value of the 'totalDigitsSupported' property
     * @param fractionDigits
     *            value of the 'fractionDigitsSupported' property
     * @param minInclusive
     *            value of the 'minInclusiveSupported' property
     * @param maxInclusive
     *            value of the 'maxInclusiveSupported' property
     * @param minExclusive
     *            value of the 'minExclusiveSupported' property
     * @param maxExclusive
     *            value of the 'maxExclusiveSupported' property
     */
    private XSDFacetProfile(boolean length, boolean minLength, boolean maxLength, boolean pattern,
            boolean enumeration, boolean whiteSpace, boolean totalDigits, boolean fractionDigits,
            boolean minInclusive, boolean maxInclusive, boolean minExclusive, boolean maxExclusive) {
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

}
