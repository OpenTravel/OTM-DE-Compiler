
package org.opentravel.schemacompiler.loader.impl;

import java.io.InputStream;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.w3._2001.xmlschema.Schema;

/**
 * Built-in library loader that obtains its content from a legacy schema (.xsd) file.
 * 
 * @author S. Livezey
 */
public class LegacySchemaBuiltInLibraryLoader extends AbstractBuiltInLibraryLoader {
	
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
