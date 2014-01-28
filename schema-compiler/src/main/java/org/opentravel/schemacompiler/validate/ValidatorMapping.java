
package org.opentravel.schemacompiler.validate;

/**
 * Configuration element used to associate a target <code>Validatable</code> class
 * with a <code>Validator</code> implementation class.
 * 
 * @param <T>  the validatable type to which this mapping applies
 * @author S. Livezey
 */
public class ValidatorMapping<T extends Validatable> {
	
	private Class<T> targetClass;
	private Class<Validator<T>> validatorClass;
	
	/**
	 * Returns the target class for the validation mapping.
	 *
	 * @return Class<T>
	 */
	public Class<T> getTargetClass() {
		return targetClass;
	}
	
	/**
	 * Assigns the target class for the validation mapping.
	 *
	 * @param targetClass  the target class to assign
	 */
	public void setTargetClass(Class<T> targetClass) {
		this.targetClass = targetClass;
	}
	
	/**
	 * Returns the validator class for the validation mapping.
	 *
	 * @return Class<Validator<T>>
	 */
	public Class<Validator<T>> getValidatorClass() {
		return validatorClass;
	}
	
	/**
	 * Assigns the validator class for the validation mapping.
	 *
	 * @param validatorClass  the validator class to assign
	 */
	public void setValidatorClass(Class<Validator<T>> validatorClass) {
		this.validatorClass = validatorClass;
	}
	
}
