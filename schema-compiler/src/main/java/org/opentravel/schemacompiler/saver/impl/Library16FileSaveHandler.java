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
package org.opentravel.schemacompiler.saver.impl;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.opentravel.ns.ota2.librarymodel_v01_06.Library;
import org.opentravel.ns.ota2.librarymodel_v01_06.ObjectFactory;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;

/**
 * Save handler for the OTM1.6 library format.
 */
public class Library16FileSaveHandler extends AbstractLibraryFileSaveHandler<Library> {
	
    private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.librarymodel_v01_06";

    private static final String LIBRARY_SCHEMA_LOCATION_DECL = SchemaDeclarations.OTA2_LIBRARY_SCHEMA_1_6.getNamespace() +
    		" " + SchemaDeclarations.OTA2_LIBRARY_SCHEMA_1_6.getFilename(CodeGeneratorFactory.XSD_TARGET_FORMAT);
    
    private static JAXBContext jaxbContext;
    private static Schema validationSchema;

	/**
	 * @see org.opentravel.schemacompiler.saver.LibrarySaveHandler#getTargetFormat()
	 */
	@Override
	public Class<Library> getTargetFormat() {
		return Library.class;
	}

	/**
	 * @see org.opentravel.schemacompiler.saver.impl.AbstractLibraryFileSaveHandler#getLibraryTargetNamespace()
	 */
	@Override
	protected String getLibraryTargetNamespace() {
		return SchemaDeclarations.OTA2_LIBRARY_SCHEMA_1_6.getNamespace();
	}
    
    /**
	 * @see org.opentravel.schemacompiler.saver.impl.AbstractLibraryFileSaveHandler#getJaxbContext()
	 */
	@Override
	protected JAXBContext getJaxbContext() {
		return jaxbContext;
	}

	/**
	 * @see org.opentravel.schemacompiler.saver.impl.AbstractLibraryFileSaveHandler#createLibraryElement(java.lang.Object)
	 */
	@Override
	protected JAXBElement<Library> createLibraryElement(Library library) {
		return new ObjectFactory().createLibrary(library);
	}

	/**
	 * @see org.opentravel.schemacompiler.saver.impl.AbstractLibraryFileSaveHandler#getValidationSchema()
	 */
	@Override
	protected Schema getValidationSchema() {
		return validationSchema;
	}

	/**
	 * @see org.opentravel.schemacompiler.saver.impl.AbstractLibraryFileSaveHandler#getLibrarySchemaLocation()
	 */
	@Override
	protected String getLibrarySchemaLocation() {
		return LIBRARY_SCHEMA_LOCATION_DECL;
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

            validationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
