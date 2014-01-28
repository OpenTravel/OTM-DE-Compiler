
package org.opentravel.schemacompiler.repository.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryState;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Static utility methods used by the OTA2.0 repository implementation.
 * 
 * @author S. Livezey
 */
public class RepositoryUtils {
	
	/**
	 * Creates a new repository item instance using information from the meta-data record provided.
	 * 
	 * @param manager  the repository manager that will own the new item
	 * @param itemMetadata  the meta-data record that represents the repository item
	 * @return RepositoryItemImpl
	 */
	public static RepositoryItemImpl createRepositoryItem(RepositoryManager manager, LibraryInfoType itemMetadata) {
		RepositoryItemImpl item = new RepositoryItemImpl();
		
		item.setRepository( manager.getRepository(itemMetadata.getOwningRepository()) );
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
	 * @param item  the repository item for which to create a meta-data record
	 * @return LibraryInfoType
	 */
	public static LibraryInfoType createItemMetadata(RepositoryItem item) {
		LibraryInfoType itemMetadata = new LibraryInfoType();
		
		itemMetadata.setOwningRepository( item.getRepository().getId() );
		itemMetadata.setNamespace( item.getNamespace() );
		itemMetadata.setBaseNamespace( item.getBaseNamespace() );
		itemMetadata.setFilename( item.getFilename() );
		itemMetadata.setLibraryName( item.getLibraryName() );
		itemMetadata.setVersion( item.getVersion() );
		itemMetadata.setVersionScheme( item.getVersionScheme() );
		itemMetadata.setStatus( LibraryStatus.valueOf( item.getStatus().toString() ) );
		itemMetadata.setState( RepositoryState.valueOf( item.getState().toString() ) );
		itemMetadata.setLockedBy( item.getLockedByUser() );
		return itemMetadata;
	}
	
	/**
	 * If the item is locked by the local user, change its state to WIP.
	 * 
	 * @param item  the repository item whose state is to be checked
	 * @param repositoryManager  the repository manager that owns the local copy of the item
	 * @throws RepositoryException  thrown if the namespace URI of the item provided is not valid
	 */
	public static void checkItemState(RepositoryItemImpl item, RepositoryManager repositoryManager) throws RepositoryException {
		// If the item is locked by the local user, change its state to managed WIP
		if ((item.getRepository() != null) && (item.getState() == RepositoryItemState.MANAGED_LOCKED)) {
			String lockOwner;
			
			if (item.getRepository() == repositoryManager) {
				lockOwner = System.getProperty("user.name");
			} else {
				lockOwner = ((RemoteRepositoryClient) item.getRepository()).getUserId();
			}
			if ((lockOwner != null) && lockOwner.equals(item.getLockedByUser())) {
				File wipFile = repositoryManager.getFileManager().getLibraryWIPContentLocation(
						item.getBaseNamespace(), item.getFilename() );
				
				// If the WIP file does not exist in the local repository, the item's state will remain
				// as MANAGED_LOCKED.  This situation can arise when the user accesses repository content
				// from a different workstation (hence, a different local repository) from the one where
				// he/she originally obtained the item's lock.
				if (wipFile.exists()) {
					item.setState(RepositoryItemState.MANAGED_WIP);
				}
			}
		}
	}
	
	/**
	 * Converts the given string to a URI.  Prior to returning, this method verifies that the conforms to
	 * the OTM repository scheme, and that all required URI components are present.
	 * 
	 * @param uriString  the URI string to analyze
	 * @return URI
	 * @throws URISyntaxException  thrown if the given string does not conform to the format of a valid OTM repository URI
	 */
	public static URI toRepositoryItemUri(String uriString) throws URISyntaxException {
		URI uri = new URI(uriString);
		
		if ((uri.getScheme() == null) || !uri.getScheme().equals("otm")) {
			throw new URISyntaxException(uriString, "The OTM repository URI provided is not valid.", 0);
		}
		if (uri.getAuthority() == null) {
			throw new URISyntaxException(uriString, "All OTM repository URI's must specify a repository ID as the authority.", 6);
		}
		if (uri.getPath() == null) {
			throw new URISyntaxException(uriString, "All OTM repository URI's must specify the filename of a managed resource..",
					(uriString.length() - 1));
		}
		return uri;
	}
	
	/**
	 * Parses the components of the given URI and returns each component in a string array.  Optional
	 * elements that are not provided are null in the array that is returned.
	 * 
	 * <p>The array elements that are returned are as follows:
	 * <ul>
	 *   <li>[0] - the repository ID (URI authority)</li>
	 *   <li>[1] - the repository item's assigned namespace (optional)</li>
	 *   <li>[2] - the repository item's filename</li>
	 *   <li>[3] - the repository item's version scheme (URI fragment; optional)</li>
	 * </ul>
	 * 
	 * @param uri  the repository item URI to be parsed
	 * @return String[]
	 */
	public static String[] parseRepositoryItemUri(URI uri) {
		if ((uri.getScheme() == null) || !uri.getScheme().equals("otm")) {
			throw new IllegalArgumentException("The URI provided is not valid for an OTM repository: " + uri.toString());
		}
		String[] uriParts = new String[4];
		String path = uri.getPath();
		int pathSeparatorIdx;
		
		uriParts[0] = uri.getAuthority();
		uriParts[3] = uri.getFragment();
		
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		
		if ((pathSeparatorIdx = path.lastIndexOf('/')) < 0) {
			uriParts[2] = path;
			
		} else {
			if (pathSeparatorIdx < (path.length() - 1)) {
				uriParts[2] = path.substring(pathSeparatorIdx + 1);
			}
			try {
				uriParts[1] = URLDecoder.decode(path.substring(0, pathSeparatorIdx), "UTF-8");
				
			} catch (UnsupportedEncodingException e) {
				// Should never happen - UTF-8 is supported on all known platforms
			}
		}
		return uriParts;
	}
	
	/**
	 * Constructs a URI for the given repository item.
	 * 
	 * @param item  the item for which to construct a URI
	 * @param fullyQualified  indicates whether the item's namespace should be included in the URI string
	 * @return URI
	 */
	public static URI newURI(RepositoryItem item, boolean fullyQualified) {
		StringBuilder uriString = new StringBuilder("otm://").append( item.getRepository().getId() ).append("/");
		VersionSchemeFactory vsFactory = VersionSchemeFactory.getInstance();
		String itemFilename = null;
		
		if (fullyQualified) {
			try {
				uriString.append( URLEncoder.encode(item.getNamespace(), "UTF-8") ).append("/");
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
		
		if ((item.getVersionScheme() != null) &&
				!vsFactory.getDefaultVersionScheme().equals(item.getVersionScheme())) {
			uriString.append("#").append( item.getVersionScheme() );
		}
		
		try {
			return new URI(uriString.toString());
			
		} catch (URISyntaxException e) {
			// Should never happen, but throw a runtime exception just in case
			throw new RuntimeException( e );
		}
	}

}
