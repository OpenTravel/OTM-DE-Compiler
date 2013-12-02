/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository;

/**
 * Exception that is thrown when a remote repository is not accessible from the network.
 *
 * @author S. Livezey
 */
public class RepositoryUnavailableException extends RepositoryException {
	
	/**
	 * Default constructor.
	 */
	public RepositoryUnavailableException() {}

	/**
	 * Constructs an exception with the specified message.
	 * 
	 * @param message  the exception message
	 */
	public RepositoryUnavailableException(String message) {
		super(message);
	}

	/**
	 * Constructs an exception with the specified underlying cause.
	 * 
	 * @param cause  the underlying exception that caused this one
	 */
	public RepositoryUnavailableException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an exception with the specified message and underlying cause.
	 * 
	 * @param message  the exception message
	 * @param cause  the underlying exception that caused this one
	 */
	public RepositoryUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
