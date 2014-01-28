
package org.opentravel.schemacompiler.model;


/**
 * Implemented by all model elements that represent named entities.
 * 
 * @author S. Livezey
 */
public interface NamedEntity extends LibraryElement {
	
	/**
	 * Returns the namespace to which the entity definition is assigned.
	 * 
	 * @return String
	 */
	public String getNamespace();
	
	/**
	 * Returns the local name (within the assigned namespace) of the entity.
	 * 
	 * @return String
	 */
	public String getLocalName();
	
}
