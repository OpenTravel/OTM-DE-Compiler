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
 * Factory used within a symbol table to create (or obtain) and register derived entities upon registration of the
 * originating concrete entity.
 * 
 * @param <E> the type of the concrete entity from which the derived entity can be obtained
 * @author S. Livezey
 */
public interface DerivedEntityFactory<E> {

    /**
     * Returns true if the given entity is considered to be an originating entity.
     * 
     * @param originatingEntity the concrete entity to analyze
     * @return boolean
     */
    public abstract boolean isOriginatingEntity(Object originatingEntity);

    /**
     * Created (or obtains) a derived entity using the originating entity and naming information provided and registers
     * it with the given symbol factory.
     * 
     * @param originatingEntity the originating entity from which the derived entity is to be created or obtained
     * @param entityNamespace the namespace to which the originating is (or will be) assigned
     * @param symbols the symbol factory where the derived entity instance is to be registered
     */
    public abstract void registerDerivedEntity(E originatingEntity, String entityNamespace, SymbolTable symbols);

}
