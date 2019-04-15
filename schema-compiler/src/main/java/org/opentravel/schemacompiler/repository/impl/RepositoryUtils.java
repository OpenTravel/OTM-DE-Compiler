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

import org.opentravel.ns.ota2.repositoryinfo_v01_00.EntityInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryHistoryItemType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryHistoryType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryItemIdentityType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryState;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.loader.impl.MultiVersionLibraryModuleLoader;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemacompiler.repository.RepositoryItemHistory;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.SchemaCompilerRuntimeException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Static utility methods used by the OTA2.0 repository implementation.
 * 
 * @author S. Livezey
 */
public class RepositoryUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private RepositoryUtils() {}

    /**
     * Creates a new repository item instance using information from the meta-data record provided.
     * 
     * @param manager the repository manager that will own the new item
     * @param itemMetadata the meta-data record that represents the repository item
     * @return RepositoryItemImpl
     */
    public static RepositoryItemImpl createRepositoryItem(RepositoryManager manager, LibraryInfoType itemMetadata) {
        RepositoryItemImpl item = new RepositoryItemImpl();

        item.setRepository( manager.getRepository( itemMetadata.getOwningRepository() ) );
        item.setNamespace( itemMetadata.getNamespace() );
        item.setBaseNamespace( itemMetadata.getBaseNamespace() );
        item.setFilename( itemMetadata.getFilename() );
        item.setLibraryName( itemMetadata.getLibraryName() );
        item.setVersion( itemMetadata.getVersion() );
        item.setVersionScheme( itemMetadata.getVersionScheme() );
        item.setStatus( TLLibraryStatus.valueOf( itemMetadata.getStatus().toString() ) );
        item.setState( RepositoryItemState.valueOf( itemMetadata.getState().toString() ) );
        item.setLockedByUser( itemMetadata.getLockedBy() );
        return item;
    }

    /**
     * Creates a new meta-data record using information from the given repository item.
     * 
     * @param item the repository item for which to create a meta-data record
     * @return LibraryInfoType
     */
    public static LibraryInfoType createItemMetadata(RepositoryItem item) {
        LibraryInfoType itemMetadata = new LibraryInfoType();

        populateMetadata( item, itemMetadata );
        return itemMetadata;
    }

    /**
     * Populates the contents of the library meta-data object.
     * 
     * @param source the repository item from which to copy meta-data values
     * @param itemMetadata the library meta-data object to populate
     */
    public static void populateMetadata(RepositoryItem source, LibraryInfoType itemMetadata) {
        TLLibraryStatus libStatus = (source.getStatus() == null) ? TLLibraryStatus.DRAFT : source.getStatus();
        RepositoryItemState itemState =
            (source.getState() == null) ? RepositoryItemState.MANAGED_UNLOCKED : source.getState();

        itemMetadata.setOwningRepository( source.getRepository().getId() );
        itemMetadata.setNamespace( source.getNamespace() );
        itemMetadata.setBaseNamespace( source.getBaseNamespace() );
        itemMetadata.setFilename( source.getFilename() );
        itemMetadata.setLibraryName( source.getLibraryName() );
        itemMetadata.setVersion( source.getVersion() );
        itemMetadata.setVersionScheme( source.getVersionScheme() );
        itemMetadata.setStatus( LibraryStatus.valueOf( libStatus.toString() ) );
        itemMetadata.setState( RepositoryState.valueOf( itemState.toString() ) );
        itemMetadata.setLockedBy( source.getLockedByUser() );
    }

    /**
     * Returns a new meta-data instance for the given entity.
     * 
     * @param entity the entity for which to return repository meta-data
     * @param manager the repository manager for the local environment
     * @return EntityInfoType
     * @throws RepositoryException thrown if the entity's owning library cannot be resolved by the repository manager
     */
    public static EntityInfoType createEntityMetadata(NamedEntity entity, RepositoryManager manager)
        throws RepositoryException {
        AbstractLibrary owningLibrary = entity.getOwningLibrary();
        try {
            VersionScheme vScheme =
                VersionSchemeFactory.getInstance().getVersionScheme( owningLibrary.getVersionScheme() );
            String libraryFilename = URLUtils.getUrlFilename( owningLibrary.getLibraryUrl() );
            String baseNS = vScheme.getBaseNamespace( owningLibrary.getNamespace() );
            RepositoryItem owningItem =
                manager.getRepositoryItem( baseNS, libraryFilename, owningLibrary.getVersion() );
            EntityInfoType entityMetadata = new EntityInfoType();

            populateMetadata( owningItem, entityMetadata );
            entityMetadata.setEntityName( entity.getLocalName() );
            entityMetadata.setEntityType( entity.getClass().getName() );
            return entityMetadata;

        } catch (VersionSchemeException e) {
            throw new RepositoryException( "Unknown version scheme for entity: " + owningLibrary.getVersionScheme() );
        }

    }

    /**
     * Constructs a user-consumable instance of the given <code>LibraryHistoryType</code> instance.
     * 
     * @param libraryHistory the persistent representation of the repository item's history
     * @param manager the repository manager for the local environment
     * @return RepositoryItemHistory
     * @throws RepositoryException thrown if the item's identity cannot be resolved by the repository manager
     */
    public static RepositoryItemHistory createItemHistory(LibraryHistoryType libraryHistory, RepositoryManager manager)
        throws RepositoryException {
        RepositoryItemIdentityType itemIdentity = libraryHistory.getRepositoryItemIdentity();
        RepositoryItem item = manager.getRepositoryItem( itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
            itemIdentity.getVersion() );
        RepositoryItemHistory itemHistory = new RepositoryItemHistory( item );
        List<RepositoryItemCommit> commitItems = new ArrayList<>();

        for (LibraryHistoryItemType libraryHistoryItem : libraryHistory.getLibraryHistoryItem()) {
            commitItems.add( new RepositoryItemCommit( libraryHistoryItem ) );
        }
        itemHistory.setCommitHistory( commitItems );
        return itemHistory;
    }

    /**
     * If the item is locked by the local user, change its state to WIP.
     * 
     * @param item the repository item whose state is to be checked
     * @param repositoryManager the repository manager that owns the local copy of the item
     * @throws RepositoryException thrown if the namespace URI of the item provided is not valid
     */
    public static void checkItemState(RepositoryItemImpl item, RepositoryManager repositoryManager)
        throws RepositoryException {
        // If the item is locked by the local user, change its state to managed WIP
        if ((item.getRepository() != null) && (item.getState() == RepositoryItemState.MANAGED_LOCKED)) {
            String lockOwner;

            if (item.getRepository() == repositoryManager) {
                lockOwner = System.getProperty( "user.name" );
            } else {
                lockOwner = ((RemoteRepositoryClient) item.getRepository()).getUserId();
            }
            if ((lockOwner != null) && lockOwner.equalsIgnoreCase( item.getLockedByUser() )) {
                File wipFile = repositoryManager.getFileManager().getLibraryWipContentLocation( item.getBaseNamespace(),
                    item.getFilename() );

                // If the WIP file does not exist in the local repository, the item's state will remain as
                // MANAGED_LOCKED. This situation can arise when the user accesses repository content from a different
                // workstation (hence, a different local repository) from the one where he/she originally obtained the
                // item's lock.
                if (wipFile.exists()) {
                    item.setState( RepositoryItemState.MANAGED_WIP );
                }
            }
        }
    }

    /**
     * Converts the given string to a URI. Prior to returning, this method verifies that the conforms to the OTM
     * repository scheme, and that all required URI components are present.
     * 
     * @param uriString the URI string to analyze
     * @return URI
     * @throws URISyntaxException thrown if the given string does not conform to the format of a valid OTM repository
     *         URI
     */
    public static URI toRepositoryItemUri(String uriString) throws URISyntaxException {
        URI uri = new URI( uriString );

        if ((uri.getScheme() == null) || !uri.getScheme().equals( "otm" )) {
            throw new URISyntaxException( uriString, "The OTM repository URI provided is not valid.", 0 );
        }
        if (uri.getAuthority() == null) {
            throw new URISyntaxException( uriString,
                "All OTM repository URI's must specify a repository ID as the authority.", 6 );
        }
        if (uri.getPath() == null) {
            throw new URISyntaxException( uriString,
                "All OTM repository URI's must specify the filename of a managed resource..",
                (uriString.length() - 1) );
        }
        return uri;
    }

    /**
     * Parses the components of the given URI and returns each component in a string array. Optional elements that are
     * not provided are null in the array that is returned.
     * 
     * <p>
     * The array elements that are returned are as follows:
     * <ul>
     * <li>[0] - the repository ID (URI authority)</li>
     * <li>[1] - the repository item's assigned namespace (optional)</li>
     * <li>[2] - the repository item's filename</li>
     * <li>[3] - the repository item's version scheme (URI fragment; optional)</li>
     * </ul>
     * 
     * @param uri the repository item URI to be parsed
     * @return String[]
     */
    public static String[] parseRepositoryItemUri(URI uri) {
        if ((uri.getScheme() == null) || !uri.getScheme().equals( "otm" )) {
            throw new IllegalArgumentException(
                "The URI provided is not valid for an OTM repository: " + uri.toString() );
        }
        String[] uriParts = new String[4];
        String path = uri.getPath();
        int pathSeparatorIdx;

        uriParts[0] = uri.getAuthority();
        uriParts[3] = uri.getFragment();

        if (path.startsWith( "/" )) {
            path = path.substring( 1 );
        }

        if ((pathSeparatorIdx = path.lastIndexOf( '/' )) < 0) {
            uriParts[2] = path;

        } else {
            if (pathSeparatorIdx < (path.length() - 1)) {
                uriParts[2] = path.substring( pathSeparatorIdx + 1 );
            }
            try {
                uriParts[1] = URLDecoder.decode( path.substring( 0, pathSeparatorIdx ), "UTF-8" );

            } catch (UnsupportedEncodingException e) {
                // Should never happen - UTF-8 is supported on all known platforms
            }
        }
        return uriParts;
    }

    /**
     * Constructs a URI for the given repository item.
     * 
     * @param item the item for which to construct a URI
     * @param fullyQualified indicates whether the item's namespace should be included in the URI string
     * @return URI
     */
    public static URI newURI(RepositoryItem item, boolean fullyQualified) {
        StringBuilder uriString = new StringBuilder( "otm://" ).append( item.getRepository().getId() ).append( "/" );
        VersionSchemeFactory vsFactory = VersionSchemeFactory.getInstance();
        String itemFilename = null;

        if (fullyQualified) {
            try {
                uriString.append( URLEncoder.encode( item.getNamespace(), "UTF-8" ) ).append( "/" );
            } catch (UnsupportedEncodingException e) {
                // Should never happen - UTF-8 encoding is supported on all known platforms
            }
        }

        if (item instanceof ProjectItem) {
            try {
                ProjectItem pItem = (ProjectItem) item;

                if (pItem.getContent() instanceof TLLibrary) {
                    VersionScheme vScheme = vsFactory.getVersionScheme( item.getVersionScheme() );
                    TLLibrary itemLibrary = (TLLibrary) pItem.getContent();

                    itemFilename = vScheme.getDefaultFileHint( itemLibrary.getNamespace(), itemLibrary.getName() );
                }

            } catch (VersionSchemeException e) {
                // Ignore error and use assigned filename (below)
            }
        }
        if (itemFilename == null) {
            itemFilename = item.getFilename();
        }
        uriString.append( itemFilename );

        if ((item.getVersionScheme() != null)
            && !vsFactory.getDefaultVersionScheme().equals( item.getVersionScheme() )) {
            uriString.append( "#" ).append( item.getVersionScheme() );
        }

        try {
            return new URI( uriString.toString() );

        } catch (URISyntaxException e) {
            // Should never happen, but throw a runtime exception just in case
            throw new SchemaCompilerRuntimeException( e );
        }
    }

    /**
     * Returns the corresponding library status for the JAXB library status provided.
     * 
     * @param repoStatus library status value from JAXB repository namespace
     * @return TLLibraryStatus
     */
    public static TLLibraryStatus getLibraryStatus(LibraryStatus repoStatus) {
        TLLibraryStatus result = null;

        if (repoStatus != null) {
            switch (repoStatus) {
                case DRAFT:
                    result = TLLibraryStatus.DRAFT;
                    break;
                case UNDER_REVIEW:
                    result = TLLibraryStatus.UNDER_REVIEW;
                    break;
                case FINAL:
                    result = TLLibraryStatus.FINAL;
                    break;
                case OBSOLETE:
                    result = TLLibraryStatus.OBSOLETE;
                    break;
                default:
                    // No default action required
            }
        }
        return result;
    }

    /**
     * Returns true if the given 'status' should be considered included in queries for the 'checkStatus' value.
     * 
     * @param status the status value being evalulated
     * @param checkStatus value to determine whether the 'status' is included within
     * @return boolean
     */
    public static boolean isInclusiveStatus(TLLibraryStatus status, TLLibraryStatus checkStatus) {
        boolean result = false;

        if (status != null) {
            int checkRank = (checkStatus == null) ? 0 : checkStatus.getRank();

            result = (checkRank <= status.getRank());
        }
        return result;
    }

    /**
     * Returns true if the given library was originally saved in the OTM 1.6 file format.
     * 
     * @param library the library to be analyzed
     * @return boolean
     */
    public static boolean isOTM16Library(AbstractLibrary library) {
        boolean is16Library = false;

        if ((library != null) && (library.getLibraryUrl() != null)) {
            is16Library = isOTM16Library( URLUtils.toFile( library.getLibraryUrl() ) );
        }
        return is16Library;
    }

    /**
     * Returns true if the given repository item is a library that was originally saved in the OTM 1.6 file format.
     * 
     * @param item the repository item to be analyzed
     * @param repositoryManager the repository manager to use when accessing the file content
     * @return boolean
     */
    public static boolean isOTM16Library(RepositoryItem item, RepositoryManager repositoryManager) {
        boolean is16Library = false;

        if ((item != null) && (repositoryManager != null)) {
            try {
                File libraryFile = repositoryManager.getFileManager()
                    .getLibraryContentLocation( item.getBaseNamespace(), item.getFilename(), item.getVersion() );

                is16Library = isOTM16Library( libraryFile );

            } catch (RepositoryException e) {
                // Ignore error - method will return false
            }
        }

        return is16Library;
    }

    /**
     * Returns true if the given library was originally saved in the OTM 1.6 file format.
     * 
     * @param libraryFile the library file to be analyzed
     * @return boolean
     */
    private static boolean isOTM16Library(File libraryFile) {
        boolean is16Library = false;

        try {
            if ((libraryFile != null) && libraryFile.exists()
                && !libraryFile.getName().toLowerCase().endsWith( ".xsd" )) {
                LibraryModuleLoader<InputStream> loader = new MultiVersionLibraryModuleLoader();
                LibraryModuleInfo<Object> moduleInfo =
                    loader.loadLibrary( new LibraryStreamInputSource( libraryFile ), new ValidationFindings() );
                Object jaxbLibrary = moduleInfo.getJaxbArtifact();

                if (jaxbLibrary != null) {
                    is16Library = (jaxbLibrary instanceof org.opentravel.ns.ota2.librarymodel_v01_06.Library);
                }
            }
        } catch (Exception e) {
            // No action - method will return false
        }
        return is16Library;
    }

}
