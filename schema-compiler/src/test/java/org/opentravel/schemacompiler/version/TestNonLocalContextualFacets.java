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

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.util.OTM16Upgrade;

/**
 * Verifies the versioning features for non-local contextual facet rollups.
 */
public class TestNonLocalContextualFacets extends AbstractVersionHelperTests {
	
	private static boolean originalEnabledFlag;
	
	@BeforeClass
	public static void enableOTM16() throws Exception {
		originalEnabledFlag = OTM16Upgrade.otm16Enabled;
		OTM16Upgrade.otm16Enabled = true;
	}
	
	@AfterClass
	public static void disableOTM16() throws Exception {
		OTM16Upgrade.otm16Enabled = originalEnabledFlag;
	}
	
	@Test
	public void testNonLocalContextualFacetRollups() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2, FILE_FACET1_1, FILE_FACET2_1);
        TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
        TLLibrary origFacet1Library = (TLLibrary) model.getLibrary(NS_FACET1_VERSION_1, FACET1_LIBRARY_NAME);
        TLLibrary origFacet2Library = (TLLibrary) model.getLibrary(NS_FACET2_VERSION_1, FACET2_LIBRARY_NAME);
        File newVersionLibraryFile = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/library_v02_00.otm"));
        File newVersionFacet1LibraryFile = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/facets1_v02_00.otm"));
        File newVersionFacet2LibraryFile = purgeExistingFile(new File(System.getProperty("user.dir"),
                "/target/test-save-location/facets2_v02_00.otm"));
        MajorVersionHelper helper = new MajorVersionHelper();

        assertNotNull(majorVersionLibrary);
        assertNotNull(origFacet1Library);
        assertNotNull(origFacet2Library);
        
        TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion(majorVersionLibrary, newVersionLibraryFile);
        TLLibrary newVersionFacet1Library = helper.createNewMajorVersion(origFacet1Library, newVersionFacet1LibraryFile);
        TLLibrary newVersionFacet2Library = helper.createNewMajorVersion(origFacet2Library, newVersionFacet2LibraryFile);
        
        assertNotNull(newMajorVersionLibrary);
        assertNotNull(newVersionFacet1Library);
        assertNotNull(newVersionFacet2Library);
        
        TLChoiceObject origChoice = majorVersionLibrary.getChoiceObjectType("LookupChoice");
        TLContextualFacet origChoiceA = origChoice.getChoiceFacet("ChoiceA");
        TLContextualFacet origChoiceB = origChoice.getChoiceFacet("ChoiceB");
        TLContextualFacet origChoiceANonLocal1 = getChildFacet( origChoiceA, "LookupChoice_ChoiceA_NonLocal1", origFacet1Library );
        TLContextualFacet origChoiceBNonLocal2 = getChildFacet( origChoiceB, "LookupChoice_ChoiceB_NonLocal2", origFacet1Library );
        TLContextualFacet newChoiceANonLocal1 = getChildFacet( origChoiceA, "LookupChoice_ChoiceA_NonLocal1", newVersionFacet1Library );
        TLContextualFacet newChoiceBNonLocal2 = getChildFacet( origChoiceB, "LookupChoice_ChoiceB_NonLocal2", newVersionFacet1Library );
        
        assertNotNull(origChoiceANonLocal1);
        assertNotNull(origChoiceBNonLocal2);
        assertNotNull(newChoiceANonLocal1);
        assertNotNull(newChoiceBNonLocal2);
        assertEquals(origChoiceANonLocal1.getOwningEntity(), origChoiceA);
        assertEquals(origChoiceBNonLocal2.getOwningEntity(), origChoiceB);
        assertEquals(newChoiceANonLocal1.getOwningEntity(), origChoiceA);
        assertEquals(newChoiceBNonLocal2.getOwningEntity(), origChoiceB);
        
        TLContextualFacet origChoiceANonLocal1a = getChildFacet( origChoiceANonLocal1, "LookupChoice_ChoiceA_NonLocal1_NonLocal1a", origFacet2Library );
        TLContextualFacet origChoiceBNonLocal2a = getChildFacet( origChoiceBNonLocal2, "LookupChoice_ChoiceB_NonLocal2_NonLocal2a", origFacet2Library );
        TLContextualFacet newChoiceANonLocal1a = getChildFacet( origChoiceANonLocal1, "LookupChoice_ChoiceA_NonLocal1_NonLocal1a", newVersionFacet2Library );
        TLContextualFacet newChoiceBNonLocal2a = getChildFacet( origChoiceBNonLocal2, "LookupChoice_ChoiceB_NonLocal2_NonLocal2a", newVersionFacet2Library );
        
        assertNotNull(origChoiceANonLocal1a);
        assertNotNull(origChoiceBNonLocal2a);
        assertNotNull(newChoiceANonLocal1a);
        assertNotNull(newChoiceBNonLocal2a);
        assertEquals(origChoiceANonLocal1a.getOwningEntity(), origChoiceANonLocal1);
        assertEquals(origChoiceBNonLocal2a.getOwningEntity(), origChoiceBNonLocal2);
        assertEquals(newChoiceANonLocal1a.getOwningEntity(), origChoiceANonLocal1);
        assertEquals(newChoiceBNonLocal2a.getOwningEntity(), origChoiceBNonLocal2);
	}
	
	private TLContextualFacet getChildFacet(TLContextualFacet owner, String facetName, TLLibrary facetLibrary) {
		TLContextualFacet childFacet = null;
		
		for (TLContextualFacet facet : owner.getChildFacets()) {
			if (facetName.equals( facet.getLocalName() ) && (facet.getOwningLibrary() == facetLibrary)) {
				childFacet = facet;
				break;
			}
		}
		return childFacet;
	}
	
}
