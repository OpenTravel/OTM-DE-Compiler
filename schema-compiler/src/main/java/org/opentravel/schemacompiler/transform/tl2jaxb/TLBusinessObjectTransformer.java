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
package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_05.BusinessObject;
import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_05.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_05.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_05.FacetContextual;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLBusinessObject</code> type to the
 * <code>BusinessObject</code> type.
 * 
 * @author S. Livezey
 */
public class TLBusinessObjectTransformer extends
        TLComplexTypeTransformer<TLBusinessObject, BusinessObject> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public BusinessObject transform(TLBusinessObject source) {
        ObjectTransformer<TLFacet, Facet, SymbolResolverTransformerContext> facetTransformer = getTransformerFactory()
                .getTransformer(TLFacet.class, Facet.class);
        ObjectTransformer<TLContextualFacet, FacetContextual, SymbolResolverTransformerContext> facetContextualTransformer =
        		getTransformerFactory().getTransformer(TLContextualFacet.class, FacetContextual.class);
        ObjectTransformer<TLEquivalent, Equivalent, SymbolResolverTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(TLEquivalent.class, Equivalent.class);
        BusinessObject businessObject = new BusinessObject();

        businessObject.setName(trimString(source.getName(), false));
        businessObject.setNotExtendable(source.isNotExtendable());
        businessObject.getAliases().addAll(getAliasNames(source.getAliases()));

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            businessObject.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
            businessObject.getEquivalent().add(equivTransformer.transform(sourceEquiv));
        }

        businessObject.setID(facetTransformer.transform(source.getIdFacet()));
        businessObject.setSummary(facetTransformer.transform(source.getSummaryFacet()));
        businessObject.setDetail(facetTransformer.transform(source.getDetailFacet()));

        for (TLContextualFacet customFacet : source.getCustomFacets()) {
            businessObject.getCustom().add(facetContextualTransformer.transform(customFacet));
        }
        for (TLContextualFacet queryFacet : source.getQueryFacets()) {
            businessObject.getQuery().add(facetContextualTransformer.transform(queryFacet));
        }

        if (source.getExtension() != null) {
            ObjectTransformer<TLExtension, Extension, SymbolResolverTransformerContext> extensionTransformer = getTransformerFactory()
                    .getTransformer(TLExtension.class, Extension.class);

            businessObject.setExtension(extensionTransformer.transform(source.getExtension()));
        }

        return businessObject;
    }

}
