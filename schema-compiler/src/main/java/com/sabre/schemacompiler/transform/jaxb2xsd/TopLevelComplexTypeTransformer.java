/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb2xsd;

import org.w3._2001.xmlschema.TopLevelComplexType;

import com.sabre.schemacompiler.model.XSDComplexType;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TopLevelComplexType</code> type to the
 * <code>XSDComplexType</code> type.
 *
 * @author S. Livezey
 */
public class TopLevelComplexTypeTransformer extends BaseTransformer<TopLevelComplexType,XSDComplexType,DefaultTransformerContext> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public XSDComplexType transform(TopLevelComplexType source) {
		return new XSDComplexType(source.getName(), source);
	}
	
}
