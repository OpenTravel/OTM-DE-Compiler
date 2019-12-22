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

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.management.openmbean.OpenDataException;

/**
 * Implementation of the <code>TestMBean</code> interface.
 */
public class TestMBeanImpl extends StandardMBean implements TestMBean {

    private StructuredData sd;

    /**
     * Default constructor.
     */
    public TestMBeanImpl() throws NotCompliantMBeanException, OpenDataException {
        super( TestMBean.class );
        sd = new StructuredData();
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getStringAttribute()
     */
    @Override
    public String getStringAttribute() {
        return "StringValue";
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getDoubleAttribute()
     */
    @Override
    public Double getDoubleAttribute() {
        return 10.0d;
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getFloatAttribute()
     */
    @Override
    public Float getFloatAttribute() {
        return 10.0f;
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getLongAttribute()
     */
    @Override
    public Long getLongAttribute() {
        return 10L;
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getIntegerAttribute()
     */
    @Override
    public Integer getIntegerAttribute() {
        return 10;
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getShortAttribute()
     */
    @Override
    public Short getShortAttribute() {
        return 10;
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getByteAttribute()
     */
    @Override
    public Byte getByteAttribute() {
        return 10;
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getBigIntegerAttribute()
     */
    @Override
    public BigInteger getBigIntegerAttribute() {
        return new BigInteger( "10" );
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getBigDecimalAttribute()
     */
    @Override
    public BigDecimal getBigDecimalAttribute() {
        return new BigDecimal( 10 );
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getBooleanAttribute()
     */
    @Override
    public Boolean getBooleanAttribute() {
        return true;
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getStructuredDataAttribute()
     */
    @Override
    public StructuredData getStructuredDataAttribute() {
        return sd;
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getInvalidAttribute()
     */
    @Override
    public String[] getInvalidAttribute() {
        return new String[] {"Invalid"};
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getInvalidNumberAttribute()
     */
    @Override
    public Fraction getInvalidNumberAttribute() {
        return Fraction.getFraction( 1, 2 );
    }

    /**
     * @see org.opentravel.nagios.TestMBean#getNullAttribute()
     */
    @Override
    public String getNullAttribute() {
        return null;
    }

    /**
     * @see org.opentravel.nagios.TestMBean#testOperation()
     */
    @Override
    public void testOperation() {
        // No action required
    }

}
