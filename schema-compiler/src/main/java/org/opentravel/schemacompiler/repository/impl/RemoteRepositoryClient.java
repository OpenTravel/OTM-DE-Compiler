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
package org.opentravel.schemacompiler.repository.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ListItemsRQType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.NamespaceListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RefreshPolicy;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryItemIdentityType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermissionType;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.repository.RepositoryUnavailableException;
import org.opentravel.schemacompiler.security.PasswordHelper;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.xml.XMLGregorianCalendarConverter;

/**
 * Represents the remote repositories that are accessible from the local environment.
 * 
 * @author S. Livezey
 */
public class RemoteRepositoryClient implements RemoteRepository {

    private static final String SERVICE_CONTEXT = "/service";
    private static final String REPOSITORY_METADATA_ENDPOINT = SERVICE_CONTEXT + "/repository-metadata";
    private static final String ALL_NAMSPACES_ENDPOINT = SERVICE_CONTEXT + "/all-namespaces";
    private static final String BASE_NAMSPACES_ENDPOINT = SERVICE_CONTEXT + "/base-namespaces";
    private static final String NAMESPACE_CHILDREN_ENDPOINT = SERVICE_CONTEXT + "/namespace-children";
    private static final String LIST_ITEMS_ENDPOINT = SERVICE_CONTEXT + "/list-items";
    private static final String VERSION_HISTORY_ENDPOINT = SERVICE_CONTEXT + "/version-history";
    private static final String SEARCH_ENDPOINT = SERVICE_CONTEXT + "/search";
    private static final String CREATE_ROOT_NAMESPACE_ENDPOINT = SERVICE_CONTEXT + "/create-root-namespace";
    private static final String DELETE_ROOT_NAMESPACE_ENDPOINT = SERVICE_CONTEXT + "/delete-root-namespace";
    private static final String CREATE_NAMESPACE_ENDPOINT = SERVICE_CONTEXT + "/create-namespace";
    private static final String DELETE_NAMESPACE_ENDPOINT = SERVICE_CONTEXT + "/delete-namespace";
    private static final String PUBLISH_ENDPOINT = SERVICE_CONTEXT + "/publish";
    private static final String LOCK_ENDPOINT = SERVICE_CONTEXT + "/lock";
    private static final String UNLOCK_ENDPOINT = SERVICE_CONTEXT + "/unlock";
    private static final String COMMIT_ENDPOINT = SERVICE_CONTEXT + "/commit";
    private static final String PROMOTE_ENDPOINT = SERVICE_CONTEXT + "/promote";
    private static final String DEMOTE_ENDPOINT = SERVICE_CONTEXT + "/demote";
    private static final String RECALCULATE_CRC_ENDPOINT = SERVICE_CONTEXT + "/recalculate-crc";
    private static final String DELETE_ENDPOINT = SERVICE_CONTEXT + "/delete";
    private static final String REPOSITORY_ITEM_METADATA_ENDPOINT = SERVICE_CONTEXT + "/metadata";
    private static final String REPOSITORY_ITEM_CONTENT_ENDPOINT = SERVICE_CONTEXT + "/content";
    private static final String USER_AUTHORIZATION_ENDPOINT = SERVICE_CONTEXT + "/user-authorization";

    private static final int HTTP_RESPONSE_STATUS_OK = 200;

    private static final DateFormat dateOnlyFormat = new SimpleDateFormat("dd-MM-yyyy");

    private static Log log = LogFactory.getLog(RemoteRepositoryClient.class);
    protected static ObjectFactory objectFactory = new ObjectFactory();
    
    private RepositoryManager manager;
    private String id;
    private String displayName;
    private String endpointUrl;
    private List<String> rootNamespaces = new ArrayList<String>();
    private RefreshPolicy refreshPolicy;
    private String userId;
    private String encryptedPassword;

    /**
     * Initializes this instance with a handle to the <code>RepositoryManager</code> that controls
     * access to this <code>RemoteRepository</code>.
     * 
     * @param manager
     *            the owning repository manager instance
     */
    public RemoteRepositoryClient(RepositoryManager manager) {
        this.manager = manager;
    }

    private static HttpClient createHttpClient() {
		return HttpClientBuilder.create().useSystemProperties()
                .setDefaultCredentialsProvider(new NTLMSystemCredentialsProvider()).build();
    }

    private HttpResponse executeWithAuthentication(HttpUriRequest request)
            throws ClientProtocolException, IOException {
        return createHttpClient().execute(request, createHttpContext());
    }

