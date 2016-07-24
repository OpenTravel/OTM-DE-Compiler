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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;

/**
 * Verifies the operation of the repository's search functions.
 * 
 * @author S. Livezey
 */
public class TestRepositorySearch extends RepositoryTestBase {

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea(TestRepositorySearch.class);
        startTestServer("versions-repository", 9292, TestRepositorySearch.class);
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testListRootNamespaces() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<String> rootNamespaces = testRepository.listRootNamespaces();

        assertEquals(1, rootNamespaces.size());
        assertTrue(rootNamespaces.contains("http://www.OpenTravel.org"));
    }

    @Test
    public void testListBaseNamespaces() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<String> baseNamespaces = testRepository.listBaseNamespaces();

        assertEquals(5, baseNamespaces.size());
        assertTrue(baseNamespaces.contains("http://www.OpenTravel.org"));
        assertTrue(baseNamespaces.contains("http://www.OpenTravel.org/ns"));
        assertTrue(baseNamespaces.contains("http://www.OpenTravel.org/ns/OTA2"));
        assertTrue(baseNamespaces.contains("http://www.OpenTravel.org/ns/OTA2/SchemaCompiler"));
        assertTrue(baseNamespaces
                .contains("http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test"));
    }

    @Test
    public void testListNamespaces() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<String> allNamespaces = testRepository.listAllNamespaces();

        assertEquals(8, allNamespaces.size());
        assertTrue(allNamespaces.contains("http://www.OpenTravel.org"));
        assertTrue(allNamespaces.contains("http://www.OpenTravel.org/ns"));
        assertTrue(allNamespaces.contains("http://www.OpenTravel.org/ns/OTA2"));
        assertTrue(allNamespaces.contains("http://www.OpenTravel.org/ns/OTA2/SchemaCompiler"));
        assertTrue(allNamespaces
                .contains("http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test"));
        assertTrue(allNamespaces
                .contains("http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_00"));
        assertTrue(allNamespaces
                .contains("http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_01"));
        assertTrue(allNamespaces
                .contains("http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_01_01"));
    }

    @Test
    public void testListNamespaceItems_allVersions_includeDraft() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<RepositoryItem> items = testRepository.listItems(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", false, true);
        List<String> itemFilenames = getFilenames(items);

        assertEquals(3, items.size());
        assertTrue(itemFilenames.contains("Version_Test_1_0_0.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_1_1.otm"));
    }

    @Test
    public void testListNamespaceItems_allVersions_finalOnly() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<RepositoryItem> items = testRepository.listItems(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", false, true);
        RepositoryItem item100 = findRepositoryItem(items, "Version_Test_1_0_0.otm");
        RepositoryItem item110 = findRepositoryItem(items, "Version_Test_1_1_0.otm");
        List<String> itemFilenames;

        try {
            testRepository.promote(item100);
            testRepository.promote(item110);

            items = testRepository.listItems(
                    "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", false, false);
            itemFilenames = getFilenames(items);

            assertEquals(2, items.size());
            assertTrue(itemFilenames.contains("Version_Test_1_0_0.otm"));
            assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));

        } finally {
            testRepository.demote(item110);
            testRepository.demote(item100);
        }
    }

    @Test
    public void testListNamespaceItems_latestVersion_includeDraft() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<RepositoryItem> items = testRepository.listItems(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", true, true);
        List<String> itemFilenames = getFilenames(items);

        assertEquals(1, items.size());
        assertTrue(itemFilenames.contains("Version_Test_1_1_1.otm"));
    }

    @Test
    public void testListNamespaceItems_latestVersion_finalOnly() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<RepositoryItem> items = testRepository.listItems(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", false, true);
        RepositoryItem item100 = findRepositoryItem(items, "Version_Test_1_0_0.otm");
        RepositoryItem item110 = findRepositoryItem(items, "Version_Test_1_1_0.otm");
        List<String> itemFilenames;

        try {
            testRepository.promote(item100);
            testRepository.promote(item110);

            items = testRepository.listItems(
                    "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", true, false);
            itemFilenames = getFilenames(items);

            assertEquals(1, items.size());
            assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));

        } finally {
            testRepository.demote(item110);
            testRepository.demote(item100);
        }
    }

    @Test
    public void testGetVersionHistory() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<RepositoryItem> allItems = testRepository.listItems(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", false, true);
        List<RepositoryItem> historyItems;
        List<String> itemFilenames;
        RepositoryItem testItem;

        // Make sure the results are the same, regardless of which version we send with the request
        testItem = findRepositoryItem(allItems, "Version_Test_1_0_0.otm");
        historyItems = repositoryManager.get().getVersionHistory(testItem);
        itemFilenames = getFilenames(historyItems);
        assertEquals(3, historyItems.size());
        assertTrue(itemFilenames.contains("Version_Test_1_1_1.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_0_0.otm"));

        testItem = findRepositoryItem(allItems, "Version_Test_1_1_0.otm");
        historyItems = repositoryManager.get().getVersionHistory(testItem);
        itemFilenames = getFilenames(historyItems);
        assertEquals(3, historyItems.size());
        assertTrue(itemFilenames.contains("Version_Test_1_1_1.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_0_0.otm"));

        testItem = findRepositoryItem(allItems, "Version_Test_1_1_1.otm");
        historyItems = repositoryManager.get().getVersionHistory(testItem);
        itemFilenames = getFilenames(historyItems);
        assertEquals(3, historyItems.size());
        assertTrue(itemFilenames.contains("Version_Test_1_1_1.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_0_0.otm"));
    }

    @Test
    public void testFreeTextSearch_allVersions_includeDraft() throws Exception {
        List<String> itemFilenames;

        // Search for a keyword in only two libraries
        itemFilenames = getFilenames(repositoryManager.get().search("red", false, true));

        assertEquals(3, itemFilenames.size());
        assertTrue(itemFilenames.contains("Version_Test_1_0_0.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_1_1.otm"));

        // Search for a keyword in only one library
        itemFilenames = getFilenames(repositoryManager.get().search("green", false, true));

        assertEquals(2, itemFilenames.size());
        assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));
        assertTrue(itemFilenames.contains("Version_Test_1_1_1.otm"));

        // Search for a non-existent keyword
        itemFilenames = getFilenames(repositoryManager.get().search("blue", false, true));

        assertEquals(0, itemFilenames.size());
    }

    @Test
    public void testFreeTextSearch_allVersions_finalOnly() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<RepositoryItem> items = testRepository.listItems(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", false, true);
        RepositoryItem item100 = findRepositoryItem(items, "Version_Test_1_0_0.otm");
        RepositoryItem item110 = findRepositoryItem(items, "Version_Test_1_1_0.otm");
        List<String> itemFilenames;

        try {
            testRepository.promote(item100);
            testRepository.promote(item110);

            // Search for a keyword in all three libraries
            itemFilenames = getFilenames(repositoryManager.get().search("red", false, false));

            assertEquals(2, itemFilenames.size());
            assertTrue(itemFilenames.contains("Version_Test_1_0_0.otm"));
            assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));

            // Search for a keyword in only two libraries
            itemFilenames = getFilenames(repositoryManager.get().search("green", false, false));

            assertEquals(1, itemFilenames.size());
            assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));

            // Search for a keyword in only one library
            itemFilenames = getFilenames(repositoryManager.get().search("blue", false, false));

            assertEquals(0, itemFilenames.size());

        } finally {
            testRepository.demote(item110);
            testRepository.demote(item100);
        }
    }

    @Test
    public void testFreeTextSearch_finalVersion_includeDraft() throws Exception {
        List<String> itemFilenames;

        // Search for a keyword in only two libraries
        itemFilenames = getFilenames(repositoryManager.get().search("red", true, true));

        assertEquals(1, itemFilenames.size());
        assertTrue(itemFilenames.contains("Version_Test_1_1_1.otm"));

        // Search for a keyword in only one library
        itemFilenames = getFilenames(repositoryManager.get().search("green", true, true));

        assertEquals(1, itemFilenames.size());
        assertTrue(itemFilenames.contains("Version_Test_1_1_1.otm"));

        // Search for a non-existent keyword
        itemFilenames = getFilenames(repositoryManager.get().search("blue", true, true));

        assertEquals(0, itemFilenames.size());
    }

    @Test
    public void testFreeTextSearch_latestVersion_finalOnly() throws Exception {
        Repository testRepository = repositoryManager.get().getRepository("test-repository");
        List<RepositoryItem> items = testRepository.listItems(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", false, true);
        RepositoryItem item100 = findRepositoryItem(items, "Version_Test_1_0_0.otm");
        RepositoryItem item110 = findRepositoryItem(items, "Version_Test_1_1_0.otm");
        List<String> itemFilenames;

        try {
            testRepository.promote(item100);
            testRepository.promote(item110);

            // Search for a keyword in all three libraries
            itemFilenames = getFilenames(repositoryManager.get().search("red", true, false));

            assertEquals(1, itemFilenames.size());
            assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));

            // Search for a keyword in only two libraries
            itemFilenames = getFilenames(repositoryManager.get().search("green", true, false));

            assertEquals(1, itemFilenames.size());
            assertTrue(itemFilenames.contains("Version_Test_1_1_0.otm"));

            // Search for a keyword in only one library
            itemFilenames = getFilenames(repositoryManager.get().search("blue", true, false));

            assertEquals(0, itemFilenames.size());

        } finally {
            testRepository.demote(item110);
            testRepository.demote(item100);
        }
    }

    private List<String> getFilenames(List<RepositoryItem> items) {
        List<String> itemFilenames = new ArrayList<String>();

        for (RepositoryItem item : items) {
            itemFilenames.add(item.getFilename());
        }
        return itemFilenames;
    }

}
