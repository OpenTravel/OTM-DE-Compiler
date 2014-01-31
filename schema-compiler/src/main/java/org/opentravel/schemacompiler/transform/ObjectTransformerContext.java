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
    public TransformerFactory<?> getTransformerFactory();

    /**
     * Assigns the factory instance to be associated with this context.
     * 
     * @param factory
     *            the factory instance to associated with the context
     */
    public void setTransformerFactory(TransformerFactory<?> factory);

}
