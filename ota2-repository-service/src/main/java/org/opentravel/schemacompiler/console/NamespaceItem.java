
package org.opentravel.schemacompiler.console;

import java.io.File;
import java.util.Date;

import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Encapsulates a single item to display in the repository browser, search results, or item
 * details page.
 * 
 * @author S. Livezey
 */
public class NamespaceItem {
	
	private String baseNamespace;
	private String filename;
	private String label;
	private String version;
	private TLLibraryStatus status;
	private RepositoryItemState state;
	private Date lastModified;
	private boolean canDelete;
	
	/**
	 * Constructor that represents a root namespace.
	 * 
	 * @param rootNamespace  the root namespace to display
	 */
	public NamespaceItem(String rootNamespace) {
		this.label = this.baseNamespace = rootNamespace;
	}
	
	/**
	 * Constructor that represents a child namespace of the one currently being displayed.
	 * 
	 * @param rootNamespace  the base namespace of the item to display
	 * @param label  the display label for the item
	 */
	public NamespaceItem(String baseNamespace, String label) {
		this.baseNamespace = baseNamespace;
		this.label = label;
	}
	
	/**
	 * Constructor that represents a child namespace of the one currently being displayed.
	 * 
	 * @param itemUrl  the relative URL of the namespace item
	 */
	public NamespaceItem(RepositoryItem item) {
		this.baseNamespace = item.getBaseNamespace();
		this.filename = item.getFilename();
		this.label = item.getLibraryName();
		this.version = item.getVersion();
		this.status = item.getStatus();
		this.state = item.getState();
		this.lastModified = getLastModified( item );
	}
	
	/**
	 * Returns the value of the 'baseNamespace' field.
	 *
	 * @return String
	 */
	public String getBaseNamespace() {
		return baseNamespace;
	}

	/**
	 * Returns the value of the 'filename' field.
	 *
	 * @return String
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the value of the 'label' field.
	 *
	 * @return String
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the value of the 'version' field.
	 *
	 * @return String
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the value of the 'status' field.
	 *
	 * @return TLLibraryStatus
	 */
	public TLLibraryStatus getStatus() {
		return status;
	}

	/**
	 * Returns the value of the 'state' field.
	 *
	 * @return RepositoryItemState
	 */
	public RepositoryItemState getState() {
		return state;
	}

	/**
	 * Returns the value of the 'lastModified' field.
	 *
	 * @return Date
	 */
	public Date getLastModified() {
		return lastModified;
	}
	
	/**
	 * Returns the value of the 'canDelete' field.
	 *
	 * @return boolean
	 */
	public boolean isCanDelete() {
		return canDelete;
	}

	/**
	 * Assigns the value of the 'canDelete' field.
	 *
	 * @param canDelete  the field value to assign
	 */
	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	/**
	 * Returns the last modified timestamp of the metadata file for the specified repository item.
	 * 
	 * @param item  the repository item for which to return a timestamp
	 * @return Date
	 */
	private Date getLastModified(RepositoryItem item) {
		Date lastModified = null;
		
		if (item != null) {
			try {
				RepositoryManager repositoryManager = item.getRepository().getManager();
				File metadataFile = repositoryManager.getFileManager().getLibraryMetadataLocation(
						item.getBaseNamespace(), item.getFilename(), item.getVersion() );
				
				if ((metadataFile != null) && metadataFile.exists()) {
					lastModified = new Date( metadataFile.lastModified() );
				}
			} catch (Throwable t) {
				t.printStackTrace(System.out);
			}
		}
		return lastModified;
	}
	
}
