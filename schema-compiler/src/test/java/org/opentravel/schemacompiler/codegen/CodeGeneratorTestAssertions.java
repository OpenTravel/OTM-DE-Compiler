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
package org.opentravel.schemacompiler.codegen;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.AssertionFailedError;

import org.junit.Assert;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.FileSystemResourceResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * Static assertion methods used to validate generated XML schema and WSDL documents.
 * 
 * @author S. Livezey
 */
public class CodeGeneratorTestAssertions {
	
	private static final boolean DEBUG = false;
	
    private static final String WSDL_SCHEMA_CONTEXT = ":org.xmlsoap.schemas.wsdl"
            + ":org.xmlsoap.schemas.wsdl.soap" + ":org.w3._2001.xmlschema"
            + ":org.opentravel.ns.ota2.appinfo_v01_00";
    
    private static final Collection<String> ignoreSchemaErrors = Arrays.asList(
    		"cvc-pattern-valid",		// Value 'Example String Value' is not facet-valid with respect to pattern '[A-Za-z]*' for type 'LookupSimple'.
    		"cvc-type.3.1.3",			// The value 'Counter_4-example-value' of element 'element2' is not valid.
    		"cvc-attribute.3",			// The value 'Counter_4-example-value' of attribute 'attr2' on element
    									//    'SampleCore_Alias1Detail' is not valid with respect to its type, 'Counter_4'.
    		"cvc-datatype-valid.1.2.1",	// 'element3-ex' is not a valid value for 'integer'.
    		"cvc-id.1"					// There is no ID/IDREF binding for IDREF 'a333'.
    	);
    
    private static DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    private static Map<String,Schema> xmlSchemaCache = new HashMap<>();
    private static Schema wsdlValidationSchema;
    private static JAXBContext jaxbWsdlContext;
    
    /**
     * For each file in the given list perform a format-specific validation check.
     * 
     * @param generatedFiles  the list of generated files
     */
    public static void validateGeneratedFiles(List<File> generatedFiles) {
    	validateGeneratedFiles( generatedFiles, true );
    }
    
    /**
     * For each file in the given list perform a format-specific validation check.
     * 
     * @param generatedFiles  the list of generated files
     * @param validateExamples  flag indicating whether example XML/JSON files should be validated
     */
    public static void validateGeneratedFiles(List<File> generatedFiles, boolean validateExamples) {
    	for (File generatedFile : generatedFiles) {
    		String filename = generatedFile.getName().toLowerCase();
    		
    		if (filename.endsWith(".xsd")) {
    			validateXMLSchema( generatedFile );
    		}
    	}
    	for (File generatedFile : generatedFiles) {
    		String filename = generatedFile.getName().toLowerCase();
    		
    		if (filename.endsWith(".xml")) {
    			if (validateExamples) {
        			validateXMLDocument( generatedFile );
    			}
    			
    		} else if (filename.endsWith(".wsdl")) {
    			validateWsdlDocument( generatedFile );
    			
    		} else if (filename.endsWith(".schema.json")) {
    			validateJSONSchema( generatedFile );
    			
    		} else if (filename.endsWith(".swagger.json")) {
    			validateSwaggerDocument( generatedFile );
    			
    		} else if (filename.endsWith(".json")) {
    			if (validateExamples) {
        			validateJSONDocument( generatedFile );
    			}
    		}
    	}
    }
    
    /**
     * Validates that the given XML schema file is syntactically and symantically correct.
     * 
     * @param xmlSchemaFile  the XML schema file to validate
     */
    private static void validateXMLSchema(File xmlSchemaFile) {
    	getValidationSchema( xmlSchemaFile );
    }
    
