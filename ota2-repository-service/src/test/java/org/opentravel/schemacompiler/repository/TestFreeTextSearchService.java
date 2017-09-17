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
package org.opentravel.schemacompiler.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.index.RealTimeFreeTextSearchService;
import org.opentravel.schemacompiler.index.ReleaseSearchResult;
import org.opentravel.schemacompiler.index.SearchResult;
import org.opentravel.schemacompiler.util.RepositoryTestUtils;

/**
 * Verifies the operation of the OTA2.0 repository's indexing service and free-text search
 * utilities.
 * 
 * @author S. Livezey
 */
public class TestFreeTextSearchService {

    @Test
    public void testIndexAllRepositoryItems() throws Exception {
        FreeTextSearchService service = initSearchService("testIndexAllRepositoryItems");

        // Search on the base namespace and make sure all three repository items are returned
        try {
            List<SearchResult<?>> searchResults = service.search("version", null, false, false);
            Collection<String> filenames = getFilenames(searchResults);

            assertEquals(4, searchResults.size());
            assertTrue(filenames.contains("Version_Test_1_0_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_1.otm"));
            assertTrue(filenames.contains("Version_Release_1_0_0.otr"));

        } finally {
            service.stopService();
        }

        // Re-start the service and re-run the search to make sure we can reopen the index after
        // closing it
        service.startService();

        try {
            List<SearchResult<?>> searchResults = service.search("version", null, false, false);
            Collection<String> filenames = getFilenames(searchResults);

            assertEquals(4, searchResults.size());
            assertTrue(filenames.contains("Version_Test_1_0_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_1.otm"));
            assertTrue(filenames.contains("Version_Release_1_0_0.otr"));
            
            // Perform a where-used search for the release
            for (LibrarySearchResult libResult : getLibraryResults( searchResults )) {
            	List<ReleaseSearchResult> releases = service.getLibraryReleases( libResult, false );
            	
            	if (libResult.getRepositoryItem().getVersion().equals("1.1.1")) {
            		// Version 1.1.1 is not part of the release
            		assertEquals(0, releases.size());
            		
            	} else {
                	assertEquals(1,releases.size());
                	assertNotNull(releases.get(0).getItemContent());
                	assertEquals("Version_Release_1_0_0.otr", releases.get(0).getFilename());
            	}
            }

        } finally {
            service.stopService();
        }
    }

    @Test
    public void testContentKeywordSearch() throws Exception {
    	FreeTextSearchService service = initSearchService("testContentKeywordSearch");
        List<SearchResult<?>> searchResults;
        Collection<String> filenames;

        try {
            // Search for a keyword in all three libraries
            searchResults = service.search("red", null, false, false);
            filenames = getFilenames(searchResults);
            
            assertEquals(3, filenames.size());
            assertTrue(filenames.contains("Version_Test_1_0_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_1.otm"));

            // Search for a keyword in only two libraries
            searchResults = service.search("green", null, false, false);
            filenames = getFilenames(searchResults);

            assertEquals(2, filenames.size());
            assertTrue(filenames.contains("Version_Test_1_1_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_1.otm"));

            // Search for a non-existent keyword
            searchResults = service.search("blue", null, false, false);
            filenames = getFilenames(searchResults);

            assertEquals(0, filenames.size());

        } finally {
            service.stopService();
        }
    }

    protected FreeTextSearchService initSearchService(String testName) throws Exception {
        File repositorySnapshot = new File(System.getProperty("user.dir"),
                "/src/test/resources/repo-snapshots/versions-repository");
        File testRepository = new File(System.getProperty("user.dir"), "/target/test-workspace/"
                + TestFreeTextSearchService.class.getSimpleName() + "/" + testName
                + "/test-repository");
        File indexFolder = new File(System.getProperty("user.dir"), "/target/test-workspace/"
                + TestFreeTextSearchService.class.getSimpleName() + "/" + testName + "/index-test");
        FreeTextSearchService service;

        RepositoryTestUtils.deleteContents(indexFolder);
        RepositoryTestUtils.deleteContents(testRepository);
        RepositoryTestUtils.copyContents(repositorySnapshot, testRepository);

        service = new RealTimeFreeTextSearchService(indexFolder, new RepositoryManager(testRepository));
        service.startService();
        service.indexAllRepositoryItems();
        return service;
    }

    private Collection<String> getFilenames(Collection<SearchResult<?>> searchResults) {
        Collection<String> filenames = new HashSet<String>();

        for (SearchResult<?> result : searchResults) {
        	if (result instanceof LibrarySearchResult) {
                filenames.add( ((LibrarySearchResult) result).getRepositoryItem().getFilename() );
                
        	} else if (result instanceof ReleaseSearchResult) {
                filenames.add( ((ReleaseSearchResult) result).getFilename() );
        	}
        }
        return filenames;
    }
    
    private List<LibrarySearchResult> getLibraryResults(List<SearchResult<?>> fullResults) {
    	List<LibrarySearchResult> libResults = new ArrayList<>();
    	
    	for (SearchResult<?> result : fullResults) {
    		if (result instanceof LibrarySearchResult) {
    			libResults.add( (LibrarySearchResult) result );
    		}
    	}
    	return libResults;
    }
}
