/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.symbols;

import java.util.HashMap;
import java.util.Map;

import org.opentravel.schemacompiler.loader.LibraryModuleImport;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.transform.PrefixResolver;

/**
 * Prefix resolver that obtains its prefix mappings from a JAXB Library instance.
 * 
 * @author S. Livezey
 */
public class JaxbLibraryPrefixResolver implements PrefixResolver {
	
	private Map<String,String> prefixMappings = new HashMap<String, String>();
	private String localNamespace;
	
	/**
	 * Constructor that specifies the underlying library that defines the prefix mappings.
	 * 
	 * @param libraryInfo  the JAXB library instance that defines the prefix mappings
	 * @throws IllegalArgumentException  thrown if the instance provided is not recognized
	 *				as a supported JAXB library
	 */
	public JaxbLibraryPrefixResolver(LibraryModuleInfo<?> libraryInfo) {
		for (LibraryModuleImport nsImport : libraryInfo.getImports()) {
			String prefix = nsImport.getPrefix();
			String namespace = nsImport.getNamespace();
			
			if ((prefix != null) && (prefix.length() > 0)
					&& (namespace != null) && (namespace.length() > 0)) {
				prefixMappings.put(prefix, namespace);
			}
		}
		localNamespace = libraryInfo.getNamespace();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.transform.PrefixResolver#getLocalNamespace()
	 */
	@Override
	public String getLocalNamespace() {
		return localNamespace;
	}

	/**
	 * @see org.opentravel.schemacompiler.transform.PrefixResolver#resolveNamespaceFromPrefix(java.lang.String)
	 */
	@Override
	public String resolveNamespaceFromPrefix(String prefix) {
		return prefixMappings.get(prefix);
	}

	/**
	 * @see org.opentravel.schemacompiler.transform.PrefixResolver#getPrefixForNamespace(java.lang.String)
	 */
	@Override
	public String getPrefixForNamespace(String namespace) {
		String prefix = null;
		
		if ((namespace != null) &&
				(namespace.equals(localNamespace) || namespace.equals(AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE))) {
			prefix = "";
		} else {
			for (String p : prefixMappings.keySet()) {
				String ns = prefixMappings.get(p);
				
				if (ns.equals(namespace)) {
					prefix = p;
					break;
				}
			}
		}
		return prefix;
	}
	
}
