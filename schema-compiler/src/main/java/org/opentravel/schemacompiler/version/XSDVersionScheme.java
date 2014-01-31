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
import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Version Schema for built-in xsd types.
 */
public class XSDVersionScheme implements VersionScheme {

    public static final String ID = "XSD";

    private static String getVersion() {
        return "1.0";
    }

    private static String getNamespace() {
        return "http://www.w3.org/2001/XMLSchema";
    }

    @Override
    public String getVersionIdentifier(String majorVersion, String minorVersion, String patchLevel) {
        return getVersion();
    }

    @Override
    public String getVersionIdentifier(String namespaceUri) {
        return getVersion();
    }

    @Override
    public String setVersionIdentifier(String namespaceUri, String versionIdentifier) {
        return getNamespace();
    }

    @Override
    public String getDefaultVersionIdentifer() {
        return getVersion();
    }

    @Override
    public String getPrefix(String prefix, String versionIdentifier) {
        return prefix;
    }

    @Override
    public String getMajorVersion(String versionIdentifer) {
        return getVersion();
    }

    @Override
    public String getMinorVersion(String versionIdentifer) {
        return getVersion();
    }

    @Override
    public String getPatchLevel(String versionIdentifer) {
        return getVersion();
    }

    @Override
    public String getBaseNamespace(String namespaceUri) {
        return getNamespace();
    }

    @Override
    public String getMajorVersionNamespace(String namespaceUri) {
        return getNamespace();
    }

    @Override
    public boolean isValidNamespace(String namespaceUri) {
        return true;
    }

    @Override
    public boolean isValidVersionIdentifier(String versionIdentifier) {
        return true;
    }

    @Override
    public boolean isMajorVersion(String namespaceUri) {
        return true;
    }

    @Override
    public boolean isMinorVersion(String namespaceUri) {
        return false;
    }

    @Override
    public boolean isPatchVersion(String namespaceUri) {
        return false;
    }

    @Override
    public Comparator<Versioned> getComparator(boolean sortAscending) {
        return new Comparator<Versioned>() {

            @Override
            public int compare(Versioned o1, Versioned o2) {
                return 0;
            }

        };
    }

    @Override
    public String incrementMajorVersion(String versionIdentifer) {
        return getVersion();
    }

    @Override
    public String decrementMajorVersion(String versionIdentifer) {
        return getVersion();
    }

    @Override
    public String incrementMinorVersion(String versionIdentifer) {
        return getVersion();
    }

    @Override
    public String decrementMinorVersion(String versionIdentifer) {
        return getVersion();
    }

    @Override
    public String incrementPatchLevel(String versionIdentifer) {
        return getVersion();
    }

    @Override
    public String decrementPatchLevel(String versionIdentifer) {
        return getVersion();
    }

    @Override
    public List<String> getMajorVersionChain(String namespaceUri) {
        return Collections.emptyList();
    }

    @Override
    public String getDefaultFileHint(String namespaceUri, String libraryName) {
        return libraryName + ".xsd";
    }

}
