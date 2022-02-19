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

import java.util.List;

/**
 * Represents a single selectable value for an HTML select form control.
 */
public class SelectOption {

    private String value;
    private String displayName;
    private boolean selected = false;

    /**
     * Constructor that specifies separate display and option values.
     * 
     * @param value the parameter value for the select control
     * @param displayName the display name for the option value
     */
    public SelectOption(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Assigns the selected value for the list of options.
     * 
     * @param options the list of options for which to assign a selection
     * @param selectedValue selected value (null for no selection)
     */
    public static void setSelectedValue(List<SelectOption> options, String selectedValue) {
        for (SelectOption option : options) {
            option.setSelected( (selectedValue != null) && selectedValue.equals( option.value ) );
        }
    }

    /**
     * Constructor that specifies identical display and option values.
     * 
     * @param value the parameter value for the select control
     */
    public SelectOption(String value) {
        this.value = this.displayName = value;
    }

    /**
     * Returns the parameter value for the select control.
     *
     * @return String
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the display name for the option value.
     *
     * @return String
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the flag indicating whether this value is currently selected.
     *
     * @return boolean
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Assigns the flag indicating whether this value is currently selected.
     *
     * @param selected the field value to assign
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Returns "selected" if selection is true and an empty string for false.
     * 
     * @return String
     */
    public String getSelectedTag() {
        return selected ? "selected" : "";
    }

}
