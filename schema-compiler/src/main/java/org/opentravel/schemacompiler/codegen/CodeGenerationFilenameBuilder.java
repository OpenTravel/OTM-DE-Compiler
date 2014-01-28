
package org.opentravel.schemacompiler.codegen;


/**
 * Service to be implemented by components that can generate file names for the target model element(s)
 * of a code generation service.
 * 
 * @param <T>  the type of model element for which filenames can be generated
 * @author S. Livezey
 */
public interface CodeGenerationFilenameBuilder<T> {
	
	/**
	 * Returns the filename that is associated with the given model element.
	 * 
	 * @param item  the model element for which to return a filename
	 * @param fileExtension  the extension of the filename to be created
	 * @return String
	 */
	public String buildFilename(T item, String fileExtension);
	
}
