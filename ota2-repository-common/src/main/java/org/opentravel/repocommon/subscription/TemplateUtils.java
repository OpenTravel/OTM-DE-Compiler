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

package org.opentravel.repocommon.subscription;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility methods for use during Velocity template processing.
 */
public class TemplateUtils {

    private static final String RB_LOCATION = "org.opentravel.notification.notification_messages";

    /**
     * Applies URL encoding to the given string.
     * 
     * @param str the string to be URL encoded
     * @return String
     */
    public String urlEncode(String str) {
        String result;
        try {
            result = URLEncoder.encode( str, "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            result = str;
        }
        return result;
    }

    /**
     * Returns the display value by performing a resource bundle lookup of the given string. If no such key exists in
     * the resource bundle, the original string will be returned.
     * 
     * @param str the string from which to lookup a localized display value
     * @return String
     */
    public String getDisplayValue(String str) {
        ResourceBundle displayLabels = ResourceBundle.getBundle( RB_LOCATION, Locale.getDefault() );
        String displayValue;

        try {
            displayValue = (str == null) ? null : displayLabels.getString( str );

        } catch (MissingResourceException e) {
            displayValue = str;
        }
        return displayValue;
    }

    /**
     * Returns a user-displayable message for the given action.
     * 
     * @param action the repository action type
     * @param params the message parameters
     * @return String
     */
    public String getActionMessage(RepositoryActionType action, Object... params) {
        String messageTemplate = getDisplayValue( action + ".message" );

        return MessageFormat.format( messageTemplate, params );
    }

}
