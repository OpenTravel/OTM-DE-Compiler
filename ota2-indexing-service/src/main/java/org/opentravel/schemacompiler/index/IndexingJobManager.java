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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.repocommon.util.RepositoryJaxbContext;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.util.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


/**
 * Manages the flow and sequence of indexing jobs. New jobs are saved to persistent storage ahead of processing to
 * prevent loss in case of system errors. As job batches are completed in the order received, the batches are deleted
 * from persistent storage.
 */
public class IndexingJobManager {

    public static final int MAX_INDEXING_BATCH_SIZE = 10;

    private static final ObjectFactory objectFactory = new ObjectFactory();
    private static Logger log = LogManager.getLogger( IndexingJobManager.class );

    private DateFormat baseFilenamePattern = new SimpleDateFormat( "yyyyMMddkkmmssSSS" );
    private RepositoryManager repositoryManager;
    private File indexingJobsFolder;
    private File currentJobFile;
    private IndexingJobType currentJobType;
    private SubscriptionTarget subscriptionTarget;
    private List<RepositoryItem> repositoryItems;

    /**
     * Constructor that specifies the folder location of the search index.
     * 
     * @param repositoryManager the repository manager used to access local repository content
     * @param searchIndexLocation the folder location of the search index
     */
    public IndexingJobManager(RepositoryManager repositoryManager, File searchIndexLocation) {
        this.repositoryManager = repositoryManager;
        this.indexingJobsFolder =
            new File( searchIndexLocation.getParentFile().getAbsolutePath() + "/search-index-jobs" );
        this.indexingJobsFolder.mkdirs();
    }

    /**
     * Returns the folder location where the indexing batch job files are stored.
     *
     * @return File
     */
    public File getIndexingJobsFolder() {
        return indexingJobsFolder;
    }

    /**
     * Iterates to the next available indexing job. If a new job is available, this method will return true (false
     * otherwise).
     * 
     * @return boolean
     */
    public synchronized boolean nextIndexingJob() {
        boolean success = false;
        List<File> jobFiles;

        while (!success) {
            FileUtils.delete( currentJobFile );
            jobFiles = new ArrayList<>( Arrays.asList( indexingJobsFolder.listFiles() ) );
            Collections.sort( jobFiles, (f1, f2) -> f1.getName().compareTo( f2.getName() ) );
            File jobFile = jobFiles.isEmpty() ? null : jobFiles.get( 0 );

            try {
                loadJob( jobFile );
                success = true;

            } catch (Exception e) {
                log.error(
                    "Error loading indexing batch job file: " + ((jobFile == null) ? "NULL" : jobFile.getName()) );
            }
        }
        return (currentJobFile != null);
    }

    /**
     * Loads the next indexing batch job from the specified file. If a null value is passed to this method, the current
     * indexing job is cleared to indicate that no further jobs currently exist.
     * 
     * @param jobFile the indexing batch job file (may be null)
     * @throws JAXBException thrown if an error occurs during unmarshalling content from the job file
     * @throws IOException thrown if the file content is unreadable
     */
    protected void loadJob(File jobFile) throws IOException, JAXBException {
        if (jobFile != null) {
            try (Reader reader = new FileReader( jobFile )) {
                currentJobType = IndexingJobType.fromJobFile( jobFile );
                currentJobFile = jobFile;

                switch (currentJobType) {
                    case CREATE:
                    case DELETE:
                        repositoryItems = unmarshallRepositoryItems( reader );
                        subscriptionTarget = null;
                        break;
                    case DELETE_ALL:
                        repositoryItems = null;
                        subscriptionTarget = null;
                        break;
                    case SUBSCRIPTION:
                        subscriptionTarget = unmarshallSubscriptionTarget( reader );
                        repositoryItems = null;
                        break;
                }
            }

        } else {
            clearCurrentJob();
        }
    }

    /**
     * Clears all information about the current indexing batch job.
     */
    protected void clearCurrentJob() {
        currentJobFile = null;
        currentJobType = null;
        repositoryItems = null;
        subscriptionTarget = null;
    }

    /**
     * Returns the type of the current job. If no job is currently available, this method will return null.
     * <p>
     * NOTE: For thread safety, it is important that this method is called from the same thread used to call the
     * {@link #nextIndexingJob()} method.
     * 
     * @return IndexingJobType
     */
    public IndexingJobType currentJobType() {
        return currentJobType;
    }

    /**
     * Returns the subscription target for the current job. If no job is currently available or the job is not related
     * to a subscription, this method will return null.
     * <p>
     * NOTE: For thread safety, it is important that this method is called from the same thread used to call the
     * {@link #nextIndexingJob()} method.
     * 
     * @return SubscriptionTarget
     */
    public SubscriptionTarget currentSubscriptionTarget() {
        return subscriptionTarget;
    }

    /**
     * Returns the list of repository items for the current job. If no job is currently available or the job is not
     * related to repository items, this method will return null.
     * <p>
     * NOTE: For thread safety, it is important that this method is called from the same thread used to call the
     * {@link #nextIndexingJob()} method.
     * 
     * @return List&lt;RepositoryItem&gt;
     */
    public List<RepositoryItem> currentRepositoryItems() {
        return repositoryItems;
    }

