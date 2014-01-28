/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform;


/**
 * Object transformers implement a one-way mapping transformation from an object of one type
 * to an object of another type.
 * 
 * @param <S>  the source type of the object transformation
 * @param <T>  the target type of the object transformation
 * @param <C>  the type of context required by the transformer instance
 * @author S. Livezey
 */
public interface ObjectTransformer<S,T,C extends ObjectTransformerContext> {
	
	/**
	 * Transforms the source object into a new instance of the target object type.
	 * 
	 * @param source  the source object of the transformation
	 * @return T
	 */
	public T transform(S source);
	
	/**
	 * Assigns the shared context for this <code>ObjectTransformer</code>.
	 * 
	 * @param context  the context with which this transformer is associated
	 */
	public void setContext(C context);
	
}
