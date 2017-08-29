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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryHistoryType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RefreshPolicy;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RemoteRepositoriesType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RemoteRepositoryType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryState;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.loader.impl.MultiVersionLibraryModuleLoader;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.impl.DefaultRepositoryFileManager;
import org.opentravel.schemacompiler.repository.impl.ProjectFileUtils;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemVersionedWrapper;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.saver.impl.Library15FileSaveHandler;
import org.opentravel.schemacompiler.saver.impl.Library16FileSaveHandler;
import org.opentravel.schemacompiler.security.PasswordHelper;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.xml.XMLGregorianCalendarConverter;

/**
 * Manager that coordinates the actions, lookups, and relationships between OTA2.0 repositories and
 * the items that are managed within those repositories.
 * 
 * @author S. Livezey
 */
public class RepositoryManager implements Repository {

    private static final String CURRENT_USER_BASE_NAMESPACE = "http://opentravel.org/local/";
    private static final String ENCRYPTED_PASSWORD_PREFIX = "enc:";
    private static final int MAX_DISPLAY_NAME_LENGTH = 256;
    private static final Pattern REPOSITORY_ID_PATTERN = Pattern.compile("([A-Za-z0-9\\-._~!$&'()*+,;=]|%[0-9A-Fa-f]{2})*");
    
    private static final String REMARK_PUBLISH = "Initial publication.";
    private static final String REMARK_PROMOTE = "Promoted to \"{0}\" status.";
    private static final String REMARK_DEMOTE  = "Demoted to \"{0}\" status.";
    private static final String REMARK_CRC     = "Recalculated library CRC.";

    private static RepositoryManager defaultInstance;
    private static Log log = LogFactory.getLog(RepositoryManager.class);

    private RepositoryFileManager fileManager;
    private RepositoryHistoryManager historyManager = new RepositoryHistoryManager( this );
    private String localRepositoryId;
    private String localRepositoryDisplayName;
    private Date lastUpdatedDate;
    private List<RemoteRepositoryClient> remoteRepositories = new ArrayList<RemoteRepositoryClient>();
    private List<String> rootNamespaces;
    private List<RepositoryListener> listeners = new ArrayList<>();
    

    /**
     * Constructor that specifies the root location of the repository to manage.
     * 
     * @param repositoryLocation
     *            the file system location of the repository to manage
     * @throws RepositoryException
     *             thrown if the local repository information cannot be initialized
     */
    public RepositoryManager(File repositoryLocation) throws RepositoryException {
        this(new DefaultRepositoryFileManager(repositoryLocation));
    }

    /**
     * Constructor that specifies the root location of the repository to manage and the file manager
     * to use when interacting with the local file system.
     * 
     * @param repositoryLocation
     *            the file system location of the repository to manage
     * @param fileManager
     *            the file manager to use when interacting with the local file system (null for
     *            default)
     * @throws RepositoryException
     *             thrown if the local repository information cannot be initialized
     */
    public RepositoryManager(RepositoryFileManager fileManager) throws RepositoryException {
        this.fileManager = fileManager;
        initializeLocalRepositoryInfo();
    }

    /**
     * Returns the default <code>RepositoryManager</code> instance to manage the repository at the
     * default file system location.
     * 
     * @return RepositoryManager
     * @throws RepositoryException
     *             thrown if the local repository information cannot be initialized
     */
    public static RepositoryManager getDefault() throws RepositoryException {
        synchronized (RepositoryManager.class) {
            if (defaultInstance == null) {
                defaultInstance = new RepositoryManager(
                        RepositoryFileManager.getDefaultRepositoryLocation());
            }
            return defaultInstance;
        }
    }

    /**
     * Returns the location of the OTA2.0 repository that is managed by this
     * <code>RepositoryManager</code> instance.
     * 
     * @return File
     */
    public File getRepositoryLocation() {
        return fileManager.getRepositoryLocation();
    }

    /**
     * Returns the file location for the projects folder for the local repository.
     * 
     * @return File
     */
    public File getProjectsFolder() {
        return fileManager.getProjectsFolder();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getManager()
     */
    @Override
    public RepositoryManager getManager() {
        return this;
    }

    /**
     * Returns the file manager used by this repository manager instance.
     * 
     * @return RepositoryFileManager
     */
    public RepositoryFileManager getFileManager() {
        return fileManager;
    }

    /**
     * Returns the history manager used by this repository manager instance.
     * 
     * @return RepositoryHistoryManager
     */
    public RepositoryHistoryManager getHistoryManager() {
        return historyManager;
    }

    /**
     * Clears the cache memory of recently downloaded files.
     */
    public void resetDownloadCache() {
    	for (RemoteRepositoryClient remoteRepo : remoteRepositories) {
    		remoteRepo.resetDownloadCache();
    	}
    }
    
    /**
     * Adds a listener that will be notified of stateful actions that are performed by
     * this <code>RepositoryManager</code>.
     * 
     * @param listener  the listener to add
     */
    public void addListener(RepositoryListener listener) {
    	if (listener != null) {
    		listeners.add( listener );
    	}
    }
    
    /**
     * Removes the given listener from this <code>RepositoryManager</code>.
     * 
     * @param listener  the listener to remove
     */
    public void removeListener(RepositoryListener listener) {
		listeners.remove( listener );
    }
    
    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getId()
     */
    @Override
    public String getId() {
        return getLocalRepositoryId();
    }

    /**
     * Returns the ID of the local repository instance.
     * 
     * @return String
     */
    public String getLocalRepositoryId() {
        refreshLocalRepositoryInfo(false);
        return localRepositoryId;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return getLocalRepositoryDisplayName();
    }

    /**
     * Returns the display name of the local repository instance.
     * 
     * @return String
     */
    public String getLocalRepositoryDisplayName() {
        refreshLocalRepositoryInfo(false);
        return localRepositoryDisplayName;
    }

    /**
     * Updates the identity information for the local repository.
     * 
     * @param repositoryId
     *            the ID of the local repository
     * @param displayName
     *            the display name for the local repository
     * @throws RepositoryException
     *             thrown if the local repository's configuration settings cannot be updated
     */
    public void updateLocalRepositoryIdentity(String repositoryId, String displayName)
            throws RepositoryException {
        boolean success = false;

        try {
        	if (repositoryId == null) repositoryId = "";
        	if (displayName == null) displayName = "";
        	
        	if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
        		throw new RepositoryException("Invalid display name for repository (max length is " + MAX_DISPLAY_NAME_LENGTH + ")");
        	}
        	if (!REPOSITORY_ID_PATTERN.matcher( repositoryId ).matches()) {
        		throw new RepositoryException("Invalid repository ID: " + repositoryId);
        	}
            this.localRepositoryId = repositoryId;
            this.localRepositoryDisplayName = displayName;

            fileManager.startChangeSet();
            saveLocalRepositoryMetadata();
            fileManager.commitChangeSet();
            success = true;

        } finally {
            if (!success)
                fileManager.rollbackChangeSet();
        }
    }

    /**
     * Constructs a new local repository instance using the information provided.
     * 
     * @param repositoryId
     *            the ID of the new repository to create
     * @param displayName
     *            the display name for the new repository
     * @throws RepositoryException
     *             throw if the repository cannot be created
     */
    public void createLocalRepository(String repositoryId, String displayName)
            throws RepositoryException {
        File repositoryMetadataFile = new File(fileManager.getRepositoryLocation(),
                RepositoryFileManager.REPOSITORY_METADATA_FILENAME);

        if (repositoryMetadataFile.exists()) {
            throw new RepositoryException("An OTA2.0 repository already exists at location: "
                    + fileManager.getRepositoryLocation().getAbsolutePath());
        }

        // Create and save the repository meta-data record
        RepositoryInfoType repositoryMetadata = new RepositoryInfoType();
        boolean success = false;

        try {
            repositoryMetadata.setID(repositoryId);
            repositoryMetadata.setDisplayName(displayName);
            repositoryMetadata.setRemoteRepositories(new RemoteRepositoriesType());

            fileManager.startChangeSet();
            fileManager.saveRepositoryMetadata(repositoryMetadata);
            fileManager.commitChangeSet();
            success = true;

            // Refresh the contents of the repository to overwrite any of the old settings
            refreshLocalRepositoryInfo(true);

        } finally {
            if (!success)
                fileManager.rollbackChangeSet();
        }
    }

    /**
     * Returns a list of all known OTA2.0 remote repositories.
     * 
     * @return List<RemoteRepository>
     */
    public List<RemoteRepository> listRemoteRepositories() {
        List<RemoteRepository> repositoryList = new ArrayList<RemoteRepository>();

        repositoryList.addAll(remoteRepositories);
        repositoryList.remove(this);
        return repositoryList;
    }

    /**
     * Returns the repository with the specified ID.
     * 
     * @param id
     *            the ID of the repository to return
     * @return Repository
     */
    public Repository getRepository(String id) {
        if (id == null)
            return null;
        Repository repository = null;

        if (id.equals(localRepositoryId)) {
            repository = this;

        } else {
            for (RemoteRepositoryClient remoteRepository : remoteRepositories) {
                if (id.equals(remoteRepository.getId())) {
                    repository = remoteRepository;
                    break;
                }
            }
        }
        return repository;
    }

