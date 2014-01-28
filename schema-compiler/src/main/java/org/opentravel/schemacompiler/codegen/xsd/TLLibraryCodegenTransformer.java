/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.xsd;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilterBuilder;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;

/**
 * Performs the translation from <code>TLLibrary</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLLibraryCodegenTransformer extends AbstractXsdTransformer<TLLibrary,Schema> {

	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
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