    /**
     * Adds the given indexing message request to the backlog of indexing batch jobs.
     * 
     * @param messageType the type of the indexing request
     * @param messageContent the message content of the indexing request
     * @throws IOException thrown if an error occurrs while saving the indexing job to persistent storage
     * @throws JAXBException thrown if the message content cannot be parsed
     */
    public synchronized void addIndexingJobs(String messageType, String messageContent)
        throws IOException, JAXBException {
        IndexingJobType jobType = IndexingJobType.fromMessageType( messageType );

        // Pausing for 1ms seems trivial but it eliminates a race condition that can occur if
        // this method is called twice within the same millisecond. This is because we use a
        // timestamp down to and including milliseconds in our job file naming algorithm.
        pause( 1L );

        switch (jobType) {
            case CREATE:
            case DELETE:
                List<RepositoryItem> itemList = unmarshallRepositoryItems( new StringReader( messageContent ) );
                List<LibraryInfoListType> batchList = createBatchJobs( itemList );

                for (LibraryInfoListType batch : batchList) {
                    marshallRepositoryItems( batch, nextIndexingFile( messageType ) );
                }
                break;
            case SUBSCRIPTION:
                saveBatchContent( nextIndexingFile( messageType ), messageContent );
                break;
            case DELETE_ALL:
                saveBatchContent( nextIndexingFile( messageType ), "" );
                break;
        }
    }

    /**
     * Returns the next indexing file to which indexing jobs should be saved.
     * 
     * @param messageType the type of the indexing request
     * @return File
     */
    protected File nextIndexingFile(String messageType) {
        String filenameTimestamp = baseFilenamePattern.format( new Date() );
        int fileCounter = 0;
        File jobFile;

        do {
            jobFile = IndexingJobType.toJobFile( String.format( "%s-%05d", filenameTimestamp, fileCounter ),
                indexingJobsFolder, messageType );
            fileCounter++;

        } while (jobFile.exists());
        return jobFile;
    }

    /**
     * Breaks up the given list of repository items into smaller discrete batches that can be processed separately.
     * 
     * @param itemList the full list of repository items to divide into batches
     * @return List&lt;LibraryInfoListType&gt;
     */
    private List<LibraryInfoListType> createBatchJobs(List<RepositoryItem> itemList) {
        List<LibraryInfoListType> batchList = new ArrayList<>();
        LibraryInfoListType currentBatch = new LibraryInfoListType();

        for (RepositoryItem item : itemList) {
            List<LibraryInfoType> jaxbItems = currentBatch.getLibraryInfo();

            jaxbItems.add( RepositoryUtils.createItemMetadata( item ) );

            if (jaxbItems.size() == MAX_INDEXING_BATCH_SIZE) {
                batchList.add( currentBatch );
                currentBatch = new LibraryInfoListType();
            }
        }

        if (!currentBatch.getLibraryInfo().isEmpty()) {
            batchList.add( currentBatch );
        }
        return batchList;
    }

    /**
     * Marshalls the list of repository items to the specified indexing job file.
     * 
     * @param itemList the list of repository items to marshall
     * @param jobFile the indexing batch job file where the list of repository items will be stored
     * @throws JAXBException thrown if an error occurs during unmarshalling
     * @throws IOException thrown if the message content is unreadable
     */
    private void marshallRepositoryItems(LibraryInfoListType itemList, File jobFile) throws JAXBException, IOException {
        try (Writer writer = new FileWriter( jobFile )) {
            JAXBContext jaxbContext = RepositoryJaxbContext.getContext();
            Marshaller m = jaxbContext.createMarshaller();

            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            m.marshal( objectFactory.createLibraryInfoList( itemList ), writer );
        }
    }

    /**
     * Unmarshalls and returns the list of <code>RepositoryItem</code>s from the given reader.
     * 
     * @param reader the reader from which to obtain message content
     * @return List&lt;RepositoryItem&gt;
     * @throws JAXBException thrown if an error occurs during unmarshalling
     */
    @SuppressWarnings("unchecked")
    private List<RepositoryItem> unmarshallRepositoryItems(Reader reader) throws JAXBException {
        JAXBContext jaxbContext = RepositoryJaxbContext.getContext();
        Unmarshaller u = jaxbContext.createUnmarshaller();
        JAXBElement<LibraryInfoListType> msgElement = (JAXBElement<LibraryInfoListType>) u.unmarshal( reader );
        List<RepositoryItem> itemList = new ArrayList<>();

        for (LibraryInfoType msgItem : msgElement.getValue().getLibraryInfo()) {
            itemList.add( RepositoryUtils.createRepositoryItem( repositoryManager, msgItem ) );
        }
        return itemList;
    }

    /**
     * Unmarshalls and returns the <code>SubscriptionTarget</code>s from the given reader.
     * 
     * @param reader the reader from which to obtain message content
     * @return SubscriptionTarget
     * @throws JAXBException thrown if an error occurs during unmarshalling
     */
    @SuppressWarnings("unchecked")
    private SubscriptionTarget unmarshallSubscriptionTarget(Reader reader) throws JAXBException {
        JAXBContext jaxbContext = RepositoryJaxbContext.getExtContext();
        Unmarshaller u = jaxbContext.createUnmarshaller();
        JAXBElement<SubscriptionTarget> msgElement = (JAXBElement<SubscriptionTarget>) u.unmarshal( reader );

        return msgElement.getValue();
    }

    /**
     * Saves the raw batch content provided to the specified indexing job file.
     * 
     * @param jobFile the indexing batch job file to which content should be saved
     * @param batchContent the batch content to be saved
     * @throws IOException thrown if an error occurs while saving content to the file
     */
    private void saveBatchContent(File jobFile, String batchContent) throws IOException {
        try (Writer writer = new FileWriter( jobFile )) {
            writer.write( batchContent );
        }
    }

    /**
     * Pauses execution for the specified number of milliseconds.
     * 
     * @param millis the number of milliseconds to pause
     */
    private void pause(long millis) {
        try {
            Thread.sleep( millis );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
