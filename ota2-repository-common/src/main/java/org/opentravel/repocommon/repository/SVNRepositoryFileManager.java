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

package org.opentravel.repocommon.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.repocommon.jmx.OTMRepositoryStats;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.util.FileUtils;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCommitPacket;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Uses a Subversion (SVN) vault to maintain a persistent copy of all files in the repository. Each time a change set is
 * committed by a remote user, its files are committed to the Subversion repository. Likewise, all of the files in
 * change sets that are rolled back are reverted to their previous state.
 * 
 * @author S. Livezey
 */
public class SVNRepositoryFileManager extends RepositoryFileManager {

    private static final String INITIALIZATION_COMMIT_MESSAGE =
        "New content detected during repository initialization.";
    private static final String USER_COMMIT_MESSAGE = "Committed by repository user: {0}.";

    private static Object svnCommitLock = new Object();
    private static Logger log = LogManager.getLogger( SVNRepositoryFileManager.class );

    private ThreadLocal<Set<File>> svnChangeSet = ThreadLocal.withInitial( HashSet::new );

    private SVNClientManager svnClient;

    /**
     * Constructor that initializes the OTA2.0 repository folder for use with the Subversion client using default
     * credentials from the SVN configuration directory.
     * 
     * @param repositoryLocation the root directory of the OTA2.0 repository
     * @param svnConfigDirectory the current user's SVN configuration directory (null for default '~/.subversion')
     * @throws RepositoryException thrown if the SVN client cannot be initialized for the specified repository location
     */
    public SVNRepositoryFileManager(File repositoryLocation, File svnConfigDirectory) throws RepositoryException {
        this( repositoryLocation, svnConfigDirectory, null );
    }

    /**
     * Constructor that initializes the OTA2.0 repository folder for use with the Subversion client. If an
     * 'svnCredentials' file is provided, a properties file is expected that specifies the credentials of the SVN user's
     * account. The user ID is specified with the property key 'svn.userid', and the password is specified with
     * 'svn.password'. If the 'svnCredentialsFile' parameter is null, authentication will be performed using the last
     * known repository credentials found in the SVN configuration directory.
     * 
     * @param repositoryLocation the root directory of the OTA2.0 repository
     * @param svnConfigDirectory the current user's SVN configuration directory (null for default '~/.subversion')
     * @param svnCredentialsFile location of the properties file that specifies the SVN credentials (may be null)
     * @throws RepositoryException thrown if the SVN client cannot be initialized for the specified repository location
     */
    public SVNRepositoryFileManager(File repositoryLocation, File svnConfigDirectory, File svnCredentialsFile)
        throws RepositoryException {
        super( repositoryLocation );
        initializeSVNRepository( repositoryLocation, svnConfigDirectory, svnCredentialsFile );
    }

    /**
     * @see org.opentravel.repocommon.repository.RepositoryFileManager#commitChangeSet(java.util.Set)
     */
    @Override
    protected void commitChangeSet(Set<File> changeSet) throws RepositoryException {
        synchronized (svnCommitLock) {
            try {
                // Add any unmanaged files to SVN version control before committing
                File[] svnFiles = buildSvnChangeSet( changeSet );

                // Commit the changes from the change set
                if (svnFiles.length > 0) {
                    SVNCommitClient commitClient = svnClient.getCommitClient();
                    SVNCommitPacket commitPacket =
                        commitClient.doCollectCommitItems( svnFiles, false, false, SVNDepth.INFINITY, null );

                    if (commitPacket.getCommitItems().length > 0) {
                        String userId = getCurrentUserId();

                        commitClient.doCommit( commitPacket, false,
                            MessageFormat.format( USER_COMMIT_MESSAGE, (userId == null) ? "Unknown" : userId ) );
                    }
                }
            } catch (SVNException e) {
                throw new RepositoryException( "SVN error while committing change set: " + e.getMessage(), e );
            }
        }
    }

