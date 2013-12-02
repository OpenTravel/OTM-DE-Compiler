/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform;

/**
 * Base class to be extended by components that can resolve and assign forward-declared objects.
 * 
 * @author S. Livezey
 */
public abstract class ForwardDeclarationResolver {
	
	private ForwardDeclaration forwardDeclaration;
	
	/**
	 * Constructor that assigns the forward declaration to be resolved.
	 * 
	 * @param forwardDeclaration  the forward declaration to be resolved
	 */
	public ForwardDeclarationResolver(ForwardDeclaration forwardDeclaration) {
		this.forwardDeclaration = forwardDeclaration;
	}
	
	/**
	 * Resolves the entity represented by the forward declaration associated with this resolver
	 * instance.  This method will return true if the forward declaration was resolved successfully,
	 * false otherwise.
	 * 
	 * @param symbolTable  the symbol table from which the forward-declared entity can be resolved
	 * @return boolean
	 */
	public boolean resolveForwardDeclaration(SymbolTable symbolTable) {
		return resolveForwardDeclaration(forwardDeclaration, symbolTable);
	}
	
	/**
	 * Resolves the entity represented by the specified forward declaration.  This method will return
	 * true if the forward declaration was resolved successfully, false otherwise.
	 * 
	 * @param forwardDeclaration  the forward declaration to resolve
	 * @param symbolTable  the symbol table from which the forward-declared entity can be resolved
	 * @return boolean
	 */
	protected abstract boolean resolveForwardDeclaration(ForwardDeclaration forwardDeclaration, SymbolTable symbolTable);
	
}
