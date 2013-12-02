/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb2xsd;

import org.w3._2001.xmlschema.TopLevelSimpleType;

import com.sabre.schemacompiler.model.XSDSimpleType;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TopLevelSimpleType</code> type to the
 * <code>XSDSimpleType</code> type.
 *
 * @author S. Livezey
 */
public class TopLevelSimpleTypeTransformer extends BaseTransformer<TopLevelSimpleType,XSDSimpleType,DefaultTransformerContext> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public XSDSimpleType transform(TopLevelSimpleType source) {
		return new XSDSimpleType(source.getName(), source);
	}
	
}
