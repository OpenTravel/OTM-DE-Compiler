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
package org.opentravel.schemacompiler.codegen.swagger;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.ResourceFilenameBuilder;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerDocument;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Code generator implementation used to generate Swagger documents from <code>TLResource</code>
 * meta-model components.
 * 
 * <p>The following context variable(s) are required when invoking this code generation module:
 * <ul>
 *   <li><code>schemacompiler.OutputFolder</code> - the folder where generated Swagger files should be stored</li>
 * </ul>
 */
public class SwaggerCodeGenerator extends AbstractCodeGenerator<TLResource> {
	
	public static final String JSON_SCHEMA_FILENAME_EXT = "swagger.json";
	
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private TransformerFactory<CodeGenerationTransformerContext> transformerFactory;
    
    /**
     * Default constructor.
     */
    public SwaggerCodeGenerator() {
        transformerFactory = TransformerFactory.getInstance(
                SchemaCompilerApplicationContext.SWAGGER_CODEGEN_TRANSFORMER_FACTORY,
                new CodeGenerationTransformerContext(this));
    }
    
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(org.opentravel.schemacompiler.model.ModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	public void doGenerateOutput(TLResource source, CodeGenerationContext context) throws CodeGenerationException {
        Writer out = null;
        try {
            SwaggerDocument swaggerDoc = transformSourceObjectToSwaggerDocument(source, context);
            File outputFile = getOutputFile( source, context );
            out = new FileWriter( outputFile );
            gson.toJson( swaggerDoc.toJson(), out );
            out.close();
            out = null;

            addGeneratedFile(outputFile);

        } catch (Throwable t) {
            throw new CodeGenerationException(t);

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Throwable t) {}
        }
	}
	
    /**
     * Performs the translation from meta-model element to a Swagger Document object that will
     * be used to generate the output content.
     * 
     * @param source  the resource instance to transform
     * @param context  the code generation context
     * @return SwaggerDocument
     * @throws CodeGenerationException  thrown if an error occurs during object translation
     */
    protected SwaggerDocument transformSourceObjectToSwaggerDocument(TLResource source, CodeGenerationContext context)
            throws CodeGenerationException {
        ObjectTransformer<TLResource,SwaggerDocument, CodeGenerationTransformerContext> transformer =
        		getTransformerFactory( context ).getTransformer( source, SwaggerDocument.class );
        
		if (transformer != null) {
			return transformer.transform(source);
			
		} else {
			String sourceType = (source == null) ? "UNKNOWN" : source.getClass().getSimpleName();
			throw new CodeGenerationException(
					"No object transformer available for model element of type " + sourceType);
		}
    }
    
    /**
     * Returns the <code>TransformerFactory</code> to be used for JAXB translations by the code
     * generator.
     * 
     * @param codegenContext  the current context for the code generator
     * @return TransformerFactory<CodeGenerationTransformerContext>
     */
    protected TransformerFactory<CodeGenerationTransformerContext> getTransformerFactory(
            CodeGenerationContext codegenContext) {
        transformerFactory.getContext().setCodegenContext(codegenContext);
        return transformerFactory;
    }
    
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#canGenerateOutput(org.opentravel.schemacompiler.model.ModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected boolean canGenerateOutput(TLResource source, CodeGenerationContext context) {
		return !source.isAbstract();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.ModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected File getOutputFile(TLResource source, CodeGenerationContext context) {
        if (source == null) {
            throw new NullPointerException("Source model element cannot be null.");
        }
        AbstractLibrary library = getLibrary( source );
        URL libraryUrl = (library == null) ? null : library.getLibraryUrl();
        File outputFolder = getOutputFolder( context, libraryUrl );
        String filename = getFilenameBuilder().buildFilename( source, JSON_SCHEMA_FILENAME_EXT );

        return new File( outputFolder, filename );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
	 */
	@Override
	protected CodeGenerationFilenameBuilder<TLResource> getDefaultFilenameBuilder() {
		return new ResourceFilenameBuilder();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(org.opentravel.schemacompiler.model.ModelElement)
	 */
	@Override
	protected AbstractLibrary getLibrary(TLResource source) {
		return source.getOwningLibrary();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(org.opentravel.schemacompiler.model.ModelElement)
	 */
	@Override
	protected boolean isSupportedSourceObject(TLResource source) {
		return true;
	}
	
}
