/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemacompiler.transform.symbols;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

    private Map<String,String> prefixMappings = new HashMap<>();
    private String localNamespace;

    /**
     * Constructor that specifies the underlying library that defines the prefix mappings.
     * 
     * @param libraryInfo
     *            the JAXB library instance that defines the prefix mappings
     * @throws IllegalArgumentException
     *             thrown if the instance provided is not recognized as a supported JAXB library
     */
    public JaxbLibraryPrefixResolver(LibraryModuleInfo<?> libraryInfo) {
        for (LibraryModuleImport nsImport : libraryInfo.getImports()) {
            String prefix = nsImport.getPrefix();
            String namespace = nsImport.getNamespace();

            if ((prefix != null) && (prefix.length() > 0) && (namespace != null)
                    && (namespace.length() > 0)) {
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
		
		if ((namespace != null) && (namespace.equals(localNamespace)
				|| namespace.equals(AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE))) {
			prefix = "";
		} else {
			for (Entry<String,String> entry : prefixMappings.entrySet()) {
				if (entry.getValue().equals(namespace)) {
					prefix = entry.getKey();
					break;
				}
			}
		}
		return prefix;
	}
	
}
