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

package org.opentravel.schemacompiler.codegen;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.XSDLibrary;

/**
 * Filter used to determine which libraries and member artifacts should be processed during code generation.
 * 
 * @author S. Livezey
 */
public interface CodeGenerationFilter {

    /**
     * Returns true if artifacts for the given library should be produced during code generation processing.
     * 
     * @param library the library to check
     * @return boolean
     */
    public boolean processLibrary(AbstractLibrary library);

    /**
     * Returns true if an extension schema for the given legacy schema should be produced during code generation
     * processing.
     * 
     * @param legacySchema the legacy schema to check
     * @return boolean
     */
    public boolean processExtendedLibrary(XSDLibrary legacySchema);

    /**
     * Returns true if output components should be generated for the given library element during code generation
     * processing.
     * 
     * @param entity the library member to check
     * @return boolean
     */
    public boolean processEntity(LibraryElement entity);

    /**
     * Adds the given built-in library to the list of libraries allowed by this filter.
     * 
     * @param library the built-in library to add
     */
    public void addBuiltInLibrary(BuiltInLibrary library);

}
