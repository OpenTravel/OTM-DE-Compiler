/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.loader.impl;

import java.io.InputStream;

import org.w3._2001.xmlschema.Schema;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.ioc.SchemaDeclaration;
import com.sabre.schemacompiler.loader.LibraryInputSource;
import com.sabre.schemacompiler.loader.LibraryLoaderException;
import com.sabre.schemacompiler.loader.LibraryModuleInfo;
import com.sabre.schemacompiler.loader.LibraryModuleLoader;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.TransformerFactory;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Built-in library loader that obtains its content from a legacy schema (.xsd) file.
 * 
 * @author S. Livezey
 */
public class LegacySchemaBuiltInLibraryLoader extends AbstractBuiltInLibraryLoader {
	
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
			LibraryModuleInfo<Schema> schemaInfo = moduleLoader.loadSchema(inputSource, findings);
			
			// Next, transform the schema into an XSDLibrary
			if (!findings.hasFinding()) {
				DefaultTransformerContext transformContext = new DefaultTransformerContext();
				TransformerFactory<DefaultTransformerContext> transformerFactory =
						TransformerFactory.getInstance(SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY, transformContext);
				ObjectTransformer<Schema,XSDLibrary,DefaultTransformerContext> transformer =
						transformerFactory.getTransformer(schemaInfo.getJaxbArtifact(), XSDLibrary.class);
				XSDLibrary xsdLibrary = transformer.transform(schemaInfo.getJaxbArtifact());
				
				if (xsdLibrary.getPrefix() == null) {
					xsdLibrary.setPrefix(getLibraryDeclaration().getDefaultPrefix());
				}
				
				if (xsdLibrary != null) {
					SchemaDeclaration libraryDeclaration = getLibraryDeclaration();
					
					library = new BuiltInLibrary(schemaInfo.getJaxbArtifact().getTargetNamespace(), libraryDeclaration.getName(),
							libraryDeclaration.getDefaultPrefix(), inputSource.getLibraryURL(), xsdLibrary.getNamedMembers(),
							xsdLibrary.getNamespaceImports(), xsdLibrary.getIncludes(), getLibraryDeclaration(), xsdLibrary.getVersionScheme());
				}
			}
		} catch (Throwable t) {
			throw new LibraryLoaderException("Error constructing built-in library instance (" + inputSource.getLibraryURL() + ")");
		}
		return library;
	}

}
