/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb2xsd;

import org.w3._2001.xmlschema.TopLevelElement;

import com.sabre.schemacompiler.model.XSDElement;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TopLevelElement</code> type to the
 * <code>XSDElement</code> type.
 *
 * @author S. Livezey
 */
public class TopLevelElementTransformer extends BaseTransformer<TopLevelElement,XSDElement,DefaultTransformerContext> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public XSDElement transform(TopLevelElement source) {
		return new XSDElement(source.getName(), source);
	}
	
}
