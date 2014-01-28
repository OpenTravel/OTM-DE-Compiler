/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.jaxb2xsd;

import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;
import org.w3._2001.xmlschema.TopLevelComplexType;

/**
 * Handles the transformation of objects from the <code>TopLevelComplexType</code> type to the
 * <code>XSDComplexType</code> type.
 *
 * @author S. Livezey
 */
public class TopLevelComplexTypeTransformer extends BaseTransformer<TopLevelComplexType,XSDComplexType,DefaultTransformerContext> {

	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public XSDComplexType transform(TopLevelComplexType source) {
		return new XSDComplexType(source.getName(), source);
	}
	
}
