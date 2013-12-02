/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.legacy_xsd;

import java.io.File;
import java.io.InputStream;

import org.junit.BeforeClass;

import com.sabre.schemacompiler.loader.LibraryInputSource;
import com.sabre.schemacompiler.loader.LibraryModelLoader;
import com.sabre.schemacompiler.loader.LibraryNamespaceResolver;
import com.sabre.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import com.sabre.schemacompiler.loader.impl.LibraryStreamInputSource;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.validate.FindingMessageFormat;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Abstract base class for test classes that validate the XSD object transformers.
 *
 * @author S. Livezey
 */
public class AbstractXSDTestTransformers {
	
	public static final String PACKAGE_1_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1";
	public static final String PACKAGE_2_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2";
	public static final String PACKAGE_3_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v3";
	public static final String LEGACY_NAMESPACE    = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/legacy_namespace";
	
	protected static TLModel testModel;
	
	@BeforeClass
	public static void loadTestModel() throws Exception {
		LibraryNamespaceResolver namespaceResolver = new CatalogLibraryNamespaceResolver(
				new File(System.getProperty("user.dir"), "/src/test/resources/libraries_1_4/empty-catalog.xml"));
		LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(
				new File(System.getProperty("user.dir"), "/src/test/resources/libraries_1_4/test-package_v3/sample_library.xml"));
		LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
		
		modelLoader.setNamespaceResolver(namespaceResolver);
		ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);
		
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
	
	protected NamedEntity getNamedEntity(String namespace, String memberName) {
		NamedEntity entity = null;
		
		for (AbstractLibrary library : testModel.getLibrariesForNamespace(namespace)) {
			for (NamedEntity member : library.getNamedMembers()) {
				if (member.getLocalName().equals(memberName)) {
					entity = member;
					break;
				}
			}
		}
		return entity;
	}
	
}
