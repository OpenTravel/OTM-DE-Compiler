/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.loader.impl;

import java.io.InputStream;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Built-in library loader that obtains its content from an OTA2 <code>TLLibrary</code> file.
 * 
 * @author S. Livezey
 */
public class OTA2BuiltInLibraryLoader extends AbstractBuiltInLibraryLoader {
	
	/**
	 * @see org.opentravel.schemacompiler.loader.BuiltInLibraryLoader#loadBuiltInLibrary()
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
