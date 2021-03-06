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

package org.opentravel.schemacompiler.codegen.json;

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.CorrelatedCodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegateFactory;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Performs the translation from <code>TLChoiceObject</code> objects to the JSON schema elements used to produce the
 * output.
 */
public class TLChoiceObjectJsonCodegenTransformer
    extends AbstractJsonSchemaTransformer<TLChoiceObject,CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLChoiceObject source) {
        FacetJsonSchemaDelegateFactory delegateFactory = new FacetJsonSchemaDelegateFactory( context );
        CorrelatedCodegenArtifacts artifacts = new CorrelatedCodegenArtifacts();
        TLLibrary sourceLibrary = (TLLibrary) source.getOwningLibrary();

        generateFacetArtifacts( delegateFactory.getDelegate( source.getSharedFacet() ), artifacts );

        generateContextualFacetArtifacts( source.getChoiceFacets(), sourceLibrary, delegateFactory, artifacts );
        generateContextualFacetArtifacts( FacetCodegenUtils.findGhostFacets( source, TLFacetType.CHOICE ),
            sourceLibrary, delegateFactory, artifacts );

        return artifacts.getConsolidatedArtifacts();
    }

}
