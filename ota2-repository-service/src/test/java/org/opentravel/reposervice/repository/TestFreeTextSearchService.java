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

package org.opentravel.reposervice.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyIdentityType;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyType;
import org.opentravel.repocommon.index.AssemblySearchResult;
import org.opentravel.repocommon.index.FreeTextSearchService;
import org.opentravel.repocommon.index.LibrarySearchResult;
import org.opentravel.repocommon.index.RealTimeFreeTextSearchService;
import org.opentravel.repocommon.index.ReleaseSearchResult;
import org.opentravel.repocommon.index.SearchResult;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.ServiceAssembly;
import org.opentravel.schemacompiler.repository.testutil.RepositoryTestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Verifies the operation of the OTA2.0 repository's indexing service and free-text search utilities.
 * 
 * @author S. Livezey
 */
public class TestFreeTextSearchService {

    @Test
    public void testIndexAllRepositoryItems() throws Exception {
        FreeTextSearchService service = initSearchService( "testIndexAllRepositoryItems" );

        // Search on the base namespace and make sure all three repository items are returned
        try {
            List<SearchResult<Object>> searchResults = service.search( "version", null, false, false );
            Collection<String> filenames = getFilenames( searchResults );

            assertEquals( 5, searchResults.size() );
            assertTrue( filenames.contains( "Version_Test_1_0_0.otm" ) );
            assertTrue( filenames.contains( "Version_Test_1_1_0.otm" ) );
            assertTrue( filenames.contains( "Version_Test_1_1_1.otm" ) );
            assertTrue( filenames.contains( "Version_Release_1_0_0.otr" ) );
            assertTrue( filenames.contains( "Version_Assembly_1_0_0.osm" ) );

        } finally {
            service.stopService();
        }

        // Re-start the service and re-run the search to make sure we can reopen the index after
        // closing it
        service.startService();

        try {
            List<SearchResult<Object>> searchResults = service.search( "version", null, false, false );
            Collection<String> filenames = getFilenames( searchResults );

            assertEquals( 5, searchResults.size() );
            assertTrue( filenames.contains( "Version_Test_1_0_0.otm" ) );
            assertTrue( filenames.contains( "Version_Test_1_1_0.otm" ) );
            assertTrue( filenames.contains( "Version_Test_1_1_1.otm" ) );
            assertTrue( filenames.contains( "Version_Release_1_0_0.otr" ) );
            assertTrue( filenames.contains( "Version_Assembly_1_0_0.osm" ) );

            // Perform a where-used search for the release
            for (LibrarySearchResult libResult : getLibraryResults( searchResults )) {
                List<ReleaseSearchResult> releases = service.getLibraryReleases( libResult, false );

                if (libResult.getRepositoryItem().getVersion().equals( "1.1.1" )) {
                    // Version 1.1.1 is not part of the release
                    assertEquals( 0, releases.size() );

                } else {
                    assertEquals( 1, releases.size() );
                    assertNotNull( releases.get( 0 ).getItemContent() );
                    assertEquals( "Version_Release_1_0_0.otr", releases.get( 0 ).getFilename() );
                }
            }

        } finally {
            service.stopService();
        }
    }

    @Test
    public void testContentKeywordSearch() throws Exception {
        FreeTextSearchService service = initSearchService( "testContentKeywordSearch" );
        List<SearchResult<Object>> searchResults;
        Collection<String> filenames;

        try {
            // Search for a keyword in all three libraries
            searchResults = service.search( "red", null, false, false );
            filenames = getFilenames( searchResults );

            assertEquals( 3, filenames.size() );
            assertTrue( filenames.contains( "Version_Test_1_0_0.otm" ) );
            assertTrue( filenames.contains( "Version_Test_1_1_0.otm" ) );
            assertTrue( filenames.contains( "Version_Test_1_1_1.otm" ) );

            // Search for a keyword in only two libraries
            searchResults = service.search( "green", null, false, false );
            filenames = getFilenames( searchResults );

            assertEquals( 2, filenames.size() );
            assertTrue( filenames.contains( "Version_Test_1_1_0.otm" ) );
            assertTrue( filenames.contains( "Version_Test_1_1_1.otm" ) );

            // Search for a non-existent keyword
            searchResults = service.search( "nonexistentkeyword", null, false, false );
            filenames = getFilenames( searchResults );

            assertEquals( 0, filenames.size() );

        } finally {
            service.stopService();
        }
    }

