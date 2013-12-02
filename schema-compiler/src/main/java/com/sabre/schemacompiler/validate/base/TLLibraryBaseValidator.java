/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.base;

import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLInclude;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLLibrary</code> class.
 * 
 * @author S. Livezey
 */
public class TLLibraryBaseValidator extends TLValidatorBase<TLLibrary> {
	
	public static final String ERROR_DUPLICATE_LIBRARY_MEMBER_NAME = "DUPLICATE_LIBRARY_MEMBER_NAME";
	public static final String ERROR_DUPLICATE_SERVICE_NAME        = "DUPLICATE_SERVICE_NAME";
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validate(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	public ValidationFindings validate(TLLibrary target) {
		setContextLibrary(target); // Assign the context library to use for name resolution
		return super.validate(target);
	}

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateChildren(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLLibrary target) {
		Validator<TLContext> contextValidator = getValidatorFactory().getValidatorForClass(TLContext.class);
		Validator<TLInclude> includeValidator = getValidatorFactory().getValidatorForClass(TLInclude.class);
		TLValidationBuilder builder = newValidationBuilder(target);
		
		for (TLContext context : target.getContexts()) {
			builder.addFindings( contextValidator.validate(context) );
		}
		
		for (TLInclude include : target.getIncludes()) {
			builder.addFindings( includeValidator.validate(include) );
		}
		
		// Now validate each individual member with its own validator
		for (LibraryMember member : target.getNamedMembers()) {
			Validator<LibraryMember> childValidator = getValidatorFactory().getValidatorForTarget(member);
			
			if (childValidator != null) {
				builder.addFindings( childValidator.validate(member) );
			}
		}
		return builder.getFindings();
	}
	
}
