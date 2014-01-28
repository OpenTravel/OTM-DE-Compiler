
package org.opentravel.schemacompiler.transform.symbols;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.transform.SymbolTable;


/**
 * Static utility methods to construct symbol tables using either JAXB libraries or
 * 'TLModel' instances.
 * 
 * @author S. Livezey
 */
public class SymbolTableFactory {
	
	private Map<Class<?>,SymbolTablePopulator<?>> populatorMap = new HashMap<Class<?>,SymbolTablePopulator<?>>();
	
	/**
	 * Private constructor.
	 */
	private SymbolTableFactory() {}
	
	/**
	 * Returns the singleton instance of the symbol table factory from the application context.
	 * 
	 * @return SymbolTableFactory
	 */
	public static SymbolTableFactory getInstance() {
		return (SymbolTableFactory) SchemaCompilerApplicationContext.getContext().getBean(
				SchemaCompilerApplicationContext.SYMBOL_TABLE_FACTORY);
	}
	
	/**
	 * Assigns the collection of <code>SymbolTablePopulators</code> for this factory instance.
	 * 
	 * @param populators  the collection of symbol table populators
	 */
	public void setSymbolTablePopulators(Collection<SymbolTablePopulator<?>> populators) {
		populatorMap.clear();
		
		for (SymbolTablePopulator<?> populator : populators) {
			populatorMap.put(populator.getSourceEntityType(), populator);
		}
	}
	
	/**
	 * Constructs a symbol table for the given source entity.
	 * 
	 * @param sourceEntity  the source entity for which to create a symbol table
	 * @return SymbolTable
	 */
	public SymbolTable newSymbolTable(Object sourceEntity) {
		SymbolTable symbols = new SymbolTable();
		
		appendToSymbolTable(sourceEntity, symbols);
		return symbols;
	}
	
	/**
	 * Constructs a symbol table for the given source entity.
	 * 
	 * @param sourceEntity  the source entity for which to create a symbol table
	 */
	@SuppressWarnings("unchecked")
	public <T> void appendToSymbolTable(T sourceEntity, SymbolTable symbols) {
		if (sourceEntity != null) {
			SymbolTablePopulator<T> populator = (SymbolTablePopulator<T>) populatorMap.get(sourceEntity.getClass());
			
			if (populator != null) {
				populator.populateSymbols(sourceEntity, symbols);
			}
		}
	}
	
	/**
	 * Attempts to resolve the local name of the source object.  If the name cannot be resolved,
	 * null will be returned.
	 * 
	 * @param sourceObject  the source object for which to return the local name
	 * @return String
	 */
	public String getLocalName(Object sourceObject) {
		String localName = null;
		
		for (SymbolTablePopulator<?> populator : populatorMap.values()) {
			localName = populator.getLocalName(sourceObject);
			if (localName != null) break;
		}
		return localName;
	}
	
	/**
	 * Constructs a symbol table for the given source entity.
	 * 
	 * @param sourceEntity  the source entity for which to create a symbol table
	 * @return SymbolTable
	 */
	public static SymbolTable newSymbolTableFromEntity(Object sourceEntity) {
		return getInstance().newSymbolTable(sourceEntity);
	}
	
	/**
	 * Constructs a new symbol table for the given model.
	 * 
	 * @param model  the model for which to contstruct a symbol table
	 * @return SymbolTable
	 */
	public static SymbolTable newSymbolTableFromModel(TLModel model) {
		return newSymbolTableFromEntity(model);
	}
	
}
