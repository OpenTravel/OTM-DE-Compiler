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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Appinfo;
import org.w3._2001.xmlschema.Documentation;

/**
 * Performs the translation from <code>TLDocumentation</code> objects to the JAXB nodes used to
 * produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLDocumentationCodegenTransformer extends
        AbstractXsdTransformer<TLDocumentation, Annotation> {

    public static final String DEPRECATION_SOURCE = "Deprecation";
    public static final String DESCRIPTION_SOURCE = "Description";
    public static final String REFERENCE_SOURCE = "Reference";
    public static final String IMPLEMENTER_SOURCE = "Implementer";
    public static final String MORE_INFO_SOURCE = "MoreInfo";

    private static List<String> standardSources = Arrays.asList(new String[] { DESCRIPTION_SOURCE,
            DEPRECATION_SOURCE, REFERENCE_SOURCE, IMPLEMENTER_SOURCE, MORE_INFO_SOURCE });

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public Annotation transform(TLDocumentation source) {
        Annotation annotation = new Annotation();

        if ((source.getDescription() != null) && (source.getDescription().length() > 0)) {
            Documentation desc = new Documentation();

            desc.setSource(DESCRIPTION_SOURCE);
            desc.getContent().add( StringEscapeUtils.escapeXml10( source.getDescription() ) );
            annotation.getAppinfoOrDocumentation().add(desc);
        }
        for (TLDocumentationItem item : source.getDeprecations()) {
            annotation.getAppinfoOrDocumentation().add(newDocumentation(item, DEPRECATION_SOURCE));
        }
        for (TLDocumentationItem item : source.getReferences()) {
            annotation.getAppinfoOrDocumentation().add(newDocumentation(item, REFERENCE_SOURCE));
        }
        for (TLDocumentationItem item : source.getImplementers()) {
            annotation.getAppinfoOrDocumentation().add(newDocumentation(item, IMPLEMENTER_SOURCE));
        }
        for (TLDocumentationItem item : source.getMoreInfos()) {
            annotation.getAppinfoOrDocumentation().add(newDocumentation(item, MORE_INFO_SOURCE));
        }
        for (TLAdditionalDocumentationItem item : source.getOtherDocs()) {
            annotation.getAppinfoOrDocumentation().add(newDocumentation(item, item.getContext()));
        }
        return (annotation.getAppinfoOrDocumentation().size() == 0) ? null : annotation;
    }

    /**
     * Merges multiple schema documentation elements into a single annotation.
     * 
     * @param annotations
     *            the XML schema annotations to merge
     * @return Annotation
     */
    public static Annotation mergeDocumentation(Annotation... annotations) {
        Map<String, List<Documentation>> documentationBySource = new HashMap<String, List<Documentation>>();
        List<Documentation> otherDocumentations = new ArrayList<Documentation>();
        List<Appinfo> appInfos = new ArrayList<Appinfo>();
        Annotation result = new Annotation();

        // First, organize each of the documentation elements by type
        for (Annotation annotation : annotations) {
            if (annotation == null)
                continue;

            for (Object appInfoOrDocumentation : annotation.getAppinfoOrDocumentation()) {
                if (appInfoOrDocumentation instanceof Documentation) {
                    Documentation documentation = (Documentation) appInfoOrDocumentation;

                    if (standardSources.contains(documentation.getSource())) {
                        List<Documentation> docs = documentationBySource.get(documentation
                                .getSource());

                        if (docs == null) {
                            docs = new ArrayList<Documentation>();
                            documentationBySource.put(documentation.getSource(), docs);
                        }
                        docs.add(documentation);

                    } else {
                        otherDocumentations.add(documentation);
                    }
                } else if (appInfoOrDocumentation instanceof Appinfo) {
                    appInfos.add((Appinfo) appInfoOrDocumentation);
                }
            }
        }

        // Assemble the collated documentation elements (and app-infos) into a new annotation
        for (String source : standardSources) {
            List<Documentation> docs = documentationBySource.get(source);

            if (docs != null) {
                result.getAppinfoOrDocumentation().addAll(docs);
            }
        }
        result.getAppinfoOrDocumentation().addAll(otherDocumentations);
        result.getAppinfoOrDocumentation().addAll(appInfos);

        return result;
    }

    /**
     * Constructs a new XML schema documentation element using the values provided.
     * 
     * @param item
     *            the documentation item to convert
     * @param source
     *            the documentation source
     * @return Documentation
     */
    private Documentation newDocumentation(TLDocumentationItem item, String source) {
        Documentation doc = null;

        if ((item != null) && (item.getText() != null)) {
            doc = new Documentation();
            doc.setSource(source);
            doc.getContent().add( StringEscapeUtils.escapeXml10( item.getText() ) );
        }
        return doc;
    }

}
