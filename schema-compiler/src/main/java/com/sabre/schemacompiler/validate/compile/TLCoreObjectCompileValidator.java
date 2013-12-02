/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.codegen.util.FacetCodegenUtils;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLFacetOwner;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.base.TLCoreObjectBaseValidator;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.version.PatchVersionHelper;
import com.sabre.schemacompiler.version.VersionScheme;
import com.sabre.schemacompiler.version.VersionSchemeException;

/**
 * Validator for the <code>TLCoreObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLCoreObjectCompileValidator extends TLCoreObjectBaseValidator {

	public static final String ERROR_INVALID_VERSION_EXTENSION = "INVALID_VERSION_EXTENSION";
	public static final String ERROR_ILLEGAL_PATCH             = "ILLEGAL_PATCH";

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLCoreObject target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank()
			.assertPatternMatch(NAME_XML_PATTERN);

		builder.setProperty("roles", target.getRoleEnumeration().getRoles()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
	
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		if (!target.getSummaryFacet().declaresContent()) {
			TLFacetOwner baseEntity = FacetCodegenUtils.getFacetOwnerExtension(target);
			
			if (baseEntity == null) {
				builder.addFinding(FindingType.ERROR, "summaryFacet", TLValidationBuilder.ERROR_UNDER_MINIMUM_SIZE);
			}
		}

		checkSchemaNamingConflicts( target, builder );
		
		// Validate versioning rules
		try {
			PatchVersionHelper helper = new PatchVersionHelper();
			VersionScheme vScheme = helper.getVersionScheme( target );
			
			if ((vScheme != null) && vScheme.isPatchVersion(target.getNamespace())) {
				builder.addFinding(FindingType.ERROR, "name", ERROR_ILLEGAL_PATCH);
			}
			
			if (isInvalidVersionExtension(target)) {
				builder.addFinding(FindingType.ERROR, "versionExtension", ERROR_INVALID_VERSION_EXTENSION);
			}
			checkMajorVersionNamingConflicts(target, builder);
			
		} catch (VersionSchemeException e) {
			// Ignore - Invalid version scheme error will be reported when the owning library is validated
		}
		
		return builder.getFindings();
	}

}
