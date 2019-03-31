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

import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles the construction of qualified type names for JSON schemas. This is useful when JSON definitions must be
 * consolidated into a single file in order to ignore name collisions from different OTM namespaces.
 */
public class JsonTypeNameBuilder {

    private static String counterChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private Map<String,String> prefixRegistry = new HashMap<>();
    private Set<String> localNameCollisions = new HashSet<>();

    /**
     * Constructor that builds the registry of unique prefixes for all libararies in the given model.
     * 
     * @param model the model from which to construct a prefix registry
     * @param filter the code generation filter (may be null)
     */
    public JsonTypeNameBuilder(TLModel model, CodeGenerationFilter filter) {
        initLibraryPrefixes( model );

        // Search for local name collisions in the generated schemas
        SymbolTable symbolTable = SymbolTableFactory.newSymbolTableFromModel( model );
        Set<String> allLocalNames = new HashSet<>();

        for (String ns : symbolTable.getNamespaces()) {
            for (String localName : symbolTable.getLocalNames( ns )) {
                LibraryElement entity = (LibraryElement) symbolTable.getEntity( ns, localName );

                if ((filter == null) || filter.processEntity( entity )) {
                    if (allLocalNames.contains( localName )) {
                        localNameCollisions.add( localName );
                    }
                    allLocalNames.add( localName );
                }
            }
        }
    }

    /**
     * Compute a unique prefix for every library in the model.
     * 
     * @param model the model containing the libraries to be processed
     */
    private void initLibraryPrefixes(TLModel model) {
        for (AbstractLibrary library : model.getAllLibraries()) {
            String libNS = library.getNamespace();

            if (!prefixRegistry.containsKey( libNS )) {
                String basePrefix = library.getPrefix().replaceAll( "-", "" ).toUpperCase();
                String prefix = basePrefix;
                int counter = 0;

                while (prefixRegistry.containsValue( prefix )) {
                    prefix = basePrefix + counterChars.charAt( counter );
                    counter++;
                }
                prefixRegistry.put( libNS, prefix );
            }
        }
    }

    /**
     * Returns a fully-qualified JSON type name for the given entity.
     * 
     * @param entity the named entity for which to return a qualified type name
     * @return String
     */
    public String getJsonTypeName(NamedEntity entity) {
        String typeName = JsonSchemaNamingUtils.getGlobalDefinitionName( entity );

        if (localNameCollisions.contains( typeName )) {
            String suffix = prefixRegistry.get( entity.getNamespace() );

            if (suffix == null) {
                suffix = "unknown";
            }
            typeName += "_" + suffix;
        }
        return typeName;
    }

}
