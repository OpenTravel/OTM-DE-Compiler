/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform;

/**
 * Factory used within a symbol table to create (or obtain) and register derived entities upon
 * registration of the originating concrete entity.
 * 
 * @param <E>  the type of the concrete entity from which the derived entity can be obtained
 * @author S. Livezey
 */
public interface DerivedEntityFactory<E> {
	
	/**
	 * Returns true if the given entity is considered to be an originating entity.
	 * 
	 * @param originatingEntity  the concrete entity to analyze
	 * @return boolean
	 */
	public abstract boolean isOriginatingEntity(Object originatingEntity);
	
	/**
	 * Created (or obtains) a derived entity using the originating entity and naming information provided and
	 * registers it with the given symbol factory.
	 * 
	 * @param originatingEntity  the originating entity from which the derived entity is to be created or obtained
	 * @param entityNamespace  the namespace to which the originating is (or will be) assigned
	 * @param symbols  the symbol factory where the derived entity instance is to be registered
	 */
	public abstract void registerDerivedEntity(E originatingEntity, String entityNamespace, SymbolTable symbols);
	
}
