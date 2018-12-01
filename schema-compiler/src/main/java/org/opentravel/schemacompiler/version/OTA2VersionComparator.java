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
package org.opentravel.schemacompiler.version;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Comparator used to sort <code>Versioned</code> objects according to their version identifier.
 * 
 * @author S. Livezey
 */
public class OTA2VersionComparator implements Comparator<Versioned> {

    private Map<String,VersionIdentifier> identifierCache = new HashMap<>();
    private boolean sortAscending;

    /**
     * Constructor that creates a comparator to be used for ascending sorts.
     */
    public OTA2VersionComparator() {
        this(true);
    }

    /**
     * Constructor that creates a comparator to be used for either ascending or descending sorts.
     * 
     * @param sortAscending
     *            indicates the sort direction for the comparator
     */
    public OTA2VersionComparator(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Versioned obj1, Versioned obj2) {
        String version1 = (obj1 == null) ? null : obj1.getVersion();
        String version2 = (obj2 == null) ? null : obj2.getVersion();
        int result;

        if ((version1 == null) || (version2 == null)) {
            result = (version1 == null) ? ((version2 == null) ? 0 : -1) : 1;
        } else {
            VersionIdentifier versionId1 = getVersionIdentifier(version1);
            VersionIdentifier versionId2 = getVersionIdentifier(version2);

            result = versionId1.compareTo(versionId2);
        }
        return (sortAscending) ? result : -result;
    }

    /**
     * Returns a <code>VersionIdentifier</code> instance that can be used to perform comparisons of
     * the given version ID.
     * 
     * @param versionStr
     *            the version identifier string
     * @return VersionIdentifier
     */
    private VersionIdentifier getVersionIdentifier(String versionStr) {
        VersionIdentifier versionId = identifierCache.get(versionStr);

        if (versionId == null) {
            versionId = new VersionIdentifier(versionStr);
            identifierCache.put(versionStr, versionId);
        }
        return versionId;
    }

    /**
     * Encapsulates a single version identifier to use for comparison purposes.
     */
    private class VersionIdentifier implements Comparable<VersionIdentifier> {

        private int[] versionParts;

        public VersionIdentifier(String versionIdentifier) {
            this.versionParts = (versionIdentifier == null) ? new int[] { 0, 0, 0 }
                    : OTA2VersionScheme.splitVersionIdentifier(versionIdentifier);
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(VersionIdentifier that) {
            int result = 0;

            if (that == null) {
                result = 1; // simple case - always greater than a null object

            } else {
                for (int i = 0; i < versionParts.length; i++) {
                    if (this.versionParts[i] != that.versionParts[i]) {
                        result = (this.versionParts[i] < that.versionParts[i]) ? -1 : 1;
                        break;
                    }
                }
            }
            return result;
        }

    }

}
