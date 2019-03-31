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
import org.opentravel.schemacompiler.model.TLContextualFacet;

/**
 * Performs the translation from <code>TLContextualFacet</code> objects to the JSON schema elements used to produce the
 * output.
 */
public class TLContextualFacetJsonCodegenTransformer
    extends AbstractJsonSchemaTransformer<TLContextualFacet,CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLContextualFacet source) {
        FacetJsonSchemaDelegateFactory delegateFactory = new FacetJsonSchemaDelegateFactory( context );
        CorrelatedCodegenArtifacts artifacts = new CorrelatedCodegenArtifacts();

        artifacts.addAllArtifacts( delegateFactory.getDelegate( source ).generateArtifacts() );
        return artifacts.getConsolidatedArtifacts();
    }

}
