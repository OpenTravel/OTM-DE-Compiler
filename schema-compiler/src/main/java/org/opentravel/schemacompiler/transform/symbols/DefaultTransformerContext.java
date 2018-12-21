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
package org.opentravel.schemacompiler.transform.symbols;

import java.util.HashMap;
import java.util.Map;

import org.opentravel.schemacompiler.transform.ObjectTransformerContext;
import org.opentravel.schemacompiler.transform.TransformerFactory;

/**
 * Default implementation of the <code> implements ObjectTransformerContext</code>.
 * 
 * @author S. Livezey
 */
public class DefaultTransformerContext implements ObjectTransformerContext {

    private Map<String,Object> contextCache = new HashMap<>();
    private TransformerFactory<?> factory;

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformerContext#getTransformerFactory()
     */
    @SuppressWarnings("unchecked")
	@Override
    public <C extends ObjectTransformerContext> TransformerFactory<C> getTransformerFactory() {
        return (TransformerFactory<C>) factory;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformerContext#setTransformerFactory(org.opentravel.schemacompiler.transform.TransformerFactory)
     */
    @Override
    public void setTransformerFactory(TransformerFactory<?> factory) {
        this.factory = factory;
    }
    
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformerContext#getContextCacheEntry(java.lang.String)
	 */
	@Override
	public Object getContextCacheEntry(String cacheKey) {
		return contextCache.get( cacheKey );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformerContext#setContextCacheEntry(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setContextCacheEntry(String cacheKey, Object cacheValue) {
		contextCache.put( cacheKey, cacheValue );
	}
	
}
