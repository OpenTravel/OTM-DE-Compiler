/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import junit.framework.AssertionFailedError;

import com.sabre.schemacompiler.ioc.SchemaDeclarations;
import com.sabre.schemacompiler.util.ClasspathResourceResolver;

/**
 * Static assertion methods used to validate generated XML schema and WSDL documents.
 * 
 * @author S. Livezey
 */
public class CodeGeneratorTestAssertions {
	
	private static final String XSD_SCHEMA_CONTEXT =
			":org.w3._2001.xmlschema" +
			":org.opentravel.ns.ota2.appinfo_v01_00";
	
	private static final String WSDL_SCHEMA_CONTEXT =
			":org.xmlsoap.schemas.wsdl" +
			":org.xmlsoap.schemas.wsdl.soap" +
			":org.w3._2001.xmlschema" +
			":org.opentravel.ns.ota2.appinfo_v01_00";
	
	private static Schema xsdValidationSchema;
	private static Schema wsdlValidationSchema;
	private static JAXBContext jaxbXsdContext;
	private static JAXBContext jaxbWsdlContext;
	
	/**
	 * Parses and validate the specified XML file to determine if it is a well-formatted XML schema
	 * document.  This method also has an implied assertion that the file exists.
	 * 
	 * @param xsdFile  the XML schema file to validate
	 * @throws AssertionFailedError  thrown if the file does not exist, or is not a valid XML schema document
	 */
	public static void assertValidXsd(File xsdFile) {
		try {
			if (xsdFile == null) {
				throw new NullPointerException("The XML schema file to validate is null.");
			}
			if (!xsdFile.exists()) {
				throw new AssertionFailedError("The XML schema file does not exist: " + xsdFile.getAbsolutePath());
			}
			Unmarshaller unmarshaller = jaxbXsdContext.createUnmarshaller();
			unmarshaller.setSchema(xsdValidationSchema);

			unmarshaller.unmarshal( new FileInputStream(xsdFile) );
			
		} catch (IOException e) {
			e.printStackTrace(System.out);
			throw new AssertionFailedError("IOException encountered during XML schema validation: " + e.getMessage());
			
		} catch (JAXBException e) {
			e.printStackTrace(System.out);
			throw new AssertionFailedError("Invalid XML schema document: " + e.getMessage());
		}
	}
	
	/**
	 * Parses and validate the specified XML file to determine if it is a well-formatted WSDL
	 * document.  This method also has an implied assertion that the file exists.
	 * 
	 * @param wsdlFile  the WSDL document file to validate
	 * @throws AssertionFailedError  thrown if the file does not exist, or is not a valid WSDL document
	 */
	public static void assertValidWsdl(File wsdlFile) {
		try {
			if (wsdlFile == null) {
				throw new NullPointerException("The WSDL document file to validate is null.");
			}
			if (!wsdlFile.exists()) {
				throw new AssertionFailedError("The WSDL document file does not exist: " + wsdlFile.getAbsolutePath());
			}
			Unmarshaller unmarshaller = jaxbWsdlContext.createUnmarshaller();
			unmarshaller.setSchema(wsdlValidationSchema);

			unmarshaller.unmarshal( new FileInputStream(wsdlFile) );
			
		} catch (IOException e) {
			e.printStackTrace(System.out);
			throw new AssertionFailedError("IOException encountered during WSDL validation: " + e.getMessage());
			
		} catch (JAXBException e) {
			e.printStackTrace(System.out);
			throw new AssertionFailedError("Invalid WSDL document document: " + e.getMessage());
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
    		xsdValidationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
    		jaxbXsdContext = JAXBContext.newInstance(XSD_SCHEMA_CONTEXT);
			
    		schemaStream = SchemaDeclarations.WSDL_SCHEMA.getContent();
    		wsdlValidationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
    		jaxbWsdlContext = JAXBContext.newInstance(WSDL_SCHEMA_CONTEXT);
    		
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
