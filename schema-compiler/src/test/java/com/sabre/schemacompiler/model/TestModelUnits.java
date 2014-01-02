/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

import org.junit.Test;

import com.sabre.schemacompiler.loader.LibraryInputSource;
import com.sabre.schemacompiler.loader.LibraryModelLoader;
import com.sabre.schemacompiler.loader.LibraryNamespaceResolver;
import com.sabre.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import com.sabre.schemacompiler.loader.impl.LibraryStreamInputSource;
import com.sabre.schemacompiler.util.SchemaCompilerTestUtils;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Unit tests for elements of the compiler meta-model.
 * 
 * @author S. Livezey
 */
public class TestModelUnits {
	
	public static final String PACKAGE_2_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2";
	
	@Test
	public void testClearModel() throws Exception {
		LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(
				new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v2/library_2_p2.xml"));
		LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
		ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);
		
		SchemaCompilerTestUtils.printFindings(findings);
		assertFalse(findings.hasFinding(FindingType.ERROR));
		
		TLModel model = modelLoader.getLibraryModel();
		assertEquals(3, model.getBuiltInLibraries().size());
		assertEquals(5, model.getUserDefinedLibraries().size());
		
		model.clearModel();
		assertEquals(3, model.getBuiltInLibraries().size());
		assertEquals(0, model.getUserDefinedLibraries().size());
	}
	
	@Test
	public void testGetReferenceCount() throws Exception {
		LibraryNamespaceResolver namespaceResolver = new CatalogLibraryNamespaceResolver(
				new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/library-catalog.xml"));
		LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
		modelLoader.setNamespaceResolver(namespaceResolver);
		ValidationFindings findings = modelLoader.loadLibraryModel(new URI(PACKAGE_2_NAMESPACE));
		
		SchemaCompilerTestUtils.printFindings(findings);
		assertFalse(findings.hasFinding(FindingType.ERROR));
		
		TLModel model = modelLoader.getLibraryModel();
		AbstractLibrary library = null;
		
		for (AbstractLibrary lib : model.getAllLibraries()) {
			if (lib.getName().equals("library_2_p2")) {
				library = lib;
			}
		}
		assertNotNull(library);
		assertEquals(5, library.getReferenceCount());
	}
	
	@Test
	public void testLibraryServiceMethods() throws Exception {
		LibraryNamespaceResolver namespaceResolver = new CatalogLibraryNamespaceResolver(
				new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/library-catalog.xml"));
		LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(
				new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v2/library_1_p2.xml"));
		LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
		modelLoader.setNamespaceResolver(namespaceResolver);
		ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);
		
		SchemaCompilerTestUtils.printFindings(findings);
		assertFalse(findings.hasFinding(FindingType.ERROR));
		
		TLModel model = modelLoader.getLibraryModel();
		TLLibrary library = (TLLibrary) model.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
		TLService service = library.getService();
		
		assertNotNull(service);
		library.removeNamedMember(service);
		assertNull(library.getService());
		assertNull(service.getOwningLibrary());
		library.addNamedMember(service);
		assertNotNull(library.getService());
		assertNotNull(service.getOwningLibrary());
	}
	
	@Test
	public void testDocumentationOwner() throws Exception {
		TLBusinessObject sourceBO = new TLBusinessObject();
		TLBusinessObject destinationBO = new TLBusinessObject();
		TLDocumentation doc = new TLDocumentation();
		
		sourceBO.setDocumentation(doc);
		destinationBO.setDocumentation(doc);
		
		assertEquals(doc, destinationBO.getDocumentation());
		assertEquals(doc, sourceBO.getDocumentation());
		assertEquals(destinationBO, doc.getOwner());
		
		// Original Bug: Documentation owner was nulled out with this call prior to fix
		destinationBO.setDocumentation(sourceBO.getDocumentation());
		
		assertEquals(doc, destinationBO.getDocumentation());
		assertEquals(doc, sourceBO.getDocumentation());
		assertEquals(destinationBO, doc.getOwner());
	}
	
}
