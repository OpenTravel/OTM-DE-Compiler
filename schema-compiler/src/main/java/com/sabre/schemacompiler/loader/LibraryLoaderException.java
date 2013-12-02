/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.loader;

import com.sabre.schemacompiler.util.SchemaCompilerException;

/**
 * Exception thrown when errors are encountered when attempting to load library content.
 * 
 * @author S. Livezey
 */
public class LibraryLoaderException extends SchemaCompilerException {
	
	/**
	 * Default constructor.
	 */
	public LibraryLoaderException() {}

	/**
	 * Constructs an exception with the specified message.
	 * 
	 * @param message  the exception message
	 */
	public LibraryLoaderException(String message) {
		super(message);
	}

	/**
	 * Constructs an exception with the specified underlying cause.
	 * 
	 * @param cause  the underlying exception that caused this one
	 */
	public LibraryLoaderException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an exception with the specified message and underlying cause.
	 * 
	 * @param message  the exception message
	 * @param cause  the underlying exception that caused this one
	 */
	public LibraryLoaderException(String message, Throwable cause) {
		super(message, cause);
	}

}
