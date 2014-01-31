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

import java.util.ResourceBundle;

/**
 * Enumeration of the various types of validation findings.
 * 
 * @author S. Livezey
 */
public enum FindingType {

    /** Finding type for errors. */
    ERROR("schemacompiler.findingType.ERROR", "Error"),

    /** Finding type for warnings. */
    WARNING("schemacompiler.findingType.WARNING", "Warning");

    private String resourceKey;
    private String displayName;

    /**
     * Constructor that assigns the resource key and display name for an enum value.
     * 
     * @param resourceKey
     *            the resource key for the value
     * @param displayName
     *            the display name for the value
     */
    private FindingType(String resourceKey, String displayName) {
        this.resourceKey = resourceKey;
        this.displayName = displayName;
    }

    /**
     * Returns the resource key for the value.
     * 
     * @return String
     */
    public String getResourceKey() {
        return resourceKey;
    }

    /**
     * Returns the display name for the value.
     * 
     * @return String
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the display name for the value.
     * 
     * @param bundle
     *            the resource bundle to use for display name lookups
     * @return String
     */
    public String getDisplayName(ResourceBundle bundle) {
        String result = displayName;

        if ((bundle != null) && bundle.containsKey(resourceKey)) {
            result = bundle.getString(resourceKey);
        }
        return result;
    }

}
