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

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.codegen.json.AbstractJsonSchemaCodeGenerator;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Compiler task used to generate full (non-trimmed) JSON schema output for the libraries of a model.
 */
public class JsonSchemaCompilerTask extends AbstractCompilerTask implements SchemaCompilerTaskOptions {
	
    private boolean generateExamples = true;
    private boolean generateMaxDetailsForExamples = true;
    private String exampleContext;
    private Integer exampleMaxRepeat;
    private Integer exampleMaxDepth;

    /**
     * Constructor that specifies the filename of the project for which schemas are being compiled.
     * 
     * @param projectFilename  the name of the project (.otp) file
     */
    public JsonSchemaCompilerTask(String projectFilename) {
        this.projectFilename = projectFilename;
    }

	/**
	 * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection, java.util.Collection)
	 */
	@Override
	protected void generateOutput(Collection<TLLibrary> userDefinedLibraries, Collection<XSDLibrary> legacySchemas)
			throws SchemaCompilerException {
        CodeGenerationContext context = createContext();

        // Generate schemas for all of the user-defined libraries
        compileJsonSchemas(userDefinedLibraries, legacySchemas, context, null, null);

        // Generate example files if required
        if (isGenerateExamples()) {
            generateExampleArtifacts(
            		userDefinedLibraries, context, new LibraryFilenameBuilder<AbstractLibrary>(),
            		null, CodeGeneratorFactory.JSON_TARGET_FORMAT);
        }
	}
	
