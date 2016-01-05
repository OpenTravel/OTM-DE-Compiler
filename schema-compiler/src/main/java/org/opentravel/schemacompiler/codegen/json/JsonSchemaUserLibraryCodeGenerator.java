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
package org.opentravel.schemacompiler.codegen.json;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Code generator implementation used to generate XSD documents from meta-model components.
 * 
 * <p>
 * The following context variable(s) are required when invoking this code generation module:
 * <ul>
 *   <li><code>schemacompiler.OutputFolder</code> - the folder where generated JSON schema files should be stored</li>
 *   <li><code>schemacompiler.SchemaFilename</code> - the name of the JSON schema file to be generated (uses library name/version if not specified)</li>
 * </ul>
 */
public class JsonSchemaUserLibraryCodeGenerator extends AbstractJsonSchemaCodeGenerator<TLLibrary> {

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
	 */
	@Override
	protected CodeGenerationFilenameBuilder<TLLibrary> getDefaultFilenameBuilder() {
        return new LibraryFilenameBuilder<TLLibrary>();
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(org.opentravel.schemacompiler.model.ModelElement)
	 */
	@Override
	protected AbstractLibrary getLibrary(TLLibrary source) {
		return source;
	}
	
}
