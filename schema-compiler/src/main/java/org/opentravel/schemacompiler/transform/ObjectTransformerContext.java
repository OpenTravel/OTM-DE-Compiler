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
package org.opentravel.schemacompiler.transform;

/**
 * Base interface for contexts that provide required information for the transformation process
 * beyond the immediate scope of the object being converted.
 * 
 * @author S. Livezey
 */
public interface ObjectTransformerContext {

    /**
     * Returns the transformer factory associated with the context.
     * 
     * @return TransformerFactory<?>
     */
    public <C extends ObjectTransformerContext> TransformerFactory<C> getTransformerFactory();

    /**
     * Assigns the factory instance to be associated with this context.
     * 
     * @param factory
     *            the factory instance to associated with the context
     */
    public void setTransformerFactory(TransformerFactory<?> factory);

    /**
     * Returns an entry from the context cache, or null if an entry with the specified
     * key has not been defined.
     * 
     * @param cacheKey  the key for the validation cache entry to return
     * @return Object
     */
    public Object getContextCacheEntry(String cacheKey);
    
    /**
     * Assigns a key/value entry to the context cache.
     * 
     * @param cacheKey  the key for the validation cache entry to assign
     * @param cacheValue  the value to be associated with the specified key
     */
    public void setContextCacheEntry(String cacheKey, Object cacheValue);
    
}
