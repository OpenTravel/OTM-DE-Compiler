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
package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_03.SimpleFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>SimpleFacet</code> type to the
 * <code>TLSimpleFacet</code> type.
 * 
 * @author S. Livezey
 */
public class SimpleFacetTransformer extends
        BaseTransformer<SimpleFacet, TLSimpleFacet, SymbolResolverTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLSimpleFacet transform(SimpleFacet source) {
        ObjectTransformer<Equivalent, TLEquivalent, SymbolResolverTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(Equivalent.class, TLEquivalent.class);
        String exampleValue = trimString(source.getEx());
        final TLSimpleFacet facet = new TLSimpleFacet();

        facet.setSimpleTypeName(trimString(source.getType()));

        if (exampleValue != null) {
            TLExample example = new TLExample();

            example.setContext(LibraryTransformer.DEFAULT_CONTEXT_ID);
            example.setValue(exampleValue);
            facet.addExample(example);
        }

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            facet.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
            facet.addEquivalent(equivTransformer.transform(sourceEquiv));
        }

        return facet;
    }

}
