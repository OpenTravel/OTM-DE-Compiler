
package org.opentravel.schemacompiler.validate.impl;

/**
 * Resolves the identity name of an object during validation, typically for the purpose
 * of identifying duplicate model entities during validation.
 * 
 * @param <E>  the entity type for which names will be resolved
 * @author S. Livezey
 */
public interface IdentityResolver<E> {
	
	/**
	 * Resolves the identity name of the given entity.
	 * 
	 * @param entity  the entity for which to return an identity
	 * @return String
	 */
	public String getIdentity(E entity);
	
}
