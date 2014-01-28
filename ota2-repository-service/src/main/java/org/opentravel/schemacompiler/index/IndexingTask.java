package org.opentravel.schemacompiler.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Implements the logic required to index a single repository item for free-text searching.
 * 
 * @author S. Livezey
 */
public class IndexingTask extends AbstractFreeTextSearchTask {

    private static Log log = LogFactory.getLog(IndexingTask.class);

    private IndexWriter indexWriter;

    /**
     * Constructor that provides the index writer to use when publishing documents for free-text
     * searching, and the repository manager to use when accessing all repository content.
     * 
     * @param indexWriter
     *            the index writer to which all indexed documents are to be published
     * @param repositoryManager
     *            the repository that owns all content to be indexed
     */
    public IndexingTask(IndexWriter indexWriter, RepositoryManager repositoryManager) {
        super(repositoryManager);
        this.indexWriter = indexWriter;
    }

    /**
     * Constructs a new indexing task that will create or update the item's index and (optionally)
     * the index of its head version.
     * 
     * @param itemToIndex
     *            the item to be indexed
     * @param indexHeadVersion
     *            flag indicating whether the head version of the repository item should also be
     *            indexed
     * @return TaskInfo
     */
    public static TaskInfo newIndexingTask(RepositoryItem itemToIndex, boolean indexHeadVersion) {
        return new TaskInfo(itemToIndex, indexHeadVersion, false);
    }

    /**
     * Constructs a new indexing task that will delete the index for the given item.
     * 
     * @param itemToDelete
     *            the item whose index is to be deleted
     * @return TaskInfo
     */
    public static TaskInfo newDeleteIndexTask(RepositoryItem itemToDelete) {
        return new TaskInfo(itemToDelete, true, true);
    }

    /**
     * Creates an indexed document for the given repository item.
     * 
     * @param task
     *            the indexing task that specifies the repository item to be indexed
     */
    public void indexRepositoryItem(TaskInfo task) {
        try {
            RepositoryItem item = task.getItemToIndex();

            if (log.isInfoEnabled()) {
                log.info("Indexing repository item '" + item.getFilename() + "' ["
                        + item.getNamespace() + "].");
            }

            // Start by creating a standard index for the item that was submitted for indexing
            buildIndex(item, IndexVersionType.STANDARD, buildIndexingTempFile(task.itemToIndex));

            // Index the head version, if requested
            if (task.indexHeadVersion) {
                indexHeadVersion(item);
            }

        } catch (Throwable t) {
            log.warn("Error indexing repository item: " + t.getMessage(), t);
        }
    }

    /**
     * Removes the index for the specified repository item. If other versions of the item still
     * exist, the version chain will be re-indexed. If the item is the last existing version, the
     * index will be deleted outright.
     * 
     * @param task
     *            the indexing task that specifies the repository item to be indexed
     */
    public void deleteRepositoryItemIndex(TaskInfo task) {
        try {
            RepositoryItem item = task.getItemToIndex();

            if (log.isInfoEnabled()) {
                log.info("Deleting repository item index '" + item.getFilename() + "' ["
                        + item.getNamespace() + "].");
            }

            deleteIndex(item, IndexVersionType.STANDARD);
            indexHeadVersion(item);

        } catch (Throwable t) {
            log.warn("Error deleting repository item index: " + t.getMessage(), t);
        }
    }

    /**
     * Creates an index for the head version of the repository item. The head version index contains
     * the union of all minor and patch versions for the item. If no minor or patch version exist
     * for the item, any existing head version index is deleted.
     * 
     * @param item
     *            the item whose head version is to be indexed
     * @throws RepositoryException
     *             thrown if the items version history cannot be retrieved
     * @throws VersionSchemeException
     *             thrown if the item's version scheme is not recognized
     * @throws IOException
     *             thrown if the search index cannot be updated
     */
    private void indexHeadVersion(RepositoryItem item) throws RepositoryException,
            VersionSchemeException, IOException {
        // Collect the list of items for the head-version index that includes draft items
        List<RepositoryItem> headVersions = getHeadVersionsToIndex(item, true);

        if (!headVersions.isEmpty()) {
            buildIndex(headVersions.get(0), IndexVersionType.HEAD,
                    buildIndexingTempFile(headVersions));

        } else {
            deleteIndex(item, IndexVersionType.HEAD);
        }

        // Collect the list of items for the head-version index that includes only final items
        List<RepositoryItem> headFinalVersions = getHeadVersionsToIndex(item, false);

        if (!headFinalVersions.isEmpty()) {
            buildIndex(headFinalVersions.get(0), IndexVersionType.HEAD_FINAL,
                    buildIndexingTempFile(headFinalVersions));

        } else {
            deleteIndex(item, IndexVersionType.HEAD_FINAL);
        }
    }

