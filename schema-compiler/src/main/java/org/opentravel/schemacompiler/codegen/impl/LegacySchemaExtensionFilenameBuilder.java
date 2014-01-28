
package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;

/**
 * Filename builder used to generate filenames for legacy schema extension files produced
 * during the code generation process.
 * 
 * @param <T>  the type of model element for which filenames can be generated
 * @author S. Livezey
 */
public class LegacySchemaExtensionFilenameBuilder<T> implements CodeGenerationFilenameBuilder<T> {
	
	private CodeGenerationFilenameBuilder<T> delegateBuilder;
	
	/**
	 * Constructor that specifies the builder to use when generating the base filename from
	 * which the extension schema's filename will be derived.
	 * 
	 * @param filenameBuilder  the base filename builder to assign
	 */
	public LegacySchemaExtensionFilenameBuilder(CodeGenerationFilenameBuilder<T> filenameBuilder) {
		this.delegateBuilder = filenameBuilder;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(java.lang.Object, java.lang.String)
	 */
	@Override
	public String buildFilename(T item, String fileExtension) {
		String fileExt = (fileExtension.length() == 0) ? "" : ("." + fileExtension);
		String baseFilename = delegateBuilder.buildFilename(item, "");
		
		return baseFilename + "_Ext" + fileExt;
	}
	
}
