
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLBusinessObjectBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLBusinessObject</code> class.
 * 
 * @author S. Livezey
 */
public class TLBusinessObjectSaveValidator extends TLBusinessObjectBaseValidator {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLBusinessObject target) {
		TLValidationBuilder builder = newValidationBuilder(target).addFindings( super.validateFields(target) );
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.WARNING)
			.assertPatternMatch(NAME_XML_PATTERN);
	
		return builder.getFindings();
	}

}