    /**
     * Adds the given <code>Repository</code> to the universe of known remotely-accessible
     * repositories. This information is added to the global OTA2.0 configuration that applies to
     * all projects and models that can be accessed by the local user.
     * 
     * <p>
     * NOTE: The remote repository must be available for this method to function properly. If the
     * repository's web service is not accessible, this method will throw an exception.
     * 
     * @param endpointUrl
     *            the URL of the repository's web service endpoint
     * @throws RepositoryException
     *             thrown if the specified repository cannot be added
     */
    public RemoteRepository addRemoteRepository(String endpointUrl) throws RepositoryException {
        RepositoryInfoType localRepositoryMetadata = fileManager.loadRepositoryMetadata();
        RepositoryInfoType remoteRepositoryMetadata = RemoteRepositoryClient
                .getRepositoryMetadata(endpointUrl);
        String newRepositoryID = remoteRepositoryMetadata.getID();

        // Make sure the requested endpoint is not a duplicate of an existing repository
        if (localRepositoryMetadata.getRemoteRepositories() != null) {
            if (localRepositoryMetadata.getID().equals(newRepositoryID)) {
                throw new RepositoryException(
                        "The remote repository has the same ID as that of the local repository instance.");
            }
            for (RemoteRepositoryType existingRepository : localRepositoryMetadata
                    .getRemoteRepositories().getRemoteRepository()) {
                if (existingRepository.getID().equals(newRepositoryID)) {
                    throw new RepositoryException(
                            "A remote repository is already defined with ID: " + newRepositoryID);
                }
                if (existingRepository.getEndpointUrl().equals(endpointUrl)) {
                    throw new RepositoryException(
                            "A remote repository with the specified endpoint URL already exists: "
                                    + newRepositoryID);
                }
            }
        } else {
            localRepositoryMetadata.setRemoteRepositories(new RemoteRepositoriesType());
        }

        // Add the new remote repository and update the local repository's meta-data
        RemoteRepositoryType newRepository = new RemoteRepositoryType();
        boolean success = false;

        newRepository.setID(newRepositoryID);
        newRepository.setDisplayName(remoteRepositoryMetadata.getDisplayName());
        newRepository.setEndpointUrl(endpointUrl);
        newRepository.setRefreshPolicy(RefreshPolicy.DAILY);
        localRepositoryMetadata.getRemoteRepositories().getRemoteRepository().add(newRepository);

        try {
            fileManager.startChangeSet();
            fileManager.saveRepositoryMetadata(localRepositoryMetadata);
            fileManager.commitChangeSet();
            success = true;

        } finally {
            if (!success) {
                try {
                    fileManager.rollbackChangeSet();

                } catch (RepositoryException e) {
                    log.warn("Error rolling back the active change set.", e);
                }
            }
        }

        // Refresh the list of repositories information and return the newly-added item
        refreshLocalRepositoryInfo(true);
        return (RemoteRepository) getRepository(newRepositoryID);
    }

    /**
     * Removes the given <code>Repository</code> from the universe of known remotely-accessible
     * repositories. The associated record is deleted from the global OTA2.0 configuration that
     * applies to all projects and models that can be accessed by the local user.
     * 
     * @param repository
     *            the remote repository to add
     * @throws RepositoryException
     *             thrown if the local repository's configuration settings cannot be updated
     */
    public void removeRemoteRepository(RemoteRepository repository) throws RepositoryException {
        boolean success = false;

        if (remoteRepositories.contains(repository)) {
            remoteRepositories.remove(repository);
        }

        // Save this change to the local repository's metadata
        try {
            fileManager.startChangeSet();
            saveLocalRepositoryMetadata();
            fileManager.commitChangeSet();
            success = true;

        } finally {
            if (!success) {
                try {
                    fileManager.rollbackChangeSet();

                } catch (RepositoryException e) {
                    log.warn("Error rolling back the active change set.", e);
                }
                refreshLocalRepositoryInfo(true);
            }
        }
    }

    /**
     * Refreshes the server-provided configuration of all remote repositories that are know to the
     * local host.
     * 
     * @throws RepositoryException
     *             thrown if the local configuration settings cannot be updated
     */
    public void refreshRemoteRepositories() throws RepositoryException {
        boolean success = false;

        for (RemoteRepositoryClient repository : remoteRepositories) {
            try {
                repository.refreshRepositoryMetadata();

            } catch (RepositoryException e) {
                log.warn("Unable to refresh configuration of remote repository: "
                        + repository.getId());
            }
        }

        // Save any updates obtained from the remote repositories to the local repository's metadata
        try {
            fileManager.startChangeSet();
            saveLocalRepositoryMetadata();
            fileManager.commitChangeSet();
            success = true;

        } finally {
            if (!success) {
                try {
                    fileManager.rollbackChangeSet();

                } catch (RepositoryException e) {
                    log.warn("Error rolling back the active change set.", e);
                }
                refreshLocalRepositoryInfo(true);
            }
        }
    }

    /**
     * Assigns a policy value for the given repository that indicates how often items that are
     * cached in the local repository should be checked for updates.
     * 
     * @param repository
     *            the repository for which the credentials apply
     * @param refreshPolicy
     *            the refresh policy value to apply
     * @throws RepositoryException
     *             thrown if the local repository's configuration settings cannot be updated
     */
    public void setRefreshPolicy(RemoteRepository repository, RefreshPolicy refreshPolicy)
            throws RepositoryException {
        if (!remoteRepositories.contains(repository)) {
            throw new IllegalArgumentException(
                    "The specified repository is not accessible from the local workspace.");
        }
        RemoteRepositoryClient remoteRepository = (RemoteRepositoryClient) repository;
        boolean success = false;

        remoteRepository.setRefreshPolicy(refreshPolicy);

        // Save this change to the local repository's metadata
        try {
            fileManager.startChangeSet();
            saveLocalRepositoryMetadata();
            fileManager.commitChangeSet();
            success = true;

        } finally {
            if (!success) {
                try {
                    fileManager.rollbackChangeSet();

                } catch (RepositoryException e) {
                    log.warn("Error rolling back the active change set.", e);
                }
                refreshLocalRepositoryInfo(true);
            }
        }
    }

    /**
     * Assigns the user ID and password credentials for the given repository.
     * 
     * @param repository
     *            the remote repository for which the credentials apply
     * @param userId
     *            the user ID credential for the repository
     * @param password
     *            the plain-text password credential for the repository
     * @throws RepositoryException
     *             thrown if the local repository's configuration settings cannot be updated
     */
    public void setCredentials(Repository repository, String userId, String password)
            throws RepositoryException {
        if (repository == this) {
            throw new IllegalArgumentException(
                    "Security credentials are not required for local repository access.");
        }
        if (!remoteRepositories.contains(repository)) {
            throw new IllegalArgumentException(
                    "The specified repository is not accessible from the local workspace.");
        }
        RemoteRepositoryClient remoteRepository = (RemoteRepositoryClient) repository;
        boolean success = false;

        remoteRepository.setUserId(userId);
        remoteRepository.setEncryptedPassword(((userId == null) || (password == null)) ? null
                : PasswordHelper.encrypt(password));

        // Save this change to the local repository's metadata
        try {
            fileManager.startChangeSet();
            saveLocalRepositoryMetadata();
            fileManager.commitChangeSet();
            success = true;

        } finally {
            if (!success) {
                try {
                    fileManager.rollbackChangeSet();

                } catch (RepositoryException e) {
                    log.warn("Error rolling back the active change set.", e);
                }
                refreshLocalRepositoryInfo(true);
            }
        }
    }

