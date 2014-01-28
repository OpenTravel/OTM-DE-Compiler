/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
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
	 * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#populateSymbols(java.lang.Object, org.opentravel.schemacompiler.transform.SymbolTable)
	 */
	@Override
	public void populateSymbols(TLLibrary sourceEntity, SymbolTable symbols) {
		configureSymbolTable( symbols );
		populateLibrarySymbols( sourceEntity, symbols );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#getSourceEntityType()
	 */
	@Override
	public Class<TLLibrary> getSourceEntityType() {
		return TLLibrary.class;
	}
	
}
