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

package org.opentravel.schemacompiler.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryState;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.repocommon.index.IndexingConstants;
import org.opentravel.repocommon.util.RepositoryJaxbContext;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.testutil.RepositoryTestUtils;

import java.io.File;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * Verifies the functions of the <code>IndexingJobManager</code> class.
 */
public class TestIndexingJobManager {

    private static final org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory objectFactory1 =
        new org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory();
    private static final org.opentravel.ns.ota2.repositoryinfoext_v01_00.ObjectFactory objectFactory2 =
        new org.opentravel.ns.ota2.repositoryinfoext_v01_00.ObjectFactory();

    @Rule
    public TestName testName = new TestName();
    private RepositoryManager repositoryManager;
    private IndexingJobManager jobManager;
    private File jobFolder;

    @Before
    public void setup() throws Exception {
        File repositoryLocation =
            new File( System.getProperty( "user.dir" ), "/src/test/resources/repo-snapshots/versions-repository" );
        File mockSearchIndexLocation = new File( System.getProperty( "user.dir" ), "/target/test-output/"
            + TestIndexingJobManager.class.getSimpleName() + "/" + testName.getMethodName() + "/search-index" );

        repositoryManager = new RepositoryManager( repositoryLocation );
        jobManager = new IndexingJobManager( repositoryManager, mockSearchIndexLocation );
        jobFolder = jobManager.getIndexingJobsFolder();
        RepositoryTestUtils.deleteContents( jobFolder );
        jobFolder.mkdirs();
    }

    @Test
    public void testAddCreateIndexJob() throws Exception {
        jobManager.addIndexingJobs( IndexingConstants.JOB_TYPE_CREATE_INDEX, newRepositoryItemMessage( 1 ) );
        File[] jobFiles = jobFolder.listFiles();

        // Ensure that the correct type of job file(s) were created
        assertEquals( 1, jobFiles.length );
        assertEquals( IndexingJobType.CREATE, IndexingJobType.fromJobFile( jobFiles[0] ) );

        // Use the manager to retrieve the content of the job(s) we just created
        assertTrue( jobManager.nextIndexingJob() );
        assertEquals( IndexingJobType.CREATE, jobManager.currentJobType() );
        assertNotNull( jobManager.currentRepositoryItems() );
        assertEquals( 1, jobManager.currentRepositoryItems().size() );
        assertNull( jobManager.currentSubscriptionTarget() );

        // Make sure that no more jobs remain and that the previous batch file gets deleted
        assertFalse( jobManager.nextIndexingJob() );
        assertEquals( 0, jobFolder.listFiles().length );
    }

    @Test
    public void testAddDeleteIndexJob() throws Exception {
        jobManager.addIndexingJobs( IndexingConstants.JOB_TYPE_DELETE_INDEX, newRepositoryItemMessage( 1 ) );
        File[] jobFiles = jobFolder.listFiles();

        // Ensure that the correct type of job file(s) were created
        assertEquals( 1, jobFiles.length );
        assertEquals( IndexingJobType.DELETE, IndexingJobType.fromJobFile( jobFiles[0] ) );

        // Use the manager to retrieve the content of the job(s) we just created
        assertTrue( jobManager.nextIndexingJob() );
        assertEquals( IndexingJobType.DELETE, jobManager.currentJobType() );
        assertNotNull( jobManager.currentRepositoryItems() );
        assertEquals( 1, jobManager.currentRepositoryItems().size() );
        assertNull( jobManager.currentSubscriptionTarget() );

        // Make sure that no more jobs remain and that the previous batch file gets deleted
        assertFalse( jobManager.nextIndexingJob() );
        assertEquals( 0, jobFolder.listFiles().length );
    }

    @Test
    public void testAddDeleteAllJob() throws Exception {
        jobManager.addIndexingJobs( IndexingConstants.JOB_TYPE_DELETE_ALL, "" );
        File[] jobFiles = jobFolder.listFiles();

        // Ensure that the correct type of job file(s) were created
        assertEquals( 1, jobFiles.length );
        assertEquals( IndexingJobType.DELETE_ALL, IndexingJobType.fromJobFile( jobFiles[0] ) );

        // Use the manager to retrieve the content of the job(s) we just created
        assertTrue( jobManager.nextIndexingJob() );
        assertEquals( IndexingJobType.DELETE_ALL, jobManager.currentJobType() );
        assertNull( jobManager.currentRepositoryItems() );
        assertNull( jobManager.currentSubscriptionTarget() );

        // Make sure that no more jobs remain and that the previous batch file gets deleted
        assertFalse( jobManager.nextIndexingJob() );
        assertEquals( 0, jobFolder.listFiles().length );
    }

