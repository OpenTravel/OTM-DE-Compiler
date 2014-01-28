/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.ioc;

import org.opentravel.schemacompiler.ota2.OTA2CompilerConstants;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Singleton handle to the spring application context.
 * 
 * @author S. Livezey
 */
public final class SchemaCompilerApplicationContext implements ApplicationContextAware {
	
	public static final String BUILT_IN_LIBRARY_FACTORY         = "builtInLibraryFactory";
	public static final String SCHEMA_DEPENDENCIES              = "schemaDependencies";
	public static final String SYMBOL_TABLE_FACTORY             = "symbolTableFactory";
	public static final String LOADER_TRANSFORMER_FACTORY       = "loaderTransformFactory";
	public static final String SAVER_TRANSFORMER_FACTORY        = "saverTransformFactory";
	public static final String XSD_CODEGEN_TRANSFORMER_FACTORY  = "xsdCodeGeneratorTransformFactory";
	public static final String WSDL_CODEGEN_TRANSFORMER_FACTORY = "wsdlCodeGeneratorTransformFactory";
	public static final String CODE_GENERATION_WSDL_BINDINGS    = "codeGenerationWsdlBindings";
	public static final String EXAMPLE_GENERATOR                = "exampleGenerator";
	public static final String PROTECTED_NAMESPACE_REGISTRY     = "protectedNamespaceRegistry";
	public static final String LIBRARY_ACCESS_CONTROLLER        = "libraryAccessController";
	
	private static ApplicationContext context;
	
	/**
	 * Returns the spring application context for the schema compiler.
	 *
	 * @return ApplicationContext
	 */
	public static ApplicationContext getContext() {
		if (context == null) {
			CompilerExtensionRegistry.setActiveExtension( OTA2CompilerConstants.OTA2_COMPILER_EXTENSION_ID );
		}
		return context;
	}
	
	/**
	 * Assigns the active application context for the schema compiler.
	 * 
	 * @param context  the application context to assign
	 */
	static void setActiveContext(ApplicationContext context) {
		SchemaCompilerApplicationContext.context = context;
	}
	
	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		context = ctx;
	}
	
}
