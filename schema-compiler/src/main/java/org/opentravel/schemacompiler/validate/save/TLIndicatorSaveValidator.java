
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLIndicatorBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLIndicator</code> class.
 * 
 * @author S. Livezey
 */
public class TLIndicatorSaveValidator extends TLIndicatorBaseValidator {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLIndicator target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
			.assertPatternMatch(NAME_XML_PATTERN);
		
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();

		return builder.getFindings();
	}
	
}
