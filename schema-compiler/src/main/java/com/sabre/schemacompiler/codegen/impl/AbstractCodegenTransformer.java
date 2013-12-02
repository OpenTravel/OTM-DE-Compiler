/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.impl;

import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Base class for all <code>ObjectTransformer</code> implementations that are part of the
 * code generation subsystem.
 * 
 * @param <S>  the source type of the object transformation
 * @param <T>  the target type of the object transformation
 * @author S. Livezey
 */
public abstract class AbstractCodegenTransformer<S,T> extends BaseTransformer<S,T,CodeGenerationTransformerContext> {
	
	/**
	 * Returns the sub-folder location (relative to the target output folder) where built-in schemas should
	 * be stored during the code generation process.  If no sub-folder location is specified by the code
	 * generation context, this method will return an empty string, indicating that built-ins schemas should
	 * be saved in the same target output folder as the user-defined library/service output.
	 * 
	 * @return String
	 */
	protected String getBuiltInSchemaOutputLocation() {
		return XsdCodegenUtils.getBuiltInSchemaOutputLocation( context.getCodegenContext() );
	}
	
	/**
	 * Returns the sub-folder location (relative to the target output folder) where legacy schemas should
	 * be stored during the code generation process.  If no sub-folder location is specified by the code
	 * generation context, this method will return an empty string, indicating that legacy schemas should
	 * be saved in the same target output folder as the user-defined library/service output.
	 * 
	 * @return String
	 */
	protected String getLegacySchemaOutputLocation() {
		return XsdCodegenUtils.getLegacySchemaOutputLocation( context.getCodegenContext() );
	}
	
}
