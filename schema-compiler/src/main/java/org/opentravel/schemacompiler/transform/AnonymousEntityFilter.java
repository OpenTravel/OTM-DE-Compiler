/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform;

/**
 * Used in conjunction with a symbol resolver to identify valid symbol references to "Anonymous"
 * entities that can belong to multiple namespaces.  The behavior generally follows the chameleon
 * pattern employed during entity resolution for XML schemas.
 * 
 * @author S. Livezey
 */
public interface AnonymousEntityFilter {
	
	public static final String ANONYMOUS_PSEUDO_NAMESPACE = "http://chameleon.anonymous/ns";
	
	/**
	 * Resolves an anonymous (aka. chameleon) entity from the symbol table provided.
	 * 
	 * @param localName  the local name of the entity to resolve
	 * @param symbols  the symbol table from which the resulting entity should be resolved
	 * @return Object
	 */
	public Object getAnonymousEntity(String localName, SymbolTable symbols);
	
}
