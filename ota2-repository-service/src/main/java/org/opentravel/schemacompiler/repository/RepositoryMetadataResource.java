/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.repository;

import java.io.File;
import java.io.IOException;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryInfoType;
import org.opentravel.schemacompiler.config.FileResource;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.impl.DefaultRepositoryFileManager;

/**
 * File resource used to access the repository meta-data record.
 * 
 * @author S. Livezey
 */
public class RepositoryMetadataResource extends FileResource<RepositoryInfoType> {
	
	public RepositoryFileManager fileManager;
	
	public RepositoryMetadataResource(File repositoryLocation) {
		super(repositoryLocation);
		this.fileManager = new DefaultRepositoryFileManager(repositoryLocation);
		invalidateResource();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.config.FileResource#loadResource(java.io.File)
	 */
	@Override
	protected RepositoryInfoType loadResource(File dataFile) throws IOException {
		try {
			RepositoryInfoType metadata = null;
			
			if (fileManager != null) {
				metadata = fileManager.loadRepositoryMetadata();
			}
			return metadata;
			
		} catch (RepositoryException e) {
			throw new IOException(e.getMessage());
		}
	}
	
}