    /**
     * Creates a Lucene index document using the information and file content provided. After the
     * indexing is complete, the temporary file content is deleted from the file system.
     * 
     * @param item
     *            the repository item to be indexed
     * @param versionType
     *            the type of version-index to be created
     * @param indexingTempFile
     *            the temporary file content to be indexed for searching
     * @throws IOException
     *             thrown if the index document cannot be created
     */
    private void buildIndex(RepositoryItem item, IndexVersionType versionType, File indexingTempFile)
            throws IOException {
        try {
            String indexIdentity = getIndexIdentity(item, versionType);
            Document indexDoc = new Document();
            TLLibraryStatus itemStatus;

            switch (versionType) {
                case HEAD:
                    itemStatus = TLLibraryStatus.DRAFT;
                    break;
                case HEAD_FINAL:
                    itemStatus = TLLibraryStatus.FINAL;
                    break;
                default:
                    itemStatus = item.getStatus();
            }
            indexDoc.add(new StringField(IDENTITY_FIELD, indexIdentity, Field.Store.YES));
            indexDoc.add(new StringField(BASE_NAMESPACE_FIELD, item.getBaseNamespace(),
                    Field.Store.YES));
            indexDoc.add(new StringField(FILENAME_FIELD, item.getFilename(), Field.Store.YES));
            indexDoc.add(new StringField(VERSION_FIELD, item.getVersion(), Field.Store.YES));
            indexDoc.add(new TextField(STATUS_FIELD, itemStatus.toString(), Field.Store.YES));
            indexDoc.add(new TextField(VERSION_TYPE_FIELD, versionType.toString(), Field.Store.YES));
            indexDoc.add(new TextField(CONTENT_FIELD, new BufferedReader(new FileReader(
                    indexingTempFile))));

            indexWriter.updateDocument(new Term(IDENTITY_FIELD, indexIdentity), indexDoc);
            indexWriter.commit();

        } finally {
            if (indexingTempFile != null)
                indexingTempFile.delete();
        }
    }

    /**
     * Deletes the given document from the index. If no matching document exists, this method takes
     * no action.
     * 
     * @param item
     *            the repository item to be deleted from the index
     * @param versionType
     *            the type of version-index to be deleted from the index
     */
    private void deleteIndex(RepositoryItem item, IndexVersionType versionType) {
        String indexIdentity = getIndexIdentity(item, versionType);
        try {
            indexWriter.deleteDocuments(new Term(IDENTITY_FIELD, indexIdentity));
            indexWriter.commit();

        } catch (Throwable t) {
            // Ignore error if no such document exists
        }
    }

    /**
     * Returns the string that is used to identify the item's document in the Lucene index.
     * 
     * @param item
     *            the repository item for which to return an identity string
     * @param versionType
     *            the type of version-index to be created for the repository item
     * @return String
     */
    private String getIndexIdentity(RepositoryItem item, IndexVersionType versionType) {
        StringBuilder identity = new StringBuilder();

        identity.append(item.getBaseNamespace()).append(':');
        identity.append(item.getLibraryName()).append(':');
        identity.append((versionType == IndexVersionType.STANDARD) ? item.getVersion()
                : versionType.toString());
        return identity.toString();
    }

    /**
     * Returns a list of the versions of the repository item that should be included in the HEAD (or
     * HEAD-FINAL) indexing document. If the given repository item is not a member of the latest
     * major version chain for the item, this method will return an empty list.
     * 
     * If the 'includeDraftVersions' flag is true, all versions will be included in the resulting
     * list of repository items. If false, all versions up to and including the latest final version
     * will be included.
     * 
     * @param item
     *            the item for which head versions should be returned
     * @param includeDraftVersions
     *            indicates whether draft versions are to be included in the indexed content
     * @return List<RepositoryItem>
     */
    private List<RepositoryItem> getHeadVersionsToIndex(RepositoryItem item,
            boolean includeDraftVersions) throws VersionSchemeException, RepositoryException {
        VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                item.getVersionScheme());
        String itemMajorVersion = versionScheme.getMajorVersion(item.getVersion());
        List<RepositoryItem> allVersions = repositoryManager.getVersionHistory(item);
        List<RepositoryItem> headVersions = new ArrayList<RepositoryItem>();

