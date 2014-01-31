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

import org.opentravel.schemacompiler.transform.SymbolTable;

/**
 * Interface to be implemented by components that are cabable of creating named entries in a
 * <code>SymbolTable</code>.
 * 
 * @param <S>
 *            the type of entity for which symbols will be populated
 * @author S. Livezey
 */
public interface SymbolTablePopulator<S> {

    /**
     * Adds new symbols to the given symbol table using information and/or sub-components from the
     * source entity provided.
     * 
     * @param sourceEntity
     *            the source entity from which symbols will be derived
     * @param symbols
     *            the symbol table to populate
     */
    public void populateSymbols(S sourceEntity, SymbolTable symbols);

    /**
     * Attempts to resolve the local name of the source object. If the name cannot be resolved by
     * this populator instance, null will be returned.
     * 
     * @param sourceObject
     *            the source object for which to return the local name
     * @return String
     */
    public String getLocalName(Object sourceObject);

    /**
     * Returns the source entity type for this symbol table populator.
     * 
     * @return Class<S>
     */
    public Class<S> getSourceEntityType();

}
