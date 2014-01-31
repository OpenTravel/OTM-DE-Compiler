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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
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
            List<RepositoryItem> searchResults = service.query("version", false, true);
            Collection<String> filenames = getFilenames(searchResults);

            assertEquals(3, searchResults.size());
            assertTrue(filenames.contains("Version_Test_1_0_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_1.otm"));

        } finally {
            service.stopService();
        }

        // Re-start the service and re-run the search to make sure we can reopen the index after
        // closing it
        service.startService();

        try {
            List<RepositoryItem> searchResults = service.query("version", false, true);
            Collection<String> filenames = getFilenames(searchResults);

            assertEquals(3, searchResults.size());
            assertTrue(filenames.contains("Version_Test_1_0_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_1.otm"));

        } finally {
            service.stopService();
        }
    }

    @Test
    public void testContentKeywordSearch() throws Exception {
        FreeTextSearchService service = initSearchService("testContentKeywordSearch");
        List<RepositoryItem> searchResults;
        Collection<String> filenames;

        try {
            // Search for a keyword in all three libraries
            searchResults = service.query("red", false, true);
            filenames = getFilenames(searchResults);

            assertEquals(3, searchResults.size());
            assertTrue(filenames.contains("Version_Test_1_0_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_1.otm"));

            // Search for a keyword in only two libraries
            searchResults = service.query("green", false, true);
            filenames = getFilenames(searchResults);

            assertEquals(2, searchResults.size());
            assertTrue(filenames.contains("Version_Test_1_1_0.otm"));
            assertTrue(filenames.contains("Version_Test_1_1_1.otm"));

            // Search for a keyword in only one library
            searchResults = service.query("blue", false, true);
            filenames = getFilenames(searchResults);

            assertEquals(1, searchResults.size());
            assertTrue(filenames.contains("Version_Test_1_1_1.otm"));

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

        service = new FreeTextSearchService(indexFolder, new RepositoryManager(testRepository));
        service.setRealTimeIndexing(true);
        service.startService();
        service.indexAllRepositoryItems();
        return service;
    }

    private Collection<String> getFilenames(Collection<RepositoryItem> items) {
        Collection<String> filenames = new HashSet<String>();

        for (RepositoryItem item : items) {
            filenames.add(item.getFilename());
        }
        return filenames;
    }

}
