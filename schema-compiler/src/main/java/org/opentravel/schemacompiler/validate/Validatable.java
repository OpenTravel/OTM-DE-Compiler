
package org.opentravel.schemacompiler.validate;

/**
 * Interface used to define a human-readable identify for an object that is capable of
 * being validated.
 * 
 * @author S. Livezey
 */
public interface Validatable {
	
	/**
	 * Returns the human-readable identity string of the object.
	 * 
	 * @return String
	 */
	public String getValidationIdentity();
	
}
