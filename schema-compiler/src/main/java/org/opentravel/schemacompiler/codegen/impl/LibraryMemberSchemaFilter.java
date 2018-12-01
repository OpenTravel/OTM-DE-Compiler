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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.XSDLibrary;

/**
 * Code generation filter that only allows generation of artifacts derived directly from a single
 * library member.
 * 
 * @author S. Livezey
 */
public class LibraryMemberSchemaFilter implements CodeGenerationFilter {

    private List<AbstractLibrary> builtInLibraries = new ArrayList<>();
    private LibraryMember libraryMember;
    private CodeGenerationFilter libraryFilter;

    /**
     * Constructor that specifies the library member for which output artifacts are to be generated.
     * 
     * @param libraryMember
     *            the target library member
     * @param libraryFilter
     *            code generation filter that contains information about the libraries upon which
     *            the member's schema will depend
     */
    public LibraryMemberSchemaFilter(LibraryMember libraryMember, CodeGenerationFilter libraryFilter) {
        this.libraryMember = libraryMember;
        this.libraryFilter = libraryFilter;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processLibrary(org.opentravel.schemacompiler.model.AbstractLibrary)
     */
    @Override
    public boolean processLibrary(AbstractLibrary library) {
        return builtInLibraries.contains(library) || libraryFilter.processLibrary(library); // delegate
                                                                                            // this
                                                                                            // value
                                                                                            // to
                                                                                            // the
                                                                                            // nested
                                                                                            // filter
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processExtendedLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
     */
    @Override
    public boolean processExtendedLibrary(XSDLibrary legacySchema) {
        return libraryFilter.processExtendedLibrary(legacySchema); // delegate this value to the
                                                                   // nested filter
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processEntity(org.opentravel.schemacompiler.model.LibraryElement)
     */
    @Override
    public boolean processEntity(LibraryElement entity) {
        return (entity == libraryMember);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#addBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
     */
    @Override
    public void addBuiltInLibrary(BuiltInLibrary library) {
        builtInLibraries.add(library);
    }

}
