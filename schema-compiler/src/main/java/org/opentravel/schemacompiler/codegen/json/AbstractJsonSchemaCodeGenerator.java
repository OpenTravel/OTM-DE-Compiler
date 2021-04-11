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

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.DependencyFilterBuilder;
import org.opentravel.schemacompiler.codegen.json.model.JsonDiscriminator;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.springframework.context.ApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <code>CodeGenerator</code> base class that handles schema output generation using the <code>JsonSchema</code> model
 * objects as a mechanism for producing the output content.
 * 
 * @param <S> the source type for which output content will be generated
 */
public abstract class AbstractJsonSchemaCodeGenerator<S extends AbstractLibrary> extends AbstractCodeGenerator<S> {

    private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private List<SchemaDeclaration> compileTimeDependencies = new ArrayList<>();
    private TransformerFactory<CodeGenerationTransformerContext> transformerFactory;

    /**
     * Default constructor.
     */
    public AbstractJsonSchemaCodeGenerator() {
        transformerFactory =
            TransformerFactory.getInstance( SchemaCompilerApplicationContext.JSON_SCHEMA_CODEGEN_TRANSFORMER_FACTORY,
                new CodeGenerationTransformerContext( this ) );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    public void doGenerateOutput(S source, CodeGenerationContext context) throws CodeGenerationException {
        File outputFile = getOutputFile( source, context );

        context.setValue( CodeGenerationContext.CK_BASE_DEFINITIONS_PATH, "#/definitions/" );
        context.setValue( CodeGenerationContext.CK_JSON_DISCRIMINATOR_FORMAT,
            JsonDiscriminator.DiscriminatorFormat.OPENAPI.toString() );

        try (Writer out = new FileWriter( outputFile )) {
            JsonSchema jsonSchema = transformSourceObjectToJsonSchema( source, context );
            JsonObject jsonDocument = jsonSchema.toJson();

            if (context.getBooleanValue( CodeGenerationContext.CK_SUPRESS_OTM_EXTENSIONS )) {
                JsonSchemaCodegenUtils.stripOtmExtensions( jsonDocument );
            }
            gson.toJson( jsonDocument, out );

            // Finish up by copying any dependencies that were identified during code generation
            if (context.getBooleanValue( CodeGenerationContext.CK_COPY_COMPILE_TIME_DEPENDENCIES )) {
                copyCompileTimeDependencies( context );
            }
            addGeneratedFile( outputFile );

        } catch (Exception e) {
            throw new CodeGenerationException( e );
        }
    }

    /**
     * Copies any schema documents that were identified at compile-time to the output folder if they do not already
     * exist.
     * 
     * @param context the code generation context
     * @throws CodeGenerationException thrown if one or more of the files cannot be copied
     */
    protected void copyCompileTimeDependencies(CodeGenerationContext context) throws CodeGenerationException {
        try {
            for (SchemaDeclaration schemaDeclaration : getCompileTimeDependencies()) {
                String sdFilename = schemaDeclaration.getFilename( CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT );

                if ((schemaDeclaration == SchemaDeclarations.SCHEMA_FOR_SCHEMAS) || (sdFilename == null)
                    || !sdFilename.endsWith( ".schema.json" )) {
                    continue;
                }
                File outputFolder = getOutputFolder( context, null );
                String builtInFolder = getBuiltInSchemaOutputLocation( context );

                if (builtInFolder != null) {
                    outputFolder = new File( outputFolder, builtInFolder );

                    if (!outputFolder.exists()) {
                        outputFolder.mkdirs();
                    }
                }

                // Copy the contents of the file to the built-in folder location
                File outputFile = new File( outputFolder, sdFilename );

                if (!outputFile.exists()) {
                    BufferedReader reader = new BufferedReader( new InputStreamReader(
                        schemaDeclaration.getContent( CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT ) ) );

                    try (BufferedWriter writer = new BufferedWriter( new FileWriter( outputFile ) )) {
                        String line = null;

                        while ((line = reader.readLine()) != null) {
                            writer.write( line );
                            writer.write( LINE_SEPARATOR );
                        }
                    }
                    reader.close();
                }
                addGeneratedFile( outputFile ); // count dependency as generated - even if it already existed
            }
        } catch (IOException e) {
            throw new CodeGenerationException( e );
        }
    }

    /**
     * Performs the translation from meta-model element to a JSON schema object that will be used to generate the output
     * content.
     * 
     * @param source the meta-model element to translate
     * @param context the code generation context
     * @return JsonSchema
     * @throws CodeGenerationException thrown if an error occurs during object translation
     */
    protected JsonSchema transformSourceObjectToJsonSchema(S source, CodeGenerationContext context)
        throws CodeGenerationException {
        ObjectTransformer<S,JsonSchema,CodeGenerationTransformerContext> transformer =
            getTransformerFactory( context ).getTransformer( source, JsonSchema.class );

        if (transformer != null) {
            return transformer.transform( source );

        } else {
            String sourceType = (source == null) ? "UNKNOWN" : source.getClass().getSimpleName();
            throw new CodeGenerationException(
                "No object transformer available for model element of type " + sourceType );
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
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#generateOutput(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    public Collection<File> generateOutput(final S source, CodeGenerationContext context)
        throws ValidationException, CodeGenerationException {
        // If a filter has not already been defined, create one that will allow processing of all members and
        // only those libraries that are directly required by the members of the current source library.
        if (getFilter() == null) {
            final AbstractLibrary sourceLibrary = getLibrary( source );
            final CodeGenerationFilter libraryFilter;

            if (source instanceof LibraryMember) {
                libraryFilter = new DependencyFilterBuilder( (LibraryMember) source ).buildFilter();
            } else {
                libraryFilter = new DependencyFilterBuilder( sourceLibrary ).buildFilter();
            }

            setFilter( new CodeGenerationFilter() {

                @Override
                public boolean processEntity(LibraryElement entity) {
                    return true;
                }

                @Override
                public boolean processExtendedLibrary(XSDLibrary legacySchema) {
                    return false; // No legacy XSD's for JSON schema generation
                }

                @Override
                public boolean processLibrary(AbstractLibrary library) {
                    return (library == sourceLibrary) || libraryFilter.processLibrary( library );
                }

                /**
                 * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#addBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
                 */
                @Override
                public void addBuiltInLibrary(BuiltInLibrary library) {
                    libraryFilter.addBuiltInLibrary( library );
                }

            } );
        }
        return super.generateOutput( source, context );
    }

    /**
     * Adds the given schema declaration to the list of compile-time schema dependencies. At the end of the code
     * generation process, these dependencies will be copied to the output folder.
     * 
     * @param compileTimeDependency the compile-time dependency to add
     */
    public void addCompileTimeDependency(SchemaDeclaration compileTimeDependency) {
        if (!compileTimeDependencies.contains( compileTimeDependency )) {
            compileTimeDependencies.add( compileTimeDependency );
        }
    }

    /**
     * Returns the list of compile-time schema dependencies that have been reported during code generation.
     * 
     * @return Collection&lt;SchemaDeclaration&gt;
     */
    public Collection<SchemaDeclaration> getCompileTimeDependencies() {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
        Collection<SchemaDeclaration> dependencies = new ArrayList<>( compileTimeDependencies );

        for (SchemaDeclaration dependency : compileTimeDependencies) {
            resolveIndirectDependencies( dependency.getDependencies(), appContext, dependencies );
        }
        return dependencies;
    }

    /**
     * Resolves all of the indirect dependencies provided in the given list of bean ID's.
     * 
     * @param dependencyBeanIds the application context bean ID's of any indirectly-dependent item(s)
     * @param appContext the spring application context for the compiler
     * @param dependencyList the list of schema dependencies being constructed
     */
    private void resolveIndirectDependencies(List<String> dependencyBeanIds, ApplicationContext appContext,
        Collection<SchemaDeclaration> dependencyList) {
        for (String beanId : dependencyBeanIds) {
            if (appContext.containsBean( beanId )) {
                SchemaDeclaration dependency = (SchemaDeclaration) appContext.getBean( beanId );

                if (!dependencyList.contains( dependency )) {
                    dependencyList.add( dependency );
                    resolveIndirectDependencies( dependency.getDependencies(), appContext, dependencyList );
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(org.opentravel.schemacompiler.model.ModelElement)
     */
    @Override
    protected boolean isSupportedSourceObject(S source) {
        return (source != null);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#canGenerateOutput(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected boolean canGenerateOutput(S source, CodeGenerationContext context) {
        CodeGenerationFilter filter = getFilter();

        return super.canGenerateOutput( source, context )
            && ((filter == null) || filter.processLibrary( getLibrary( source ) ));
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected File getOutputFile(S source, CodeGenerationContext context) {
        File outputFolder = getOutputFolder( context, getLibrary( source ).getLibraryUrl() );
        String filename = context.getValue( CodeGenerationContext.CK_SCHEMA_FILENAME );

        if ((filename == null) || filename.trim().equals( "" )) {
            filename = getFilenameBuilder().buildFilename( source, JsonSchemaCodegenUtils.JSON_SCHEMA_FILENAME_EXT );
        }
        return new File( outputFolder, filename );
    }

}
