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

package org.opentravel.schemacompiler.transform.jaxb15_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_05.BusinessObject;
import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_05.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_05.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_05.FacetContextual;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>BusinessObject</code> type to the <code>TLBusinessObject</code>
 * type.
 * 
 * @author S. Livezey
 */
public class BusinessObjectTransformer extends ComplexTypeTransformer<BusinessObject,TLBusinessObject> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLBusinessObject transform(BusinessObject source) {
        ObjectTransformer<Facet,TLFacet,DefaultTransformerContext> facetTransformer =
            getTransformerFactory().getTransformer( Facet.class, TLFacet.class );
        ObjectTransformer<FacetContextual,TLContextualFacet,DefaultTransformerContext> facetContextualTransformer =
            getTransformerFactory().getTransformer( FacetContextual.class, TLContextualFacet.class );
        ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
            getTransformerFactory().getTransformer( Equivalent.class, TLEquivalent.class );
        TLBusinessObject businessObject = new TLBusinessObject();

        businessObject.setName( trimString( source.getName() ) );
        businessObject.setNotExtendable( (source.isNotExtendable() != null) && source.isNotExtendable() );

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( Documentation.class, TLDocumentation.class );

            businessObject.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        if (source.getExtension() != null) {
            ObjectTransformer<Extension,TLExtension,DefaultTransformerContext> extensionTransformer =
                getTransformerFactory().getTransformer( Extension.class, TLExtension.class );

            businessObject.setExtension( extensionTransformer.transform( source.getExtension() ) );
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
            businessObject.addEquivalent( equivTransformer.transform( sourceEquiv ) );
        }

        for (String aliasName : trimStrings( source.getAliases() )) {
            TLAlias alias = new TLAlias();

            alias.setName( aliasName );
            businessObject.addAlias( alias );
        }

        if (source.getID() != null) {
            businessObject.setIdFacet( facetTransformer.transform( source.getID() ) );
        }
        if (source.getSummary() != null) {
            businessObject.setSummaryFacet( facetTransformer.transform( source.getSummary() ) );
        }
        if (source.getDetail() != null) {
            businessObject.setDetailFacet( facetTransformer.transform( source.getDetail() ) );
        }

        if (source.getCustom() != null) {
            for (FacetContextual sourceFacet : source.getCustom()) {
                businessObject.addCustomFacet( facetContextualTransformer.transform( sourceFacet ) );
            }
        }
        if (source.getQuery() != null) {
            for (FacetContextual sourceFacet : source.getQuery()) {
                businessObject.addQueryFacet( facetContextualTransformer.transform( sourceFacet ) );
            }
        }

        return businessObject;
    }

}
