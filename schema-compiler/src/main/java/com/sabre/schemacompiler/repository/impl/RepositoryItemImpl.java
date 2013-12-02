/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository.impl;

import java.net.URI;

import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.repository.Repository;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.repository.RepositoryItemState;

/**
 * Implementation of the <code>RepositoryItem</code> interface.
 * 
 * @author S. Livezey
 */
public class RepositoryItemImpl implements RepositoryItem {
	
	private Repository owningRepository;
	private String namespace;
	private String baseNamespace;
	private String filename;
	private String libraryName;
	private String version;
	private String versionScheme;
	private TLLibraryStatus status;
	private RepositoryItemState state;
	private String lockedByUser;
	
	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getRepository()
	 */
	@Override
	public Repository getRepository() {
		return owningRepository;
	}
	
	/**
	 * Assigns the value of the 'repository' field.
	 *
	 * @param owningRepository  the field value to assign
	 */
	public void setRepository(Repository owningRepository) {
		this.owningRepository = owningRepository;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Assigns the value of the 'namespace' field.
	 *
	 * @param namespace  the field value to assign
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getBaseNamespace()
	 */
	@Override
	public String getBaseNamespace() {
		return baseNamespace;
	}
	
	/**
	 * Assigns the value of the 'baseNamespace' field.
	 *
	 * @param baseNamespace  the field value to assign
	 */
	public void setBaseNamespace(String baseNamespace) {
		this.baseNamespace = baseNamespace;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getFilename()
	 */
	@Override
	public String getFilename() {
		return filename;
	}
	
	/**
	 * Assigns the value of the 'filename' field.
	 *
	 * @param filename  the field value to assign
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getLibraryName()
	 */
	public String getLibraryName() {
		return libraryName;
	}

	/**
	 * Assigns the value of the 'libraryName' field.
	 *
	 * @param libraryName  the field value to assign
	 */
	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getVersion()
	 */
	@Override
	public String getVersion() {
		return version;
	}
	
	/**
	 * Assigns the value of the 'version' field.
	 *
	 * @param version  the field value to assign
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getVersionScheme()
	 */
	public String getVersionScheme() {
		return versionScheme;
	}

	/**
	 * Assigns the value of the 'versionScheme' field.
	 *
	 * @param versionScheme  the field value to assign
	 */
	public void setVersionScheme(String versionScheme) {
		this.versionScheme = versionScheme;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getStatus()
	 */
	@Override
	public TLLibraryStatus getStatus() {
		return status;
	}
	
	/**
	 * Assigns the value of the 'status' field.
	 *
	 * @param status  the field value to assign
	 */
	public void setStatus(TLLibraryStatus status) {
		this.status = status;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getState()
	 */
	@Override
	public RepositoryItemState getState() {
		return state;
	}
	
	/**
	 * Assigns the value of the 'state' field.
	 *
	 * @param state  the field value to assign
	 */
	public void setState(RepositoryItemState state) {
		this.state = state;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#getLockedByUser()
	 */
	@Override
	public String getLockedByUser() {
		return lockedByUser;
	}

	/**
	 * Assigns the value of the 'lockedByUser' field.
	 *
	 * @param lockedByUser  the field value to assign
	 */
	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#toURI()
	 */
	@Override
	public URI toURI() {
		return toURI( false );
	}

	/**
	 * @see com.sabre.schemacompiler.repository.RepositoryItem#toURI(boolean)
	 */
	@Override
	public URI toURI(boolean fullyQualified) {
		return RepositoryUtils.newURI( this, fullyQualified );
	}

}