    /**
     * Since the <code>RepositoryManager</code> represents the root repository, it will always
     * return a single namespace URI that is based on the current user's ID. Only remote
     * repositories can manage namespaces other than this default URI.
     * 
     * @see org.opentravel.schemacompiler.repository.Repository#listRootNamespaces()
     */
    @Override
    public List<String> listRootNamespaces() throws RepositoryException {
        List<String> rootNamespaces = new ArrayList<String>();

        if ((this.rootNamespaces == null) || this.rootNamespaces.isEmpty()) {
            rootNamespaces.add(CURRENT_USER_BASE_NAMESPACE
                    + System.getProperty("user.name").toLowerCase());

        } else {
            rootNamespaces.addAll(this.rootNamespaces);
        }
        return rootNamespaces;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#listNamespaceChildren(java.lang.String)
     */
    @Override
    public List<String> listNamespaceChildren(String baseNamespace) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
        List<String> childPaths = fileManager.findChildBaseNamespacePaths(baseNS);

        Collections.sort(childPaths);
        return childPaths;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#listBaseNamespaces()
     */
    @Override
    public List<String> listBaseNamespaces() throws RepositoryException {
        List<String> nsList = fileManager.findAllBaseNamespaces(listRootNamespaces());
        List<String> baseNamespaces = new ArrayList<String>();

        for (String ns : nsList) {
            baseNamespaces.add(ns);
        }
        Collections.sort(baseNamespaces);
        return baseNamespaces;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#listAllNamespaces()
     */
    @Override
    public List<String> listAllNamespaces() throws RepositoryException {
        List<String> nsList = fileManager.findAllNamespaces(listRootNamespaces());
        List<String> namespaces = new ArrayList<String>();

        for (String ns : nsList) {
            namespaces.add(ns);
        }
        Collections.sort(namespaces);
        return namespaces;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#listItems(java.lang.String, boolean, boolean)
     */
    @Override
    public List<RepositoryItem> listItems(String baseNamespace, boolean latestVersionsOnly,
            boolean includeDraftVersions) throws RepositoryException {
    	return listItems( baseNamespace, includeDraftVersions ? null : TLLibraryStatus.FINAL, latestVersionsOnly );
    }

    /**
	 * @see org.opentravel.schemacompiler.repository.Repository#listItems(java.lang.String, org.opentravel.schemacompiler.model.TLLibraryStatus, boolean)
	 */
	@Override
	public List<RepositoryItem> listItems(String baseNamespace, TLLibraryStatus includeStatus,
			boolean latestVersionsOnly) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
        Map<String, List<RepositoryItemVersionedWrapper>> libraryVersionMap = new HashMap<String, List<RepositoryItemVersionedWrapper>>();
        List<LibraryInfoType> metadataList = fileManager.loadLibraryMetadataRecords(baseNS);
        List<RepositoryItem> itemList = new ArrayList<RepositoryItem>();

        for (LibraryInfoType itemMetadata : metadataList) {
        	TLLibraryStatus itemStatus = RepositoryUtils.getLibraryStatus( itemMetadata.getStatus() );
        	
            // Create a map that groups each library's versions together
            if (localRepositoryId.equals(itemMetadata.getOwningRepository())
                    && RepositoryUtils.isInclusiveStatus(itemStatus, includeStatus)) {
                RepositoryItem item = RepositoryUtils.createRepositoryItem(this, itemMetadata);
                List<RepositoryItemVersionedWrapper> libraryVersions = libraryVersionMap.get(item
                        .getLibraryName());

                if (libraryVersions == null) {
                    libraryVersions = new ArrayList<RepositoryItemVersionedWrapper>();
                    libraryVersionMap.put(item.getLibraryName(), libraryVersions);
                }
                libraryVersions.add(new RepositoryItemVersionedWrapper(item));
            }
        }
        
        // Sort the results by library name first, then by descending version number
        List<String> libraryNames = new ArrayList<String>();

        libraryNames.addAll(libraryVersionMap.keySet());
        Collections.sort(libraryNames);

        for (String libraryName : libraryNames) {
            List<RepositoryItemVersionedWrapper> libraryVersions = libraryVersionMap
                    .get(libraryName);
            String versionSchemeId = libraryVersions.get(0).getVersionScheme();

            try {
                VersionScheme versionScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                        versionSchemeId);
                Collections.sort(libraryVersions, versionScheme.getComparator(false));

            } catch (VersionSchemeException e) {
                log.warn("Unable to sort library versions - unrecognized version scheme: "
                        + versionSchemeId);
            }

            if (latestVersionsOnly) {
                RepositoryUtils.checkItemState((RepositoryItemImpl) libraryVersions.get(0)
                        .getItem(), this);
                itemList.add(libraryVersions.get(0).getItem());

            } else {
                for (RepositoryItemVersionedWrapper itemWrapper : libraryVersions) {
                    RepositoryUtils
                            .checkItemState((RepositoryItemImpl) itemWrapper.getItem(), this);
                    itemList.add(itemWrapper.getItem());
                }
            }
        }
        return itemList;
	}

	/**
     * @see org.opentravel.schemacompiler.repository.Repository#search(java.lang.String, boolean, boolean)
     */
    @SuppressWarnings("deprecation")
	@Override
    public List<RepositoryItem> search(String freeTextQuery, boolean latestVersionsOnly,
            boolean includeDraftVersions) throws RepositoryException {
        List<RepositoryItem> searchResults = new ArrayList<RepositoryItem>();

        for (RemoteRepository repository : remoteRepositories) {
            try {
                List<RepositoryItem> itemList = repository.search(freeTextQuery,
                        latestVersionsOnly, includeDraftVersions);

                for (RepositoryItem item : itemList) {
                    RepositoryUtils.checkItemState((RepositoryItemImpl) item, this);
                    searchResults.add(item);
                }

            } catch (RepositoryException e) {
                log.warn("Error contacting remote repository: " + repository.getId() + ", reason: "
                        + ExceptionUtils.getExceptionMessage(e));
            }
        }
        return searchResults;
    }

    /**
	 * @see org.opentravel.schemacompiler.repository.Repository#search(java.lang.String, org.opentravel.schemacompiler.model.TLLibraryStatus, boolean)
	 */
	@Override
	public List<RepositorySearchResult> search(String freeTextQuery, TLLibraryStatus includeStatus,
			boolean latestVersionsOnly) throws RepositoryException {
        List<RepositorySearchResult> searchResults = new ArrayList<>();

        for (RemoteRepository repository : remoteRepositories) {
            try {
                List<RepositorySearchResult> resultList = repository.search(freeTextQuery,
                        includeStatus, latestVersionsOnly);
                
                searchResults.addAll( resultList );

            } catch (RepositoryException e) {
                log.warn("Error contacting remote repository: " + repository.getId() + ", reason: "
                        + ExceptionUtils.getExceptionMessage(e));
            }
        }
        return searchResults;
	}

	/**
     * @see org.opentravel.schemacompiler.repository.Repository#getVersionHistory(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public List<RepositoryItem> getVersionHistory(RepositoryItem item) throws RepositoryException {
        List<RepositoryItem> versionHistory = new ArrayList<RepositoryItem>();

        if (item.getRepository() == this) {
            String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
            List<LibraryInfoType> metadataList = fileManager.loadLibraryMetadataRecords(baseNS);
            List<RepositoryItemVersionedWrapper> versionList = new ArrayList<RepositoryItemVersionedWrapper>();

            for (LibraryInfoType libraryMetadata : metadataList) {
                if (libraryMetadata.getLibraryName().equals(item.getLibraryName())) {
                    RepositoryItem itemVersion = RepositoryUtils.createRepositoryItem(this,
                            libraryMetadata);

                    versionList.add(new RepositoryItemVersionedWrapper(itemVersion));
                }
            }
            try {
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                        item.getVersionScheme());

                Collections.sort(versionList, vScheme.getComparator(false));

                for (RepositoryItemVersionedWrapper itemWrapper : versionList) {
                    RepositoryUtils
                            .checkItemState((RepositoryItemImpl) itemWrapper.getItem(), this);
                    versionHistory.add(itemWrapper.getItem());
                }

            } catch (VersionSchemeException e) {
                throw new RepositoryException("Unknown version scheme :" + item.getVersionScheme(),
                        e);
            }
        } else if (item.getRepository() != null) {
            versionHistory = item.getRepository().getVersionHistory(item);
        }
        return versionHistory;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getRepositoryItem(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public RepositoryItem getRepositoryItem(String baseNamespace, String filename,
            String versionIdentifier) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
        File itemContent = fileManager.getLibraryContentLocation(baseNS, filename,
                versionIdentifier);

        for (RemoteRepositoryClient repository : remoteRepositories) {
            if (itemContent.exists())
                break;
            try {
                repository.downloadContent(baseNS, filename, versionIdentifier, false);

            } catch (RepositoryException e) {
                // Ignore and move onto the next repository
            }
        }
        if (!itemContent.exists()) {
            throw new RepositoryException(
                    "The managed content for the requested resource could not be located.");
        }
        LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS, filename,
                versionIdentifier);
        RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(this, libraryMetadata);

        RepositoryUtils.checkItemState(item, this);
        return item;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getRepositoryItem(java.lang.String)
     */
    @Override
    public RepositoryItem getRepositoryItem(String itemUri) throws RepositoryException,
            URISyntaxException {
        URI uri = RepositoryUtils.toRepositoryItemUri(itemUri);
        Repository repository = getRepository(uri.getAuthority());
        RepositoryItem item;

        if (repository == null) {
            throw new RepositoryException("Unknown repository ID: " + uri.getAuthority());
        }
        if (repository == this) {
            item = null;
        } else {
            item = repository.getRepositoryItem(itemUri, null);
        }
        return item;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getRepositoryItem(java.lang.String,java.lang.String)
     */
    @Override
    public RepositoryItem getRepositoryItem(String itemUri, String itemNamespace)
            throws RepositoryException, URISyntaxException {
        URI uri = RepositoryUtils.toRepositoryItemUri(itemUri);
        Repository repository = getRepository(uri.getAuthority());
        RepositoryItem item;

        if (repository == null) {
            throw new RepositoryException("Unknown repository ID: " + uri.getAuthority());
        }
        if (repository == this) {
            VersionSchemeFactory vsFactory = VersionSchemeFactory.getInstance();
            String[] uriParts = RepositoryUtils.parseRepositoryItemUri(uri);
            String versionScheme = (uriParts[3] == null) ? vsFactory.getDefaultVersionScheme()
                    : uriParts[3];

            if (itemNamespace == null) {
                itemNamespace = uriParts[1];
            }
            if (itemNamespace == null) {
                throw new RepositoryException(
                        "Unable to identify the repository item because its namespace was not specified.");
            }
            try {
                VersionScheme vScheme = vsFactory.getVersionScheme(versionScheme);
                String baseNS = vScheme.getBaseNamespace(itemNamespace);
                String versionIdentifier = vScheme.getVersionIdentifier(itemNamespace);

                LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS,
                        uriParts[2], versionIdentifier);

                if ((libraryMetadata == null)
                        || !libraryMetadata.getOwningRepository().equals(uriParts[0])) {
                    throw new RepositoryException("Resource not found in repository: "
                            + uriParts[0]);
                }
                RepositoryItemImpl itemImpl = RepositoryUtils.createRepositoryItem(this,
                        libraryMetadata);
                RepositoryUtils.checkItemState(itemImpl, this);
                item = itemImpl;

            } catch (VersionSchemeException e) {
                throw new RepositoryException("Unknown version scheme specified by URI: "
                        + versionScheme);
            }
        } else {
            item = repository.getRepositoryItem(itemUri, itemNamespace);
        }
        return item;
    }
    
    /**
     * Returns the corresponding repository item for the given library file if it is under
     * the control of this local repository (whether it is managed or a remote copy).  If
     * the specified file does not exist or is not under control of the local repository,
     * this method will return null.
     * 
     * @param libraryFile  the library file for which to return a repository item
     * @return RepositoryItem
     * @throws RepositoryException  thrown if an error occurs while loading the library's metadata
     */
    public RepositoryItem getRepositoryItem(File libraryFile) throws RepositoryException {
    	RepositoryItem item = null;
    	
    	if (fileManager.isRepositoryFile( libraryFile )) {
    		String metadataFilename = fileManager.getLibraryMetadataFilename( libraryFile.getName() );
    		File metadataFile = new File( libraryFile.getParentFile(), metadataFilename );
    		
    		if (metadataFile.exists()) {
    			LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata( metadataFile );
    			
    			item = RepositoryUtils.createRepositoryItem( this, libraryMetadata );
    		}
    	}
    	return item;
    }
    
    /**
	 * @see org.opentravel.schemacompiler.repository.Repository#getHistory(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public RepositoryItemHistory getHistory(RepositoryItem item) throws RepositoryException {
		RepositoryItemHistory history;
		
        if (item.getRepository() == this) {
        	LibraryHistoryType libraryHistory = historyManager.getHistory( item );
        	
        	if (libraryHistory == null) {
        		throw new RepositoryException("Commit history not found for " + item.getFilename());
        	}
        	history = RepositoryUtils.createItemHistory( libraryHistory, this );
        	
        } else {
        	history = item.getRepository().getHistory( item );
        }
        return history;
	}

	/**
     * Always returns write permissions for the local repository.
     * 
     * @see org.opentravel.schemacompiler.repository.RemoteRepository#getUserAuthorization(java.lang.String)
     */
    @Override
    public RepositoryPermission getUserAuthorization(String baseNamespace)
            throws RepositoryException {
        return RepositoryPermission.WRITE;
    }

    /**
	 * @see org.opentravel.schemacompiler.repository.Repository#getLockedItems()
	 */
	@Override
	public List<RepositoryItem> getLockedItems() throws RepositoryException {
		List<RepositoryItem> lockedItems = new ArrayList<>();
		
		for (String baseNS : listBaseNamespaces()) {
			for (RepositoryItem item : listItems( baseNS, null, false )) {
				if (item.getState() == RepositoryItemState.MANAGED_WIP) {
					lockedItems.add( item );
				}
			}
		}
		return lockedItems;
	}

	/**
     * @see org.opentravel.schemacompiler.repository.Repository#createRootNamespace(java.lang.String)
     */
    @Override
    public void createRootNamespace(String rootNamespace) throws RepositoryException {
        String rootNS = RepositoryNamespaceUtils.normalizeUri(rootNamespace);
        boolean success = false;
        try {
            fileManager.startChangeSet();
            File rootNSFolder = fileManager.getNamespaceFolder(rootNS, null);
            File nsidFile = new File(rootNSFolder, RepositoryFileManager.NAMESPACE_ID_FILENAME);

            // Validation Check - Make sure the namespace follows a proper URL format
            try {
                URL nsUrl = new URL(rootNamespace);

                if ((nsUrl.getProtocol() == null) || (nsUrl.getAuthority() == null)) {
                    throw new MalformedURLException(); // URLs without protocols or authorities are
                                                       // not valid for the repository
                }
                if (rootNamespace.indexOf('?') >= 0) {
                    throw new RepositoryException(
                            "Query strings are not allowed on root namespace URIs.");
                }
            } catch (MalformedURLException e) {
                throw new RepositoryException(
                        "The root namespace does not conform to the required URI format.");
            }

            // Validation Check - Look for a file conflict with an existing namespace
            if (nsidFile.exists()) {
                throw new RepositoryException(
                        "The root namespace cannot be created because it conflicts with an existing one.");
            }

            // Validation Check - Check to see if the new root is a parent or child of an existing
            // root namespace
            String repositoryBaseFolder = fileManager.getRepositoryLocation().getAbsolutePath();
            List<String> existingRootNSFolderPaths = new ArrayList<String>();
            String rootNSTestPath = rootNSFolder.getAbsolutePath();

            if (!rootNSTestPath.endsWith("/")) {
                rootNSTestPath += "/";
            }

            for (String existingRootNS : listRootNamespaces()) {
                File existingRootNSFolder = fileManager.getNamespaceFolder(existingRootNS, null);

                if (rootNSTestPath.startsWith(existingRootNSFolder.getAbsolutePath())) {
                    throw new RepositoryException(
                            "The root namespace cannot be created because it conflicts with an existing one.");
                }
                while ((existingRootNSFolder != null)
                        && !repositoryBaseFolder.equals(existingRootNSFolder.getAbsolutePath())) {
                    existingRootNSFolderPaths.add(existingRootNSFolder.getAbsolutePath());
                    existingRootNSFolder = existingRootNSFolder.getParentFile();
                }
            }
            if (existingRootNSFolderPaths.contains(rootNSFolder.getAbsolutePath())) {
                throw new RepositoryException(
                        "The root namespace cannot be created because it conflicts with an existing one.");
            }

            // Create the new root namespace if all of the validation checks passed
            rootNamespaces.add(rootNS);
            saveLocalRepositoryMetadata();
            fileManager.createNamespaceIdFiles(rootNS);
            success = true;

            log.info("Successfully created root namespace: " + rootNS + " by "
                    + fileManager.getCurrentUserId());

        } finally {
            // Commit or roll back the changes based on the result of the operation
            if (success) {
                fileManager.commitChangeSet();
                
                // Notify listeners
                for (RepositoryListener listener : listeners) {
                	try {
                		listener.onCreateRootNamespace( rootNamespace );
                		
                	} catch (Throwable t) {
                		log.warn("Unexpected error during listener invocation.", t);
                	}
                }
                
            } else {
                try {
                    fileManager.rollbackChangeSet();
                } catch (Throwable t) {
                    log.error("Error rolling back the current change set.", t);
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#deleteRootNamespace(java.lang.String)
     */
    @Override
    public void deleteRootNamespace(String rootNamespace) throws RepositoryException {
        String rootNS = RepositoryNamespaceUtils.normalizeUri(rootNamespace);
        boolean success = false;
        try {
            fileManager.startChangeSet();

            if (!rootNamespaces.contains(rootNS)) {
                throw new RepositoryException("The root namespace to be deleted is not valid.");
            }
            if (!listNamespaceChildren(rootNS).isEmpty()) {
                throw new RepositoryException(
                        "The root namespace cannot be deleted because it is not empty.");
            }

            fileManager.deleteNamespaceIdFile(rootNS, true);
            rootNamespaces.remove(rootNS);
            saveLocalRepositoryMetadata();
            success = true;

            log.info("Successfully deleted root namespace: " + rootNS + " by "
                    + fileManager.getCurrentUserId());

        } finally {
            // Commit or roll back the changes based on the result of the operation
            if (success) {
                fileManager.commitChangeSet();
                
                // Notify listeners
                for (RepositoryListener listener : listeners) {
                	try {
                		listener.onDeleteRootNamespace( rootNamespace );
                		
                	} catch (Throwable t) {
                		log.warn("Unexpected error during listener invocation.", t);
                	}
                }
                
            } else {
                try {
                    fileManager.rollbackChangeSet();
                } catch (Throwable t) {
                    log.error("Error rolling back the current change set.", t);
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#createNamespace(java.lang.String)
     */
    @Override
    public void createNamespace(String baseNamespace) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
        boolean success = false;
        try {
            fileManager.startChangeSet();
            File nsidFile = new File(fileManager.getNamespaceFolder(baseNS, null),
                    RepositoryFileManager.NAMESPACE_ID_FILENAME);

            if (nsidFile.exists()) {
                throw new RepositoryException(
                        "The namespace cannot be created because it conflicts with an existing one.");
            }
            fileManager.createNamespaceIdFiles(baseNS);
            success = true;

            log.info("Successfully created namespace: " + baseNS);

        } finally {
            // Commit or roll back the changes based on the result of the operation
            if (success) {
                fileManager.commitChangeSet();
                
                // Notify listeners
                for (RepositoryListener listener : listeners) {
                	try {
                		listener.onCreateNamespace( baseNamespace );
                		
                	} catch (Throwable t) {
                		log.warn("Unexpected error during listener invocation.", t);
                	}
                }
                
            } else {
                try {
                    fileManager.rollbackChangeSet();
                } catch (Throwable t) {
                    log.error("Error rolling back the current change set.", t);
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#deleteNamespace(java.lang.String)
     */
    @Override
    public void deleteNamespace(String baseNamespace) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
        boolean success = false;
        try {
            fileManager.startChangeSet();
            fileManager.deleteNamespaceIdFile(baseNS, false);
            success = true;

            log.info("Successfully deleted namespace: " + baseNS);

        } finally {
            // Commit or roll back the changes based on the result of the operation
            if (success) {
                fileManager.commitChangeSet();
                
                // Notify listeners
                for (RepositoryListener listener : listeners) {
                	try {
                		listener.onDeleteNamespace( baseNamespace );
                		
                	} catch (Throwable t) {
                		log.warn("Unexpected error during listener invocation.", t);
                	}
                }
                
            } else {
                try {
                    fileManager.rollbackChangeSet();
                } catch (Throwable t) {
                    log.error("Error rolling back the current change set.", t);
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#publish(java.io.InputStream,
     *      java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, org.opentravel.schemacompiler.model.TLLibraryStatus)
     */
    @Override
    public RepositoryItem publish(InputStream unmanagedContent, String filename,
            String libraryName, String namespace, String versionIdentifier, String versionScheme,
            TLLibraryStatus initialStatus) throws RepositoryException {
        String targetNS = RepositoryNamespaceUtils.normalizeUri(namespace);
        RepositoryItem publishedItem = null;
        boolean success = false;
        try {
            log.info("Publishing '" + filename + "' to namespace '" + targetNS + "'");
            fileManager.startChangeSet();

            // Check to see if the library has already been published to the repository
            String baseNamespace = targetNS;

            if (versionScheme != null) {
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                        versionScheme);

                if (vScheme.isValidNamespace(targetNS)) {
                    baseNamespace = vScheme.getBaseNamespace(targetNS);

                } else {
                    throw new RepositoryException(
                            "Cannot publish '"
                                    + filename
                                    + "' because its namespace is not valid for the assigned version scheme.");
                }
            }

            try {
                fileManager.loadLibraryMetadata(baseNamespace, filename, versionIdentifier);
                throw new IllegalStateException();

            } catch (IllegalStateException e) {
                throw new RepositoryException("Unable to publish - the library '" + filename
                        + "' has already been published to a repository.");

            } catch (RepositoryException e) {
                // Happy path - the item must not yet exist in the repository
            }

            // Create the namespace folder (if it does not already exist)
            fileManager.createNamespaceIdFiles(baseNamespace);

            // Save the library meta-data file
            LibraryInfoType libraryMetadata = new LibraryInfoType();

            libraryMetadata.setNamespace(targetNS);
            libraryMetadata.setBaseNamespace(baseNamespace);
            libraryMetadata.setFilename(filename);
            libraryMetadata.setLibraryName(libraryName);
            libraryMetadata.setVersion(versionIdentifier);
            libraryMetadata.setVersionScheme(versionScheme);
            libraryMetadata.setStatus(initialStatus.toRepositoryStatus());
            libraryMetadata.setState(RepositoryState.MANAGED_UNLOCKED);
            libraryMetadata.setOwningRepository(localRepositoryId);

            File metadataFile = fileManager.saveLibraryMetadata(libraryMetadata);
            log.info("Library metadata saved: " + metadataFile.getAbsolutePath());
            File contentFile = new File(metadataFile.getParent(), filename);

            // Save the library content
            fileManager.saveFile(contentFile, unmanagedContent);
            log.info("Library content saved: " + contentFile.getAbsolutePath());
            
            // Build and return the repository item to represent the content we just published
            publishedItem = RepositoryUtils.createRepositoryItem(this, libraryMetadata);

            // Create the history entry
            historyManager.addToHistory( publishedItem, new Date(), REMARK_PUBLISH );

            success = true;
            log.info("Content '" + filename + "' published successfully to namespace '"
                    + baseNamespace + "'");
            return publishedItem;

        } catch (VersionSchemeException e) {
            throw new RepositoryException(e.getMessage(), e);

        } finally {
            // Close all streams
            try {
                if (unmanagedContent != null)
                    unmanagedContent.close();
            } catch (Throwable t) {
            }

            // Commit or roll back the changes based on the result of the operation
            if (success) {
                fileManager.commitChangeSet();
                
                // Notify listeners
                for (RepositoryListener listener : listeners) {
                	try {
                		listener.onPublish( publishedItem );
                		
                	} catch (Throwable t) {
                		log.warn("Unexpected error during listener invocation.", t);
                	}
                }
                
            } else {
                try {
                    fileManager.rollbackChangeSet();
                } catch (Throwable t) {
                    log.error("Error rolling back the current change set.", t);
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#commit(org.opentravel.schemacompiler.repository.RepositoryItem)
     * @deprecated  use {@link #commit(RepositoryItem, String)} instead
     */
    @Override
    @Deprecated
    public void commit(RepositoryItem item) throws RepositoryException {
    	commit(item, (String) null);
    }

    /**
	 * @see org.opentravel.schemacompiler.repository.Repository#commit(org.opentravel.schemacompiler.repository.RepositoryItem, java.lang.String)
	 */
	@Override
	public void commit(RepositoryItem item, String remarks) throws RepositoryException {
        if (item.getRepository() == this) {
            InputStream wipContent = null;
            try {
                File wipFile = fileManager.getLibraryWIPContentLocation(item.getBaseNamespace(), item.getFilename());

                if (!wipFile.exists()) {
                    throw new RepositoryException("The work-in-process file does not exist: "
                            + item.getFilename());
                }
                wipContent = new FileInputStream(wipFile);
                commit(item, wipContent, remarks);

            } catch (IOException e) {
                throw new RepositoryException("The work-in-process file cannot be accessed: "
                        + item.getFilename());
            }

        } else {
            ((RemoteRepository) item.getRepository()).commit(item, remarks);
        }
	}

	/**
     * Commits the content of the specified <code>RepositoryItem</code> by updating its repository
     * contents to match the data obtained from the input stream for the work-in-process content
     * provided.
     * 
     * @param item  the repository item to commit
     * @param wipContent  the work-in-process content that will replace the current content of the
     *					  repository item
     * @throws RepositoryException  thrown if the file content cannot be committed or is not yet
     *								locked by the current user
     * @deprecated  use {@link #commit(RepositoryItem, InputStream, String)} instead
     */
	@Deprecated
    public void commit(RepositoryItem item, InputStream wipContent) throws RepositoryException {
    	commit( item, wipContent, null );
    }
    
    /**
     * Commits the content of the specified <code>RepositoryItem</code> by updating its repository
     * contents to match the data obtained from the input stream for the work-in-process content
     * provided.
     * 
     * @param item  the repository item to commit
     * @param wipContent  the work-in-process content that will replace the current content of the
     *					  repository item
     * @param remarks  free-text remarks that describe the nature of the change being committed
     * @throws RepositoryException  thrown if the file content cannot be committed or is not yet
     *								locked by the current user
     */
    public void commit(RepositoryItem item, InputStream wipContent, String remarks) throws RepositoryException {
    	commit( item, wipContent, remarks, true );
    }
    
    /**
     * Commits the content of the specified <code>RepositoryItem</code> by updating its repository
     * contents to match the data obtained from the input stream for the work-in-process content
     * provided.
     * 
     * @param item  the repository item to commit
     * @param wipContent  the work-in-process content that will replace the current content of the
     *					  repository item
     * @param remarks  free-text remarks that describe the nature of the change being committed
     * @throws RepositoryException  thrown if the file content cannot be committed or is not yet
     *								locked by the current user
     */
    private void commit(RepositoryItem item, InputStream wipContent, String remarks, boolean notifyListeners)
    		throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
        LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS,
                item.getFilename(), item.getVersion());

        if (((libraryMetadata.getState() != RepositoryState.MANAGED_LOCKED) && (item.getState() != RepositoryItemState.MANAGED_WIP))
                || (item.getLockedByUser() == null)
                || !item.getLockedByUser().equalsIgnoreCase(libraryMetadata.getLockedBy())) {
            throw new RepositoryException(
                    "Unable to commit - only work-in-process items can be committed to the repository.");
        }

        boolean success = false;
        try {
            log.info("Committing updated content '" + item.getFilename() + "' to namespace '"
                    + item.getBaseNamespace() + "'");
            fileManager.startChangeSet();

            // Overwrite the existing content with the WIP data
            File contentFile = fileManager.getLibraryContentLocation(item.getBaseNamespace(),
                    item.getFilename(), item.getVersion());

            fileManager.saveFile(contentFile, wipContent);

            // Create the history entry for this update
            historyManager.addToHistory( item, new Date(), remarks );

            // Update the library's meta-data with the current date
            libraryMetadata.setLastUpdated(XMLGregorianCalendarConverter
                    .toXMLGregorianCalendar(new Date()));
            fileManager.saveLibraryMetadata(libraryMetadata);

            log.info("Successfully committed content '" + item.getFilename() + "' to namespace '"
                    + baseNS + "'");
            success = true;

        } finally {
            try {
                if (wipContent != null)
                    wipContent.close();
            } catch (Throwable t) {
            }

            // Commit or roll back the changes based on the result of the operation
            if (success) {
                fileManager.commitChangeSet();
                
                // Notify listeners (if required)
                if (notifyListeners) {
                    for (RepositoryListener listener : listeners) {
                    	try {
                    		listener.onCommit( item, remarks );
                    		
                    	} catch (Throwable t) {
                    		log.warn("Unexpected error during listener invocation.", t);
                    	}
                    }
                }
                
            } else {
                try {
                    fileManager.rollbackChangeSet();
                } catch (Throwable t) {
                    log.error("Error rolling back the current change set.", t);
                }
            }
        }
    }

    /**
     * Reverts the specified file to match the content currently stored in the item's owning
     * repository.
     * 
     * @param item
     *            the repository item to be reverted
     * @throws RepositoryException
     *             thrown if the file content cannot be reverted
     */
    public void revert(RepositoryItem item) throws RepositoryException {
        boolean success = false;
        InputStream in = null;
        try {
            fileManager.startChangeSet();
            String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
            LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS,
                    item.getFilename(), item.getVersion());

            if (((libraryMetadata.getState() != RepositoryState.MANAGED_LOCKED) && (item.getState() != RepositoryItemState.MANAGED_WIP))
                    || (item.getLockedByUser() == null)
                    || !item.getLockedByUser().equalsIgnoreCase(libraryMetadata.getLockedBy())) {
                throw new RepositoryException(
                        "Unable to revert - only work-in-process items can be reverted.");
            }

            // Replace the existing WIP content with the latest content from the local repository
            File repositoryContentFile = fileManager.getLibraryContentLocation(
                    item.getBaseNamespace(), item.getFilename(), item.getVersion());
            File wipContentFile = fileManager.getLibraryWIPContentLocation(item.getBaseNamespace(),
                    item.getFilename());

            fileManager.saveFile(wipContentFile, new FileInputStream(repositoryContentFile));
            success = true;

        } catch (IOException e) {
            throw new RepositoryException("Unable to revert the repository item: "
                    + item.getFilename(), e);

        } finally {
            // Close all open streams
            try {
                if (in != null)
                    in.close();
            } catch (Throwable t) {
            }

            // Commit or roll back the changes based on the result of the operation
            if (success) {
                fileManager.commitChangeSet();
                
            } else {
                try {
                    fileManager.rollbackChangeSet();
                } catch (Throwable t) {
                    log.error("Error rolling back the current change set.", t);
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#lock(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public void lock(RepositoryItem item) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
        LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS,
                item.getFilename(), item.getVersion());

        if (libraryMetadata.getState() != RepositoryState.MANAGED_UNLOCKED) {
            throw new RepositoryException("Unable to obtain lock - item is not currently unlocked");
        }
        if (libraryMetadata.getStatus() == LibraryStatus.FINAL) {
            throw new RepositoryException(
                    "Unable to obtain lock - only draft repository items can be edited");
        }
        if (item.getRepository() == this) {
            boolean success = false;
            try {
                log.info("Locking content for '" + item.getFilename() + "' to namespace '" + baseNS
                        + "'");
                fileManager.startChangeSet();
                libraryMetadata.setState(RepositoryState.MANAGED_LOCKED);
                libraryMetadata.setLockedBy(item.getLockedByUser());
                libraryMetadata.setLastUpdated(XMLGregorianCalendarConverter
                        .toXMLGregorianCalendar(new Date()));

                fileManager.saveLibraryMetadata(libraryMetadata);

                ((RepositoryItemImpl) item).setState(RepositoryItemState.MANAGED_WIP);
                log.info("Successfully locked content for '" + item.getFilename()
                        + "' to namespace '" + baseNS + "'");
                success = true;

            } finally {
                // Commit or roll back the changes based on the result of the operation
                if (success) {
                    fileManager.commitChangeSet();
                    
                    // Notify listeners
                    for (RepositoryListener listener : listeners) {
                    	try {
                    		listener.onLock( item );
                    		
                    	} catch (Throwable t) {
                    		log.warn("Unexpected error during listener invocation.", t);
                    	}
                    }
                    
                } else {
                    try {
                        fileManager.rollbackChangeSet();
                    } catch (Throwable t) {
                        log.error("Error rolling back the current change set.", t);
                    }
                }
            }

        } else {
            ((RemoteRepository) item.getRepository()).lock(item);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#unlock(org.opentravel.schemacompiler.repository.RepositoryItem,boolean)
     * @deprecated use {@link #unlock(RepositoryItem, boolean, String)} instead
     */
    @Override
    @Deprecated
    public void unlock(RepositoryItem item, boolean commitWIP) throws RepositoryException {
    	unlock(item, commitWIP, null);
    }

    /**
	 * @see org.opentravel.schemacompiler.repository.Repository#unlock(org.opentravel.schemacompiler.repository.RepositoryItem, boolean, java.lang.String)
	 */
	@Override
	public void unlock(RepositoryItem item, boolean commitWIP, String remarks) throws RepositoryException {
        File wipFile = fileManager.getLibraryWIPContentLocation(item.getBaseNamespace(),
                item.getFilename());

        if (item.getRepository() == this) {
            InputStream wipContent = null;

            try {
                if (commitWIP) {
                    if (!wipFile.exists()) {
                        throw new RepositoryException("The work-in-process file does not exist: "
                                + item.getFilename());
                    }
                    wipContent = new FileInputStream(wipFile);
                }
                unlock(item, wipContent, remarks);

            } catch (IOException e) {
                throw new RepositoryException("The work-in-process file cannot be accessed: "
                        + item.getFilename());

            } finally {
                try {
                    if (wipContent != null)
                        wipContent.close();
                } catch (Throwable t) {
                }
            }

        } else {
            ((RemoteRepository) item.getRepository()).unlock(item, commitWIP, remarks);
        }

        // Rename WIP file to ".bak" since the 'real' content is now managed by the repository.
        File backupFile = new File(wipFile.getParentFile(),
                ProjectFileUtils.getBackupFilename(wipFile));

        if (backupFile.exists()) { // delete the old backup, if one exists
            backupFile.delete();
        }
        wipFile.renameTo(backupFile);
	}

	/**
     * Locks the specified repository item using the credentials for its owning repository. If the
     * 'wipContent' stream is provided, the work-in-process conent of the will be committed to the
     * remote repository before the existing lock is released. If the stream parameter is null, any
     * changes in the item's WIP content will be discarded.
     * 
     * @param item
     *            the repository item to lock
     * @param wipContent
     *            stream for content that should be committed before the item's lock is released
     *            (may be null)
     * @throws RepositoryException
     *             thrown if the file content cannot be unlocked or is not yet locked by the current
     *             user
     * @deprecated  use {@link #unlock(RepositoryItem, InputStream, String)} instead
     */
	@Deprecated
    public void unlock(RepositoryItem item, InputStream wipContent) throws RepositoryException {
		unlock(item, wipContent, null);
	}
	
	/**
     * Locks the specified repository item using the credentials for its owning repository. If the
     * 'wipContent' stream is provided, the work-in-process conent of the will be committed to the
     * remote repository before the existing lock is released. If the stream parameter is null, any
     * changes in the item's WIP content will be discarded.
     * 
     * @param item
     *            the repository item to lock
     * @param wipContent
     *            stream for content that should be committed before the item's lock is released
     *            (may be null)
	 * @param remarks
	 *            remarks provided by the user to describe the nature of the commit (ignored if wipContent is null)
     * @throws RepositoryException
     *             thrown if the file content cannot be unlocked or is not yet locked by the current
     *             user
     */
    public void unlock(RepositoryItem item, InputStream wipContent, String remarks) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
        LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS,
                item.getFilename(), item.getVersion());

        if (((libraryMetadata.getState() != RepositoryState.MANAGED_LOCKED) && (item.getState() != RepositoryItemState.MANAGED_WIP))
                || (item.getLockedByUser() == null)
                || !item.getLockedByUser().equalsIgnoreCase(libraryMetadata.getLockedBy())) {
            throw new RepositoryException("Unable to release lock - item is not currently locked");
        }

        boolean success = false;
        try {
            log.info("Unlocking content for '" + item.getFilename() + "' to namespace '" + baseNS
                    + "'");

            // Commit the existing WIP content if requested by the caller
            if (wipContent != null) {
                commit(item, wipContent, remarks, false);
            }
            fileManager.startChangeSet();

            // Update the meta-data to release the lock
            libraryMetadata.setState(RepositoryState.MANAGED_UNLOCKED);
            libraryMetadata.setLockedBy(null);
            libraryMetadata.setLastUpdated(XMLGregorianCalendarConverter
                    .toXMLGregorianCalendar(new Date()));

            fileManager.saveLibraryMetadata(libraryMetadata);

            ((RepositoryItemImpl) item).setState(RepositoryItemState.MANAGED_UNLOCKED);
            log.info("Successfully unlocked content for '" + item.getFilename()
                    + "' to namespace '" + baseNS + "'");
            success = true;

        } finally {
            // Commit or roll back the changes based on the result of the operation
            if (success) {
                fileManager.commitChangeSet();
                
                // Notify listeners
                for (RepositoryListener listener : listeners) {
                	try {
                		listener.onUnlock( item, (wipContent != null), remarks );
                		
                	} catch (Throwable t) {
                		log.warn("Unexpected error during listener invocation.", t);
                	}
                }
                
            } else {
                try {
                    fileManager.rollbackChangeSet();
                } catch (Throwable t) {
                    log.error("Error rolling back the current change set.", t);
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#promote(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public void promote(RepositoryItem item) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
        LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS,
                item.getFilename(), item.getVersion());
        File contentFile = fileManager.getLibraryContentLocation(baseNS, item.getFilename(),
                item.getVersion());
        LibraryContentWrapper libraryContent = loadOtmLibraryContent(contentFile);
        TLLibraryStatus originalStatus = item.getStatus();
        TLLibraryStatus targetStatus = null;
        
        if (libraryMetadata.getState() != RepositoryState.MANAGED_UNLOCKED) {
            throw new RepositoryException(
                    "Unable to promote - the item is currently locked for editing.");
        }
        if (libraryMetadata.getStatus() == null) {
            throw new RepositoryException(
                    "Unable to promote - the item's status is not yet assigned.");
        }
        
        if (libraryContent.is16Library) {
            if (libraryMetadata.getStatus() == LibraryStatus.OBSOLETE) {
                throw new RepositoryException(
                        "Unable to promote - only user-defined libraries that are not in OBSOLETE status can be promoted.");
            }
        	
        } else {
            if (libraryMetadata.getStatus() != LibraryStatus.DRAFT) {
                throw new RepositoryException(
                        "Unable to promote - only user-defined libraries in DRAFT status can be promoted.");
            }
        }

        if (item.getRepository() == this) {
            boolean success = false;
            try {
                fileManager.startChangeSet();

                // Change the status of the library metadata and content
                TLLibraryStatus currentStatus = TLLibraryStatus.fromRepositoryStatus( libraryMetadata.getStatus() );
                
                targetStatus = libraryContent.is16Library ? currentStatus.nextStatus() : TLLibraryStatus.FINAL;

                if (libraryContent != null) {
                    libraryContent.content.setStatus(targetStatus);
                }
                libraryMetadata.setStatus(targetStatus.toRepositoryStatus());
                libraryMetadata.setLastUpdated(XMLGregorianCalendarConverter.toXMLGregorianCalendar(new Date()));

                // Save the changes and update the repository item sent for this method call
                if (libraryContent != null) {
                	saveOtmLibraryContent( libraryContent );
                }
                fileManager.saveLibraryMetadata(libraryMetadata);

                // Create the history entry for this update
                historyManager.addToHistory( item, new Date(), MessageFormat.format( REMARK_PROMOTE,
                		SchemaCompilerApplicationContext.getContext().getMessage( targetStatus.toString(), null, Locale.getDefault() ) ) );

                if (!(item instanceof ProjectItem)) {
                    // Only required for non-ProjectItems; ProjectItem derives the status value from
                    // its library content
                    ((RepositoryItemImpl) item).setStatus(targetStatus);
                }
                log.info("Successfully promoted managed item '" + item.getFilename()
                        + "' to " + targetStatus + " status by " + fileManager.getCurrentUserId());
                success = true;

            } catch (Exception e) {
                throw new RepositoryException("Unable to promote the repository item: "
                        + item.getFilename(), e);

            } finally {
                // Commit or roll back the changes based on the result of the operation
                if (success) {
                    fileManager.commitChangeSet();
                    
                    // Notify listeners
                    for (RepositoryListener listener : listeners) {
                    	try {
                    		listener.onPromote( item, originalStatus );
                    		
                    	} catch (Throwable t) {
                    		log.warn("Unexpected error during listener invocation.", t);
                    	}
                    }
                    
                } else {
                    try {
                        fileManager.rollbackChangeSet();
                    } catch (Throwable t) {
                        log.error("Error rolling back the current change set.", t);
                    }
                }
            }

        } else {
            ((RemoteRepository) item.getRepository()).promote(item);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#demote(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public void demote(RepositoryItem item) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
        LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS,
                item.getFilename(), item.getVersion());
        File contentFile = fileManager.getLibraryContentLocation(baseNS, item.getFilename(),
                item.getVersion());
        LibraryContentWrapper libraryContent = loadOtmLibraryContent(contentFile);
        TLLibraryStatus originalStatus = item.getStatus();
        TLLibraryStatus targetStatus = null;

        if (libraryMetadata.getState() != RepositoryState.MANAGED_UNLOCKED) {
            throw new RepositoryException(
                    "Unable to demote - the item is currently locked for editing.");
        }
        if (libraryMetadata.getStatus() == null) {
            throw new RepositoryException(
                    "Unable to demote - the item's status is not yet assigned.");
        }
        
        if (libraryContent.is16Library) {
            if (libraryMetadata.getStatus() == LibraryStatus.DRAFT) {
                throw new RepositoryException(
                        "Unable to demote - only user-defined libraries that are not in DRAFT status can be demoted.");
            }
        	
        } else {
            if (libraryMetadata.getStatus() != LibraryStatus.FINAL) {
                throw new RepositoryException(
                        "Unable to demote - only user-defined libraries in FINAL status can be demoted.");
            }
        }

        if (item.getRepository() == this) {
            boolean success = false;
            try {
                fileManager.startChangeSet();

                // Change the status of the library metadata and content
                TLLibraryStatus currentStatus = TLLibraryStatus.fromRepositoryStatus( libraryMetadata.getStatus() );
                
                targetStatus = libraryContent.is16Library ? currentStatus.previousStatus() : TLLibraryStatus.DRAFT;

                if (libraryContent != null) {
                    libraryContent.content.setStatus(targetStatus);
                }
                libraryMetadata.setStatus(targetStatus.toRepositoryStatus());
                libraryMetadata.setLastUpdated(XMLGregorianCalendarConverter
                        .toXMLGregorianCalendar(new Date()));

                if (libraryContent != null) {
                	saveOtmLibraryContent( libraryContent );
                }
                fileManager.saveLibraryMetadata(libraryMetadata);

                // Create the history entry for this update
                historyManager.addToHistory( item, new Date(), MessageFormat.format( REMARK_DEMOTE,
                		SchemaCompilerApplicationContext.getContext().getMessage(
                				targetStatus.toString(), null, Locale.getDefault() ) ) );

                if (!(item instanceof ProjectItem)) {
                    // Only required for non-ProjectItems; ProjectItem derives the status value from
                    // its library content
                    ((RepositoryItemImpl) item).setStatus(targetStatus);
                }
                log.info("Successfully demoted managed item '" + item.getFilename()
                        + "' to " + targetStatus + " status by " + fileManager.getCurrentUserId());
                success = true;

            } catch (Exception e) {
                throw new RepositoryException("Unable to demote the repository item: "
                        + item.getFilename(), e);

            } finally {
                // Commit or roll back the changes based on the result of the operation
                if (success) {
                    fileManager.commitChangeSet();
                    
                    // Notify listeners
                    for (RepositoryListener listener : listeners) {
                    	try {
                    		listener.onDemote( item, originalStatus );
                    		
                    	} catch (Throwable t) {
                    		log.warn("Unexpected error during listener invocation.", t);
                    	}
                    }
                    
                } else {
                    try {
                        fileManager.rollbackChangeSet();
                    } catch (Throwable t) {
                        log.error("Error rolling back the current change set.", t);
                    }
                }
            }

        } else {
            ((RemoteRepository) item.getRepository()).demote(item);
        }
    }
    
    /**
	 * @see org.opentravel.schemacompiler.repository.Repository#updateStatus(org.opentravel.schemacompiler.repository.RepositoryItem, org.opentravel.schemacompiler.model.TLLibraryStatus)
	 */
	@Override
	public void updateStatus(RepositoryItem item, TLLibraryStatus newStatus) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
        LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS,
                item.getFilename(), item.getVersion());
        File contentFile = fileManager.getLibraryContentLocation(baseNS, item.getFilename(),
                item.getVersion());
        TLLibraryStatus originalStatus = item.getStatus();

        if (libraryMetadata.getState() != RepositoryState.MANAGED_UNLOCKED) {
            throw new RepositoryException(
                    "Unable to update status - the item is currently locked for editing.");
        }
        if (newStatus == null) {
            throw new RepositoryException(
                    "Unable to update status - the new status cannot be null.");
        }
        
        if (item.getRepository() == this) {
            boolean success = false;
            try {
                fileManager.startChangeSet();

                // Change the status of the library metadata and content
                LibraryContentWrapper libraryContent = loadOtmLibraryContent(contentFile);

                if (libraryContent != null) {
                    libraryContent.content.setStatus(newStatus);
                }
                libraryMetadata.setStatus(newStatus.toRepositoryStatus());
                libraryMetadata.setLastUpdated(XMLGregorianCalendarConverter
                        .toXMLGregorianCalendar(new Date()));

                if (libraryContent != null) {
                	saveOtmLibraryContent( libraryContent );
                }
                fileManager.saveLibraryMetadata(libraryMetadata);

                if (!(item instanceof ProjectItem)) {
                    // Only required for non-ProjectItems; ProjectItem derives the status value from
                    // its library content
                    ((RepositoryItemImpl) item).setStatus(newStatus);
                }
                log.info("Successfully assigned managed item '" + item.getFilename()
                        + "' to " + newStatus + " status by " + fileManager.getCurrentUserId());
                success = true;

            } catch (Exception e) {
                throw new RepositoryException("Unable to the repository item's status: "
                        + item.getFilename(), e);

            } finally {
                // Commit or roll back the changes based on the result of the operation
                if (success) {
                    fileManager.commitChangeSet();
                    
                    // Notify listeners
                    for (RepositoryListener listener : listeners) {
                    	try {
                    		listener.onUpdateStatus( item, originalStatus );
                    		
                    	} catch (Throwable t) {
                    		log.warn("Unexpected error during listener invocation.", t);
                    	}
                    }
                    
                } else {
                    try {
                        fileManager.rollbackChangeSet();
                    } catch (Throwable t) {
                        log.error("Error rolling back the current change set.", t);
                    }
                }
            }

        } else {
            ((RemoteRepository) item.getRepository()).updateStatus(item, newStatus);
        }
	}

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#recalculateCrc(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public void recalculateCrc(RepositoryItem item) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
        LibraryInfoType libraryMetadata = fileManager.loadLibraryMetadata(baseNS,
                item.getFilename(), item.getVersion());
        File contentFile = fileManager.getLibraryContentLocation(baseNS, item.getFilename(),
                item.getVersion());

        if ((libraryMetadata.getStatus() == null) || (libraryMetadata.getStatus() == LibraryStatus.DRAFT)) {
            throw new RepositoryException(
                    "Unable to recalculate - CRC values can only be recalculated for user-defined libraries in non-DRAFT status.");
        }

        if (item.getRepository() == this) {
            boolean success = false;
            try {
                fileManager.startChangeSet();

                // Re-save the library content; this will force a recalculation of the CRC value
                LibraryContentWrapper libraryContent = loadOtmLibraryContent(contentFile);

                if (libraryContent != null) {
                    // Set the library's status - just in case it is out of sync with the meta-data record
                    libraryContent.content.setStatus(TLLibraryStatus.fromRepositoryStatus(libraryMetadata.getStatus()));
                	saveOtmLibraryContent( libraryContent );
                }

                libraryMetadata.setLastUpdated(XMLGregorianCalendarConverter
                        .toXMLGregorianCalendar(new Date()));
                fileManager.saveLibraryMetadata(libraryMetadata);

                // Create the history entry
                historyManager.addToHistory( item, new Date(), REMARK_CRC );

                log.info("Successfully recalculated CRC for item '" + item.getFilename() + "' by "
                        + fileManager.getCurrentUserId());
                success = true;

            } catch (Exception e) {
                throw new RepositoryException("Unable to recalculate the CRC for repository item: "
                        + item.getFilename(), e);

            } finally {
                // Commit or roll back the changes based on the result of the operation
                if (success) {
                    fileManager.commitChangeSet();
                    
                    // Notify listeners
                    for (RepositoryListener listener : listeners) {
                    	try {
                    		listener.onRecalculateCrc( item );
                    		
                    	} catch (Throwable t) {
                    		log.warn("Unexpected error during listener invocation.", t);
                    	}
                    }
                    
                } else {
                    try {
                        fileManager.rollbackChangeSet();
                    } catch (Throwable t) {
                        log.error("Error rolling back the current change set.", t);
                    }
                }
            }

        } else {
            ((RemoteRepository) item.getRepository()).recalculateCrc(item);
        }
    }

	/**
     * @see org.opentravel.schemacompiler.repository.Repository#delete(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public void delete(RepositoryItem item) throws RepositoryException {
        if (item.getRepository() == this) {
            String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
            boolean success = false;
            try {
                fileManager.startChangeSet();

                File metadataFile = fileManager.getLibraryMetadataLocation(baseNS,
                        item.getFilename(), item.getVersion());
                File contentFile = fileManager.getLibraryContentLocation(baseNS,
                        item.getFilename(), item.getVersion());

                fileManager.addToChangeSet(metadataFile);
                fileManager.addToChangeSet(contentFile);

                if (!metadataFile.delete()) {
                    throw new RepositoryException("Unable to delete library meta-data file: "
                            + metadataFile.getAbsolutePath());
                }
                if (!contentFile.delete()) {
                    throw new RepositoryException("Unable to delete library content file: "
                            + contentFile.getAbsolutePath());
                }
                
                historyManager.deleteHistory( item );
                
                log.info("Successfully deleted managed item '" + item.getFilename() + "', by "
                        + fileManager.getCurrentUserId());
                success = true;

            } finally {
                // Commit or roll back the changes based on the result of the operation
                if (success) {
                    fileManager.commitChangeSet();
                    
                    // Notify listeners
                    for (RepositoryListener listener : listeners) {
                    	try {
                    		listener.onDelete( item );
                    		
                    	} catch (Throwable t) {
                    		log.warn("Unexpected error during listener invocation.", t);
                    	}
                    }
                    
                } else {
                    try {
                        fileManager.rollbackChangeSet();
                    } catch (Throwable t) {
                        log.error("Error rolling back the current change set.", t);
                    }
                }
            }

        } else {
            ((RemoteRepository) item.getRepository()).delete(item);
        }
    }

    /**
     * Returns the URL location of the repository item's content.
     * 
     * @param item
     *            the repository item whose location is to be returned
     * @return URL
     * @throws RepositoryException
     *             thrown if the content is not available
     */
    public URL getContentLocation(RepositoryItem item) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
        URL contentLocation;

        if (item.getState() == RepositoryItemState.MANAGED_WIP) {
            contentLocation = URLUtils.toURL(fileManager.getLibraryWIPContentLocation(baseNS,
                    item.getFilename()));

        } else {
            Repository repository = item.getRepository();
        	File itemFile = fileManager.getLibraryContentLocation(baseNS,
        			item.getFilename(), item.getVersion());
            
            if ((repository instanceof RemoteRepository) && !itemFile.exists()) {
                ((RemoteRepository) repository).downloadContent(item, false);
            }
            contentLocation = URLUtils.toURL( itemFile );
        }
        return contentLocation;
    }
    
    /**
	 * @see org.opentravel.schemacompiler.repository.Repository#getHistoricalContentSource(org.opentravel.schemacompiler.repository.RepositoryItem, java.util.Date)
	 */
	@Override
	public LibraryInputSource<InputStream> getHistoricalContentSource(RepositoryItem item, Date effectiveDate)
			throws RepositoryException {
		LibraryInputSource<InputStream> contentSource;
		
		if (effectiveDate == null) {
			contentSource = new LibraryStreamInputSource( getContentLocation( item ) );
			
		} else {
			Repository repository = item.getRepository();
			
			if (repository == this) {
				contentSource = new LibraryStreamInputSource(
						historyManager.getHistoricalContent( item, effectiveDate ) );
				
			} else {
				contentSource = ((RemoteRepository) repository).getHistoricalContentSource( item, effectiveDate );
			}
		}
		return contentSource;
	}

	/**
     * If the given <code>RepositoryItem</code> is owned by a remote repository, the local
     * repository's copy is updated with the latest available content. If the item is owned by the
     * local repository, this method has no effect. This update is performed regardless of the
     * update policy for the repository that owns and manages the item.
     * 
     * <p>This method will return true if the item's local copy was replaced by new content from
     * the remote repository.
     * 
     * @param item
     *            the repository item to refresh
     * @throws RepositoryException
     *             thrown if the file content cannot be locked by the current user
     */
    public boolean refreshLocalCopy(RepositoryItem item) throws RepositoryException {
        Repository repository = item.getRepository();
        boolean isRefreshed = false;
        
        if (repository instanceof RemoteRepository) {
        	resetDownloadCache();
            isRefreshed = ((RemoteRepository) repository).downloadContent(item, true);
        }
        return isRefreshed;
    }

    /**
     * Saves the configuration settings for the local repository.
     * 
     * NOTE: Although the repository metadata will be saved by this method, the active change set
     * will not be committed. It is the responsibility of the calling method to commit or roll back
     * the change set depending on the success of the end-to-end transaction.
     * 
     * @throws RepositoryException
     *             thrown if the local repository's configuration settings cannot be updated
     */
    public void saveLocalRepositoryMetadata() throws RepositoryException {
        List<String> rootNamespaces = this.rootNamespaces;
        boolean success = false;

        // Get the list of root namespaces from the existing file if the list has not yet been
        // initialized. They are not accessible from local repository fields because a default
        // namespace is always published for the local repository.
        if (rootNamespaces == null) {
            try {
                RepositoryInfoType fileMetadata = fileManager.loadRepositoryMetadata();
                rootNamespaces = fileMetadata.getRootNamespace();

            } catch (RepositoryException e) {
                // Not an error if the file does not yet exist - just use an empty list
                rootNamespaces = new ArrayList<String>();
            }
        }

        try {
            RepositoryInfoType repositoryMetadata = new RepositoryInfoType();

            repositoryMetadata.setID(localRepositoryId);
            repositoryMetadata.setDisplayName(localRepositoryDisplayName);
            repositoryMetadata.getRootNamespace().addAll(rootNamespaces);
            repositoryMetadata.setRemoteRepositories(new RemoteRepositoriesType());

            for (RemoteRepositoryClient remoteRepository : remoteRepositories) {
                RemoteRepositoryType jaxbRepository = new RemoteRepositoryType();

                jaxbRepository.setID(remoteRepository.getId());
                jaxbRepository.setDisplayName(remoteRepository.getDisplayName());
                jaxbRepository.setEndpointUrl(remoteRepository.getEndpointUrl());
                jaxbRepository.setRefreshPolicy(remoteRepository.getRefreshPolicy());
                jaxbRepository.getRootNamespace().addAll(remoteRepository.listRootNamespaces());
                jaxbRepository.setUserID(remoteRepository.getUserId());
                jaxbRepository.setPassword(ENCRYPTED_PASSWORD_PREFIX
                        + remoteRepository.getEncryptedPassword());
                repositoryMetadata.getRemoteRepositories().getRemoteRepository()
                        .add(jaxbRepository);
            }
            fileManager.saveRepositoryMetadata(repositoryMetadata);
            success = true;

        } finally {
            // If an error occurred, refresh all local settings to make sure we are still in-sync
            // with the local file system
            try {
                if (!success) {
                    refreshLocalRepositoryInfo(true);
                }
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Initializes the settings for the local OTA2.0 repository. If a local repository does not yet
     * exist, one is created automatically.
     * 
     * @throws RepositoryException
     *             thrown if the local repository cannot be accessed
     */
    private void initializeLocalRepositoryInfo() throws RepositoryException {
        // If a repository does not already exist, create one automatically
        if (fileManager.getRepositoryMetadataLastUpdated() == null) {
            String newRepositoryId;

            try {
                newRepositoryId = InetAddress.getLocalHost().getHostName();

            } catch (UnknownHostException e) { // Not an error - just use 'localhost'
                newRepositoryId = "localhost";
            }
            createLocalRepository(newRepositoryId, "Local OTA2.0 Repository");
        }

        // Populate this singleton instance with the repository's meta-data properties
        RepositoryInfoType repositoryInfo = fileManager.loadRepositoryMetadata();

        this.localRepositoryId = repositoryInfo.getID();
        this.localRepositoryDisplayName = repositoryInfo.getDisplayName();
        this.rootNamespaces = repositoryInfo.getRootNamespace();
        this.lastUpdatedDate = new Date();

        for (RemoteRepositoryType jaxbRemoteRepository : repositoryInfo.getRemoteRepositories()
                .getRemoteRepository()) {
            RemoteRepositoryClient remoteRepositoryClient = new RemoteRepositoryClient(this);

            try {
                copyRemoteRepositorySettings(jaxbRemoteRepository, remoteRepositoryClient);
                remoteRepositoryClient.refreshRepositoryMetadata();

            } catch (RepositoryException e) {
                // Ignore and use locally-cached data
            }
            this.remoteRepositories.add(remoteRepositoryClient);
        }
    }

    /**
     * Checks the last-updated date of the local repository's meta-data and reloads it if required.
     * 
     * @param forceRefresh
     *            indicates whether the metadata should be refreshed regardless of the file's
     *            last-upated timestamp
     */
    private void refreshLocalRepositoryInfo(boolean forceRefresh) {
        try {
            Date actualLastUpdated = fileManager.getRepositoryMetadataLastUpdated();

            if (actualLastUpdated == null) {
                log.error("OTA2.0 repository not found at location: "
                        + fileManager.getRepositoryLocation().getAbsolutePath());

            } else if (forceRefresh || (lastUpdatedDate == null)
                    || actualLastUpdated.after(lastUpdatedDate)) {
                RepositoryInfoType repositoryInfo = fileManager.loadRepositoryMetadata();
                Map<String, RemoteRepositoryClient> oldRepositories = new HashMap<String, RemoteRepositoryClient>();
                Map<String, RemoteRepositoryType> newRepositories = new HashMap<String, RemoteRepositoryType>();

                // Build a catalog of all the new and existing repositories, indexed by ID
                for (RemoteRepositoryClient oldRepository : remoteRepositories) {
                    oldRepositories.put(oldRepository.getId(), oldRepository);
                }
                for (RemoteRepositoryType newRepository : repositoryInfo.getRemoteRepositories()
                        .getRemoteRepository()) {
                    newRepositories.put(newRepository.getID(), newRepository);
                }

                // Refresh the remote repository content by updating, adding, and deleting as needed
                Iterator<RemoteRepositoryClient> iterator = remoteRepositories.iterator();

                while (iterator.hasNext()) {
                    RemoteRepositoryClient oldRepository = iterator.next();

                    if (!newRepositories.containsKey(oldRepository.getId())) {
                        iterator.remove();
                    }
                }
                for (RemoteRepositoryType newRepository : newRepositories.values()) {
                    RemoteRepositoryClient oldRepository = oldRepositories.get(newRepository
                            .getID());

                    if (oldRepository == null) {
                        oldRepository = new RemoteRepositoryClient(this);
                        remoteRepositories.add(oldRepository);
                    }
                    try {
                        copyRemoteRepositorySettings(newRepository, oldRepository);
                        oldRepository.refreshRepositoryMetadata();

                    } catch (RepositoryException e) {
                        // Ignore and use locally-cached data
                    }
                }

                // Update the remaining meta-data fields
                this.localRepositoryId = repositoryInfo.getID();
                this.localRepositoryDisplayName = repositoryInfo.getDisplayName();
                this.rootNamespaces = repositoryInfo.getRootNamespace();
                this.lastUpdatedDate = new Date();
            }

        } catch (RepositoryException e) {
            log.warn("Unable to refresh contents of local OTA2.0 repository. Reason: "
                    + ExceptionUtils.getExceptionMessage(e));
        }
    }

    /**
     * Copies all settings from the JAXB remote repository instance to the object used to represent
     * that repository within the schema compiler's framework.
     * 
     * @param jaxbRemoteRepository
     *            the JAXB object containing the remote repository configuration settings
     * @param remoteRepositoryImpl
     *            the schema compiler's remote repository object instance
     */
    private void copyRemoteRepositorySettings(RemoteRepositoryType jaxbRemoteRepository,
            RemoteRepositoryClient remoteRepository) {
        remoteRepository.setId(jaxbRemoteRepository.getID());
        remoteRepository.setDisplayName(jaxbRemoteRepository.getDisplayName());
        remoteRepository.setEndpointUrl(jaxbRemoteRepository.getEndpointUrl());
        remoteRepository.setRootNamespaces(jaxbRemoteRepository.getRootNamespace());
        remoteRepository.setRefreshPolicy(jaxbRemoteRepository.getRefreshPolicy());
        remoteRepository.setUserId(jaxbRemoteRepository.getUserID());

        // If the password is already encrypted, use the value as-is; otherwise encrypt it now
        String jaxbPassword = jaxbRemoteRepository.getPassword();

        if (jaxbPassword != null) {
            String encryptedPassword;

            if (jaxbPassword.startsWith(ENCRYPTED_PASSWORD_PREFIX)) {
                encryptedPassword = jaxbPassword.substring(ENCRYPTED_PASSWORD_PREFIX.length());

            } else {
                encryptedPassword = PasswordHelper.encrypt(jaxbPassword);
            }
            remoteRepository.setEncryptedPassword(encryptedPassword);

        } else {
            remoteRepository.setEncryptedPassword(null);
        }
    }

    /**
     * Attempts to load the content of the specified file as an OTM library. If any non-validation
     * exceptions occur during the load, the file will be assumed to be a non-OTM file. In such,
     * cases this method will return null instead of throwing an exception.
     * 
     * @param libraryFile  the library file load
     * @return LibraryContentWrapper
     */
    private LibraryContentWrapper loadOtmLibraryContent(File libraryFile) {
    	boolean is16Library = false;
        TLLibrary library = null;

        try {
            if ((libraryFile != null) && libraryFile.exists()
                    && !libraryFile.getName().toLowerCase().endsWith(".xsd")) {
                LibraryModuleLoader<InputStream> loader = new MultiVersionLibraryModuleLoader();
                LibraryModuleInfo<Object> moduleInfo = loader.loadLibrary(
                        new LibraryStreamInputSource(libraryFile), new ValidationFindings());
                Object jaxbLibrary = moduleInfo.getJaxbArtifact();
                
                if (jaxbLibrary != null) {
                    TransformerFactory<DefaultTransformerContext> transformerFactory = TransformerFactory
                            .getInstance(
                                    SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY,
                                    new DefaultTransformerContext());
                    ObjectTransformer<Object, TLLibrary, DefaultTransformerContext> transformer = transformerFactory
                            .getTransformer(jaxbLibrary, TLLibrary.class);

                    library = transformer.transform(jaxbLibrary);
                    library.setLibraryUrl(URLUtils.toURL(libraryFile));
                    is16Library = (jaxbLibrary instanceof org.opentravel.ns.ota2.librarymodel_v01_06.Library);
                }
            }
        } catch (Exception e) {
            // No action - method will return a null library
        }
        return new LibraryContentWrapper( library, libraryFile, is16Library );
    }
    
    /**
     * Saves the OTM library content using the original file format from which it was loaded.
     * 
     * @param libraryContent  the library content to be saved
     * @throws RepositoryException  thrown if the library file cannot be added to the current change set
     * @throws LibrarySaveException  thrown if an error occurs during the save operation
     */
    private void saveOtmLibraryContent(LibraryContentWrapper libraryContent)
    			throws RepositoryException, LibrarySaveException {
        LibraryModelSaver modelSaver = new LibraryModelSaver();
        
        if (libraryContent.is16Library) {
        	modelSaver.setSaveHandler( new Library16FileSaveHandler() );
        } else {
        	modelSaver.setSaveHandler( new Library15FileSaveHandler() );
        }
        fileManager.addToChangeSet( libraryContent.contentFile );
        modelSaver.getSaveHandler().setCreateBackupFile( false );
        modelSaver.saveLibrary( libraryContent.content );
    }
    
    /**
     * Wrapper class that contains the <code>TLLibrary</code> content as well as an
     * indicator of whether the library was originally saved in the 1.6 format.
     */
    private class LibraryContentWrapper {
    	
    	public TLLibrary content;
    	public File contentFile;
    	public boolean is16Library;
    	
    	/**
    	 * Full constructor.
    	 * 
    	 * @param content  the OTM library content
    	 * @param contentFile  the original file from which the library content was loaded
    	 * @param is16Library  flag indicating whether the library was originally saved in the 1.6 format
    	 */
    	public LibraryContentWrapper(TLLibrary content, File contentFile, boolean is16Library) {
    		this.content = content;
    		this.contentFile = contentFile;
    		this.is16Library = is16Library;
    	}
    	
    }
    
}
