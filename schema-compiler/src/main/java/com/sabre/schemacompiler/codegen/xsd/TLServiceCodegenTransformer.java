/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.codegen.impl.CodegenArtifacts;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLService</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLServiceCodegenTransformer extends AbstractXsdTransformer<TLService,CodegenArtifacts> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
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
