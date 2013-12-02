/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.impl;

import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.transform.SymbolTable;
import com.sabre.schemacompiler.transform.symbols.AbstractSymbolResolver;
import com.sabre.schemacompiler.transform.symbols.SymbolTableFactory;

/**
 * Symbol resolver that utilizes a single symbol table constructed from the members of
 * a <code>TLModel</code> instance.
 * 
 * @author S. Livezey
 */
public class TLModelSymbolResolver extends AbstractSymbolResolver {
	
	private SymbolTable modelSymbols;
	
	/**
	 * Constructor that initializes its symbol table from the members of the given model.
	 * 
	 * @param model  the model from which to construct the internal symbol table
	 */
	public TLModelSymbolResolver(TLModel model) {
		this( (model == null) ? new SymbolTable() : SymbolTableFactory.newSymbolTableFromModel(model) );
	}
	
	/**
	 * Constructor that initializes its symbol table from the members of the given model.
	 * 
	 * @param symbolTable  the symbol table containing all possible symbol lookups required by this resolver
	 */
	public TLModelSymbolResolver(SymbolTable symbolTable) {
		this.modelSymbols = symbolTable;
	}
	
	/**
	 * @see com.sabre.schemacompiler.transform.SymbolResolver#resolveQualifiedEntity(java.lang.String, java.lang.String)
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
	 * @see com.sabre.schemacompiler.transform.symbols.AbstractSymbolResolver#resolveQualifiedOperationEntity(java.lang.String, java.lang.String)
	 */
	@Override
	protected Object resolveQualifiedOperationEntity(String namespace, String localName) {
		return modelSymbols.getOperationEntity(namespace, localName);
	}

	/**
	 * @see com.sabre.schemacompiler.transform.SymbolResolver#getEntityNamespace(java.lang.Object)
	 */
	@Override
	public String getEntityNamespace(Object entity) {
		return modelSymbols.getNamespaceForEntity(entity);
	}

}
