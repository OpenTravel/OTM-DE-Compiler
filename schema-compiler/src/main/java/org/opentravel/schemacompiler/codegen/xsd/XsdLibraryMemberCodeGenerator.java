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
package org.opentravel.schemacompiler.codegen.xsd;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.CodegenNamespacePrefixMapper;
import org.opentravel.schemacompiler.codegen.impl.LibraryMemberFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;

/**
 * Code generator implementation used to generate XSD documents from meta-model components.
 * 
 * <p>
 * The following context variable(s) are required when invoking this code generation module:
 * <ul>
 * <li><code>schemacompiler.OutputFolder</code> - the folder where generated XSD schema files should
 * be stored</li>
 * <li><code>schemacompiler.SchemaFilename</code> - the name of the XSD schema file to be generated
 * (uses library name/version if not specified)</li>
 * </ul>
 * 
 * @author S. Livezey
 */
public class XsdLibraryMemberCodeGenerator extends AbstractXsdCodeGenerator<LibraryMember> {

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.AbstractXsdCodeGenerator#canGenerateOutput(org.opentravel.schemacompiler.model.TLModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected boolean canGenerateOutput(LibraryMember source, CodeGenerationContext context) {
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(java.lang.Object)
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
        return new LibraryMemberFilenameBuilder<LibraryMember>();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getMarshaller(org.opentravel.schemacompiler.model.TLModelElement,
     *      org.w3._2001.xmlschema.Schema)
     */
    @Override
    protected Marshaller getMarshaller(LibraryMember source, org.w3._2001.xmlschema.Schema schema)
            throws JAXBException {
        Marshaller m = jaxbContext.createMarshaller();

        m.setSchema(validationSchema);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CodegenNamespacePrefixMapper(
                getLibrary(source), false, this, schema));
        return m;
    }

}
