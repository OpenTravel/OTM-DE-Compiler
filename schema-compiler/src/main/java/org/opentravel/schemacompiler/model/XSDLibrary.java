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
package org.opentravel.schemacompiler.model;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;

/**
 * Library that encapsulates the declarations of legacy XML schema (XSD) files.
 * 
 * @author S. Livezey
 */
public class XSDLibrary extends AbstractLibrary {

    private static final Set<Class<?>> validMemberTypes;

    private String libraryName;

    /**
     * Extends the base class behavior by assigning this library to the pseudo-namespace used for
     * chameleon schemas if the given namespace is empty or null.
     * 
     * @see org.opentravel.schemacompiler.model.AbstractLibrary#setNamespace(java.lang.String)
     */
    @Override
    public void setNamespace(String namespace) {
        if ((namespace == null) || (namespace.length() == 0)) {
            super.setNamespace(AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE);
        } else {
            super.setNamespace(namespace);
        }
    }

    /**
     * Returns true if this library schema is a chameleon.
     * 
     * @return boolean
     */
    public boolean isChameleon() {
        return AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE.equals(getNamespace());
    }

    /**
     * Returns the XSD simple type with the specified name.
     * 
     * @param localName
     *            the local name of the XSD simple type to return
     * @return XSDSimpleType
     */
    public XSDSimpleType getSimpleType(String localName) {
        LibraryMember member = getNamedMember(localName);
        return (member instanceof XSDSimpleType) ? (XSDSimpleType) member : null;
    }

    /**
     * Returns the XSD complex type with the specified name.
     * 
     * @param localName
     *            the local name of the XSD complex type to return
     * @return XSDComplexType
     */
    public XSDComplexType getComplexType(String localName) {
        LibraryMember member = getNamedMember(localName);
        return (member instanceof XSDComplexType) ? (XSDComplexType) member : null;
    }

    /**
     * Returns the XSD element with the specified name.
     * 
     * @param localName
     *            the local name of the XSD element to return
     * @return XSDElement
     */
    public XSDElement getElement(String localName) {
        LibraryMember member = getNamedMember(localName);
        return (member instanceof XSDElement) ? (XSDElement) member : null;
    }

    /**
     * Returns true if the given item will be considered a valid member of an XML schema library
     * instance.
     * 
     * @param namedMember
     *            the candidate member to analyze
     * @return boolean
     */
    public static boolean isValidLibraryMember(LibraryMember namedMember) {
        return (namedMember != null) && validMemberTypes.contains(namedMember.getClass());
    }

    /**
     * @see org.opentravel.schemacompiler.model.AbstractLibrary#isValidMember(org.opentravel.schemacompiler.model.LibraryMember)
     */
    @Override
    protected boolean isValidMember(LibraryMember namedMember) {
        return isValidLibraryMember( namedMember );
    }

    /**
     * Returns the value of the 'name' field.
     * 
     * @return String
     */
    @Override
    public String getName() {
        return libraryName;
    }

    /**
     * Assigns the value of the 'name' field.
     * 
     * @param name
     *            the field value to assign
     */
    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Operation not supported for XML schema libraries.");
    }

    /**
     * @see org.opentravel.schemacompiler.model.AbstractLibrary#setLibraryUrl(java.net.URL)
     */
    @Override
    public void setLibraryUrl(URL libraryUrl) {
        String[] pathParts = libraryUrl.getPath().split("/");
        String filePart = (pathParts.length == 0) ? "[UNKNOWN SCHEMA]"
                : pathParts[pathParts.length - 1];
        int dotIdx = filePart.lastIndexOf('.');

        if (dotIdx >= 0) {
            filePart = filePart.substring(0, dotIdx);
        }
        libraryName = filePart;
        super.setLibraryUrl(libraryUrl);
    }

    /**
     * Initializes the list of valid member types for this library.
     */
    static {
        try {
            Set<Class<?>> validTypes = new HashSet<>();

            validTypes.add(XSDSimpleType.class);
            validTypes.add(XSDComplexType.class);
            validTypes.add(XSDElement.class);
            validMemberTypes = Collections.unmodifiableSet(validTypes);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
