
package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLLibrary</code> class.
 * 
 * @author S. Livezey
 */
public class TLLibraryBaseValidator extends TLValidatorBase<TLLibrary> {
	
	public static final String ERROR_DUPLICATE_LIBRARY_MEMBER_NAME = "DUPLICATE_LIBRARY_MEMBER_NAME";
	public static final String ERROR_DUPLICATE_SERVICE_NAME        = "DUPLICATE_SERVICE_NAME";
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validate(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	public ValidationFindings validate(TLLibrary target) {
		setContextLibrary(target); // Assign the context library to use for name resolution
		return super.validate(target);
	}

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
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
