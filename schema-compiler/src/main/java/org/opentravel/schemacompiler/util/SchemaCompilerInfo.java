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

    private static final String COMPILER_INFO_FILE = "/org/opentravel/schemacompiler/compiler-info.properties";

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
