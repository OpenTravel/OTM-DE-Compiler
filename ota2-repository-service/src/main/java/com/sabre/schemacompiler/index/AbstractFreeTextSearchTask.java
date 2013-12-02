/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.index;

import java.io.File;
import java.net.URL;

import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.repository.RepositoryManager;
import com.sabre.schemacompiler.util.URLUtils;

/**
 * Abstract base class for all tasks associated with the <code>FreeTextSearchService</code> of
 * the OTA2.0 repository.
 * 
 * @author S. Livezey
 */
public abstract class AbstractFreeTextSearchTask {
	
	/**
	 * Indicates the version type of an indexed document.
	 */
	protected static enum IndexVersionType { STANDARD, HEAD, HEAD_FINAL };
	
	protected static final String IDENTITY_FIELD       = "identity";
	protected static final String BASE_NAMESPACE_FIELD = "baseNamespace";
	protected static final String FILENAME_FIELD       = "filename";
	protected static final String STATUS_FIELD         = "status";
	protected static final String VERSION_FIELD        = "version";
	protected static final String VERSION_TYPE_FIELD   = "versionType";
	protected static final String CONTENT_FIELD        = "content";
	
	protected RepositoryManager repositoryManager;
	
	/**
	 * Constructor that provides the repository manager to use when accessing all
	 * repository context.
	 * 
	 * @param repositoryManager  the repository that owns all content to be indexed
	 */
	public AbstractFreeTextSearchTask(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
	
	/**
	 * Returns the metadata for the given repository item.
	 * 
	 * @param item  the item whose metadata is to be identified
	 * @return File
	 */
	protected File getMetadataFile(RepositoryItem item) throws RepositoryException {
		URL metadataUrl = repositoryManager.getContentLocation( item );
		File metadataFile = URLUtils.toFile( metadataUrl );
		
		return metadataFile;
	}
	
	/**
	 * Returns the file content of the given repository item.
	 * 
	 * @param item  the item whose content is to be identified
	 * @return File
	 */
	protected File getContentFile(RepositoryItem item) throws RepositoryException {
		URL contentUrl = repositoryManager.getContentLocation( item );
		File contentFile = URLUtils.toFile( contentUrl );
		
		return contentFile;
	}
	
}