    @Test
    public void testAddSubscriptionIndexJob() throws Exception {
        jobManager.addIndexingJobs( IndexingConstants.JOB_TYPE_SUBSCRIPTION, newSubscriptionTargetMessage() );
        File[] jobFiles = jobFolder.listFiles();

        // Ensure that the correct type of job file(s) were created
        assertEquals( 1, jobFiles.length );
        assertEquals( IndexingJobType.SUBSCRIPTION, IndexingJobType.fromJobFile( jobFiles[0] ) );

        // Use the manager to retrieve the content of the job(s) we just created
        assertTrue( jobManager.nextIndexingJob() );
        assertEquals( IndexingJobType.SUBSCRIPTION, jobManager.currentJobType() );
        assertNull( jobManager.currentRepositoryItems() );
        assertNotNull( jobManager.currentSubscriptionTarget() );

        // Make sure that no more jobs remain and that the previous batch file gets deleted
        assertFalse( jobManager.nextIndexingJob() );
        assertEquals( 0, jobFolder.listFiles().length );
    }

    @Test
    public void testAddLargeBatchJob() throws Exception {
        jobManager.addIndexingJobs( IndexingConstants.JOB_TYPE_CREATE_INDEX,
            newRepositoryItemMessage( IndexingJobManager.MAX_INDEXING_BATCH_SIZE * 3 ) );
        File[] jobFiles = jobFolder.listFiles();

        assertEquals( 3, jobFiles.length );

        for (int i = 0; i < 3; i++) {
            assertTrue( jobManager.nextIndexingJob() );
            assertEquals( IndexingJobType.CREATE, jobManager.currentJobType() );
            assertNotNull( jobManager.currentRepositoryItems() );
            assertEquals( IndexingJobManager.MAX_INDEXING_BATCH_SIZE, jobManager.currentRepositoryItems().size() );
        }
        assertFalse( jobManager.nextIndexingJob() );
        assertEquals( 0, jobFolder.listFiles().length );
    }

    @Test
    public void testInvalidMessageContent() throws Exception {
        jobManager.addIndexingJobs( IndexingConstants.JOB_TYPE_SUBSCRIPTION, "bad-content" );
        jobManager.addIndexingJobs( IndexingConstants.JOB_TYPE_SUBSCRIPTION, newSubscriptionTargetMessage() );

        assertEquals( 2, jobFolder.listFiles().length );
        assertTrue( jobManager.nextIndexingJob() ); // Silently skipped the first batch due to bad content
        assertEquals( IndexingJobType.SUBSCRIPTION, jobManager.currentJobType() );

        assertFalse( jobManager.nextIndexingJob() );
        assertEquals( 0, jobFolder.listFiles().length );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMessageType() throws Exception {
        IndexingJobType.toJobFile( "test", jobFolder, "bad-message-type" );
    }

    private String newSubscriptionTargetMessage() throws Exception {
        SubscriptionTarget sTarget = new SubscriptionTarget();

        sTarget.setBaseNamespace( "http://www.opentravel.org/ns" );
        sTarget.setLibraryName( "TestLibrary" );
        sTarget.setVersion( "1.0.0" );

        try (StringWriter writer = new StringWriter()) {
            JAXBContext jaxbContext = RepositoryJaxbContext.getExtContext();
            Marshaller m = jaxbContext.createMarshaller();

            m.marshal( objectFactory2.createSubscriptionTarget( sTarget ), writer );
            return writer.toString();
        }
    }

    private String newRepositoryItemMessage(int itemCount) throws Exception {
        LibraryInfoListType itemList = new LibraryInfoListType();

        for (int i = 0; i < itemCount; i++) {
            LibraryInfoType item = new LibraryInfoType();

            item.setOwningRepository( repositoryManager.getId() );
            item.setBaseNamespace( "http://www.opentravel.org/ns" );
            item.setFilename( "TestLibrary_1_0_0.otm" );
            item.setLibraryName( "TestLibrary" );
            item.setVersion( "1.0.0" );
            item.setVersionScheme( "OTA2" );
            item.setStatus( LibraryStatus.DRAFT );
            item.setState( RepositoryState.MANAGED_UNLOCKED );
            itemList.getLibraryInfo().add( item );
        }

        try (StringWriter writer = new StringWriter()) {
            JAXBContext jaxbContext = RepositoryJaxbContext.getContext();
            Marshaller m = jaxbContext.createMarshaller();

            m.marshal( objectFactory1.createLibraryInfoList( itemList ), writer );
            return writer.toString();
        }
    }

}
