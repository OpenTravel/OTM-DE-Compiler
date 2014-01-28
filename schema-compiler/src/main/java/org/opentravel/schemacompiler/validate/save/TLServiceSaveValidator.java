
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLServiceBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLService</code> class.
 * 
 * @author S. Livezey
 */
public class TLServiceSaveValidator extends TLServiceBaseValidator {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLService target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
			.assertPatternMatch(NAME_XML_PATTERN);

		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();

		builder.setProperty("operations", target.getOperations()).setFindingType(FindingType.WARNING)
			.assertMinimumSize(1);

		return builder.getFindings();
	}

}
