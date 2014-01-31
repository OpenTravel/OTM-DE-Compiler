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

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.SymbolTable;

/**
 * Symbol table populator that creates named entries using the members of the <code>TLLibrary</code>
 * instance provied.
 * 
 * @author S. Livezey
 */
public class TLLibrarySymbolTablePopulator extends AbstractTLSymbolTablePopulator<TLLibrary> {

    /**
     * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#populateSymbols(java.lang.Object,
     *      org.opentravel.schemacompiler.transform.SymbolTable)
     */
    @Override
    public void populateSymbols(TLLibrary sourceEntity, SymbolTable symbols) {
        configureSymbolTable(symbols);
        populateLibrarySymbols(sourceEntity, symbols);
    }

    /**
     * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#getSourceEntityType()
     */
    @Override
    public Class<TLLibrary> getSourceEntityType() {
        return TLLibrary.class;
    }

}
