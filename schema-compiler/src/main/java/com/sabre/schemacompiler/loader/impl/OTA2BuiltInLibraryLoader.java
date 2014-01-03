/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.loader.impl;

import java.io.InputStream;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.loader.LibraryInputSource;
import com.sabre.schemacompiler.loader.LibraryLoaderException;
import com.sabre.schemacompiler.loader.LibraryModuleInfo;
import com.sabre.schemacompiler.loader.LibraryModuleLoader;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.TransformerFactory;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Built-in library loader that obtains its content from an OTA2 <code>TLLibrary</code> file.
 * 
 * @author S. Livezey
 */
public class OTA2BuiltInLibraryLoader extends AbstractBuiltInLibraryLoader {
	
	/**
	 * @see com.sabre.schemacompiler.loader.BuiltInLibraryLoader#loadBuiltInLibrary()
	 */
	@Override
	public BuiltInLibrary loadBuiltInLibrary() throws LibraryLoaderException {
		LibraryInputSource<InputStream> inputSource = getInputSource();
		BuiltInLibrary library = null;
		
		try {
			// First, load the schema from the specified classpath location
			LibraryModuleLoader<InputStream> moduleLoader = new MultiVersionLibraryModuleLoader();
			ValidationFindings findings = new ValidationFindings();
			LibraryModuleInfo<Object> libraryInfo = moduleLoader.loadLibrary(inputSource, findings);
			
			// Next, transform the schema into an XSDLibrary
			if (!findings.hasFinding()) {
				DefaultTransformerContext transformContext = new DefaultTransformerContext();
				TransformerFactory<DefaultTransformerContext> transformFactory =
						TransformerFactory.getInstance(SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY, transformContext);
				ObjectTransformer<Object,TLLibrary,DefaultTransformerContext> transformer =
						transformFactory.getTransformer(libraryInfo.getJaxbArtifact(), TLLibrary.class);
				TLLibrary ota2Library = transformer.transform(libraryInfo.getJaxbArtifact());
				
				if (ota2Library.getPrefix() == null) {
					ota2Library.setPrefix(getLibraryDeclaration().getDefaultPrefix());
				}
				
				if (ota2Library != null) {
					library = new BuiltInLibrary(ota2Library.getNamespace(), ota2Library.getName(), ota2Library.getPrefix(),
							inputSource.getLibraryURL(), ota2Library.getNamedMembers(), ota2Library.getNamespaceImports(),
							ota2Library.getIncludes(), getLibraryDeclaration(), ota2Library.getVersionScheme());
				}
			}
		} catch (Throwable t) {
			throw new LibraryLoaderException("Error constructing built-in library instance (" + inputSource.getLibraryURL() + ")", t);
		}
		return library;
	}

}
