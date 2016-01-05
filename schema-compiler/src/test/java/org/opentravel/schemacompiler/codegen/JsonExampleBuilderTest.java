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
package org.opentravel.schemacompiler.codegen;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.codegen.example.ExampleJsonBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions.DetailLevel;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.AbstractTestTransformers;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Verifies the operation of the <code>JsonExampleBuilder</code>.
 * 
 * @author E. Bronson
 */
public class JsonExampleBuilderTest extends AbstractTestTransformers {

	private TLFacet testFacet;

	private ExampleJsonBuilder exampleBuilder;

	private ExampleGeneratorOptions options;

	@Before
	public void setUp() throws Exception {
		TLBusinessObject exampleBusinessObject = getBusinessObject(
				PACKAGE_2_NAMESPACE, "library_3_p2", "ExampleBusinessObject");
		testFacet = exampleBusinessObject.getDetailFacet();
		options = new ExampleGeneratorOptions();

		options.setDetailLevel(DetailLevel.MAXIMUM);
		options.setPreferredFacet(exampleBusinessObject, testFacet);
		exampleBuilder = new ExampleJsonBuilder(options);
		exampleBuilder.setModelElement(exampleBusinessObject);
	}

	@Test
	public void testItShouldCreateANodeForEachProperty() throws Exception {

		try {
			JsonNode node = exampleBuilder.buildTree();

			String exampleContent = exampleBuilder.buildString();
			assertNotNull(exampleContent);
			assertTrue("Top level node is not an object.", node.isObject());
			assertNotNull(
					"No top level object.",
					node.findValue(XsdCodegenUtils.getGlobalElementName(
							testFacet).getLocalPart()));
			List<TLProperty> elements = testFacet.getElements();
			assertTrue("No elements.", elements.size() > 0);
			for (TLProperty tlprop : elements) {
				String name = tlprop.getName();
				assertNotNull("Field " + name + " not found.",
						node.findValue(name));
			}
			List<TLAttribute> attributes = testFacet.getAttributes();
			assertTrue("No attributes.", attributes.size() > 0);
			for (TLAttribute tlatt : attributes) {
				String name = tlatt.getName();
				assertNotNull("No field found.", node.findValue(name));
			}

		} catch (ValidationException e) {
			SchemaCompilerTestUtils.printFindings(e.getFindings());
			assertFalse(e.getFindings().hasFinding(FindingType.ERROR));
			fail();
		}
	}

	@Test
	public void testRepeatablePropertiesShouldBeAnArray() throws Exception {
		JsonNode node = exampleBuilder.buildTree();
		for (TLProperty tlprop : testFacet.getElements()) {
			if (tlprop.getRepeat() > 1) {
				String name = tlprop.getName();
				assertTrue("Node " + name + " is not an array.", node
						.findValue(name).isArray());
			}
		}
	}

	@Test
	public void testSimpleListsShouldBeAnArray() throws Exception {
		JsonNode node = exampleBuilder.buildTree();
		TLAttribute attribute = testFacet.getAttribute("sampleCoreAttrlist");
		TLAttributeType type = attribute.getType();
		assertTrue(type instanceof TLListFacet);
		assertTrue(((TLListFacet) type).getItemFacet() instanceof TLSimpleFacet);
		String name = attribute.getName();
		assertTrue("Node is not an array.", node.findValue(name).isArray());
	}

	@Test
	public void testItShouldHaveATypeValue() throws Exception {
			JsonNode node = exampleBuilder.buildTree();
			JsonNode jn = node.findValue("SampleBusinessObject");
			assertTrue(jn.isArray());
			ArrayNode an = (ArrayNode) jn;
			Iterator<JsonNode> nodeIt = an.elements();
			while (nodeIt.hasNext()) {
				JsonNode item = nodeIt.next();
				assertTrue(item.has("@type"));
			}
	}

