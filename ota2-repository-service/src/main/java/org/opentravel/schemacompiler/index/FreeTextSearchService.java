
package org.opentravel.schemacompiler.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.opentravel.schemacompiler.index.IndexingTask.TaskInfo;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Repository service that uses Apache Lucene for indexing of reposited artifacts
 * and free-text searching.
 * 
 * @author S. Livezey
 */
public class FreeTextSearchService {
	
	private static Log log = LogFactory.getLog(FreeTextSearchService.class);
	
	private static FreeTextSearchService defaultInstance;
	private static Set<Object> serviceOwners = new HashSet<Object>();
	
	private Thread indexingThread;
	private List<TaskInfo> indexingQueue = Collections.synchronizedList( new ArrayList<TaskInfo>() );
	private boolean shutdownRequested = false;
	private boolean isRunning = false;
	
	private File indexLocation;
	private RepositoryManager repositoryManager;
	private Directory indexDirectory;
	private IndexWriterConfig writerConfig;
	private IndexWriter indexWriter;
	private SearcherManager searchManager;
	private IndexingTask indexingTask;
	private QueryTask queryTask;
	private boolean realTimeIndexing = false;
	
	
	/**
	 * Private constructor that specifies the folder location of the index and the repository manager
	 * used to access the content to be indexed and searched for.
	 * 
	 * <p>NOTE: This constructor is typically used for testing purposes.  When utilizing the service
	 * in a shared container environment, the singleton accessor methods should be used to obtain a
	 * running instance of the service.
	 * 
	 * @param indexLocation  the folder location of the index directory
	 * @param repositoryManager  the repository that owns all content to be indexed
	 * @throws IOException  thrown if a low-level error occurs while initializing the search index
	 */
	public FreeTextSearchService(File indexLocation, RepositoryManager repositoryManager) throws IOException {
		if (!indexLocation.exists()) {
			indexLocation.mkdirs();
		}
		this.writerConfig = new IndexWriterConfig( Version.LUCENE_41, new StandardAnalyzer(Version.LUCENE_41) );
		this.writerConfig.setOpenMode( OpenMode.CREATE_OR_APPEND );
		this.repositoryManager = repositoryManager;
		this.indexLocation = indexLocation;
		this.realTimeIndexing = System.getProperty("ota2.repository.realTimeIndexing", "false").equalsIgnoreCase("true");
	}
	
	/**
	 * Returns the singleton instance of the service.  This method only returns a service instance
	 * after the initializeInstance() has been called successfully.
	 * 
	 * @return FreeTextSearchService
	 */
	public static FreeTextSearchService getInstance() {
		return defaultInstance;
	}
	
	/**
	 * Constructs a singleton instance of the service and starts it using the information provided.  If
	 * a singleton instance has already been initialized, this method takes no action.
	 * 
	 * @param indexLocation  the folder location of the index directory
	 * @param repositoryManager  the repository that owns all content to be indexed
	 * @throws IOException  thrown if a low-level error occurs while initializing the search index
	 */
	public static synchronized void initializeSingleton(File indexLocation, RepositoryManager repositoryManager) throws IOException {
		if (defaultInstance == null) {
			defaultInstance = new FreeTextSearchService( indexLocation, repositoryManager );
			defaultInstance.startService();
		}
	}
	
	/**
	 * Shuts down the running service and nulls the singleton if two conditions are met.  First, a singleton
	 * must already exist.  Second, the collection of service owners must be empty.  If either of these two
	 * conditions is not met, this method will take no action.
	 * 
	 * @throws IOException  thrown if a low-level error occurs while closing the service's index reader or writer
	 */
	public static synchronized void destroySingleton() throws IOException {
		if ((defaultInstance != null) && serviceOwners.isEmpty()) {
			try {
				defaultInstance.stopService();
				
			} finally {
				defaultInstance = null;
			}
		}
	}
	
	/**
	 * Registers the given component as an owner of the singleton instance of this service.
	 * 
	 * <p>NOTE: The singleton instance of the service DOES NOT have to be initialized to register
	 * a service owner. 
	 * 
	 * @param owner  the component to be registered as a service owner
	 */
	public static synchronized void registerServiceOwner(Object owner) {
		if (owner != null) {
			serviceOwners.add( owner );
		}
	}
	
	/**
	 * Un-registers the given component as an owner of the singleton instance of this service.
	 * 
	 * @param owner  the component to be removed from the list of registered service owners
	 */
	public static synchronized void unregisterServiceOwner(Object owner) {
		serviceOwners.remove( owner );
	}
	
