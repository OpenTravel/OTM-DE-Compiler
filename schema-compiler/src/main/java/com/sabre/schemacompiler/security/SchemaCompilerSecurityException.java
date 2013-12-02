/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security;

import com.sabre.schemacompiler.util.SchemaCompilerException;

/**
 * Thrown if an error occurs while attempting to determine whether a user should have write
 * access to a protected namespace.
 * 
 * @author S. Livezey
 */
public class SchemaCompilerSecurityException extends SchemaCompilerException {
	
	/**
	 * Default constructor.
	 */
	public SchemaCompilerSecurityException() {}

	/**
	 * Constructs an exception with the specified message.
	 * 
	 * @param message  the exception message
	 */
	public SchemaCompilerSecurityException(String message) {
		super(message);
	}

	/**
	 * Constructs an exception with the specified underlying cause.
	 * 
	 * @param cause  the underlying exception that caused this one
	 */
	public SchemaCompilerSecurityException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an exception with the specified message and underlying cause.
	 * 
	 * @param message  the exception message
	 * @param cause  the underlying exception that caused this one
	 */
	public SchemaCompilerSecurityException(String message, Throwable cause) {
		super(message, cause);
	}

}
