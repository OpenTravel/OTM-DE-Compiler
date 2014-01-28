
package org.opentravel.schemacompiler.transform.symbols;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.transform.SymbolTable;

/**
 * Symbol table populator that creates named entries using the members of the <code>TLModel</code>
 * instance provied.
 * 
 * @author S. Livezey
 */
public class TLModelSymbolTablePopulator extends AbstractTLSymbolTablePopulator<TLModel> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#populateSymbols(java.lang.Object, org.opentravel.schemacompiler.transform.SymbolTable)
	 */
	@Override
	public void populateSymbols(TLModel sourceEntity, SymbolTable symbols) {
		configureSymbolTable( symbols );
		
		for (AbstractLibrary library : sourceEntity.getAllLibraries()) {
			populateLibrarySymbols( library, symbols );
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#getSourceEntityType()
	 */
	@Override
	public Class<TLModel> getSourceEntityType() {
		return TLModel.class;
	}
	
}
