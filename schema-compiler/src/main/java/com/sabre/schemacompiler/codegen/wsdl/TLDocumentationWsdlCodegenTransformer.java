/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.codegen.wsdl;

import org.xmlsoap.schemas.wsdl.TDocumentation;

import com.sabre.schemacompiler.codegen.xsd.AbstractXsdTransformer;
import com.sabre.schemacompiler.model.TLDocumentation;

/**
 * Performs the translation from <code>TLDocumentation</code> objects to the JAXB nodes used
 * to produce the WSDL output.
 *
 * @author S. Livezey
 */
public class TLDocumentationWsdlCodegenTransformer extends AbstractXsdTransformer<TLDocumentation,TDocumentation> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TDocumentation transform(TLDocumentation source) {
		String description = trimString( source.getDescription() );
		TDocumentation documentation = null;
		
		if (description != null) {
			documentation = new TDocumentation();
			documentation.getContent().add( description );
		}
		return documentation;
	}
	
}
