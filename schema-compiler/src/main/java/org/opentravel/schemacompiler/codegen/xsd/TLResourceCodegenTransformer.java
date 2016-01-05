/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemacompiler.codegen.xsd;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLResource</code> objects to the JAXB nodes used to
 * produce the schema output.
 */
public class TLResourceCodegenTransformer extends AbstractXsdTransformer<TLResource,CodegenArtifacts> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLResource source) {
        ObjectTransformer<TLActionRequest, CodegenArtifacts, CodeGenerationTransformerContext> requestTransformer = getTransformerFactory()
                .getTransformer(TLActionRequest.class, CodegenArtifacts.class);
        ObjectTransformer<TLActionResponse, CodegenArtifacts, CodeGenerationTransformerContext> responseTransformer = getTransformerFactory()
                .getTransformer(TLActionResponse.class, CodegenArtifacts.class);
        CodegenArtifacts artifacts = new CodegenArtifacts();
        
        // The only TLResource artifacts that need to be represented in the XML schema are the
        // action requests and responses.
        for (TLAction action : source.getActions()) {
        	if (action.getRequest() != null) {
        		artifacts.addAllArtifacts( requestTransformer.transform( action.getRequest() ) );
        	}
        	for (TLActionResponse response : action.getResponses()) {
        		artifacts.addAllArtifacts( responseTransformer.transform( response ) );
        	}
        }
        return artifacts;
	}
	
}