    /**
     * @see org.opentravel.repocommon.repository.RepositoryFileManager#rollbackChangeSet(java.util.Set)
     */
    @Override
    protected void rollbackChangeSet(Set<File> changeSet) throws RepositoryException {
        synchronized (svnCommitLock) {
            try {
                SVNWCClient wcClient = svnClient.getWCClient();
                File[] svnFiles = buildSvnChangeSet( changeSet );

                // Use SVN to revert any files that were deleted or modified files
                wcClient.doRevert( svnFiles, SVNDepth.IMMEDIATES, null );

                // Rebuild the change set; any files remaining after the revert must be new files
                // that need to be removed from the file system.
                svnFiles = buildSvnChangeSet( changeSet );

                for (File file : svnFiles) {
                    FileUtils.delete( file );
                }

            } catch (SVNException e) {
                throw new RepositoryException( "SVN error while rolling back change set: " + e.getMessage(), e );
            }
        }
    }

    /**
     * Initializes the OTA2.0 repository file system for use with the Subversion client. Prior to returning, this method
     * updates the contents of the entire repository from the SVN server. After the update is complete, the contents of
     * the repository are analyzed to determine if any unmanaged files exist. If so, they are immediately committed to
     * the remote SVN server.
     * 
     * @param repositoryLocation the root directory of the OTA2.0 repository
     * @param svnConfigDirectory the current user's SVN configuration directory (null for default '~/.subversion')
     * @param svnCredentialsFile location of the properties file that specifies the SVN credentials (may be null)
     * @throws RepositoryException thrown if the SVN client cannot be initialized for the specified repository location
     */
    protected void initializeSVNRepository(File repositoryLocation, File svnConfigDirectory, File svnCredentialsFile)
        throws RepositoryException {
        log.info( "Initializing OTA2.0 repository for use with SVN: " + repositoryLocation.getAbsolutePath() );
        try {
            svnClient = newSVNClientManager( svnConfigDirectory, svnCredentialsFile );

            // Search for any new, modified, or deleted files on the local file system and commit
            // them to the remote repository
            File[] svnChgSet = buildSvnChangeSet( getRepositoryLocation() );

            if (svnChgSet.length > 0) {
                SVNCommitClient commitClient = svnClient.getCommitClient();
                SVNCommitPacket commitPacket =
                    commitClient.doCollectCommitItems( svnChgSet, false, false, SVNDepth.INFINITY, null );

                if (commitPacket.getCommitItems().length > 0) {
                    commitClient.doCommit( commitPacket, false, INITIALIZATION_COMMIT_MESSAGE );
                }
            }

            // Update with the latest content from the repository
            svnClient.getUpdateClient().doUpdate( repositoryLocation, SVNRevision.HEAD, SVNDepth.INFINITY, true,
                false );

            log.info( "Done with OTA2.0 repository initialization.: " + repositoryLocation.getAbsolutePath() );
            OTMRepositoryStats.getInstance().setSvnServiceAvailable( true );

        } catch (SVNException e) {
            OTMRepositoryStats.getInstance().setSvnUserConfigOk( false );
            throw new RepositoryException( "Error during repository file system initialization: " + e.getMessage(), e );
        }
    }

