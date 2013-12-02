/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.loader.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.opentravel.ns.ota2.librarymodel_v01_03.Library;
import org.opentravel.ns.ota2.librarymodel_v01_03.NamespaceImport;

import com.sabre.schemacompiler.ioc.SchemaDeclarations;
import com.sabre.schemacompiler.loader.LibraryInputSource;
import com.sabre.schemacompiler.loader.LibraryLoaderException;
import com.sabre.schemacompiler.loader.LibraryModuleImport;
import com.sabre.schemacompiler.loader.LibraryModuleInfo;
import com.sabre.schemacompiler.util.ClasspathResourceResolver;
import com.sabre.schemacompiler.util.ExceptionUtils;
import com.sabre.schemacompiler.util.FileHintUtils;
import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Default implementation of the <code>LibraryModuleLoader</code> that loads and parses XML content
 * from an input stream. Unless configured otherwise, a <code>CatalogNamespaceResolver</code> is
 * used to resolve library namespaces.
 * 
 * @author S. Livezey
 */
public class LibrarySchema1_3_ModuleLoader extends AbstractLibraryModuleLoader {

	public static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.librarymodel_v01_03";

	private static javax.xml.validation.Schema libraryValidationSchema;
	private static JAXBContext jaxbContext;

	/**
	 * @see com.sabre.schemacompiler.loader.LibraryModuleLoader#loadLibrary(com.sabre.schemacompiler.loader.LibraryInputSource,
	 *      com.sabre.schemacompiler.validate.ValidationFindings)
	 */
	public synchronized LibraryModuleInfo<Object> loadLibrary(LibraryInputSource<InputStream> inputSource, ValidationFindings validationFindings)
			throws LibraryLoaderException {
		URL libraryUrl = (inputSource == null) ? null : inputSource.getLibraryURL();
		LibraryModuleInfo<Object> moduleInfo = null;
		try {
			Library jaxbLibrary;
			
			try {
				jaxbLibrary = (Library) loadLibrary(inputSource, validationFindings, jaxbContext, libraryValidationSchema);
				
			} catch (JAXBException e) {
				String urlString = (libraryUrl == null) ? "[MISSING URL]" : URLUtils.getShortRepresentation(libraryUrl);
				
				// If we are not able to load the library with validation turned on, load it
				// with validation disabled and issue a loader warning.  If the content is still
				// invalid with validation turned off, the file is completely unreadable.  If that
				// is the case, a JAXB exception will be thrown and the last catch block of this
				// method will issue a loader validation error.
				try {
					jaxbLibrary = (Library) loadLibrary(inputSource, validationFindings, jaxbContext, null);
					
					if (jaxbLibrary != null) {
						validationFindings.addFinding(FindingType.WARNING, new URLValidationSource(libraryUrl),
								WARNING_CORRUPT_LIBRARY_CONTENT, urlString, ExceptionUtils.getExceptionClass(e).getSimpleName(),
								ExceptionUtils.getExceptionMessage(e));
					}
				} catch (ClassCastException cce) {
					throw new JAXBException("The library file does not contain OTM v1.3 content.", cce);
				}
			}
			
			// Before returning, we need to normalize the namespace and library name values
			// since they are used for name lookups
			if (jaxbLibrary != null) {
				jaxbLibrary.setName((jaxbLibrary.getName() == null) ? null : jaxbLibrary.getName().trim());
				jaxbLibrary.setNamespace((jaxbLibrary.getNamespace() == null) ? null : jaxbLibrary.getNamespace().trim());
				moduleInfo = buildModuleInfo( jaxbLibrary, libraryUrl );
			}
			
		} catch (JAXBException e) {
			String urlString = (libraryUrl == null) ? "[MISSING URL]" : URLUtils.getShortRepresentation(libraryUrl);

			validationFindings.addFinding(FindingType.ERROR, new URLValidationSource(libraryUrl),
					ERROR_UNREADABLE_LIBRARY_CONTENT, urlString, ExceptionUtils.getExceptionClass(e).getSimpleName(),
					ExceptionUtils.getExceptionMessage(e));
		}
		return moduleInfo;
	}
	
	/**
	 * Constructs the <code>LibraryModuleInfo</code> to be returned for the given schema.
	 * 
	 * @param library  the library that was loaded
	 * @param libraryUrl  the URL location of the library that was loaded
	 * @return LibraryModuleInfo
	 */
	private LibraryModuleInfo<Object> buildModuleInfo(Library library, URL libraryUrl) {
		List<LibraryModuleImport> imports = new ArrayList<LibraryModuleImport>();
		List<String> includes = library.getIncludes();
		
		for (NamespaceImport nsImport : library.getImport()) {
			imports.add( new LibraryModuleImport(
					nsImport.getNamespace(), nsImport.getPrefix(), FileHintUtils.resolveHints(nsImport.getFileHints(), libraryUrl)) );
		}
		return new LibraryModuleInfo<Object>(library, library.getName(), library.getNamespace(),
				library.getVersionScheme(), includes, imports);
	}
	
	/**
	 * Initializes the validation schema and shared JAXB context.
	 */
	static {
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			InputStream schemaStream = SchemaDeclarations.OTA2_LIBRARY_SCHEMA_1_3.getContent();

			schemaFactory.setResourceResolver(new ClasspathResourceResolver());
			libraryValidationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
			jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}

}
