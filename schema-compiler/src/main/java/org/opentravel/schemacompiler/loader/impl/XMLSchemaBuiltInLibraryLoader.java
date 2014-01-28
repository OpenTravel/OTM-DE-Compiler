package org.opentravel.schemacompiler.loader.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import org.opentravel.ns.ota2.librarymodel_v01_04.EnumXsdSimpleType;
import org.opentravel.schemacompiler.loader.BuiltInLibraryLoader;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.XSDSimpleType;

/**
 * Handles the construction of a built-in library for the W3C built-in library.
 * 
 * @author S. Livezey
 */
public class XMLSchemaBuiltInLibraryLoader implements BuiltInLibraryLoader {

    private static final URL XML_SCHEMA_LIBRARY_URL;
    private static final String XML_SCHEMA_LIBRARY_NAME = "XMLSchema";
    private static final List<LibraryMember> XML_SCHEMA_LIBRARY_MEMBERS;

    private String defaultPrefix = "xsd";

    /**
     * Returns the default prefix for the built-in library produced by this loader.
     * 
     * @return String
     */
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    /**
     * Assigns the default prefix for the built-in library produced by this loader.
     * 
     * @param defaultPrefix
     *            the default prefix to assign
     */
    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }

    /**
     * @see org.opentravel.schemacompiler.loader.BuiltInLibraryLoader#loadBuiltInLibrary()
     */
    @Override
    public BuiltInLibrary loadBuiltInLibrary() throws LibraryLoaderException {
        return new BuiltInLibrary(XMLConstants.W3C_XML_SCHEMA_NS_URI, XML_SCHEMA_LIBRARY_NAME,
                defaultPrefix, XML_SCHEMA_LIBRARY_URL, XML_SCHEMA_LIBRARY_MEMBERS);
    }

    /**
     * Initializes the constants required for the XML schema built-in library.
     */
    static {
        try {
            List<LibraryMember> members = new ArrayList<LibraryMember>();

            for (EnumXsdSimpleType xsdSimpleType : EnumXsdSimpleType.values()) {
                members.add(new XSDSimpleType(xsdSimpleType.value(), null));
            }
            XML_SCHEMA_LIBRARY_MEMBERS = members;
            XML_SCHEMA_LIBRARY_URL = new URL(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
