
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLSimpleFacetBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLSimpleFacet</code> class.
 * 
 *
 * @author S. Livezey
 */
public class TLSimpleFacetSaveValidator extends TLSimpleFacetBaseValidator {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLSimpleFacet target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.WARNING)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		return builder.getFindings();
	}
	
}