    /**
     * Constructs the SVN client managerusing the information provided. If an 'svnCredentials' file is provided, a
     * properties file is expected that specifies the credentials of the SVN user's account. The user ID is specified
     * with the property key 'svn.userid', and the password is specified with 'svn.password'. If the
     * 'svnCredentialsFile' parameter is null, authentication will be performed using the last known repository
     * credentials found in the SVN configuration directory.
     * 
     * @param svnConfigDirectory the current user's SVN configuration directory (null for default '~/.subversion')
     * @param svnCredentialsFile location of the properties file that specifies the SVN credentials (may be null)
     * @return SVNClientManager
     */
    protected SVNClientManager newSVNClientManager(File svnConfigDirectory, File svnCredentialsFile) {
        SVNClientManager client;
        String userId = null;
        String password = null;

        // Attempt to load the SVN credentials from the property file (if one was specified)
        if (svnCredentialsFile != null) {
            if (svnCredentialsFile.exists()) {
                try (InputStream is = new FileInputStream( svnCredentialsFile )) {
                    Properties credentialsProps = new Properties();

                    credentialsProps.load( is );
                    userId = credentialsProps.getProperty( "svn.userid" );
                    password = credentialsProps.getProperty( "svn.password" );
                    OTMRepositoryStats.getInstance().setSvnUserConfigOk( true );

                } catch (IOException e) {
                    log.warn( "SVN credentials file unreadable (using default credentials): "
                        + svnCredentialsFile.getAbsolutePath() );
                    OTMRepositoryStats.getInstance().setSvnUserConfigOk( false );
                }

            } else {
                log.warn( "SVN credentials file not found (using default credentials): "
                    + svnCredentialsFile.getAbsolutePath() );
                OTMRepositoryStats.getInstance().setSvnUserConfigOk( false );
            }
        }

        // Construct the SVN client manager instance
        if (svnConfigDirectory != null) {
            if ((userId != null) || (password != null)) {
                client = SVNClientManager.newInstance( SVNWCUtil.createDefaultOptions( svnConfigDirectory, true ),
                    userId, password );
            } else {
                client = SVNClientManager.newInstance( SVNWCUtil.createDefaultOptions( svnConfigDirectory, true ) );
            }
        } else {
            client = SVNClientManager.newInstance();
        }
        client.setEventHandler( new RepositorySVNEventHandler() );
        return client;
    }

    /**
     * Scans the specified folder structure to construct a change set that can be committed (or rolled back) by the
     * SVNKit client API's. Only newly-created, modified, and deleted files will be included in the change set.
     * 
     * @param rootFolderLocation the root folder of the directory structure to scan for changes
     * @return File[]
     * @throws SVNException thrown if an error occurs while creating the change set
     */
    private File[] buildSvnChangeSet(File rootFolderLocation) throws SVNException {
        Set<File> threadLocalChangeSet = svnChangeSet.get();

        threadLocalChangeSet.clear();

        try {
            if (rootFolderLocation.exists()) {
                SVNStatusClient c = svnClient.getStatusClient();

                c.doStatus( rootFolderLocation, SVNRevision.HEAD, SVNDepth.INFINITY, false, true, false, false,
                    new RepositorySVNStatusHandler(), null );
            }
        } catch (SVNException e) {
            log.error( "Error constructing SVN change set for all repository content.", e );
        }
        return threadLocalChangeSet.toArray( new File[threadLocalChangeSet.size()] );
    }

