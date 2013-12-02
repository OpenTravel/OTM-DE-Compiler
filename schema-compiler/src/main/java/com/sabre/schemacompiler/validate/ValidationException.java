/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate;

import com.sabre.schemacompiler.util.SchemaCompilerException;

/**
 * Exception that encapsulates a <code>ValidationFindings</code> instance, allowing it to
 * throw multiple findings instead of a single error condition.
 * 
 * @author S. Livezey
 */
public class ValidationException extends SchemaCompilerException {
	
	private ValidationFindings findings;
	
	/**
	 * Constructor that assigns the validation findings to be thrown.
	 * 
	 * @param findings  the validation findings to throw
	 */
	public ValidationException(ValidationFindings findings) {
		super();
		this.findings = findings;
	}
	
	/**
	 * Constructor that assigns the exception message and the validation findings to
	 * be thrown.
	 * 
	 * @param message  the message string for the exception
	 * @param findings  the validation findings to throw
	 */
	public ValidationException(String message, ValidationFindings findings) {
		super(message);
		this.findings = findings;
	}
	
	/**
	 * Returns the findings encapsulated by the exception.
	 * 
	 * @return ValidationFindings
	 */
	public ValidationFindings getFindings() {
		return findings;
	}
	
}
