/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.symbols;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.transform.SymbolTable;

/**
 * Symbol table populator that creates named entries using the members of the <code>TLModel</code>
 * instance provied.
 * 
 * @author S. Livezey
 */
public class TLModelSymbolTablePopulator extends AbstractTLSymbolTablePopulator<TLModel> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.symbols.SymbolTablePopulator#populateSymbols(java.lang.Object, com.sabre.schemacompiler.transform.SymbolTable)
	 */
	@Override
	public void populateSymbols(TLModel sourceEntity, SymbolTable symbols) {
		configureSymbolTable( symbols );
		
		for (AbstractLibrary library : sourceEntity.getAllLibraries()) {
			populateLibrarySymbols( library, symbols );
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.transform.symbols.SymbolTablePopulator#getSourceEntityType()
	 */
	@Override
	public Class<TLModel> getSourceEntityType() {
		return TLModel.class;
	}
	
}
