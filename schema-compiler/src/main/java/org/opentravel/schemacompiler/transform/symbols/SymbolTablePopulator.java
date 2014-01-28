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
