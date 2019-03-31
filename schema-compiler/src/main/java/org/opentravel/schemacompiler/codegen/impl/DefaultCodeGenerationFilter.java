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

package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.XSDLibrary;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of the <code>CodeGenerationFilter</code> interface.
 * 
 * @author S. Livezey
 */
public class DefaultCodeGenerationFilter implements CodeGenerationFilter {

    private Set<AbstractLibrary> allowedLibraries = new HashSet<>();
    private Set<XSDLibrary> extensionLibraries = new HashSet<>();
    private Set<LibraryElement> allowedEntities = new HashSet<>();

    /**
     * Adds the given library to the list of libraries that will be allowed by this filter.
     * 
     * @param library the library to allow
     */
    public void addProcessedLibrary(AbstractLibrary library) {
        if ((library != null) && !allowedLibraries.contains( library )) {
            allowedLibraries.add( library );
        }
    }

    /**
     * Adds the given library to the list of schema extensions that will be allowed by this filter.
     * 
     * @param legacySchema the legacy schema for which an extension will be required
     */
    public void addExtensionLibrary(XSDLibrary legacySchema) {
        if ((legacySchema != null) && !extensionLibraries.contains( legacySchema )) {
            extensionLibraries.add( legacySchema );
        }
    }

    /**
     * Adds the given library element to the list of entities that will be allowed by this filter.
     * 
     * @param entity the library element to allow
     */
    public void addProcessedElement(LibraryElement entity) {
        if ((entity != null) && !allowedEntities.contains( entity )) {
            allowedEntities.add( entity );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processLibrary(org.opentravel.schemacompiler.model.AbstractLibrary)
     */
    @Override
    public boolean processLibrary(AbstractLibrary library) {
        return allowedLibraries.contains( library );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processExtendedLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
     */
    @Override
    public boolean processExtendedLibrary(XSDLibrary legacySchema) {
        return extensionLibraries.contains( legacySchema );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processEntity(org.opentravel.schemacompiler.model.LibraryElement)
     */
    @Override
    public boolean processEntity(LibraryElement entity) {
        return allowedEntities.contains( entity );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#addBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
     */
    @Override
    public void addBuiltInLibrary(BuiltInLibrary library) {
        addProcessedLibrary( library );
    }

    /**
     * Displays the contents of this filter to standard output for debugging purposes.
     */
    @SuppressWarnings("squid:S106") // Invalid Sonar finding since this method is only used for console debugging
    public void display() {
        display( System.out );
    }

    /**
     * Displays the contents of this filter for debugging purposes.
     * 
     * @param out the stream to which debugging output should be directed
     */
    public void display(PrintStream out) {
        out.println( "CODE GENERATION FILTER:" );

        for (AbstractLibrary l : allowedLibraries) {
            out.println( "  LIBRARY: " + l.getName() + " / " + l.getNamespace() );
        }
        for (LibraryElement e : allowedEntities) {
            out.println( "  ENTITY : " + e.getValidationIdentity() );
        }
    }

}
