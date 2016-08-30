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

package org.opentravel.schemacompiler.codegen.json;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModel;

/**
 * Handles the construction of qualified type names for JSON schemas.  This is useful
 * when JSON definitions must be consolidated into a single file in order to ignore name
 * collisions from different OTM namespaces.
 */
public class JsonTypeNameBuilder {
	
	private Map<QName,String> prefixRegistry = new HashMap<>();
	
	/**
	 * Constructor that builds the registry of unique prefixes for all libararies in the
	 * given model.
	 * 
	 * @param model  the model from which to construct a prefix registry
	 */
	public JsonTypeNameBuilder(TLModel model) {
		for (AbstractLibrary library : model.getAllLibraries()) {
			QName libName = getLibraryQName( library );
			String prefix = library.getPrefix();
			int counter = 1;
			
			// Compute a unique prefix for every library in the model
			while (prefixRegistry.containsValue( prefix )) {
				prefix = library.getPrefix() + counter;
				counter++;
			}
			prefixRegistry.put( libName,  prefix );
		}
	}
	
	/**
	 * Returns a fully-qualified JSON type name for the given entity.
	 * 
	 * @param entity  the named entity for which to return a qualified type name
	 * @return String
	 */
	public String getJsonTypeName(NamedEntity entity) {
		String prefix = prefixRegistry.get( getLibraryQName( entity.getOwningLibrary() ) );
		
		if (prefix == null) {
			prefix = "unknown";
		}
		return prefix + "_" + JsonSchemaNamingUtils.getGlobalDefinitionName( entity );
	}
	
	/**
	 * Returns a qualified name for the given library.
	 * 
	 * @param library  the library for which to return a qualified name
	 * @return QName
	 */
	private QName getLibraryQName(AbstractLibrary library) {
		return new QName( library.getNamespace(), library.getName() );
	}
	
}
