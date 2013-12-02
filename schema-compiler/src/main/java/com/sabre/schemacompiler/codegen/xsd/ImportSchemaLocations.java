/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.codegen.xsd;

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

import org.w3._2001.xmlschema.FormChoice;
import org.w3._2001.xmlschema.Include;

import com.sabre.schemacompiler.codegen.CodeGenerationException;
import com.sabre.schemacompiler.ioc.SchemaDeclarations;
import com.sabre.schemacompiler.util.ClasspathResourceResolver;
import com.sabre.schemacompiler.util.URLUtils;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

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
	
	private Map<String,File> namespaceSchemaLocations = new HashMap<String,File>();
	private Map<String,String> namespacePrefixes = new HashMap<String, String>();
	private Map<String,List<File>> originalSchemaLocations = new HashMap<String,List<File>>();
	private Set<String> importedNamespaces = new HashSet<String>();
	private File baseOutputFolder;
	
	/**
	 * Constructor that assigns the base output folder location of a code generation task.
	 * 
	 * @param baseOutputFolder  the base folder location for all output produced by the code generator(s)
	 */
	public ImportSchemaLocations(File baseOutputFolder) {
		this.baseOutputFolder = baseOutputFolder;
	}
	
	/**
	 * Assigns a schema location for the specified namespace.
	 * 
	 * @param namespace  the namespace of the associated schema file
	 * @param preferredPrefix  the preferred prefix to use when referring to the specified namespace
	 * @param schemaLocation  the file location of the schema file for the import
	 */
	public void setSchemaLocation(String namespace, String preferredPrefix, File schemaLocation) {
		if ((namespace == null) || (schemaLocation == null)) {
			throw new IllegalArgumentException("The namespace and schema location values cannot be null.");
		}
		
		if (!namespaceSchemaLocations.containsKey(namespace)) {
			String uniquePrefix = getUniquePrefix(preferredPrefix);
			
			namespaceSchemaLocations.put(namespace, schemaLocation);
			namespacePrefixes.put(namespace, uniquePrefix);
			
		} else {
			List<File> schemaFiles = originalSchemaLocations.get(namespace);
			
			if (schemaFiles == null) {
				schemaFiles = new ArrayList<File>();
				schemaFiles.add( namespaceSchemaLocations.get(namespace) );
				originalSchemaLocations.put( namespace, schemaFiles );
				namespaceSchemaLocations.put( namespace, getConsolidatedImportFilename(namespace) );
			}
			schemaFiles.add( schemaLocation );
		}
	}
	
	/**
	 * Notifies this component that an import declaration was added for the specified namespace.
	 * 
	 * @param namespace  the namespace for which an import declaration was added
	 */
	public void importAddedForNamespace(String namespace) {
		if (namespace != null) {
			importedNamespaces.add(namespace);
		}
	}
	
	/**
	 * Returns the overridden location of the schema for the specified namespace.
	 * 
	 * @param namespace  the namespace for which to return the schema location override
	 * @return File
	 */
	public File getSchemaLocation(String namespace) {
		return namespaceSchemaLocations.get(namespace);
	}
	
	/**
	 * Generates consolidated import files in the target output directory for any namespace that has more
	 * than one schema associated with it.  The names and locations of all generated schemas will be returned
	 * in the resulting list.
	 * 
	 * <p>NOTE: Files will only be generated for namespaces that have been identified as imports via the
	 * <code>importAddedForNamespace()</code> method.
	 * 
	 * @return List<File>
	 * @throws CodeGenerationException
	 */
	public List<File> generateConsolidatedImportFiles() throws CodeGenerationException {
		List<File> importFiles = new ArrayList<File>();
		
		for (String namespace : originalSchemaLocations.keySet()) {
			if (importedNamespaces.contains(namespace)) {
				generateConsolidatedImportFile(namespace);
				importFiles.add( namespaceSchemaLocations.get(namespace) );
			}
		}
		return importFiles;
	}
	
	/**
	 * Returns a unique namespace prefix based on the preferred one provided.
	 * 
	 * @param preferredPrefix  the preferred prefix designation
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
	 * Returns the file name and location of the add-on schema that will consolidate multiple imports
	 * from the given namespace into a single schema.
	 * 
	 * @param namespace  the namespace for which to return the name of the consolidated import file
	 * @return File
	 */
	private File getConsolidatedImportFilename(String namespace) {
		StringBuilder filename = new StringBuilder();
		
		filename.append("/").append(namespacePrefixes.get(namespace)).append("_imports.xsd");
		return new File(baseOutputFolder, filename.toString());
	}
	
	/**
	 * Creates a new schema file that is assigned to the specified target namespace, and includes all of the schema
	 * locations for the namespace.
	 * 
	 * @param namespace  the namespace for which the new schema that consolidates imports for all of the original schemas
	 * @throws CodeGenerationException  thrown if an error occurs while creating the new schema file
	 */
	private void generateConsolidatedImportFile(String namespace)
			 throws CodeGenerationException {
		// Construct the content of the consolidated schema
		org.w3._2001.xmlschema.Schema consolidatedSchema = new org.w3._2001.xmlschema.Schema();
		URL outputFolderUrl = URLUtils.toURL( baseOutputFolder );
		
		consolidatedSchema.setTargetNamespace( namespace );
		consolidatedSchema.setElementFormDefault( FormChoice.QUALIFIED );
		consolidatedSchema.setAttributeFormDefault( FormChoice.UNQUALIFIED );
		
		for (File schemaFile : originalSchemaLocations.get(namespace)) {
			URL schemaFileURL = URLUtils.toURL(schemaFile);
			String relativeLocation = URLUtils.getRelativeURL(outputFolderUrl, schemaFileURL, false);
			Include include = new Include();
			
			include.setSchemaLocation( relativeLocation );
			consolidatedSchema.getIncludeOrImportOrRedefine().add( include );
		}
		saveSchemaToFile(consolidatedSchema, namespaceSchemaLocations.get( namespace ));
	}
	
	/**
	 * Saves the XML schema content provided to the specified file location.
	 * 
	 * @param schema  the schema content to be saved
	 * @param schemaFile  the file location to which the schema should be saved
	 * @throws CodeGenerationException  thrown if the schema file cannot be saved for any reason
	 */
	private void saveSchemaToFile(final org.w3._2001.xmlschema.Schema schema, File schemaFile) throws CodeGenerationException {
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			
			marshaller.setSchema(validationSchema);
			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
				
				public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
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
			
		} catch (Throwable t) {
			throw new CodeGenerationException(t);
		}
	}
	
	/**
	 * Initializes the validation schema and shared JAXB context.
	 */
	static {
		try {
    		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    		InputStream schemaStream = SchemaDeclarations.SCHEMA_FOR_SCHEMAS.getContent();
    		
    		schemaFactory.setResourceResolver(new ClasspathResourceResolver());
    		validationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
			jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
