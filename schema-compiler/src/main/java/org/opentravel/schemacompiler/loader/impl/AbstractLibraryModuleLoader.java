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

import java.io.IOException;
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

import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleImport;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.loader.LoaderConstants;
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
public abstract class AbstractLibraryModuleLoader implements LibraryModuleLoader<InputStream> {

	protected static final Logger log = LoggerFactory.getLogger(AbstractLibraryModuleLoader.class);

	private static final String MISSING_URL = "[MISSING URL]";
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
        Object jaxbLibrary = null;

        try (InputStream is = (inputSource == null) ? null : inputSource.getLibraryContent()) {
            if (is != null) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                if (validationSchema != null) {
                    unmarshaller.setSchema(validationSchema);
                }

                JAXBElement<?> documentElement = (JAXBElement<?>) unmarshaller.unmarshal( is );
                jaxbLibrary = documentElement.getValue();

            } else {
                validationFindings.addFinding(
                        FindingType.WARNING,
                        new URLValidationSource(libraryUrl),
                        LoaderConstants.WARNING_LIBRARY_NOT_FOUND,
                        (libraryUrl == null) ? MISSING_URL : URLUtils
                                .getShortRepresentation(libraryUrl));
            }
        } catch (IOException e) {
        	throw new LibraryLoaderException("Error reading from library input source", e);
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

        try (InputStream is = (inputSource == null) ? null : inputSource.getLibraryContent()) {
            Schema schema = null;
            
            if (is != null) {
                XMLStreamReader xmlsReader = XMLInputFactory.newInstance()
                        .createXMLStreamReader(inputSource.getLibraryContent());
                Map<String,String> namespacePrefixMappings = new HashMap<>();
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
                        LoaderConstants.WARNING_SCHEMA_NOT_FOUND,
                        (schemaUrl == null) ? MISSING_URL : URLUtils
                                .getShortRepresentation(schemaUrl));
            }
                
        } catch (XMLStreamException e) {
            throw new LibraryLoaderException("Error creating XMLStreamReader instance.", e);

        } catch (IOException e) {
        	throw new LibraryLoaderException("Error reading from library input source", e);
        	
        } catch (JAXBException e) {
            String urlString = (schemaUrl == null) ? MISSING_URL : URLUtils
                    .getShortRepresentation(schemaUrl);

            validationFindings.addFinding(FindingType.ERROR, new URLValidationSource(schemaUrl),
                    LoaderConstants.ERROR_UNREADABLE_SCHEMA_CONTENT, urlString, ExceptionUtils.getExceptionClass(e)
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

        return new LibraryModuleInfo<>(schema, null, schema.getTargetNamespace(), null,
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

        @Override
        public int next() throws XMLStreamException {
            int nextResult = super.next();

            if ((nextResult == START_ELEMENT) && getLocalName().equals("schema")) {
                for (int i = 0; i < getNamespaceCount(); i++) {
                    String namespaceURI = getNamespaceURI(i);
                    String namespacePrefix = getNamespacePrefix(i);
                    
                    namespacePrefixMappings.putIfAbsent( namespaceURI, namespacePrefix );
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
            InputStream schemaStream = SchemaDeclarations.SCHEMA_FOR_SCHEMAS.getContent(
            		CodeGeneratorFactory.XSD_TARGET_FORMAT);

            schemaFactory.setResourceResolver(new ClasspathResourceResolver());
            schemaValidationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
