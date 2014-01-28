/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.xsd;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLService</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLServiceCodegenTransformer extends AbstractXsdTransformer<TLService,CodegenArtifacts> {

	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLService source) {
		ObjectTransformer<TLOperation,CodegenArtifacts,CodeGenerationTransformerContext> opTransformer =
				getTransformerFactory().getTransformer(TLOperation.class, CodegenArtifacts.class);
		CodegenArtifacts artifacts = new CodegenArtifacts();
		
		for (TLOperation operation : source.getOperations()) {
			artifacts.addAllArtifacts( opTransformer.transform(operation) );
		}
		return artifacts;
	}
	
}
