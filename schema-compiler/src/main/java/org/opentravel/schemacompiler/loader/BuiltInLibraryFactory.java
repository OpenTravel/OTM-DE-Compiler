
package org.opentravel.schemacompiler.loader;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.BuiltInLibrary;

/**
 * Factory used to construct the list of built-in libraries that should be included with
 * every model instance that is processed by the loader.
 * 
 * @author S. Livezey
 */
public final class BuiltInLibraryFactory {
	
	private List<BuiltInLibraryLoader> loaders;
	
	/**
	 * Returns the default factory instance.
	 * 
	 * @return BuiltInLibraryFactory
	 */
	public static BuiltInLibraryFactory getInstance() {
		return (BuiltInLibraryFactory) SchemaCompilerApplicationContext.getContext().getBean(
				SchemaCompilerApplicationContext.BUILT_IN_LIBRARY_FACTORY);
	}
	
	/**
	 * Returns the list of built-in library loaders to be used by this factory.
	 * 
	 * @return List<BuiltInLibraryLoader>
	 */
	public List<BuiltInLibraryLoader> getLoaders() {
		return loaders;
	}
	
	/**
	 * Assigns the list of built-in library loaders to be used by this factory.
	 * 
	 * @param loaders  the list of built-in laoders
	 */
	public void setLoaders(List<BuiltInLibraryLoader> loaders) {
		this.loaders = loaders;
	}
	
	/**
	 * Returns a list of built-in library instances to be included in a new model.
	 * 
	 * @return List<BuiltInLibrary>
	 */
	public List<BuiltInLibrary> getBuiltInLibraries() {
		List<BuiltInLibrary> builtIns = new ArrayList<BuiltInLibrary>();
		
		for (BuiltInLibraryLoader loader : loaders) {
			try {
				BuiltInLibrary library = loader.loadBuiltInLibrary();
				
				if (library != null) {
					builtIns.add(library);
				}
			} catch (LibraryLoaderException e) {
				e.printStackTrace(); // print error and move on to the next library
			}
		}
		return builtIns;
	}
	
}
