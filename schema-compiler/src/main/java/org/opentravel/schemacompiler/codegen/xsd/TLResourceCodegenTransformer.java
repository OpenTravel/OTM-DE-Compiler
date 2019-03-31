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

import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLResource</code> objects to the JAXB nodes used to produce the schema output.
 */
public class TLResourceCodegenTransformer extends AbstractXsdTransformer<TLResource,CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLResource source) {
        ObjectTransformer<TLActionFacet,CodegenArtifacts,CodeGenerationTransformerContext> afTransformer =
            getTransformerFactory().getTransformer( TLActionFacet.class, CodegenArtifacts.class );
        CodeGenerationFilter filter = getCodegenFilter();
        CodegenArtifacts artifacts = new CodegenArtifacts();

        // The only TLResource artifacts that need to be represented in the XML schema are the
        // action facets.
        for (TLActionFacet actionFacet : source.getActionFacets()) {
            if ((filter != null) && !filter.processEntity( actionFacet )) {
                continue;
            }
            if (!ResourceCodegenUtils.isTemplateActionFacet( actionFacet )) {
                artifacts.addAllArtifacts( afTransformer.transform( actionFacet ) );
            }
        }
        return artifacts;
    }

}
