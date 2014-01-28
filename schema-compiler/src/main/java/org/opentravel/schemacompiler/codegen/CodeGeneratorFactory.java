/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.springframework.context.ApplicationContext;

/**
 * Factory used to instantiate <code>CodeGenerator</code> instances used to generate output for
 * a specific target format.
 * 
 * @author S. Livezey
 */
public final class CodeGeneratorFactory {
	
	public static final String XSD_TARGET_FORMAT      = "XSD";
	public static final String EXT_XSD_TARGET_FORMAT  = "EXT_XSD";
	public static final String RAS_XSD_TARGET_FORMAT  = "RASXSD";
	public static final String WSDL_TARGET_FORMAT     = "WSDL";
	public static final String XML_TARGET_FORMAT      = "XML";
	
	private static final String FACTORY_NAME = "codeGeneratorFactory";
	private static final Pattern whitespacePattern = Pattern.compile("\\s+");
	
	private Map<String,CodeGeneratorMapping> targetFormatMappings = new HashMap<String,CodeGeneratorMapping>();
	
	/**
	 * Default constructor (private).
	 */
	private CodeGeneratorFactory() {}
	
	/**
	 * Returns the singleton instance of the factory.
	 * 
	 * @return CodeGeneratorFactory
	 */
	public static CodeGeneratorFactory getInstance() {
		return getInstance( FACTORY_NAME );
	}
	
	/**
	 * Returns the singleton instance of the factory with the specified name.
	 * 
	 * @param factoryName  the bean name of the code generation factory in the application context
	 * @return CodeGeneratorFactory
	 */
	public static CodeGeneratorFactory getInstance(String factoryName) {
		ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
		CodeGeneratorFactory factory = (CodeGeneratorFactory) appContext.getBean( factoryName );
		
		return factory;
	}
	
	/**
	 * Returns a new <code>CodeGenerator</code> instance that will produce output for the given
	 * target form.
	 * 
	 * @param <S>  the source meta-model type to be converted
	 * @param targetFormat  the string that identifies the desired target output format
	 * @param sourceType  the source meta-model type to be converted
	 * @return CodeGenerator
	 * @throws IllegalArgumentException  thrown if the requested target format is not supported
	 * @throws CodeGenerationException  thrown if an instance of the code generator cannot be created
	 */
	@SuppressWarnings("unchecked")
	public <S> CodeGenerator<S> newCodeGenerator(String targetFormat, Class<S> sourceType) throws CodeGenerationException {
		// Do some validity checking
		if (targetFormat == null) {
			throw new NullPointerException("Target format cannot be null.");
		} else if (sourceType == null) {
			throw new NullPointerException("Source type cannot be null.");
		} else {
			boolean hasWhitespace = whitespacePattern.matcher(targetFormat).find();
			
			if ((targetFormat.length() == 0) || hasWhitespace) {
				throw new IllegalArgumentException("The requeste target format is not valid: '" + targetFormat + "'");
			}
		}
		
		// Identifiy the code generator class
		String mappingKey = sourceType.getName() + ":" + targetFormat;
		CodeGeneratorMapping mapping = targetFormatMappings.get(mappingKey);
		CodeGenerator<S> codeGenerator = null;
		
		if (mapping != null) {
			Class<? extends CodeGenerator<S>> codegenClass = (Class<? extends CodeGenerator<S>>) mapping.getCodeGenerator();
			
			if (codegenClass != null) {
				try {
					codeGenerator = codegenClass.newInstance();
					
				} catch (Throwable t) {
					throw new CodeGenerationException("Unable to create code generator instance.", t);
				}
			}
		}
		if (codeGenerator == null) {
			throw new IllegalArgumentException("Code generator not specified for source / target format: " + mappingKey);
		}
		return codeGenerator;
	}
	
	/**
	 * Assings the source-type/target-format mappings for all code generators supported by this factory instance.
	 * 
	 * @param mappings  the code generation mappings to assign
	 */
	public void setCodeGeneratorMappings(Collection<CodeGeneratorMapping> mappings) {
		for (CodeGeneratorMapping mapping : mappings) {
			String mappingKey = mapping.getSourceType().getName() + ":" + mapping.getTargetFormat();
			
			targetFormatMappings.put(mappingKey, mapping);
		}
	}
	
	/**
	 * Adds the mappings from each of the nested factories provided to this instance.
	 * 
	 * @param nestedFactories  the code generation factories whose mappings are to be included in this instance
	 */
	public void setNestedFactories(Collection<CodeGeneratorFactory> nestedFactories) {
		for (CodeGeneratorFactory factory : nestedFactories) {
			setCodeGeneratorMappings( factory.targetFormatMappings.values() );
		}
	}
	
}