    /**
     * Scans the given set of files to construct a change set that can be committed by the <code>SVNCommitClient</code>.
     * If any of the given files do not exist or their content has not been modified, they will be omitted from the SVN
     * change set.
     * 
     * @param changeSet the repository change set to process
     * @return File[]
     * @throws SVNException thrown if an error occurs while creating the change set
     */
    private File[] buildSvnChangeSet(Set<File> changeSet) throws SVNException {
        SVNStatusClient statusClient = svnClient.getStatusClient();
        ISVNStatusHandler statusHandler = new RepositorySVNStatusHandler();
        SVNWCClient wcClient = svnClient.getWCClient();
        Set<File> threadLocalChangeSet = svnChangeSet.get();
        List<File> changeSetList = new ArrayList<>( changeSet );
        String baseFolder = getRepositoryLocation().getAbsolutePath();

        changeSetList.sort( new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getAbsolutePath().compareTo( f2.getAbsolutePath() );
            }
        } );
        threadLocalChangeSet.clear();
        log.info( "BUILDING SVN CHANGE SET:" );

        for (File file : changeSetList) {
            File folder = file.getParentFile();

            // For new/modified files, make sure that all parent folders are also under version control
            if (file.exists()) {
                while ((folder != null) && !folder.getAbsolutePath().equals( baseFolder )) {
                    try {
                        statusClient.doStatus( folder, SVNRevision.HEAD, SVNDepth.EMPTY, false, true, false, false,
                            statusHandler, null );
                        break; // If the parent folder is already under version control, there is no need to continue

                    } catch (SVNException e) {
                        handleStatusException( e, file, wcClient );
                    }
                    folder = folder.getParentFile();
                }
            }

            // Add the new/modified file to the list of committed items
            try {
                log.info( "  " + file.getAbsolutePath() );
                statusClient.doStatus( file, SVNRevision.HEAD, SVNDepth.INFINITY, false, true, false, false,
                    statusHandler, null );

            } catch (SVNException e) {
                handleStatusException( e, file, wcClient );
            }
        }
        return threadLocalChangeSet.toArray( new File[threadLocalChangeSet.size()] );
    }

    /**
     * Handles an SVN status exception by checking to see if it was thrown because a file or directory was not yet under
     * version control. In those cases, the working copy client is used to add it to the list of items that will be
     * committed.
     * 
     * @param e the SVN status exception that was thrown
     * @param file the file (or directory) for which the exception was thrown
     * @param wcClient the SVNKit working copy client
     * @throws SVNException thrown if an error occurs while adding the item to the list of changes to commit
     */
    private void handleStatusException(SVNException e, File file, SVNWCClient wcClient) throws SVNException {
        SVNErrorCode errorCode = e.getErrorMessage().getErrorCode();

        if ((errorCode == SVNErrorCode.WC_NOT_DIRECTORY) || (errorCode == SVNErrorCode.WC_NOT_FILE)
            || (errorCode == SVNErrorCode.WC_PATH_NOT_FOUND)) {
            wcClient.doAdd( file, false, false, false, SVNDepth.FILES, false, true );
        }
    }

    /**
     * Event handler for processing status checks on repository files to determine which items have been added,
     * modified, and deleted on the local working copy. Any changes are added to the thread local 'svnChangeSet'
     * instance.
     */
    private class RepositorySVNStatusHandler implements ISVNStatusHandler {

        /**
         * @see org.tmatesoft.svn.core.wc.ISVNStatusHandler#handleStatus(org.tmatesoft.svn.core.wc.SVNStatus)
         */
        @Override
        public void handleStatus(SVNStatus status) throws SVNException {
            File file = status.getFile();

            if (file.isDirectory() && (status.getCommittedRevision() == SVNRevision.UNDEFINED)) {
                log.info( "Adding Directory to SVN Change Set: " + file.getAbsolutePath() );
                svnChangeSet.get().add( file );

            } else if (!file.exists()) {
                // File will be deleted from the change set by the
                // RepositorySVNEventHandler.handleEvent() callback
                svnClient.getWCClient().doDelete( file, true, false, false );

            } else if (status.getContentsStatus() == SVNStatusType.STATUS_NORMAL) {
                // No action required

            } else if ((status.getContentsStatus() == SVNStatusType.CHANGED)
                || (status.getContentsStatus() == SVNStatusType.STATUS_MODIFIED)) {
                log.info( "Adding Modified File to SVN Change Set: " + file.getAbsolutePath() );
                svnChangeSet.get().add( file );

            } else if (status.getNodeStatus() == SVNStatusType.STATUS_ADDED) {
                log.info( "Adding New File to SVN Change Set: " + file.getAbsolutePath() );
                svnChangeSet.get().add( file );

            } else if ((status.getContentsStatus() == SVNStatusType.STATUS_UNVERSIONED)
                || (status.getContentsStatus() == SVNStatusType.STATUS_NONE)) {
                // File will be added to the change set by the RepositorySVNEventHandler.handleEvent() callback
                svnClient.getWCClient().doAdd( file, false, false, false, SVNDepth.FILES, false, true );

            } else {
                log.error( "Unrecognized SVN status '" + status.getContentsStatus() + "' for file '" + file.getName()
                    + "' - Unable to commit." );
            }
        }

    }

    /**
     * Logs the individual actions performed by the SVN client, and perform specialized processing for unversioned (new)
     * files that are encountered during processing.
     */
    private class RepositorySVNEventHandler implements ISVNEventHandler {

        private Map<SVNEventAction,String> eventDisplayLabels = new HashMap<>();

        /**
         * @see org.tmatesoft.svn.core.wc.ISVNEventHandler#handleEvent(org.tmatesoft.svn.core.wc.SVNEvent, double)
         */
        @Override
        public void handleEvent(SVNEvent event, double progress) throws SVNException {
            // Debug logging for the event...
            if (log.isDebugEnabled()) {
                StringBuilder eventMessage = new StringBuilder( getDisplayLabel( event.getAction() ) );

                if (event.getFile() != null) {
                    eventMessage.append( ": " ).append( event.getFile().getAbsolutePath() );

                } else if (event.getURL() != null) {
                    eventMessage.append( ": " ).append( event.getURL().toString() );
                }
                log.debug( eventMessage.toString() );
            }

            // If this is an Add event, add the file to the thread-local change set
            if (event.getAction() == SVNEventAction.ADD) {
                if (log.isInfoEnabled()) {
                    log.info( "Adding New File to SVN Change Set: " + event.getFile().getAbsolutePath() );
                }
                svnChangeSet.get().add( event.getFile() );

                // If this is a folder, sub-folders will not be added automatically so we have to
                // explicitly add each subfolder and its contents to the change set.
                if (event.getFile().isDirectory()) {
                    for (File folderItem : event.getFile().listFiles()) {
                        if (folderItem.isDirectory()) {
                            addAllToChangeSet( folderItem );
                        }
                    }
                }
            }

            if (event.getAction() == SVNEventAction.DELETE) {
                if (log.isInfoEnabled()) {
                    log.info( "Adding Deleted File to SVN Change Set: " + event.getFile().getAbsolutePath() );
                }
                svnChangeSet.get().add( event.getFile() );
            }
        }

        /**
         * Adds the given folder and all of its contents (recursively) to the current change set.
         * 
         * @param fileOrFolder the file or folder to add to the current change set
         */
        private void addAllToChangeSet(File fileOrFolder) {
            if (log.isInfoEnabled()) {
                log.info( "Adding New File to SVN Change Set: " + fileOrFolder.getAbsolutePath() );
            }
            svnChangeSet.get().add( fileOrFolder );

            if (fileOrFolder.isDirectory()) {
                for (File folderItem : fileOrFolder.listFiles()) {
                    addAllToChangeSet( folderItem );
                }
            }
        }

        /**
         * Returns a user-displayable label for the given event type.
         * 
         * @param eventType the SVN event type
         * @return String
         */
        private String getDisplayLabel(SVNEventAction eventType) {
            String label = eventDisplayLabels.get( eventType );

            if (label == null) {
                String[] words = eventType.toString().split( "_" );
                StringBuilder lbl = new StringBuilder();

                for (String word : words) {
                    if (word.equals( "none" )) {
                        continue;
                    }
                    String camelCaseWord;

                    if (word.length() > 1) {
                        camelCaseWord = word.substring( 0, 1 ).toUpperCase() + word.substring( 1 );
                    } else {
                        camelCaseWord = word.toUpperCase();
                    }
                    if (lbl.length() > 0) {
                        lbl.append( ' ' );
                    }
                    lbl.append( camelCaseWord );
                }
                label = lbl.toString();
            }
            return label;
        }

        /**
         * @see org.tmatesoft.svn.core.ISVNCanceller#checkCancelled()
         */
        @Override
        public void checkCancelled() throws SVNCancelException {
            // No action required
        }

    }

}
