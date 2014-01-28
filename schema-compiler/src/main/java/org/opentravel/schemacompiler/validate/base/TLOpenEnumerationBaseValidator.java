
package org.opentravel.schemacompiler.validate.base;

import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLOpenEnumeration</code> class.
 * 
 * @author S. Livezey
 */
public class TLOpenEnumerationBaseValidator extends TLValidatorBase<TLOpenEnumeration> {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateChildren(TLOpenEnumeration target) {
		Validator<TLEnumValue> enumValueValidator = getValidatorFactory().getValidatorForClass(TLEnumValue.class);
		ValidationFindings findings = new ValidationFindings();
		
		for (TLEnumValue enumValue : target.getValues()) {
			findings.addAll( enumValueValidator.validate(enumValue) );
		}
		
		if (target.getDocumentation() != null) {
			Validator<TLDocumentation> docValidator = getValidatorFactory().getValidatorForClass(TLDocumentation.class);
			
			findings.addAll( docValidator.validate(target.getDocumentation()) );
		}
		return findings;
	}
	
}
