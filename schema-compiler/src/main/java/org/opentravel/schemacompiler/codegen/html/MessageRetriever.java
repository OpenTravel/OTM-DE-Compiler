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

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;



/**
 * Retrieve and format messages stored in a resource.
 *
 * <p>
 * This code is not part of an API. It is implementation that is subject to change. Do not use it as an API
 *
 * @since 1.2
 * @author Atul M Dambalkar
 * @author Robert Field
 */
public class MessageRetriever {
    /**
     * The global configuration information for this run.
     */
    private final Configuration configuration;

    /**
     * The location from which to lazily fetch the resource..
     */
    private final String resourcelocation;

    /**
     * The lazily fetched resource..
     */
    private ResourceBundle messageBundle;

    /**
     * Initilize the ResourceBundle with the given resource.
     *
     * @param configuration the configuration
     * @param resourcelocation Resource.
     */
    public MessageRetriever(Configuration configuration, String resourcelocation) {
        this.configuration = configuration;
        this.resourcelocation = resourcelocation;
    }

    /**
     * Get and format message string from resource
     *
     * @param key selects message from resource
     * @param args arguments to be replaced in the message.
     * @return String
     */
    public String getText(String key, Object... args) {
        if (messageBundle == null) {
            try {
                messageBundle = ResourceBundle.getBundle( resourcelocation );
            } catch (MissingResourceException e) {
                throw new SchemaCompilerRuntimeException(
                    "Fatal: Resource (" + resourcelocation + ") for javadoc doclets is missing." );
            }
        }
        String messageText;

        try {
            String message = messageBundle.getString( key );
            messageText = MessageFormat.format( message, args );

        } catch (MissingResourceException e) {
            messageText = key;
        }
        return messageText;
    }

    /**
     * Print error message, increment error count.
     *
     * @param pos the position of the source
     * @param msg message to print
     */
    private void printError(SourcePosition pos, String msg) {
        configuration.printError( pos, msg );
    }

    /**
     * Print error message, increment error count.
     *
     * @param msg message to print
     */
    private void printError(String msg) {
        configuration.printError( msg );
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param pos the position of the source
     * @param msg message to print
     */
    private void printWarning(SourcePosition pos, String msg) {
        configuration.printWarning( pos, msg );
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param msg message to print
     */
    private void printWarning(String msg) {
        configuration.printWarning( msg );
    }

    /**
     * Print a message.
     *
     * @param pos the position of the source
     * @param msg message to print
     */
    private void printNotice(SourcePosition pos, String msg) {
        configuration.printNotice( pos, msg );
    }



    /**
     * Print a message.
     *
     * @param msg message to print
     */
    private void printNotice(String msg) {
        configuration.printNotice( msg );
    }

    /**
     * Print error message, increment error count.
     *
     * @param pos the position of the source
     * @param key selects message from resource
     * @param args arguments to be replaced in the message.
     */
    public void error(SourcePosition pos, String key, Object... args) {
        printError( pos, getText( key, args ) );
    }

    /**
     * Print error message, increment error count.
     *
     * @param key selects message from resource
     * @param args arguments to be replaced in the message.
     */
    public void error(String key, Object... args) {
        printError( getText( key, args ) );
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param pos the position of the source
     * @param key selects message from resource
     * @param args arguments to be replaced in the message.
     */
    public void warning(SourcePosition pos, String key, Object... args) {
        printWarning( pos, getText( key, args ) );
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     * @param args arguments to be replaced in the message.
     */
    public void warning(String key, Object... args) {
        printWarning( getText( key, args ) );
    }

    /**
     * Print a message.
     *
     * @param pos the position of the source
     * @param key selects message from resource
     * @param args arguments to be replaced in the message.
     */
    public void notice(SourcePosition pos, String key, Object... args) {
        printNotice( pos, getText( key, args ) );
    }

    /**
     * Print a message.
     *
     * @param key selects message from resource
     * @param args arguments to be replaced in the message.
     */
    public void notice(String key, Object... args) {
        printNotice( getText( key, args ) );
    }
}