    @Test
    public void testSearchQueries() throws Exception {
        FreeTextSearchService service = initSearchService( "testSearchQueries" );
        List<SearchResult<Object>> searchResults;

        searchResults = service.search( "Version", TLLibraryStatus.DRAFT, false, true );
        assertEquals( 5, searchResults.size() );

        // Test resolution of item content and search results based on search index IDs and
        // repository items
        for (SearchResult<Object> result : searchResults) {
            if (result.getEntityType().equals( TLLibrary.class )) {
                LibrarySearchResult lsr = service.getLibrary( result.getSearchIndexId(), true );
                TLLibrary library = lsr.getItemContent();
                RepositoryItem item;

                assertNotNull( library );
                assertEquals( result.getSearchIndexId(), lsr.getSearchIndexId() );
                item = service.getRepositoryManager().getRepositoryItem( library.getBaseNamespace(),
                    library.getName() + "_" + library.getVersion().replace( '.', '_' ) + ".otm", library.getVersion() );
                lsr = service.getLibrary( item, true );
                assertEquals( result.getSearchIndexId(), lsr.getSearchIndexId() );

            } else if (result.getEntityType().equals( Release.class )) {
                ReleaseSearchResult rsr = service.getRelease( result.getSearchIndexId(), true );
                Release release = rsr.getItemContent();
                RepositoryItem item;

                assertNotNull( release );
                assertEquals( result.getSearchIndexId(), rsr.getSearchIndexId() );
                item = service.getRepositoryManager().getRepositoryItem( release.getBaseNamespace(),
                    release.getName() + "_" + release.getVersion().replace( '.', '_' ) + ".otr", release.getVersion() );
                rsr = service.getRelease( item, true );
                assertEquals( result.getSearchIndexId(), rsr.getSearchIndexId() );

            } else if (result.getEntityType().equals( ServiceAssembly.class )) {
                AssemblySearchResult asr = service.getAssembly( result.getSearchIndexId(), true );
                AssemblyType assembly = asr.getItemContent();
                AssemblyIdentityType asmIdentity;
                RepositoryItem item;

                assertNotNull( assembly );
                assertEquals( result.getSearchIndexId(), asr.getSearchIndexId() );
                asmIdentity = assembly.getAssemblyIdentity();
                item = service.getRepositoryManager().getRepositoryItem( asmIdentity.getBaseNamespace(),
                    asmIdentity.getFilename(), asmIdentity.getVersion() );
                asr = service.getAssembly( item, true );
                assertEquals( result.getSearchIndexId(), asr.getSearchIndexId() );
            }
        }

        // Test various other types of search parameters
        searchResults = service.search( "Version", TLLibraryStatus.UNDER_REVIEW, false, true );
        assertEquals( 0, searchResults.size() );
        searchResults = service.search( "Version", TLLibraryStatus.FINAL, false, true );
        assertEquals( 0, searchResults.size() );
        searchResults = service.search( "Version", TLLibraryStatus.OBSOLETE, false, true );
        assertEquals( 0, searchResults.size() );

        searchResults = service.search( "Version", TLLibraryStatus.DRAFT, true, true );
        assertEquals( 3, searchResults.size() );
        searchResults = service.search( "Version", TLLibraryStatus.UNDER_REVIEW, true, true );
        assertEquals( 2, searchResults.size() );
        searchResults = service.search( "Version", TLLibraryStatus.FINAL, true, true );
        assertEquals( 2, searchResults.size() );
        searchResults = service.search( "Version", TLLibraryStatus.OBSOLETE, true, true );
        assertEquals( 0, searchResults.size() );

        // Search for direct and indirect where-used
        RepositoryItem item = service.getRepositoryManager().getRepositoryItem(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", "Version_Test_1_0_0.otm", "1.0.0" );
        LibrarySearchResult lsr = service.getLibrary( item, true );
        org.opentravel.repocommon.index.EntitySearchResult esr =
            service.getEntity( lsr.getSearchIndexId(), "SimpleCore", true );

        assertEquals( 2, service.getLibraryWhereUsed( lsr, true, true ).size() );
        assertEquals( 1, service.getEntityWhereUsed( esr, true, true ).size() );

        // Search for validation errors/warnings
        assertEquals( 1, service.getLibraryFindings( lsr.getSearchIndexId() ).size() );
        assertEquals( 1, service.getEntityFindings( esr.getSearchIndexId() ).size() );
    }

    protected FreeTextSearchService initSearchService(String testName) throws Exception {
        File repositorySnapshot =
            new File( System.getProperty( "user.dir" ), "/src/test/resources/repo-snapshots/versions-repository" );
        File testRepository = new File( System.getProperty( "user.dir" ), "/target/test-workspace/"
            + TestFreeTextSearchService.class.getSimpleName() + "/" + testName + "/test-repository" );
        File indexFolder = new File( System.getProperty( "user.dir" ), "/target/test-workspace/"
            + TestFreeTextSearchService.class.getSimpleName() + "/" + testName + "/index-test" );
        FreeTextSearchService service;

        RepositoryTestUtils.deleteContents( indexFolder );
        RepositoryTestUtils.deleteContents( testRepository );
        RepositoryTestUtils.copyContents( repositorySnapshot, testRepository );

        service = new RealTimeFreeTextSearchService( indexFolder, new RepositoryManager( testRepository ) );
        service.startService();
        service.indexAllRepositoryItems();
        return service;
    }

    private Collection<String> getFilenames(Collection<SearchResult<Object>> searchResults) {
        Collection<String> filenames = new HashSet<String>();

        for (SearchResult<?> result : searchResults) {
            if (result instanceof LibrarySearchResult) {
                filenames.add( ((LibrarySearchResult) result).getRepositoryItem().getFilename() );

            } else if (result instanceof ReleaseSearchResult) {
                filenames.add( ((ReleaseSearchResult) result).getFilename() );

            } else if (result instanceof AssemblySearchResult) {
                filenames.add( ((AssemblySearchResult) result).getFilename() );
            }
        }
        return filenames;
    }

    private List<LibrarySearchResult> getLibraryResults(List<SearchResult<Object>> fullResults) {
        List<LibrarySearchResult> libResults = new ArrayList<>();

        for (SearchResult<?> result : fullResults) {
            if (result instanceof LibrarySearchResult) {
                libResults.add( (LibrarySearchResult) result );
            }
        }
        return libResults;
    }
}
