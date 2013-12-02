/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.transform.symbols;

import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.transform.SymbolTable;

/**
 * Symbol table populator that creates named entries using the members of the <code>TLLibrary</code>
 * instance provied.
 *
 * @author S. Livezey
 */
public class TLLibrarySymbolTablePopulator extends AbstractTLSymbolTablePopulator<TLLibrary> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.symbols.SymbolTablePopulator#populateSymbols(java.lang.Object, com.sabre.schemacompiler.transform.SymbolTable)
	 */
	@Override
	public void populateSymbols(TLLibrary sourceEntity, SymbolTable symbols) {
		configureSymbolTable( symbols );
		populateLibrarySymbols( sourceEntity, symbols );
	}
	
	/**
	 * @see com.sabre.schemacompiler.transform.symbols.SymbolTablePopulator#getSourceEntityType()
	 */
	@Override
	public Class<TLLibrary> getSourceEntityType() {
		return TLLibrary.class;
	}
	
}
