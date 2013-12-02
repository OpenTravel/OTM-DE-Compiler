/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.model;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus;

/**
 * Indicates the lifecycle status of a <code>TLLibrary</code>.
 * 
 * @author S. Livezey
 */
public enum TLLibraryStatus {
	
	/** Indicates that the contents of a library may be modified without increasing its version number. */
	DRAFT( LibraryStatus.DRAFT ),
	
	/** Indicates that a new version must be created before modifying the content of a library. */
	FINAL( LibraryStatus.FINAL );
	
	private LibraryStatus repositoryStatus;
	
	/**
	 * Constructor that associates the corresponding repository status with the new value.
	 * 
	 * @param repositoryStatus  the associated repository status value
	 */
	private TLLibraryStatus(LibraryStatus repositoryStatus) {
		this.repositoryStatus = repositoryStatus;
	}
	
	/**
	 * Returns the corresponding repository status for the value.
	 * 
	 * @return LibraryStatus
	 */
	public LibraryStatus toRepositoryStatus() {
		return repositoryStatus;
	}
	
}
