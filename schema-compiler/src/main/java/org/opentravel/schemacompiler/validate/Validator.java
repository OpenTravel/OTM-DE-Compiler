/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.validate;

/**
 * Interface for components used to perform validation checks and report findings.
 * 
 * @param <T>  the type of the object to be validated
 * @author S. Livezey
 */
public interface Validator<T extends Validatable> {
	
	/**
	 * Assigns the context that can provide relevant information or resources that extend beyond
	 * the object being validated.
	 * 
	 * @param context  the validation context to assign
	 */
	public void setValidationContext(ValidationContext context);
	
	/**
	 * Returns the factory that created this validator instance.
	 * 
	 * @return ValidatorFactory
	 */
	public ValidatorFactory getValidatorFactory();
	
	/**
	 * Assings a reference to the factory used to create this validator instance.
	 * 
	 * @param factory  the factory that created this validator instance
	 */
	public void setValidatorFactory(ValidatorFactory factory);
	
	/**
	 * Performs all necessary validation checks and returns any findings that were discovered.  If
	 * no findings were reported, null is a valid return value from this method.
	 * 
	 * @param target  the target object to be validated
	 * @return ValidationFindings
	 */
	public ValidationFindings validate(T target);
	
}