    private static HttpResponse execute(HttpUriRequest request) throws ClientProtocolException,
            IOException {
        return createHttpClient().execute(request);
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getManager()
     */
    @Override
    public RepositoryManager getManager() {
        return manager;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Assigns the ID of the remote repository.
     * 
     * @param id
     *            the repository ID to assign
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Assigns the display name of the remote repository.
     * 
     * @param displayName
     *            the display name value to assign
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.RemoteRepository#getRefreshPolicy()
     */
    @Override
    public RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    /**
     * Assigns the value of the 'refreshPolicy' field.
     * 
     * @param refreshPolicy
     *            the field value to assign
     */
    public void setRefreshPolicy(RefreshPolicy refreshPolicy) {
        this.refreshPolicy = refreshPolicy;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.RemoteRepository#getEndpointUrl()
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }

    /**
     * Assigns the web service endpoint URL of the remote repository.
     * 
     * @param endpointUrl
     *            the endpoint URL to assign
     */
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    /**
     * Returns the user ID credential for the remote repository's web service.
     * 
     * @return String
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Assigns the user ID credential for the remote repository's web service.
     * 
     * @param userId
     *            the user ID value to assign
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the encryptedPassword credential for the remote repository's web service.
     * 
     * @return String
     */
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    /**
     * Assigns the encryptedPassword credential for the remote repository's web service.
     * 
     * @param encryptedPassword
     *            the encryptedPassword value to assign
     */
    public void setEncryptedPassword(String password) {
        this.encryptedPassword = password;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.RemoteRepository#refreshRepositoryMetadata()
     */
    @Override
    public void refreshRepositoryMetadata() throws RepositoryException {
        RepositoryInfoType updatedMetadata = getRepositoryMetadata(endpointUrl);

        if (updatedMetadata != null) {
            this.id = updatedMetadata.getID();
            this.displayName = updatedMetadata.getDisplayName();
            setRootNamespaces(updatedMetadata.getRootNamespace());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#listRootNamespaces()
     */
    @Override
    public List<String> listRootNamespaces() throws RepositoryException {
        return rootNamespaces;
    }

    /**
     * Assigns the list of root namespaces that are managed by the remote repository.
     * 
     * @param rootNamespaces
     */
    public synchronized void setRootNamespaces(List<String> rootNamespaces) {
        this.rootNamespaces.clear();

        if (rootNamespaces != null) {
            this.rootNamespaces.addAll(rootNamespaces);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#listNamespaceChildren(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> listNamespaceChildren(String baseNamespace) throws RepositoryException {
        try {
            String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
            HttpGet request = newGetRequest(NAMESPACE_CHILDREN_ENDPOINT, new HttpGetParam(
                    "baseNamespace", baseNS));
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<NamespaceListType> jaxbElement = (JAXBElement<NamespaceListType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());
            List<String> nsList = new ArrayList<String>();

            nsList.addAll(jaxbElement.getValue().getNamespace());
            return nsList;

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#listBaseNamespaces()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> listBaseNamespaces() throws RepositoryException {
        try {
            HttpGet request = newGetRequest(BASE_NAMSPACES_ENDPOINT);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<NamespaceListType> jaxbElement = (JAXBElement<NamespaceListType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());
            List<String> nsList = new ArrayList<String>();

            nsList.addAll(jaxbElement.getValue().getNamespace());
            return nsList;

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the service response is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#listAllNamespaces()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> listAllNamespaces() throws RepositoryException {
        try {
            HttpGet request = newGetRequest(ALL_NAMSPACES_ENDPOINT);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<NamespaceListType> jaxbElement = (JAXBElement<NamespaceListType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());
            List<String> nsList = new ArrayList<String>();

            nsList.addAll(jaxbElement.getValue().getNamespace());
            return nsList;

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the service response is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#listItems(java.lang.String, boolean,
     *      boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<RepositoryItem> listItems(String baseNamespace, boolean latestVersionsOnly,
            boolean includeDraftVersions) throws RepositoryException {
        try {
            String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
            HttpPost request = newPostRequest(LIST_ITEMS_ENDPOINT);
            Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext().createMarshaller();
            ListItemsRQType listItemsRQ = new ListItemsRQType();
            StringWriter xmlWriter = new StringWriter();

            listItemsRQ.setNamespace(baseNS);
            listItemsRQ.setLatestVersionOnly(latestVersionsOnly);
            listItemsRQ.setIncludeDraft(includeDraftVersions);
            marshaller.marshal(objectFactory.createListItemsRQ(listItemsRQ), xmlWriter);
            request.setEntity(new StringEntity(xmlWriter.toString(), ContentType.TEXT_XML));

            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<LibraryInfoListType> jaxbElement = (JAXBElement<LibraryInfoListType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());
            List<RepositoryItem> itemList = new ArrayList<RepositoryItem>();

            for (LibraryInfoType itemMetadata : jaxbElement.getValue().getLibraryInfo()) {
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(manager,
                        itemMetadata);

                RepositoryUtils.checkItemState(item, manager);
                itemList.add(item);
            }
            return itemList;

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the service response is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#search(java.lang.String, boolean,
     *      boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<RepositoryItem> search(String freeTextQuery, boolean latestVersionsOnly,
            boolean includeDraftVersions) throws RepositoryException {
        try {
            HttpGet request = newGetRequest(SEARCH_ENDPOINT, new HttpGetParam("query",
                    freeTextQuery), new HttpGetParam("latestVersion", latestVersionsOnly + ""),
                    new HttpGetParam("includeDraft", includeDraftVersions + ""));
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<LibraryInfoListType> jaxbElement = (JAXBElement<LibraryInfoListType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());
            List<RepositoryItem> itemList = new ArrayList<RepositoryItem>();

            for (LibraryInfoType itemMetadata : jaxbElement.getValue().getLibraryInfo()) {
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(manager,
                        itemMetadata);

                RepositoryUtils.checkItemState(item, manager);
                itemList.add(item);
            }
            return itemList;

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getVersionHistory(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<RepositoryItem> getVersionHistory(RepositoryItem item) throws RepositoryException {
        try {
            validateRepositoryItem(item);

            // Build the HTTP request for the remote service
            RepositoryItemIdentityType itemIdentity = createItemIdentity(item);
            Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext().createMarshaller();
            HttpPost request = newPostRequest(VERSION_HISTORY_ENDPOINT);
            StringWriter xmlWriter = new StringWriter();

            marshaller.marshal(objectFactory.createRepositoryItemIdentity(itemIdentity), xmlWriter);
            request.setEntity(new StringEntity(xmlWriter.toString(), ContentType.TEXT_XML));

            // Send the web service request and unmarshall the updated meta-data from the response
            log.info("Sending version history request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Version history response received - Status OK");

            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<LibraryInfoListType> jaxbElement = (JAXBElement<LibraryInfoListType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());
            List<RepositoryItem> itemList = new ArrayList<RepositoryItem>();

            for (LibraryInfoType itemMetadata : jaxbElement.getValue().getLibraryInfo()) {
                RepositoryItemImpl itemVersion = RepositoryUtils.createRepositoryItem(manager,
                        itemMetadata);

                RepositoryUtils.checkItemState(itemVersion, manager);
                itemList.add(itemVersion);
            }
            return itemList;

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getRepositoryItem(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public RepositoryItem getRepositoryItem(String baseNamespace, String filename,
            String versionIdentifier) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);

        downloadContent(baseNS, filename, versionIdentifier, false);

        LibraryInfoType libraryInfo = manager.getFileManager().loadLibraryMetadata(baseNS,
                filename, versionIdentifier);
        return newRepositoryItem(libraryInfo);
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getRepositoryItem(java.lang.String)
     */
    @Override
    public RepositoryItem getRepositoryItem(String itemUri) throws RepositoryException,
            URISyntaxException {
        return getRepositoryItem(itemUri, null);
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#getRepositoryItem(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public RepositoryItem getRepositoryItem(String itemUri, String itemNamespace)
            throws RepositoryException, URISyntaxException {
        URI uri = RepositoryUtils.toRepositoryItemUri(itemUri);
        RepositoryItem item;

        if ((uri.getAuthority() == null) || !uri.getAuthority().equals(id)) {
            throw new RepositoryException(
                    "Unable to retrieve the requested item becuase it does not belong to this repository.");
        }
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

            downloadContent(baseNS, uriParts[2], versionIdentifier, false);

            LibraryInfoType libraryMetadata = manager.getFileManager().loadLibraryMetadata(baseNS,
                    uriParts[2], versionIdentifier);

            if ((libraryMetadata == null)
                    || !libraryMetadata.getOwningRepository().equals(uriParts[0])) {
                throw new RepositoryException("Resource not found in repository: " + uriParts[0]);
            }
            item = newRepositoryItem(libraryMetadata);

        } catch (VersionSchemeException e) {
            throw new RepositoryException("Unknown version scheme specified by URI: "
                    + versionScheme);
        }
        return item;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.RemoteRepository#getUserAuthorization(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public RepositoryPermission getUserAuthorization(String baseNamespace)
            throws RepositoryException {
        try {
            String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
            HttpGet request = newGetRequest(USER_AUTHORIZATION_ENDPOINT, new HttpGetParam(
                    "baseNamespace", baseNS));

            // Send the web service request and check the response
            log.info("Sending user-authorization request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<RepositoryPermissionType> jaxbElement = (JAXBElement<RepositoryPermissionType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());

            log.info("User-authorization response received - Status OK");
            return jaxbElement.getValue().getRepositoryPermission();

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the service response is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#createRootNamespace(java.lang.String)
     */
    @Override
    public void createRootNamespace(String rootNamespace) throws RepositoryException {
        try {
            String rootNS = RepositoryNamespaceUtils.normalizeUri(rootNamespace);
            HttpGet request = newGetRequest(CREATE_ROOT_NAMESPACE_ENDPOINT, new HttpGetParam(
                    "rootNamespace", rootNS));

            // Send the web service request and check the response
            log.info("Sending create-root-namespace request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            refreshRepositoryMetadata();
            log.info("Create-root-namespace response received - Status OK");

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#deleteRootNamespace(java.lang.String)
     */
    @Override
    public void deleteRootNamespace(String rootNamespace) throws RepositoryException {
        try {
            String rootNS = RepositoryNamespaceUtils.normalizeUri(rootNamespace);
            HttpGet request = newGetRequest(DELETE_ROOT_NAMESPACE_ENDPOINT, new HttpGetParam(
                    "rootNamespace", rootNS));

            // Send the web service request and check the response
            log.info("Sending delete-root-namespace request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            refreshRepositoryMetadata();
            log.info("Delete-root-namespace response received - Status OK");

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#createNamespace(java.lang.String)
     */
    @Override
    public void createNamespace(String baseNamespace) throws RepositoryException {
        try {
            String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
            HttpGet request = newGetRequest(CREATE_NAMESPACE_ENDPOINT, new HttpGetParam(
                    "baseNamespace", baseNS));

            // Send the web service request and check the response
            log.info("Sending create-namespace request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Create-namespace response received - Status OK");

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#deleteNamespace(java.lang.String)
     */
    @Override
    public void deleteNamespace(String baseNamespace) throws RepositoryException {
        try {
            String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
            HttpGet request = newGetRequest(DELETE_NAMESPACE_ENDPOINT, new HttpGetParam(
                    "baseNamespace", baseNS));

            // Send the web service request and check the response
            log.info("Sending delete-namespace request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Delete-namespace response received - Status OK");

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
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
        try {
            // Build a repository item to represent the content that we are attempting to publish
            String targetNS = RepositoryNamespaceUtils.normalizeUri(namespace);
            RepositoryItemImpl item = new RepositoryItemImpl();
            String baseNamespace = targetNS;

            if (versionScheme != null) {
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                        versionScheme);
                baseNamespace = vScheme.getBaseNamespace(targetNS);
            }

            item.setRepository(this);
            item.setNamespace(targetNS);
            item.setBaseNamespace(baseNamespace);
            item.setFilename(filename);
            item.setVersion(versionIdentifier);
            item.setStatus(initialStatus);
            item.setState(RepositoryItemState.MANAGED_UNLOCKED);

            // Invoke the remote web service call to perform the publication
            HttpPost postRequest = newPostRequest(PUBLISH_ENDPOINT);
            MultipartEntityBuilder mpEntity = MultipartEntityBuilder.create();

            if (versionScheme != null) {
                mpEntity.addTextBody("versionScheme", versionScheme);
            }
            mpEntity.addBinaryBody("fileContent", toByteArray(unmanagedContent),
                    ContentType.DEFAULT_BINARY, filename);
            mpEntity.addTextBody("namespace", targetNS);
            mpEntity.addTextBody("libraryName", libraryName);
            mpEntity.addTextBody("version", versionIdentifier);
            mpEntity.addTextBody("status", initialStatus.toRepositoryStatus().toString());
            postRequest.setEntity(mpEntity.build());

            log.info("Sending publish request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(postRequest);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Publish response received - Status OK");
            return item;

        } catch (VersionSchemeException e) {
            throw new RepositoryException(e.getMessage(), e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);

        } finally {
            try {
                if (unmanagedContent != null)
                    unmanagedContent.close();
            } catch (Throwable t) {
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#commit(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public void commit(RepositoryItem item) throws RepositoryException {
        InputStream wipContent = null;
        try {
            validateRepositoryItem(item);

            // Obtain a file stream for the item's WIP content
            File wipFile = manager.getFileManager().getLibraryWIPContentLocation(
                    item.getBaseNamespace(), item.getFilename());

            if (!wipFile.exists()) {
                throw new RepositoryException("The work-in-process file does not exist: "
                        + item.getFilename());
            }
            wipContent = new FileInputStream(wipFile);

            // Build the HTTP request for the remote service
            RepositoryItemIdentityType itemIdentity = createItemIdentity(item);
            Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext().createMarshaller();
            HttpPost request = newPostRequest(COMMIT_ENDPOINT);
            MultipartEntityBuilder mpEntity = MultipartEntityBuilder.create();
            StringWriter xmlWriter = new StringWriter();

            marshaller.marshal(objectFactory.createRepositoryItemIdentity(itemIdentity), xmlWriter);
            mpEntity.addTextBody("item", xmlWriter.toString(), ContentType.TEXT_XML);
            mpEntity.addBinaryBody("fileContent", toByteArray(wipContent),
                    ContentType.DEFAULT_BINARY, item.getFilename());

            // mpEntity.addPart( "fileContent", new InputStreamBody(wipContent, item.getFilename())
            // );

            request.setEntity(mpEntity.build());

            // Send the web service request and check the response
            log.info("Sending commit request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Commit response received - Status OK");

            // Update the local cache with the content we just sent to the remote web service
            downloadContent(item, true);

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);

        } finally {
            try {
                if (wipContent != null)
                    wipContent.close();
            } catch (Throwable t) {
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#lock(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void lock(RepositoryItem item) throws RepositoryException {
        boolean success = false;
        try {
            validateRepositoryItem(item);
            manager.getFileManager().startChangeSet();

            // Build the HTTP request for the remote service
            RepositoryItemIdentityType itemIdentity = createItemIdentity(item);
            Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext().createMarshaller();
            HttpPost request = newPostRequest(LOCK_ENDPOINT);
            StringWriter xmlWriter = new StringWriter();

            marshaller.marshal(objectFactory.createRepositoryItemIdentity(itemIdentity), xmlWriter);
            request.setEntity(new StringEntity(xmlWriter.toString(), ContentType.TEXT_XML));

            // Send the web service request and unmarshall the updated meta-data from the response
            log.info("Sending lock request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Lock response received - Status OK");

            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<LibraryInfoType> jaxbElement = (JAXBElement<LibraryInfoType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());

            // Update the local cache with the content we just received from the remote web service
            manager.getFileManager().saveLibraryMetadata(jaxbElement.getValue());

            // Update the local repository item with the latest state information
            ((RepositoryItemImpl) item).setState(RepositoryItemState.MANAGED_WIP);
            ((RepositoryItemImpl) item).setLockedByUser(userId);
            success = true;

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);

        } finally {
            // Commit or roll back the changes based on the result of the operation
            if (success) {
                manager.getFileManager().commitChangeSet();
            } else {
                try {
                    manager.getFileManager().rollbackChangeSet();
                } catch (Throwable t) {
                    log.error("Error rolling back the current change set.", t);
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#unlock(org.opentravel.schemacompiler.repository.RepositoryItem,
     *      boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void unlock(RepositoryItem item, boolean commitWIP) throws RepositoryException {
        InputStream wipContent = null;
        boolean success = false;
        try {
            validateRepositoryItem(item);
            manager.getFileManager().startChangeSet();

            // Build the HTTP request for the remote service
            RepositoryItemIdentityType itemIdentity = createItemIdentity(item);
            Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext().createMarshaller();
            HttpPost request = newPostRequest(UNLOCK_ENDPOINT);

            MultipartEntityBuilder mpEntity = MultipartEntityBuilder.create();
            StringWriter xmlWriter = new StringWriter();

            if (commitWIP) {
                File wipFile = manager.getFileManager().getLibraryWIPContentLocation(
                        item.getBaseNamespace(), item.getFilename());

                if (!wipFile.exists()) {
                    throw new RepositoryException("The work-in-process file does not exist: "
                            + item.getFilename());
                }
                wipContent = new FileInputStream(wipFile);
                mpEntity.addBinaryBody("fileContent", toByteArray(wipContent),
                        ContentType.DEFAULT_BINARY, item.getFilename());
                // mpEntity.addPart( "fileContent", new InputStreamBody(wipContent,
                // item.getFilename()) );
            }
            marshaller.marshal(objectFactory.createRepositoryItemIdentity(itemIdentity), xmlWriter);
            mpEntity.addTextBody("item", xmlWriter.toString(), ContentType.TEXT_XML);
            request.setEntity(mpEntity.build());

            // Send the web service request and unmarshall the updated meta-data from the response
            log.info("Sending lock request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Lock response received - Status OK");

            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<LibraryInfoType> jaxbElement = (JAXBElement<LibraryInfoType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());

            // Update the local cache with the content we just received from the remote web service
            manager.getFileManager().saveLibraryMetadata(jaxbElement.getValue());

            // Update the local repository item with the latest state information
            ((RepositoryItemImpl) item).setState(RepositoryItemState.MANAGED_UNLOCKED);
            ((RepositoryItemImpl) item).setLockedByUser(null);

            // Force a re-download of the updated content to make sure the local copy is
            // synchronized
            // with the remote repository.
            downloadContent(item, true);

            success = true;

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);

        } finally {
            // Close the WIP content input stream
            try {
                if (wipContent != null)
                    wipContent.close();
            } catch (Throwable t) {
            }

            // Commit or roll back the changes based on the result of the operation
            if (success) {
                manager.getFileManager().commitChangeSet();
            } else {
                try {
                    manager.getFileManager().rollbackChangeSet();
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
        try {
            validateRepositoryItem(item);

            // Build the HTTP request for the remote service
            RepositoryItemIdentityType itemIdentity = createItemIdentity(item);
            Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext().createMarshaller();
            HttpPost request = newPostRequest(PROMOTE_ENDPOINT);
            StringWriter xmlWriter = new StringWriter();

            marshaller.marshal(objectFactory.createRepositoryItemIdentity(itemIdentity), xmlWriter);
            request.setEntity(new StringEntity(xmlWriter.toString(), ContentType.TEXT_XML));

            // Send the web service request and check the response
            log.info("Sending promote request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Promote response received - Status OK");

            // Update the local cache with the content that was just modified in the remote
            // repository
            downloadContent(item, true);

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#demote(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public void demote(RepositoryItem item) throws RepositoryException {
        try {
            validateRepositoryItem(item);

            // Build the HTTP request for the remote service
            RepositoryItemIdentityType itemIdentity = createItemIdentity(item);
            Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext().createMarshaller();
            HttpPost request = newPostRequest(DEMOTE_ENDPOINT);
            StringWriter xmlWriter = new StringWriter();

            marshaller.marshal(objectFactory.createRepositoryItemIdentity(itemIdentity), xmlWriter);
            request.setEntity(new StringEntity(xmlWriter.toString(), ContentType.TEXT_XML));

            // Send the web service request and check the response
            log.info("Sending promote request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Promote response received - Status OK");

            // Update the local cache by deleting the local copy of the item
            downloadContent(item, true);

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#recalculateCrc(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public void recalculateCrc(RepositoryItem item) throws RepositoryException {
        try {
            validateRepositoryItem(item);

            // Build the HTTP request for the remote service
            RepositoryItemIdentityType itemIdentity = createItemIdentity(item);
            Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext().createMarshaller();
            HttpPost request = newPostRequest(RECALCULATE_CRC_ENDPOINT);
            StringWriter xmlWriter = new StringWriter();

            marshaller.marshal(objectFactory.createRepositoryItemIdentity(itemIdentity), xmlWriter);
            request.setEntity(new StringEntity(xmlWriter.toString(), ContentType.TEXT_XML));

            // Send the web service request and check the response
            log.info("Sending recalculate-crc request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Recalculate-crc response received - Status OK");

            // Update the local cache by deleting the local copy of the item
            downloadContent(item, true);

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Repository#delete(org.opentravel.schemacompiler.repository.RepositoryItem)
     */
    @Override
    public void delete(RepositoryItem item) throws RepositoryException {
        try {
            validateRepositoryItem(item);

            // Build the HTTP request for the remote service
            RepositoryItemIdentityType itemIdentity = createItemIdentity(item);
            Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext().createMarshaller();
            HttpPost request = newPostRequest(DELETE_ENDPOINT);
            StringWriter xmlWriter = new StringWriter();

            marshaller.marshal(objectFactory.createRepositoryItemIdentity(itemIdentity), xmlWriter);
            request.setEntity(new StringEntity(xmlWriter.toString(), ContentType.TEXT_XML));

            // Send the web service request and check the response
            log.info("Sending delete request to HTTP endpoint: " + endpointUrl);
            HttpResponse response = executeWithAuthentication(request);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            log.info("Delete response received - Status OK");

            // Update the local cache with the content that was just modified in the remote
            // repository
            File itemMetadata = manager.getFileManager().getLibraryMetadataLocation(
                    item.getBaseNamespace(), item.getFilename(), item.getVersion());
            File itemContent = manager.getFileManager().getLibraryContentLocation(
                    item.getBaseNamespace(), item.getFilename(), item.getVersion());

            itemMetadata.delete();
            itemContent.delete();

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the library meta-data is unreadable.", e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.RemoteRepository#downloadContent(org.opentravel.schemacompiler.repository.RepositoryItem,
     *      boolean)
     */
    @Override
    public boolean downloadContent(RepositoryItem item, boolean forceUpdate)
            throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());

        return downloadContent(baseNS, item.getFilename(), item.getVersion(), forceUpdate);
    }

    /**
     * Downloads the specified content (and its associated meta-data) from the remote repository
     * into the local instance. If the refresh policy for this repository does not require an update
     * (or the remote repository is not accessible), the locally cached copy of the content will be
     * used.
     * 
     * <p>This method will return true if the local copy was replaced by newer content from the remote
     * repository.  False will be returned if the local copy was up-to-date, even if a refresh was
     * forced by the caller or the update policy.
     * 
     * @param baseNamespace
     *            the namespace of the repository item to download
     * @param filename
     *            the filename of the repository item to download
     * @param versionIdentifier
     *            the version identifier of the repository item to download
     * @param forceUpdate
     *            disregards the repository's update policy and forces the remote content to be
     *            downloaded
     * @return boolean
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     */
    @SuppressWarnings("unchecked")
    public boolean downloadContent(String baseNamespace, String filename, String versionIdentifier,
            boolean forceUpdate) throws RepositoryException {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(baseNamespace);
        LibraryInfoType contentMetadata = null;
        boolean refreshRequired, isStaleContent = false;
        Date localLastUpdated;

        try {
            contentMetadata = manager.getFileManager().loadLibraryMetadata(baseNS, filename, versionIdentifier);
            localLastUpdated = XMLGregorianCalendarConverter.toJavaDate(contentMetadata.getLastUpdated());
            refreshRequired = forceUpdate || (contentMetadata.getStatus() == LibraryStatus.DRAFT) || // always update draft items
                    (refreshPolicy == RefreshPolicy.ALWAYS);

            if (!refreshRequired && (refreshPolicy == RefreshPolicy.DAILY)) {
                refreshRequired = !dateOnlyFormat.format(localLastUpdated).equals(dateOnlyFormat.format(new Date()));
            }
            
        } catch (RepositoryException e) {
        	localLastUpdated = new Date( 0L ); // make sure the local date is earlier than anything we will get from the repository
            refreshRequired = true;
        }

        // If the item was previously downloaded, make sure it originated from this remote
        // repository
        if ((contentMetadata != null) && !contentMetadata.getOwningRepository().equals(id)) {
            throw new RepositoryException(
                    "The requested content is managed by a different remote repository.");
        }

        // If a refresh is required, download the item's metadata and content from the remote web
        // service
        if (refreshRequired) {
            File repositoryContentFile = manager.getFileManager().getLibraryContentLocation(baseNS,
                    filename, versionIdentifier);
            boolean success = false;

            try {
                log.info("Downloading content from repository '" + id + "' - " + baseNS + "; "
                        + filename + "; " + versionIdentifier);
                manager.getFileManager().startChangeSet();

                // Marshal the JAXB content to a string and construct the HTTP requests
                HttpPost metadataRequest = newPostRequest(REPOSITORY_ITEM_METADATA_ENDPOINT);
                HttpPost contentRequest = newPostRequest(REPOSITORY_ITEM_CONTENT_ENDPOINT);
                RepositoryItemIdentityType itemIdentity = new RepositoryItemIdentityType();
                Marshaller marshaller = RepositoryFileManager.getSharedJaxbContext()
                        .createMarshaller();
                StringWriter xmlWriter = new StringWriter();

                itemIdentity.setBaseNamespace(baseNS);
                itemIdentity.setFilename(filename);
                itemIdentity.setVersion(versionIdentifier);

                marshaller.marshal(objectFactory.createRepositoryItemIdentity(itemIdentity),
                        xmlWriter);
                metadataRequest.setEntity(new StringEntity(xmlWriter.toString(),
                        ContentType.TEXT_XML));
                contentRequest.setEntity(new StringEntity(xmlWriter.toString(),
                        ContentType.TEXT_XML));

                // Send the requests for meta-data and content to the remote web service
                HttpResponse metadataResponse = executeWithAuthentication(metadataRequest);
                HttpResponse contentResponse = executeWithAuthentication(contentRequest);

                if (metadataResponse.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                    throw new RepositoryException(getResponseErrorMessage(metadataResponse));
                }
                if (contentResponse.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                    throw new RepositoryException(getResponseErrorMessage(contentResponse));
                }

                // Update the local cache with the content we just received from the remote web
                // service
                Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext().createUnmarshaller();
                JAXBElement<LibraryInfoType> jaxbElement = (JAXBElement<LibraryInfoType>) unmarshaller
                        .unmarshal(metadataResponse.getEntity().getContent());
                LibraryInfoType libraryMetadata = jaxbElement.getValue();

                manager.getFileManager().createNamespaceIdFiles(
                        jaxbElement.getValue().getBaseNamespace());
                manager.getFileManager().saveLibraryMetadata(libraryMetadata);
                manager.getFileManager().saveFile(repositoryContentFile,
                        contentResponse.getEntity().getContent());
                success = true;
                
                // Compare the last-updated with our previous local value and return true if the
                // local content was modified.
                Date remoteLastUpdated = XMLGregorianCalendarConverter.toJavaDate(libraryMetadata.getLastUpdated());
                isStaleContent = remoteLastUpdated.after( localLastUpdated );

            } catch (UnknownHostException e) {
                // If the remote repository is inaccessible, it is only an error if we are
                // downloading the
                // files for the first time.
                File repositoryMetadataFile = manager.getFileManager().getLibraryMetadataLocation(
                        baseNS, filename, versionIdentifier);

                if (repositoryMetadataFile.exists() && repositoryContentFile.exists()) {
                    log.warn("Remote repository is unavailable - using cached copy of file '"
                            + filename + "'.");

                } else {
                    throw new RepositoryUnavailableException(
                            "The remote repository is unavailable.", e);
                }

            } catch (JAXBException e) {
                throw new RepositoryException("The format of the library meta-data is unreadable.",
                        e);

            } catch (IOException e) {
                throw new RepositoryException("The remote repository is unavailable.", e);

            } finally {
                // Commit or roll back the changes based on the result of the operation
                if (success) {
                    manager.getFileManager().commitChangeSet();
                } else {
                    try {
                        manager.getFileManager().rollbackChangeSet();
                    } catch (Throwable t) {
                        log.error("Error rolling back the current change set.", t);
                    }
                }
            }
        }
        return isStaleContent;
    }

    /**
     * Contacts the repository web service at the specified endpoint URL, and returns the repository
     * meta-data information.
     * 
     * @param endpointUrl
     *            the URL endpoint of the OTA2.0 repository web service
     * @return RepositoryInfoType
     * @throws RepositoryException
     *             thrown if the remote web service is not available
     */
    @SuppressWarnings("unchecked")
    public static RepositoryInfoType getRepositoryMetadata(String endpointUrl)
            throws RepositoryException {
        try {
            HttpGet getRequest = new HttpGet(endpointUrl + REPOSITORY_METADATA_ENDPOINT);
            HttpResponse response = execute(getRequest);

            if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
                throw new RepositoryException(getResponseErrorMessage(response));
            }
            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext()
                    .createUnmarshaller();
            JAXBElement<RepositoryInfoType> jaxbElement = (JAXBElement<RepositoryInfoType>) unmarshaller
                    .unmarshal(response.getEntity().getContent());

            return jaxbElement.getValue();

        } catch (JAXBException e) {
            throw new RepositoryException("The format of the repository meta-data is unreadable.",
                    e);

        } catch (IOException e) {
            throw new RepositoryException("The remote repository is unavailable.", e);
        }
    }

    /**
     * Constructs a new repository item instance using values from the given meta-data record.
     * 
     * @param libraryInfo
     *            the library meta-data record for the repository item
     * @return RepositoryItem
     * @throws RepositoryException
     *             thrown if the namespace URI from the item's meta-data is not valid
     */
    private RepositoryItem newRepositoryItem(LibraryInfoType libraryInfo)
            throws RepositoryException {
        RepositoryItemImpl item = new RepositoryItemImpl();

        item.setRepository(this);
        item.setNamespace(libraryInfo.getNamespace());
        item.setBaseNamespace(libraryInfo.getBaseNamespace());
        item.setFilename(libraryInfo.getFilename());
        item.setVersion(libraryInfo.getVersion());
        item.setStatus(TLLibraryStatus.valueOf(libraryInfo.getStatus().toString().toUpperCase()));
        item.setState(RepositoryItemState.valueOf(libraryInfo.getState().toString()));
        item.setLockedByUser(libraryInfo.getLockedBy());
        RepositoryUtils.checkItemState(item, manager);
        return item;
    }

    /**
     * Verifies that the given repository item is owned by this repository.
     * 
     * @param item
     *            the repository item to validate
     * @throws IllegalArgumentException
     *             thrown if the item is owned by another repository
     */
    private void validateRepositoryItem(RepositoryItem item) {
        if (item.getRepository() != this) {
            throw new IllegalArgumentException(
                    "The repository item is not a member of this repository.");
        }
    }

    /**
     * Constructs a new <code>RepositoryItemIdentityType</code> that is used by the remote web
     * service to uniquely identify a repository item.
     * 
     * @param item
     *            the repository item to be identified
     * @return RepositoryItemIdentityType
     */
    protected RepositoryItemIdentityType createItemIdentity(RepositoryItem item) {
        String baseNS = RepositoryNamespaceUtils.normalizeUri(item.getBaseNamespace());
        RepositoryItemIdentityType itemIdentity = new RepositoryItemIdentityType();

        itemIdentity.setBaseNamespace(baseNS);
        itemIdentity.setFilename(item.getFilename());
        itemIdentity.setVersion(item.getVersion());
        return itemIdentity;
    }

    /**
     * Returns a new HTTP-GET request for the remote web service.
     * 
     * @param path
     *            the base URL path for the request
     * @urlParams the parameters to include on the request URL
     * @return HttpGet
     */
    protected HttpGet newGetRequest(String path, HttpGetParam... urlParams) {
        StringBuilder requestUrl = new StringBuilder(endpointUrl).append(path);
        boolean firstParam = true;

        for (HttpGetParam urlParam : urlParams) {
            requestUrl.append(firstParam ? '?' : '&');
            requestUrl.append(urlParam.paramName).append('=');

            if (urlParam.paramValue != null) {
                try {
                    requestUrl.append(URLEncoder.encode(urlParam.paramValue, "UTF-8"));

                } catch (UnsupportedEncodingException e) {
                    // No error - UTF-8 is always supported
                }
            }
            firstParam = false;
        }

        HttpGet request = new HttpGet(requestUrl.toString());

        return request;
    }

    /**
     * Returns a new HTTP-POST request for the remote web service.
     * 
     * @param path
     *            the base URL path for the request
     * @return HttpPost
     */
    protected HttpPost newPostRequest(String path) {
        HttpPost request = new HttpPost(endpointUrl + path);

        return request;
    }

    /**
     * Configures the 'Authorization' header on the given HTTP request using the current user ID and
     * encrypted password that is configured for this repository.
     */
    private HttpClientContext createHttpContext() {
		HttpClientContext context = HttpClientContext.create();
		
        if ((userId != null) && (encryptedPassword != null)) {
            AuthState target = new AuthState();
            target.update(
                    new BasicScheme(),
                    new UsernamePasswordCredentials(userId, PasswordHelper
                            .decrypt(encryptedPassword)));
            context.setAttribute(HttpClientContext.TARGET_AUTH_STATE, target);
        }
        return context;
    }

    /**
     * If the response status indicates an error condition and a message is provided, the text of
     * that message is returned.
     * 
     * @param response
     *            the HTTP response to process
     * @return String
     */
    private static String getResponseErrorMessage(HttpResponse response) {
        String errorMessage = null;

        if (response.getStatusLine().getStatusCode() != HTTP_RESPONSE_STATUS_OK) {
            try {
                InputStream responseStream = response.getEntity().getContent();
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[256];
                int bytesRead;

                while ((bytesRead = responseStream.read(buffer)) >= 0) {
                    byteStream.write(buffer, 0, bytesRead);
                }
                responseStream.close();
                errorMessage = new String(byteStream.toByteArray(), "UTF-8");

            } catch (IOException e) {
                errorMessage = "Unknown repository error on the remote host.";
            }
        }
        return errorMessage;
    }

    /**
     * Encapsulates a single name/value pair that should be included as a URL parameter on an HTTP
     * GET request.
     */
    private class HttpGetParam {

        public String paramName;
        public String paramValue;

        public HttpGetParam(String paramName, String paramValue) {
            this.paramName = paramName;
            this.paramValue = paramValue;
        }

    }

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[DEFAULT_BUFFER_SIZE];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

}
