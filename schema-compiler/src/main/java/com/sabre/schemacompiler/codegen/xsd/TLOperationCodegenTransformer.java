/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import com.sabre.schemacompiler.codegen.impl.CodegenArtifacts;
import com.sabre.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import com.sabre.schemacompiler.model.TLOperation;

/**
 * Performs the translation from <code>TLOperation</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLOperationCodegenTransformer extends AbstractXsdTransformer<TLOperation,CodegenArtifacts> {
	
	@Override
	public CodegenArtifacts transform(TLOperation source) {
		FacetCodegenDelegateFactory delegateFactory = new FacetCodegenDelegateFactory(context);
		CodegenArtifacts artifacts = new CodegenArtifacts();
		
		artifacts.addAllArtifacts( buildCorrelatedArtifacts(source,
				delegateFactory.getDelegate(source.getRequest()).generateElements(),
				delegateFactory.getDelegate(source.getRequest()).generateArtifacts()
			) );
		artifacts.addAllArtifacts( buildCorrelatedArtifacts(source,
				delegateFactory.getDelegate(source.getResponse()).generateElements(),
				delegateFactory.getDelegate(source.getResponse()).generateArtifacts()
			) );
		artifacts.addAllArtifacts( buildCorrelatedArtifacts(source,
				delegateFactory.getDelegate(source.getNotification()).generateElements(),
				delegateFactory.getDelegate(source.getNotification()).generateArtifacts()
			) );
		return artifacts;
	}
	
}
