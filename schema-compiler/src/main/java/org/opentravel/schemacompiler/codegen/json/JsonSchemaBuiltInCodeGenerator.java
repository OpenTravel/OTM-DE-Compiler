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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.validate.ValidationException;

/**
 * Code generator for built-in JSON schemas referenced in a library meta-model. The behavior of this
 * code generator is to simply copy the content of the file from its source location in the local
 * classpath to the proper output location.
 */
public class JsonSchemaBuiltInCodeGenerator extends AbstractCodeGenerator<BuiltInLibrary> {
	
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(org.opentravel.schemacompiler.model.ModelElement)
	 */
	@Override
	protected AbstractLibrary getLibrary(BuiltInLibrary source) {
		return source;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(org.opentravel.schemacompiler.model.ModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	public void doGenerateOutput(BuiltInLibrary source, CodeGenerationContext context) throws CodeGenerationException {
        switch (source.getBuiltInType()) {
            case TLLIBRARY_BUILTIN:
                generateUserLibraryOutput(source, context);
                break;
            case XSD_BUILTIN:
                generateJsonLibraryOutput(source, context);
                break;
            default:
                // skip code generation of the schema-for-schemas built-in
        }
    }
	
    /**
     * Generates output for user-defined (<code>TLLibrary</code>) built-in libraries.
     * 
     * @param source  the built-in library for which to generate output
     * @param context  the code generation context
     * @throws CodeGenerationException  thrown if an error occurs during code generation
     */
    protected void generateUserLibraryOutput(BuiltInLibrary source, CodeGenerationContext context)
            throws CodeGenerationException {
        try {
            JsonSchemaUserBuiltInCodeGenerator delegateCodeGenerator = new JsonSchemaUserBuiltInCodeGenerator();

            delegateCodeGenerator.setFilenameBuilder(getFilenameBuilder());
            addGeneratedFiles(delegateCodeGenerator.generateOutput(source, context));
        	
        } catch (ValidationException e) {
            throw new CodeGenerationException(
                    "Validation error encountered while generating schema for built-in library.");
        }
    }
    
    /**
     * Generates output for JSON built-in libraries.
     * 
     * @param source  the built-in library for which to generate output
     * @param context  the code generation context
     * @throws CodeGenerationException  thrown if an error occurs during code generation
     */
    protected void generateJsonLibraryOutput(BuiltInLibrary source, CodeGenerationContext context)
            throws CodeGenerationException {
        if (!source.getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
        	try (InputStream is = source.getSchemaDeclaration()
                    .getContent(CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT)) {
            	if (is != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader( is ));
                    File outputFile = getOutputFile(source, context);
                    String line = null;

                    try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.write(LINE_SEPARATOR);
                        }
                        addGeneratedFile(outputFile);
                    }
            	}
            	
            } catch (IOException e) {
                throw new CodeGenerationException(e);
        	}
        }
    }
    
    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.TLModelElement,org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected File getOutputFile(BuiltInLibrary source, CodeGenerationContext context) {
        File outputFolder = getOutputFolder(context, null);
        String filename = context.getValue(CodeGenerationContext.CK_SCHEMA_FILENAME);

        if ((filename == null) || filename.trim().equals("")) {
            filename = getFilenameBuilder().buildFilename(source, JsonSchemaCodegenUtils.JSON_SCHEMA_FILENAME_EXT);
        }
        return new File(outputFolder, filename);
    }
    
    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFolder(org.opentravel.schemacompiler.codegen.CodeGenerationContext,java.net.URL)
     */
    @Override
    protected File getOutputFolder(CodeGenerationContext context, URL libraryUrl) {
        File outputFolder = super.getOutputFolder(context, libraryUrl);
        String builtInSchemaFolder = getBuiltInSchemaOutputLocation(context);

        if (builtInSchemaFolder != null) {
            outputFolder = new File(outputFolder, builtInSchemaFolder);
            if (!outputFolder.exists())
                outputFolder.mkdirs();
        }
        return outputFolder;
    }
    
    /**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getFilenameBuilder()
	 */
	@Override
	public CodeGenerationFilenameBuilder<BuiltInLibrary> getFilenameBuilder() {
		return (item, fileExtension) -> {
	        String fileExt = (fileExtension.length() == 0) ? "" : ("." + fileExtension);
	        String filename = item.getName();

	        if (filename.toLowerCase().endsWith(".xsd")) {
	        	filename = filename.substring( 0, filename.length() - 4 );
	        }
	        if (!filename.toLowerCase().endsWith(fileExt)) {
	            filename += fileExt;
	        }
	        return filename;
		};
	}

	/**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(java.lang.Object)
     */
    @Override
    protected boolean isSupportedSourceObject(BuiltInLibrary source) {
        return (source != null);
    }
    
    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#canGenerateOutput(org.opentravel.schemacompiler.model.TLModelElement,org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected boolean canGenerateOutput(BuiltInLibrary source, CodeGenerationContext context) {
        CodeGenerationFilter filter = getFilter();

        return super.canGenerateOutput(source, context)
                && ((filter == null) || filter.processLibrary(source));
    }
    
    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
     */
    @Override
    protected CodeGenerationFilenameBuilder<BuiltInLibrary> getDefaultFilenameBuilder() {
        return new LibraryFilenameBuilder<>();
    }
    
    /**
     * Handles the generation of XSD output for user-defined (<code>TLLibrary</code>) built-ins on
     * behalf of the owning built-in code generator.
     * 
     * @author S. Livezey
     */
    private class JsonSchemaUserBuiltInCodeGenerator extends
            AbstractJsonSchemaCodeGenerator<BuiltInLibrary> {

		/**
		 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
		 */
		@Override
		protected CodeGenerationFilenameBuilder<BuiltInLibrary> getDefaultFilenameBuilder() {
            return new LibraryFilenameBuilder<>();
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(org.opentravel.schemacompiler.model.ModelElement)
		 */
		@Override
		protected AbstractLibrary getLibrary(BuiltInLibrary source) {
			return source;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.json.AbstractJsonSchemaCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.TLModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
		 */
		@Override
		protected File getOutputFile(BuiltInLibrary source, CodeGenerationContext context) {
			return JsonSchemaBuiltInCodeGenerator.this.getOutputFile(source, context);
		}
    	
    }
    
}
