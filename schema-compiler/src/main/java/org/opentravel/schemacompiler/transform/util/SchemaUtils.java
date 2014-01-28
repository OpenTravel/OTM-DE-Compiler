package org.opentravel.schemacompiler.transform.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opentravel.schemacompiler.loader.LibraryModuleImport;
import org.w3._2001.xmlschema.Import;
import org.w3._2001.xmlschema.Include;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;

/**
 * Static utility methods used for the transformation of XML schema imports and includes.
 * 
 * @author S. Livezey
 */
public class SchemaUtils {

    /**
     * Constructs a list of standard library <code>TLNamespaceImport</code> objects using the import
     * and prefix information from the XML schema provided.
     * 
     * @param schema
     *            the schema from which to construct the list of namespace imports
     * @return List<LibraryModuleImport>
     */
    public static List<LibraryModuleImport> getSchemaImports(Schema schema) {
        List<LibraryModuleImport> imports = new ArrayList<LibraryModuleImport>();

        if (schema != null) {
            int nsCount = 1;

            for (OpenAttrs element : schema.getIncludeOrImportOrRedefine()) {
                if (element instanceof Import) {
                    Import schemaImport = (Import) element;
                    String prefix = "ns" + nsCount;

                    nsCount++;
                    imports.add(new LibraryModuleImport(schemaImport.getNamespace(), prefix, Arrays
                            .asList(new String[] { schemaImport.getSchemaLocation() })));
                }
            }
        }
        return imports;
    }

    /**
     * Extracts a list of strings from the given schema representing the schema locations of each
     * included schema.
     * 
     * @param schema
     *            the schema from which to extract the list of includes
     * @return List<String>
     */
    public static List<String> getSchemaIncludes(Schema schema) {
        List<String> includes = new ArrayList<String>();

        if (schema != null) {
            for (OpenAttrs element : schema.getIncludeOrImportOrRedefine()) {
                if (element instanceof Include) {
                    String schemaLocation = ((Include) element).getSchemaLocation();

                    if (schemaLocation != null) {
                        includes.add(schemaLocation);
                    }
                }
            }
        }
        return includes;
    }

}
