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

import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_05.Example;
import org.opentravel.ns.ota2.librarymodel_v01_05.SimpleFacet;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLSimpleFacet</code> type to the <code>SimpleFacet</code> type.
 * 
 * @author S. Livezey
 */
public class TLSimpleFacetTransformer
    extends BaseTransformer<TLSimpleFacet,SimpleFacet,SymbolResolverTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public SimpleFacet transform(TLSimpleFacet source) {
        ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
            getTransformerFactory().getTransformer( TLEquivalent.class, Equivalent.class );
        ObjectTransformer<TLExample,Example,SymbolResolverTransformerContext> exTransformer =
            getTransformerFactory().getTransformer( TLExample.class, Example.class );
        NamedEntity simpleType = source.getSimpleType();
        SimpleFacet facet = new SimpleFacet();

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( TLDocumentation.class, Documentation.class );

            facet.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
            facet.getEquivalent().add( equivTransformer.transform( sourceEquiv ) );
        }

        for (TLExample sourceEx : source.getExamples()) {
            facet.getExample().add( exTransformer.transform( sourceEx ) );
        }

        if (simpleType != null) {
            facet.setType(
                context.getSymbolResolver().buildEntityName( simpleType.getNamespace(), simpleType.getLocalName() ) );
        }
        if (facet.getType() == null) {
            facet.setType( trimString( source.getSimpleTypeName(), false ) );
        }
        return facet;
    }

}
