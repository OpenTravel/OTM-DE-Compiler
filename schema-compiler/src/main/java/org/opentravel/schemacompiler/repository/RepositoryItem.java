
package org.opentravel.schemacompiler.repository;

import java.net.URI;

import org.opentravel.schemacompiler.model.TLLibraryStatus;

/**
 * Defines a single item that is managed within the scope of an OTA2.0 <code>Repository</code>.
 * 
 * @author S. Livezey
 */
public interface RepositoryItem {
	
	/**
	 * Returns a reference to the repository that owns this item.  If this item's state is
	 * <code>UNMANAGED</code>, this method will return null.
	 * 
	 * @return Repository
	 */
	public Repository getRepository();
	
	/**
	 * Returns the namespace that is assigned to the library instance that is represented by this item.
	 * 
	 * @return String
	 */
	public String getNamespace();
	
	/**
	 * Returns the base namespace (without the version component of the URI path) that is assigned to the
	 * library instance that is represented by this item.
	 * 
	 * @return String
	 */
	public String getBaseNamespace();
	
	/**
	 * Returns the filename of the library that is represented by this item without any absolute or relative
	 * path information.
	 * 
	 * @return String
	 */
	public String getFilename();
	
	/**
	 * Returns the name of the library that is represented by this item.
	 * 
	 * @return String
	 */
	public String getLibraryName();
	
	/**
	 * Returns the version identifier of the library that is represented by this repository item.
	 * 
	 * @return String
	 */
	public String getVersion();
	
	/**
	 * Returns the version scheme identifier of the library that is represented by this repository item.
	 * 
	 * @return String
	 */
	public String getVersionScheme();
	
	/**
	 * Returns the status of the library that is represented by this repository item.
	 * 
	 * @return TLLibraryStatus
	 */
	public TLLibraryStatus getStatus();
	
	/**
	 * Indicates the current state of this repository item.
	 * 
	 * @return RepositoryItemState
	 */
	public RepositoryItemState getState();
	
	/**
	 * Returns the ID of the user who has obtained a lock for this item, or null if the file is not locked.
	 * 
	 * @return String
	 */
	public String getLockedByUser();
	
	/**
	 * Returns a URI for this repository item.  By default, the item's namespace is not encoded
	 * with the URI that is returned.
	 * 
	 * @return URI
	 */
	public URI toURI();
	
	/**
	 * Returns a URI for this repository item.  If the 'fullyQualified' flag is true, the item's namespace
	 * will encoded with the URI that is returned.
	 * 
	 * @param fullyQualified  indicates whether the item's namespace should be included in the URI string
	 * @return URI
	 */
	public URI toURI(boolean fullyQualified);
	
}
