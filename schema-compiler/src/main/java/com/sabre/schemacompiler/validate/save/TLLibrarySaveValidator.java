/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.save;

import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLLibraryBaseValidator;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLLibrary</code> class.
 * 
 * @author S. Livezey
 */
public class TLLibrarySaveValidator extends TLLibraryBaseValidator {
	
	public static final String ERROR_DUPLICATE_LIBRARY_MEMBER_NAME = "DUPLICATE_LIBRARY_MEMBER_NAME";
	public static final String ERROR_DUPLICATE_SERVICE_NAME        = "DUPLICATE_SERVICE_NAME";
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLLibrary target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("libraryUrl", target.getLibraryUrl()).setFindingType(FindingType.WARNING)
			.assertNotNull();
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
			.assertPatternMatch(NAME_FILE_PATTERN);

		builder.setProperty("versionScheme", target.getVersionScheme()).setFindingType(FindingType.WARNING)
			.assertNotNullOrBlank();
		
		builder.setProperty("namespace", target.getNamespace()).setFindingType(FindingType.WARNING)
			.assertNotNullOrBlank();

		builder.setProperty("prefix", target.getPrefix()).setFindingType(FindingType.WARNING)
			.assertNotNullOrBlank();

		builder.setProperty("includes", target.getIncludes()).setFindingType(FindingType.WARNING)
			.assertContainsNoNullElements();

		builder.setProperty("namedMembers", target.getNamedMembers()).setFindingType(FindingType.WARNING)
			.assertContainsNoNullElements();
		
		return builder.getFindings();
	}

}
