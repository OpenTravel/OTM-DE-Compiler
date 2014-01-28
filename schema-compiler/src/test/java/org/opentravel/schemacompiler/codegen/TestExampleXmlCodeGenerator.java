/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions.DetailLevel;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.AbstractTestTransformers;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;

/**
 * Verifies the operation of the <code>ExampleXmlCodeGenerator</code>.
 * 
 * @author S. Livezey
 */
public class TestExampleXmlCodeGenerator extends AbstractTestTransformers {
	
	@Test
	public void testExampleXmlGenerator() throws Exception {
		TLBusinessObject sampleBusinessObject = getBusinessObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleBusinessObject");
		TLBusinessObject compoundBusinessObject = getBusinessObject(PACKAGE_2_NAMESPACE, "library_1_p2", "CompoundBusinessObject");
		TLFacet customFacet = sampleBusinessObject.getCustomFacet("Sample", "Test1");
		ExampleGeneratorOptions options = new ExampleGeneratorOptions();
		
		options.setDetailLevel(DetailLevel.MAXIMUM);
		
		assertNotNull(sampleBusinessObject);
		assertNotNull(compoundBusinessObject);
		assertNotNull(customFacet);
		
		try {
			String exampleContent = new ExampleDocumentBuilder(options).setModelElement(compoundBusinessObject).buildString();
			assertNotNull(exampleContent); // for now, just make sure we generate some content without error
			
		} catch (ValidationException e) {
			SchemaCompilerTestUtils.printFindings(e.getFindings());
			assertFalse( e.getFindings().hasFinding(FindingType.ERROR) );
		}
	}
	
	private TLBusinessObject getBusinessObject(String namespace, String libraryName, String typeName) throws Exception {
		TLLibrary library = getLibrary(namespace, libraryName);
		
		return (library == null) ? null : library.getBusinessObjectType(typeName);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.transform.AbstractTestTransformers#getBaseLocation()
	 */
	@Override
	protected String getBaseLocation() {
		return SchemaCompilerTestUtils.getBaseLibraryLocation();
	}
	
}
