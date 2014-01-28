
package org.opentravel.schemacompiler.validate.impl;

import java.util.HashMap;
import java.util.Map;

import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.validate.ValidationContext;

/**
 * Validation context to be used during the validation of <code>TLModel</code> elements.
 * 
 * @author S. Livezey
 */
public class TLModelValidationContext implements ValidationContext {
	
	private Map<String,Object> validationCache = new HashMap<String,Object>();
	private SymbolResolver symbolResolver;
	private TLModel model;
	
	/**
	 * Constructor that creates a validation context for the model provided.
	 * 
	 * @param model  the model that owns all elements to be validated
	 */
	public TLModelValidationContext(TLModel model) {
		this.symbolResolver = new TLModelSymbolResolver(model);
		this.model = model;
	}
	
	/**
	 * Returns the model associated with this validation context.
	 */
	public TLModel getModel() {
		return model;
	}
	
	/**
	 * Returns a <code>SymbolResolver</code> that can be used to resolve names for any member
	 * type in the context model.
	 * 
	 * @return SymbolResolver
	 */
	public SymbolResolver getSymbolResolver() {
		return symbolResolver;
	}
	
	/**
	 * Returns an entry from the validation context cache, or null if an entry with the specified
	 * key has not been defined.
	 * 
	 * @param cacheKey  the key for the validation cache entry to return
	 * @return Object
	 */
	public Object getContextCacheEntry(String cacheKey) {
		return validationCache.get(cacheKey);
	}
	
	/**
	 * Assigns a key/value entry to the validation context cache.
	 * 
	 * @param cacheKey  the key for the validation cache entry to assign
	 * @param cacheValue  the value to be associated with the specified key
	 * @return Object
	 */
	public void setContextCacheEntry(String cacheKey, Object cacheValue) {
		validationCache.put(cacheKey, cacheValue);
	}
	
}
