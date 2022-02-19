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

package org.opentravel.reposervice.util;

import org.opentravel.repocommon.index.EntitySearchResult;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helper class that assists with the rendering of <code>TLDocumentation</code> entries for OTM <code>NamedEntity</code>
 * objects.
 * 
 * @author S. Livezey
 */
public class DocumentationHelper {

    private static final Pattern urlPattern =
        Pattern.compile( "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]" );
    private String description;
    private List<String> implementerDocs;
    private List<String> deprecationDocs;
    private List<String> referenceDocs;
    private List<String> moreInfoDocs;
    private List<String> otherDocs;

    /**
     * Constructor that initializes the documentation to be displayed for a named entity.
     * 
     * @param indexEntity named entity entry retrieved from the search index
     */
    public DocumentationHelper(EntitySearchResult indexEntity) {
        NamedEntity entity = indexEntity.getItemContent();

        if (entity instanceof TLDocumentationOwner) {
            TLDocumentation modelDoc = ((TLDocumentationOwner) entity).getDocumentation();

            if (modelDoc != null) {
                implementerDocs = getDocumentationValues( modelDoc.getImplementers() );
                deprecationDocs = getDocumentationValues( modelDoc.getDeprecations() );
                referenceDocs = getDocumentationValues( modelDoc.getReferences() );
                moreInfoDocs = getDocumentationValues( modelDoc.getMoreInfos() );
                otherDocs = getDocumentationValues( modelDoc.getOtherDocs() );

            } else {
                implementerDocs = deprecationDocs = referenceDocs = moreInfoDocs = otherDocs = Collections.emptyList();
            }
        }
        this.description = PageUtils.trimString( indexEntity.getItemDescription() );
    }

    /**
     * Returns true if the <code>NamedEntity</code> contains any documentation.
     * 
     * @return boolean
     */
    public boolean hasDocumentation() {
        return hasDescription() || hasAdditionalDocs();
    }

    /**
     * Returns true if the <code>NamedEntity</code> contains a DESCRIPTION.
     * 
     * @return boolean
     */
    public boolean hasDescription() {
        return (description != null) && (description.length() > 0);
    }

    /**
     * Returns true if the <code>NamedEntity</code> contains any documentation other than the DESCRIPTION.
     * 
     * @return boolean
     */
    public boolean hasAdditionalDocs() {
        return !implementerDocs.isEmpty() || !deprecationDocs.isEmpty() || !referenceDocs.isEmpty()
            || !moreInfoDocs.isEmpty() || !otherDocs.isEmpty();
    }

    /**
     * Returns the DESCRIPTION for the <code>NamedEntity</code>.
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the DESCRIPTION string for the given OTM entity, or null if the entity does not declare a documentation
     * value.
     * 
     * @param entity the OTM model entity
     * @return String
     */
    public String getDescription(Object entity) {
        String desc = null;
    
        if (entity instanceof TLDocumentationOwner) {
            TLDocumentation doc = ((TLDocumentationOwner) entity).getDocumentation();
    
            if (doc != null) {
                desc = PageUtils.trimString( doc.getDescription() );
            }
        }
        return desc;
    }

    /**
     * Returns the implementer documentation for the <code>NamedEntity</code>.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getImplementerDocs() {
        return implementerDocs;
    }

    /**
     * Returns the deprecation documentation for the <code>NamedEntity</code>.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getDeprecationDocs() {
        return deprecationDocs;
    }

    /**
     * Returns the reference documentation for the <code>NamedEntity</code>.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getReferenceDocs() {
        return referenceDocs;
    }

    /**
     * Returns the more-info documentation for the <code>NamedEntity</code>.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getMoreInfoDocs() {
        return moreInfoDocs;
    }

    /**
     * Returns the other-docs documentation for the <code>NamedEntity</code>.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getOtherDocs() {
        return otherDocs;
    }

    /**
     * Returns true if the given documentation value is a well-formed URL.
     * 
     * @param docValue the documentation value to analyze
     * @return boolean
     */
    public boolean isUrl(String docValue) {
        return urlPattern.matcher( docValue ).matches();
    }

    /**
     * Returns the list of non-empty documentation string values from the given list.
     * 
     * @param docItems the list of OTM model documentation items
     * @return List&lt;String&gt;
     */
    private List<String> getDocumentationValues(List<? extends TLDocumentationItem> docItems) {
        List<String> docValues = new ArrayList<>();

        if (docItems != null) {
            for (TLDocumentationItem docItem : docItems) {
                String docValue = PageUtils.trimString( docItem.getText() );

                if (docValue != null) {
                    docValues.add( docValue );
                }
            }
        }
        return docValues;
    }

}
