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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.FileSystemResourceResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import junit.framework.AssertionFailedError;

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
			Schema vSchema = xmlSchemaCache.get( schemaFile.getName() );
			
			if (vSchema == null) {
		    	SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
		    	
		    	sf.setResourceResolver( new FileSystemResourceResolver( schemaFile ) );
				vSchema = sf.newSchema( schemaFile );
				xmlSchemaCache.put( schemaFile.getName(), vSchema );
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
    	// TODO: Implement JSON schema validation checks
    }
    
    /**
     * Validates that the given JSON file is syntactically and symantically correct.
     * 
     * @param jsonFile  the JSON file to validate
     */
    private static void validateJSONDocument(File jsonFile) {
    	// TODO: Implement JSON document validation checks
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
