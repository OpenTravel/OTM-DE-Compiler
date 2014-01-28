
package org.opentravel.schemacompiler.validate.compile;

import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLContextBaseValidator;
import org.opentravel.schemacompiler.validate.impl.IdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLContext</code> class.
 *
 * @author S. Livezey
 */
public class TLContextCompileValidator extends TLContextBaseValidator {
	
	public static final String ERROR_INVALID_CONTEXT = "INVALID_CONTEXT";

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLContext target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("contextId", target.getContextId()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank();
		
		builder.setProperty("applicationContext", target.getApplicationContext()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank();
		
		builder.setProperty("applicationContext", target.getOwningLibrary().getContexts()).setFindingType(FindingType.ERROR)
			.assertNoDuplicates(
				new IdentityResolver<TLContext>() {
					public String getIdentity(TLContext entity) {
						return (entity == null) ? null : entity.getApplicationContext();
					}
				});
		
		builder.setProperty("contextId", target.getOwningLibrary().getContexts()).setFindingType(FindingType.ERROR)
			.assertNoDuplicates(
				new IdentityResolver<TLContext>() {
					public String getIdentity(TLContext entity) {
						return (entity == null) ? null : entity.getContextId();
					}
				});
	
		return builder.getFindings();
	}

}