    /**
     * Validates that the given XML file is syntactically and symantically correct.
     * 
     * @param xmlFile  the XML file to validate
     */
    private static void validateXMLDocument(File xmlFile) {
		try {
			DocumentBuilder dBuilder = domFactory.newDocumentBuilder();
	    	Document xmlDoc = dBuilder.parse( xmlFile );
	    	String schemaLocation = getSchemaLocation( xmlDoc );
	    	
	    	if (schemaLocation != null) {
	    		File schemaFile = new File( xmlFile.getParentFile(), "/" + schemaLocation );
	    		Schema vSchema = getValidationSchema( schemaFile );
	    		Validator validator = vSchema.newValidator();
	    		final List<String> errorList = new ArrayList<>();
	    		
	    		validator.setErrorHandler( new ErrorHandler() {
					public void warning(SAXParseException ex) throws SAXException {
						// No action for warnings
					}
					public void error(SAXParseException ex) throws SAXException {
						addError( ex );
					}
					public void fatalError(SAXParseException ex) throws SAXException {
						addError( ex );
					}
					private void addError(SAXParseException ex) {
						String errorId = ex.getMessage().split(":")[0];
						
						if (!ignoreSchemaErrors.contains( errorId )) {
							errorList.add( ex.getMessage() );
						}
					}
				});
	    		validator.validate( new StreamSource( xmlFile ) );
	    		
	    		if (!errorList.isEmpty()) {
	    			if (DEBUG) {
	    				System.out.println("ERROR(S) VALIDATING: " + xmlFile.getCanonicalPath());
	    				
	    				for (String message : errorList) {
	    					System.out.println("  " + message);
	    				}
	    			} else {
	    				// Only throw an exception if we are not debugging
		                throw new AssertionFailedError(
		                		"One or more errors found in example document: " + xmlFile.getName() );
	    			}
	    		}
	    	}
	    	
		} catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionFailedError( e.getMessage() );
		}
    }
    
    /**
     * Returns a new validation schema that resolves resource references based
     * on relative file system paths.
     * 
     * @return SchemaFactory
     */
    private static Schema getValidationSchema(File schemaFile) {
		try {
			Schema vSchema = xmlSchemaCache.get( schemaFile.getCanonicalPath() );
			
			if (vSchema == null) {
		    	SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
		    	
		    	sf.setResourceResolver( new FileSystemResourceResolver( schemaFile ) );
				vSchema = sf.newSchema( schemaFile );
				xmlSchemaCache.put( schemaFile.getCanonicalPath(), vSchema );
			}
			return vSchema;
			
		} catch (Throwable t) {
            throw new AssertionFailedError( t.getMessage() );
		}
    }
    
    /**
     * Returns the path of the schema location from the given XML document.  If a schema
     * location is not specified, this method will return null.
     * 
     * @param xmlDoc  the XML document for which to return a schema location
     * @return String
     */
    private static String getSchemaLocation(Document xmlDoc) {
    	Element rootElement = xmlDoc.getDocumentElement();
    	String schemaLocation = rootElement.getAttribute("xsi:schemaLocation");
    	
    	if (schemaLocation.equals("")) {
    		schemaLocation = null;
    		
    	} else {
    		schemaLocation = schemaLocation.split("\\s")[1];
    	}
    	return schemaLocation;
    }
    
    /**
     * Parses and validate the specified XML file to determine if it is a well-formatted WSDL
     * document. This method also has an implied assertion that the file exists.
     * 
     * @param wsdlFile
     *            the WSDL document file to validate
     * @throws AssertionFailedError
     *             thrown if the file does not exist, or is not a valid WSDL document
     */
    private static void validateWsdlDocument(File wsdlFile) {
        try {
            if (wsdlFile == null) {
                throw new NullPointerException("The WSDL document file to validate is null.");
            }
            if (!wsdlFile.exists()) {
                throw new AssertionFailedError("The WSDL document file does not exist: "
                        + wsdlFile.getAbsolutePath());
            }
            Unmarshaller unmarshaller = jaxbWsdlContext.createUnmarshaller();
            unmarshaller.setSchema(wsdlValidationSchema);

            unmarshaller.unmarshal(new FileInputStream(wsdlFile));

        } catch (IOException e) {
            throw new AssertionFailedError("IOException encountered during WSDL validation: " + e.getMessage());

        } catch (JAXBException e) {
            throw new AssertionFailedError("Invalid WSDL document document: " + e.getMessage());
        }
    }

    /**
     * Validates that the given JSON schema file is syntactically and symantically correct.
     * 
     * @param jsonSchemaFile  the JSON schema file to validate
     */
    private static void validateJSONSchema(File jsonSchemaFile) {
    	try {
    		JsonNode schemaNode = JsonLoader.fromFile( jsonSchemaFile );
    		JsonSchema schema = newJsonSchemaFactory( jsonSchemaFile.getParentFile() ).getJsonSchema( schemaNode );
    		
    		try {
    			schema.validate( new ObjectNode( JsonNodeFactory.instance ) );
    			
    		} catch (InvalidSchemaException e) {
    			System.out.println( e.getProcessingMessage().asJson().asText() );
    			fail( e.getMessage() );
    		}
    		
    	} catch (ProcessingException | IOException e) {
    		if (DEBUG) {
    			e.printStackTrace( System.out );
    		}
            throw new AssertionFailedError( "Error validating JSON schema: " + jsonSchemaFile.getName() );
    	}
    }
    
    /**
     * Validates that the given JSON file is syntactically and symantically correct.
     * 
     * @param jsonFile  the JSON file to validate
     */
    private static void validateJSONDocument(File jsonFile) {
    	try {
    		JsonNode jsonNode = JsonLoader.fromFile( jsonFile );
    		File jsonSchemaFile = findJsonSchema( jsonFile.getParentFile(), jsonNode );
    		
    		if (jsonSchemaFile != null) {
        		JsonNode schemaNode = JsonLoader.fromFile( jsonSchemaFile );
        		JsonSchema schema = newJsonSchemaFactory( jsonSchemaFile.getParentFile() ).getJsonSchema( schemaNode );
        		ProcessingReport report = schema.validate( jsonNode );
        		List<ProcessingMessage> errors = getValidationErrors( report );
        		
        		if (DEBUG) {
        			if (errors.size() > 0) {
        				System.out.println("Validation Results: " + jsonFile.getAbsolutePath());
        				
            			for (ProcessingMessage error : errors) {
            				System.out.println( error );
            			}
        				System.out.println("ERROR COUNT: " + errors.size());
        			}
        		}
        		Assert.assertEquals( 0, errors.size() );
    		}
    		
    	} catch (ProcessingException | IOException e) {
    		if (DEBUG) {
    			System.out.println("Error validating JSON document: " + jsonFile.getAbsolutePath());
    			System.out.println(e.getMessage());
    		}
            throw new AssertionFailedError( "Error validating JSON document: " + jsonFile.getName() );
    	}
    }
    
    /**
     * Validates that the given Swagger API specification is syntactically and
     * symantically correct.
     * 
     * @param swaggerFile  the Swagger document to validate
     */
    public static void validateSwaggerDocument(File swaggerFile) {
    	// TODO: Implement the 'validateSwaggerDocument()' method
    }
    
	/**
	 * Returns a new <code>JsonSchemaFactory</code> instance.
	 * 
	 * @param schemaFolder  the folder location where JSON schemas are located
	 * @return JsonSchemaFactory
	 */
	private static JsonSchemaFactory newJsonSchemaFactory(File schemaFolder) {
		return JsonSchemaFactory.newBuilder().setLoadingConfiguration(
				LoadingConfiguration.newBuilder().setURITranslatorConfiguration(
						URITranslatorConfiguration.newBuilder()
								.setNamespace( "http://opentravel.org/schemas/json/" )
								.addPathRedirect( "http://opentravel.org/schemas/json/", schemaFolder.toURI().toString() )
			            		.freeze()
						).freeze()
				).freeze();

	}
	
	/**
	 * Returns the JSON schema file that should be used to validate the given JSON document.
	 * If no qualifying schema can be located, this method will return null.
	 * 
	 * @param jsonFolder  the folder from which the JSON document was loaded
	 * @param jsonDocument  the JSON document to be validated
	 * @return File
	 */
	private static File findJsonSchema(File jsonFolder, JsonNode jsonDocument) {
		Iterator<String> fieldNames = jsonDocument.fieldNames();
		String rootElement = fieldNames.hasNext() ? fieldNames.next() : null;
		File[] searchFolders = new File[] {
				jsonFolder.getParentFile(), jsonFolder.getParentFile().getParentFile()
		};
		File schemaFile = null;
		
		for (File schemaFolder : searchFolders) {
			for (File candidateFile : schemaFolder.listFiles()) {
				if (candidateFile.isFile() && candidateFile.getName().endsWith(
						JsonSchemaCodegenUtils.JSON_SCHEMA_FILENAME_EXT )) {
		    		try {
						JsonNode schemaNode = JsonLoader.fromFile( candidateFile );
						
						if (canValidate( schemaNode, rootElement )) {
							schemaFile = candidateFile;
							break;
						}
						
					} catch (IOException e) {
						// Ignore error and skip this file
					}
				}
			}
		}
		return schemaFile;
	}
	
	/**
	 * Returns true if the given JSON schema can be used to validate a JSON document
	 * with the specified root element.
	 * 
	 * @param jsonSchema  the JSON schema that may be used to validate the document
	 * @param rootElement  the root element name of the JSON document to be validated
	 * @return boolean
	 */
	private static boolean canValidate(JsonNode jsonSchema, String rootElement) {
		boolean validatable = false;
		
		if (rootElement != null) {
			JsonNode schemaOneOf = jsonSchema.get( "oneOf" );
			
			if (schemaOneOf instanceof ArrayNode) {
				for (JsonNode oneOfEntry : (ArrayNode) schemaOneOf) {
					JsonNode oneOfProperties = oneOfEntry.get( "properties" );
					
					if ((oneOfProperties != null) && (oneOfProperties.get( rootElement ) != null)) {
						validatable = true;
						break;
					}
				}
			}
		}
		return validatable;
	}
	
	/**
	 * Flattens the contents of the given JSON validation report and returns the
	 * entries that represent errors.
	 * 
	 * @param report  the JSON validation report
	 * @return ProcessingMessage
	 * @throws ProcessingException  thrown if an error occurs during JSON validation
	 */
	private static List<ProcessingMessage> getValidationErrors(ProcessingReport report) throws ProcessingException {
		Iterator<ProcessingMessage> iterator = report.iterator();
		List<ProcessingMessage> errors = new ArrayList<>();
		
		while (iterator.hasNext()) {
			ProcessingMessage message = iterator.next();
			
			if (message.getLogLevel() == LogLevel.ERROR) {
				JsonNode messageNode = message.asJson();
				JsonNode reportsNode = messageNode.get( "reports" );
				
				if (reportsNode == null) {
					errors.add( message );
					
				} else {
					Iterator<String> rnIterator = reportsNode.fieldNames();
					
					while (rnIterator.hasNext()) {
						ArrayNode reportJson = (ArrayNode) reportsNode.get( rnIterator.next() );
						
						if (!isSuperfluousReportNode( reportJson )) {
							errors.addAll( getValidationErrors( buildReport( reportJson ) ) );
						}
					}
				}
			}
		}
		return errors;
	}
	
	/**
	 * Returns true if the given JSON report contains a single error that is not relevant
	 * to the overall validation findings.
	 * 
	 * @param reportJson  the JSON contents of the validation report
	 * @return boolean
	 */
	private static boolean isSuperfluousReportNode(ArrayNode reportJson) {
		boolean result = false;
		
		if (reportJson.size() == 1) {
			JsonNode messageJson = reportJson.get( 0 );
			JsonNode schemaJson = messageJson.get( "schema" );
			
			if (schemaJson != null) {
				JsonNode pointerJson = schemaJson.get( "pointer" );
				result = (pointerJson != null) && pointerJson.asText( "" ).startsWith( "/oneOf/" );
			}
		}
		return result;
	}
	
	/**
	 * Reconstructs a <code>ProcessingReport</code> instance from the JSON content provided.
	 * 
	 * @param reportJson  the JSON content of the validation report
	 * @return ProcessingReport
	 * @throws ProcessingException  thrown if an error occurs while reconstructing the report instance
	 */
	private static ProcessingReport buildReport(ArrayNode reportJson) throws ProcessingException {
		ProcessingReport report = new ListProcessingReport();
		Iterator<JsonNode> iterator = reportJson.iterator();
		
		while (iterator.hasNext()) {
			ProcessingMessage message = new ProcessingMessage();
			JsonNode messageJson = iterator.next();
			Iterator<String> fnIterator = messageJson.fieldNames();
			
			while (fnIterator.hasNext()) {
				String fieldName = fnIterator.next();
				JsonNode fieldValue = messageJson.get( fieldName );
				
				if (fieldName.equals("level")) {
					message.setLogLevel( LogLevel.valueOf( fieldValue.asText().toUpperCase() ) );
				} else {
					message.put( fieldName, fieldValue );
				}
			}
			switch (message.getLogLevel()) {
				case FATAL:
					report.fatal( message );
					break;
				case ERROR:
					report.error( message );
					break;
				case WARNING:
					report.warn( message );
					break;
				case INFO:
					report.info( message );
					break;
				case DEBUG:
					report.debug( message );
					break;
				default:
					break;
			}
		}
		return report;
	}
	
    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
        	SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream schemaStream = SchemaDeclarations.SCHEMA_FOR_SCHEMAS.getContent(
            		CodeGeneratorFactory.XSD_TARGET_FORMAT);

            schemaFactory.setResourceResolver(new ClasspathResourceResolver());

            schemaStream = SchemaDeclarations.WSDL_SCHEMA.getContent(
            		CodeGeneratorFactory.XSD_TARGET_FORMAT);
            wsdlValidationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            jaxbWsdlContext = JAXBContext.newInstance(WSDL_SCHEMA_CONTEXT);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
