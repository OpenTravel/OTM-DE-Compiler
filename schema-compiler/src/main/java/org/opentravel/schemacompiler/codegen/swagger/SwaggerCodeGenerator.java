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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Map.Entry;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.ResourceFilenameBuilder;
import org.opentravel.schemacompiler.codegen.json.JsonTypeNameBuilder;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerDocument;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
	
	public static final String SWAGGER_FILENAME_EXT     = "swagger";
	public static final String SWAGGER_DEFS_FILENAME_EXT = "defs.swagger";
	
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
            JsonObject swaggerJson = swaggerDoc.toJson();
            File outputFile = getOutputFile( source, context );
            out = new FileWriter( outputFile );
            
			if (isSingleFileEnabled( context )) {
				addBuiltInDefinitions( swaggerJson );
			}
			
            gson.toJson( swaggerJson, out );
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
			if (isSingleFileEnabled( context )) {
				// If single-file swagger generation is enabled, we need to create a JSON Type
				// Name Builder and add it to the transform context
				transformerFactory.getContext().setContextCacheEntry(
						JsonTypeNameBuilder.class.getSimpleName(), new JsonTypeNameBuilder( source.getOwningModel() ) );
			}
			return transformer.transform(source);
			
		} else {
			String sourceType = (source == null) ? "UNKNOWN" : source.getClass().getSimpleName();
			throw new CodeGenerationException(
					"No object transformer available for model element of type " + sourceType);
		}
    }
    
    /**
     * Adds all of the built-in type definitions to the given Swagger.  This should only be done
     * when single-file Swagger generation is enabled.
     * 
     * @param swaggerJson  the JSON content of the Swagger document
     * @throws CodeGenerationException  thrown if an error occurs while processing the built-in types
     */
    private void addBuiltInDefinitions(JsonObject swaggerJson) throws CodeGenerationException {
		try (Reader reader = new InputStreamReader(
				SchemaDeclarations.OTM_COMMON_SCHEMA.getContent(
						CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT ) )) {
			JsonObject swaggerDefs = swaggerJson.get( "definitions" ).getAsJsonObject();
    		JsonObject builtInSchema = new JsonParser().parse( reader ).getAsJsonObject();
			JsonObject builtInDefs = builtInSchema.get( "definitions" ).getAsJsonObject();
    		
			for (Entry<String,JsonElement> builtInDef : builtInDefs.entrySet()) {
				swaggerDefs.add( builtInDef.getKey(), builtInDef.getValue() );
			}
			
		} catch (IOException e) {
			throw new CodeGenerationException("Error loading JSON built-in definitons.", e);
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
		return !source.isAbstract() && !ResourceCodegenUtils.getQualifiedActions( source ).isEmpty();
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
        String filename = getFilenameBuilder().buildFilename( source,
        		isSingleFileEnabled( context ) ? SWAGGER_DEFS_FILENAME_EXT : SWAGGER_FILENAME_EXT );

        return new File( outputFolder, filename );
	}
	
	/**
	 * Returns true if single-file Swagger document generation is enabled.
	 * 
	 * @param context  the code generation context
	 * @return boolean
	 */
	private boolean isSingleFileEnabled(CodeGenerationContext context) {
        return "true".equalsIgnoreCase( context.getValue( CodeGenerationContext.CK_ENABLE_SINGLE_FILE_SWAGGER ) );
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
