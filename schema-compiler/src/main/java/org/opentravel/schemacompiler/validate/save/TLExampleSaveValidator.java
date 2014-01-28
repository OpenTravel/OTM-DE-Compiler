
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLExample</code> class.
 *
 * @author S. Livezey
 */
public class TLExampleSaveValidator extends TLValidatorBase<TLExample> {
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLExample target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("context", target.getContext()).setFindingType(FindingType.WARNING)
			.assertNotNull();
		
		return builder.getFindings();
	}

}
