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

package org.opentravel.reposervice.util;

import org.opentravel.schemacompiler.model.TLLibraryStatus;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Returns user-displayable labels for various types and enum values.
 */
public class MessageFormatter {

    private static final String SCHEMA_COMPILER_MESSAGES = "org/opentravel/schemacompiler/compiler-messages";
    private static ResourceBundle bundle = ResourceBundle.getBundle( SCHEMA_COMPILER_MESSAGES, Locale.getDefault() );

    /**
     * Returns the display name for the given entity type.
     * 
     * @param entityType the entity type for which to return a display name
     * @return String
     */
    public String getEntityTypeDisplayName(Class<?> entityType) {
        String displayName = null;
        try {
            if (entityType != null) {
                displayName = bundle.getString( entityType.getSimpleName() + ".displayName" );
            }

        } catch (MissingResourceException e) {
            displayName = entityType.getSimpleName();
        }
        return displayName;
    }

    /**
     * Returns the display name for the given library status.
     * 
     * @param status the library status for which to return a display name
     * @return String
     */
    public String getLibraryStatusDisplayName(TLLibraryStatus status) {
        String displayName = null;
        try {
            if (status != null) {
                displayName = bundle.getString( status.toString() );
            }

        } catch (MissingResourceException e) {
            displayName = status.toString();
        }
        return displayName;
    }

}
