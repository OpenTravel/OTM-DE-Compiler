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

package org.opentravel.schemacompiler.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;

/**
 * Verifies the operation of the <code>DocumentationUtils</code> functions.
 */
public class TestDocumentationUtils {
	
	private static final File TEST_PROJECT = new File( System.getProperty("user.dir" ), "/src/test/resources/projects_1_6/project_1.xml" );
	
	private static TLLibrary testLibrary;
	
	@BeforeClass
	public static void initTestLibrary() throws Exception {
		testLibrary = loadTestLibrary();
	}
	
	@Test
	public void testContextPath() throws Exception {
		testBuildPathAndResolve( testLibrary.getContext("test") );
	}
	
	@Test
	public void testLibraryMemberPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getBusinessObjectType("CompoundBusinessObject") );
	}
	
	@Test
	public void testServicePath() throws Exception {
		testBuildPathAndResolve( testLibrary.getService() );
	}
	
	@Test
	public void testOperationPath() throws Exception {
		testBuildPathAndResolve( testLibrary.getService()
				.getOperation("RequestResponseOperation") );
	}
	
	@Test
	public void testSimpleFacetPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getCoreObjectType("SampleCore").getSimpleFacet() );
	}
	
	@Test
	public void testFacetPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getBusinessObjectType("SampleBusinessObject")
				.getDetailFacet() );
	}
	
	@Test
	public void testContextualFacetPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getBusinessObjectType("SampleBusinessObject")
				.getCustomFacet("Test1") );
	}
	
	@Test
	public void testListFacetPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getCoreObjectType("SampleCore").getSummaryListFacet() );
		testBuildPathAndResolve(
				testLibrary.getCoreObjectType("SampleCore").getDetailListFacet() );
	}
	
	@Test
	public void testMemberFieldPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getChoiceObjectType("SampleChoice")
				.getChoiceFacet("ChoiceA")
				.getAttribute("attributeA"));
		testBuildPathAndResolve(
				testLibrary.getChoiceObjectType("SampleChoice")
				.getChoiceFacet("ChoiceA")
				.getElement("elementA"));
		testBuildPathAndResolve(
				testLibrary.getChoiceObjectType("SampleChoice")
				.getChoiceFacet("ChoiceA")
				.getIndicator("indicatorA"));
	}
	
	@Test
	public void testEnumValuePath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getOpenEnumerationType("SampleEnum_Open")
				.getValues().get(0) );
	}
	
	@Test
	public void testRolePath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getCoreObjectType("SampleCore")
				.getRoleEnumeration().getRoles().get(0) );
	}
	
	@Test
	public void testExtensionPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getOpenEnumerationType("SampleEnumExt_Open")
				.getExtension() );
		testBuildPathAndResolve(
				testLibrary.getChoiceObjectType("ExtendedChoice")
				.getExtension() );
	}
	
	@Test
	public void testResourceParentRefPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getResourceType("SampleResource")
				.getParentRefs().get(0) );
	}
	
	@Test
	public void testParamGroupPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getResourceType("SampleResource")
				.getParamGroup("IDParameters") );
	}
	
	@Test
	public void testParameterPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getResourceType("SampleResource")
				.getParamGroup("IDParameters")
				.getParameter("sample_oid") );
	}
	
	@Test
	public void testActionPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getResourceType("SampleResource")
				.getAction("Create") );
	}
	
	@Test
	public void testActionRequestPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getResourceType("SampleResource")
				.getAction("Create").getRequest() );
	}
	
	@Test
	public void testActionResponsePath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getResourceType("SampleResource")
				.getAction("Create").getResponses().get(0) );
	}
	
	@Test
	public void testActionFacetPath() throws Exception {
		testBuildPathAndResolve(
				testLibrary.getResourceType("SampleResource")
				.getActionFacet("ObjectList") );
	}
	
	private void testBuildPathAndResolve(TLDocumentationOwner origOwner) throws Exception {
		String docPath = DocumentationPathBuilder.buildPath( origOwner );
		TLDocumentationOwner finalOwner = DocumentationPathResolver.resolve( docPath, testLibrary );
		
		assertEquals( origOwner, finalOwner );
	}
	
	private static TLLibrary loadTestLibrary() throws Exception {
		ProjectManager projectManager = new ProjectManager( false );
		TLModel model;
		
		projectManager.loadProject( TEST_PROJECT );
		model = projectManager.getModel();
		return (TLLibrary) model.getLibrary(
				"http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2", "library_1_p2" );
	}
	
}
