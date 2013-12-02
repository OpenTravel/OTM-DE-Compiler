/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.event;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

import org.junit.BeforeClass;

import com.sabre.schemacompiler.loader.LibraryModelLoader;
import com.sabre.schemacompiler.loader.LibraryNamespaceResolver;
import com.sabre.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.util.SchemaCompilerTestUtils;
import com.sabre.schemacompiler.validate.FindingMessageFormat;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Base class that provides common methods used for testing the event sub-system of the schema compiler.
 * 
 * @author S. Livezey
 */
public abstract class AbstractModelEventTests {
	
	public static final String PACKAGE_1_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1";
	public static final String PACKAGE_2_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2";
	
	protected static TLModel testModel;
	
	@BeforeClass
	public static void loadTestModel() throws Exception {
		LibraryNamespaceResolver namespaceResolver = new CatalogLibraryNamespaceResolver(
				new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/library-catalog.xml"));
		LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
		
		modelLoader.setNamespaceResolver(namespaceResolver);
		ValidationFindings findings = modelLoader.loadLibraryModel(new URI(PACKAGE_2_NAMESPACE));
		
		// Display any error messages to standard output before returning
		if (findings.hasFinding(FindingType.ERROR)) {
			String[] messages = findings.getValidationMessages(
					FindingType.ERROR, FindingMessageFormat.DEFAULT);
			
			System.out.println("Problems occurred while loading the test model:");
			
			for (String message : messages) {
				System.out.println("  " + message);
			}
		}
		testModel = modelLoader.getLibraryModel();
	}
	
}
