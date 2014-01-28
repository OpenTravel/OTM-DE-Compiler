
package org.opentravel.schemacompiler.model;

/**
 * Interface to be implemented by any element that can specify a reference to a context declaration.
 * 
 * @author S. Livezey
 */
public interface TLContextReferrer extends LibraryElement {
	
	/**
	 * Returns the ID of the context declaration to which this entity refers.
	 * 
	 * @return String
	 */
	public String getContext();
	
	/**
	 * Assigns the ID of the context declaration to which this entity refers.
	 * 
	 * @param context  the context ID to assign
	 */
	public void setContext(String context);
	
}
