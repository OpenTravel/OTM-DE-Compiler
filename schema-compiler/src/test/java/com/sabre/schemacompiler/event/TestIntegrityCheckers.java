/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.event;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;

import com.sabre.schemacompiler.ic.ModelIntegrityChecker;
import com.sabre.schemacompiler.loader.LibraryInputSource;
import com.sabre.schemacompiler.loader.LibraryModelLoader;
import com.sabre.schemacompiler.loader.impl.LibraryStreamInputSource;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLAttributeType;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLInclude;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLNamespaceImport;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.util.SchemaCompilerTestUtils;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Verifies the operation of the various integrity-checker listener routines.
 * 
 * @author S. Livezey
 */
public class TestIntegrityCheckers extends AbstractModelEventTests {
	
	@Test
	public void testTypeAssignmentChangeIntegrityChecker() throws Exception {
		ModelEventListener<?,?> listener = new ModelIntegrityChecker();
		TLLibrary library1p1 = (TLLibrary) testModel.getLibrary(PACKAGE_1_NAMESPACE, "library_1_p1");
		TLLibrary library1p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
		TLLibrary library2p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_2_p2");
		TLValueWithAttributes testVWA = library2p2.getValueWithAttributesType("MyVWA");
		TLSimple testType = library2p2.getSimpleType("Counter_4");
		TLAttributeType originalParentType = testType.getParentType();
		TLSimple counter1 = library1p1.getSimpleType("Counter_1");
		TLSimple counter3 = library1p2.getSimpleType("Counter_3");
		try {
			testModel.addListener(listener);
			
			// Minor change for test setup
			library2p2.removeNamedMember(testVWA);
			
			// Make sure our setup data is correct
			assertNotNull(testType);
			assertNotNull(counter1);
			assertNotNull(counter3);
			assertDoesNotIncludeLibrary(library2p2, "library_1_p2.xml");
			assertImportsNamespace(library2p2, PACKAGE_1_NAMESPACE, "../test-package_v1/library_2_p1.xml");
			assertDoesNotImportFileHint(library2p2, PACKAGE_1_NAMESPACE, "../test-package_v1/library_1_p1.xml");
			
			// Test adding an import
			testType.setParentType(counter1);
			assertImportsNamespace(library2p2, PACKAGE_1_NAMESPACE, "../test-package_v1/library_1_p1.xml");
			
			// Test removing an import
			testType.setParentType(null);
			assertDoesNotImportNamespace(library2p2, PACKAGE_1_NAMESPACE);
			
			// Test adding an include
			testType.setParentType(counter3);
			assertIncludesLibrary(library2p2, "library_1_p2.xml");
			
			// Test removing an include
			testType.setParentType(originalParentType);
			assertDoesNotIncludeLibrary(library2p2, "library_1_p2.xml");
			
		} finally {
			library2p2.addNamedMember(testVWA);
			testType.setParentType(originalParentType);
			testModel.removeListener(listener);
		}
	}
	
	@Test
	public void testFacetMemberRemovalIntegrityChecker() throws Exception {
		ModelEventListener<?,?> listener = new ModelIntegrityChecker();
		TLLibrary library2p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_2_p2");
		TLValueWithAttributes testVWA = library2p2.getValueWithAttributesType("MyVWA");
		TLAttribute testAttr = testVWA.getAttribute("testAttr");
		try {
			testModel.addListener(listener);
			
			// Make sure our setup data is correct
			assertNotNull(testVWA);
			assertNotNull(testAttr);
			assertIncludesLibrary(library2p2, "library_1_p2.xml");
			
			// Test removing an attribute
			testVWA.removeAttribute(testAttr);
			assertDoesNotIncludeLibrary(library2p2, "library_1_p2.xml");
			
		} finally {
			testVWA.addAttribute(testAttr);
			testModel.removeListener(listener);
		}
	}
	
