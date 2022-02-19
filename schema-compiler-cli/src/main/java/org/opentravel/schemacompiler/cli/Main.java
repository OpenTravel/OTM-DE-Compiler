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

package org.opentravel.schemacompiler.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.task.TaskFactory;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Test class to verify command-line invocation.
 * 
 * @author S. Livezey
 */
public class Main {

    private static final String SCRIPT_WINDOWS = "ota2compile.bat";
    private static final String SCRIPT_BASH = "ota2compile.sh";

    private static final String SCRIPT_NAME =
        System.getProperty( "os.name" ).startsWith( "Windows" ) ? SCRIPT_WINDOWS : SCRIPT_BASH;
    private static final String SCRIPT_SYNTAX = SCRIPT_NAME + " [options] <library-file>";

    private static final String MESSAGE_RB = "/org/opentravel/schemacompiler/cli/cli-messages.properties";
    private static final ResourceBundle messageBundle;

    private static final Logger log = LogManager.getLogger( Main.class );

    @SuppressWarnings("squid:S106")
    private PrintWriter out = new PrintWriter( System.out );

    /**
     * Main method invoked from the command-line.
     * 
     * @param args the GNU-style command-line arguments
     */
    public static void main(String[] args) {
        try {
            new Main().execute( args );

        } catch (Exception e) {
            Throwable rootCause = getRootCauseException( e );
            String errorMessage = MessageFormat.format( messageBundle.getString( "errorMessage" ),
                ((rootCause.getMessage() == null) ? rootCause.getClass().getSimpleName() : rootCause.getMessage()) );

            log.error( errorMessage );
        }
    }

    /**
     * Executes the compilation tasks using the command-line arguments provided.
     * 
     * @param args the GNU-style command-line arguments
     * @throws SchemaCompilerException thrown if an error occurs during library compilation
     * @throws ParseException thrown if the command-line arguments cannot be parsed
     * @throws IOException thrown if the specified library file does not exist or cannot be read
     */
    public void execute(String[] args) throws SchemaCompilerException, ParseException, IOException {
        try {
            CommandLine commandLineArgs = new GnuParser().parse( getCommandLineOptions(), args );

            if (validateCommandLine( commandLineArgs )) {
                CommandLineCompilerTaskOptions taskOptions = new CommandLineCompilerTaskOptions( commandLineArgs );

                // Select the user-specified schema compiler extension
                String bindingStyle = taskOptions.getBindingStyle();
                boolean validBinding = setActiveBindingStyle( bindingStyle );

                // Execute the compiler for the user-specified file
                String filename = commandLineArgs.getArgs()[0];
                File libraryFile = new File( System.getProperty( "user.dir" ), filename );

                if (!libraryFile.exists()) {
                    // If the relative path was not valid, try using the provided filename
                    // as an absolute path
                    libraryFile = new File( filename );
                }

                if (!validBinding) {
                    return;
                }

                if (libraryFile.exists()) {
                    CompileAllCompilerTask compilerTask = TaskFactory.getTask( CompileAllCompilerTask.class );
                    ValidationFindings findings;

                    compilerTask.applyTaskOptions( taskOptions );
                    findings = compilerTask.compileOutput( libraryFile );
                    logFindings( findings );

                } else {
                    throw new IOException(
                        MessageFormat.format( messageBundle.getString( "fileNotFound" ), filename ) );
                }

            } else {
                displayHelp();
            }

        } finally {
            out.flush();
        }
    }

    /**
     * Assigns the active binding style for the OTM compiler. If the specified binding style is valid this method will
     * return true; false otherwise.
     * 
     * @param bindingStyle the binding style to assign
     * @return boolean
     */
    private boolean setActiveBindingStyle(String bindingStyle) {
        boolean validBinding = true;

        if (bindingStyle != null) {
            if (CompilerExtensionRegistry.getAvailableExtensionIds().contains( bindingStyle )) {
                CompilerExtensionRegistry.setActiveExtension( bindingStyle );

            } else {
                out.println( MessageFormat.format( messageBundle.getString( "invalidBindingStyle" ), bindingStyle ) );
                validBinding = false;
            }
        }
        return validBinding;
    }

