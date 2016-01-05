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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.DependencyFilterBuilder;
import org.opentravel.schemacompiler.codegen.impl.LegacySchemaExtensionFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.AbstractXsdCodeGenerator;
import org.opentravel.schemacompiler.codegen.xsd.ImportSchemaLocations;
import org.opentravel.schemacompiler.codegen.xsd.XsdBuiltInCodeGenerator;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.springframework.context.ApplicationContext;

/**
 * Extends the base class by adding support for XML schema (XSD) and example
 * output generation.
 * 
 * @author S. Livezey
 */
public abstract class AbstractSchemaCompilerTask extends AbstractCompilerTask
		implements SchemaCompilerTaskOptions {

	private boolean generateExamples = true;
	private boolean generateMaxDetailsForExamples = true;
	private String exampleContext;
	private Integer exampleMaxRepeat;
	private Integer exampleMaxDepth;

	/**
	 * Compiles the XML schema files for the given model using the context,
	 * filename builder, and code generation filter provided.
	 * 
	 * @param userDefinedLibraries
	 *            the list of user-defined libraries for which to compile XML
	 *            schema artifacts
	 * @param legacySchemas
	 *            the list of legacy schemas (xsd files) for which to compile
	 *            XML schema artifacts
	 * @param context
	 *            the code generation context to use for code generation
	 * @param filenameBuilder
	 *            the filename builder to assign to the code generator(s) used
	 *            by this method
	 * @param filter
	 *            the filter used to identify specific artifacts for which
	 *            schema generation is required
	 * @throws SchemaCompilerException
	 */
	@SuppressWarnings("unchecked")
	protected void compileXmlSchemas(
			Collection<TLLibrary> userDefinedLibraries,
			Collection<XSDLibrary> legacySchemas,
			CodeGenerationContext context,
			CodeGenerationFilenameBuilder<?> filenameBuilder,
			CodeGenerationFilter filter) throws SchemaCompilerException {

		// Generate output for all user-defined libraries
		TLModel model = getModel(userDefinedLibraries, legacySchemas);

		if (model == null) {
			throw new SchemaCompilerException(
					"No libraries or legacy schemas found for code generation task.");
		}
		ImportSchemaLocations importLocations = analyzeImportDependencies(
				model, context, filenameBuilder, filter);

		for (TLLibrary library : userDefinedLibraries) {
			CodeGenerator<TLLibrary> xsdGenerator = newCodeGenerator(
					CodeGeneratorFactory.XSD_TARGET_FORMAT, TLLibrary.class,
					(CodeGenerationFilenameBuilder<TLLibrary>) filenameBuilder,
					filter);

			if (xsdGenerator instanceof AbstractXsdCodeGenerator) {
				((AbstractXsdCodeGenerator<?>) xsdGenerator)
						.setImportSchemaLocations(importLocations);
			}
			addGeneratedFiles(xsdGenerator.generateOutput(library, context));

			// If any non-xsd built-in dependencies were identified, add them to
			// the current filter
			if ((filter != null)
					&& (xsdGenerator instanceof AbstractJaxbCodeGenerator)) {
				AbstractJaxbCodeGenerator<?> generator = (AbstractJaxbCodeGenerator<?>) xsdGenerator;

				for (SchemaDeclaration schemaDeclaration : generator
						.getCompileTimeDependencies()) {
					if (schemaDeclaration.getFilename().endsWith(".xsd")) {
						continue;
					}
					AbstractLibrary dependentLib = model.getLibrary(
							schemaDeclaration.getNamespace(),
							schemaDeclaration.getName());

					if (dependentLib instanceof BuiltInLibrary) {
						filter.addBuiltInLibrary((BuiltInLibrary) dependentLib);
					}
				}
			}
		}

		// Generate output for all legacy XML schema libraries

		// If a filter was not passed to this method create one that will
		// identify the legacy
		// schemas
		// (and schema extensions) are really needed in the output folder.
		CodeGenerationFilter legacySchemaFilter = filter;

		if (legacySchemaFilter == null) {
			DependencyFilterBuilder filterBuilder = new DependencyFilterBuilder()
					.setIncludeExtendedLegacySchemas(true);

			for (TLLibrary library : userDefinedLibraries) {
				filterBuilder.addLibrary(library);
			}
			for (XSDLibrary library : legacySchemas) {
				filterBuilder.addLibrary(library);
			}
			legacySchemaFilter = filterBuilder.buildFilter();
		}

		// Copy the required legacy schemas to the output folder
		CodeGenerator<XSDLibrary> legacyGenerator = newCodeGenerator(
				CodeGeneratorFactory.XSD_TARGET_FORMAT, XSDLibrary.class,
				(CodeGenerationFilenameBuilder<XSDLibrary>) filenameBuilder,
				legacySchemaFilter);
		CodeGenerator<XSDLibrary> legacyExtensionGenerator = newCodeGenerator(
				CodeGeneratorFactory.EXT_XSD_TARGET_FORMAT, XSDLibrary.class,
				(CodeGenerationFilenameBuilder<XSDLibrary>) filenameBuilder,
				legacySchemaFilter);

		for (XSDLibrary library : legacySchemas) {
			addGeneratedFiles(legacyGenerator.generateOutput(library, context));
			addGeneratedFiles(legacyExtensionGenerator.generateOutput(library,
					context));
		}

		// Generate output for all built-in libraries
		if (model != null) {
			CodeGenerator<BuiltInLibrary> xsdGenerator = newCodeGenerator(
					CodeGeneratorFactory.XSD_TARGET_FORMAT,
					BuiltInLibrary.class,
					(CodeGenerationFilenameBuilder<BuiltInLibrary>) filenameBuilder,
					filter);
			CodeGenerationContext builtInContext = context.getCopy();

			if (xsdGenerator instanceof XsdBuiltInCodeGenerator) {
				((XsdBuiltInCodeGenerator) xsdGenerator)
						.setImportSchemaLocations(importLocations);
			}

			for (BuiltInLibrary library : model.getBuiltInLibraries()) {
				addGeneratedFiles(xsdGenerator.generateOutput(library,
						builtInContext));
			}
		}

		// Generate any consolidated import files (if needed)
		addGeneratedFiles(importLocations.generateConsolidatedImportFiles());
	}

	/**
	 * Performs a dependency analysis of all the libraries that are to be
	 * included in the generated schema output. For each included library, the
	 * name and location of the generated schemas are pre-calculated for the
	 * purpose of creating consistent import declarations across all of the
	 * generated schemas.
	 * 
	 * @param model
	 *            the model from which schemas are to be generated
	 * @param context
	 *            the code generation context to use for code generation
	 * @param filenameBuilder
	 *            the filename builder to assign to the code generator(s) used
	 *            by this method
	 * @param filter
	 *            the filter used to identify specific artifacts for which
	 *            schema generation is required
	 * @return ImportSchemaLocations
	 */
	@SuppressWarnings("unchecked")
	protected ImportSchemaLocations analyzeImportDependencies(TLModel model,
			CodeGenerationContext context,
			CodeGenerationFilenameBuilder<?> filenameBuilder,
			CodeGenerationFilter filter) {
		if (filenameBuilder == null) {
			filenameBuilder = new LibraryFilenameBuilder<AbstractLibrary>();
		}
		CodeGenerationFilenameBuilder<TLLibrary> tlFilenameBuilder = (CodeGenerationFilenameBuilder<TLLibrary>) filenameBuilder;
		CodeGenerationFilenameBuilder<XSDLibrary> xsdFilenameBuilder = (CodeGenerationFilenameBuilder<XSDLibrary>) filenameBuilder;
		CodeGenerationFilenameBuilder<XSDLibrary> extFilenameBuilder = new LegacySchemaExtensionFilenameBuilder<XSDLibrary>(
				xsdFilenameBuilder);
		CodeGenerationFilenameBuilder<BuiltInLibrary> builtInFilenameBuilder = new LibraryFilenameBuilder<BuiltInLibrary>();

		File outputFolder = XsdCodegenUtils.getBaseOutputFolder(context);
		File legacyOutputFolder = new File(outputFolder,
				XsdCodegenUtils.getLegacySchemaOutputLocation(context));
		File builtInOutputFolder = new File(outputFolder,
				XsdCodegenUtils.getBuiltInSchemaOutputLocation(context));

		ImportSchemaLocations importLocations = new ImportSchemaLocations(
				outputFolder);

		for (TLLibrary library : model.getUserDefinedLibraries()) {
			if ((filter == null) || filter.processLibrary(library)) {
				importLocations.setSchemaLocation(
						library.getNamespace(),
						library.getPrefix(),
						new File(outputFolder, tlFilenameBuilder.buildFilename(
								library, "xsd")));
			}
		}
		for (XSDLibrary library : model.getLegacySchemaLibraries()) {
			if ((filter == null) || filter.processLibrary(library)) {
				importLocations.setSchemaLocation(
						library.getNamespace(),
						library.getPrefix(),
						new File(legacyOutputFolder, xsdFilenameBuilder
								.buildFilename(library, "xsd")));
			}
			if ((filter != null) && filter.processExtendedLibrary(library)) {
				importLocations.setSchemaLocation(
						library.getNamespace(),
						library.getPrefix(),
						new File(legacyOutputFolder, extFilenameBuilder
								.buildFilename(library, "xsd")));
			}
		}

		// For built-ins, we have to dig into the schema declaration
		// dependencies that are defined
		// in
		// the Spring application context
		Set<SchemaDeclaration> builtInDependencies = new HashSet<SchemaDeclaration>();

		for (BuiltInLibrary library : model.getBuiltInLibraries()) {
			if (library.getNamespace().equals(
					XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
				continue; // skip the schema-for-schemas built-in
			}
			if ((filter == null) || filter.processLibrary(library)) {
				addBuiltInSchemaDependencies(library.getSchemaDeclaration(),
						builtInDependencies);
			}
		}
		for (SchemaDeclaration builtIn : builtInDependencies) {
			String filename = builtIn.getFilename();

			if (!filename.toLowerCase().endsWith(".xsd")) {
				BuiltInLibrary builtInLib = getBuiltInLibrary(builtIn, model);

				if (builtInLib != null) {
					filename = builtInFilenameBuilder.buildFilename(builtInLib,
							"xsd");
				}
			}
			importLocations.setSchemaLocation(builtIn.getNamespace(), builtIn
					.getDefaultPrefix(),
					new File(builtInOutputFolder, filename));
		}
		return importLocations;
	}

	/**
	 * Searches the implied schema dependencies of the given built-in and adds
	 * any required libraries to the list.
	 * 
	 * @param builtIn
	 *            the built-in library to analyze
	 * @param builtInDependencies
	 */
	private void addBuiltInSchemaDependencies(SchemaDeclaration builtIn,
			Set<SchemaDeclaration> schemaDependencies) {
		ApplicationContext appContext = SchemaCompilerApplicationContext
				.getContext();

		if (!schemaDependencies.contains(builtIn)) {
			for (String dependencyId : builtIn.getDependencies()) {
				addBuiltInSchemaDependencies(
						(SchemaDeclaration) appContext.getBean(dependencyId),
						schemaDependencies);
			}
			schemaDependencies.add(builtIn);
		}
	}

	/**
	 * Returns the built-in library that is associated with the schema
	 * declaration provided.
	 * 
	 * @param builtInDeclaration
	 *            the schema declaration for the built-in library
	 * @param model
	 *            the model from which the built-in library should be retrieved
	 * @return BuiltInLibrary
	 */
	private BuiltInLibrary getBuiltInLibrary(
			SchemaDeclaration builtInDeclaration, TLModel model) {
		BuiltInLibrary library = null;

		for (BuiltInLibrary lib : model.getBuiltInLibraries()) {
			if (lib.getSchemaDeclaration() == builtInDeclaration) {
				library = lib;
				break;
			}
		}
		return library;
	}

	/**
	 * Generates example XML files for all elements of the given library.
	 * 
	 * @param userDefinedLibraries
	 *            the list of user-defined libraries for which to generate
	 *            example XML files
	 * @param context
	 *            the code generation context to use for code generation
	 * @param filenameBuilder
	 *            the filename builder to use for schema location filename
	 *            construction
	 * @param filter
	 *            the filter used to identify specific artifacts for which
	 *            example generation is required
	 * @throws SchemaCompilerException
	 */
	protected void generateExampleArtifacts(
			Collection<TLLibrary> userDefinedLibraries,
			CodeGenerationContext context,
			CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder,
			CodeGenerationFilter filter) throws SchemaCompilerException {
		Collection<? extends CodeGenerator<TLModelElement>> exampleGenerators = getExampleGenerators(filenameBuilder);

		CodeGenerationContext exampleContext = context.getCopy();

		// Generate examples for all model entities that are not excluded by the
		// filter
		for (TLLibrary library : userDefinedLibraries) {
			if ((filter != null) && !filter.processLibrary(library)) {
				continue;
			}

			// Generate example files for each member of the library
			for (LibraryMember member : library.getNamedMembers()) {
				if ((filter != null) && !filter.processEntity(member)) {
					continue;
				}
				exampleContext.setValue(CodeGenerationContext.CK_OUTPUT_FOLDER,
						getExampleOutputFolder(member, context));
				exampleContext.setValue(
						CodeGenerationContext.CK_EXAMPLE_SCHEMA_RELATIVE_PATH,
						getSchemaRelativeFolderPath(member, exampleContext));

				if (member instanceof TLService) {
					TLService service = (TLService) member;

					for (TLOperation operation : service.getOperations()) {
						if (operation.getRequest().declaresContent()) {
							for (CodeGenerator<TLModelElement> exampleGenerator : exampleGenerators) {
								addGeneratedFiles(exampleGenerator
										.generateOutput(operation.getRequest(),
												exampleContext));
							}
						}
						if (operation.getResponse().declaresContent()) {
							for (CodeGenerator<TLModelElement> exampleGenerator : exampleGenerators) {
								addGeneratedFiles(exampleGenerator
										.generateOutput(
												operation.getResponse(),
												exampleContext));
							}
						}
						if (operation.getNotification().declaresContent()) {
							for (CodeGenerator<TLModelElement> exampleGenerator : exampleGenerators) {
								addGeneratedFiles(exampleGenerator
										.generateOutput(
												operation.getNotification(),
												exampleContext));
							}
						}
					}
				} else {
					for (CodeGenerator<TLModelElement> exampleGenerator : exampleGenerators) {
						addGeneratedFiles(exampleGenerator.generateOutput(
								member, exampleContext));
					}
				}
			}
		}
	}

	/**
	 * Gets the set of example generators. Currently only JSON and XML. If any
	 * more are to be added consider an enum for the types.
	 * 
	 * @param filenameBuilder
	 * 
	 * @return the list of example generators.
	 * @throws CodeGenerationException
	 */
	private Collection<? extends CodeGenerator<TLModelElement>> getExampleGenerators(
			CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder)
			throws CodeGenerationException {
		List<CodeGenerator<TLModelElement>> exampleGenerators = new ArrayList<CodeGenerator<TLModelElement>>();
		CodeGenerator<TLModelElement> exampleGenerator = CodeGeneratorFactory
				.getInstance().newCodeGenerator(
						CodeGeneratorFactory.XML_TARGET_FORMAT,
						TLModelElement.class);
		configureFileNameBuilder(exampleGenerator, filenameBuilder);
		exampleGenerators.add(exampleGenerator);
		exampleGenerator = CodeGeneratorFactory.getInstance().newCodeGenerator(
				CodeGeneratorFactory.JSON_TARGET_FORMAT, TLModelElement.class);
		exampleGenerators.add(exampleGenerator);
		return exampleGenerators;
	}

	private void configureFileNameBuilder(
			CodeGenerator<TLModelElement> exampleGenerator,
			CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder) {
		TrimmedExampleFilenameBuilder trimmedFilenameBuilder = new TrimmedExampleFilenameBuilder(
				exampleGenerator.getFilenameBuilder(), filenameBuilder);
		exampleGenerator.setFilenameBuilder(trimmedFilenameBuilder);
	}

	/**
	 * 
	 * Wrapper around default {@link CodeGenerationFilenameBuilder} that support
	 * different handling for {@link AbstractLibrary}.
	 * 
	 * @author Pawel Jedruch
	 * 
	 */
	class TrimmedExampleFilenameBuilder implements
			CodeGenerationFilenameBuilder<TLModelElement> {

		private CodeGenerationFilenameBuilder<TLModelElement> defaultNonLibraryBuilder;
		private CodeGenerationFilenameBuilder<AbstractLibrary> libraryFilenameBuilder;

		/**
		 * @param defaultNonLibraryBuilder
		 *            - builder used for all non {@link AbstractLibrary} models
		 * @param libraryFilenameBuilder
		 *            - builder used only for {@link AbstractLibrary}
		 */
		public TrimmedExampleFilenameBuilder(
				CodeGenerationFilenameBuilder<TLModelElement> defaultNonLibraryBuilder,
				CodeGenerationFilenameBuilder<AbstractLibrary> libraryFilenameBuilder) {
			this.defaultNonLibraryBuilder = defaultNonLibraryBuilder;
			this.libraryFilenameBuilder = libraryFilenameBuilder;
		}

		@Override
		public String buildFilename(TLModelElement item, String fileExtension) {
			if (item instanceof AbstractLibrary) {
				return libraryFilenameBuilder.buildFilename(
						(AbstractLibrary) item, fileExtension);
			} else {
				return defaultNonLibraryBuilder.buildFilename(
						(TLModelElement) item, fileExtension);
			}
		}

	}

	/**
	 * Returns the location of the example output folder for all members of the
	 * given library.
	 * 
	 * @param libraryMember
	 *            the library member element for which the example output folder
	 *            is needed
	 * @param context
	 *            the code generation context
	 * @return String
	 */
	protected abstract String getExampleOutputFolder(
			LibraryMember libraryMember, CodeGenerationContext context);

	/**
	 * Returns the relative path to the XML schema that can be used to validate
	 * the generated XML output.
	 * 
	 * @param schemaFile
	 *            the name of the XML schema for the example output
	 * @param libraryMember
	 *            the library member element for which the schema location is
	 *            needed
	 * @param context
	 *            the code generation context
	 * @return String
	 */
	protected String getSchemaLocation(String schemaFile,
			LibraryMember libraryMember, CodeGenerationContext context) {
		String schemaLocation = null;

		if (schemaFile != null) {
			return getSchemaRelativeFolderPath(libraryMember, context)
					+ schemaFile;
		}
		return schemaLocation;
	}

	/**
	 * Returns a string that specifies the relative folder location of the
	 * schema to be generated for the specified library member.
	 * 
	 * @param libraryMember
	 *            the library member element for which the schema location
	 *            folder is needed
	 * @param context
	 *            the code generation context
	 * @return String
	 */
	protected abstract String getSchemaRelativeFolderPath(
			LibraryMember libraryMember, CodeGenerationContext context);

	/**
	 * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#createContext()
	 */
	protected CodeGenerationContext createContext() {
		CodeGenerationContext context = super.createContext();

		if (!generateMaxDetailsForExamples) {
			context.setValue(CodeGenerationContext.CK_EXAMPLE_DETAIL_LEVEL,
					"MINIMUM");
		}
		if (exampleContext != null) {
			context.setValue(CodeGenerationContext.CK_EXAMPLE_CONTEXT,
					exampleContext);
		}
		if (exampleMaxRepeat != null) {
			context.setValue(CodeGenerationContext.CK_EXAMPLE_CONTEXT,
					exampleMaxRepeat.toString());
		}
		if (exampleMaxDepth != null) {
			context.setValue(CodeGenerationContext.CK_EXAMPLE_CONTEXT,
					exampleMaxDepth.toString());
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
			setGenerateMaxDetailsForExamples(exampleOptions
					.isGenerateMaxDetailsForExamples());
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
	 * Assigns the option flag indicating that example XML documents should be
	 * generated.
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
	 * Assigns the flag indicating whether the maximum amount of detail is to be
	 * included in generated example data. If false, minimum detail will be
	 * generated.
	 * 
	 * @param generateMaxDetailsForExamples
	 *            the boolean flag value to assign
	 */
	public void setGenerateMaxDetailsForExamples(
			boolean generateMaxDetailsForExamples) {
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
	 * Assigns the preferred context to use when producing example values for
	 * simple data types.
	 * 
	 * @param exampleContext
	 *            the context ID to assign
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
	 * Assigns the maximum number of times that repeating elements should be
	 * displayed in generated example output.
	 * 
	 * @param exampleMaxRepeat
	 *            the max repeat value to assign
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
	 * Assigns the maximum depth that should be included for nested elements in
	 * generated example output.
	 * 
	 * @param exampleMaxDepth
	 *            the max depth value to assign
	 */
	public void setExampleMaxDepth(Integer exampleMaxDepth) {
		this.exampleMaxDepth = exampleMaxDepth;
	}

}
