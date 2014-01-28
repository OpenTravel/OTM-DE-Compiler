/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

import org.junit.BeforeClass;
import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.LibraryNamespaceResolver;
import org.opentravel.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Abstract base class for test classes that validate the code generation subsystem.
 * 
 * @author S. Livezey
 */
public abstract class AbstractTestCodeGenerators {
	
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
	
	protected TLLibrary getLibrary(String namespace, String libraryName) {
		TLLibrary library = null;
		
		for (AbstractLibrary lib : testModel.getLibrariesForNamespace(namespace)) {
			if (lib.getName().equals(libraryName)) {
				library = (TLLibrary) lib;
				break;
			}
		}
		return library;
	}
	
	protected TLService getService(String libraryNamespace, String libraryName) {
		TLLibrary library = getLibrary(libraryNamespace, libraryName);
		
		return (library == null) ? null : library.getService();
	}
	
	protected CodeGenerationContext getContext() {
		CodeGenerationContext context = new CodeGenerationContext();
		
		context.setValue(CodeGenerationContext.CK_OUTPUT_FOLDER,
				System.getProperty("user.dir") + "/target/codegen-output");
		context.setValue(CodeGenerationContext.CK_SERVICE_ENDPOINT_URL, "http://www.OpenTravel.org/services");
		return context;
	}
	
}
