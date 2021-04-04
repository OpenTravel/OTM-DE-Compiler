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

package org.opentravel.schemacompiler.codegen.openapi;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.ResourceFilenameBuilder;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.JsonTypeNameBuilder;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiDocument;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Map.Entry;

/**
 * Code generator implementation used to generate OpenAPI documents from <code>TLResource</code> meta-model components.
 * 
 * <p>
 * The following context variable(s) are required when invoking this code generation module:
 * <ul>
 * <li><code>schemacompiler.OutputFolder</code> - the folder where generated OpenAPI files should be stored</li>
 * </ul>
 */
public class OpenApiCodeGenerator extends AbstractCodeGenerator<TLResource> {

    private static final String DEFINITIONS = "definitions";

    public static final String OPENAPI_FILENAME_EXT = "openapi";
    public static final String OPENAPI_DEFS_FILENAME_EXT = "defs.openapi";

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private TransformerFactory<CodeGenerationTransformerContext> transformerFactory;

    /**
     * Default constructor.
     */
    public OpenApiCodeGenerator() {
        transformerFactory =
            TransformerFactory.getInstance( SchemaCompilerApplicationContext.OPENAPI_CODEGEN_TRANSFORMER_FACTORY,
                new CodeGenerationTransformerContext( this ) );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    public void doGenerateOutput(TLResource source, CodeGenerationContext context) throws CodeGenerationException {
        if (JsonSchemaCodegenUtils.isLatestMinorVersion( source )) {
            File outputFile = getOutputFile( source, context );

            try (Writer out = new FileWriter( outputFile )) {
                OpenApiDocument openapiDoc = transformSourceObjectToOpenApiDocument( source, context );
                JsonObject openapiJson = openapiDoc.toJson();

                if (isSingleFileEnabled( context )) {
                    addBuiltInDefinitions( openapiJson );
                }
                if (context.getBooleanValue( CodeGenerationContext.CK_SUPRESS_OTM_EXTENSIONS )) {
                    JsonSchemaCodegenUtils.stripOtmExtensions( openapiJson );
                }

                gson.toJson( openapiJson, out );
                addGeneratedFile( outputFile );

            } catch (Exception e) {
                throw new CodeGenerationException( e );
            }
        }
    }

    /**
     * Performs the translation from meta-model element to an OpenAPI Document object that will be used to generate the
     * output content.
     * 
     * @param source the resource instance to transform
     * @param context the code generation context
     * @return OpenApiDocument
     * @throws CodeGenerationException thrown if an error occurs during object translation
     */
    protected OpenApiDocument transformSourceObjectToOpenApiDocument(TLResource source, CodeGenerationContext context)
        throws CodeGenerationException {
        ObjectTransformer<TLResource,OpenApiDocument,CodeGenerationTransformerContext> transformer =
            getTransformerFactory( context ).getTransformer( source, OpenApiDocument.class );

        if (transformer != null) {
            if (isSingleFileEnabled( context )) {
                // If single-file swagger generation is enabled, we need to create a JSON Type
                // Name Builder and add it to the transform context
                CodeGenerationFilter filter = transformerFactory.getContext().getCodeGenerator().getFilter();

                transformerFactory.getContext().setContextCacheEntry( JsonTypeNameBuilder.class.getSimpleName(),
                    new JsonTypeNameBuilder( source.getOwningModel(), filter ) );
            }
            return transformer.transform( source );

        } else {
            String sourceType = (source == null) ? "UNKNOWN" : source.getClass().getSimpleName();
            throw new CodeGenerationException(
                "No object transformer available for model element of type " + sourceType );
        }
    }

    /**
     * Adds all of the built-in type definitions to the given Swagger. This should only be done when single-file Swagger
     * generation is enabled.
     * 
     * @param swaggerJson the JSON content of the Swagger document
     * @throws CodeGenerationException thrown if an error occurs while processing the built-in types
     */
    private void addBuiltInDefinitions(JsonObject swaggerJson) throws CodeGenerationException {
        try (Reader reader = new InputStreamReader(
            SchemaDeclarations.OTM_COMMON_SCHEMA.getContent( CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT ) )) {
            JsonObject openapiDefs;

            if (swaggerJson.has( DEFINITIONS )) {
                openapiDefs = swaggerJson.get( DEFINITIONS ).getAsJsonObject();

            } else {
                openapiDefs = new JsonObject();
                swaggerJson.add( DEFINITIONS, openapiDefs );
            }
            JsonObject builtInSchema = new JsonParser().parse( reader ).getAsJsonObject();
            JsonObject builtInDefs = builtInSchema.get( DEFINITIONS ).getAsJsonObject();

            for (Entry<String,JsonElement> builtInDef : builtInDefs.entrySet()) {
                openapiDefs.add( builtInDef.getKey(), builtInDef.getValue() );
            }

        } catch (IOException e) {
            throw new CodeGenerationException( "Error loading JSON built-in definitons.", e );
        }
    }

    /**
     * Returns the <code>TransformerFactory</code> to be used for JAXB translations by the code generator.
     * 
     * @param codegenContext the current context for the code generator
     * @return TransformerFactory&lt;CodeGenerationTransformerContext&gt;
     */
    protected TransformerFactory<CodeGenerationTransformerContext> getTransformerFactory(
        CodeGenerationContext codegenContext) {
        transformerFactory.getContext().setCodegenContext( codegenContext );
        return transformerFactory;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#canGenerateOutput(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected boolean canGenerateOutput(TLResource source, CodeGenerationContext context) {
        return !source.isAbstract() && !ResourceCodegenUtils.getQualifiedActions( source ).isEmpty();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected File getOutputFile(TLResource source, CodeGenerationContext context) {
        if (source == null) {
            throw new NullPointerException( "Source model element cannot be null." );
        }
        AbstractLibrary library = getLibrary( source );
        URL libraryUrl = (library == null) ? null : library.getLibraryUrl();
        File outputFolder = getOutputFolder( context, libraryUrl );
        String filename = getFilenameBuilder().buildFilename( source,
            isSingleFileEnabled( context ) ? OPENAPI_DEFS_FILENAME_EXT : OPENAPI_FILENAME_EXT );

        return new File( outputFolder, filename );
    }

    /**
     * Returns true if single-file Swagger document generation is enabled.
     * 
     * @param context the code generation context
     * @return boolean
     */
    private boolean isSingleFileEnabled(CodeGenerationContext context) {
        return "true".equalsIgnoreCase( context.getValue( CodeGenerationContext.CK_ENABLE_SINGLE_FILE ) );
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
