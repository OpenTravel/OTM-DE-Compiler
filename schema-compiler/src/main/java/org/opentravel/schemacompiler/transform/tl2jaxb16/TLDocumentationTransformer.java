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

import org.opentravel.ns.ota2.librarymodel_v01_06.AdditionalDoc;
import org.opentravel.ns.ota2.librarymodel_v01_06.Description;
import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the transformation of objects from the <code>TLDocumentation</code> type to the <code>Documentation</code>
 * type.
 * 
 * @author S. Livezey
 */
public class TLDocumentationTransformer
    extends BaseTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public Documentation transform(TLDocumentation source) {
        Description jaxbDescription = new Description();
        Documentation target = new Documentation();

        if (source.getDescription() != null) {
            jaxbDescription.setValue( trimString( source.getDescription(), false ) );
        } else {
            jaxbDescription.setValue( "" );
        }
        target.setDescription( jaxbDescription );

        target.getDeprecated().addAll( buildDescriptions( source.getDeprecations() ) );
        target.getImplementer().addAll( buildDescriptions( source.getImplementers() ) );
        target.getReference().addAll( buildTexts( source.getReferences() ) );
        target.getMoreInfo().addAll( buildTexts( source.getMoreInfos() ) );

        for (TLAdditionalDocumentationItem sourceOtherDoc : source.getOtherDocs()) {
            if (sourceOtherDoc != null) {
                AdditionalDoc otherDoc = new AdditionalDoc();

                otherDoc.setContext( trimString( sourceOtherDoc.getContext(), false ) );
                otherDoc.setValue( trimString( sourceOtherDoc.getText(), false ) );
                target.getOtherDoc().add( otherDoc );
            }
        }
        return target;
    }

    /**
     * Constructs a list of JAXB descriptions using the list of <code>TLDocumentationItems</code> provided.
     * 
     * @param items the list of documentation items to convert
     * @return List&lt;Description&gt;
     */
    private List<Description> buildDescriptions(List<TLDocumentationItem> items) {
        List<Description> result = new ArrayList<>();

        if (items != null) {
            for (TLDocumentationItem item : items) {
                String text = (item == null) ? null : trimString( item.getText() );
                Description jaxbDescription = new Description();

                jaxbDescription.setValue( (text == null) ? "" : text );
                result.add( jaxbDescription );
            }
        }
        return result;
    }

    /**
     * Constructs a list of strings using the list of <code>TLDocumentationItems</code> provided.
     * 
     * @param items the list of documentation items to convert
     * @return List&lt;String&gt;
     */
    private List<String> buildTexts(List<TLDocumentationItem> items) {
        List<String> result = new ArrayList<>();

        if (items != null) {
            for (TLDocumentationItem item : items) {
                String text = (item == null) ? null : trimString( item.getText() );

                result.add( (text == null) ? "" : text );
            }
        }
        return result;
    }

}
