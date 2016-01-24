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
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.tests.util.ModelBuilder;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;

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

        assertNotNull(majorVersionLibrary);
        assertNotNull(minorVersionLibrary1);
        assertNotNull(minorVersionLibrary2);
        assertNotNull(patchVersionLibrary);

        laterMinorVersions = getLibraryNames(helper.getLaterMinorVersions(majorVersionLibrary));
        assertEquals(2, laterMinorVersions.size());
        assertEquals(LIBNAME_VERSION_1_1, laterMinorVersions.get(0));
        assertEquals(LIBNAME_VERSION_1_2, laterMinorVersions.get(1));

        laterMinorVersions = getLibraryNames(helper.getLaterMinorVersions(minorVersionLibrary1));
        assertEquals(1, laterMinorVersions.size());
        assertEquals(LIBNAME_VERSION_1_2, laterMinorVersions.get(0));

        laterMinorVersions = getLibraryNames(helper.getLaterMinorVersions(minorVersionLibrary2));
        assertEquals(0, laterMinorVersions.size());

        laterMinorVersions = getLibraryNames(helper.getLaterMinorVersions(patchVersionLibrary));
        assertEquals(0, laterMinorVersions.size());
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

        assertNotNull(majorVersionLibrary);
        assertNotNull(minorVersionLibrary1);
        assertNotNull(minorVersionLibrary2);
        assertNotNull(patchVersionLibrary);

        priorMinorVersion = helper.getPriorMinorVersion(majorVersionLibrary);
        assertNull(priorMinorVersion);

        priorMinorVersion = helper.getPriorMinorVersion(minorVersionLibrary1);
        assertEquals(LIBNAME_VERSION_1, getLibraryName(priorMinorVersion));

        priorMinorVersion = helper.getPriorMinorVersion(minorVersionLibrary2);
        assertEquals(LIBNAME_VERSION_1_1, getLibraryName(priorMinorVersion));

        priorMinorVersion = helper.getPriorMinorVersion(patchVersionLibrary);
        assertEquals(LIBNAME_VERSION_1_2, getLibraryName(priorMinorVersion));
    }

    @Test
    public void testGetPriorVersionExtension() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLLibrary priorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
        MinorVersionHelper helper = new MinorVersionHelper();
        TLBusinessObject currentVersionBO = minorVersionLibrary.getBusinessObjectType("LookupBO");
        TLCoreObject currentVersionCore = minorVersionLibrary.getCoreObjectType("LookupCore");
        TLChoiceObject currentVersionChoice = minorVersionLibrary.getChoiceObjectType("LookupChoice");
        TLValueWithAttributes currentVersionVWA = minorVersionLibrary.getValueWithAttributesType("LookupVWA");
        TLOperation currentVersionOp = minorVersionLibrary.getService().getOperation("LookupOperation");
        TLOpenEnumeration currentVersionOpenEnum = minorVersionLibrary.getOpenEnumerationType("LookupOpenEnum");
        TLClosedEnumeration currentVersionClosedEnum = minorVersionLibrary.getClosedEnumerationType("LookupClosedEnum");
        TLSimple currentVersionSimple = minorVersionLibrary.getSimpleType("LookupSimple");
        TLResource currentVersionResource = minorVersionLibrary.getResourceType("LookupResource");

        assertNotNull(currentVersionBO);
        assertNotNull(currentVersionCore);
        assertNotNull(currentVersionChoice);
        assertNotNull(currentVersionVWA);
        assertNotNull(currentVersionOp);
        assertNotNull(currentVersionOpenEnum);
        assertNotNull(currentVersionClosedEnum);
        assertNotNull(currentVersionSimple);
        assertNotNull(currentVersionResource);

        TLBusinessObject priorVersionBO = helper.getVersionExtension(currentVersionBO);
        TLCoreObject priorVersionCore = helper.getVersionExtension(currentVersionCore);
        TLChoiceObject priorVersionChoice = helper.getVersionExtension(currentVersionChoice);
        TLValueWithAttributes priorVersionVWA = helper.getVersionExtension(currentVersionVWA);
        TLOperation priorVersionOp = helper.getVersionExtension(currentVersionOp);
        TLOpenEnumeration priorVersionOpenEnum = helper.getVersionExtension(currentVersionOpenEnum);
        TLClosedEnumeration priorVersionClosedEnum = helper.getVersionExtension(currentVersionClosedEnum);
        TLSimple priorVersionSimple = helper.getVersionExtension(currentVersionSimple);
        TLResource priorVersionResource = helper.getVersionExtension(currentVersionResource);

        assertNotNull(priorVersionBO);
        assertEquals("1.1.0", priorVersionBO.getVersion());
        assertTrue(priorVersionBO.getOwningLibrary() == priorVersionLibrary);

        assertNotNull(priorVersionChoice);
        assertEquals("1.1.0", priorVersionChoice.getVersion());
        assertTrue(priorVersionChoice.getOwningLibrary() == priorVersionLibrary);

        assertNotNull(priorVersionCore);
        assertEquals("1.1.0", priorVersionCore.getVersion());
        assertTrue(priorVersionCore.getOwningLibrary() == priorVersionLibrary);

        assertNotNull(priorVersionVWA);
        assertEquals("1.1.0", priorVersionVWA.getVersion());
        assertTrue(priorVersionVWA.getOwningLibrary() == priorVersionLibrary);

        assertNotNull(priorVersionOp);
        assertEquals("1.1.0", priorVersionOp.getVersion());
        assertTrue(priorVersionOp.getOwningLibrary() == priorVersionLibrary);

        assertNotNull(priorVersionOpenEnum);
        assertEquals("1.1.0", priorVersionOp.getVersion());
        assertTrue(priorVersionOp.getOwningLibrary() == priorVersionLibrary);

        assertNotNull(priorVersionClosedEnum);
        assertEquals("1.1.0", priorVersionOp.getVersion());
        assertTrue(priorVersionOp.getOwningLibrary() == priorVersionLibrary);

        assertNotNull(priorVersionSimple);
        assertEquals("1.1.0", priorVersionOp.getVersion());
        assertTrue(priorVersionOp.getOwningLibrary() == priorVersionLibrary);
        
        assertNotNull(priorVersionResource);
        assertEquals("1.1.0", priorVersionResource.getVersion());
        assertTrue(priorVersionResource.getOwningLibrary() == priorVersionLibrary);
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
        TLChoiceObject currentVersionChoice = minorVersionLibrary.getChoiceObjectType("LookupChoice");
        TLValueWithAttributes currentVersionVWA = minorVersionLibrary.getValueWithAttributesType("LookupVWA");
        TLOperation currentVersionOp = minorVersionLibrary.getService().getOperation("LookupOperation");
        TLOpenEnumeration currentVersionOpenEnum = minorVersionLibrary.getOpenEnumerationType("LookupOpenEnum");
        TLClosedEnumeration currentVersionClosedEnum = minorVersionLibrary.getClosedEnumerationType("LookupClosedEnum");
        TLSimple currentVersionSimple = minorVersionLibrary.getSimpleType("LookupSimple");
        TLResource currentVersionResource = minorVersionLibrary.getResourceType("LookupResource");

        assertNotNull(currentVersionBO);
        assertNotNull(currentVersionCore);
        assertNotNull(currentVersionChoice);
        assertNotNull(currentVersionVWA);
        assertNotNull(currentVersionOp);
        assertNotNull(currentVersionOpenEnum);
        assertNotNull(currentVersionClosedEnum);
        assertNotNull(currentVersionSimple);
        assertNotNull(currentVersionResource);

        List<TLBusinessObject> priorBOVersions = helper.getAllVersionExtensions(currentVersionBO);
        List<TLCoreObject> priorCoreVersions = helper.getAllVersionExtensions(currentVersionCore);
        List<TLChoiceObject> priorChoiceVersions = helper.getAllVersionExtensions(currentVersionChoice);
        List<TLValueWithAttributes> priorVwaVersions = helper.getAllVersionExtensions(currentVersionVWA);
        List<TLOperation> priorOpVersions = helper.getAllVersionExtensions(currentVersionOp);
        List<TLOpenEnumeration> priorOpenEnumVersions = helper.getAllVersionExtensions(currentVersionOpenEnum);
        List<TLClosedEnumeration> priorClosedEnumVersions = helper.getAllVersionExtensions(currentVersionClosedEnum);
        List<TLSimple> priorSimpleVersions = helper.getAllVersionExtensions(currentVersionSimple);
        List<TLResource> priorResourceVersions = helper.getAllVersionExtensions(currentVersionResource);

        assertEquals(2, priorBOVersions.size());
        assertEquals("1.1.0", priorBOVersions.get(0).getVersion());
        assertTrue(priorBOVersions.get(0).getOwningLibrary() == priorVersionLibrary);
        assertEquals("1.0.0", priorBOVersions.get(1).getVersion());
        assertTrue(priorBOVersions.get(1).getOwningLibrary() == majorVersionLibrary);

        assertEquals(2, priorCoreVersions.size());
        assertEquals("1.1.0", priorCoreVersions.get(0).getVersion());
        assertTrue(priorCoreVersions.get(0).getOwningLibrary() == priorVersionLibrary);
        assertEquals("1.0.0", priorCoreVersions.get(1).getVersion());
        assertTrue(priorCoreVersions.get(1).getOwningLibrary() == majorVersionLibrary);

        assertEquals(2, priorChoiceVersions.size());
        assertEquals("1.1.0", priorChoiceVersions.get(0).getVersion());
        assertTrue(priorChoiceVersions.get(0).getOwningLibrary() == priorVersionLibrary);
        assertEquals("1.0.0", priorChoiceVersions.get(1).getVersion());
        assertTrue(priorChoiceVersions.get(1).getOwningLibrary() == majorVersionLibrary);

        assertEquals(2, priorVwaVersions.size());
        assertEquals("1.1.0", priorVwaVersions.get(0).getVersion());
        assertTrue(priorVwaVersions.get(0).getOwningLibrary() == priorVersionLibrary);
        assertEquals("1.0.0", priorVwaVersions.get(1).getVersion());
        assertTrue(priorVwaVersions.get(1).getOwningLibrary() == majorVersionLibrary);

        assertEquals(2, priorOpVersions.size());
        assertEquals("1.1.0", priorOpVersions.get(0).getVersion());
        assertTrue(priorOpVersions.get(0).getOwningLibrary() == priorVersionLibrary);
        assertEquals("1.0.0", priorOpVersions.get(1).getVersion());
        assertTrue(priorOpVersions.get(1).getOwningLibrary() == majorVersionLibrary);

        assertEquals(2, priorOpenEnumVersions.size());
        assertEquals("1.1.0", priorOpenEnumVersions.get(0).getVersion());
        assertTrue(priorOpenEnumVersions.get(0).getOwningLibrary() == priorVersionLibrary);
        assertEquals("1.0.0", priorOpenEnumVersions.get(1).getVersion());
        assertTrue(priorOpenEnumVersions.get(1).getOwningLibrary() == majorVersionLibrary);

        assertEquals(2, priorClosedEnumVersions.size());
        assertEquals("1.1.0", priorClosedEnumVersions.get(0).getVersion());
        assertTrue(priorClosedEnumVersions.get(0).getOwningLibrary() == priorVersionLibrary);
        assertEquals("1.0.0", priorClosedEnumVersions.get(1).getVersion());
        assertTrue(priorClosedEnumVersions.get(1).getOwningLibrary() == majorVersionLibrary);

        assertEquals(2, priorSimpleVersions.size());
        assertEquals("1.1.0", priorSimpleVersions.get(0).getVersion());
        assertTrue(priorSimpleVersions.get(0).getOwningLibrary() == priorVersionLibrary);
        assertEquals("1.0.0", priorSimpleVersions.get(1).getVersion());
        assertTrue(priorSimpleVersions.get(1).getOwningLibrary() == majorVersionLibrary);
        
        assertEquals(2, priorResourceVersions.size());
        assertEquals("1.1.0", priorResourceVersions.get(0).getVersion());
        assertTrue(priorResourceVersions.get(0).getOwningLibrary() == priorVersionLibrary);
        assertEquals("1.0.0", priorResourceVersions.get(1).getVersion());
        assertTrue(priorResourceVersions.get(1).getOwningLibrary() == majorVersionLibrary);
    }

    @Test
    public void testGetLaterMinorVersions() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary1 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary2 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLBusinessObject majorVersionBO = majorVersionLibrary.getBusinessObjectType("LookupBO");
        TLCoreObject majorVersionCore = majorVersionLibrary.getCoreObjectType("LookupCore");
        TLChoiceObject majorVersionChoice = majorVersionLibrary.getChoiceObjectType("LookupChoice");
        TLValueWithAttributes majorVersionVWA = majorVersionLibrary .getValueWithAttributesType("LookupVWA");
        TLOperation majorVersionOp = majorVersionLibrary.getService().getOperation( "LookupOperation");
        TLOpenEnumeration majorVersionOpenEnum = majorVersionLibrary.getOpenEnumerationType( "LookupOpenEnum");
        TLClosedEnumeration majorVersionClosedEnum = majorVersionLibrary.getClosedEnumerationType( "LookupClosedEnum");
        TLSimple majorVersionSimple = majorVersionLibrary.getSimpleType( "LookupSimple");
        TLResource majorVersionResource = majorVersionLibrary.getResourceType( "LookupResource");
        MinorVersionHelper helper = new MinorVersionHelper();

        assertNotNull(majorVersionBO);
        assertNotNull(majorVersionCore);
        assertNotNull(majorVersionChoice);
        assertNotNull(majorVersionVWA);
        assertNotNull(majorVersionOp);
        assertNotNull(majorVersionOpenEnum);
        assertNotNull(majorVersionClosedEnum);
        assertNotNull(majorVersionSimple);
        assertNotNull(majorVersionResource);

        List<TLBusinessObject> laterBOVersions = helper.getLaterMinorVersions(majorVersionBO);
        List<TLCoreObject> laterCoreVersions = helper.getLaterMinorVersions(majorVersionCore);
        List<TLChoiceObject> laterChoiceVersions = helper.getLaterMinorVersions(majorVersionChoice);
        List<TLValueWithAttributes> laterVwaVersions = helper.getLaterMinorVersions(majorVersionVWA);
        List<TLOperation> laterOpVersions = helper.getLaterMinorVersions(majorVersionOp);
        List<TLOpenEnumeration> laterOpenEnumVersions = helper.getLaterMinorVersions(majorVersionOpenEnum);
        List<TLClosedEnumeration> laterClosedVersions = helper.getLaterMinorVersions(majorVersionClosedEnum);
        List<TLSimple> laterSimpleVersions = helper.getLaterMinorVersions(majorVersionSimple);
        List<TLResource> laterResourceVersions = helper.getLaterMinorVersions(majorVersionResource);

        assertEquals(2, laterBOVersions.size());
        assertEquals("1.1.0", laterBOVersions.get(0).getVersion());
        assertTrue(laterBOVersions.get(0).getOwningLibrary() == minorVersionLibrary1);
        assertEquals("1.2.0", laterBOVersions.get(1).getVersion());
        assertTrue(laterBOVersions.get(1).getOwningLibrary() == minorVersionLibrary2);

        assertEquals(2, laterCoreVersions.size());
        assertEquals("1.1.0", laterCoreVersions.get(0).getVersion());
        assertTrue(laterCoreVersions.get(0).getOwningLibrary() == minorVersionLibrary1);
        assertEquals("1.2.0", laterCoreVersions.get(1).getVersion());
        assertTrue(laterCoreVersions.get(1).getOwningLibrary() == minorVersionLibrary2);

        assertEquals(2, laterChoiceVersions.size());
        assertEquals("1.1.0", laterChoiceVersions.get(0).getVersion());
        assertTrue(laterChoiceVersions.get(0).getOwningLibrary() == minorVersionLibrary1);
        assertEquals("1.2.0", laterChoiceVersions.get(1).getVersion());
        assertTrue(laterChoiceVersions.get(1).getOwningLibrary() == minorVersionLibrary2);

        assertEquals(2, laterVwaVersions.size());
        assertEquals("1.1.0", laterVwaVersions.get(0).getVersion());
        assertTrue(laterVwaVersions.get(0).getOwningLibrary() == minorVersionLibrary1);
        assertEquals("1.2.0", laterVwaVersions.get(1).getVersion());
        assertTrue(laterVwaVersions.get(1).getOwningLibrary() == minorVersionLibrary2);

        assertEquals(2, laterOpVersions.size());
        assertEquals("1.1.0", laterOpVersions.get(0).getVersion());
        assertTrue(laterOpVersions.get(0).getOwningLibrary() == minorVersionLibrary1);
        assertEquals("1.2.0", laterOpVersions.get(1).getVersion());
        assertTrue(laterOpVersions.get(1).getOwningLibrary() == minorVersionLibrary2);

        assertEquals(2, laterOpenEnumVersions.size());
        assertEquals("1.1.0", laterOpenEnumVersions.get(0).getVersion());
        assertTrue(laterOpenEnumVersions.get(0).getOwningLibrary() == minorVersionLibrary1);
        assertEquals("1.2.0", laterOpenEnumVersions.get(1).getVersion());
        assertTrue(laterOpenEnumVersions.get(1).getOwningLibrary() == minorVersionLibrary2);

        assertEquals(2, laterClosedVersions.size());
        assertEquals("1.1.0", laterClosedVersions.get(0).getVersion());
        assertTrue(laterClosedVersions.get(0).getOwningLibrary() == minorVersionLibrary1);
        assertEquals("1.2.0", laterClosedVersions.get(1).getVersion());
        assertTrue(laterClosedVersions.get(1).getOwningLibrary() == minorVersionLibrary2);

        assertEquals(2, laterSimpleVersions.size());
        assertEquals("1.1.0", laterSimpleVersions.get(0).getVersion());
        assertTrue(laterSimpleVersions.get(0).getOwningLibrary() == minorVersionLibrary1);
        assertEquals("1.2.0", laterSimpleVersions.get(1).getVersion());
        assertTrue(laterSimpleVersions.get(1).getOwningLibrary() == minorVersionLibrary2);

        assertEquals(2, laterResourceVersions.size());
        assertEquals("1.1.0", laterResourceVersions.get(0).getVersion());
        assertTrue(laterResourceVersions.get(0).getOwningLibrary() == minorVersionLibrary1);
        assertEquals("1.2.0", laterResourceVersions.get(1).getVersion());
        assertTrue(laterResourceVersions.get(1).getOwningLibrary() == minorVersionLibrary2);
    }

    @Test
    public void testGetEligibleMinorVersionTargets() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        File libraryFile13 = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/library_v01_03.otm"));
        MinorVersionHelper helper = new MinorVersionHelper();
        List<TLLibrary> eligibleLibraries;

        TLLibrary minorVersionLibrary10 = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary11 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary13 = helper.createNewMinorVersion(minorVersionLibrary12, libraryFile13);

        TLBusinessObject minorVersionBO = minorVersionLibrary10.getBusinessObjectType("LaterMinorVersionBO");
        TLCoreObject minorVersionCore = minorVersionLibrary10.getCoreObjectType("LaterMinorVersionCore");
        TLChoiceObject minorVersionChoice = minorVersionLibrary10.getChoiceObjectType("LaterMinorVersionChoice");
        TLValueWithAttributes minorVersionVWA = minorVersionLibrary10.getValueWithAttributesType("LaterMinorVersionVWA");
        TLOperation minorVersionOp = minorVersionLibrary10.getService().getOperation("LaterMinorVersionOperation");
        TLOpenEnumeration minorVersionOpenEnum = minorVersionLibrary10.getOpenEnumerationType("LaterMinorVersionOpenEnum");
        TLClosedEnumeration minorVersionClosedEnum = minorVersionLibrary10.getClosedEnumerationType("LaterMinorVersionClosedEnum");
        TLSimple minorVersionSimple = minorVersionLibrary10.getSimpleType("LaterMinorVersionSimple");
        TLResource minorVersionResource = minorVersionLibrary10.getResourceType("LaterMinorVersionResource");

        eligibleLibraries = helper.getEligibleMinorVersionTargets(minorVersionBO);
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(minorVersionLibrary11));
        assertTrue(eligibleLibraries.contains(minorVersionLibrary13));

        eligibleLibraries = helper.getEligibleMinorVersionTargets(minorVersionCore);
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(minorVersionLibrary11));
        assertTrue(eligibleLibraries.contains(minorVersionLibrary13));

        eligibleLibraries = helper.getEligibleMinorVersionTargets(minorVersionChoice);
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(minorVersionLibrary11));
        assertTrue(eligibleLibraries.contains(minorVersionLibrary13));

        eligibleLibraries = helper.getEligibleMinorVersionTargets(minorVersionVWA);
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(minorVersionLibrary11));
        assertTrue(eligibleLibraries.contains(minorVersionLibrary13));

        eligibleLibraries = helper.getEligibleMinorVersionTargets(minorVersionOp);
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(minorVersionLibrary11));
        assertTrue(eligibleLibraries.contains(minorVersionLibrary13));

        eligibleLibraries = helper.getEligibleMinorVersionTargets(minorVersionOpenEnum);
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(minorVersionLibrary11));
        assertTrue(eligibleLibraries.contains(minorVersionLibrary13));

        eligibleLibraries = helper.getEligibleMinorVersionTargets(minorVersionClosedEnum);
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(minorVersionLibrary11));
        assertTrue(eligibleLibraries.contains(minorVersionLibrary13));

        eligibleLibraries = helper.getEligibleMinorVersionTargets(minorVersionSimple);
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(minorVersionLibrary11));
        assertTrue(eligibleLibraries.contains(minorVersionLibrary13));

        eligibleLibraries = helper.getEligibleMinorVersionTargets(minorVersionResource);
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(minorVersionLibrary11));
        assertTrue(eligibleLibraries.contains(minorVersionLibrary13));
    }

    @Test
    public void testGetPreferredMinorVersionTarget() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        File patchLibraryFile = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/library_v01_03.otm"));
        MinorVersionHelper helper = new MinorVersionHelper();

        TLLibrary minorVersionLibrary10 = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary13 = helper.createNewMinorVersion(minorVersionLibrary12, patchLibraryFile);

        TLBusinessObject minorVersionBO = minorVersionLibrary10.getBusinessObjectType("LaterMinorVersionBO");
        TLCoreObject minorVersionCore = minorVersionLibrary10.getCoreObjectType("LaterMinorVersionCore");
        TLChoiceObject minorVersionChoice = minorVersionLibrary10.getChoiceObjectType("LaterMinorVersionChoice");
        TLValueWithAttributes minorVersionVWA = minorVersionLibrary10.getValueWithAttributesType("LaterMinorVersionVWA");
        TLOperation minorVersionOp = minorVersionLibrary10.getService().getOperation("LaterMinorVersionOperation");
        TLOpenEnumeration minorVersionOpenEnum = minorVersionLibrary10.getOpenEnumerationType( "LaterMinorVersionOpenEnum");
        TLClosedEnumeration minorVersionClosedEnum = minorVersionLibrary10.getClosedEnumerationType( "LaterMinorVersionClosedEnum");
        TLSimple minorVersionSimple = minorVersionLibrary10.getSimpleType( "LaterMinorVersionSimple");
        TLResource minorVersionResource = minorVersionLibrary10.getResourceType( "LaterMinorVersionResource");

        assertTrue(helper.getPreferredMinorVersionTarget(minorVersionBO) == minorVersionLibrary13);
        assertTrue(helper.getPreferredMinorVersionTarget(minorVersionCore) == minorVersionLibrary13);
        assertTrue(helper.getPreferredMinorVersionTarget(minorVersionChoice) == minorVersionLibrary13);
        assertTrue(helper.getPreferredMinorVersionTarget(minorVersionVWA) == minorVersionLibrary13);
        assertTrue(helper.getPreferredMinorVersionTarget(minorVersionOp) == minorVersionLibrary13);
        assertTrue(helper.getPreferredMinorVersionTarget(minorVersionOpenEnum) == minorVersionLibrary13);
        assertTrue(helper.getPreferredMinorVersionTarget(minorVersionClosedEnum) == minorVersionLibrary13);
        assertTrue(helper.getPreferredMinorVersionTarget(minorVersionSimple) == minorVersionLibrary13);
        assertTrue(helper.getPreferredMinorVersionTarget(minorVersionResource) == minorVersionLibrary13);
    }

    @Test
    public void testNewMinorLibraryVersion() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        File newVersionLibraryFile = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/library_v01_03.otm"));
        MinorVersionHelper helper = new MinorVersionHelper();

        TLLibrary newMinorVersionLibrary = helper.createNewMinorVersion(minorVersionLibrary, newVersionLibraryFile);
        TLBusinessObject newMinorVersionBO = newMinorVersionLibrary.getBusinessObjectType("LookupBO");
        TLCoreObject newMinorVersionCore = newMinorVersionLibrary.getCoreObjectType("LookupCore");
        TLChoiceObject newMinorVersionChoice = newMinorVersionLibrary.getChoiceObjectType("LookupChoice");
        TLOperation newMinorVersionOp = newMinorVersionLibrary.getService().getOperation("LookupOperation");

        assertNotNull(newMinorVersionLibrary);
        assertEquals("1.3.0", newMinorVersionLibrary.getVersion());
        assertEquals(minorVersionLibrary.getName(), newMinorVersionLibrary.getName());
        assertEquals(minorVersionLibrary.getBaseNamespace(), newMinorVersionLibrary.getBaseNamespace());
        assertEquals(minorVersionLibrary.getComments(), newMinorVersionLibrary.getComments());

        assertNotNull(newMinorVersionBO);
        assertContainsAttributes(newMinorVersionBO.getSummaryFacet(), "extBOAttribute121", "extBOAttribute122");

        assertNotNull(newMinorVersionCore);
        assertContainsAttributes(newMinorVersionCore.getSummaryFacet(), "extCoreAttribute121", "extCoreAttribute122");

        assertNotNull(newMinorVersionChoice);
        assertContainsAttributes(newMinorVersionChoice.getSharedFacet(), "extChoiceAttribute121", "extChoiceAttribute122");

        assertNotNull(newMinorVersionOp);
        assertContainsAttributes(newMinorVersionOp.getRequest(), "extOperationAttribute121", "extOperationAttribute122");

        // Types defined in the previous minor version are not copied forward to
        // the new minor version unless patches are defined for them
        assertNull(newMinorVersionLibrary.getNamedMember("LookupVWA"));
        assertNull(newMinorVersionLibrary.getNamedMember("LookupOpenEnum"));
        assertNull(newMinorVersionLibrary.getNamedMember("LookupClosedEnum"));
        assertNull(newMinorVersionLibrary.getNamedMember("LookupSimple"));
        assertNull(newMinorVersionLibrary.getNamedMember("LookupResource"));
    }

    @Test
    public void testNewMinorVersion_noRollup() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary minorVersionLibrary11 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        MinorVersionHelper helper = new MinorVersionHelper();

        TLBusinessObject existingMinorVersionBO = minorVersionLibrary11.getBusinessObjectType("MinorVersionTestBO");
        TLCoreObject existingMinorVersionCore = minorVersionLibrary11.getCoreObjectType("MinorVersionTestCore");
        TLChoiceObject existingMinorVersionChoice = minorVersionLibrary11.getChoiceObjectType("MinorVersionTestChoice");
        TLValueWithAttributes existingMinorVersionVWA = minorVersionLibrary11.getValueWithAttributesType("MinorVersionTestVWA");
        TLOperation existingMinorVersionOp = minorVersionLibrary11.getService().getOperation("MinorVersionTestOperation");
        TLOpenEnumeration existingMinorVersionOpenEnum = minorVersionLibrary11.getOpenEnumerationType("MinorVersionTestOpenEnum");
        TLClosedEnumeration existingMinorVersionClosedEnum = minorVersionLibrary11.getClosedEnumerationType("MinorVersionTestClosedEnum");
        TLSimple existingMinorVersionSimple = minorVersionLibrary11.getSimpleType("MinorVersionTestSimple");
        TLResource existingMinorVersionResource = minorVersionLibrary11.getResourceType("MinorVersionTestResource");
        
        TLBusinessObject newMinorVersionBO = helper.createNewMinorVersion(existingMinorVersionBO, minorVersionLibrary12);
        TLCoreObject newMinorVersionCore = helper.createNewMinorVersion(existingMinorVersionCore, minorVersionLibrary12);
        TLChoiceObject newMinorVersionChoice = helper.createNewMinorVersion(existingMinorVersionChoice, minorVersionLibrary12);
        TLValueWithAttributes newMinorVersionVWA = helper.createNewMinorVersion(existingMinorVersionVWA, minorVersionLibrary12);
        TLOperation newMinorVersionOp = helper.createNewMinorVersion(existingMinorVersionOp, minorVersionLibrary12);
        TLOpenEnumeration newMinorVersionOpenEnum = helper.createNewMinorVersion(existingMinorVersionOpenEnum, minorVersionLibrary12);
        TLClosedEnumeration newMinorVersionClosedEnum = helper.createNewMinorVersion(existingMinorVersionClosedEnum, minorVersionLibrary12);
        TLSimple newMinorVersionSimple = helper.createNewMinorVersion(existingMinorVersionSimple, minorVersionLibrary12);
        TLResource newMinorVersionResource = helper.createNewMinorVersion(existingMinorVersionResource, minorVersionLibrary12);

        assertNotNull(newMinorVersionBO);
        assertTrue(newMinorVersionBO.getOwningLibrary() == minorVersionLibrary12);
        assertTrue(newMinorVersionBO.getExtension().getExtendsEntity() == existingMinorVersionBO);
        assertEquals(0, newMinorVersionBO.getSummaryFacet().getAttributes().size());
        assertEquals(0, newMinorVersionBO.getSummaryFacet().getElements().size());
        assertEquals(0, newMinorVersionBO.getSummaryFacet().getIndicators().size());

        assertNotNull(newMinorVersionCore);
        assertTrue(newMinorVersionCore.getOwningLibrary() == minorVersionLibrary12);
        assertTrue(newMinorVersionCore.getExtension().getExtendsEntity() == existingMinorVersionCore);
        assertEquals(0, newMinorVersionCore.getSummaryFacet().getAttributes().size());
        assertEquals(0, newMinorVersionCore.getSummaryFacet().getElements().size());
        assertEquals(0, newMinorVersionCore.getSummaryFacet().getIndicators().size());

        assertNotNull(newMinorVersionChoice);
        assertTrue(newMinorVersionChoice.getOwningLibrary() == minorVersionLibrary12);
        assertTrue(newMinorVersionChoice.getExtension().getExtendsEntity() == existingMinorVersionChoice);
        assertEquals(0, newMinorVersionChoice.getSharedFacet().getAttributes().size());
        assertEquals(0, newMinorVersionChoice.getSharedFacet().getElements().size());
        assertEquals(0, newMinorVersionChoice.getSharedFacet().getIndicators().size());

        assertNotNull(newMinorVersionVWA);
        assertTrue(newMinorVersionVWA.getOwningLibrary() == minorVersionLibrary12);
        assertTrue(newMinorVersionVWA.getParentType() == existingMinorVersionVWA);
        assertEquals(0, newMinorVersionVWA.getAttributes().size());
        assertEquals(0, newMinorVersionVWA.getIndicators().size());
        
        assertNotNull(newMinorVersionOp);
        assertTrue(newMinorVersionOp.getOwningLibrary() == minorVersionLibrary12);
        assertTrue(newMinorVersionOp.getExtension().getExtendsEntity() == existingMinorVersionOp);
        assertEquals(0, newMinorVersionOp.getRequest().getAttributes().size());
        assertEquals(0, newMinorVersionOp.getRequest().getElements().size());
        assertEquals(0, newMinorVersionOp.getRequest().getIndicators().size());

        assertNotNull(newMinorVersionOpenEnum);
        assertTrue(newMinorVersionOpenEnum.getOwningLibrary() == minorVersionLibrary12);
        assertTrue(newMinorVersionOpenEnum.getExtension().getExtendsEntity() == existingMinorVersionOpenEnum);
        assertEquals(0, newMinorVersionOpenEnum.getValues().size());

        assertNotNull(newMinorVersionClosedEnum);
        assertTrue(newMinorVersionClosedEnum.getOwningLibrary() == minorVersionLibrary12);
        assertTrue(newMinorVersionClosedEnum.getExtension().getExtendsEntity() == existingMinorVersionClosedEnum);
        assertEquals(0, newMinorVersionClosedEnum.getValues().size());

        assertNotNull(newMinorVersionSimple);
        assertTrue(newMinorVersionSimple.getOwningLibrary() == minorVersionLibrary12);
        assertTrue(newMinorVersionSimple.getParentType() == existingMinorVersionSimple);
        assertNull(newMinorVersionSimple.getPattern());
        assertEquals(-1, newMinorVersionSimple.getMinLength());
        assertEquals(-1, newMinorVersionSimple.getMaxLength());
        assertEquals(-1, newMinorVersionSimple.getFractionDigits());
        assertEquals(-1, newMinorVersionSimple.getTotalDigits());
        assertNull(newMinorVersionSimple.getMinInclusive());
        assertNull(newMinorVersionSimple.getMaxInclusive());
        assertNull(newMinorVersionSimple.getMinExclusive());
        assertNull(newMinorVersionSimple.getMaxExclusive());

        assertNotNull(newMinorVersionResource);
        assertTrue(newMinorVersionResource.getOwningLibrary() == minorVersionLibrary12);
        assertTrue(newMinorVersionResource.getExtension().getExtendsEntity() == existingMinorVersionResource);
        assertEquals(0, newMinorVersionResource.getParentRefs().size());
        assertEquals(0, newMinorVersionResource.getParamGroups().size());
        assertEquals(0, newMinorVersionResource.getActionFacets().size());
        assertEquals(0, newMinorVersionResource.getActions().size());
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
        TLChoiceObject firstMinorVersionChoice = minorVersionLibrary10.getChoiceObjectType("LaterMinorVersionChoice");
        TLValueWithAttributes firstMinorVersionVWA = minorVersionLibrary10.getValueWithAttributesType("LaterMinorVersionVWA");
        TLOperation firstMinorVersionOp = minorVersionLibrary10.getService().getOperation("LaterMinorVersionOperation");
        TLOpenEnumeration firstMinorVersionOpenEnum = minorVersionLibrary10.getOpenEnumerationType("LaterMinorVersionOpenEnum");
        TLClosedEnumeration firstMinorVersionClosedEnum = minorVersionLibrary10.getClosedEnumerationType("LaterMinorVersionClosedEnum");
        TLSimple firstMinorVersionSimple = minorVersionLibrary10.getSimpleType("LaterMinorVersionSimple");
        TLResource firstMinorVersionResource = minorVersionLibrary10.getResourceType("LaterMinorVersionResource");

        TLBusinessObject laterMinorVersionBO = minorVersionLibrary12.getBusinessObjectType("LaterMinorVersionBO");
        TLCoreObject laterMinorVersionCore = minorVersionLibrary12.getCoreObjectType("LaterMinorVersionCore");
        TLChoiceObject laterMinorVersionChoice = minorVersionLibrary12.getChoiceObjectType("LaterMinorVersionChoice");
        TLValueWithAttributes laterMinorVersionVWA = minorVersionLibrary12.getValueWithAttributesType("LaterMinorVersionVWA");
        TLOperation laterMinorVersionOp = minorVersionLibrary12.getService().getOperation("LaterMinorVersionOperation");
        TLOpenEnumeration laterMinorVersionOpenEnum = minorVersionLibrary12.getOpenEnumerationType("LaterMinorVersionOpenEnum");
        TLClosedEnumeration laterMinorVersionClosedEnum = minorVersionLibrary12.getClosedEnumerationType("LaterMinorVersionClosedEnum");
        TLSimple laterMinorVersionSimple = minorVersionLibrary12.getSimpleType("LaterMinorVersionSimple");
        TLResource laterMinorVersionResource = minorVersionLibrary12.getResourceType("LaterMinorVersionResource");

        assertTrue(laterMinorVersionBO.getExtension().getExtendsEntity() == firstMinorVersionBO);
        assertTrue(laterMinorVersionCore.getExtension().getExtendsEntity() == firstMinorVersionCore);
        assertTrue(laterMinorVersionChoice.getExtension().getExtendsEntity() == firstMinorVersionChoice);
        assertTrue(laterMinorVersionVWA.getParentType() == firstMinorVersionVWA);
        assertTrue(laterMinorVersionOp.getExtension().getExtendsEntity() == firstMinorVersionOp);
        assertTrue(laterMinorVersionOpenEnum.getExtension().getExtendsEntity() == firstMinorVersionOpenEnum);
        assertTrue(laterMinorVersionClosedEnum.getExtension().getExtendsEntity() == firstMinorVersionClosedEnum);
        assertTrue(laterMinorVersionSimple.getParentType() == firstMinorVersionSimple);
        assertTrue(laterMinorVersionResource.getExtension().getExtendsEntity() == firstMinorVersionResource);

        TLBusinessObject newMinorVersionBO = helper.createNewMinorVersion(firstMinorVersionBO, minorVersionLibrary11);
        TLCoreObject newMinorVersionCore = helper.createNewMinorVersion(firstMinorVersionCore, minorVersionLibrary11);
        TLChoiceObject newMinorVersionChoice = helper.createNewMinorVersion(firstMinorVersionChoice, minorVersionLibrary11);
        TLValueWithAttributes newMinorVersionVWA = helper.createNewMinorVersion(firstMinorVersionVWA, minorVersionLibrary11);
        TLOperation newMinorVersionOp = helper.createNewMinorVersion(firstMinorVersionOp, minorVersionLibrary11);
        TLOpenEnumeration newMinorVersionOpenEnum = helper.createNewMinorVersion(firstMinorVersionOpenEnum, minorVersionLibrary11);
        TLClosedEnumeration newMinorVersionClosedEnum = helper.createNewMinorVersion(firstMinorVersionClosedEnum, minorVersionLibrary11);
        TLSimple newMinorVersionSimple = helper.createNewMinorVersion(firstMinorVersionSimple, minorVersionLibrary11);
        TLResource newMinorVersionResource = helper.createNewMinorVersion(firstMinorVersionResource, minorVersionLibrary11);

        assertNotNull(newMinorVersionBO);
        assertTrue(newMinorVersionBO.getOwningLibrary() == minorVersionLibrary11);
        assertTrue(newMinorVersionBO.getExtension().getExtendsEntity() == firstMinorVersionBO);
        assertEquals(1, newMinorVersionBO.getSummaryFacet().getAttributes().size());
        assertEquals(0, newMinorVersionBO.getSummaryFacet().getElements().size());
        assertEquals(0, newMinorVersionBO.getSummaryFacet().getIndicators().size());
        assertContainsAttributes(newMinorVersionBO.getSummaryFacet(), "extBOAttribute101");

        assertNotNull(newMinorVersionCore);
        assertTrue(newMinorVersionCore.getOwningLibrary() == minorVersionLibrary11);
        assertTrue(newMinorVersionCore.getExtension().getExtendsEntity() == firstMinorVersionCore);
        assertEquals(1, newMinorVersionCore.getSummaryFacet().getAttributes().size());
        assertEquals(0, newMinorVersionCore.getSummaryFacet().getElements().size());
        assertEquals(0, newMinorVersionCore.getSummaryFacet().getIndicators().size());
        assertContainsAttributes(newMinorVersionCore.getSummaryFacet(), "extCoreAttribute101");

        assertNotNull(newMinorVersionChoice);
        assertTrue(newMinorVersionChoice.getOwningLibrary() == minorVersionLibrary11);
        assertTrue(newMinorVersionChoice.getExtension().getExtendsEntity() == firstMinorVersionChoice);
        assertEquals(1, newMinorVersionChoice.getSharedFacet().getAttributes().size());
        assertEquals(0, newMinorVersionChoice.getSharedFacet().getElements().size());
        assertEquals(0, newMinorVersionChoice.getSharedFacet().getIndicators().size());
        assertContainsAttributes(newMinorVersionChoice.getSharedFacet(), "extChoiceAttribute101");

        assertNotNull(newMinorVersionVWA);
        assertTrue(newMinorVersionVWA.getOwningLibrary() == minorVersionLibrary11);
        assertTrue(newMinorVersionVWA.getParentType() == firstMinorVersionVWA);
        assertEquals(0, newMinorVersionVWA.getAttributes().size());
        assertEquals(0, newMinorVersionVWA.getIndicators().size());

        assertNotNull(newMinorVersionOp);
        assertTrue(newMinorVersionOp.getOwningLibrary() == minorVersionLibrary11);
        assertTrue(newMinorVersionOp.getExtension().getExtendsEntity() == firstMinorVersionOp);
        assertEquals(1, newMinorVersionOp.getRequest().getAttributes().size());
        assertEquals(0, newMinorVersionOp.getRequest().getElements().size());
        assertEquals(0, newMinorVersionOp.getRequest().getIndicators().size());
        assertContainsAttributes(newMinorVersionOp.getRequest(), "extOperationAttribute101");

        assertNotNull(newMinorVersionOpenEnum);
        assertTrue(newMinorVersionOpenEnum.getOwningLibrary() == minorVersionLibrary11);
        assertTrue(newMinorVersionOpenEnum.getExtension().getExtendsEntity() == firstMinorVersionOpenEnum);
        
        assertNotNull(newMinorVersionClosedEnum);
        assertTrue(newMinorVersionClosedEnum.getOwningLibrary() == minorVersionLibrary11);
        assertTrue(newMinorVersionClosedEnum.getExtension().getExtendsEntity() == firstMinorVersionClosedEnum);
        
        assertNotNull(newMinorVersionSimple);
        assertTrue(newMinorVersionSimple.getOwningLibrary() == minorVersionLibrary11);
        assertTrue(newMinorVersionSimple.getParentType() == firstMinorVersionSimple);
        
        assertNotNull(newMinorVersionResource);
        assertTrue(newMinorVersionResource.getOwningLibrary() == minorVersionLibrary11);
        assertTrue(newMinorVersionResource.getExtension().getExtendsEntity() == firstMinorVersionResource);
        
        // Make sure the later minor version was adjusted to extend the new minor version we just
        // created
        assertTrue(laterMinorVersionBO.getExtension().getExtendsEntity() == newMinorVersionBO);
        assertTrue(laterMinorVersionCore.getExtension().getExtendsEntity() == newMinorVersionCore);
        assertTrue(laterMinorVersionChoice.getExtension().getExtendsEntity() == newMinorVersionChoice);
        assertTrue(laterMinorVersionVWA.getParentType() == newMinorVersionVWA);
        assertTrue(laterMinorVersionOp.getExtension().getExtendsEntity() == newMinorVersionOp);
        assertTrue(laterMinorVersionOpenEnum.getExtension().getExtendsEntity() == newMinorVersionOpenEnum);
        assertTrue(laterMinorVersionClosedEnum.getExtension().getExtendsEntity() == newMinorVersionClosedEnum);
        assertTrue(laterMinorVersionSimple.getParentType() == newMinorVersionSimple);
        assertTrue(laterMinorVersionResource.getExtension().getExtendsEntity() == newMinorVersionResource);
    }

    @Test
    public void patchRollupWithCOAndEPFShouldCreateNewCoAndSetBaseType()
            throws LibrarySaveException, VersionSchemeException, ValidationException, IOException {
        // given
        ModelBuilder mb = ModelBuilder.create();
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
        coElement.setType((TLPropertyType) resolveEntity(m, "xsd:string"));
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
        SchemaCompilerTestUtils.printFindings(findings);
        assertFalse(findings.hasFinding());
    }

    @Test
    public void patchRollupWithBOAndEPFShouldCreateNewCoAndSetBaseType()
            throws LibrarySaveException, VersionSchemeException, ValidationException, IOException {
        // given
        ModelBuilder mb = ModelBuilder.create();
        TLModel m = mb.getModel();

        TLLibrary patchV000 = mb.newLibrary("PatchRollup", "http://test.org/rollup/patch").build();
        patchV000.setPrefix("a");
        patchV000.setLibraryUrl(URLUtils.toURL(tmp.newFile(patchV000.getName())));

        TLBusinessObject bo = new TLBusinessObject();
        bo.setName("BO");
        TLProperty coElement = new TLProperty();
        coElement.setName("id");
        coElement.setType((TLPropertyType) resolveEntity(m, "xsd:long"));
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
        ValidationFindings findings = TLModelCompileValidator
                .validateModelElement(minorv020, false);
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
        ModelBuilder mb = ModelBuilder.create();
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
        idElement.setType((TLPropertyType) resolveEntity(m, "xsd:long"));
        bo.getIdFacet().addElement(idElement);
        TLProperty boElement = new TLProperty();
        boElement.setName("simple");
        boElement.setType(s);
        bo.getSummaryFacet().addElement(boElement);
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
        ValidationFindings findings = TLModelCompileValidator
                .validateModelElement(minorv020, false);
        assertFalse(findings.hasFinding());
        TLBusinessObject minorCO = (TLBusinessObject) minorv020.getNamedMember(bo.getName());
        assertNotNull(minorCO.getExtension());
        Assert.assertSame(bo, minorCO.getExtension().getExtendsEntity());
    }

    @Test
    public void patchRollupDuplicatedVWAType() throws LibrarySaveException, VersionSchemeException,
            ValidationException, IOException {
        // given
        ModelBuilder mb = ModelBuilder.create();
        TLModel m = mb.getModel();
        LibraryModelSaver s = new LibraryModelSaver();

        TLLibrary majorV000 = mb.newLibrary("PatchRollup", "http://test.org/rollup/patch").build();
        majorV000.setPrefix("a");
        majorV000.setLibraryUrl(URLUtils.toURL(tmp.newFile(majorV000.getName())));

        MinorVersionHelper minor = new MinorVersionHelper();
        TLLibrary minorV010 = minor.createNewMinorVersion(majorV000);

        TLValueWithAttributes vwa = new TLValueWithAttributes();
        vwa.setName("VwaInMinor");
        vwa.setParentType((TLAttributeType) resolveEntity(m, "xsd:string"));
        TLAttribute attr = new TLAttribute();
        attr.setName("attr");
        attr.setType((TLAttributeType) resolveEntity(m, "xsd:string"));
        vwa.addAttribute(attr);
        minorV010.addNamedMember(vwa);

        minor.createNewMinorVersion(minorV010);

        // when
        MajorVersionHelper major = new MajorVersionHelper();
        TLLibrary majorV100 = major.createNewMajorVersion(majorV000);

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
