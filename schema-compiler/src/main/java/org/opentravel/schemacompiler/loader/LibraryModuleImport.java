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

package org.opentravel.schemacompiler.loader;

import java.util.List;

/**
 * Provides a normalized version of the import declarations from a JAXB library.
 * 
 * @author S. Livezey
 */
public class LibraryModuleImport {

    private String namespace;
    private String prefix;
    private List<String> fileHints;

    /**
     * Full constructor.
     * 
     * @param namespace the namespace that was imported by the library
     * @param prefix the prefix assigned to the imported namespace
     * @param fileHints the file hints for associated resource locations
     */
    public LibraryModuleImport(String namespace, String prefix, List<String> fileHints) {
        this.namespace = namespace;
        this.prefix = prefix;
        this.fileHints = fileHints;
    }

    /**
     * Returns the namespace that was imported by the library.
     * 
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Assigns the namespace that was imported by the library.
     *
     * @param namespace the namespace URI to assign
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns the prefix assigned to the imported namespace.
     * 
     * @return String
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Assigns the prefix assigned to the imported namespace.
     *
     * @param prefix the namespace prefix to assign
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Returns the file hints for associated resource locations.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getFileHints() {
        return fileHints;
    }

    /**
     * Assigns the file hints for associated resource locations.
     *
     * @param fileHints the list of file hints to assign
     */
    public void setFileHints(List<String> fileHints) {
        this.fileHints = fileHints;
    }

}
