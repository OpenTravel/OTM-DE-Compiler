package org.opentravel.schemacompiler.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;

/**
 * Provides meta-data information about the compiler.
 * 
 * @author S. Livezey
 */
public class SchemaCompilerInfo {

    private static final String COMPILER_INFO_FILE = "/com/sabre/schemacompiler/compiler-info.properties";

    private static SchemaCompilerInfo defaultInstance = new SchemaCompilerInfo();

    private String compilerVersion = "UNKNOWN";

    /**
     * Default constructor.
     */
    private SchemaCompilerInfo() {
        InputStream is = null;
        try {
            is = CompilerExtensionRegistry.loadResource(COMPILER_INFO_FILE);

            if (is != null) {
                Properties props = new Properties();

                props.load(is);
                compilerVersion = props.getProperty("compiler.version", compilerVersion);
            }
        } catch (IOException e) {
            // No error - just use default values (should never happen)
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Returns the default singleton instance of this class.
     * 
     * @return SchemaCompilerInfo
     */
    public static SchemaCompilerInfo getInstance() {
        return defaultInstance;
    }

    /**
     * Returns the current version of the compiler.
     * 
     * @return String
     */
    public String getCompilerVersion() {
        return compilerVersion;
    }

}
