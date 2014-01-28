
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLFacetBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLFacet</code> class.
 * 
 * @author S. Livezey
 */
public class TLFacetSaveValidator extends TLFacetBaseValidator {
	
	public static final String ERROR_EXTENSIBILITY_NOT_ALLOWED = "EXTENSIBILITY_NOT_ALLOWED";
	public static final String ERROR_EXTENDS_NOT_ALLOWED       = "EXTENDS_NOT_ALLOWED";
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLFacet target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		TLFacetType facetType = target.getFacetType();
		
		builder.setProperty("aliases", target.getAliases()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		if (facetType != null) {
			if (facetType.isContextual()) {
				
				builder.setProperty("context", target.getContext()).setFindingType(FindingType.WARNING)
					.assertPatternMatch(NAME_XML_PATTERN);
				
				builder.setProperty("label", target.getLabel()).setFindingType(FindingType.WARNING)
					.assertPatternMatch(NAME_XML_PATTERN);
			}
		}
	
		builder.setProperty("attributes", target.getAttributes()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();
	
		builder.setProperty("elements", target.getElements()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();
	
		builder.setProperty("indicators", target.getIndicators()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();
		return builder.getFindings();
	}

}
