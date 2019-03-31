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

package org.opentravel.schemacompiler.diff;


/**
 * Describes a single change identified during comparison of two OTM fields.
 */
public class FieldChangeItem extends ChangeItem<FieldChangeType> {

    private FieldChangeSet changeSet;

    /**
     * Constructor used when a field value was changed.
     * 
     * @param changeSet the change set to which this item belongs
     * @param changeType the type of field change
     * @param oldValue the affected value from the old version
     * @param newValue the affected value from the new version
     */
    public FieldChangeItem(FieldChangeSet changeSet, FieldChangeType changeType, String oldValue, String newValue) {
        this.changeSet = changeSet;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the change set to which this item belongs.
     *
     * @return FieldChangeSet
     */
    public FieldChangeSet getChangeSet() {
        return changeSet;
    }

}
