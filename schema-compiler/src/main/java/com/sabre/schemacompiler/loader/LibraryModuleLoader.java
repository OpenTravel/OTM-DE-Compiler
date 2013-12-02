/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.loader;

import java.net.URL;

import org.w3._2001.xmlschema.Schema;

import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Loader that handles the loading and/or parsing of content from a single library
 * module's input source.
 * 
 * @param <C>  the content type must be returned by input sources used by the loader
 * @author S. Livezey
 */
public interface LibraryModuleLoader<C> {
	
	/**
	 * Creates and returns a new input source for the library module located at the specified URL.
	 * 
	 * @param libraryUrl  the URL of the library module to load
	 * @return LibraryInputSource<C>
	 */
	public LibraryInputSource<C> newInputSource(URL libraryUrl);
	
	/**
	 * Returns true if the given input source is for a <code>Library</code> element.  If the input source
	 * is directed at a <code>Schema</code> resource, false will be returned.
	 * 
	 * @param inputSource  the input source to analyze
	 * @return boolean
	 */
	public boolean isLibraryInputSource(LibraryInputSource<C> inputSource);
	
	/**
	 * Loads the content of a single library module from the input source provided.  If the library
	 * cannot be loaded from the specified URL, a validation finding will be added to the collection
	 * provided, and this method will return null.
	 * 
	 * @param inputSource  the input source from which to load module's content
	 * @param validationFindings  the validation errors/warnings discovered during the loading process
	 * @return LibraryModuleInfo<Object>
	 * @throws LibraryLoaderException  thrown if a system-level exception occurs
	 */
	public LibraryModuleInfo<Object> loadLibrary(LibraryInputSource<C> inputSource, ValidationFindings validationFindings) throws LibraryLoaderException;
	
	/**
	 * Loads the content of a single XML schema from the input source provided.  If the schema
	 * cannot be loaded from the specified URL, a validation finding will be added to the collection
	 * provided, and this method will return null.
	 * 
	 * @param inputSource  the input source from which to load module's content
	 * @param validationFindings  the validation errors/warnings discovered during the loading process
	 * @return LibraryModuleInfo<Schema>
	 * @throws LibraryLoaderException  thrown if a system-level exception occurs
	 */
	public LibraryModuleInfo<Schema> loadSchema(LibraryInputSource<C> inputSource, ValidationFindings validationFindings) throws LibraryLoaderException;
	
}