	@Test
	public void testTypeNameIntegrityChecker() throws Exception {
		ModelEventListener<?,?> listener = new ModelIntegrityChecker();
		TLLibrary library1p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
		TLLibrary library2p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_2_p2");
		TLSimple testType = library2p2.getSimpleType("SampleDocumentation");
		TLAttributeType originalParentType = testType.getParentType();
		TLSimple counter3 = library1p2.getSimpleType("Counter_3");
		try {
			testModel.addListener(listener);
			
			// Verify setup of test data
			assertEquals("Counter_3", counter3.getName());
			assertEquals("xsd:string", testType.getParentTypeName());
			
			testType.setParentType(counter3);
			assertEquals("Counter_3", testType.getParentTypeName());
			testType.setParentType(originalParentType);
			assertEquals("xsd:string", testType.getParentTypeName());
			
		} finally {
			testType.setParentType(originalParentType);
			testModel.removeListener(listener);
		}
	}
	
	@Test
	public void testNameChangeIntegrityChecker() throws Exception {
		ModelEventListener<?,?> listener = new ModelIntegrityChecker();
		TLLibrary library1p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
		TLLibrary library2p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_2_p2");
		TLSimple testType = library2p2.getSimpleType("SampleDocumentation");
		TLAttributeType originalParentType = testType.getParentType();
		TLSimple counter3 = library1p2.getSimpleType("Counter_3");
		try {
			testModel.addListener(listener);
			
			// Verify setup of test data
			assertEquals("Counter_3", counter3.getName());
			assertEquals("xsd:string", testType.getParentTypeName());
			
			testType.setParentType(counter3);
			assertEquals("Counter_3", testType.getParentTypeName());
			
			counter3.setName("test_Counter_3");
			assertEquals("test_Counter_3", testType.getParentTypeName());
			
			counter3.setName("Counter_3");
			assertEquals("Counter_3", testType.getParentTypeName());
			
		} finally {
			testType.setParentType(originalParentType);
			testModel.removeListener(listener);
		}
	}
	
	@Test
	public void testPrefixChangeIntegrityChecker() throws Exception {
		ModelEventListener<?,?> listener = new ModelIntegrityChecker();
		TLLibrary library1p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
		TLSimple counter3 = library1p2.getSimpleType("Counter_3");
		try {
			TLNamespaceImport package1Import = null;
			
			for (TLNamespaceImport nsImport : library1p2.getNamespaceImports()) {
				if (nsImport.getNamespace().equals(PACKAGE_1_NAMESPACE)) {
					package1Import = nsImport;
					break;
				}
			}
			assertNotNull(package1Import);
			
			testModel.addListener(listener);
			
			// Verify setup of test data
			assertEquals("Counter_3", counter3.getName());
			assertEquals("pkg1:Counter_1", counter3.getParentTypeName());
			
			package1Import.setPrefix("pkg1_test");
			assertEquals("pkg1_test:Counter_1", counter3.getParentTypeName());
			
			package1Import.setPrefix("pkg1");
			assertEquals("pkg1:Counter_1", counter3.getParentTypeName());
			
		} finally {
			testModel.removeListener(listener);
		}
	}
	
	@Test
	public void testNamespaceChangeIntegrityChecker() throws Exception {
		ModelEventListener<?,?> listener = new ModelIntegrityChecker();
		TLLibrary library2p1 = (TLLibrary) testModel.getLibrary(PACKAGE_1_NAMESPACE, "library_2_p1");
		TLLibrary library2p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_2_p2");
		try {
			testModel.addListener(listener);
			
			// Make sure our setup data is correct
			assertIncludesLibrary(library2p2, "library_1_p2.xml");
			assertDoesNotIncludeLibrary(library2p2, "../test-package_v1/library_2_p1.xml");
			assertImportsNamespace(library2p2, PACKAGE_1_NAMESPACE, "../test-package_v1/library_2_p1.xml");
			
			// Test moving a library from package-1 to package-2
			library2p1.setNamespace(PACKAGE_2_NAMESPACE);
			assertIncludesLibrary(library2p2, "../test-package_v1/library_2_p1.xml");
			assertDoesNotImportNamespace(library2p2, PACKAGE_1_NAMESPACE);
			
			library2p1.setNamespace(PACKAGE_1_NAMESPACE);
			assertDoesNotIncludeLibrary(library2p2, "../test-package_v1/library_2_p1.xml");
			assertImportsNamespace(library2p2, PACKAGE_1_NAMESPACE, "../test-package_v1/library_2_p1.xml");
			
		} finally {
			testModel.removeListener(listener);
		}
	}
	
