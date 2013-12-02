/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.sabre.schemacompiler.codegen.example.ExampleDocumentBuilder;
import com.sabre.schemacompiler.codegen.example.ExampleGeneratorOptions;
import com.sabre.schemacompiler.codegen.example.ExampleGeneratorOptions.DetailLevel;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.transform.AbstractTestTransformers;
import com.sabre.schemacompiler.util.SchemaCompilerTestUtils;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationException;

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
	 * @see com.sabre.schemacompiler.transform.AbstractTestTransformers#getBaseLocation()
	 */
	@Override
	protected String getBaseLocation() {
		return SchemaCompilerTestUtils.getBaseLibraryLocation();
	}
	
}
