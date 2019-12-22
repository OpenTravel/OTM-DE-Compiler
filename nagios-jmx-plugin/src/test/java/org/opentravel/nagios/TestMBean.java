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

package org.opentravel.nagios;

import org.apache.commons.lang3.math.Fraction;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * MBean interface used for testing the check_jmx Nagios plugin.
 */
public interface TestMBean {

    /**
     * Returns the value of a string attribute.
     * 
     * @return String
     */
    public String getStringAttribute();

    /**
     * Returns the value of a double attribute.
     * 
     * @return Double
     */
    public Double getDoubleAttribute();

    /**
     * Returns the value of a float attribute.
     * 
     * @return Float
     */
    public Float getFloatAttribute();

    /**
     * Returns the value of a long integer attribute.
     * 
     * @return Long
     */
    public Long getLongAttribute();

    /**
     * Returns the value of a integer attribute.
     * 
     * @return Integer
     */
    public Integer getIntegerAttribute();

    /**
     * Returns the value of a short attribute.
     * 
     * @return Short
     */
    public Short getShortAttribute();

    /**
     * Returns the value of a byte attribute.
     * 
     * @return Byte
     */
    public Byte getByteAttribute();

    /**
     * Returns the value of a big integer attribute.
     * 
     * @return BigInteger
     */
    public BigInteger getBigIntegerAttribute();

    /**
     * Returns the value of a big decimal attribute.
     * 
     * @return BigDecimal
     */
    public BigDecimal getBigDecimalAttribute();

    /**
     * Returns the value of a boolean attribute.
     * 
     * @return Boolean
     */
    public Boolean getBooleanAttribute();

    /**
     * Returns a structured data attribute.
     * 
     * @return StructuredData
     */
    public StructuredData getStructuredDataAttribute();

    /**
     * Returns an attribute with a data type that is invalid for the check_jmx plugin.
     * 
     * @return String[]
     */
    public String[] getInvalidAttribute();

    /**
     * Returns an attribute with an invalid numeric type.
     * 
     * @return Fraction
     */
    public Fraction getInvalidNumberAttribute();

    /**
     * Returns a null attribute value.
     * 
     * @return String
     */
    public String getNullAttribute();

    /**
     * Operation that can be called as part of the check_jmx plugin flow.
     */
    public void testOperation();

}
