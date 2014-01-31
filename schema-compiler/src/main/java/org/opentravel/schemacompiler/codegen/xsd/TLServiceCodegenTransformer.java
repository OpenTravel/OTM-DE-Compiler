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
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLService</code> objects to the JAXB nodes used to produce
 * the schema output.
 * 
 * @author S. Livezey
 */
public class TLServiceCodegenTransformer extends
        AbstractXsdTransformer<TLService, CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLService source) {
        ObjectTransformer<TLOperation, CodegenArtifacts, CodeGenerationTransformerContext> opTransformer = getTransformerFactory()
                .getTransformer(TLOperation.class, CodegenArtifacts.class);
        CodegenArtifacts artifacts = new CodegenArtifacts();

        for (TLOperation operation : source.getOperations()) {
            artifacts.addAllArtifacts(opTransformer.transform(operation));
        }
        return artifacts;
    }

}
