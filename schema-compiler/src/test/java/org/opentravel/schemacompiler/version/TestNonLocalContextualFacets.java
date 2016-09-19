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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.util.URLUtils;

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
        MajorVersionHelper helper = new MajorVersionHelper();

        assertNotNull(majorVersionLibrary);
        assertNotNull(origFacet1Library);
        assertNotNull(origFacet2Library);
        
        List<TLContextualFacet> nonLocalFacets = helper.getNonLocalEntityFacets( majorVersionLibrary );
        TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion(majorVersionLibrary, newVersionLibraryFile);
        
        assertNotNull(newMajorVersionLibrary);
        assertEquals(4, nonLocalFacets.size());
        
        // The map holds the association between each of the original library versions
        // and the new version that was created from it.
        Map<TLLibrary,TLLibrary> sourceToTargetLibraryMap = new HashMap<>();
        
        sourceToTargetLibraryMap.put( majorVersionLibrary, newMajorVersionLibrary );
        
        // In this case, we are creating a new version of each library required for
        // our non-local facets.  When running against a repository, you will probably
        // need to check on whether a later (editable) version already exists.  If so,
        // we can just add that to the map instead of creating a new version.
        for (TLContextualFacet facet : nonLocalFacets) {
        	TLLibrary sourceLibrary = (TLLibrary) facet.getOwningLibrary();
        	TLLibrary targetLibrary = sourceToTargetLibraryMap.get( sourceLibrary );
        	
        	if (targetLibrary == null) {
        		File newVersionFile = getNewVersionLibraryFile( sourceLibrary );
        		
        		targetLibrary = helper.createNewMajorVersion(sourceLibrary, newVersionFile);
        		sourceToTargetLibraryMap.put( sourceLibrary, targetLibrary );
        		
        		// Double-check that the library is empty
                assertEquals(0, targetLibrary.getContextualFacetTypes().size());
        	}
        }
        
        // Perform the rollup of the non-local facets
        helper.rollupNonLocalFacets( nonLocalFacets, sourceToTargetLibraryMap );
        
        // Now validate the number of contextual facets that were just created
        int contextualFacetCount = 0;
        
        for (TLLibrary library : sourceToTargetLibraryMap.values()) {
        	if (library == newMajorVersionLibrary) continue;
        	contextualFacetCount += library.getContextualFacetTypes().size();
        }
        assertEquals(4, contextualFacetCount);
	}
	
	/**
	 * Returns the filename of the new version.  This is not necessary when running against
	 * a repository since the library names are pre-determined by the name/version of the
	 * original library version.
	 * 
	 * @param sourceLibrary  the source library for which a new version is being created
	 * @return File
	 */
	private File getNewVersionLibraryFile(TLLibrary sourceLibrary) {
		String sourceFilename = URLUtils.getUrlFilename( sourceLibrary.getLibraryUrl() );
		int versionIdx = sourceFilename.indexOf("_v");
		String targetFilename = sourceFilename.substring(0, versionIdx) + "_v20_00.otm";
		
		return purgeExistingFile(new File(System.getProperty("user.dir"),
				"/target/test-save-location/" + targetFilename));
	}
	
}
