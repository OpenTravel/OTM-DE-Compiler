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

package org.opentravel.schemacompiler.transform.tl2jaxb16;

import org.opentravel.ns.ota2.librarymodel_v01_06.ChoiceObject;
import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_06.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_06.Facet;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLChoiceObject</code> type to the <code>ChoiceObject</code>
 * type.
 *
 * @author S. Livezey
 */
public class TLChoiceObjectTransformer extends TLComplexTypeTransformer<TLChoiceObject,ChoiceObject> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public ChoiceObject transform(TLChoiceObject source) {
        ObjectTransformer<TLFacet,Facet,SymbolResolverTransformerContext> facetTransformer =
            getTransformerFactory().getTransformer( TLFacet.class, Facet.class );
        ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
            getTransformerFactory().getTransformer( TLEquivalent.class, Equivalent.class );
        ChoiceObject choiceObject = new ChoiceObject();

        choiceObject.setName( trimString( source.getName(), false ) );
        choiceObject.setNotExtendable( source.isNotExtendable() );
        choiceObject.getAliases().addAll( getAliasNames( source.getAliases() ) );

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( TLDocumentation.class, Documentation.class );

            choiceObject.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }
        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
            choiceObject.getEquivalent().add( equivTransformer.transform( sourceEquiv ) );
        }

        choiceObject.setShared( facetTransformer.transform( source.getSharedFacet() ) );

        if (source.getExtension() != null) {
            ObjectTransformer<TLExtension,Extension,SymbolResolverTransformerContext> extensionTransformer =
                getTransformerFactory().getTransformer( TLExtension.class, Extension.class );

            choiceObject.setExtension( extensionTransformer.transform( source.getExtension() ) );
        }

        return choiceObject;
    }

}
