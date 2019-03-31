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

package org.opentravel.schemacompiler.codegen.html;

import org.opentravel.schemacompiler.util.SchemaCompilerRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility for integrating with javadoc tools and for localization. Handle Resources. Access to error and warning
 * counts. Message formatting. <br>
 * Also provides implementation for DocErrorReporter.
 *
 * @see java.util.ResourceBundle
 * @see java.text.MessageFormat
 * @author Neal Gafter (rewrite)
 */
public class Messager {

    private static final Logger log = LoggerFactory.getLogger( Messager.class );

    /**
     * The maximum number of errors/warnings that are reported.
     */
    public final int maxErrors;
    public final int maxWarnings;

    /**
     * The number of errors encountered so far.
     */
    private int nerrors = 0;

    /**
     * The number of warnings encountered so far.
     */
    private int nwarnings = 0;

    /**
     * Switch: prompt user on each error.
     */
    private boolean promptOnError;

    public class ExitJavadoc extends Error {
        private static final long serialVersionUID = 0;
    }

    final String programName;

    private ResourceBundle messageBundle = null;

    /**
     * Constructor
     * 
     * @param programName Name of the program (for error messages).
     */
    public Messager(String programName) {
        this.programName = programName;
        this.maxErrors = getDefaultMaxErrors();
        this.maxWarnings = getDefaultMaxWarnings();
    }

    protected int getDefaultMaxErrors() {
        return Integer.MAX_VALUE;
    }


    protected int getDefaultMaxWarnings() {
        return Integer.MAX_VALUE;
    }

    /**
     * Get string from ResourceBundle, initialize ResourceBundle if needed.
     */
    private String getString(String key) {
        if (messageBundle == null) {
            try {
                messageBundle =
                    ResourceBundle.getBundle( "org.opentravel.schemacompiler.codegen.html.resources.javadoc" );
            } catch (MissingResourceException e) {
                throw new SchemaCompilerRuntimeException( "Fatal: Resource for javadoc is missing", e );
            }
        }
        return messageBundle.getString( key );
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     */
    String getText(String key) {
        return getText( key, (String) null );
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     */
    String getText(String key, String a1) {
        return getText( key, a1, null );
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     */
    String getText(String key, String a1, String a2) {
        return getText( key, a1, a2, null );
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    String getText(String key, String a1, String a2, String a3) {
        return getText( key, a1, a2, a3, null );
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     * @param a4 fourth argument
     */
    String getText(String key, String a1, String a2, String a3, String a4) {
        try {
            String message = getString( key );
            String[] args = new String[4];
            args[0] = a1;
            args[1] = a2;
            args[2] = a3;
            args[3] = a4;
            return MessageFormat.format( message, (Object[]) args );
        } catch (MissingResourceException e) {
            return "********** Resource for javadoc is broken. There is no " + key + " key in resource.";
        }
    }

    /**
     * Print error message, increment error count. Part of DocErrorReporter.
     *
     * @param msg message to print
     */
    public void printError(String msg) {
        printError( null, msg );
    }

    /**
     * Print error message, increment error count. Part of DocErrorReporter.
     *
     * @param pos the position where the error occurs
     * @param msg message to print
     */
    public void printError(SourcePosition pos, String msg) {
        if (log.isErrorEnabled() && (getNerrors() < maxErrors)) {
            String prefix = (pos == null) ? programName : pos.toString();
            log.error( String.format( "%s: %s - %s", prefix, getText( "javadoc.error" ), msg ) );
            prompt();
            setNerrors( getNerrors() + 1 );
        }
    }

    /**
     * Print warning message, increment warning count. Part of DocErrorReporter.
     *
     * @param msg message to print
     */
    public void printWarning(String msg) {
        printWarning( null, msg );
    }

    /**
     * Print warning message, increment warning count. Part of DocErrorReporter.
     *
     * @param pos the position where the error occurs
     * @param msg message to print
     */
    public void printWarning(SourcePosition pos, String msg) {
        if (log.isWarnEnabled() && (getNwarnings() < maxWarnings)) {
            String prefix = (pos == null) ? programName : pos.toString();
            log.warn( String.format( "%s: %s - %s", prefix, getText( "javadoc.warning" ), msg ) );
            setNwarnings( getNwarnings() + 1 );
        }
    }

    /**
     * Print a message. Part of DocErrorReporter.
     *
     * @param msg message to print
     */
    public void printNotice(String msg) {
        printNotice( null, msg );
    }

    /**
     * Print a message. Part of DocErrorReporter.
     *
     * @param pos the position where the error occurs
     * @param msg message to print
     */
    public void printNotice(SourcePosition pos, String msg) {
        if (log.isInfoEnabled()) {
            if (pos == null) {
                log.info( msg );
            } else {
                log.info( String.format( "%s: %s", pos, msg ) );
            }
        }
    }

    /**
     * Print error message, increment error count.
     *
     * @param pos the source position where the error occurred
     * @param key selects message from resource
     */
    public void error(SourcePosition pos, String key) {
        printError( pos, getText( key ) );
    }

    /**
     * Prompt user after an error.
     */
    public void prompt() {
        if (isPromptOnError()) {
            log.error( "resume.abort" );// localize?
            try {
                while (true) {
                    switch (System.in.read()) {
                        case 'a':
                        case 'A':
                            System.exit( -1 );
                            return;
                        case 'r':
                        case 'R':
                            return;
                        case 'x':
                        case 'X':
                            throw new AssertionError( "user abort" );
                        default:
                    }
                }
            } catch (IOException e) {
                log.warn( "Error reading from standard input." );
            }
        }
    }

    /**
     * Returns the value of the 'nerrors' field.
     *
     * @return int
     */
    public int getNerrors() {
        return nerrors;
    }

    /**
     * Assigns the value of the 'nerrors' field.
     *
     * @param nerrors the field value to assign
     */
    public void setNerrors(int nerrors) {
        this.nerrors = nerrors;
    }

    /**
     * Returns the value of the 'nwarnings' field.
     *
     * @return int
     */
    public int getNwarnings() {
        return nwarnings;
    }

    /**
     * Assigns the value of the 'nwarnings' field.
     *
     * @param nwarnings the field value to assign
     */
    public void setNwarnings(int nwarnings) {
        this.nwarnings = nwarnings;
    }

    /**
     * Returns the value of the 'promptOnError' field.
     *
     * @return boolean
     */
    public boolean isPromptOnError() {
        return promptOnError;
    }

    /**
     * Assigns the value of the 'promptOnError' field.
     *
     * @param promptOnError the field value to assign
     */
    public void setPromptOnError(boolean promptOnError) {
        this.promptOnError = promptOnError;
    }
}
