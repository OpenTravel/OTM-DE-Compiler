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
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.AbstractSymbolResolver;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;

/**
 * Symbol resolver that utilizes a single symbol table constructed from the members of a
 * <code>TLModel</code> instance.
 * 
 * @author S. Livezey
 */
public class TLModelSymbolResolver extends AbstractSymbolResolver {

    private SymbolTable modelSymbols;
    
	/**
     * Constructor that initializes its symbol table from the members of the given model.
     * 
     * @param model
     *            the model from which to construct the internal symbol table
     */
    public TLModelSymbolResolver(TLModel model) {
        this((model == null) ? new SymbolTable() : SymbolTableFactory
                .newSymbolTableFromModel(model));
    }

    /**
     * Constructor that initializes its symbol table from the members of the given model.
     * 
     * @param symbolTable
     *            the symbol table containing all possible symbol lookups required by this resolver
     */
    public TLModelSymbolResolver(SymbolTable symbolTable) {
        this.modelSymbols = symbolTable;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#resolveQualifiedEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Object resolveQualifiedEntity(String namespace, String localName) {
        Object entity = modelSymbols.getEntity(namespace, localName);

        // If we cannot identify an entity in the requested namespace, attempt to search the
        // anonymous entities for a match
        if (entity == null) {

            // Only attempt anonymous lookups if we are searching the namespace that is
            // currently considered to be the local one
            if ((namespace != null) && (prefixResolver != null)
                    && namespace.equals(prefixResolver.getLocalNamespace())) {
                entity = resolveAnonymousEntity(localName, modelSymbols);
            }
        }
        return entity;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.symbols.AbstractSymbolResolver#resolveQualifiedOperationEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    protected Object resolveQualifiedOperationEntity(String namespace, String localName) {
        return modelSymbols.getOperationEntity(namespace, localName);
    }

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#getEntityNamespace(java.lang.Object)
     */
    @Override
    public String getEntityNamespace(Object entity) {
        return modelSymbols.getNamespaceForEntity(entity);
    }

    /**
	 * @see org.opentravel.schemacompiler.transform.symbols.AbstractSymbolResolver#displaySymbolTable()
	 */
	@Override
	public void displaySymbolTable() {
    	modelSymbols.displayTable();
	}

}
