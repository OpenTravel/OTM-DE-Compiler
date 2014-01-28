/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.providers;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/**
 * Resolves the JAXB context to the one that includes the required packages for all JAXB classes.
 * 
 * @author S. Livezey
 */
@Provider
public final class JAXBContextResolver implements ContextResolver<JAXBContext> {
	
	private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.repositoryinfo_v01_00";
	
	private static JAXBContext jaxbContext;
	
	/**
	 * @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
	 */
	public JAXBContext getContext(Class<?> type) {
		return jaxbContext;
	}
	
	/**
	 * Initializes the shared JAXB context.
	 */
	static {
		try {
			jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}