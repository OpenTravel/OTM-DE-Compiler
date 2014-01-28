
package org.opentravel.schemacompiler.security;

import org.opentravel.schemacompiler.repository.RepositoryException;

/**
 * Exception that is thrown when a web service client request fails to meet the authentication
 * and/or authorization requirements for a requested operation.
 * 
 * @author S. Livezey
 */
public class RepositorySecurityException extends RepositoryException {
	
	/**
	 * Default constructor.
	 */
	public RepositorySecurityException() {}

	/**
	 * Constructs an exception with the specified message.
	 * 
	 * @param message  the exception message
	 */
	public RepositorySecurityException(String message) {
		super(message);
	}

	/**
	 * Constructs an exception with the specified underlying cause.
	 * 
	 * @param cause  the underlying exception that caused this one
	 */
	public RepositorySecurityException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an exception with the specified message and underlying cause.
	 * 
	 * @param message  the exception message
	 * @param cause  the underlying exception that caused this one
	 */
	public RepositorySecurityException(String message, Throwable cause) {
		super(message, cause);
	}

}
