/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.util;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.transform.PrefixResolver;

/**
 * Prefix resolver that obtains its prefix mappings from an <code>AbstractLibrary</code> instance.
 *
 * @author S. Livezey
 */
public class LibraryPrefixResolver implements PrefixResolver {
	
	private AbstractLibrary library;
	
	/**
	 * Constructor that specifies the underlying library that defines the prefix mappings.
	 * 
	 * @param library  the library that defines the prefix mappings
	 */
	public LibraryPrefixResolver(AbstractLibrary library) {
		this.library = library;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.transform.PrefixResolver#getLocalNamespace()
	 */
	@Override
	public String getLocalNamespace() {
		return library.getNamespace();
	}

	/**
	 * @see org.opentravel.schemacompiler.transform.PrefixResolver#resolveNamespaceFromPrefix(java.lang.String)
	 */
	@Override
	public String resolveNamespaceFromPrefix(String prefix) {
		String namespace = (library == null) ? null : library.getNamespaceForPrefix(prefix);
		
		if ((namespace == null) && (library != null)) {
			namespace = findNamespaceFromPrefix(prefix, library.getOwningModel());
		}
		return namespace;
	}

	/**
	 * @see org.opentravel.schemacompiler.transform.PrefixResolver#getPrefixForNamespace(java.lang.String)
	 */
	@Override
	public String getPrefixForNamespace(String namespace) {
		return (library == null) ? null : library.getPrefixForNamespace(namespace);
	}
	
	/**
	 * If the namespace could not be resolved from the library's import declarations, this method searches
	 * the <code>TLModel</code> to determine whether a single namespace can be resolved from the prefix.  If
	 * so, that namespace URI is returned.  If not, this method will return null.
	 * 
	 * @param prefix  the prefix for which to attempt resolution
	 * @param model  the model to search
	 * @return String
	 */
	private String findNamespaceFromPrefix(String prefix, TLModel model) {
		String namespace = null;
		
		if ((prefix != null) && (model != null)) {
			for (AbstractLibrary library : model.getAllLibraries()) {
				if (prefix.equals( library.getPrefix() )) {
					if (namespace == null) {
						namespace = library.getNamespace();
					} else {
						// break and return null if the prefix maps to more than one namespace
						namespace = null;
						break;
					}
				}
			}
		}
		return namespace;
	}
}
