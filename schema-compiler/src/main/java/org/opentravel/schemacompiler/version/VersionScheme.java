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
import java.util.List;

/**
 * Defines the contract for the encoding and management of version schemes as part of an encoded
 * namespace URI.
 * 
 * @author S. Livezey
 */
public interface VersionScheme {

    /**
     * Returns a version identifier for this scheme using the information provided.
     * 
     * @param majorVersion
     *            the major version of the versioned component
     * @param minorVersion
     *            the minor version of the versioned component
     * @param patchLevel
     *            the patch level of the versioned component
     * @return String
     */
    public String getVersionIdentifier(String majorVersion, String minorVersion, String patchLevel);

    /**
     * Returns the version identifier string encoded into the given namespace URI, or null if a
     * version identifier is not specified on the namespace. Null will also be returned if the given
     * URI does not conform to the format required by the version scheme.
     * 
     * @param namespaceUri
     *            the namespace URI from which to obtain the version identifier
     * @return String
     */
    public String getVersionIdentifier(String namespaceUri);

    /**
     * Updates the version identifer string that is encoded into the given namespace URI.
     * 
     * @param namespaceUri
     *            the namespace URI to be updated
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     * @throws IllegalArgumentException
     *             thrown if either the namespace URI or version identifier are not valid for the
     *             version scheme
     */
    public String setVersionIdentifier(String namespaceUri, String versionIdentifier);

    /**
     * Returns the default version identifier for this version scheme (e.g. "1.0.0").
     * 
     * @return String
     */
    public String getDefaultVersionIdentifer();

    /**
     * Returns a new namespace prefix using the version information provided.
     * 
     * @param prefix
     *            the original prefix from which to derive the base prefix string
     * @param versionIdentifier
     *            the version identifier to assign for the new prefix
     * @return String
     */
    public String getPrefix(String prefix, String versionIdentifier);

    /**
     * Returns the major version component of the given version identifier string.
     * 
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     */
    public String getMajorVersion(String versionIdentifer);

    /**
     * Returns the minor version component of the given version identifier string.
     * 
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     */
    public String getMinorVersion(String versionIdentifer);

    /**
     * Returns the patch level component of the given version identifier string.
     * 
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     */
    public String getPatchLevel(String versionIdentifer);

    /**
     * Returns the base namespace (without the version identification component) for the given URI.
     * 
     * @param namespaceUri
     *            the namespace URI for which to return the base
     * @return String
     * @throws IllegalArgumentException
     *             thrown if the namespace URI is not valid for the version scheme
     */
    public String getBaseNamespace(String namespaceUri);

    /**
     * Returns the major-version namespace of the given library.
     * 
     * @param library
     *            the library for which to return the major-version namespace
     * @return String
     */
    public String getMajorVersionNamespace(String namespaceUri);

    /**
     * Returns true if the given namespace URI conforms to the formatting required by this version
     * scheme.
     * 
     * @param namespaceUri
     *            the namespace URI to validate
     * @return boolean
     */
    public boolean isValidNamespace(String namespaceUri);

    /**
     * Returns true if the given version identifier conforms to the formatting required by this
     * version scheme.
     * 
     * @param patchLevel
     *            the patch-level string to validate
     * @return boolean
     */
    public boolean isValidVersionIdentifier(String versionIdentifier);

    /**
     * Returns true if the given namespace URI represents a major version of the base namespace.
     * 
     * @param namespaceUri
     *            the namespace URI to analyze
     * @return boolean
     */
    public boolean isMajorVersion(String namespaceUri);

    /**
     * Returns true if the given namespace URI represents a minor version of the base namespace.
     * 
     * @param namespaceUri
     *            the namespace URI to analyze
     * @return boolean
     */
    public boolean isMinorVersion(String namespaceUri);

    /**
     * Returns true if the given namespace URI represents a patch version of the base namespace.
     * 
     * @param namespaceUri
     *            the namespace URI to analyze
     * @return boolean
     */
    public boolean isPatchVersion(String namespaceUri);

    /**
     * Returns a comparator that is capable of sorting <code>Versioned</code> objects that are
     * assigned to this version scheme.
     * 
     * @param sortAscending
     *            indicates the direction of the sort produced by the comparator (true = ascending,
     *            false = descending)
     * @return Comparator<Versioned>
     */
    public Comparator<Versioned> getComparator(boolean sortAscending);

    /**
     * Increments the major version number of the given version identifier.
     * 
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     */
    public String incrementMajorVersion(String versionIdentifer);

    /**
     * Decrements the major version number of the given version identifier.
     * 
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     */
    public String decrementMajorVersion(String versionIdentifer);

    /**
     * Increments the minor version number of the given version identifier.
     * 
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     */
    public String incrementMinorVersion(String versionIdentifer);

    /**
     * Decrements the minor version number of the given version identifier.
     * 
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     */
    public String decrementMinorVersion(String versionIdentifer);

    /**
     * Increments the patch-level number of the given version identifier.
     * 
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     */
    public String incrementPatchLevel(String versionIdentifer);

    /**
     * Decrements the patch-level number of the given version identifier.
     * 
     * @param versionIdentifier
     *            the un-encoded version identifier as returned by the 'getVersionIdentifer()'
     *            method
     * @return String
     */
    public String decrementPatchLevel(String versionIdentifer);

    /**
     * Returns the list of namespace URI's that represent the major version chain for the given
     * namespace. The version chain consists of all previous versions up to and including the major
     * version that originated the chain.
     * 
     * <p>
     * For example, the version chain for version <code>2.3.2</code> is: <code>2.3.2</code>,
     * <code>2.3.1</code>, <code>2.3.0</code>, <code>2.2.0</code>, <code>2.1.0</code>,
     * <code>2.0.0</code>.
     * 
     * <p>
     * Versions such as <code>2.0.1</code> or <code>2.2.5</code> would not be included in this chain
     * (even if they exist) because they would be assumed to be "rolled up" into versions that are
     * part of the chain.
     * 
     * @param namespaceUri
     * @return List<String>
     */
    public List<String> getMajorVersionChain(String namespaceUri);

    /**
     * Returns a default file hint for a library with the specified name and version identifier.
     * 
     * @param namespaceUri
     *            the namespace to which the user-defined library is assigned
     * @param libraryName
     *            the name of the user-defined library
     * @return String
     */
    public String getDefaultFileHint(String namespaceUri, String libraryName);

}
