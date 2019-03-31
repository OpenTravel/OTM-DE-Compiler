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

package org.opentravel.schemacompiler.codegen.wsdl;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryMemberFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;

/**
 * Code generator implementation used to generate WSDL documents from <code>TLService</code> meta-model components.
 * 
 * <p>
 * The following context variable(s) are required when invoking this code generation module:
 * <ul>
 * <li><code>schemacompiler.OutputFolder</code> - the folder where generated WSDL files should be stored</li>
 * </ul>
 * 
 * @author S. Livezey
 */
public class WsdlLibraryMemberCodeGenerator extends AbstractWsdlCodeGenerator<LibraryMember> {

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(org.opentravel.schemacompiler.model.ModelElement)
     */
    @Override
    protected AbstractLibrary getLibrary(LibraryMember source) {
        return (source == null) ? null : source.getOwningLibrary();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
     */
    @Override
    protected CodeGenerationFilenameBuilder<LibraryMember> getDefaultFilenameBuilder() {
        return new LibraryMemberFilenameBuilder<>();
    }

}
