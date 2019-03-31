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

package org.opentravel.schemacompiler.transform.jaxb16_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_06.AdditionalDoc;
import org.opentravel.ns.ota2.librarymodel_v01_06.Description;
import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the transformation of objects from the <code>Documentation</code> type to the <code>TLDocumentation</code>
 * type.
 * 
 * @author S. Livezey
 */
public class DocumentationTransformer extends BaseTransformer<Documentation,TLDocumentation,DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLDocumentation transform(Documentation source) {
        TLDocumentation target = new TLDocumentation();

        if (source.getDescription() != null) {
            target.setDescription( trimString( source.getDescription().getValue() ) );
        }
        for (String str : trimDescriptionStrings( source.getDeprecated() )) {
            target.addDeprecation( newDocumentationItem( str ) );
        }
        for (String str : trimDescriptionStrings( source.getImplementer() )) {
            target.addImplementer( newDocumentationItem( str ) );
        }
        for (String str : trimDescriptionStrings( source.getDeveloper() )) {
            // Deprecated, but still supported during loads
            target.addImplementer( newDocumentationItem( str ) );
        }
        for (String str : source.getReference()) {
            if ((str = trimString( str )) != null) {
                target.addReference( newDocumentationItem( str ) );
            }
        }
        for (String str : source.getMoreInfo()) {
            if ((str = trimString( str )) != null) {
                target.addMoreInfo( newDocumentationItem( str ) );
            }
        }
        for (AdditionalDoc otherDoc : source.getOtherDoc()) {
            if (otherDoc != null) {
                TLAdditionalDocumentationItem targetOtherDoc = new TLAdditionalDocumentationItem();

                targetOtherDoc.setContext( otherDoc.getContext() );
                targetOtherDoc.setText( otherDoc.getValue() );
                target.addOtherDoc( targetOtherDoc );
            }
        }
        return target;
    }

    /**
     * Compiles a list of all description strings from the collection and returns the result after normal
     * 'trimStrings()' processing.
     * 
     * @param descriptions the list of descriptions to process
     * @return List&lt;String&gt;
     */
    private List<String> trimDescriptionStrings(List<Description> descriptions) {
        List<String> result = new ArrayList<>();

        if (descriptions != null) {
            for (Description desc : descriptions) {
                result.add( desc.getValue() );
            }
        }
        return trimStrings( result );
    }

    /**
     * Returns a new <code>TLDocumentationItem</code> that wraps the documentation text provided.
     * 
     * @param text the text of the documentation item
     * @return TLDocumentationItem
     */
    private TLDocumentationItem newDocumentationItem(String text) {
        TLDocumentationItem docItem = new TLDocumentationItem();

        docItem.setText( text );
        return docItem;
    }

}
