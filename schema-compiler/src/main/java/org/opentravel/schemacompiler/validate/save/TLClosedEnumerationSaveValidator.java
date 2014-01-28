
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLClosedEnumerationBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLClosedEnumeration</code> class.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumerationSaveValidator extends TLClosedEnumerationBaseValidator {
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLClosedEnumeration target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
			.assertPatternMatch(NAME_XML_PATTERN);
	
		builder.setProperty("values", target.getValues()).setFindingType(FindingType.WARNING)
			.assertMinimumSize(1);
	
		return builder.getFindings();
	}

}
