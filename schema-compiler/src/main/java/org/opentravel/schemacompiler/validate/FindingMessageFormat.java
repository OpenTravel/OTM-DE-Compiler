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
package org.opentravel.schemacompiler.validate;

import java.util.Locale;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.springframework.context.NoSuchMessageException;

/**
 * Describes the overall format used to display error messages.
 * 
 * @author S. Livezey
 */
public enum FindingMessageFormat {

    IDENTIFIED_FORMAT("ValidationFindings.IdentityMessageFormat", "{0} [{1}]: {2}"), BARE_FORMAT(
            "ValidationFindings.BareMessageFormat", "{1}: {2}"), MESSAGE_ONLY_FORMAT(
            "ValidationFindings.MessageOnlyFormat", "{2}");

    /** The default message format value. */
    public static final FindingMessageFormat DEFAULT = FindingMessageFormat.IDENTIFIED_FORMAT;

    private String formatKey;
    private String defaultFormat;

    /**
     * Constructor that specifies the resource bundle key used to identify the formatter string, and
     * the default format string to use if the bundle does not explicitly specify a format.
     * 
     * @param formatKey
     *            the resource bundle key
     * @param defaultFormat
     *            the default format string to use if one is not explicitly defined
     */
    private FindingMessageFormat(String formatKey, String defaultFormat) {
        this.formatKey = formatKey;
        this.defaultFormat = defaultFormat;
    }

    /**
     * Returns the resource bundle key used to identify the formatter string.
     * 
     * @return String
     */
    public String getFormatKey() {
        return formatKey;
    }

    /**
     * Returns the the default formatter string.
     * 
     * @return String
     */
    public String getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * Returns the string to use for message formatting. If the bundle does not define a string for
     * the 'formatKey' value, the 'defaultFormat' will be returned.
     * 
     * @param bundle
     *            the resource bundle from which to retrieve the format string
     * @return String
     */
    public String getFormat() {
        String formatString;

        try {
            formatString = SchemaCompilerApplicationContext.getContext().getMessage(formatKey,
                    null, Locale.getDefault());

        } catch (NoSuchMessageException e) {
            formatString = defaultFormat;
        }
        return formatString;
    }

}
