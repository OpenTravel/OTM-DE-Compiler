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
package org.opentravel.schemacompiler.task;

import java.util.Collection;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Compiler task used to generate full (non-trimmed) schema output for the libraries of a model.
 * 
 * @author S. Livezey
 */
public class XmlSchemaCompilerTask extends AbstractSchemaCompilerTask {

    /**
     * Constructor that specifies the filename of the project for which schemas are being compiled.
     * 
     * @param projectFilename
     *            the name of the project (.otp) file
     */
    public XmlSchemaCompilerTask(String projectFilename) {
        this.projectFilename = projectFilename;
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection,
     *      java.util.Collection)
     */
    @Override
    protected void generateOutput(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException {
        CodeGenerationContext context = createContext();
//        AbstractLibrary primaryLibrary = getPrimaryLibrary();
        CodeGenerationFilter filter = null;

        // Generate schemas for all of the user-defined libraries
        /* TODO: Re-enable filter code once GUI has been implemented 
        if (primaryLibrary != null) {
        	filter = new DependencyFilterBuilder()
        			.setIncludeExtendedLegacySchemas( true )
        			.addLibrary( primaryLibrary ).buildFilter();
        }
        */
        compileXmlSchemas(userDefinedLibraries, legacySchemas, context, null, filter);

        // Generate example files if required
        if (isGenerateExamples()) {
            generateExampleArtifacts(
            		userDefinedLibraries, context, new LibraryFilenameBuilder<AbstractLibrary>(),
            		filter, CodeGeneratorFactory.XML_TARGET_FORMAT);
        }
    }

}
