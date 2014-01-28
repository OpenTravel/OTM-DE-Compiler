
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLValueWithAttributesBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLValueWithAttributes</code> class.
 * 
 * @author S. Livezey
 */
public class TLValueWithAttributesSaveValidator extends TLValueWithAttributesBaseValidator {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLValueWithAttributes target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
			.assertPatternMatch(NAME_XML_PATTERN);

		builder.setProperty("attributes", target.getAttributes()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		builder.setProperty("indicators", target.getIndicators()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();
	
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();

		return builder.getFindings();
	}

}
