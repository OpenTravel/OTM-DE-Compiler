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

import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.task.AbstractCompilerTask;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * @author Eric.Bronson
 *
 */
public class DocumentationCompileTask extends AbstractCompilerTask {

	private static final String HTML = "HTML";

	public static final String CODE_GENERATOR_FACTORY = "javaCodeGeneratorFactory";

	/**
	 * Default Constructor
	 */
	public DocumentationCompileTask() {
	}

	/**
	 * Validates an existing <code>TLModel</code> instance and compiles the
	 * output using the options assigned for this task.
	 * 
	 * @param model
	 *            the model that contains all of the libraries for which to
	 *            compile output
	 * @return ValidationFindings
	 * @throws SchemaCompilerException
	 *             thrown if an unexpected error occurs during the compilation
	 *             process
	 */
	public ValidationFindings compileOutput(TLModel model)
			throws SchemaCompilerException {
		CodeGenerator<TLModel> docGenerator = CodeGeneratorFactory.getInstance().newCodeGenerator(HTML, TLModel.class);
		docGenerator.generateOutput(model, createContext());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.task.AbstractCompilerTask#generateOutput
	 * (java.util.Collection, java.util.Collection)
	 */
	@Override
	protected void generateOutput(Collection<TLLibrary> userDefinedLibraries,
			Collection<XSDLibrary> legacySchemas)
			throws SchemaCompilerException {
		if(!userDefinedLibraries.isEmpty()){
			TLModel model = userDefinedLibraries.iterator().next().getOwningModel();
			compileOutput(model);
		}
	}

}