	/**
	 * Starts the indexing service.
	 * 
	 * @throws IllegalStateException  thrown if the service is already running
	 * @throws IOException  thrown if a low-level error occurs while initializing the index reader or writer
	 */
	public synchronized void startService() throws IOException {
		if ((indexingThread != null) && indexingThread.isAlive()) {
			throw new IllegalStateException("Unable to start - the indexing service is already running.");
		}
		
		// Check to make sure the index was properly closed, and release any lock that might exist
		this.indexDirectory = FSDirectory.open( indexLocation );
		
		if (IndexWriter.isLocked(indexDirectory)) {
			log.warn("The search index was not properly shut down - releasing lock.");
			IndexWriter.unlock(indexDirectory);
		}
		
		// Configure the indexing and search components
		this.indexWriter = new IndexWriter( indexDirectory, writerConfig );
		this.searchManager = new SearcherManager( indexWriter, true, new SearcherFactory() );
		this.indexingTask = new IndexingTask( indexWriter, repositoryManager );
		this.queryTask = new QueryTask( searchManager, repositoryManager );
		
		// Start the background thread used to index repository items
		shutdownRequested = false;
		isRunning = false;
		indexingThread = new Thread( new IndexRunner() );
		indexingThread.start();
		
		// Wait for the service to start up before returning
		while (!isRunning) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * Stops the indexing service.  This method halts processing of the indexing tasks that have
	 * been submitted to this service, but does not clear the queue of pending tasks.
	 * 
	 * @throws IllegalStateException  thrown if the service is already running
	 * @throws IOException  thrown if a low-level error occurs while closing the index reader or writer
	 */
	public synchronized void stopService() throws IOException {
		if ((indexingThread == null) || !indexingThread.isAlive()) {
			throw new IllegalStateException("Unable to stop - the indexing service is not currently running.");
		}
		
		// Shut down the indexing thread
		shutdownRequested = true;
		indexingThread.interrupt();
		
		// Wait for the service to shut down before returning
		while (isRunning) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {}
		}
		indexingThread = null;
		
		// Close all of the indexing resources that were allocated during service startup
		try {
			searchManager.close();
			indexWriter.close();
			indexDirectory.clearLock( indexDirectory.getLockID() );
			indexDirectory.close();
			
		} finally {
			searchManager = null;
			indexWriter = null;
			indexDirectory = null;
			indexingTask = null;
			queryTask = null;
		}
		isRunning = false;
	}
	
	/**
	 * Returns true if the service is running.
	 * 
	 * @return boolean
	 */
	public boolean isRunning() {
		return isRunning;
	}
	
	/**
	 * Re-indexes (or indexes for the first time) all items in the repository.
	 * 
	 * @throws RepositoryException  thrown if an error occurs while retrieving content from the repository
	 */
	public void indexAllRepositoryItems() throws RepositoryException {
		if (indexingTask == null) {
			throw new IllegalStateException("Unable to perform indexing task - the service is not currently running.");
		}
		
		// First, clear the contents of the entire index (synchronize on the indexing queue so no
		// indexing jobs can proceed during the deletion process)
		synchronized (indexingQueue) {
			try {
				indexWriter.deleteAll();
				
			} catch (IOException e) {
				throw new RepositoryException("Error clearing existing content from search index.", e);
			}
		}
		
		// Retrieve and index all items in the repository
		for (String baseNamespace : repositoryManager.listBaseNamespaces()) {
			Map<String,List<RepositoryItem>> libraryVersionMap = new HashMap<String,List<RepositoryItem>>();
			
			// Start by separating the contents of the base namespace into lists of versions for each library
			// that was published to that base namespace
			for (RepositoryItem item : repositoryManager.listItems(baseNamespace, false, true)) {
				List<RepositoryItem> libraryVersions = libraryVersionMap.get( item.getLibraryName() );
				
				if (libraryVersions == null) {
					libraryVersions = new ArrayList<RepositoryItem>();
					libraryVersionMap.put( item.getLibraryName(), libraryVersions );
				}
				libraryVersions.add( item );
			}
			
			// For each library, publish all of the versions and the HEAD/HEAD_FINAL versions
			for (List<RepositoryItem> versionList : libraryVersionMap.values()) {
				if (!versionList.isEmpty()) {
					boolean firstItem = true;
					
					for (RepositoryItem item : versionList) {
						indexRepositoryItem( item, firstItem );
						firstItem = false;
					}
				}
				
			}
		}
	}
	
	/**
	 * Submits the given repository item for indexing.  The service does not need to be
	 * running for this call to succeed.
	 * 
	 * @param item  the repository item to be indexed
	 */
	public void indexRepositoryItem(RepositoryItem item) {
		indexRepositoryItem( item, true );
	}
	
