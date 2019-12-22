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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;


/**
 * Verifies the functions of the <code>NagiosJmxPlugin</code> class.
 */
public class TestNagiosJmxPlugin {

    public static final String TEST_MBEAN_NAME = "org.opentravel.jmx:type=TestMBean";
    public static final String SERVICE_URL = "service:jmx:rmi://localhost/jndi/rmi://localhost:12001/jmxrmi";

    @Test
    public void testOkValues() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "StringAttribute", null, null ), 0,
            "JMX OK - StringAttribute = StringValue" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "DoubleAttribute", null, null ), 0,
            "JMX OK - DoubleAttribute = 10.0 | 'DoubleAttribute'=10.0;;;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "BooleanAttribute", null, null ), 0,
            "JMX OK - BooleanAttribute = true" );
        runCheckJmx(
            buildMainArgs( SERVICE_URL, TEST_MBEAN_NAME, "StructuredDataAttribute", "Value1", null, null, null, null ),
            0, "JMX OK - StructuredDataAttribute.Value1 = 1 | 'StructuredDataAttribute Value1'=1;;;;" );
        runCheckJmx( buildMainArgs( SERVICE_URL, TEST_MBEAN_NAME, "IntegerAttribute", null, null, null, null, "ms" ), 0,
            "JMX OK - IntegerAttribute = 10ms | 'IntegerAttribute'=10ms;;;;" );
    }

    @Test
    public void testWarningThresholds() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "StringAttribute", "WarningValue", "StringValue" ), 1,
            "JMX WARNING - StringAttribute = StringValue" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "DoubleAttribute", "5", "15" ), 1,
            "JMX WARNING - DoubleAttribute = 10.0 | 'DoubleAttribute'=10.0;5;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "FloatAttribute", "5", "15" ), 1,
            "JMX WARNING - FloatAttribute = 10.0 | 'FloatAttribute'=10.0;5;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "LongAttribute", "5", "15" ), 1,
            "JMX WARNING - LongAttribute = 10 | 'LongAttribute'=10;5;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "IntegerAttribute", "5", "15" ), 1,
            "JMX WARNING - IntegerAttribute = 10 | 'IntegerAttribute'=10;5;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "ShortAttribute", "5", "15" ), 1,
            "JMX WARNING - ShortAttribute = 10 | 'ShortAttribute'=10;5;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "ByteAttribute", "5", "15" ), 1,
            "JMX WARNING - ByteAttribute = 10 | 'ByteAttribute'=10;5;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "BigDecimalAttribute", "5", "15" ), 1,
            "JMX WARNING - BigDecimalAttribute = 10 | 'BigDecimalAttribute'=10;5;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "BigIntegerAttribute", "5", "15" ), 1,
            "JMX WARNING - BigIntegerAttribute = 10 | 'BigIntegerAttribute'=10;5;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "BooleanAttribute", "false", "true" ), 1,
            "JMX WARNING - BooleanAttribute = true" );
    }

    @Test
    public void testCriticalThresholds() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "StringAttribute", "StringValue", "CriticalValue" ), 2,
            "JMX CRITICAL - StringAttribute = StringValue" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "DoubleAttribute", "0", "5" ), 2,
            "JMX CRITICAL - DoubleAttribute = 10.0 | 'DoubleAttribute'=10.0;0;5;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "FloatAttribute", "0", "5" ), 2,
            "JMX CRITICAL - FloatAttribute = 10.0 | 'FloatAttribute'=10.0;0;5;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "LongAttribute", "0", "5" ), 2,
            "JMX CRITICAL - LongAttribute = 10 | 'LongAttribute'=10;0;5;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "IntegerAttribute", "0", "5" ), 2,
            "JMX CRITICAL - IntegerAttribute = 10 | 'IntegerAttribute'=10;0;5;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "ShortAttribute", "0", "5" ), 2,
            "JMX CRITICAL - ShortAttribute = 10 | 'ShortAttribute'=10;0;5;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "ByteAttribute", "0", "5" ), 2,
            "JMX CRITICAL - ByteAttribute = 10 | 'ByteAttribute'=10;0;5;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "BigDecimalAttribute", "0", "5" ), 2,
            "JMX CRITICAL - BigDecimalAttribute = 10 | 'BigDecimalAttribute'=10;0;5;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "BigIntegerAttribute", "0", "5" ), 2,
            "JMX CRITICAL - BigIntegerAttribute = 10 | 'BigIntegerAttribute'=10;0;5;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "BooleanAttribute", "@false", "@true" ), 2,
            "JMX CRITICAL - BooleanAttribute = true" );
    }

    @Test
    public void testThresholdRanges() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "IntegerAttribute", "5:", "15" ), 0,
            "JMX OK - IntegerAttribute = 10 | 'IntegerAttribute'=10;5:;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "IntegerAttribute", "5:12", "15" ), 0,
            "JMX OK - IntegerAttribute = 10 | 'IntegerAttribute'=10;5:12;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "IntegerAttribute", "~:5", "15" ), 1,
            "JMX WARNING - IntegerAttribute = 10 | 'IntegerAttribute'=10;~:5;15;;" );
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "IntegerAttribute", "@5:12", "15" ), 1,
            "JMX WARNING - IntegerAttribute = 10 | 'IntegerAttribute'=10;@5:12;15;;" );
    }

    @Test
    public void testNullReturnValue() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "NullAttribute", null, null ), 1,
            "JMX WARNING - Value not set. JMX query returned null value." );
    }

    @Test
    public void testOperationInvokation() throws Exception {
        runCheckJmx(
            buildMainArgs( SERVICE_URL, TEST_MBEAN_NAME, "IntegerAttribute", null, null, null, "testOperation", null ),
            0, "JMX OK - IntegerAttribute = 10 | 'IntegerAttribute'=10;;;;" );
    }

    @Test
    public void testMBeanQuery() throws Exception {
        runCheckJmx( buildMainArgs( "*:type=TestMBean", "StringAttribute", null, null ), 0,
            "JMX OK - StringAttribute = StringValue" );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testMBeanQuery_NoMatch() throws Exception {
        runCheckJmx( buildMainArgs( "*:type=TestMBean99", "StringAttribute", null, null ), 0,
            "JMX OK - StringAttribute = StringValue" );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testMBeanQuery_MultipleMatches() throws Exception {
        runCheckJmx( buildMainArgs( "org.opentravel.jmx:*", "StringAttribute", null, null ), 0,
            "JMX OK - StringAttribute = StringValue" );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidRegex() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "StringAttribute", null, "(.*" ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidReturnType() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "InvalidAttribute", null, null ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidNumericType() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "InvalidNumberAttribute", "5", "15" ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidThresholdValue() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "IntegerAttribute", "xyz", "15" ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidThresholdRange() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "IntegerAttribute", "?5:12", "15" ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidUnit() throws Exception {
        runCheckJmx( buildMainArgs( SERVICE_URL, TEST_MBEAN_NAME, "IntegerAttribute", null, null, null, null, "xyz" ),
            0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidOperation() throws Exception {
        runCheckJmx(
            buildMainArgs( SERVICE_URL, TEST_MBEAN_NAME, "IntegerAttribute", null, null, null, "xyzOperation", null ),
            0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testUnavailableJmxUrl() throws Exception {
        runCheckJmx( buildMainArgs( "service:jmx:rmi://localhost/jndi/rmi://localhost:12002/jmxrmi", TEST_MBEAN_NAME,
            "IntegerAttribute", null, null, null, null, null ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testMalformedJmxUrl() throws Exception {
        runCheckJmx( buildMainArgs( "--This is a malformed URL--", TEST_MBEAN_NAME, "IntegerAttribute", null, null,
            null, null, null ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidObjectName() throws Exception {
        runCheckJmx( buildMainArgs( "--Malformed object name--", "IntegerAttribute", null, null ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testMBeanNotFound() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME + "99", "IntegerAttribute", null, null ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidAttributeName() throws Exception {
        runCheckJmx( buildMainArgs( TEST_MBEAN_NAME, "NonExistentAttribute", null, null ), 0, null );
    }

    @Test(expected = NagiosJmxPluginException.class)
    public void testInvalidKeyName() throws Exception {
        runCheckJmx(
            buildMainArgs( SERVICE_URL, TEST_MBEAN_NAME, "StructuredDataAttribute", "BadKey", null, null, null, null ),
            0, null );
    }

    @Test
    public void testHelpOutput() throws Exception {
        runCheckJmx( new String[] {"-h"}, 0, "Usage: check_jmx" );
        runCheckJmx( new String[0], 2, "Usage: check_jmx" );
    }

    private void runCheckJmx(String[] mainArgs, int expectedExitCode, String expectedOutput) throws Exception {
        NagiosJmxPlugin plugin = new NagiosJmxPlugin();
        Properties props = NagiosJmxPlugin.parseArguments( mainArgs );
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream( outBytes );
        int exitCode;

        plugin.setOutputStream( out );
        exitCode = plugin.execute( props );
        assertEquals( expectedExitCode, exitCode );
        assertTrue( outBytes.toString().startsWith( expectedOutput ) );
    }

    private String[] buildMainArgs(String mbeanName, String attributeName, String warningThreshold,
        String criticalThreshold) {
        return buildMainArgs( SERVICE_URL, mbeanName, attributeName, null, warningThreshold, criticalThreshold, null,
            null );
    }

    private String[] buildMainArgs(String serviceUrl, String mbeanName, String attributeName, String keyName,
        String warningThreshold, String criticalThreshold, String operationName, String unit) {
        List<String> mainArgs = new ArrayList<>();

        mainArgs.addAll( Arrays.asList( "-U", serviceUrl ) );
        mainArgs.addAll( Arrays.asList( "-A", attributeName ) );
        mainArgs.addAll( Arrays.asList( "-O", mbeanName ) );
        mainArgs.addAll( Arrays.asList( "--username", "dummy" ) );
        mainArgs.addAll( Arrays.asList( "--password", "user" ) );
        mainArgs.add( "-v" );

        if (keyName != null) {
            mainArgs.addAll( Arrays.asList( "-K", keyName ) );
        }
        if (warningThreshold != null) {
            mainArgs.addAll( Arrays.asList( "-w", warningThreshold ) );
        }
        if (criticalThreshold != null) {
            mainArgs.addAll( Arrays.asList( "-c", criticalThreshold ) );
        }
        if (unit != null) {
            mainArgs.addAll( Arrays.asList( "-u", unit ) );
        }
        if (operationName != null) {
            mainArgs.addAll( Arrays.asList( "-o", operationName ) );
        }
        return mainArgs.toArray( new String[mainArgs.size()] );
    }

    @BeforeClass
    public static void setupMBeans() throws Exception {
        LocateRegistry.createRegistry( 12001 );
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JMXConnectorServer server =
            JMXConnectorServerFactory.newJMXConnectorServer( new JMXServiceURL( SERVICE_URL ), null, mbs );
        ObjectName mbeanName = new ObjectName( TEST_MBEAN_NAME );
        ObjectName mbean1Name = new ObjectName( TEST_MBEAN_NAME + 1 );
        TestMBeanImpl mbean = new TestMBeanImpl();
        TestMBeanImpl mbean1 = new TestMBeanImpl();

        if (!mbs.isRegistered( mbeanName )) {
            mbs.registerMBean( mbean, mbeanName );
        }
        if (!mbs.isRegistered( mbean1Name )) {
            mbs.registerMBean( mbean1, mbean1Name );
        }
        server.start();
    }

}
