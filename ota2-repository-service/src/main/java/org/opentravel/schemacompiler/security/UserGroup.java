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
package org.opentravel.schemacompiler.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the name of a group and the user ID of each member assigned to it.
 * 
 * @author S. Livezey
 */
public class UserGroup {

    private String groupName;
    private List<String> memberIds = new ArrayList<String>();

    /**
     * Constructor that defines the name of the group and the user ID of each member assigned to it.
     * 
     * @param groupName
     *            the name of the group
     * @param memberIds
     *            the user ID of each member assigned to the group
     */
    public UserGroup(String groupName, List<String> memberIds) {
        if (memberIds != null) {
            this.memberIds = memberIds;
        }
        this.groupName = groupName;
    }

    /**
     * Returns the value of the 'groupName' field.
     * 
     * @return String
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Returns the value of the 'memberIds' field.
     * 
     * @return List<String>
     */
    public List<String> getMemberIds() {
        return memberIds;
    }

}
