/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.LibraryNamespaceResolver;
import org.opentravel.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;
import org.opentravel.schemacompiler.transform.tl2jaxb.TL2JaxbLibrarySymbolResolver;
import org.opentravel.schemacompiler.transform.util.ChameleonFilter;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Abstract base class for test classes that validate the transformation subsystem.
 * 
 * @author S. Livezey
 */
public abstract class AbstractTestTransformers {
	
	public static final String PACKAGE_1_NAMESPACE   = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1";
	public static final String PACKAGE_2_NAMESPACE   = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2";
	public static final String PACKAGE_EXT_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-ext_v2";
	
	private static Map<String,TLModel> testModelsByBaseLocation = new HashMap<String,TLModel>();
	
	/**
	 * Returns the base location (root folder) for the test files that are to be used
	 * to load and construct the model.
	 */
	protected abstract String getBaseLocation();
	
	protected synchronized TLModel getTestModel() throws Exception {
		String baseLocation = getBaseLocation();
		TLModel testModel = testModelsByBaseLocation.get(baseLocation);
		
		if (testModel == null) {
			LibraryNamespaceResolver namespaceResolver = new CatalogLibraryNamespaceResolver(
					new File(getBaseLocation(), "/library-catalog.xml"));
			LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
			
			modelLoader.setNamespaceResolver(namespaceResolver);
			ValidationFindings findings = modelLoader.loadLibraryModel(new URI(PACKAGE_2_NAMESPACE));
			
			SchemaCompilerTestUtils.printFindings(findings);
			
			testModel = modelLoader.getLibraryModel();
			testModelsByBaseLocation.put(baseLocation, testModel);
		}
		return testModel;
	}
	
	protected TLLibrary getLibrary(String namespace, String libraryName) throws Exception {
		TLLibrary library = null;
		
		for (AbstractLibrary lib : getTestModel().getLibrariesForNamespace(namespace)) {
			if (lib.getName().equals(libraryName)) {
				library = (TLLibrary) lib;
				break;
			}
		}
		return library;
	}
	
	protected SymbolResolverTransformerContext getContextJAXBTransformation(AbstractLibrary contextLibrary) throws Exception {
		SymbolResolverTransformerContext context = new SymbolResolverTransformerContext();
		SymbolResolver symbolResolver = new TL2JaxbLibrarySymbolResolver( SymbolTableFactory.newSymbolTableFromModel(getTestModel()) );
		
		symbolResolver.setPrefixResolver(new LibraryPrefixResolver(contextLibrary));
		symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(contextLibrary));
		context.setSymbolResolver(symbolResolver);
		return context;
	}
	
}