	@Test
	public void testItShouldHaveAValueForVWAs() throws Exception {
		TLLibrary library = getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
		TLValueWithAttributes vwa = library
				.getValueWithAttributesType("SampleValueWithAttributes");
		exampleBuilder = new ExampleJsonBuilder(options);
		exampleBuilder.setModelElement(vwa);
		JsonNode node = exampleBuilder.buildTree();
		assertNotNull(node.findValue("value"));
		for (TLAttribute att : vwa.getAttributes()) {
			assertNotNull(node.findValue(att.getName()));
		}
	}

	@Test
	public void testItShouldHaveAValueForOpenEnums() throws Exception {
		TLLibrary library = getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
		TLOpenEnumeration open = library
				.getOpenEnumerationType("SampleEnum_Open");
		exampleBuilder = new ExampleJsonBuilder(options);
		exampleBuilder.setModelElement(open);
		JsonNode node = exampleBuilder.buildTree();
		assertNotNull(node.findValue("value"));
	}

	@Test
	public void testItShouldHaveAnExtension() throws Exception {
		TLBusinessObject compoundBusinessObject = getBusinessObject(
				PACKAGE_2_NAMESPACE, "library_1_p2", "CompoundBusinessObject");
		TLFacet facet = compoundBusinessObject.getSummaryFacet();
		exampleBuilder = new ExampleJsonBuilder(options);
		exampleBuilder.setModelElement(facet);
		JsonNode node = exampleBuilder.buildTree();
		assertNotNull(node
				.findValue("ExtensionPoint_CompoundBusinessObject_Summary"));
	}
	
	@Test
	public void testItShouldHaveAnIdRef() throws Exception {
		TLBusinessObject exampleBusinessObject = getBusinessObject(
				PACKAGE_2_NAMESPACE, "library_3_p2", "ExampleBusinessObject2");
		TLFacet facet = exampleBusinessObject.getSummaryFacet();
		exampleBuilder = new ExampleJsonBuilder(options);
		exampleBuilder.setModelElement(facet);
		JsonNode node = exampleBuilder.buildTree();
		JsonNode refNode = node
				.findValue("VWAWithIDRef");
		assertNotNull(refNode);
		assertTrue(refNode.asText().contains("vWAWithID"));
	}
	
	@Test
	public void testItShouldHaveAnId() throws Exception {
		JsonNode node = exampleBuilder.buildTree();
		JsonNode refNode = node
				.findValue("Id");
		assertNotNull(refNode);
		assertTrue(refNode.asText().contains("exampleBusinessObject"));
	}
	
	@Test
	public void testItShouldHaveASimpleValue() throws Exception {
		TLLibrary library = getLibrary(PACKAGE_2_NAMESPACE, "library_2_p2");
		TLSimple simple = library
				.getSimpleType("Counter_4");
		exampleBuilder = new ExampleJsonBuilder(options);
		exampleBuilder.setModelElement(simple);
		JsonNode node = exampleBuilder.buildTree();
		assertTrue(node.hasNonNull("Counter_4"));
	}
	
	@Test
	public void testItShouldHaveASimpleValueList() throws Exception {
		TLLibrary library = getLibrary(PACKAGE_2_NAMESPACE, "library_2_p2");
		TLSimple simple = library
				.getSimpleType("Counter_4_List");
		exampleBuilder = new ExampleJsonBuilder(options);
		exampleBuilder.setModelElement(simple);
		JsonNode node = exampleBuilder.buildTree();
		JsonNode jn = node.findValue("Counter_4_List");
		assertNotNull(jn);
		assertTrue(jn.isArray());
		assertTrue(((ArrayNode) jn).size() > 1);
		assertTrue(node.hasNonNull("Counter_4_List"));
	}


	private TLBusinessObject getBusinessObject(String namespace,
			String libraryName, String typeName) throws Exception {
		TLLibrary library = getLibrary(namespace, libraryName);

		return (library == null) ? null : library
				.getBusinessObjectType(typeName);
	}

	/**
	 * @see org.opentravel.schemacompiler.transform.AbstractTestTransformers#getBaseLocation()
	 */
	@Override
	protected String getBaseLocation() {
		return SchemaCompilerTestUtils.getBaseLibraryLocation();
	}

}
