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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleImport;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.loader.LoaderValidationMessageKeys;
import org.opentravel.schemacompiler.transform.util.SchemaUtils;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2001.xmlschema.Schema;

/**
 * Base class for <code>LibraryModuleLoader</code> implementations that obtain their content from an
 * <code>InputStream</code> source.
 * 
 * @author S. Livezey
 */
public abstract class AbstractLibraryModuleLoader implements LibraryModuleLoader<InputStream>,
        LoaderValidationMessageKeys {

    protected static final Logger log = LoggerFactory
            .getLogger(LibrarySchema1_3_ModuleLoader.class);

    private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema";

    private static javax.xml.validation.Schema schemaValidationSchema;
    private static JAXBContext jaxbContext;

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#newInputSource(java.net.URL)
     */
    public LibraryInputSource<InputStream> newInputSource(URL libraryUrl) {
        return new LibraryStreamInputSource(libraryUrl);
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#isLibraryInputSource(org.opentravel.schemacompiler.loader.LibraryInputSource)
     */
    @Override
    public boolean isLibraryInputSource(LibraryInputSource<InputStream> inputSource) {
        String urlPath = inputSource.getLibraryURL().getPath();
        return !urlPath.toLowerCase().endsWith(".xsd");
    }

    /**
     * Loads the content of the JAXB content from the specified input source. If a validation schema
     * is provided, it will be used to validate the XML content of the file.
     * 
     * @param inputSource
     *            the input source from which to load module's content
     * @param validationFindings
     *            the validation errors/warnings discovered during the loading process
     * @param jaxbContext
     *            the JAXB context to use for unmarshalling the XML library content
     * @param validationSchema
     *            the validation schema to apply to the XML library content (may be null)
     * @return Object
     * @throws LibraryLoaderException
     *             thrown if a system-level exception occurs
     * @throws JAXBException
     *             thrown if the XML content is not readable
     */
    protected Object loadLibrary(LibraryInputSource<InputStream> inputSource,
            ValidationFindings validationFindings, JAXBContext jaxbContext,
            javax.xml.validation.Schema validationSchema) throws LibraryLoaderException,
            JAXBException {
        URL libraryUrl = (inputSource == null) ? null : inputSource.getLibraryURL();
        InputStream is = (inputSource == null) ? null : inputSource.getLibraryContent();
        Object jaxbLibrary = null;

        try {
            if (is != null) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                if (validationSchema != null) {
                    unmarshaller.setSchema(validationSchema);
                }

                JAXBElement<?> documentElement = (JAXBElement<?>) unmarshaller
                        .unmarshal( is );
                jaxbLibrary = documentElement.getValue();

            } else {
                validationFindings.addFinding(
                        FindingType.WARNING,
                        new URLValidationSource(libraryUrl),
                        WARNING_LIBRARY_NOT_FOUND,
                        (libraryUrl == null) ? "[MISSING URL]" : URLUtils
                                .getShortRepresentation(libraryUrl));
            }
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Throwable t) {
            }
        }
        return jaxbLibrary;
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#loadSchema(org.opentravel.schemacompiler.loader.LibraryInputSource,
     *      org.opentravel.schemacompiler.validate.ValidationFindings)
     */
    @Override
    public LibraryModuleInfo<Schema> loadSchema(LibraryInputSource<InputStream> inputSource,
            ValidationFindings validationFindings) throws LibraryLoaderException {
        URL schemaUrl = (inputSource == null) ? null : inputSource.getLibraryURL();
        LibraryModuleInfo<Schema> moduleInfo = null;

        try {
            InputStream is = (inputSource == null) ? null : inputSource.getLibraryContent();
            Schema schema = null;

            try {
                if (is != null) {
                    XMLStreamReader xmlsReader = XMLInputFactory.newInstance()
                            .createXMLStreamReader(inputSource.getLibraryContent());
                    Map<String, String> namespacePrefixMappings = new HashMap<String, String>();
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                    unmarshaller.setSchema(schemaValidationSchema);
                    schema = (Schema) unmarshaller.unmarshal(new PrefixMappingXMLStreamReader(
                            xmlsReader, namespacePrefixMappings));

                    // Use the schema's ID field to store the prefix value
                    schema.setId(namespacePrefixMappings.get(schema.getTargetNamespace()));

                    moduleInfo = buildModuleInfo(schema);

                } else {
                    validationFindings.addFinding(
                            FindingType.WARNING,
                            new URLValidationSource(schemaUrl),
                            WARNING_SCHEMA_NOT_FOUND,
                            (schemaUrl == null) ? "[MISSING URL]" : URLUtils
                                    .getShortRepresentation(schemaUrl));
                }
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (Throwable t) {
                }
            }
        } catch (XMLStreamException e) {
            throw new LibraryLoaderException("Error creating XMLStreamReader instance.", e);

        } catch (JAXBException e) {
            String urlString = (schemaUrl == null) ? "[MISSING URL]" : URLUtils
                    .getShortRepresentation(schemaUrl);

            validationFindings.addFinding(FindingType.ERROR, new URLValidationSource(schemaUrl),
                    ERROR_UNREADABLE_SCHEMA_CONTENT, urlString, ExceptionUtils.getExceptionClass(e)
                            .getSimpleName(), ExceptionUtils.getExceptionMessage(e));
            log.debug("Error during JAXB parsing of content from URL: " + urlString, e);
        }

        return moduleInfo;
    }

    /**
     * Constructs the <code>LibraryModuleInfo</code> to be returned for the given schema.
     * 
     * @param schema
     *            the schema that was loaded
     * @return LibraryModuleInfo<Schema>
     */
    private LibraryModuleInfo<Schema> buildModuleInfo(Schema schema) {
        List<LibraryModuleImport> imports = SchemaUtils.getSchemaImports(schema);
        List<String> includes = SchemaUtils.getSchemaIncludes(schema);

        return new LibraryModuleInfo<Schema>(schema, null, schema.getTargetNamespace(), null,
                includes, imports);
    }

    /**
     * <code>XMLStreamReader</code> class used to examine XML parsing events during the
     * unmarshalling process, and capture the namespace/prefix mapping information from the
     * root-level 'schema' element of the XML document being parsed.
     * 
     * @author S. Livezey
     */
    private class PrefixMappingXMLStreamReader extends StreamReaderDelegate {

        private Map<String, String> namespacePrefixMappings;

        public PrefixMappingXMLStreamReader(XMLStreamReader reader,
                Map<String, String> namespacePrefixMappings) {
            super(reader);
            this.namespacePrefixMappings = namespacePrefixMappings;
        }

        public int next() throws XMLStreamException {
            int nextResult = super.next();

            if ((nextResult == START_ELEMENT) && getLocalName().equals("schema")) {
                for (int i = 0; i < getNamespaceCount(); i++) {
                    String namespaceURI = getNamespaceURI(i);
                    String namespacePrefix = getNamespacePrefix(i);
                    if (namespacePrefixMappings.get(namespaceURI) == null) {
                        namespacePrefixMappings.put(namespaceURI, namespacePrefix);
                    }
                }
            }
            return nextResult;
        }

    }

    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream schemaStream = SchemaDeclarations.SCHEMA_FOR_SCHEMAS.getContent();

            schemaFactory.setResourceResolver(new ClasspathResourceResolver());
            schemaValidationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
