/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen;

import java.util.HashMap;
import java.util.Map;

/**
 * Context that provides global information that may be required during the schema
 * generation process.
 * 
 * @author S. Livezey
 */
public class CodeGenerationContext {
	
	/** Context key indicating the folder location to use for generated file output. */	
	public static final String CK_OUTPUT_FOLDER = "schemacompiler.OutputFolder";
	
	/** Context key indicating the name of the XSD schema file to be generated. */	
	public static final String CK_SCHEMA_FILENAME = "schemacompiler.SchemaFilename";
	
	/** Context key indicating the name of the project (.otp) file for which to generate output. */	
	public static final String CK_PROJECT_FILENAME = "schemacompiler.ProjectFilename";
	
	/** Context key indicating the base URL for all service endpoints generated in WSDL documents. */	
	public static final String CK_SERVICE_ENDPOINT_URL = "schemacompiler.serviceEndpointUrl";
	
	/** Context key indicating whether compile-time schema dependencies should be copied to the output folder. */	
	public static final String CK_COPY_COMPILE_TIME_DEPENDENCIES = "schemacompiler.copyCompileTimeDependencies";
	
	/**
	 * Context key indicating the amount of detail that should be provided during example generation
	 * (valid values are "MINIMUM" and "MAXIMUM" - default is "MAXIMUM").
	 */	
	public static final String CK_EXAMPLE_DETAIL_LEVEL = "schemacompiler.example.detailLevel";
	
	/** Context key indicating the preferred context to use when creating example values for simple data types. */	
	public static final String CK_EXAMPLE_CONTEXT = "schemacompiler.example.context";
	
	/** Context key indicating the maximum number of times that repeating elements should be displayed in generated example output. */	
	public static final String CK_EXAMPLE_MAX_REPEAT = "schemacompiler.example.maxRepeat";
	
	/** Context key indicating the maximum depth that should be included for nested elements in generated example output. */	
	public static final String CK_EXAMPLE_MAX_DEPTH = "schemacompiler.example.maxDepth";
	
	/** Context key indicating the relative path where schema files are located, relative to the example XML document being generated. */	
	public static final String CK_EXAMPLE_SCHEMA_RELATIVE_PATH = "schemacompiler.example.schemaRelativePath";
	
	/** Context key indicating the sub-folder location to which built-in schemas should be generated. */	
	public static final String CK_BUILTIN_SCHEMA_FOLDER = "schemacompiler.built-ins.folder";
	
	/** Context key indicating the sub-folder location to which legacy schemas should be generated. */	
	public static final String CK_LEGACY_SCHEMA_FOLDER = "schemacompiler.legacy.folder";
	
	private Map<String,String> contents = new HashMap<String, String>();
	
	/**
	 * Returns a value from the context.  If a value for the specified key is not defined,
	 * this method will return null.
	 * 
	 * @param key  the key that identifies the desired value
	 * @return String
	 */
	public String getValue(String key) {
		return contents.get(key);
	}
	
	/**
	 * Returns an integer value from the context.  If the value for the specified key is not
	 * defined (or the value is not an integer), this method will return zero.
	 * 
	 * @param key  the key that identifies the desired value
	 * @return int
	 */
	public int getIntValue(String key) {
		String value = getValue(key);
		int intValue = 0;
		
		if (value != null) {
			try {
				intValue = Integer.parseInt(value);
				
			} catch (NumberFormatException e) {
				// No exception - just return zero
			}
		}
		return intValue;
	}
	
	/**
	 * Returns an boolean value from the context.  If the value for the specified key is not
	 * defined (or the value is not a boolean), this method will return false.
	 * 
	 * @param key  the key that identifies the desired value
	 * @return boolean
	 */
	public boolean getBooleanValue(String key) {
		String value = getValue(key);
		boolean booleanValue = false;
		
		if (value != null) {
			booleanValue = Boolean.parseBoolean(value);
		}
		return booleanValue;
	}
	
	/**
	 * Assigns the given value to the specified key.  If the value contains leading or trailing
	 * white space, it will be trimmed before adding it to the context.  A null or blank value
	 * will cause the key to be removed from the context.
	 * 
	 * @param key  the key with which the value should be associated
	 * @param value  the value to assign
	 */
	public void setValue(String key, String value) {
		String trimmedValue = (value == null) ? null : value.trim();
		
		if ((value != null) && !value.equals("")) {
			contents.put(key, trimmedValue);
		} else {
			removeValue(key);
		}
	}
	
	/**
	 * Removes the specified value from the context.
	 * 
	 * @param key  the key associated with the value to be removed
	 */
	public void removeValue(String key) {
		contents.remove(key);
	}
	
	/**
	 * Returns an exact copy of this code generation context.
	 * 
	 * @return CodeGenerationContext
	 */
	public CodeGenerationContext getCopy() {
		CodeGenerationContext copy = new CodeGenerationContext();
		
		copy.contents.putAll(this.contents);
		return copy;
	}
	
}
