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

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenElements;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacetType;

/**
 * Performs the translation from <code>TLBusinessObject</code> objects to the JAXB nodes used to
 * produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLBusinessObjectCodegenTransformer extends
        AbstractXsdTransformer<TLBusinessObject, CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLBusinessObject source) {
        FacetCodegenDelegateFactory delegateFactory = new FacetCodegenDelegateFactory(context);
        FacetCodegenElements elementArtifacts = new FacetCodegenElements();
        CodegenArtifacts otherArtifacts = new CodegenArtifacts();

        generateFacetArtifacts(delegateFactory.getDelegate(source.getIdFacet()), elementArtifacts,
                otherArtifacts);
        generateFacetArtifacts(delegateFactory.getDelegate(source.getSummaryFacet()),
                elementArtifacts, otherArtifacts);
        generateFacetArtifacts(delegateFactory.getDelegate(source.getDetailFacet()),
                elementArtifacts, otherArtifacts);

        generateContextualFacetArtifacts(source.getCustomFacets(), delegateFactory, elementArtifacts, otherArtifacts);
        generateContextualFacetArtifacts(FacetCodegenUtils.findGhostFacets(source, TLFacetType.CUSTOM),
        		delegateFactory, elementArtifacts, otherArtifacts);
        generateContextualFacetArtifacts(source.getQueryFacets(), delegateFactory, elementArtifacts, otherArtifacts);
        generateContextualFacetArtifacts(FacetCodegenUtils.findGhostFacets(source, TLFacetType.QUERY),
        		delegateFactory, elementArtifacts, otherArtifacts);
        generateContextualFacetArtifacts(source.getUpdateFacets(), delegateFactory, elementArtifacts, otherArtifacts);
        generateContextualFacetArtifacts(FacetCodegenUtils.findGhostFacets(source, TLFacetType.UPDATE),
        		delegateFactory, elementArtifacts, otherArtifacts);
        
        return buildCorrelatedArtifacts(source, elementArtifacts, otherArtifacts);
    }
    
}
