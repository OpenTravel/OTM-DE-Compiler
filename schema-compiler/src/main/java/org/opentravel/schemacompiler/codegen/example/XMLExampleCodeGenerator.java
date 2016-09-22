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
package org.opentravel.schemacompiler.codegen.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.xml.XMLPrettyPrinter;
import org.w3c.dom.Document;

/**
 * Code generator that produces sample XML output for <code>NamedEntity</code>
 * members of the generated libraries.
 * 
 * @author S. Livezey, E. Bronson
 * 
 */
public class XMLExampleCodeGenerator extends AbstractExampleCodeGenerator {

	/**
	 * The file extension for xml, i.e. filename.xml.
	 */
	public static final String XML_FILE_EXTENSION = "xml";

	/**
	 * Default constructor. Initializes the proper file extension.
	 */
	public XMLExampleCodeGenerator() {
		super(XML_FILE_EXTENSION);
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(org.opentravel.schemacompiler.model.TLModelElement,
	 *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	public void doGenerateOutput(TLModelElement source,
			CodeGenerationContext context) throws CodeGenerationException {
		File outputFile = getOutputFile(source, context);
		
		try (OutputStream out = new FileOutputStream(outputFile);) {
			ExampleDocumentBuilder exampleBuilder = new ExampleDocumentBuilder(getOptions(context));
			exampleBuilder.setModelElement((NamedEntity) source);

			// Register the schema location for each library in the model
			registerSchemaLocations(exampleBuilder, source.getOwningModel(),
					context);

			// Generate the XML document and send formatted content to the
			// output file
			Document domDocument = exampleBuilder.buildTree();
			new XMLPrettyPrinter().formatDocument(domDocument, out);

			addGeneratedFile(outputFile);

		} catch (Throwable t) {
			throw new CodeGenerationException(t);
		}
	}

	/**
	 * Registers schema output locations for all libraries in the specified
	 * model.
	 * 
	 * @param builder
	 *            the builder for which schema locations should be registered
	 * @param model
	 *            the model containing all possible libraries to be resolved
	 * @param context
	 *            the code generation context
	 */
	private void registerSchemaLocations(ExampleDocumentBuilder builder,
			TLModel model, CodeGenerationContext context) {
		if (model != null) {
			// Register the schema locations of all libraries
			for (AbstractLibrary library : model.getAllLibraries()) {
				String schemaPath = context
						.getValue(CodeGenerationContext.CK_EXAMPLE_SCHEMA_RELATIVE_PATH);
				String schemaFilename = getFilenameBuilder().buildFilename(
						library, "xsd");
				String schemaLocation;

				if (library instanceof TLLibrary) {
					schemaLocation = schemaPath + schemaFilename;

				} else if (library instanceof XSDLibrary) {
					schemaLocation = schemaPath
							+ getLegacySchemaOutputLocation(context) + "/"
							+ schemaFilename;

				} else { // Built-in library
					schemaLocation = schemaPath
							+ getBuiltInSchemaOutputLocation(context) + "/"
							+ schemaFilename;
				}
				builder.addSchemaLocation(library, schemaLocation);
			}
		}
	}
}
