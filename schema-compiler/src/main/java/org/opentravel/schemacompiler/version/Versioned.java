
package org.opentravel.schemacompiler.version;

import org.opentravel.schemacompiler.model.NamedEntity;

/**
 * Interface to be implemented by all model elements that can be identified by a version
 * number.
 * 
 * @author S. Livezey
 */
public interface Versioned extends NamedEntity {
	
	/**
	 * Returns the version identifier of the model element.
	 *
	 * @return String
	 */
	public String getVersion();
	
	/**
	 * Returns the string identifier for the version scheme of this model element.
	 *
	 * @return String
	 */
	public String getVersionScheme();
	
	/**
	 * Returns the fully-qualified namespace for the model element, including the version
	 * identifier suffix.
	 *
	 * @return String
	 */
	public String getNamespace();
	
	/**
	 * Returns the base namespace for the model element.  The base namespace URI is the portion
	 * that does not include the version identifier suffix.
	 *
	 * @return String
	 */
	public String getBaseNamespace();
	
	/**
	 * Returns true if the 'otherVersionedItem' meets both of the following conditions:
	 * <ul>
	 *   <li>Both versioned items are of the same type (cores, business objects, etc.) and have the same local name</li>
	 *   <li>The other item's library is assigned to the same version scheme and base namespace as this one.</li>
	 *   <li>The other item's version is considered to be later than this item's version according to the version scheme.</li>
	 * </ul>
	 * 
	 * @param otherVersionedItem  the other versioned item with which to compare this one
	 * @return boolean
	 */
	public boolean isLaterVersion(Versioned otherVersionedItem);

}
