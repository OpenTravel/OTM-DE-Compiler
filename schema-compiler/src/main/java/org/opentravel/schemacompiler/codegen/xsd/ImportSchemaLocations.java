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
package org.opentravel.schemacompiler.codegen.xsd;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.xml.NamespacePrefixMapper;
import org.w3._2001.xmlschema.FormChoice;
import org.w3._2001.xmlschema.Include;

/**
 * Optional component that allows the code generation orchestrator to pre-specify the names and
 * locations of schemas referenced by XSD import declarations.
 * 
 * @author S. Livezey
 */
public class ImportSchemaLocations {

    public static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema";

    protected static Schema validationSchema;
    protected static JAXBContext jaxbContext;

    private Map<String,File> namespaceSchemaLocations = new HashMap<>();
    private Map<String,String> namespacePrefixes = new HashMap<>();
    private Map<String,List<File>> originalSchemaLocations = new HashMap<>();
    private Set<String> importedNamespaces = new HashSet<>();
    private File baseOutputFolder;

    /**
     * Constructor that assigns the base output folder location of a code generation task.
     * 
     * @param baseOutputFolder
     *            the base folder location for all output produced by the code generator(s)
     */
    public ImportSchemaLocations(File baseOutputFolder) {
        this.baseOutputFolder = baseOutputFolder;
    }

    /**
     * Assigns a schema location for the specified namespace.
     * 
     * @param namespace
     *            the namespace of the associated schema file
     * @param preferredPrefix
     *            the preferred prefix to use when referring to the specified namespace
     * @param schemaLocation
     *            the file location of the schema file for the import
     */
    public void setSchemaLocation(String namespace, String preferredPrefix, File schemaLocation) {
        if ((namespace == null) || (schemaLocation == null)) {
            throw new IllegalArgumentException(
                    "The namespace and schema location values cannot be null.");
        }

        if (!namespaceSchemaLocations.containsKey(namespace)) {
            String uniquePrefix = getUniquePrefix(preferredPrefix);

            namespaceSchemaLocations.put(namespace, schemaLocation);
            namespacePrefixes.put(namespace, uniquePrefix);

        } else {
            List<File> schemaFiles = originalSchemaLocations.get(namespace);

            if (schemaFiles == null) {
                schemaFiles = new ArrayList<>();
                schemaFiles.add(namespaceSchemaLocations.get(namespace));
                originalSchemaLocations.put(namespace, schemaFiles);
                namespaceSchemaLocations.put(namespace, getConsolidatedImportFilename(namespace));
            }
            schemaFiles.add(schemaLocation);
        }
    }

    /**
     * Notifies this component that an import declaration was added for the specified namespace.
     * 
     * @param namespace
     *            the namespace for which an import declaration was added
     */
    public void importAddedForNamespace(String namespace) {
        if (namespace != null) {
            importedNamespaces.add(namespace);
        }
    }

    /**
     * Returns the overridden location of the schema for the specified namespace.
     * 
     * @param namespace
     *            the namespace for which to return the schema location override
     * @return File
     */
    public File getSchemaLocation(String namespace) {
        return namespaceSchemaLocations.get(namespace);
    }

    /**
     * Generates consolidated import files in the target output directory for any namespace that has
     * more than one schema associated with it. The names and locations of all generated schemas
     * will be returned in the resulting list.
     * 
     * <p>
     * NOTE: Files will only be generated for namespaces that have been identified as imports via
     * the <code>importAddedForNamespace()</code> method.
     * 
     * @return List<File>
     * @throws CodeGenerationException
     */
    public List<File> generateConsolidatedImportFiles() throws CodeGenerationException {
        List<File> importFiles = new ArrayList<>();

        for (String namespace : originalSchemaLocations.keySet()) {
            if (importedNamespaces.contains(namespace)) {
                generateConsolidatedImportFile(namespace);
                importFiles.add(namespaceSchemaLocations.get(namespace));
            }
        }
        return importFiles;
    }

    /**
     * Returns a unique namespace prefix based on the preferred one provided.
     * 
     * @param preferredPrefix
     *            the preferred prefix designation
     * @return String
     */
    private String getUniquePrefix(String preferredPrefix) {
        String basePrefix = (preferredPrefix == null) ? "ns" : preferredPrefix;
        String testPrefix = basePrefix;
        int prefixCounter = 2;

        while (namespacePrefixes.containsValue(testPrefix)) {
            testPrefix = basePrefix + prefixCounter;
            prefixCounter++;
        }
        return testPrefix;
    }

    /**
     * Returns the file name and location of the add-on schema that will consolidate multiple
     * imports from the given namespace into a single schema.
     * 
     * @param namespace
     *            the namespace for which to return the name of the consolidated import file
     * @return File
     */
    private File getConsolidatedImportFilename(String namespace) {
        StringBuilder filename = new StringBuilder();

        filename.append("/").append(namespacePrefixes.get(namespace)).append("_imports.xsd");
        return new File(baseOutputFolder, filename.toString());
    }

    /**
     * Creates a new schema file that is assigned to the specified target namespace, and includes
     * all of the schema locations for the namespace.
     * 
     * @param namespace
     *            the namespace for which the new schema that consolidates imports for all of the
     *            original schemas
     * @throws CodeGenerationException
     *             thrown if an error occurs while creating the new schema file
     */
    private void generateConsolidatedImportFile(String namespace) throws CodeGenerationException {
        // Construct the content of the consolidated schema
        org.w3._2001.xmlschema.Schema consolidatedSchema = new org.w3._2001.xmlschema.Schema();
        URL outputFolderUrl = URLUtils.toURL(baseOutputFolder);

        consolidatedSchema.setTargetNamespace(namespace);
        consolidatedSchema.setElementFormDefault(FormChoice.QUALIFIED);
        consolidatedSchema.setAttributeFormDefault(FormChoice.UNQUALIFIED);

        for (File schemaFile : originalSchemaLocations.get(namespace)) {
            URL schemaFileURL = URLUtils.toURL(schemaFile);
            String relativeLocation = URLUtils
                    .getRelativeURL(outputFolderUrl, schemaFileURL, false);
            Include include = new Include();

            include.setSchemaLocation(relativeLocation);
            consolidatedSchema.getIncludeOrImportOrRedefine().add(include);
        }
        saveSchemaToFile(consolidatedSchema, namespaceSchemaLocations.get(namespace));
    }

    /**
     * Saves the XML schema content provided to the specified file location.
     * 
     * @param schema
     *            the schema content to be saved
     * @param schemaFile
     *            the file location to which the schema should be saved
     * @throws CodeGenerationException
     *             thrown if the schema file cannot be saved for any reason
     */
    private void saveSchemaToFile(final org.w3._2001.xmlschema.Schema schema, File schemaFile)
            throws CodeGenerationException {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();

            marshaller.setSchema(validationSchema);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                    new NamespacePrefixMapper() {

                        public String getPreferredPrefix(String namespaceUri, String suggestion,
                                boolean requirePrefix) {
                            String prefix = suggestion;

                            if (namespaceUri.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                                prefix = "xsd";

                            } else if (namespaceUri.equals(schema.getTargetNamespace())) {
                                prefix = schema.getTargetNamespace();
                            }
                            return prefix;
                        }

                    });
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(schema, schemaFile);

        } catch (Exception e) {
            throw new CodeGenerationException(e);
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
            validationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
