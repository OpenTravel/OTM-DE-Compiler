/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.validate.impl;

import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.validate.ValidationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Validation context to be used during the validation of <code>TLModel</code> elements.
 * 
 * @author S. Livezey
 */
public class TLModelValidationContext implements ValidationContext {

    private Map<String,Object> validationCache = new HashMap<>();
    private SymbolResolver symbolResolver;
    private TLModel model;

    /**
     * Constructor that creates a validation context for the model provided.
     * 
     * @param model the model that owns all elements to be validated
     */
    public TLModelValidationContext(TLModel model) {
        this.symbolResolver = new TLModelSymbolResolver( model );
        this.model = model;
    }

    /**
     * Returns the model associated with this validation context.
     * 
     * @return TLModel
     */
    public TLModel getModel() {
        return model;
    }

    /**
     * Returns a <code>SymbolResolver</code> that can be used to resolve names for any member type in the context model.
     * 
     * @return SymbolResolver
     */
    public SymbolResolver getSymbolResolver() {
        return symbolResolver;
    }

    /**
     * Returns an entry from the validation context cache, or null if an entry with the specified key has not been
     * defined.
     * 
     * @param cacheKey the key for the validation cache entry to return
     * @return Object
     */
    public Object getContextCacheEntry(String cacheKey) {
        return validationCache.get( cacheKey );
    }

    /**
     * Returns an entry from the validation context cache, or null if an entry with the specified key has not been
     * defined.
     * 
     * @param cacheKey the key for the validation cache entry to return
     * @param entryType the type of the entry to be created if it does not yet exist
     * @param <T> the expected type of the cache entry value
     * @return Object
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextCacheEntry(String cacheKey, Class<T> entryType) {
        validationCache.computeIfAbsent( cacheKey, k -> {
            try {
                return validationCache.put( k, entryType.newInstance() );

            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        } );
        return (T) validationCache.get( cacheKey );
    }

    /**
     * Assigns a key/value entry to the validation context cache.
     * 
     * @param cacheKey the key for the validation cache entry to assign
     * @param cacheValue the value to be associated with the specified key
     */
    public void setContextCacheEntry(String cacheKey, Object cacheValue) {
        validationCache.put( cacheKey, cacheValue );
    }

}
