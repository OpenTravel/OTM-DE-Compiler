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

import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.SchemaCompilerException;

import java.util.Collection;

/**
 * Compiler task used to generate OpenAPI documents for the resources defined in a model, as well as the trimmed schemas
 * (XSD &amp; JSON) that contain the entities upon which those resources depend.
 */
public class OpenApiCompilerTask extends AbstractRESTCompilerTask {

    /**
     * Default constructor.
     */
    public OpenApiCompilerTask() {}

    /**
     * Constructor that assigns the repository manager for this task instance.
     * 
     * @param repositoryManager the repository manager to use when retrieving managed content
     */
    public OpenApiCompilerTask(RepositoryManager repositoryManager) {
        super( repositoryManager );
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection,java.util.Collection)
     */
    @Override
    protected void generateOutput(Collection<TLLibrary> userDefinedLibraries, Collection<XSDLibrary> legacySchemas)
        throws SchemaCompilerException {
        generateOutput( userDefinedLibraries, legacySchemas, CodeGeneratorFactory.OPENAPI_TARGET_FORMAT );
    }

}
