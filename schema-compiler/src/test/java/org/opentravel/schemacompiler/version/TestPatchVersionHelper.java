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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Verifies the operation of the <code>PatchVersionHelper</code> class.
 * 
 * @author S. Livezey
 */
public class TestPatchVersionHelper extends AbstractVersionHelperTests {

    @Test
    public void testGetLaterPatchVersionLibraries() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2, FILE_VERSION_1_0_1);
        TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary1 = (TLLibrary) model.getLibrary(NS_VERSION_1_1, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary2 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_1, TEST_LIBRARY_NAME);
        PatchVersionHelper helper = new PatchVersionHelper();
        List<String> laterPatchVersions;

        assertNotNull(majorVersionLibrary);
        assertNotNull(minorVersionLibrary1);
        assertNotNull(minorVersionLibrary2);
        assertNotNull(patchVersionLibrary);

        laterPatchVersions = getLibraryNames(helper.getLaterPatchVersions(majorVersionLibrary));
        assertEquals(1, laterPatchVersions.size());
        assertEquals(LIBNAME_VERSION_1_0_1, laterPatchVersions.get(0));

        laterPatchVersions = getLibraryNames(helper.getLaterPatchVersions(minorVersionLibrary1));
        assertEquals(0, laterPatchVersions.size());

        laterPatchVersions = getLibraryNames(helper.getLaterPatchVersions(minorVersionLibrary2));
        assertEquals(2, laterPatchVersions.size());
        assertEquals(LIBNAME_VERSION_1_2_1, laterPatchVersions.get(0));
        assertEquals(LIBNAME_VERSION_1_2_2, laterPatchVersions.get(1));

        laterPatchVersions = getLibraryNames(helper.getLaterPatchVersions(patchVersionLibrary));
        assertEquals(1, laterPatchVersions.size());
        assertEquals(LIBNAME_VERSION_1_2_2, laterPatchVersions.get(0));
    }

    @Test
    public void testGetPatchedVersion() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2, FILE_VERSION_1_0_1);
        TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_1, TEST_LIBRARY_NAME);
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLExtensionPointFacet boPatch = patchVersionLibrary
                .getExtensionPointFacetType("ExtensionPoint_LookupBO_Summary");
        TLExtensionPointFacet corePatch = patchVersionLibrary
                .getExtensionPointFacetType("ExtensionPoint_LookupCore_Summary");
        TLExtensionPointFacet choicePatch = patchVersionLibrary
                .getExtensionPointFacetType("ExtensionPoint_LookupChoice_Shared");
        TLExtensionPointFacet opPatch = patchVersionLibrary
                .getExtensionPointFacetType("ExtensionPoint_VersionedService_LookupOperation_RQ");
        TLExtensionPointFacet resourcePatch = patchVersionLibrary
                .getExtensionPointFacetType("ExtensionPoint_LookupResource_LookupFacetShared");
        PatchVersionHelper helper = new PatchVersionHelper();

        assertNotNull(boPatch);
        assertNotNull(corePatch);
        assertNotNull(choicePatch);
        assertNotNull(opPatch);
        assertNotNull(resourcePatch);

        Versioned patchedBO = helper.getPatchedVersion(boPatch);
        Versioned patchedCore = helper.getPatchedVersion(corePatch);
        Versioned patchedChoice = helper.getPatchedVersion(choicePatch);
        Versioned patchedOp = helper.getPatchedVersion(opPatch);

        assertNotNull(patchedBO);
        assertTrue(patchedBO instanceof TLBusinessObject);
        assertEquals("1.2.0", patchedBO.getVersion());
        assertTrue(patchedBO.getOwningLibrary() == minorVersionLibrary);

        assertNotNull(patchedChoice);
        assertTrue(patchedChoice instanceof TLChoiceObject);
        assertEquals("1.2.0", patchedChoice.getVersion());
        assertTrue(patchedChoice.getOwningLibrary() == minorVersionLibrary);

        assertNotNull(patchedCore);
        assertTrue(patchedCore instanceof TLCoreObject);
        assertEquals("1.2.0", patchedCore.getVersion());
        assertTrue(patchedCore.getOwningLibrary() == minorVersionLibrary);

        assertNotNull(patchedOp);
        assertTrue(patchedOp instanceof TLOperation);
        assertEquals("1.2.0", patchedOp.getVersion());
        assertTrue(patchedOp.getOwningLibrary() == minorVersionLibrary);
    }

    @Test
    public void testGetLaterPatchVersions() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLLibrary patchVersionLibrary1 = (TLLibrary) model.getLibrary(NS_VERSION_1_2_1, TEST_LIBRARY_NAME);
        TLLibrary patchVersionLibrary2 = (TLLibrary) model.getLibrary(NS_VERSION_1_2_2, TEST_LIBRARY_NAME);
        TLBusinessObject minorVersionBO = minorVersionLibrary.getBusinessObjectType("LookupBO");
        TLCoreObject minorVersionCore = minorVersionLibrary.getCoreObjectType("LookupCore");
        TLChoiceObject minorVersionChoice = minorVersionLibrary.getChoiceObjectType("LookupChoice");
        TLOperation minorVersionOp = minorVersionLibrary.getService().getOperation("LookupOperation");
        TLResource minorVersionResource = minorVersionLibrary.getResourceType("LookupResource");
        PatchVersionHelper helper = new PatchVersionHelper();

        assertNotNull(minorVersionBO);
        assertNotNull(minorVersionCore);
        assertNotNull(minorVersionChoice);
        assertNotNull(minorVersionOp);
        assertNotNull(minorVersionResource);

        List<TLExtensionPointFacet> boPatches = helper.getLaterPatchVersions(minorVersionBO);
        List<TLExtensionPointFacet> corePatches = helper.getLaterPatchVersions(minorVersionCore);
        List<TLExtensionPointFacet> choicePatches = helper.getLaterPatchVersions(minorVersionChoice);
        List<TLExtensionPointFacet> opPatches = helper.getLaterPatchVersions(minorVersionOp);

        assertEquals(3, boPatches.size());
        assertEquals("1.2.1", boPatches.get(0).getVersion());
        assertTrue(boPatches.get(0).getOwningLibrary() == patchVersionLibrary1);
        assertEquals("1.2.2", boPatches.get(1).getVersion());
        assertTrue(boPatches.get(1).getOwningLibrary() == patchVersionLibrary2);
        assertEquals("1.2.2", boPatches.get(2).getVersion());
        assertTrue(boPatches.get(2).getOwningLibrary() == patchVersionLibrary2);

        assertEquals(3, corePatches.size());
        assertEquals("1.2.1", corePatches.get(0).getVersion());
        assertTrue(corePatches.get(0).getOwningLibrary() == patchVersionLibrary1);
        assertEquals("1.2.2", corePatches.get(1).getVersion());
        assertTrue(corePatches.get(1).getOwningLibrary() == patchVersionLibrary2);
        assertEquals("1.2.2", corePatches.get(2).getVersion());
        assertTrue(corePatches.get(2).getOwningLibrary() == patchVersionLibrary2);

        assertEquals(3, choicePatches.size());
        assertEquals("1.2.1", choicePatches.get(0).getVersion());
        assertTrue(choicePatches.get(0).getOwningLibrary() == patchVersionLibrary1);
        assertEquals("1.2.2", choicePatches.get(1).getVersion());
        assertTrue(choicePatches.get(1).getOwningLibrary() == patchVersionLibrary2);
        assertEquals("1.2.2", choicePatches.get(2).getVersion());
        assertTrue(choicePatches.get(2).getOwningLibrary() == patchVersionLibrary2);

        assertEquals(3, opPatches.size());
        assertEquals("1.2.1", opPatches.get(0).getVersion());
        assertTrue(opPatches.get(0).getOwningLibrary() == patchVersionLibrary1);
        assertEquals("1.2.2", opPatches.get(1).getVersion());
        assertTrue(opPatches.get(1).getOwningLibrary() == patchVersionLibrary2);
        assertEquals("1.2.2", opPatches.get(2).getVersion());
        assertTrue(opPatches.get(2).getOwningLibrary() == patchVersionLibrary2);
    }

    @Test
    public void testGetEligiblePatchVersionTargets() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        File patchLibraryFile = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/library_v01_02_03.otm"));
        PatchVersionHelper helper = new PatchVersionHelper();
        List<TLLibrary> eligibleLibraries;

        TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLLibrary patchVersionLibrary121 = (TLLibrary) model.getLibrary(NS_VERSION_1_2_1, TEST_LIBRARY_NAME);
        TLLibrary patchVersionLibrary123 = helper.createNewPatchVersion(minorVersionLibrary12, patchLibraryFile);

        TLBusinessObject minorVersionBO = minorVersionLibrary12.getBusinessObjectType("LookupBO");
        TLCoreObject minorVersionCore = minorVersionLibrary12.getCoreObjectType("LookupCore");
        TLChoiceObject minorVersionChoice = minorVersionLibrary12.getChoiceObjectType("LookupChoice");
        TLOperation minorVersionOp = minorVersionLibrary12.getService().getOperation("LookupOperation");

        eligibleLibraries = helper.getEligiblePatchVersionTargets(minorVersionBO.getSummaryFacet());
        assertEquals(1, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(patchVersionLibrary123));

        eligibleLibraries = helper.getEligiblePatchVersionTargets(minorVersionBO.getDetailFacet());
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(patchVersionLibrary121));
        assertTrue(eligibleLibraries.contains(patchVersionLibrary123));

        eligibleLibraries = helper.getEligiblePatchVersionTargets(minorVersionCore.getSummaryFacet());
        assertEquals(1, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(patchVersionLibrary123));

        eligibleLibraries = helper.getEligiblePatchVersionTargets(minorVersionCore.getDetailFacet());
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(patchVersionLibrary121));
        assertTrue(eligibleLibraries.contains(patchVersionLibrary123));

        eligibleLibraries = helper.getEligiblePatchVersionTargets(minorVersionChoice.getSharedFacet());
        assertEquals(1, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(patchVersionLibrary123));

        eligibleLibraries = helper.getEligiblePatchVersionTargets(minorVersionChoice.getChoiceFacet(null, "ChoiceA"));
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(patchVersionLibrary121));
        assertTrue(eligibleLibraries.contains(patchVersionLibrary123));

        eligibleLibraries = helper.getEligiblePatchVersionTargets(minorVersionOp.getRequest());
        assertEquals(1, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(patchVersionLibrary123));

        eligibleLibraries = helper.getEligiblePatchVersionTargets(minorVersionOp.getResponse());
        assertEquals(2, eligibleLibraries.size());
        assertTrue(eligibleLibraries.contains(patchVersionLibrary121));
        assertTrue(eligibleLibraries.contains(patchVersionLibrary123));
    }

    @Test
    public void testGetPreferredPatchVersionTarget() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        File patchLibraryFile = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/library_v01_02_03.otm"));
        PatchVersionHelper helper = new PatchVersionHelper();

        TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLLibrary patchVersionLibrary123 = helper.createNewPatchVersion(minorVersionLibrary12, patchLibraryFile);

        TLBusinessObject minorVersionBO = minorVersionLibrary12.getBusinessObjectType("LookupBO");
        TLCoreObject minorVersionCore = minorVersionLibrary12.getCoreObjectType("LookupCore");
        TLChoiceObject minorVersionChoice = minorVersionLibrary12.getChoiceObjectType("LookupChoice");
        TLOperation minorVersionOp = minorVersionLibrary12.getService().getOperation("LookupOperation");

        assertTrue(helper.getPreferredPatchVersionTarget(minorVersionBO.getSummaryFacet()) == patchVersionLibrary123);
        assertTrue(helper.getPreferredPatchVersionTarget(minorVersionCore.getSummaryFacet()) == patchVersionLibrary123);
        assertTrue(helper.getPreferredPatchVersionTarget(minorVersionChoice.getSharedFacet()) == patchVersionLibrary123);
        assertTrue(helper.getPreferredPatchVersionTarget(minorVersionOp.getRequest()) == patchVersionLibrary123);
    }

    @Test
    public void testNewPatchLibraryVersion_FromMinorVersion() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        File patchLibraryFile = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/library_v01_02_03.otm"));
        PatchVersionHelper helper = new PatchVersionHelper();

        TLLibrary patchVersionLibrary = helper.createNewPatchVersion(minorVersionLibrary, patchLibraryFile);

        assertNotNull(patchVersionLibrary);
        assertEquals("1.2.3", patchVersionLibrary.getVersion());
        assertEquals(minorVersionLibrary.getName(), patchVersionLibrary.getName());
        assertEquals(minorVersionLibrary.getBaseNamespace(), patchVersionLibrary.getBaseNamespace());
        assertEquals(minorVersionLibrary.getComments(), patchVersionLibrary.getComments());
    }

    @Test
    public void testNewPatchLibraryVersion_FromPatchVersion() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary existingPatchLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_1, TEST_LIBRARY_NAME);
        File patchLibraryFile = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/library_v01_02_03.otm"));
        PatchVersionHelper helper = new PatchVersionHelper();

        TLLibrary patchVersionLibrary = helper.createNewPatchVersion(existingPatchLibrary, patchLibraryFile);

        assertNotNull(patchVersionLibrary);
        assertEquals("1.2.3", patchVersionLibrary.getVersion());
        assertEquals(existingPatchLibrary.getName(), patchVersionLibrary.getName());
        assertEquals(existingPatchLibrary.getBaseNamespace(),
                patchVersionLibrary.getBaseNamespace());
        assertEquals(existingPatchLibrary.getComments(), patchVersionLibrary.getComments());
    }

    @Test
    public void testCreateNewPatch() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        TLLibrary patchLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2_2, TEST_LIBRARY_NAME);
        TLBusinessObject minorVersionBO = minorVersionLibrary.getBusinessObjectType("PatchTestBO");
        TLCoreObject minorVersionCore = minorVersionLibrary.getCoreObjectType("PatchTestCore");
        TLChoiceObject minorVersionChoice = minorVersionLibrary.getChoiceObjectType("PatchTestChoice");
        TLOperation minorVersionOp = minorVersionLibrary.getService().getOperation("PatchTestOperation");
        PatchVersionHelper helper = new PatchVersionHelper();

        TLExtensionPointFacet boPatch = helper.createNewPatch(minorVersionBO.getSummaryFacet(), patchLibrary);
        TLExtensionPointFacet corePatch = helper.createNewPatch(minorVersionCore.getSummaryFacet(), patchLibrary);
        TLExtensionPointFacet choicePatch = helper.createNewPatch(minorVersionChoice.getSharedFacet(), patchLibrary);
        TLExtensionPointFacet opPatch = helper.createNewPatch(minorVersionOp.getRequest(), patchLibrary);

        assertNotNull(boPatch);
        assertTrue(boPatch.getOwningLibrary() == patchLibrary);
        assertTrue(boPatch.getExtension().getExtendsEntity() == minorVersionBO.getSummaryFacet());
        assertTrue(boPatch.getAttributes().isEmpty());
        assertTrue(boPatch.getElements().isEmpty());
        assertTrue(boPatch.getIndicators().isEmpty());

        assertNotNull(corePatch);
        assertTrue(corePatch.getOwningLibrary() == patchLibrary);
        assertTrue(corePatch.getExtension().getExtendsEntity() == minorVersionCore.getSummaryFacet());
        assertTrue(corePatch.getAttributes().isEmpty());
        assertTrue(corePatch.getElements().isEmpty());
        assertTrue(corePatch.getIndicators().isEmpty());

        assertNotNull(choicePatch);
        assertTrue(choicePatch.getOwningLibrary() == patchLibrary);
        assertTrue(choicePatch.getExtension().getExtendsEntity() == minorVersionChoice.getSharedFacet());
        assertTrue(choicePatch.getAttributes().isEmpty());
        assertTrue(choicePatch.getElements().isEmpty());
        assertTrue(choicePatch.getIndicators().isEmpty());

        assertNotNull(opPatch);
        assertTrue(opPatch.getOwningLibrary() == patchLibrary);
        assertTrue(opPatch.getExtension().getExtendsEntity() == minorVersionOp.getRequest());
        assertTrue(opPatch.getAttributes().isEmpty());
        assertTrue(opPatch.getElements().isEmpty());
        assertTrue(opPatch.getIndicators().isEmpty());
    }

}
