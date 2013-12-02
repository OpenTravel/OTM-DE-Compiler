/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.saver;

import com.sabre.schemacompiler.util.SchemaCompilerException;

/**
 * Exception thrown when errors are encountered when attempting to save library content.
 * 
 * @author S. Livezey
 */
public class LibrarySaveException extends SchemaCompilerException {
	
	/**
	 * Default constructor.
	 */
	public LibrarySaveException() {}

	/**
	 * Constructs an exception with the specified message.
	 * 
	 * @param message  the exception message
	 */
	public LibrarySaveException(String message) {
		super(message);
	}

	/**
	 * Constructs an exception with the specified underlying cause.
	 * 
	 * @param cause  the underlying exception that caused this one
	 */
	public LibrarySaveException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an exception with the specified message and underlying cause.
	 * 
	 * @param message  the exception message
	 * @param cause  the underlying exception that caused this one
	 */
	public LibrarySaveException(String message, Throwable cause) {
		super(message, cause);
	}

}
