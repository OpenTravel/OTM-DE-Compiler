/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import java.io.File;

import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;

import com.sabre.schemacompiler.codegen.CodeGenerationContext;
import com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import com.sabre.schemacompiler.codegen.CodeGenerationFilter;
import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.codegen.impl.CodegenArtifacts;
import com.sabre.schemacompiler.codegen.impl.LibraryFilterBuilder;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>BuiltInLibrary</code> objects to the JAXB nodes used
 * to produce the schema output.
 *
 * @author S. Livezey
 */
public class BuiltInLibraryCodegenTransformer extends AbstractXsdTransformer<BuiltInLibrary,Schema> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Schema transform(BuiltInLibrary source) {
		CodeGenerationFilter filter = context.getCodeGenerator().getFilter();
		Schema schema = createSchema(source.getNamespace(), null);
		
		// Add the application info for this library
		Annotation schemaAnnotation = new Annotation();
		
		schemaAnnotation.getAppinfoOrDocumentation().add( XsdCodegenUtils.getAppInfo(source, context.getCodegenContext()) );
		schema.getIncludeOrImportOrRedefine().add(schemaAnnotation);
		
		// Add entries for each non-service term declaration
		for (LibraryMember member : source.getNamedMembers()) {
			ObjectTransformer<LibraryMember,CodegenArtifacts,CodeGenerationTransformerContext> transformer =
				getTransformerFactory().getTransformer(member, CodegenArtifacts.class);
			
			if ((transformer != null) && ((filter == null) || filter.processEntity(member))) {
				CodegenArtifacts artifacts = transformer.transform(member);
				
				if (artifacts != null) {
					for (OpenAttrs artifact : artifacts.getArtifactsOfType(OpenAttrs.class)) {
						schema.getSimpleTypeOrComplexTypeOrGroup().add(artifact);
					}
				}
			}
		}
		
		// Add entries for all imports and includes
		CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder =
				(CodeGenerationFilenameBuilder<AbstractLibrary>) context.getCodeGenerator().getFilenameBuilder();
		CodeGenerationFilter libraryFilter = new LibraryFilterBuilder (source )
				.setGlobalFilter( context.getCodeGenerator().getFilter() ).buildFilter();
		
		addImports(schema, source, filenameBuilder, libraryFilter);
		addIncludes(schema, source, filenameBuilder, libraryFilter);
		
		return schema;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodegenTransformer#getBuiltInSchemaOutputLocation()
	 */
	@Override
	protected String getBuiltInSchemaOutputLocation() {
		// Since we are generating a built-in schema, all imports should be located in the local directory
		// instead of the '/built-ins' sub-folder
		return "";
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.AbstractXsdTransformer#getBaseOutputFolder()
	 */
	@Override
	protected File getBaseOutputFolder() {
		CodeGenerationContext cgContext = context.getCodegenContext();
		return new File(XsdCodegenUtils.getBaseOutputFolder(cgContext), XsdCodegenUtils.getBuiltInSchemaOutputLocation(cgContext));
	}
	
}
