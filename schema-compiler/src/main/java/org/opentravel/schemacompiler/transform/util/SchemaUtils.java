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

package org.opentravel.schemacompiler.transform.util;

import org.opentravel.schemacompiler.loader.LibraryModuleImport;
import org.w3._2001.xmlschema.Import;
import org.w3._2001.xmlschema.Include;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Static utility methods used for the transformation of XML schema imports and includes.
 * 
 * @author S. Livezey
 */
public class SchemaUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private SchemaUtils() {}

    /**
     * Constructs a list of standard library <code>TLNamespaceImport</code> objects using the import and prefix
     * information from the XML schema provided.
     * 
     * @param schema the schema from which to construct the list of namespace imports
     * @return List&lt;LibraryModuleImport&gt;
     */
    public static List<LibraryModuleImport> getSchemaImports(Schema schema) {
        List<LibraryModuleImport> imports = new ArrayList<>();

        if (schema != null) {
            int nsCount = 1;

            for (OpenAttrs element : schema.getIncludeOrImportOrRedefine()) {
                if (element instanceof Import) {
                    Import schemaImport = (Import) element;
                    String prefix = "ns" + nsCount;

                    nsCount++;
                    imports.add( new LibraryModuleImport( schemaImport.getNamespace(), prefix,
                        Arrays.asList( schemaImport.getSchemaLocation() ) ) );
                }
            }
        }
        return imports;
    }

    /**
     * Extracts a list of strings from the given schema representing the schema locations of each included schema.
     * 
     * @param schema the schema from which to extract the list of includes
     * @return List&lt;String&gt;
     */
    public static List<String> getSchemaIncludes(Schema schema) {
        List<String> includes = new ArrayList<>();

        if (schema != null) {
            for (OpenAttrs element : schema.getIncludeOrImportOrRedefine()) {
                if (element instanceof Include) {
                    String schemaLocation = ((Include) element).getSchemaLocation();

                    if (schemaLocation != null) {
                        includes.add( schemaLocation );
                    }
                }
            }
        }
        return includes;
    }

}
