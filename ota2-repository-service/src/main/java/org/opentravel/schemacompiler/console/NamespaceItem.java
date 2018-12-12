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
package org.opentravel.schemacompiler.console;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Encapsulates a single item to display in the repository browser, search results, or item details
 * page.
 * 
 * @author S. Livezey
 */
public class NamespaceItem {

    private static Log log = LogFactory.getLog(NamespaceItem.class);
    
    private String baseNamespace;
    private String filename;
    private String label;
    private String version;
    private String description;
    private TLLibraryStatus status;
    private RepositoryItemState state;
    private Date lastModified;
    private boolean canDelete;

    /**
     * Constructor that represents a root namespace.
     * 
     * @param rootNamespace
     *            the root namespace to display
     */
    public NamespaceItem(String rootNamespace) {
        this.label = this.baseNamespace = rootNamespace;
    }

    /**
     * Constructor that represents a child namespace of the one currently being displayed.
     * 
     * @param rootNamespace
     *            the base namespace of the item to display
     * @param label
     *            the display label for the item
     */
    public NamespaceItem(String baseNamespace, String label) {
        this.baseNamespace = baseNamespace;
        this.label = label;
    }

    /**
     * Constructor for a namespace item that is a managed repository item.
     * 
     * @param item  the managed repository item
     */
    public NamespaceItem(RepositoryItem item) {
        this.baseNamespace = item.getBaseNamespace();
        this.filename = item.getFilename();
        this.label = item.getLibraryName();
        this.version = item.getVersion();
        this.status = item.getStatus();
        this.state = item.getState();
        this.lastModified = getLastModified(item);
    }

    /**
     * Constructor for a namespace item that is a managed repository item obtained
     * from the free-text search index.
     * 
     * @param searchIndexItem  the repository item record retreived from the search index
     */
    public NamespaceItem(LibrarySearchResult searchIndexItem) {
    	RepositoryItem item = searchIndexItem.getRepositoryItem();
    	
        this.baseNamespace = item.getBaseNamespace();
        this.filename = item.getFilename();
        this.label = item.getLibraryName();
        this.version = item.getVersion();
        this.status = item.getStatus();
        this.state = item.getState();
        this.lastModified = getLastModified(item);
        this.description = searchIndexItem.getItemDescription();
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
	 * Returns the value of the 'DESCRIPTION' field.
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
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
     * @param canDelete
     *            the field value to assign
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
    public static Date getLastModified(RepositoryItem item) {
        Date lastModified = null;

        if (item != null) {
            try {
                RepositoryManager repositoryManager = item.getRepository().getManager();
                File metadataFile = repositoryManager.getFileManager().getLibraryMetadataLocation(
                        item.getBaseNamespace(), item.getFilename(), item.getVersion());

                if ((metadataFile != null) && metadataFile.exists()) {
                    lastModified = new Date(metadataFile.lastModified());
                }
            } catch (Exception e) {
                log.warn("Error calculating last modified date for item " + item.getFilename(), e);
            }
        }
        return lastModified;
    }

}