    /**
     * Logs the given set of validation findings. If no errors/warnings exist, no output will be produced.
     * 
     * @param findings the validation findings to log
     */
    private void logFindings(ValidationFindings findings) {
        String[] messages = findings.getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT );

        out.println( messageBundle.getObject( "validationFindings" ).toString() );

        for (String message : messages) {
            out.println( "  " + message );
        }
    }

    /**
     * Returns the command-line options for the OTA2 compiler.
     * 
     * @return Options
     */
    protected static Options getCommandLineOptions() {
        Options options = new Options();

        options.addOption( "b", "bindingStyle", true, messageBundle.getString( "bindingStyle" ) );
        options.addOption( "X", "compileXSD", false, messageBundle.getString( "compileXSD" ) );
        options.addOption( "W", "compileWSDL", false, messageBundle.getString( "compileWSDL" ) );
        options.addOption( "J", "compileJSON", false, messageBundle.getString( "compileJSON" ) );
        options.addOption( "S", "compileSwagger", false, messageBundle.getString( "compileSwagger" ) );
        options.addOption( "O", "compileOpenApi", false, messageBundle.getString( "compileOpenApi" ) );
        options.addOption( "H", "compileHTML", false, messageBundle.getString( "compileHTML" ) );
        options.addOption( "E", "generateExamples", false, messageBundle.getString( "generateExamples" ) );
        options.addOption( "C", "exampleContext", true, messageBundle.getString( "exampleContext" ) );
        options.addOption( "M", "exampleMaxDetails", false, messageBundle.getString( "exampleMaxDetails" ) );
        options.addOption( "r", "exampleMaxRepeat", true, messageBundle.getString( "exampleMaxRepeat" ) );
        options.addOption( "D", "exampleMaxDepth", true, messageBundle.getString( "exampleMaxDepth" ) );
        options.addOption( "s", "serviceEndpoint", true, messageBundle.getString( "serviceEndpoint" ) );
        options.addOption( "p", "resourceBasePath", true, messageBundle.getString( "resourceBasePath" ) );
        options.addOption( "e", "suppressOtmExtensions", false, messageBundle.getString( "suppressOtmExtensions" ) );
        options.addOption( "o", "suppressOptional", false, messageBundle.getString( "suppressOptional" ) );
        options.addOption( "d", "dir", true, messageBundle.getString( "dir" ) );
        options.addOption( "c", "catalog", true, messageBundle.getString( "catalog" ) );
        return options;
    }

    /**
     * Returns true if the given command-line arguments are valid for use with this compiler instance (false otherwise).
     * 
     * @param commandLineArgs the command-line arguments for the compilation
     * @return boolean
     */
    public boolean validateCommandLine(CommandLine commandLineArgs) {
        return (commandLineArgs.getArgs().length == 1); // only one filename allowed
    }

    /**
     * Assigns the output stream to use for all displayed output (default is system-out).
     * 
     * @param out the stream target for all CLI output
     */
    @SuppressWarnings("squid:S106")
    public void setOutput(OutputStream out) {
        this.out = new PrintWriter( (out == null) ? System.out : out );
    }

    /**
     * Displays the command-line help information.
     */
    protected void displayHelp() {
        new HelpFormatter().printHelp( out, 80, SCRIPT_SYNTAX, messageBundle.getString( "helpHeader" ),
            getCommandLineOptions(), 2, 2, null );
    }

    /**
     * Returns the root-cause exception for the given throwable object.
     * 
     * @param t the throwable for which to return the root cause
     * @return Throwable
     */
    protected static Throwable getRootCauseException(Throwable t) {
        Throwable rootCause = t;

        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    /**
     * Initializes the resource bundle containing user-displayable messages.
     */
    static {
        try {
            messageBundle = new PropertyResourceBundle( Main.class.getResourceAsStream( MESSAGE_RB ) );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

    /**
     * Initializes the default extension for the schema compiler.
     */
    static {
        try {
            // Force the load of the extension registry class and the initialization of the default
            // compiler extension (as determined by the local configuration file).
            CompilerExtensionRegistry.getActiveExtension();

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
