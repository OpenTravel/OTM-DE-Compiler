
package org.opentravel.schemacompiler.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.tests.util.ModelBuilder;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;
import org.opentravel.schemacompiler.version.MajorVersionHelper;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.PatchVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;

/**
 * Verifies the operation of the <code>MinorVersionHelper</code> class.
 * 
 * @author S. Livezey
 */
public class TestMinorVersionHelper extends AbstractVersionHelperTests {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();
	
	@Test
	public void testGetLaterMinorVersionLibraries() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary1 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary2 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_2, TEST_LIBRARY_NAME);
		MinorVersionHelper helper = new MinorVersionHelper();
		List<String> laterMinorVersions;
		
		assertNotNull( majorVersionLibrary );
		assertNotNull( minorVersionLibrary1 );
		assertNotNull( minorVersionLibrary2 );
		assertNotNull( patchVersionLibrary );
		
		laterMinorVersions = getLibraryNames( helper.getLaterMinorVersions(majorVersionLibrary) );
		assertEquals( 2, laterMinorVersions.size() );
		assertEquals( LIBNAME_VERSION_1_1, laterMinorVersions.get(0) );
		assertEquals( LIBNAME_VERSION_1_2, laterMinorVersions.get(1) );
		
		laterMinorVersions = getLibraryNames( helper.getLaterMinorVersions(minorVersionLibrary1) );
		assertEquals( 1, laterMinorVersions.size() );
		assertEquals( LIBNAME_VERSION_1_2, laterMinorVersions.get(0) );
		
		laterMinorVersions = getLibraryNames( helper.getLaterMinorVersions(minorVersionLibrary2) );
		assertEquals( 0, laterMinorVersions.size() );
		
		laterMinorVersions = getLibraryNames( helper.getLaterMinorVersions(patchVersionLibrary) );
		assertEquals( 0, laterMinorVersions.size() );
	}
	
	@Test
	public void testGetPriorMinorVersionLibraries() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary1 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary2 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_2, TEST_LIBRARY_NAME);
		MinorVersionHelper helper = new MinorVersionHelper();
		TLLibrary priorMinorVersion;
		
		assertNotNull( majorVersionLibrary );
		assertNotNull( minorVersionLibrary1 );
		assertNotNull( minorVersionLibrary2 );
		assertNotNull( patchVersionLibrary );
		
		priorMinorVersion = helper.getPriorMinorVersion(majorVersionLibrary);
		assertNull( priorMinorVersion );
		
		priorMinorVersion = helper.getPriorMinorVersion(minorVersionLibrary1);
		assertEquals( LIBNAME_VERSION_1, getLibraryName( priorMinorVersion ) );
		
		priorMinorVersion = helper.getPriorMinorVersion(minorVersionLibrary2);
		assertEquals( LIBNAME_VERSION_1_1, getLibraryName( priorMinorVersion ) );
		
		priorMinorVersion = helper.getPriorMinorVersion(patchVersionLibrary);
		assertEquals( LIBNAME_VERSION_1_2, getLibraryName( priorMinorVersion ) );
	}
	
	@Test
	public void testGetPriorVersionExtension() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		TLLibrary priorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
		MinorVersionHelper helper = new MinorVersionHelper();
		TLBusinessObject currentVersionBO = minorVersionLibrary.getBusinessObjectType("LookupBO");
		TLCoreObject currentVersionCore = minorVersionLibrary.getCoreObjectType("LookupCore");
		TLValueWithAttributes currentVersionVWA = minorVersionLibrary.getValueWithAttributesType("LookupVWA");
		TLOperation currentVersionOp = minorVersionLibrary.getService().getOperation("LookupOperation");
		
		assertNotNull( currentVersionBO );
		assertNotNull( currentVersionCore );
		assertNotNull( currentVersionVWA );
		assertNotNull( currentVersionOp );
		
		TLBusinessObject priorVersionBO = helper.getPriorVersionExtension( currentVersionBO );
		TLCoreObject priorVersionCore = helper.getPriorVersionExtension( currentVersionCore );
		TLValueWithAttributes priorVersionVWA = helper.getPriorVersionExtension( currentVersionVWA );
		TLOperation priorVersionOp = helper.getPriorVersionExtension( currentVersionOp );
		
		assertNotNull( priorVersionBO );
		assertEquals( "1.1.0", priorVersionBO.getVersion() );
		assertTrue( priorVersionBO.getOwningLibrary() == priorVersionLibrary );
		
		assertNotNull( priorVersionCore );
		assertEquals( "1.1.0", priorVersionCore.getVersion() );
		assertTrue( priorVersionCore.getOwningLibrary() == priorVersionLibrary );
		
		assertNotNull( priorVersionVWA );
		assertEquals( "1.1.0", priorVersionVWA.getVersion() );
		assertTrue( priorVersionVWA.getOwningLibrary() == priorVersionLibrary );
		
		assertNotNull( priorVersionOp );
		assertEquals( "1.1.0", priorVersionOp.getVersion() );
		assertTrue( priorVersionOp.getOwningLibrary() == priorVersionLibrary );
	}
	
	@Test
	public void testGetAllPriorVersionExtensions() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		TLLibrary priorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
		TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
		MinorVersionHelper helper = new MinorVersionHelper();
		TLBusinessObject currentVersionBO = minorVersionLibrary.getBusinessObjectType("LookupBO");
		TLCoreObject currentVersionCore = minorVersionLibrary.getCoreObjectType("LookupCore");
		TLValueWithAttributes currentVersionVWA = minorVersionLibrary.getValueWithAttributesType("LookupVWA");
		TLOperation currentVersionOp = minorVersionLibrary.getService().getOperation("LookupOperation");
		
		assertNotNull( currentVersionBO );
		assertNotNull( currentVersionCore );
		assertNotNull( currentVersionVWA );
		assertNotNull( currentVersionOp );
		
		List<TLBusinessObject> priorBOVersions = helper.getAllPriorVersionExtensions( currentVersionBO );
		List<TLCoreObject> priorCoreVersions = helper.getAllPriorVersionExtensions( currentVersionCore );
		List<TLValueWithAttributes> priorVwaVersions = helper.getAllPriorVersionExtensions( currentVersionVWA );
		List<TLOperation> priorOpVersions = helper.getAllPriorVersionExtensions( currentVersionOp );
		
		assertEquals( 2, priorBOVersions.size() );
		assertEquals( "1.1.0", priorBOVersions.get(0).getVersion() );
		assertTrue( priorBOVersions.get(0).getOwningLibrary() == priorVersionLibrary );
		assertEquals( "1.0.0", priorBOVersions.get(1).getVersion() );
		assertTrue( priorBOVersions.get(1).getOwningLibrary() == majorVersionLibrary );
		
		assertEquals( 2, priorCoreVersions.size() );
		assertEquals( "1.1.0", priorCoreVersions.get(0).getVersion() );
		assertTrue( priorCoreVersions.get(0).getOwningLibrary() == priorVersionLibrary );
		assertEquals( "1.0.0", priorCoreVersions.get(1).getVersion() );
		assertTrue( priorCoreVersions.get(1).getOwningLibrary() == majorVersionLibrary );
		
		assertEquals( 2, priorVwaVersions.size() );
		assertEquals( "1.1.0", priorVwaVersions.get(0).getVersion() );
		assertTrue( priorVwaVersions.get(0).getOwningLibrary() == priorVersionLibrary );
		assertEquals( "1.0.0", priorVwaVersions.get(1).getVersion() );
		assertTrue( priorVwaVersions.get(1).getOwningLibrary() == majorVersionLibrary );
		
		assertEquals( 2, priorOpVersions.size() );
		assertEquals( "1.1.0", priorOpVersions.get(0).getVersion() );
		assertTrue( priorOpVersions.get(0).getOwningLibrary() == priorVersionLibrary );
		assertEquals( "1.0.0", priorOpVersions.get(1).getVersion() );
		assertTrue( priorOpVersions.get(1).getOwningLibrary() == majorVersionLibrary );
	}
	
	@Test
	public void testGetLaterMinorVersions() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary1 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary2 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		TLBusinessObject majorVersionBO = majorVersionLibrary.getBusinessObjectType("LookupBO");
		TLCoreObject majorVersionCore = majorVersionLibrary.getCoreObjectType("LookupCore");
		TLValueWithAttributes majorVersionVWA = majorVersionLibrary.getValueWithAttributesType("LookupVWA");
		TLOperation majorVersionOp = majorVersionLibrary.getService().getOperation("LookupOperation");
		MinorVersionHelper helper = new MinorVersionHelper();
		
		assertNotNull( majorVersionBO );
		assertNotNull( majorVersionCore );
		assertNotNull( majorVersionVWA );
		assertNotNull( majorVersionOp );
		
		List<TLBusinessObject> laterBOVersions = helper.getLaterMinorVersions( majorVersionBO );
		List<TLCoreObject> laterCoreVersions = helper.getLaterMinorVersions( majorVersionCore );
		List<TLValueWithAttributes> laterVwaVersions = helper.getLaterMinorVersions( majorVersionVWA );
		List<TLOperation> laterOpVersions = helper.getLaterMinorVersions( majorVersionOp );
		
		assertEquals( 2, laterBOVersions.size() );
		assertEquals( "1.1.0", laterBOVersions.get(0).getVersion() );
		assertTrue( laterBOVersions.get(0).getOwningLibrary() == minorVersionLibrary1 );
		assertEquals( "1.2.0", laterBOVersions.get(1).getVersion() );
		assertTrue( laterBOVersions.get(1).getOwningLibrary() == minorVersionLibrary2 );
		
		assertEquals( 2, laterCoreVersions.size() );
		assertEquals( "1.1.0", laterCoreVersions.get(0).getVersion() );
		assertTrue( laterCoreVersions.get(0).getOwningLibrary() == minorVersionLibrary1 );
		assertEquals( "1.2.0", laterCoreVersions.get(1).getVersion() );
		assertTrue( laterCoreVersions.get(1).getOwningLibrary() == minorVersionLibrary2 );
		
		assertEquals( 2, laterVwaVersions.size() );
		assertEquals( "1.1.0", laterVwaVersions.get(0).getVersion() );
		assertTrue( laterVwaVersions.get(0).getOwningLibrary() == minorVersionLibrary1 );
		assertEquals( "1.2.0", laterVwaVersions.get(1).getVersion() );
		assertTrue( laterVwaVersions.get(1).getOwningLibrary() == minorVersionLibrary2 );
		
		assertEquals( 2, laterOpVersions.size() );
		assertEquals( "1.1.0", laterOpVersions.get(0).getVersion() );
		assertTrue( laterOpVersions.get(0).getOwningLibrary() == minorVersionLibrary1 );
		assertEquals( "1.2.0", laterOpVersions.get(1).getVersion() );
		assertTrue( laterOpVersions.get(1).getOwningLibrary() == minorVersionLibrary2 );
	}
	
	@Test
	public void testGetEligibleMinorVersionTargets() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		File patchLibraryFile = purgeExistingFile( new File(System.getProperty("user.dir"), "/target/test-save-location/library_v01_03.otm") );
		MinorVersionHelper helper = new MinorVersionHelper();
		List<TLLibrary> eligibleLibraries;
		
		TLLibrary minorVersionLibrary10 = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary11 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary13 = helper.createNewMinorVersion(minorVersionLibrary12, patchLibraryFile);
		
		TLBusinessObject minorVersionBO = minorVersionLibrary10.getBusinessObjectType("LaterMinorVersionBO");
		TLCoreObject minorVersionCore = minorVersionLibrary10.getCoreObjectType("LaterMinorVersionCore");
		TLValueWithAttributes minorVersionVWA = minorVersionLibrary10.getValueWithAttributesType("LaterMinorVersionVWA");
		TLOperation minorVersionOp = minorVersionLibrary10.getService().getOperation("LaterMinorVersionOperation");
		
		eligibleLibraries = helper.getEligibleMinorVersionTargets( minorVersionBO );
		assertEquals( 2, eligibleLibraries.size() );
		assertTrue( eligibleLibraries.contains( minorVersionLibrary11 ) );
		assertTrue( eligibleLibraries.contains( minorVersionLibrary13 ) );
		
		eligibleLibraries = helper.getEligibleMinorVersionTargets( minorVersionCore );
		assertEquals( 2, eligibleLibraries.size() );
		assertTrue( eligibleLibraries.contains( minorVersionLibrary11 ) );
		assertTrue( eligibleLibraries.contains( minorVersionLibrary13 ) );
		
		eligibleLibraries = helper.getEligibleMinorVersionTargets( minorVersionVWA );
		assertEquals( 2, eligibleLibraries.size() );
		assertTrue( eligibleLibraries.contains( minorVersionLibrary11 ) );
		assertTrue( eligibleLibraries.contains( minorVersionLibrary13 ) );
		
		eligibleLibraries = helper.getEligibleMinorVersionTargets( minorVersionOp );
		assertEquals( 2, eligibleLibraries.size() );
		assertTrue( eligibleLibraries.contains( minorVersionLibrary11 ) );
		assertTrue( eligibleLibraries.contains( minorVersionLibrary13 ) );
	}
	
	@Test
	public void testGetPreferredMinorVersionTarget() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		File patchLibraryFile = purgeExistingFile( new File(System.getProperty("user.dir"), "/target/test-save-location/library_v01_03.otm") );
		MinorVersionHelper helper = new MinorVersionHelper();
		
		TLLibrary minorVersionLibrary10 = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary13 = helper.createNewMinorVersion(minorVersionLibrary12, patchLibraryFile);
		
		TLBusinessObject minorVersionBO = minorVersionLibrary10.getBusinessObjectType("LaterMinorVersionBO");
		TLCoreObject minorVersionCore = minorVersionLibrary10.getCoreObjectType("LaterMinorVersionCore");
		TLValueWithAttributes minorVersionVWA = minorVersionLibrary10.getValueWithAttributesType("LaterMinorVersionVWA");
		TLOperation minorVersionOp = minorVersionLibrary10.getService().getOperation("LaterMinorVersionOperation");
		
		assertTrue( helper.getPreferredMinorVersionTarget(minorVersionBO) == minorVersionLibrary13 );
		assertTrue( helper.getPreferredMinorVersionTarget(minorVersionCore) == minorVersionLibrary13 );
		assertTrue( helper.getPreferredMinorVersionTarget(minorVersionVWA) == minorVersionLibrary13 );
		assertTrue( helper.getPreferredMinorVersionTarget(minorVersionOp) == minorVersionLibrary13 );
	}
	
	@Test
	public void testNewMinorLibraryVersion() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		File newVersionLibraryFile = purgeExistingFile( new File(System.getProperty("user.dir"), "/target/test-save-location/library_v01_03.otm") );
		MinorVersionHelper helper = new MinorVersionHelper();
		
		TLLibrary newMinorVersionLibrary = helper.createNewMinorVersion(minorVersionLibrary, newVersionLibraryFile);
		TLBusinessObject newMinorVersionBO = newMinorVersionLibrary.getBusinessObjectType("LookupBO");
		TLCoreObject newMinorVersionCore = newMinorVersionLibrary.getCoreObjectType("LookupCore");
		TLOperation newMinorVersionOp = newMinorVersionLibrary.getService().getOperation("LookupOperation");
		TLSimple minorVersionSimple = newMinorVersionLibrary.getSimpleType("TestMinorVersionSimple");
		TLSimple minorVersionOpenEnum = newMinorVersionLibrary.getSimpleType("TestMinorVersionClosedEnum");
		TLSimple minorVersionClosedEnum = newMinorVersionLibrary.getSimpleType("TestMinorVersionOpenEnum");
		TLSimple patchSimple = newMinorVersionLibrary.getSimpleType("TestPatchSimple");
		TLClosedEnumeration patchClosedEnum = newMinorVersionLibrary.getClosedEnumerationType("TestPatchClosedEnum");
		TLOpenEnumeration patchOpenEnum = newMinorVersionLibrary.getOpenEnumerationType("TestPatchOpenEnum");
		
		assertNotNull( newMinorVersionLibrary );
		assertEquals( "1.3.0", newMinorVersionLibrary.getVersion() );
		assertEquals( minorVersionLibrary.getName(), newMinorVersionLibrary.getName() );
		assertEquals( minorVersionLibrary.getBaseNamespace(), newMinorVersionLibrary.getBaseNamespace() );
		assertEquals( minorVersionLibrary.getComments(), newMinorVersionLibrary.getComments() );
		
		assertNotNull( newMinorVersionBO );
		assertContainsAttributes(newMinorVersionBO.getSummaryFacet(), "extBOAttribute121", "extBOAttribute122");
		
		assertNotNull( newMinorVersionCore );
		assertContainsAttributes(newMinorVersionCore.getSummaryFacet(), "extCoreAttribute121", "extCoreAttribute122");
		
		assertNotNull( newMinorVersionOp );
		assertContainsAttributes(newMinorVersionOp.getRequest(), "extOperationAttribute121", "extOperationAttribute122");
		
		// Simple types defined in the previous minor version are not
		// copied forward to the new minor version
		assertNull( minorVersionSimple );
		assertNull( minorVersionClosedEnum );
		assertNull( minorVersionOpenEnum );
		
		// Simple type defined in a patch library ARE copied forward to
		// the new minor version
		assertNotNull( patchSimple );
		assertNotNull( patchClosedEnum );
		assertNotNull( patchOpenEnum );
		
		assertNull( newMinorVersionLibrary.getNamedMember("LookupVWA") );
		assertNull( newMinorVersionLibrary.getNamedMember("PatchTestCore") );
		assertNull( newMinorVersionLibrary.getNamedMember("PatchTestBO") );
		assertNull( newMinorVersionLibrary.getService().getOperation("PatchTestOperation") );
	}
	
	@Test
	public void testNewMinorVersion_noRollup() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2);
		TLLibrary minorVersionLibrary11 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		MinorVersionHelper helper = new MinorVersionHelper();
		
		TLBusinessObject existingMinorVersionBO = minorVersionLibrary11.getBusinessObjectType("MinorVersionTestBO");
		TLCoreObject existingMinorVersionCore = minorVersionLibrary11.getCoreObjectType("MinorVersionTestCore");
		TLValueWithAttributes existingMinorVersionVWA = minorVersionLibrary11.getValueWithAttributesType("MinorVersionTestVWA");
		TLOperation existingMinorVersionOp = minorVersionLibrary11.getService().getOperation("MinorVersionTestOperation");
		
		TLBusinessObject newMinorVersionBO = helper.createNewMinorVersion(existingMinorVersionBO, minorVersionLibrary12);
		TLCoreObject newMinorVersionCore = helper.createNewMinorVersion(existingMinorVersionCore, minorVersionLibrary12);
		TLValueWithAttributes newMinorVersionVWA = helper.createNewMinorVersion(existingMinorVersionVWA, minorVersionLibrary12);
		TLOperation newMinorVersionOp = helper.createNewMinorVersion(existingMinorVersionOp, minorVersionLibrary12);
		
		assertNotNull( newMinorVersionBO );
		assertTrue( newMinorVersionBO.getOwningLibrary() == minorVersionLibrary12 );
		assertTrue( newMinorVersionBO.getExtension().getExtendsEntity() == existingMinorVersionBO );
		assertEquals( 0, newMinorVersionBO.getSummaryFacet().getAttributes().size() );
		assertEquals( 0, newMinorVersionBO.getSummaryFacet().getElements().size() );
		assertEquals( 0, newMinorVersionBO.getSummaryFacet().getIndicators().size() );
		
		assertNotNull( newMinorVersionCore );
		assertTrue( newMinorVersionCore.getOwningLibrary() == minorVersionLibrary12 );
		assertTrue( newMinorVersionCore.getExtension().getExtendsEntity() == existingMinorVersionCore );
		assertEquals( 0, newMinorVersionCore.getSummaryFacet().getAttributes().size() );
		assertEquals( 0, newMinorVersionCore.getSummaryFacet().getElements().size() );
		assertEquals( 0, newMinorVersionCore.getSummaryFacet().getIndicators().size() );
		
		assertNotNull( newMinorVersionVWA );
		assertTrue( newMinorVersionVWA.getOwningLibrary() == minorVersionLibrary12 );
		assertTrue( newMinorVersionVWA.getParentType() == existingMinorVersionVWA );
		assertEquals( 0, newMinorVersionVWA.getAttributes().size() );
		assertEquals( 0, newMinorVersionVWA.getIndicators().size() );
		
		assertNotNull( newMinorVersionOp );
		assertTrue( newMinorVersionOp.getOwningLibrary() == minorVersionLibrary12 );
		assertTrue( newMinorVersionOp.getExtension().getExtendsEntity() == existingMinorVersionOp );
		assertEquals( 0, newMinorVersionOp.getRequest().getAttributes().size() );
		assertEquals( 0, newMinorVersionOp.getRequest().getElements().size() );
		assertEquals( 0, newMinorVersionOp.getRequest().getIndicators().size() );
	}
	
	@Test
	public void testNewMinorVersion_laterMinorVersion_withRollup() throws Exception {
		TLModel model = loadTestModel(FILE_VERSION_1_2_2, FILE_VERSION_1_0_1);
		TLLibrary minorVersionLibrary10 = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary11 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
		TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
		MinorVersionHelper helper = new MinorVersionHelper();
		
		TLBusinessObject firstMinorVersionBO = minorVersionLibrary10.getBusinessObjectType("LaterMinorVersionBO");
		TLCoreObject firstMinorVersionCore = minorVersionLibrary10.getCoreObjectType("LaterMinorVersionCore");
		TLValueWithAttributes firstMinorVersionVWA = minorVersionLibrary10.getValueWithAttributesType("LaterMinorVersionVWA");
		TLOperation firstMinorVersionOp = minorVersionLibrary10.getService().getOperation("LaterMinorVersionOperation");
		
		TLBusinessObject laterMinorVersionBO = minorVersionLibrary12.getBusinessObjectType("LaterMinorVersionBO");
		TLCoreObject laterMinorVersionCore = minorVersionLibrary12.getCoreObjectType("LaterMinorVersionCore");
		TLValueWithAttributes laterMinorVersionVWA = minorVersionLibrary12.getValueWithAttributesType("LaterMinorVersionVWA");
		TLOperation laterMinorVersionOp = minorVersionLibrary12.getService().getOperation("LaterMinorVersionOperation");
		
		assertTrue( laterMinorVersionBO.getExtension().getExtendsEntity() == firstMinorVersionBO );
		assertTrue( laterMinorVersionCore.getExtension().getExtendsEntity() == firstMinorVersionCore );
		assertTrue( laterMinorVersionVWA.getParentType() == firstMinorVersionVWA );
		assertTrue( laterMinorVersionOp.getExtension().getExtendsEntity() == firstMinorVersionOp );
		
		TLBusinessObject newMinorVersionBO = helper.createNewMinorVersion(firstMinorVersionBO, minorVersionLibrary11);
		TLCoreObject newMinorVersionCore = helper.createNewMinorVersion(firstMinorVersionCore, minorVersionLibrary11);
		TLValueWithAttributes newMinorVersionVWA = helper.createNewMinorVersion(firstMinorVersionVWA, minorVersionLibrary11);
		TLOperation newMinorVersionOp = helper.createNewMinorVersion(firstMinorVersionOp, minorVersionLibrary11);
		
		assertNotNull( newMinorVersionBO );
		assertTrue( newMinorVersionBO.getOwningLibrary() == minorVersionLibrary11 );
		assertTrue( newMinorVersionBO.getExtension().getExtendsEntity() == firstMinorVersionBO );
		assertEquals( 1, newMinorVersionBO.getSummaryFacet().getAttributes().size() );
		assertEquals( 0, newMinorVersionBO.getSummaryFacet().getElements().size() );
		assertEquals( 0, newMinorVersionBO.getSummaryFacet().getIndicators().size() );
		assertContainsAttributes( newMinorVersionBO.getSummaryFacet(), "extBOAttribute101" );
		
		assertNotNull( newMinorVersionCore );
		assertTrue( newMinorVersionCore.getOwningLibrary() == minorVersionLibrary11 );
		assertTrue( newMinorVersionCore.getExtension().getExtendsEntity() == firstMinorVersionCore );
		assertEquals( 1, newMinorVersionCore.getSummaryFacet().getAttributes().size() );
		assertEquals( 0, newMinorVersionCore.getSummaryFacet().getElements().size() );
		assertEquals( 0, newMinorVersionCore.getSummaryFacet().getIndicators().size() );
		assertContainsAttributes( newMinorVersionCore.getSummaryFacet(), "extCoreAttribute101" );
		
		assertNotNull( newMinorVersionVWA );
		assertTrue( newMinorVersionVWA.getOwningLibrary() == minorVersionLibrary11 );
		assertTrue( newMinorVersionVWA.getParentType() == firstMinorVersionVWA );
		assertEquals( 0, newMinorVersionVWA.getAttributes().size() );
		assertEquals( 0, newMinorVersionVWA.getIndicators().size() );
		
		assertNotNull( newMinorVersionOp );
		assertTrue( newMinorVersionOp.getOwningLibrary() == minorVersionLibrary11 );
		assertTrue( newMinorVersionOp.getExtension().getExtendsEntity() == firstMinorVersionOp );
		assertEquals( 1, newMinorVersionOp.getRequest().getAttributes().size() );
		assertEquals( 0, newMinorVersionOp.getRequest().getElements().size() );
		assertEquals( 0, newMinorVersionOp.getRequest().getIndicators().size() );
		assertContainsAttributes( newMinorVersionOp.getRequest(), "extOperationAttribute101" );
		
		// Make sure the later minor version was adjusted to extend the new minor version we just created
		assertTrue( laterMinorVersionBO.getExtension().getExtendsEntity() == newMinorVersionBO );
		assertTrue( laterMinorVersionCore.getExtension().getExtendsEntity() == newMinorVersionCore );
		assertTrue( laterMinorVersionVWA.getParentType() == newMinorVersionVWA );
		assertTrue( laterMinorVersionOp.getExtension().getExtendsEntity() == newMinorVersionOp );
	}
	
	@Test
	public void patchRollupWithCOAndEPFShouldCreateNewCoAndSetBaseType()
			throws LibrarySaveException, VersionSchemeException, ValidationException, IOException {
		// given
		ModelBuilder mb= ModelBuilder.create();
		TLModel m = mb.getModel();

		TLLibrary patchV000 = mb.newLibrary("PatchRollup", "http://test.org/rollup/patch").build();
		patchV000.setPrefix("a");
		patchV000.setLibraryUrl(URLUtils.toURL(tmp.newFile(patchV000.getName())));

		TLCoreObject co = new TLCoreObject();
		co.setName("CO");
		co.setNotExtendable(false);
		co.getSimpleFacet().setSimpleType((NamedEntity) resolveEntity(m, "ota2:Empty"));

		TLProperty coElement = new TLProperty();
		coElement.setName("PatchElement");
		coElement.setType((TLPropertyType) resolveEntity(m ,"xsd:string"));
		co.getSummaryFacet().addElement(coElement);
		patchV000.addNamedMember(co);

		PatchVersionHelper helper = new PatchVersionHelper();
		TLLibrary patchV001 = helper.createNewPatchVersion(patchV000);
		TLExtensionPointFacet epf = new TLExtensionPointFacet();
		patchV001.addNamedMember(epf);
		TLExtension tlex = new TLExtension();
		epf.setExtension(tlex);
		tlex.setExtendsEntity(co.getSummaryFacet());
		TLIndicator ind = new TLIndicator();
		ind.setName("ExtendingInd");
		epf.addIndicator(ind);

		// when
		MinorVersionHelper minor = new MinorVersionHelper();
		TLLibrary minorv020 = minor.createNewMinorVersion(patchV000);

		// then
		ValidationFindings findings = TLModelCompileValidator
				.validateModelElement(minorv020, false);
		TLCoreObject minorCO = (TLCoreObject) minorv020.getNamedMember(co.getName());
		assertNotNull(minorCO.getExtension());
		Assert.assertSame(co, minorCO.getExtension().getExtendsEntity());
		assertFalse(findings.hasFinding());
	}

	@Test
	public void patchRollupWithBOAndEPFShouldCreateNewCoAndSetBaseType()
			throws LibrarySaveException, VersionSchemeException, ValidationException, IOException {
		// given
		ModelBuilder mb= ModelBuilder.create();
		TLModel m = mb.getModel();
		
		TLLibrary patchV000 = mb.newLibrary("PatchRollup", "http://test.org/rollup/patch").build();
		patchV000.setPrefix("a");
		patchV000.setLibraryUrl(URLUtils.toURL(tmp.newFile(patchV000.getName())));
		
		TLBusinessObject bo = new TLBusinessObject();
		bo.setName("BO");
		TLProperty coElement = new TLProperty();
		coElement.setName("id");
		coElement.setType((TLPropertyType) resolveEntity(m ,"xsd:long"));
		bo.getIdFacet().addElement(coElement);
		patchV000.addNamedMember(bo);
		
		PatchVersionHelper helper = new PatchVersionHelper();
		TLLibrary patchV001 = helper.createNewPatchVersion(patchV000);
		
		TLExtensionPointFacet epf = new TLExtensionPointFacet();
		patchV001.addNamedMember(epf);
		TLExtension tlex = new TLExtension();
		epf.setExtension(tlex);
		tlex.setExtendsEntity(bo.getSummaryFacet());
		TLIndicator ind = new TLIndicator();
		ind.setName("ExtendingInd");
		epf.addIndicator(ind);
		
		// when
		MinorVersionHelper minor = new MinorVersionHelper();
		TLLibrary minorv020 = minor.createNewMinorVersion(patchV000);
		
		// then
		ValidationFindings findings = TLModelCompileValidator.validateModelElement(minorv020, false);
		assertFalse(findings.hasFinding());
		TLBusinessObject minorCO = (TLBusinessObject) minorv020.getNamedMember(bo.getName());
		assertNotNull(minorCO.getExtension());
		Assert.assertSame(bo, minorCO.getExtension().getExtendsEntity());
	}
	@Test
	public void patchRollupWithBOAndEPFAndSimpleTypeShouldCreateNewCoAndSetBaseType()
	        throws LibrarySaveException, VersionSchemeException, ValidationException, IOException {
	    // given
	    LibraryModelSaver saver = new LibraryModelSaver();
	    ModelBuilder mb= ModelBuilder.create();
	    TLModel m = mb.getModel();
	    
	    TLLibrary majorV000 = mb.newLibrary("PatchRollup", "http://test.org/rollup/patch").build();
	    majorV000.setPrefix("a");
	    majorV000.setLibraryUrl(URLUtils.toURL(tmp.newFile(majorV000.getName())));
	    
	    TLSimple s = new TLSimple();
	    s.setName("simpleString");
	    s.setParentType((TLAttributeType) resolveEntity(m, "xsd:string"));
	    majorV000.addNamedMember(s);
	    TLBusinessObject bo = new TLBusinessObject();
	    bo.setName("BO");
	    TLProperty idElement = new TLProperty();
	    idElement.setName("id");
	    idElement.setType((TLPropertyType) resolveEntity(m ,"xsd:long"));
	    bo.getIdFacet().addElement(idElement);
	    TLProperty boElement = new TLProperty();
	    boElement.setName("simple");
	    boElement.setType(s);
	    bo.getSummaryFacet() .addElement(boElement);
	    majorV000.addNamedMember(bo);
	    
	    saver.saveLibrary(majorV000);
	    
	    PatchVersionHelper helper = new PatchVersionHelper();
	    TLLibrary patchV001 = helper.createNewPatchVersion(majorV000);
	    
	    TLExtensionPointFacet epf = new TLExtensionPointFacet();
	    patchV001.addNamedMember(epf);
	    TLExtension tlex = new TLExtension();
	    epf.setExtension(tlex);
	    tlex.setExtendsEntity(bo.getSummaryFacet());
	    TLIndicator ind = new TLIndicator();
	    ind.setName("ExtendingInd");
	    epf.addIndicator(ind);
	    
	    // when
	    MinorVersionHelper minor = new MinorVersionHelper();
	    TLLibrary minorv020 = minor.createNewMinorVersion(majorV000);
	    
	    // then
	    ValidationFindings findings = TLModelCompileValidator.validateModelElement(minorv020, false);
	    assertFalse(findings.hasFinding());
	    TLBusinessObject minorCO = (TLBusinessObject) minorv020.getNamedMember(bo.getName());
	    assertNotNull(minorCO.getExtension());
	    Assert.assertSame(bo, minorCO.getExtension().getExtendsEntity());
	}
	
	@Test
    public void patchRollupDuplicatedSimpleType()
            throws LibrarySaveException, VersionSchemeException, ValidationException, IOException {
        // given
        ModelBuilder mb= ModelBuilder.create();
        TLModel m = mb.getModel();
        
        TLLibrary majorV000 = mb.newLibrary("PatchRollup", "http://test.org/rollup/patch").build();
        majorV000.setPrefix("a");
        majorV000.setLibraryUrl(URLUtils.toURL(tmp.newFile(majorV000.getName())));
        
        PatchVersionHelper helper = new PatchVersionHelper();
        TLLibrary patchV001 = helper.createNewPatchVersion(majorV000);
        
        TLSimple s = new TLSimple();
        s.setName("simpleInPatch");
        s.setParentType((TLAttributeType) resolveEntity(m, "xsd:string"));
        patchV001.addNamedMember(s);
        
        // when
        MinorVersionHelper minor = new MinorVersionHelper();
        TLLibrary minorv010 = minor.createNewMinorVersion(majorV000);

        // then
        ValidationFindings findings = TLModelCompileValidator
                .validateModelElement(minorv010, false);
        assertFalse(findings.hasFinding());
    }
	@Test
	public void patchRollupDuplicatedSimpleTypeMultipeMinor()
	        throws LibrarySaveException, VersionSchemeException, ValidationException, IOException {
	    // given
	    ModelBuilder mb= ModelBuilder.create();
	    TLModel m = mb.getModel();
	    
	    TLLibrary majorV000 = mb.newLibrary("PatchRollup", "http://test.org/rollup/patch").build();
	    majorV000.setPrefix("a");
	    majorV000.setLibraryUrl(URLUtils.toURL(tmp.newFile(majorV000.getName())));
	    
	    PatchVersionHelper helper = new PatchVersionHelper();
	    TLLibrary patchV001 = helper.createNewPatchVersion(majorV000);
	    
	    TLSimple s = new TLSimple();
	    s.setName("simpleInPatch");
	    s.setParentType((TLAttributeType) resolveEntity(m, "xsd:string"));
	    patchV001.addNamedMember(s);
	    
	    // when
	    MinorVersionHelper minor = new MinorVersionHelper();
	    TLLibrary minorv010 = minor.createNewMinorVersion(majorV000);
	    
	    new LibraryModelSaver().saveAllLibraries(m);
	    // then
	    ValidationFindings findings = TLModelCompileValidator
	            .validateModelElement(minorv010, false);
	    assertFalse(findings.hasFinding());
	}

	@Test
	public void patchRollupDuplicatedCloseEnum()
	        throws LibrarySaveException, VersionSchemeException, ValidationException, IOException {
	    // given
	    ModelBuilder mb= ModelBuilder.create();
	    TLModel m = mb.getModel();
	    
	    TLLibrary majorV000 = mb.newLibrary("PatchRollup", "http://test.org/rollup/patch").build();
	    majorV000.setPrefix("a");
	    majorV000.setLibraryUrl(URLUtils.toURL(tmp.newFile(majorV000.getName())));
	    
	    PatchVersionHelper helper = new PatchVersionHelper();
	    TLLibrary patchV001 = helper.createNewPatchVersion(majorV000);
	    
	    TLClosedEnumeration enumC = new TLClosedEnumeration();
	    enumC.setName("Enum");
	    TLEnumValue value = new TLEnumValue();
	    value.setLiteral("FirstValue");
	    enumC.addValue(value);
	    patchV001.addNamedMember(enumC);
	    
	    // when
	    MinorVersionHelper minor = new MinorVersionHelper();
	    TLLibrary minorv010 = minor.createNewMinorVersion(majorV000);
	    
	    // then
	    ValidationFindings findings = TLModelCompileValidator
	            .validateModelElement(minorv010, false);
	    assertFalse(findings.hasFinding());
	}

	@Test
	public void patchRollupDuplicatedVWAType()
	        throws LibrarySaveException, VersionSchemeException, ValidationException, IOException {
	    // given
	    ModelBuilder mb= ModelBuilder.create();
	    TLModel m = mb.getModel();
	    LibraryModelSaver s = new LibraryModelSaver();
	    
	    TLLibrary majorV000 = mb.newLibrary("PatchRollup", "http://test.org/rollup/patch").build();
	    majorV000.setPrefix("a");
	    majorV000.setLibraryUrl(URLUtils.toURL(tmp.newFile(majorV000.getName())));
	    
	    MinorVersionHelper minor = new MinorVersionHelper();
	    TLLibrary minorV010= minor.createNewMinorVersion(majorV000);
	    
	    TLValueWithAttributes vwa = new TLValueWithAttributes();
	    vwa.setName("VwaInMinor");
	    vwa.setParentType((TLAttributeType) resolveEntity(m, "xsd:string"));
	    TLAttribute attr = new TLAttribute();
	    attr.setName("attr");
	    attr.setType((TLAttributeType) resolveEntity(m, "xsd:string"));
	    vwa.addAttribute(attr);
	    minorV010.addNamedMember(vwa);
	    
	    TLLibrary minorV020 = minor.createNewMinorVersion(minorV010);
	    
	    // when
	    MajorVersionHelper major = new MajorVersionHelper();
	    TLLibrary majorV100= major.createNewMajorVersion(majorV000);
	    
	    s.saveAllLibraries(m);
	    // then
	    ValidationFindings findings = TLModelCompileValidator
	            .validateModelElement(majorV100, false);
	    assertFalse(findings.hasFinding());
	}
	
	private Object resolveEntity(TLModel m, String name) {
		SymbolResolver resolver = new TLModelSymbolResolver(m);
		resolver.setPrefixResolver(new LibraryPrefixResolver(m.getBuiltInLibraries().get(0)));
		return resolver.resolveEntity(name);
	}
	
}
