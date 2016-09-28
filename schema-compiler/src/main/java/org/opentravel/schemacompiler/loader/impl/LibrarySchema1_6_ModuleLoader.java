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
package org.opentravel.schemacompiler.loader.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.opentravel.ns.ota2.librarymodel_v01_06.Library;
import org.opentravel.ns.ota2.librarymodel_v01_06.NamespaceImport;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleImport;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.util.FileHintUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Default implementation of the <code>LibraryModuleLoader</code> that loads and parses XML content
 * from an input stream. Unless configured otherwise, a <code>CatalogNamespaceResolver</code> is
 * used to resolve library namespaces.
 * 
 * @author S. Livezey
 */
public class LibrarySchema1_6_ModuleLoader extends AbstractLibraryModuleLoader {

    public static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.librarymodel_v01_06";

    private static javax.xml.validation.Schema libraryValidationSchema;
    private static JAXBContext jaxbContext;

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#loadLibrary(org.opentravel.schemacompiler.loader.LibraryInputSource,
     *      org.opentravel.schemacompiler.validate.ValidationFindings)
     */
    public LibraryModuleInfo<Object> loadLibrary(LibraryInputSource<InputStream> inputSource,
            ValidationFindings validationFindings) throws LibraryLoaderException {
        URL libraryUrl = (inputSource == null) ? null : inputSource.getLibraryURL();
        LibraryModuleInfo<Object> moduleInfo = null;
        try {
            Library jaxbLibrary;

            try {
                jaxbLibrary = (Library) loadLibrary(inputSource, validationFindings, jaxbContext,
                        libraryValidationSchema);

            } catch (JAXBException e) {
                String urlString = (libraryUrl == null) ? "[MISSING URL]" : URLUtils
                        .getShortRepresentation(libraryUrl);

                // If we are not able to load the library with validation turned on, load it
                // with validation disabled and issue a loader warning. If the content is still
                // invalid with validation turned off, the file is completely unreadable. If that
                // is the case, a JAXB exception will be thrown and the last catch block of this
                // method will issue a loader validation error.
                try {
                    jaxbLibrary = (Library) loadLibrary(inputSource, validationFindings,
                            jaxbContext, null);

                    if (jaxbLibrary != null) {
                        validationFindings.addFinding(FindingType.WARNING, new URLValidationSource(
                                libraryUrl), WARNING_CORRUPT_LIBRARY_CONTENT, urlString,
                                ExceptionUtils.getExceptionClass(e).getSimpleName(), ExceptionUtils
                                        .getExceptionMessage(e));
                    }
                } catch (ClassCastException cce) {
                    JAXBException je = new JAXBException(
                            "The library file does not contain OTM v1.6 content.", cce);

                    je.setLinkedException(cce);
                    throw je;
                }
            }

            // Before returning, we need to normalize the namespace and library name values
            // since they are used for name lookups
            if (jaxbLibrary != null) {
                jaxbLibrary.setName((jaxbLibrary.getName() == null) ? null : jaxbLibrary.getName()
                        .trim());
                jaxbLibrary.setNamespace((jaxbLibrary.getNamespace() == null) ? null : jaxbLibrary
                        .getNamespace().trim());
                moduleInfo = buildModuleInfo(jaxbLibrary, libraryUrl);
            }

        } catch (JAXBException e) {
            String urlString = (libraryUrl == null) ? "[MISSING URL]" : URLUtils
                    .getShortRepresentation(libraryUrl);

            validationFindings.addFinding(FindingType.ERROR, new URLValidationSource(libraryUrl),
                    ERROR_UNREADABLE_LIBRARY_CONTENT, urlString, ExceptionUtils
                            .getExceptionClass(e).getSimpleName(), ExceptionUtils
                            .getExceptionMessage(e));
        }
        return moduleInfo;
    }

    /**
     * Constructs the <code>LibraryModuleInfo</code> to be returned for the given schema.
     * 
     * @param library
     *            the library that was loaded
     * @param libraryUrl
     *            the URL location of the library that was loaded
     * @return LibraryModuleInfo
     */
    private LibraryModuleInfo<Object> buildModuleInfo(Library library, URL libraryUrl) {
        List<LibraryModuleImport> imports = new ArrayList<LibraryModuleImport>();
        List<String> includes = library.getIncludes();

        for (NamespaceImport nsImport : library.getImport()) {
            imports.add(new LibraryModuleImport(nsImport.getNamespace(), nsImport.getPrefix(),
                    FileHintUtils.resolveHints(nsImport.getFileHints(), libraryUrl)));
        }
        handleObsoleteBuiltIn( imports );
        
        return new LibraryModuleInfo<Object>(library, library.getName(), library.getNamespace(),
                library.getVersionScheme(), includes, imports);
    }

    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream schemaStream = SchemaDeclarations.OTA2_LIBRARY_SCHEMA_1_6.getContent(
            		CodeGeneratorFactory.XSD_TARGET_FORMAT);

            schemaFactory.setResourceResolver(new ClasspathResourceResolver());
            libraryValidationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
