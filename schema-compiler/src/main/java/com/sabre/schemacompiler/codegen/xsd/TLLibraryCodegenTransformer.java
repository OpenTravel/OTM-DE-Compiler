/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;

import com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import com.sabre.schemacompiler.codegen.CodeGenerationFilter;
import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.codegen.impl.CodegenArtifacts;
import com.sabre.schemacompiler.codegen.impl.LibraryFilterBuilder;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLLibrary</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLLibraryCodegenTransformer extends AbstractXsdTransformer<TLLibrary,Schema> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Schema transform(TLLibrary source) {
		CodeGenerationFilter filter = context.getCodeGenerator().getFilter();
		Schema schema = createSchema(source.getNamespace(), source.getVersion());
		
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
	
}
