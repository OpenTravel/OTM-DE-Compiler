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

import java.io.File;
import java.util.Collection;

import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * @author Eric.Bronson
 *
 */
public class DocumentationCompileTask extends AbstractCompilerTask {

	private static final String HTML = "HTML";

	/**
	 * Default Constructor
	 */
	public DocumentationCompileTask() {
	}
	
	/**
	 * Static method that will compile HTML documentation to the specified output folder.  If
	 * successful, the file handle that is returned will be the 'index.html' file for the documentation
	 * bundle.  If the compilation was unsuccessful (e.g. due to validation errors), this method will
	 * return null.
	 * 
	 * @param model  the model to be compiled
	 * @param outputFolder  the output folder where HTML documentation will be created
	 * @param findings  holder for validation errors/warnings that are discovered during compilation (may be null)
	 * @return File
	 * @throws SchemaCompilerException  thrown if an error occurs during compilation
	 */
	public static File compileDocumentation(TLModel model, File outputFolder, ValidationFindings findings) throws SchemaCompilerException {
		DocumentationCompileTask task = new DocumentationCompileTask();
		File indexFile = new File( outputFolder, "/index.html" );
		
		task.setOutputFolder( outputFolder.getAbsolutePath() );
		ValidationFindings compilerFindings = task.compileOutput( model );
		
		if (findings != null) {
			findings.addAll( compilerFindings );
		}
		if (!indexFile.exists() ||
				((compilerFindings != null) && compilerFindings.hasFinding( FindingType.ERROR ))) {
			indexFile = null; // Do not return the index file if compilation failed
		}
		return indexFile;
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
