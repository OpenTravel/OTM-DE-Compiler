/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.sabre.schemacompiler.loader.LibraryModelLoader;
import com.sabre.schemacompiler.loader.LibraryNamespaceResolver;
import com.sabre.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.symbols.SymbolTableFactory;
import com.sabre.schemacompiler.transform.tl2jaxb.TL2JaxbLibrarySymbolResolver;
import com.sabre.schemacompiler.transform.util.ChameleonFilter;
import com.sabre.schemacompiler.transform.util.LibraryPrefixResolver;
import com.sabre.schemacompiler.util.SchemaCompilerTestUtils;
import com.sabre.schemacompiler.validate.ValidationFindings;

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