	@Test
	public void testContextIntegrityCheckers() throws Exception {
		ModelEventListener<?,?> listener = new ModelIntegrityChecker();
		TLLibrary library1p2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
		TLSimple counter3 = library1p2.getSimpleType("Counter_3");
		int originalContextCount = library1p2.getContexts().size();
		try {
			TLExample testExample = new TLExample();
			testExample.setContext("unit-test-context");
			testExample.setValue("123");
			
			testModel.addListener(listener);
			
			// Verify setup of test data
			assertNull( library1p2.getContext("unit-test-context") );
			assertNotNull(counter3);
			assertEquals(0, counter3.getExamples().size());
			
			// Add an example and ensure it is auto-declared in the library
			counter3.addExample(testExample);
			assertEquals(originalContextCount + 1, library1p2.getContexts().size());
			assertNotNull( library1p2.getContext("unit-test-context") );
			
			// Modify the example's context value, and make sure it is auto-declared in the library
			testExample.setContext("unit-test-context2");
			assertEquals(originalContextCount + 2, library1p2.getContexts().size());
			assertNotNull( library1p2.getContext("unit-test-context2") );
			
			// Delete the test context declarations from the library, and make sure our example got deleted
			library1p2.removeContext( library1p2.getContext("unit-test-context") );
			library1p2.removeContext( library1p2.getContext("unit-test-context2") );
			assertEquals(originalContextCount, library1p2.getContexts().size());
			assertEquals(0, counter3.getExamples().size());
			
		} finally {
			testModel.removeListener(listener);
		}
	}
	
	@Test
	public void testLoadModelWithIntegrityCheckersEnabled() throws Exception {
		TLModel model = new TLModel();
		model.addListener( new ModelIntegrityChecker() );
		
		File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v2/library_1_p2.xml");
		LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(sourceFile);
		LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>(model);
		ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);
		
		SchemaCompilerTestUtils.printFindings(findings);
		assertFalse(findings.hasFinding(FindingType.ERROR));
	}
	
	protected void displayImportsAndIncludes(TLLibrary library) {
		System.out.println("Includes:");
		for (TLInclude include : library.getIncludes()) {
			System.out.println("  " + include.getPath());
		}
		System.out.println("Imports:");
		for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
			System.out.println("  " + nsImport.getNamespace());
			for (String fileHint : nsImport.getFileHints()) {
				System.out.println("    " + fileHint);
			}
		}
		System.out.println();
	}
	
	protected void assertImportsNamespace(TLLibrary library, String namespace, String fileHint) {
		for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
			if (namespace.equals(nsImport.getNamespace())) {
				if (fileHint != null) {
					for (String hint : nsImport.getFileHints()) {
						if (fileHint.equals(hint)) {
							return;
						}
					}
					fail("Expected file hint not expected in import: " + fileHint);
				} else {
					return;
				}
			}
		}
		fail("Expected import not found for namespace: " + namespace);
	}
	
	protected void assertIncludesLibrary(TLLibrary library, String relativeUrl) {
		for (TLInclude include : library.getIncludes()) {
			if (relativeUrl.equals(include.getPath())) {
				return;
			}
		}
		fail("Expected library include not found: " + relativeUrl);
	}
	
	protected void assertDoesNotImportNamespace(TLLibrary library, String namespace) {
		for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
			if (namespace.equals(nsImport.getNamespace())) {
				fail("Namespace not expected among library imports: " + namespace);
			}
		}
	}
	
	protected void assertDoesNotImportFileHint(TLLibrary library, String namespace, String fileHint) {
		for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
			if (namespace.equals(nsImport.getNamespace())) {
				for (String hint : nsImport.getFileHints()) {
					if (fileHint.equals(hint)) {
						fail("File hint not expected among library imports: " + fileHint);
					}
				}
			}
		}
	}
	
	protected void assertDoesNotIncludeLibrary(TLLibrary library, String relativeUrl) {
		for (TLInclude include : library.getIncludes()) {
			if (relativeUrl.equals(include.getPath())) {
				fail("Library not expected among includes: " + relativeUrl);
			}
		}
	}
	
}
