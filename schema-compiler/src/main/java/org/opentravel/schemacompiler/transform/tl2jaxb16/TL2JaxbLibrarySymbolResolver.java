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

package org.opentravel.schemacompiler.transform.tl2jaxb16;

import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.AbstractSymbolResolver;

/**
 * Symbol resolver used during the conversion from <code>TLModel</code> elements to JAXB elements.
 * 
 * @author S. Livezey
 */
public class TL2JaxbLibrarySymbolResolver extends AbstractSymbolResolver {

    private SymbolTable symbolTable;

    /**
     * Constructor that initializes the source and target symbol tables utilized by this <code>SymbolResolver</code>.
     * 
     * @param symbolTable the symbol table to use for the transformation process
     */
    public TL2JaxbLibrarySymbolResolver(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#resolveQualifiedEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Object resolveQualifiedEntity(String namespace, String localName) {
        return symbolTable.getEntity( namespace, localName );
    }

    /**
     * @see org.opentravel.schemacompiler.transform.symbols.AbstractSymbolResolver#resolveQualifiedOperationEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    protected Object resolveQualifiedOperationEntity(String namespace, String localName) {
        return symbolTable.getOperationEntity( namespace, localName );
    }

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#getEntityNamespace(java.lang.Object)
     */
    @Override
    public String getEntityNamespace(Object entity) {
        return symbolTable.getNamespaceForEntity( entity );
    }

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#setAnonymousEntityFilter(org.opentravel.schemacompiler.transform.AnonymousEntityFilter)
     */
    @Override
    public void setAnonymousEntityFilter(AnonymousEntityFilter filter) {
        // Not required for TL -> JAXB transformation
    }

    /**
     * @see org.opentravel.schemacompiler.transform.symbols.AbstractSymbolResolver#displaySymbolTable()
     */
    @Override
    public void displaySymbolTable() {
        symbolTable.displayTable();
    }

}