    /**
     * Compiles the XML schema files for the given model using the context, filename builder, and
     * code generation filter provided.
     * 
     * @param userDefinedLibraries  the list of user-defined libraries for which to compile XML schema artifacts
     * @param legacySchemas  the list of legacy schemas (xsd files) for which to compile XML schema artifacts
     * @param context  the code generation context to use for code generation
     * @param filenameBuilder  the filename builder to assign to the code generator(s) used by this method
     * @param filter  the filter used to identify specific artifacts for which schema generation is required
     * @throws SchemaCompilerException
     */
    @SuppressWarnings("unchecked")
    protected void compileJsonSchemas(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas, CodeGenerationContext context,
            CodeGenerationFilenameBuilder<?> filenameBuilder, CodeGenerationFilter filter)
            throws SchemaCompilerException {

        // Generate output for all user-defined libraries
        TLModel model = getModel(userDefinedLibraries, legacySchemas);

        if (model == null) {
            throw new SchemaCompilerException(
                    "No libraries or legacy schemas found for code generation task.");
        }

        for (TLLibrary library : userDefinedLibraries) {
            CodeGenerator<TLLibrary> jsonSchemaGenerator = newCodeGenerator(
                    CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT, TLLibrary.class,
                    (CodeGenerationFilenameBuilder<TLLibrary>) filenameBuilder, filter);

            addGeneratedFiles(jsonSchemaGenerator.generateOutput(library, context));

            // If any OTM built-in dependencies were identified, add them to the current filter
            if ((filter != null) && (jsonSchemaGenerator instanceof AbstractJsonSchemaCodeGenerator)) {
                AbstractJaxbCodeGenerator<?> generator = (AbstractJaxbCodeGenerator<?>) jsonSchemaGenerator;

                for (SchemaDeclaration schemaDeclaration : generator.getCompileTimeDependencies()) {
                	String schemaFilename = schemaDeclaration.getFilename(CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT);
                	
                    if ((schemaFilename != null) && schemaFilename.endsWith(".json")) {
                        AbstractLibrary dependentLib = model.getLibrary(
                                schemaDeclaration.getNamespace(), schemaDeclaration.getName());

                        if (dependentLib instanceof BuiltInLibrary) {
                            filter.addBuiltInLibrary((BuiltInLibrary) dependentLib);
                        }
                    }
                }
            }
        }

        // Generate output for all built-in libraries
        if (model != null) {
            CodeGenerator<BuiltInLibrary> xsdGenerator = newCodeGenerator(
                    CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT, BuiltInLibrary.class,
                    (CodeGenerationFilenameBuilder<BuiltInLibrary>) filenameBuilder, filter);
            CodeGenerationContext builtInContext = context.getCopy();

            for (BuiltInLibrary library : model.getBuiltInLibraries()) {
                addGeneratedFiles(xsdGenerator.generateOutput(library, builtInContext));
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#createContext()
     */
    protected CodeGenerationContext createContext() {
        CodeGenerationContext context = super.createContext();

        if (!generateMaxDetailsForExamples) {
            context.setValue(CodeGenerationContext.CK_EXAMPLE_DETAIL_LEVEL, "MINIMUM");
        }
        if (exampleContext != null) {
            context.setValue(CodeGenerationContext.CK_EXAMPLE_CONTEXT, exampleContext);
        }
        if (exampleMaxRepeat != null) {
            context.setValue(CodeGenerationContext.CK_EXAMPLE_MAX_REPEAT, exampleMaxRepeat.toString());
        }
        if (exampleMaxDepth != null) {
            context.setValue(CodeGenerationContext.CK_EXAMPLE_MAX_DEPTH, exampleMaxDepth.toString());
        }
        return context;
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#applyTaskOptions(org.opentravel.schemacompiler.task.CommonCompilerTaskOptions)
     */
    @Override
    public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
        if (taskOptions instanceof SchemaCompilerTaskOptions) {
            // No explicit options currently implemented
        }
        if (taskOptions instanceof ExampleCompilerTaskOptions) {
            ExampleCompilerTaskOptions exampleOptions = (ExampleCompilerTaskOptions) taskOptions;

            setGenerateExamples(exampleOptions.isGenerateExamples());
            setGenerateMaxDetailsForExamples(exampleOptions.isGenerateMaxDetailsForExamples());
            setExampleContext(exampleOptions.getExampleContext());
            setExampleMaxRepeat(exampleOptions.getExampleMaxRepeat());
            setExampleMaxDepth(exampleOptions.getExampleMaxDepth());
        }
        super.applyTaskOptions(taskOptions);
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#isGenerateExamples()
     */
    @Override
    public boolean isGenerateExamples() {
        return generateExamples;
    }

    /**
     * Assigns the option flag indicating that example XML documents should be generated.
     * 
     * @param compileRAS
     *            the task option value to assign
     */
    public void setGenerateExamples(boolean generateExamples) {
        this.generateExamples = generateExamples;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#isGenerateMaxDetailsForExamples()
     */
    @Override
    public boolean isGenerateMaxDetailsForExamples() {
        return generateMaxDetailsForExamples;
    }

    /**
     * Assigns the flag indicating whether the maximum amount of detail is to be included in
     * generated example data. If false, minimum detail will be generated.
     * 
     * @param generateMaxDetailsForExamples  the boolean flag value to assign
     */
    public void setGenerateMaxDetailsForExamples(boolean generateMaxDetailsForExamples) {
        this.generateMaxDetailsForExamples = generateMaxDetailsForExamples;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getExampleContext()
     */
    @Override
    public String getExampleContext() {
        return exampleContext;
    }

    /**
     * Assigns the preferred context to use when producing example values for simple data types.
     * 
     * @param exampleContext  the context ID to assign
     */
    public void setExampleContext(String exampleContext) {
        this.exampleContext = exampleContext;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getExampleMaxRepeat()
     */
    @Override
    public Integer getExampleMaxRepeat() {
        return exampleMaxRepeat;
    }

    /**
     * Assigns the maximum number of times that repeating elements should be displayed in generated
     * example output.
     * 
     * @param exampleMaxRepeat  the max repeat value to assign
     */
    public void setExampleMaxRepeat(Integer exampleMaxRepeat) {
        this.exampleMaxRepeat = exampleMaxRepeat;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getExampleMaxDepth()
     */
    @Override
    public Integer getExampleMaxDepth() {
        return exampleMaxDepth;
    }

    /**
     * Assigns the maximum depth that should be included for nested elements in generated example
     * output.
     * 
     * @param exampleMaxDepth
     *            the max depth value to assign
     */
    public void setExampleMaxDepth(Integer exampleMaxDepth) {
        this.exampleMaxDepth = exampleMaxDepth;
    }

}
