/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sabre.schemacompiler.validate.Validatable;
import com.sabre.schemacompiler.validate.ValidationContext;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.ValidatorFactory;

/**
 * Validator implementation that can combine and apply multiple validator implementations to
 * a single <code>Validatable</code> object.
 * 
 * @param <T>  the type of the object to be validated
 * @author S. Livezey
 */
public class CompositeValidator<T extends Validatable> implements Validator<T> {
	
	private List<Validator<T>> validators = new ArrayList<Validator<T>>();
	private ValidationContext context;
	private ValidatorFactory factory;
	
	/**
	 * Default constructor.
	 */
	public CompositeValidator() {}
	
	/**
	 * Constructor that initializes this composite validator with a single nested
	 * validator instance.
	 * 
	 * @param validator  the validator to add to this composite
	 */
	public CompositeValidator(Validator<T> validator) {
		addValidator( validator );
	}
	
	/**
	 * Constructor that initializes this composite validator with the given collection
	 * of nested validators.
	 * 
	 * @param validators  the list of validators to add to this composite
	 */
	public CompositeValidator(Collection<Validator<T>> validators) {
		if (validators != null) {
			for (Validator<T> validator : validators) {
				addValidator( validator );
			}
		}
	}
	
	/**
	 * Adds the given validator to the current list maintained by this composite.
	 * 
	 * @param validator  the validator to add to this composite
	 */
	public void addValidator(Validator<T> validator) {
		if (validator != null) {
			validators.add(validator);
			validator.setValidationContext(context);
			validator.setValidatorFactory(factory);
		}
	}
	
	/**
	 * Removes the given validator from the current list maintained by this composite.
	 * 
	 * @param validator  the validator to remove from this composite
	 */
	public void removeValidator(Validator<T> validator) {
		if (validator != null) {
			validators.remove(validator);
			validator.setValidationContext(null);
			validator.setValidatorFactory(null);
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validator#setValidationContext(com.sabre.schemacompiler.validate.ValidationContext)
	 */
	@Override
	public void setValidationContext(ValidationContext context) {
		this.context = context;
		
		for (Validator<T> validator : validators) {
			validator.setValidationContext(context);
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validator#getValidatorFactory()
	 */
	@Override
	public ValidatorFactory getValidatorFactory() {
		return factory;
	}
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validator#setValidatorFactory(com.sabre.schemacompiler.validate.ValidatorFactory)
	 */
	@Override
	public void setValidatorFactory(ValidatorFactory factory) {
		this.factory = factory;
		
		for (Validator<T> validator : validators) {
			validator.setValidatorFactory(factory);
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validator#validate(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	public ValidationFindings validate(T target) {
		ValidationFindings findings = new ValidationFindings();
		
		for (Validator<T> validator : validators) {
			findings.addAll( validator.validate(target) );
		}
		return findings;
	}
	
}
