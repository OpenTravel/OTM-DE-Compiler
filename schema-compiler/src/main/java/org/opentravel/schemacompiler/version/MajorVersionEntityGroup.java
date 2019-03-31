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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Collection of all entities assigned to the same major-version namespace, regardless of whether or not they are owned
 * by the same library. If multiple minor versions of a particular entity exist, only the latest version will be
 * included in the major-version group.
 */
public class MajorVersionEntityGroup implements Comparable<MajorVersionEntityGroup> {

    private String baseNamespace;
    private String majorVersionNamespace;
    private String majorVersion;
    private int majorVersionOrdinal;
    private Map<String,Versioned> namedMembers = new HashMap<>();

    /**
     * Constructor that defines the namespace and version scheme of the named entity group.
     * 
     * @param majorVersionNamespace the major-version namespace assigned to all members of this group
     * @param versionScheme the version scheme to be applied for all members of this group
     */
    public MajorVersionEntityGroup(String majorVersionNamespace, VersionScheme versionScheme) {
        this.baseNamespace = versionScheme.getBaseNamespace( majorVersionNamespace );
        this.majorVersion = versionScheme.getVersionIdentifier( majorVersionNamespace );
        this.majorVersionOrdinal = getMajorVersionOrdinal( majorVersion );
        this.majorVersionNamespace = majorVersionNamespace;
    }

    /**
     * Returns the base namespace assigned to all members of this group.
     *
     * @return String
     */
    public String getBaseNamespace() {
        return baseNamespace;
    }

    /**
     * Returns the major-version namespace assigned to all members of this group.
     *
     * @return String
     */
    public String getMajorVersionNamespace() {
        return majorVersionNamespace;
    }

    /**
     * Returns the version identifier assigned to all members of this group.
     *
     * @return String
     */
    public String getMajorVersion() {
        return majorVersion;
    }

    /**
     * Returns the set of names for all entities assigned to this group's namespace.
     * 
     * @return Set&lt;String&gt;
     */
    public Set<String> getMemberNames() {
        return Collections.unmodifiableSet( namedMembers.keySet() );
    }

    /**
     * Returns the entity from this group with the specified member name. If no such entity exists, this method will
     * return null.
     * 
     * @param localName the local name of the member to retrieve
     * @return Versioned
     */
    public Versioned getNamedMember(String localName) {
        return namedMembers.get( localName );
    }

    /**
     * Adds the given named entity to this group.
     * 
     * @param member the named entity to add
     */
    public void addNamedMember(Versioned member) {
        String localName = member.getLocalName();
        Versioned existingEntity = namedMembers.get( localName );

        if ((existingEntity == null) || existingEntity.isLaterVersion( member )) {
            namedMembers.put( localName, member );
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MajorVersionEntityGroup otherGroup) {
        int result;

        if (otherGroup == null) {
            result = 1;

        } else {
            if (this.majorVersionOrdinal == otherGroup.majorVersionOrdinal) {
                result = 0;
            } else {
                result = (this.majorVersionOrdinal < otherGroup.majorVersionOrdinal) ? -1 : 1;
            }
        }
        return result;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (baseNamespace == null) ? 0 : baseNamespace.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MajorVersionEntityGroup) && (compareTo( (MajorVersionEntityGroup) obj ) == 0);
    }

    /**
     * Returns the ordinal value for the major version in the given version identifer.
     * 
     * @param versionIdentifier the version identifier for which to return the major version
     * @return int
     */
    private int getMajorVersionOrdinal(String versionIdentifier) {
        int dotIdx = versionIdentifier.indexOf( '.' );
        String majorVersionStr = (dotIdx < 0) ? versionIdentifier : versionIdentifier.substring( 0, dotIdx );

        return Integer.parseInt( majorVersionStr );
    }

}