        if (!allVersions.isEmpty()) {
            String latestMajorVersion = versionScheme.getMajorVersion(allVersions.get(0)
                    .getVersion());

            // Only submit content for indexing if the version we are indexing is in the latest
            // major
            // version chain. If it is not, then the HEAD (or HEAD-FINAL) version indexing could not
            // have
            // been affected by an update to the original repository item.
            if (itemMajorVersion.equals(latestMajorVersion)) {
                boolean canIndex = includeDraftVersions;

                for (RepositoryItem itemVersion : allVersions) {
                    String currentMajorVersion = versionScheme.getMajorVersion(itemVersion
                            .getVersion());

                    // If we are not including draft items, skip over each version
                    canIndex |= (itemVersion.getStatus() == TLLibraryStatus.FINAL);

                    // If we have gone back in the history past the current major version, we can
                    // stop gathering
                    // content for the indexing task
                    if (!currentMajorVersion.equals(itemMajorVersion)) {
                        break;
                    }

                    if (canIndex) {
                        headVersions.add(itemVersion);
                    }
                }
            }
        }
        return headVersions;
    }

    /**
     * Returns a temporary file that includes only the content of the given repository items.
     * 
     * @param itemToIndex
     *            the repository item whose content is to be indexed
     * @return File
     */
    private File buildIndexingTempFile(RepositoryItem itemToIndex) throws RepositoryException,
            IOException {
        return buildIndexingTempFile(Arrays.asList(new RepositoryItem[] { itemToIndex }));
    }

    /**
     * Returns a temporary file that includes all content for each of the given repository items.
     * 
     * @param itemsToIndex
     *            the list of repository items whose content is to be included in the indexed
     *            document
     * @return File
     * @throws RepositoryException
     *             thrown if the content file for one or more of the repository items cannot be
     *             identified
     * @throws IOException
     *             thrown if the temporary file cannot be created or written to
     */
    private File buildIndexingTempFile(List<RepositoryItem> itemsToIndex)
            throws RepositoryException, IOException {
        File indexingFile = File.createTempFile("ota", ".idx");
        OutputStream out = null;
        try {
            try {
                out = new FileOutputStream(indexingFile);
                byte[] buffer = new byte[1024];

                for (RepositoryItem item : itemsToIndex) {
                    InputStream is = null;
                    try {
                        is = new FileInputStream(getContentFile(item));
                        int bytesRead;

                        while ((bytesRead = is.read(buffer)) >= 0) {
                            out.write(buffer, 0, bytesRead);
                        }
                    } finally {
                        try {
                            if (is != null)
                                is.close();
                        } catch (Throwable t) {
                        }
                    }
                }

            } catch (IOException e) {
                throw new RepositoryException("Error creating indexing file.", e);
            }

        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Throwable t) {
            }
        }
        return indexingFile;
    }

    /**
     * Encapsulates all information required to perform a single indexing task.
     */
    public static class TaskInfo {

        private RepositoryItem itemToIndex;
        private boolean indexHeadVersion;
        private boolean deleteIndex;

        /**
         * Constructor that specifies the item to be indexed, and also indicates whether the head
         * version of this item will also be indexed.
         * 
         * @param itemToIndex
         *            the item to be indexed
         * @param indexHeadVersion
         *            flag indicating whether the head version of the repository item should also be
         *            indexed
         * @param deleteIndex
         *            flag indicating that the item's index should be deleted
         */
        private TaskInfo(RepositoryItem itemToIndex, boolean indexHeadVersion, boolean deleteIndex) {
            this.itemToIndex = itemToIndex;
            this.indexHeadVersion = indexHeadVersion;
            this.deleteIndex = deleteIndex;
        }

        /**
         * Returns the item to be indexed.
         * 
         * @return RepositoryItem
         */
        public RepositoryItem getItemToIndex() {
            return itemToIndex;
        }

        /**
         * Returns the flag value indicating whether the head version of the repository item should
         * also be indexed.
         * 
         * @return boolean
         */
        public boolean isIndexHeadVersion() {
            return indexHeadVersion;
        }

        /**
         * Returns the flag value indicating whether the item's index should be deleted.
         * 
         * @return boolean
         */
        public boolean isDeleteIndex() {
            return deleteIndex;
        }

    }

}
