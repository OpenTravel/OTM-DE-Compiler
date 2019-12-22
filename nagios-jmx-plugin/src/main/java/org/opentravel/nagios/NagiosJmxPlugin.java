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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ConnectException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.InvalidKeyException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Nagios JMX plugin.
 * <p>
 * Based upon the Syabru JMX Plugin for Nagios (https://snippets.syabru.ch/nagios-jmx-plugin/download.html).
 */
public class NagiosJmxPlugin {

    /**
     * Nagios status codes and messages.
     */
    enum Status {
        /**
         * Status code NAGIOS OK.
         */
        OK(0, "JMX OK - "),

        /**
         * Status code NAGIOS WARNING.
         */
        WARNING(1, "JMX WARNING - "),

        /**
         * Status code NAGIOS CRITICAL.
         */
        CRITICAL(2, "JMX CRITICAL - "),

        /**
         * Status code NAGIOS UNKNOWN.
         */
        UNKNOWN(3, "JMX UNKNOWN - ");

        private int exitCode;
        private String messagePrefix;

        /**
         * Constructor that assigns the exit code and message prefix for the status value.
         * 
         * @param exitCode Exit code.
         * @param messagePrefix Message prefix.
         */
        private Status(int exitCode, String messagePrefix) {
            this.exitCode = exitCode;
            this.messagePrefix = messagePrefix;
        }

        /**
         * Returns the exit code for the status value.
         * 
         * @return int
         */
        public int getExitCode() {
            return exitCode;
        }

        /**
         * Returns the message prefix for the status value.
         * 
         * @return String
         */
        public String getMessagePrefix() {
            return messagePrefix;
        }

    }

    /**
     * Unit enumeration.
     */
    enum Unit {
        /**
         * Unit bytes.
         */
        BYTES("B"),
        /**
         * Unit kilobytes.
         */
        KILOBYTES("KB"),
        /**
         * Unit megabytes.
         */
        MEGABYTES("MB"),
        /**
         * Unit terabytes.
         */
        TERABYTES("TB"),
        /**
         * Unit seconds.
         */
        SECONDS("s"),
        /**
         * Unit microseconds.
         */
        MICROSECONDS("us"),
        /**
         * Unit milliseconds.
         */
        MILLISECONDS("ms"),
        /**
         * Unit counter.
         */
        COUNTER("c");

        private String abbreviation;

        /**
         * C'tor.
         * 
         * @param abbreviation Abbreviation.
         */
        private Unit(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        /**
         * Parse abbreviation and return matching unit.
         * 
         * @param abbr Abbreviation.
         * @return Matching unit, null if not found.
         */
        public static Unit parse(String abbr) {
            for (Unit unit : Unit.values()) {
                if (unit.getAbbreviation().equals( abbr ))
                    return unit;
            }
            return null;
        }
    }

    /**
     * Username system property.
     */
    public static final String PROP_USERNAME = "username";

    /**
     * Password system property.
     */
    public static final String PROP_PASS = "password";

    /**
     * Object name system property.
     */
    public static final String PROP_OBJECT_NAME = "objectName";

    /**
     * Attribute name system property.
     */
    public static final String PROP_ATTRIBUTE_NAME = "attributeName";

    /**
     * Attribute key system property.
     */
    public static final String PROP_ATTRIBUTE_KEY = "attributeKey";

    /**
     * Service URL system property.
     */
    public static final String PROP_SERVICE_URL = "serviceUrl";

    /**
     * Threshold warning level system property. The number format of this property has to correspond to the type of the
     * attribute object.
     */
    public static final String PROP_THRESHOLD_WARNING = "thresholdWarning";

    /**
     * Threshold critical level system property. The number format of this property has to correspond the type of the
     * attribute object.
     */
    public static final String PROP_THRESHOLD_CRITICAL = "thresholdCritical";

    /**
     * Units system property.
     */
    public static final String PROP_UNITS = "units";

    /**
     * Operation to invoke on MBean.
     */
    public static final String PROP_OPERATION = "operation";

    /**
     * Verbose output.
     */
    public static final String PROP_VERBOSE = "verbose";

    /**
     * Help output.
     */
    public static final String PROP_HELP = "help";

    private HashMap<MBeanServerConnection,JMXConnector> connections = new HashMap<>();
    private PrintStream out = System.out;

    /**
     * Open a connection to a MBean server.
     * 
     * @param serviceUrl Service URL, e.g. service:jmx:rmi://HOST:PORT/jndi/rmi://HOST:PORT/jmxrmi
     * @param username Username
     * @param password Password
     * @return MBeanServerConnection if succesfull.
     * @throws IOException XX
     */
    public MBeanServerConnection openConnection(JMXServiceURL serviceUrl, String username, String password)
        throws IOException {
        HashMap<String,Object> environment = new HashMap<>();
        JMXConnector connector;

        // Add environment variable to check for dead connections.
        environment.put( "jmx.remote.x.client.connection.check.period", 5000 );

        if (username != null && password != null) {
            environment = new HashMap<>();
            environment.put( JMXConnector.CREDENTIALS, new String[] {username, password} );
            connector = JMXConnectorFactory.connect( serviceUrl, environment );

        } else {
            connector = JMXConnectorFactory.connect( serviceUrl, environment );
        }
        MBeanServerConnection connection = connector.getMBeanServerConnection();
        connections.put( connection, connector );
        return connection;
    }

    /**
     * Close JMX connection.
     * 
     * @param connection Connection.
     * @throws NagiosJmxPluginException thrown if an error occurs while closing the connection
     */
    public void closeConnection(MBeanServerConnection connection) throws NagiosJmxPluginException {
        JMXConnector connector = connections.remove( connection );

        if (connector != null) {
            try {
                connector.close();

            } catch (Exception e) {
                throw new NagiosJmxPluginException( "Error closing JMX connection", e );
            }
        }
    }

    /**
     * Get object name object.
     * 
     * @param connection MBean server connection.
     * @param objectName Object name string.
     * @return Object name object.
     * @throws InstanceNotFoundException If object not found.
     * @throws MalformedObjectNameException If object name is malformed.
     * @throws NagiosJmxPluginException If object name is not unqiue.
     * @throws IOException In case of a communication error.
     */
    public ObjectName getObjectName(MBeanServerConnection connection, String objectName)
        throws InstanceNotFoundException, MalformedObjectNameException, NagiosJmxPluginException, IOException {
        ObjectName objName = new ObjectName( objectName );

        if (objName.isPropertyPattern() || objName.isDomainPattern()) {
            Set<ObjectInstance> mBeans = connection.queryMBeans( objName, null );

            if (mBeans.isEmpty()) {
                throw new InstanceNotFoundException();

            } else if (mBeans.size() > 1) {
                throw new NagiosJmxPluginException(
                    "Object name not unique: objectName pattern matches " + mBeans.size() + " MBeans." );

            } else {
                objName = mBeans.iterator().next().getObjectName();
            }
        }
        return objName;
    }

    /**
     * Query MBean object.
     * 
     * @param connection MBean server connection.
     * @param objectName Object name.
     * @param attributeName Attribute name.
     * @param attributeKey Attribute key.
     * @return Value.
     * @throws NagiosJmxPluginException thrown if any other unknown error occcurs
     */
    public Object query(MBeanServerConnection connection, String objectName, String attributeName, String attributeKey)
        throws NagiosJmxPluginException {
        try {
            ObjectName objName = getObjectName( connection, objectName );
            Object attribute = connection.getAttribute( objName, attributeName );
            Object value = null;

            if (attribute instanceof CompositeDataSupport) {
                CompositeDataSupport compositeAttr = (CompositeDataSupport) attribute;
                value = compositeAttr.get( attributeKey );

            } else {
                value = attribute;
            }
            return value;

        } catch (MalformedObjectNameException e) {
            throw new NagiosJmxPluginException( "Malformed objectName [" + objectName + "]", e );

        } catch (InstanceNotFoundException e) {
            throw new NagiosJmxPluginException( "objectName not found [" + objectName + "]", e );

        } catch (AttributeNotFoundException e) {
            throw new NagiosJmxPluginException( "attributeName not found [" + attributeName + "]", e );

        } catch (InvalidKeyException e) {
            throw new NagiosJmxPluginException( "attributeKey not found [" + attributeKey + "]", e );

        } catch (Exception e) {
            throw new NagiosJmxPluginException( "Error querying server: " + e.getMessage(), e );
        }
    }

    /**
     * Get system properties and execute query.
     * 
     * @param args Arguments as properties.
     * @return Nagios exit code.
     * @throws NagiosJmxPluginException thrown if an error occurs while communicating with the remote JMX provider
     */
    public int execute(Properties args) throws NagiosJmxPluginException {
        String username = args.getProperty( PROP_USERNAME );
        String password = args.getProperty( PROP_PASS );
        String objectName = args.getProperty( PROP_OBJECT_NAME );
        String attributeName = args.getProperty( PROP_ATTRIBUTE_NAME );
        String attributeKey = args.getProperty( PROP_ATTRIBUTE_KEY );
        String serviceUrl = args.getProperty( PROP_SERVICE_URL );
        String thresholdWarning = args.getProperty( PROP_THRESHOLD_WARNING );
        String thresholdCritical = args.getProperty( PROP_THRESHOLD_CRITICAL );
        String operation = args.getProperty( PROP_OPERATION );
        String units = args.getProperty( PROP_UNITS );
        String help = args.getProperty( PROP_HELP );
        JMXServiceURL url = null;
        Unit unit = null;

        if (help != null) {
            showHelp();
            return Status.OK.getExitCode();
        }

        if (objectName == null || attributeName == null || serviceUrl == null) {
            showUsage();
            return Status.CRITICAL.getExitCode();
        }

        if (units != null && Unit.parse( units ) == null) {
            throw new NagiosJmxPluginException( "Unknown unit [" + units + "]" );

        } else {
            unit = Unit.parse( units );
        }

        try {
            url = new JMXServiceURL( serviceUrl );

        } catch (MalformedURLException e) {
            throw new NagiosJmxPluginException( "Malformed service URL [" + serviceUrl + "]", e );
        }

        // Connect to MBean server.
        Object value = null;
        int exitCode;
        MBeanServerConnection connection = null;

        try {
            try {
                connection = openConnection( url, username, password );

            } catch (ConnectException ce) {
                throw new NagiosJmxPluginException( "Error opening RMI connection: " + ce.getMessage(), ce );
            } catch (Exception e) {
                throw new NagiosJmxPluginException( "Error opening connection: " + e.getMessage(), e );
            }

            // Query attribute.
            value = query( connection, objectName, attributeName, attributeKey );

            // Invoke operation if defined.
            if (operation != null) {
                try {
                    connection.invoke( getObjectName( connection, objectName ), operation, null, null );

                } catch (Exception e) {
                    throw new NagiosJmxPluginException(
                        "Error invoking operation [" + operation + "]: " + e.getMessage(), e );
                }
            }

        } finally {
            if (connection != null) {
                closeConnection( connection );
            }
        }

        exitCode = checkReturnValue( attributeName, attributeKey, thresholdWarning, thresholdCritical, unit, value );
        return exitCode;
    }

    /**
     * Checks the return value of a JMX attribute returned from the remote server.
     * 
     * @param attributeName the name of the MBean attribute
     * @param attributeKey the name of the attribute key (used for complex value lookups)
     * @param thresholdWarning the warning threshold value (may be null)
     * @param thresholdCritical the critical threshold value (may be null)
     * @param unit the unit of the return value
     * @param value the return value to check
     * @return int
     * @throws NagiosJmxPluginException thrown if an error occurs that prevents the return value from being evaluated
     */
    private int checkReturnValue(String attributeName, String attributeKey, String thresholdWarning,
        String thresholdCritical, Unit unit, Object value) throws NagiosJmxPluginException {
        int exitCode;

        if (value != null) {
            Status status;

            if (value instanceof Number) {
                status = checkReturnValue( (Number) value, thresholdWarning, thresholdCritical );

            } else if (value instanceof String) {
                status = checkReturnValue( (String) value, thresholdWarning, thresholdCritical );

            } else if (value instanceof Boolean) {
                status = checkReturnValue( (Boolean) value, thresholdWarning, thresholdCritical );

            } else {
                throw new NagiosJmxPluginException( "Type of return value not supported [" + value.getClass().getName()
                    + "]. Must be either a " + "Number, String, or Boolean object." );
            }
            outputStatus( out, status, attributeName, attributeKey, value, unit );

            if (value instanceof Number) {
                outputPerformanceData( out, attributeName, attributeKey, (Number) value, thresholdWarning,
                    thresholdCritical, unit );
            }
            out.println();
            exitCode = status.getExitCode();

        } else {
            out.print( Status.WARNING.getMessagePrefix() );
            out.println( "Value not set. JMX query returned null value." );
            exitCode = Status.WARNING.getExitCode();
        }
        return exitCode;
    }

    /**
     * Checks the given return value against the thresholds provided.
     * 
     * @param value the return value to check
     * @param thresholdWarning the warning threshold for the value
     * @param thresholdCritical the critical threshold for the value
     * @return Status
     * @throws NagiosJmxPluginException
     */
    private Status checkReturnValue(Number value, String thresholdWarning, String thresholdCritical)
        throws NagiosJmxPluginException {
        Status status;

        if (isOutsideThreshold( value, thresholdCritical )) {
            status = Status.CRITICAL;
        } else if (isOutsideThreshold( value, thresholdWarning )) {
            status = Status.WARNING;
        } else {
            status = Status.OK;
        }
        return status;
    }

    /**
     * Checks the given return value against the thresholds provided.
     * 
     * @param value the return value to check
     * @param thresholdWarning the warning threshold for the value
     * @param thresholdCritical the critical threshold for the value
     * @return Status
     * @throws NagiosJmxPluginException
     */
    private Status checkReturnValue(String value, String thresholdWarning, String thresholdCritical)
        throws NagiosJmxPluginException {
        Status status;

        if (matchesThreshold( value, thresholdCritical )) {
            status = Status.CRITICAL;
        } else if (matchesThreshold( value, thresholdWarning )) {
            status = Status.WARNING;
        } else {
            status = Status.OK;
        }
        return status;
    }

    /**
     * Checks the given return value against the thresholds provided.
     * 
     * @param value the return value to check
     * @param thresholdWarning the warning threshold for the value
     * @param thresholdCritical the critical threshold for the value
     * @return Status
     * @throws NagiosJmxPluginException
     */
    private Status checkReturnValue(Boolean value, String thresholdWarning, String thresholdCritical)
        throws NagiosJmxPluginException {
        Status status;

        if (matchesThreshold( value.toString(), thresholdCritical )) {
            status = Status.CRITICAL;
        } else if (matchesThreshold( value.toString(), thresholdWarning )) {
            status = Status.WARNING;
        } else {
            status = Status.OK;
        }
        return status;
    }

    /**
     * Get threshold limits. The array returned contains two values, min and max. If value is +/- inifinity, value is
     * set to null.
     * 
     * @param clazz Class threshold gets parsed as.
     * @param threshold Threshold range.
     * @return Array with two elements containing min, max.
     * @throws NagiosJmxPluginException If threshold can't be parsed as clazz or threshold format is not supported.
     * @see http://nagiosplug.sourceforge.net/developer-guidelines.html#THRESHOLDFORMAT
     */
    Number[] getThresholdLimits(Class<? extends Number> clazz, String threshold) throws NagiosJmxPluginException {
        Matcher matcher1 = Pattern.compile( "^(\\d+\\.?\\d*)$" ).matcher( threshold ); // 10
        Matcher matcher2 = Pattern.compile( "^(\\d+\\.?\\d*):$" ).matcher( threshold ); // 10:
        Matcher matcher3 = Pattern.compile( "^~:(\\d+\\.?\\d*)$" ).matcher( threshold ); // ~:10
        Matcher matcher4 = Pattern.compile( "^(\\d+\\.?\\d*):(\\d+\\.?\\d*)$" ).matcher( threshold ); // 10:20
        Matcher matcher5 = Pattern.compile( "^@(\\d+\\.?\\d*):(\\d+\\.?\\d*)$" ).matcher( threshold ); // @10:20
        Number[] limits = new Number[2];

        if (matcher1.matches()) {
            limits[0] = parseAsNumber( clazz, "0" );
            limits[1] = parseAsNumber( clazz, matcher1.group( 1 ) );
        } else if (matcher2.matches()) {
            limits[0] = parseAsNumber( clazz, matcher2.group( 1 ) );
            limits[1] = null;
        } else if (matcher3.matches()) {
            limits[0] = null;
            limits[1] = parseAsNumber( clazz, matcher3.group( 1 ) );
        } else if (matcher4.matches()) {
            limits[0] = parseAsNumber( clazz, matcher4.group( 1 ) );
            limits[1] = parseAsNumber( clazz, matcher4.group( 2 ) );
        } else if (matcher5.matches()) {
            limits[0] = parseAsNumber( clazz, matcher5.group( 2 ) );
            limits[1] = parseAsNumber( clazz, matcher5.group( 1 ) );
        } else {
            throw new NagiosJmxPluginException(
                "Error parsing threshold. " + "Unknown threshold range format [" + threshold + "]" );
        }
        return limits;
    }

    /**
     * Parse value as clazz.
     * 
     * @param clazz Class.
     * @param value Value.
     * @return Value parsed as Number of type clazz.
     * @throws NagiosJmxPluginException If clazz is not supported or value can't be parsed.
     */
    Number parseAsNumber(Class<? extends Number> clazz, String value) throws NagiosJmxPluginException {
        Number result;
        try {
            if (Double.class.equals( clazz )) {
                result = Double.valueOf( value );
            } else if (Integer.class.equals( clazz )) {
                result = Integer.valueOf( value );
            } else if (Long.class.equals( clazz )) {
                result = Long.valueOf( value );
            } else if (Short.class.equals( clazz )) {
                result = Short.valueOf( value );
            } else if (Byte.class.equals( clazz )) {
                result = Byte.valueOf( value );
            } else if (Float.class.equals( clazz )) {
                result = Float.valueOf( value );
            } else if (BigInteger.class.equals( clazz )) {
                result = new BigInteger( value );
            } else if (BigDecimal.class.equals( clazz )) {
                result = new BigDecimal( value );
            } else {
                throw new NumberFormatException( "Can't handle object type [" + value.getClass().getName() + "]" );
            }
        } catch (NumberFormatException e) {
            throw new NagiosJmxPluginException(
                "Error parsing threshold " + "value [" + value + "]. Expected [" + clazz.getName() + "]", e );
        }
        return result;
    }

    /**
     * Output status.
     * 
     * @param out Print stream.
     * @param status Status.
     * @param attributeName Attribute name.
     * @param attributeKey Attribute key, or null
     * @param value Value
     * @param unit Unit.
     */
    private void outputStatus(PrintStream out, Status status, String attributeName, String attributeKey, Object value,
        Unit unit) {
        StringBuilder output = new StringBuilder( status.getMessagePrefix() );

        output.append( attributeName );

        if (attributeKey != null) {
            output.append( "." ).append( attributeKey );
        }
        output.append( " = " ).append( value );

        if (unit != null) {
            output.append( unit.getAbbreviation() );
        }
        out.print( output.toString() );
    }

    /**
     * Get performance data output.
     * 
     * @param out Print stream.
     * @param attributeName Attribute name.
     * @param attributeKey Attribute key, or null
     * @param value Value
     * @param thresholdWarning Warning threshold.
     * @param thresholdCritical Critical threshold.
     * @param unit Unit, null if not defined.
     */
    private void outputPerformanceData(PrintStream out, String attributeName, String attributeKey, Number value,
        String thresholdWarning, String thresholdCritical, Unit unit) {
        StringBuilder output = new StringBuilder();

        output.append( " | '" );
        output.append( attributeName );

        if (attributeKey != null) {
            output.append( " " ).append( attributeKey );
        }
        output.append( "'=" ).append( value );

        if (unit != null) {
            output.append( unit.getAbbreviation() );
        }
        output.append( ";" );

        if (thresholdWarning != null) {
            output.append( thresholdWarning );
        }
        output.append( ";" );

        if (thresholdCritical != null) {
            output.append( thresholdCritical );
        }
        output.append( ";;" );
        out.print( output.toString() );
    }

    /**
     * Check if value is outside threshold range.
     * 
     * @param value Value, which is either Double, Long, Integer, Short, Byte, or Float.
     * @param threshold Threshold range, which must be parsable in same number format as value, can be null
     * @return true if value is outside threshold, false otherwise.
     * @throws NagiosJmxPluginException If number format is not parseable.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean isOutsideThreshold(Number value, String threshold) throws NagiosJmxPluginException {
        boolean outsideThreshold = false;

        if (threshold != null) {
            Number[] limits = getThresholdLimits( value.getClass(), threshold );
            Number min = limits[0];
            Number max = limits[1];

            if (value instanceof Double || value instanceof Float) {
                outsideThreshold = checkThresholds( value.doubleValue(), min, max );

            } else if (value instanceof Long || value instanceof Integer || value instanceof Short
                || value instanceof Byte) {
                outsideThreshold = checkThresholds( value.longValue(), min, max );

            } else if (value instanceof BigInteger || value instanceof BigDecimal) {
                outsideThreshold = checkThresholds( (Comparable) value, min, max );

            } else {
                throw new NumberFormatException( "Can't handle object type [" + value.getClass().getName() + "]" );
            }

        } else {
            outsideThreshold = false;
        }
        return outsideThreshold;
    }

    /**
     * Compares the given value to the min and max thresholds. If the value is outside the permitted range, this method
     * will return true; false otherwise.
     * 
     * @param value the value to check
     * @param min the min value limit (may be null)
     * @param max the max value limit (may be null)
     * @return boolean
     */
    private boolean checkThresholds(double value, Number min, Number max) {
        return (min != null && value < min.doubleValue()) || (max != null && value > max.doubleValue());
    }

    /**
     * Compares the given value to the min and max thresholds. If the value is outside the permitted range, this method
     * will return true; false otherwise.
     * 
     * @param value the value to check
     * @param min the min value limit (may be null)
     * @param max the max value limit (may be null)
     * @return boolean
     */
    private boolean checkThresholds(long value, Number min, Number max) {
        return (min != null && value < min.longValue()) || (max != null && value > max.longValue());
    }

    /**
     * Compares the given value to the min and max thresholds. If the value is outside the permitted range, this method
     * will return true; false otherwise.
     * 
     * @param value the value to check
     * @param min the min value limit (may be null)
     * @param max the max value limit (may be null)
     * @return boolean
     */
    private boolean checkThresholds(Comparable<Number> value, Number min, Number max) {
        return (min != null && value.compareTo( min ) < 0) || (max != null && value.compareTo( max ) > 0);
    }

    /**
     * Check if value matches threshold regular expression. A threshold starting with @ means that the threshold must
     * not match to return true.
     * 
     * @param value Value.
     * @param threshold Threshold regular expression.
     * @return true if value matches threshold regex, otherwise false.
     * @throws NagiosJmxPluginException If threshold regex is not parseable.
     */
    private boolean matchesThreshold(String value, String threshold) throws NagiosJmxPluginException {
        boolean matchesThreshold = false;
        try {
            if (threshold == null) {
                matchesThreshold = false;

            } else {
                if (threshold.startsWith( "@" )) {
                    matchesThreshold = Pattern.matches( threshold.substring( 1 ), value );
                } else {
                    matchesThreshold = !Pattern.matches( threshold, value );
                }
            }
            return matchesThreshold;

        } catch (PatternSyntaxException e) {
            throw new NagiosJmxPluginException( "Error parsing threshold " + "regex [" + threshold + "]", e );
        }
    }

    /**
     * Main method.
     * 
     * @param args Command line arguments.
     */
    @SuppressWarnings("squid:S106")
    public static void main(String[] args) {
        NagiosJmxPlugin plugin = new NagiosJmxPlugin();
        Properties props = parseArguments( args );
        String verbose = props.getProperty( PROP_VERBOSE );
        PrintStream out = System.out;
        int exitCode;

        try {
            exitCode = plugin.execute( props );

        } catch (NagiosJmxPluginException e) {
            out.println( Status.CRITICAL.getMessagePrefix() + e.getMessage() );

            if (verbose != null) {
                e.printStackTrace( out );
            }
            exitCode = Status.CRITICAL.getExitCode();

        } catch (Exception e) {
            out.println( Status.UNKNOWN.getMessagePrefix() + e.getMessage() );

            if (verbose != null) {
                e.printStackTrace( out );
            }
            exitCode = Status.UNKNOWN.getExitCode();
        }
        System.exit( exitCode );
    }

    /**
     * Show usage.
     * 
     * @throws NagiosJmxPluginException XX
     */
    private void showUsage() throws NagiosJmxPluginException {
        outputResource( getClass().getResource( "/usage.txt" ) );
    }

    /**
     * Show help.
     * 
     * @throws NagiosJmxPluginException thrown if an error occurs while displaying the help message
     */
    private void showHelp() throws NagiosJmxPluginException {
        outputResource( getClass().getResource( "/help.txt" ) );
    }

    /**
     * Output resource.
     * 
     * @param url Resource URL.
     * @throws NagiosJmxPluginException thrown if an error occurs while reading from the given URL
     */
    @SuppressWarnings("squid:S106")
    private void outputResource(URL url) throws NagiosJmxPluginException {
        try (Reader r = new InputStreamReader( url.openStream() )) {
            StringBuilder sbHelp = new StringBuilder();
            char[] buffer = new char[1024];

            for (int len = r.read( buffer ); len != -1; len = r.read( buffer )) {
                sbHelp.append( buffer, 0, len );
            }
            out.println( sbHelp.toString() );

        } catch (IOException e) {
            throw new NagiosJmxPluginException( e );
        }
    }

    /**
     * Parse command line arguments.
     * 
     * @param args Command line arguments.
     * @return Command line arguments as properties.
     */
    protected static Properties parseArguments(String[] args) {
        Properties props = new Properties();
        int i = 0;

        while (i < args.length) {
            if ("-h".equals( args[i] )) {
                props.put( PROP_HELP, "" );
            } else if ("-U".equals( args[i] )) {
                props.put( PROP_SERVICE_URL, args[++i] );
            } else if ("-O".equals( args[i] )) {
                props.put( PROP_OBJECT_NAME, args[++i] );
            } else if ("-A".equals( args[i] )) {
                props.put( PROP_ATTRIBUTE_NAME, args[++i] );
            } else if ("-K".equals( args[i] )) {
                props.put( PROP_ATTRIBUTE_KEY, args[++i] );
            } else if ("-v".equals( args[i] )) {
                props.put( PROP_VERBOSE, "true" );
            } else if ("-w".equals( args[i] )) {
                props.put( PROP_THRESHOLD_WARNING, args[++i] );
            } else if ("-c".equals( args[i] )) {
                props.put( PROP_THRESHOLD_CRITICAL, args[++i] );
            } else if ("--username".equals( args[i] )) {
                props.put( PROP_USERNAME, args[++i] );
            } else if ("--password".equals( args[i] )) {
                props.put( PROP_PASS, args[++i] );
            } else if ("-u".equals( args[i] )) {
                props.put( PROP_UNITS, args[++i] );
            } else if ("-o".equals( args[i] )) {
                props.put( PROP_OPERATION, args[++i] );
            }
            i++;
        }
        return props;
    }

    /**
     * Assigns the stream to which output will be directed (used for testing purposes - default is standard output).
     * 
     * @param out the stream to which output will be directed
     */
    protected void setOutputStream(PrintStream out) {
        this.out = out;
    }

}
