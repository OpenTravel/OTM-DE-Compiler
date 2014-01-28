
package org.opentravel.schemacompiler.loader;

import java.util.List;

/**
 * Provides a normalized version of the import declarations from a JAXB library.
 * 
 * @author S. Livezey
 */
public class LibraryModuleImport {
	
	private String namespace;
	private String prefix;
	private List<String> fileHints;
	
	/**
	 * Full constructor.
	 * 
	 * @param namespace  the namespace that was imported by the library
	 * @param prefix  the prefix assigned to the imported namespace
	 * @param fileHints  the file hints for associated resource locations
	 */
	public LibraryModuleImport(String namespace, String prefix, List<String> fileHints) {
		this.namespace = namespace;
		this.prefix = prefix;
		this.fileHints = fileHints;
	}

	/**
	 * Returns the namespace that was imported by the library.
	 *
	 * @return String
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Returns the prefix assigned to the imported namespace.
	 *
	 * @return String
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Returns the file hints for associated resource locations.
	 *
	 * @return List<String>
	 */
	public List<String> getFileHints() {
		return fileHints;
	}
	
}
