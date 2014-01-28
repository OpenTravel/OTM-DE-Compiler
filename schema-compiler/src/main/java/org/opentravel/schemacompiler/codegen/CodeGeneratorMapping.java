/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen;


/**
 * Specifies the <code>CodeGenerator</code> to be used for a specific source-type and
 * target-format combination.
 *
 * @author S. Livezey
 */
public class CodeGeneratorMapping {
	
	private Class<?> sourceType;
	private String targetFormat;
	private Class<?> codeGenerator;
	
	/**
	 * Returns the source-type of this mapping entry.
	 *
	 * @return Class<?>
	 */
	public Class<?> getSourceType() {
		return sourceType;
	}
	
	/**
	 * Assigns the source-type of this mapping entry.
	 *
	 * @param sourceType  the source type value to assign
	 */
	public void setSourceType(Class<?> sourceType) {
		this.sourceType = sourceType;
	}
	
	/**
	 * Returns the target-format of this mapping entry.
	 *
	 * @return String
	 */
	public String getTargetFormat() {
		return targetFormat;
	}
	
	/**
	 * Assigns the target-format of this mapping entry.
	 *
	 * @param targetFormat  the target format value to assign
	 */
	public void setTargetFormat(String targetFormat) {
		this.targetFormat = targetFormat;
	}
	
	/**
	 * Returns the type of code generator to use for the source-type / target-format
	 * combination.
	 *
	 * @return Class<?>
	 */
	public Class<?> getCodeGenerator() {
		return codeGenerator;
	}
	
	/**
	 * Assigns the type of code generator to use for the source-type / target-format
	 * combination.
	 *
	 * @param codeGenerator  the code generator to assign for this mapping
	 */
	public void setCodeGenerator(Class<?> codeGenerator) {
		this.codeGenerator = codeGenerator;
	}

}