	/**
	 * Submits the given repository item for indexing.  The service does not need to be
	 * running for this call to succeed.
	 * 
	 * @param item  the repository item to be indexed
	 * @param indexHeadVersion  flag indicating whether the head version of the repository item should also be indexed
	 */
	private void indexRepositoryItem(RepositoryItem item, boolean indexHeadVersion) {
		TaskInfo indexingJob = IndexingTask.newIndexingTask( item, indexHeadVersion );
		
		if (!realTimeIndexing) {
			synchronized (indexingQueue) {
				indexingQueue.add( indexingJob );
				indexingQueue.notify();
			}
		} else {
			indexingTask.indexRepositoryItem( indexingJob );
		}
	}
	
	/**
	 * Removes the index for the specified repository item.  If other versions of the item still exist,
	 * the version chain will be re-indexed.  If the item is the last existing version, the index will
	 * be deleted outright.
	 * 
	 * @param item  the repository item whose index is to be deleted
	 */
	public void deleteRepositoryItemIndex(RepositoryItem item) {
		TaskInfo indexingJob = IndexingTask.newDeleteIndexTask( item );
		
		if (!realTimeIndexing) {
			synchronized (indexingQueue) {
				indexingQueue.add( indexingJob );
				indexingQueue.notify();
			}
		} else {
			indexingTask.deleteRepositoryItemIndex( indexingJob );
		}
	}
	
	/**
	 * Clears all indexing tasks from the queue.  The service does not need to be running
	 * for this call to succeed.
	 */
	public void clearIndexingTasks() {
		synchronized (indexingQueue) {
			indexingQueue.clear();
		}
	}
	
	/**
	 * Returns true if indexing tasks are still pending.
	 * 
	 * @return boolean
	 */
	public boolean isPendingIndexingTasks() {
		synchronized (indexingQueue) {
			return !indexingQueue.isEmpty();
		}
	}
	
	/**
	 * Performs a search of the repository and returns all matching results.
	 * 
	 * @param queryString  the query string for the search
	 * @param latestVersionsOnly  flag indicating whether the results should include all matching versions or just the latest version of each library
	 * @param includeDraftVersions  flag indicating whether items in <code>DRAFT</code> status should be included in the resulting list
	 * @throws RepositoryException  thrown if an error occurs while executing the search
	 */
	public List<RepositoryItem> query(String queryString, boolean latestVersionsOnly,
			boolean includeDraftVersions) throws RepositoryException {
		return queryTask.queryRepositoryItems( queryString, latestVersionsOnly, includeDraftVersions );
	}
	
	/**
	 * Waits for indexing tasks to be submitted and then processes those tasks until the
	 * queue is empty.
	 */
	private class IndexRunner implements Runnable {

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			log.info("Repository indexing service started.");
			isRunning = true;
			
			while (!shutdownRequested) {
				try {
					TaskInfo task = null;
					
					// Get the next indexing task from the queue
					synchronized (indexingQueue) {
						if (!shutdownRequested && !indexingQueue.isEmpty()) {
							task = indexingQueue.remove(0);
						}
					}
					
					// Perform the indexing (monitor lock is released during this time)
					if (task != null) {
						if (task.isDeleteIndex()) {
							indexingTask.deleteRepositoryItemIndex( task );
						} else {
							indexingTask.indexRepositoryItem( task );
						}
					}
					
					// If the queue is empty, wait for the next task to be submitted
					if (!shutdownRequested && indexingQueue.isEmpty()) {
						synchronized (indexingQueue) {
							indexingQueue.wait();
						}
					}
					
				} catch (InterruptedException e) {
					// No action - continue looping
					
				} catch (Throwable t) {
					log.warn("Indexing Service Error: " + t.getMessage(), t);
				}
				synchronized (indexingQueue) {
				}
			}
			isRunning = false;
			log.info("Repository indexing service shut down.");
		}
		
	}
	
	/**
	 * Returns true if real-time indexing has been enabled.  By default, this
	 * value is false to enable background processing of indexing tasks.
	 * 
	 * <p>NOTE: Real-time indexing is intended for testing purposes only, and should
	 * not be utilized for production repository deployments.
	 *
	 * @return boolean
	 */
	public boolean isRealTimeIndexing() {
		return realTimeIndexing;
	}

	/**
	 * Assigns the flag value that indicates whether real-time indexing has been
	 * enabled.  By default, this value is false to enable background processing
	 * of indexing tasks.
	 * 
	 * <p>NOTE: Real-time indexing is intended for testing purposes only, and should
	 * not be utilized for production repository deployments.
	 *
	 * @param realTimeIndexing  the flag value to assign
	 */
	public void setRealTimeIndexing(boolean realTimeIndexing) {
		this.realTimeIndexing = realTimeIndexing;
	}

}
