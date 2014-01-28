
package org.opentravel.schemacompiler.util;

/**
 * Base exception thrown by schema compiler components.
 * 
 * @author S. Livezey
 */
public class SchemaCompilerException extends Exception {

	/**
	 * Default constructor.
	 */
	public SchemaCompilerException() {}

	/**
	 * Constructs an exception with the specified message.
	 * 
	 * @param message  the exception message
	 */
	public SchemaCompilerException(String message) {
		super(message);
	}

	/**
	 * Constructs an exception with the specified underlying cause.
	 * 
	 * @param cause  the underlying exception that caused this one
	 */
	public SchemaCompilerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an exception with the specified message and underlying cause.
	 * 
	 * @param message  the exception message
	 * @param cause  the underlying exception that caused this one
	 */
	public SchemaCompilerException(String message, Throwable cause) {
		super(message, cause);
	}

}
