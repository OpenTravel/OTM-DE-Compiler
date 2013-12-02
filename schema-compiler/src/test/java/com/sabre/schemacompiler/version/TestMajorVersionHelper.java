/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLValueWithAttributes;

/**
 * Verifies the operation of the <code>MajorVersionHelper</code> class.
 * 
 * @author S. Livezey
 */
public class TestMajorVersionHelper extends AbstractVersionHelperTests {
	
	@Test
	public void testGetVersionChain_latestVersion() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_2, TEST_LIBRARY_NAME);
		MajorVersionHelper helper = new MajorVersionHelper();
		List<TLLibrary> versionChain = helper.getVersionChain(patchVersionLibrary);
		
		assertEquals( 5, versionChain.size() );
		assertEquals( NS_VERSION_1_2_2, versionChain.get(0).getNamespace() );
		assertEquals( NS_VERSION_1_2_1, versionChain.get(1).getNamespace() );
		assertEquals( NS_VERSION_1_2, versionChain.get(2).getNamespace() );
		assertEquals( NS_VERSION_1_1, versionChain.get(3).getNamespace() );
		assertEquals( NS_VERSION_1, versionChain.get(4).getNamespace() );
	}
	
	@Test
	public void testGetVersionChain_notLatestVersion() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_1, TEST_LIBRARY_NAME);
		MajorVersionHelper helper = new MajorVersionHelper();
		List<TLLibrary> versionChain = helper.getVersionChain(patchVersionLibrary);
		
		assertEquals( 4, versionChain.size() );
		assertEquals( NS_VERSION_1_2_1, versionChain.get(0).getNamespace() );
		assertEquals( NS_VERSION_1_2, versionChain.get(1).getNamespace() );
		assertEquals( NS_VERSION_1_1, versionChain.get(2).getNamespace() );
		assertEquals( NS_VERSION_1, versionChain.get(3).getNamespace() );
	}
	
	@Test
	public void testGetMajorVersion() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_2, TEST_LIBRARY_NAME);
		MajorVersionHelper helper = new MajorVersionHelper();
		
		assertNotNull( majorVersionLibrary );
		assertNotNull( minorVersionLibrary );
		assertNotNull( patchVersionLibrary );
		
		assertEquals( majorVersionLibrary, helper.getMajorVersion(majorVersionLibrary) );
		assertEquals( majorVersionLibrary, helper.getMajorVersion(minorVersionLibrary) );
		assertEquals( majorVersionLibrary, helper.getMajorVersion(patchVersionLibrary) );
		
		model.removeLibrary( majorVersionLibrary );
		assertEquals( null, helper.getMajorVersion(minorVersionLibrary) );
		assertEquals( null, helper.getMajorVersion(patchVersionLibrary) );
	}
	
	@Test
	public void testNewMajorLibraryVersion_fromMajorVersion() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2, FILE_VERSION_1_0_1);
		TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
		File newVersionLibraryFile = purgeExistingFile( new File(System.getProperty("user.dir"), "/target/test-save-location/library_v02_00.otm") );
		MajorVersionHelper helper = new MajorVersionHelper();
		
		TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion(majorVersionLibrary, newVersionLibraryFile);
		
		assertNotNull( newMajorVersionLibrary );
		assertEquals( "2.0.0", newMajorVersionLibrary.getVersion() );
		assertEquals( majorVersionLibrary.getName(), newMajorVersionLibrary.getName() );
		assertEquals( majorVersionLibrary.getBaseNamespace(), newMajorVersionLibrary.getBaseNamespace() );
		assertEquals( majorVersionLibrary.getComments(), newMajorVersionLibrary.getComments() );
		validateLibraryContents( newMajorVersionLibrary );
	}
	
	@Test
	public void testNewMajorLibraryVersion_fromMinorVersion() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2, FILE_VERSION_1_0_1);
		TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
		File newVersionLibraryFile = purgeExistingFile( new File(System.getProperty("user.dir"), "/target/test-save-location/library_v02_00.otm") );
		MajorVersionHelper helper = new MajorVersionHelper();
		
		TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion(minorVersionLibrary, newVersionLibraryFile);
		
		assertNotNull( newMajorVersionLibrary );
		assertEquals( "2.0.0", newMajorVersionLibrary.getVersion() );
		assertEquals( minorVersionLibrary.getName(), newMajorVersionLibrary.getName() );
		assertEquals( minorVersionLibrary.getBaseNamespace(), newMajorVersionLibrary.getBaseNamespace() );
		assertEquals( minorVersionLibrary.getComments(), newMajorVersionLibrary.getComments() );
		validateLibraryContents( newMajorVersionLibrary );
	}
	
	@Test
	public void testNewMajorLibraryVersion_fromPatchVersion() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2, FILE_VERSION_1_0_1);
		TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_1, TEST_LIBRARY_NAME);
		File newVersionLibraryFile = purgeExistingFile( new File(System.getProperty("user.dir"), "/target/test-save-location/library_v02_00.otm") );
		MajorVersionHelper helper = new MajorVersionHelper();
		
		TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion(patchVersionLibrary, newVersionLibraryFile);
		
		assertNotNull( newMajorVersionLibrary );
		assertEquals( "2.0.0", newMajorVersionLibrary.getVersion() );
		assertEquals( patchVersionLibrary.getName(), newMajorVersionLibrary.getName() );
		assertEquals( patchVersionLibrary.getBaseNamespace(), newMajorVersionLibrary.getBaseNamespace() );
		assertEquals( patchVersionLibrary.getComments(), newMajorVersionLibrary.getComments() );
		validateLibraryContents( newMajorVersionLibrary );
	}
	
	private void validateLibraryContents(TLLibrary newMajorVersionLibrary) throws Exception {
		
		// Verify that all required simple types were carried forward
		assertNotNull( newMajorVersionLibrary.getSimpleType("TestMajorVersionSimple") );
		assertNotNull( newMajorVersionLibrary.getClosedEnumerationType("TestMajorVersionClosedEnum") );
		assertNotNull( newMajorVersionLibrary.getOpenEnumerationType("TestMajorVersionOpenEnum") );
		assertNotNull( newMajorVersionLibrary.getSimpleType("TestMinorVersionSimple") );
		assertNotNull( newMajorVersionLibrary.getClosedEnumerationType("TestMinorVersionClosedEnum") );
		assertNotNull( newMajorVersionLibrary.getOpenEnumerationType("TestMinorVersionOpenEnum") );
		assertNotNull( newMajorVersionLibrary.getSimpleType("TestPatchSimple") );
		assertNotNull( newMajorVersionLibrary.getClosedEnumerationType("TestPatchClosedEnum") );
		assertNotNull( newMajorVersionLibrary.getOpenEnumerationType("TestPatchOpenEnum") );
		
		
		// Validate the 'Lookup...' grouping
		TLBusinessObject lookupBO = newMajorVersionLibrary.getBusinessObjectType("LookupBO");
		TLCoreObject lookupCore = newMajorVersionLibrary.getCoreObjectType("LookupCore");
		TLValueWithAttributes lookupVWA = newMajorVersionLibrary.getValueWithAttributesType("LookupVWA");
		TLOperation lookupOp = newMajorVersionLibrary.getService().getOperation("LookupOperation");
		
		assertNotNull( lookupBO );
		assertEquals( 2, lookupBO.getSummaryFacet().getAttributes().size() );
		assertEquals( 3, lookupBO.getSummaryFacet().getElements().size() );
		assertContainsAttributes(lookupBO.getSummaryFacet(), "extBOAttribute121", "extBOAttribute122");
		assertContainsElements(lookupBO.getSummaryFacet(), "Element1", "Element11", "Element12");
		
		assertNotNull( lookupCore );
		assertEquals( 2, lookupCore.getSummaryFacet().getAttributes().size() );
		assertEquals( 3, lookupCore.getSummaryFacet().getElements().size() );
		assertContainsAttributes(lookupCore.getSummaryFacet(), "extCoreAttribute121", "extCoreAttribute122");
		assertContainsElements(lookupCore.getSummaryFacet(), "Element1", "Element11", "Element12");
		
		assertNotNull( lookupVWA );
		assertEquals( 3, lookupVWA.getAttributes().size() );
		assertContainsAttributes(lookupVWA, "vwaAttribute1", "vwaAttribute11", "vwaAttribute12");
		
		assertNotNull( lookupOp );
		assertEquals( 2, lookupOp.getRequest().getAttributes().size() );
		assertEquals( 3, lookupOp.getRequest().getElements().size() );
		assertContainsAttributes(lookupOp.getRequest(), "extOperationAttribute121", "extOperationAttribute122");
		assertContainsElements(lookupOp.getRequest(), "RequestValue1", "RequestValue11", "RequestValue12");
		
		
		// Validate the 'LaterMinorVersion...' grouping
		TLBusinessObject laterMinorVersionBO = newMajorVersionLibrary.getBusinessObjectType("LaterMinorVersionBO");
		TLCoreObject laterMinorVersionCore = newMajorVersionLibrary.getCoreObjectType("LaterMinorVersionCore");
		TLValueWithAttributes laterMinorVersionVWA = newMajorVersionLibrary.getValueWithAttributesType("LaterMinorVersionVWA");
		TLOperation laterMinorVersionOp = newMajorVersionLibrary.getService().getOperation("LaterMinorVersionOperation");
		
		assertNotNull( laterMinorVersionBO );
		assertEquals( 0, laterMinorVersionBO.getSummaryFacet().getAttributes().size() );
		assertEquals( 2, laterMinorVersionBO.getSummaryFacet().getElements().size() );
		assertContainsElements(laterMinorVersionBO.getSummaryFacet(), "Element1", "Element12");
		
		assertNotNull( laterMinorVersionCore );
		assertEquals( 0, laterMinorVersionCore.getSummaryFacet().getAttributes().size() );
		assertEquals( 2, laterMinorVersionCore.getSummaryFacet().getElements().size() );
		assertContainsElements(laterMinorVersionCore.getSummaryFacet(), "Element1", "Element12");
		
		assertNotNull( laterMinorVersionVWA );
		assertEquals( 2, laterMinorVersionVWA.getAttributes().size() );
		assertContainsAttributes(laterMinorVersionVWA, "vwaAttribute1", "vwaAttribute12");
		
		assertNotNull( laterMinorVersionOp );
		assertEquals( 0, laterMinorVersionOp.getRequest().getAttributes().size() );
		assertEquals( 2, laterMinorVersionOp.getRequest().getElements().size() );
		assertContainsElements(laterMinorVersionOp.getRequest(), "RequestValue1", "RequestValue12");
		
		
		// Validate the 'MinorVersionTest...' grouping
		TLBusinessObject minorVersionTestBO = newMajorVersionLibrary.getBusinessObjectType("MinorVersionTestBO");
		TLCoreObject minorVersionTestCore = newMajorVersionLibrary.getCoreObjectType("MinorVersionTestCore");
		TLValueWithAttributes minorVersionTestVWA = newMajorVersionLibrary.getValueWithAttributesType("MinorVersionTestVWA");
		TLOperation minorVersionTestOp = newMajorVersionLibrary.getService().getOperation("MinorVersionTestOperation");
		
		assertNotNull( minorVersionTestBO );
		assertEquals( 0, minorVersionTestBO.getSummaryFacet().getAttributes().size() );
		assertEquals( 1, minorVersionTestBO.getSummaryFacet().getElements().size() );
		assertContainsElements(minorVersionTestBO.getSummaryFacet(), "Element11");
		
		assertNotNull( minorVersionTestCore );
		assertEquals( 0, minorVersionTestCore.getSummaryFacet().getAttributes().size() );
		assertEquals( 1, minorVersionTestCore.getSummaryFacet().getElements().size() );
		assertContainsElements(minorVersionTestCore.getSummaryFacet(), "Element11");
		
		assertNotNull( minorVersionTestVWA );
		assertEquals( 1, minorVersionTestVWA.getAttributes().size() );
		assertContainsAttributes(minorVersionTestVWA, "vwaAttribute11");
		
		assertNotNull( minorVersionTestOp );
		assertEquals( 0, minorVersionTestOp.getRequest().getAttributes().size() );
		assertEquals( 1, minorVersionTestOp.getRequest().getElements().size() );
		assertContainsElements(minorVersionTestOp.getRequest(), "RequestValue11");
		
		
		// Validate the 'PatchTest...' grouping
		TLBusinessObject patchTestBO = newMajorVersionLibrary.getBusinessObjectType("PatchTestBO");
		TLCoreObject patchTestCore = newMajorVersionLibrary.getCoreObjectType("PatchTestCore");
		TLOperation patchTestOp = newMajorVersionLibrary.getService().getOperation("PatchTestOperation");
		
		assertNotNull( patchTestBO );
		assertEquals( 0, patchTestBO.getSummaryFacet().getAttributes().size() );
		assertEquals( 1, patchTestBO.getSummaryFacet().getElements().size() );
		assertContainsElements(patchTestBO.getSummaryFacet(), "Element12");
		
		assertNotNull( patchTestCore );
		assertEquals( 0, patchTestCore.getSummaryFacet().getAttributes().size() );
		assertEquals( 1, patchTestCore.getSummaryFacet().getElements().size() );
		assertContainsElements(patchTestCore.getSummaryFacet(), "Element12");
		
		assertNotNull( patchTestOp );
		assertEquals( 0, patchTestOp.getRequest().getAttributes().size() );
		assertEquals( 1, patchTestOp.getRequest().getElements().size() );
		assertContainsElements(patchTestOp.getRequest(), "RequestValue12");
		
		
		// Verify the total number of elements to make sure nothing
		// exists, except for the items we just tested.
		assertEquals( 21, newMajorVersionLibrary.getNamedMembers().size() );
		assertEquals( 4, newMajorVersionLibrary.getService().getOperations().size() );
	}
	
}
