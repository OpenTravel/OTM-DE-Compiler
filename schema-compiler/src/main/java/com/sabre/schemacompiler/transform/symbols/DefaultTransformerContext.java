/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.symbols;

import com.sabre.schemacompiler.transform.ObjectTransformerContext;
import com.sabre.schemacompiler.transform.TransformerFactory;

/**
 * Default implementation of the <code> implements ObjectTransformerContext</code>.
 * 
 * @author S. Livezey
 */
public class DefaultTransformerContext implements ObjectTransformerContext {
	
	private TransformerFactory<?> factory;
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformerContext#getTransformerFactory()
	 */
	@Override
	public TransformerFactory<?> getTransformerFactory() {
		return factory;
	}

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformerContext#setTransformerFactory(com.sabre.schemacompiler.transform.TransformerFactory)
	 */
	@Override
	public void setTransformerFactory(TransformerFactory<?> factory) {
		this.factory = factory;
	}
	
}
