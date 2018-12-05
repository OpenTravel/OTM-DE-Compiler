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

import java.math.BigInteger;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.Element;
import org.w3._2001.xmlschema.TopLevelElement;

/**
 * Performs the translation from <code>TLIndicator</code> objects to the JAXB nodes used to produce
 * the schema output.
 * 
 * @author S. Livezey
 */
public class TLIndicatorCodegenTransformer extends AbstractXsdTransformer<TLIndicator, Annotated> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public Annotated transform(TLIndicator source) {
        boolean publishAsElement = source.isPublishAsElement()
                && !(source.getOwner() instanceof TLValueWithAttributes);
        String indicatorName = source.getName();
        Annotated indicator;

        if (!indicatorName.endsWith("Ind")) {
            indicatorName += "Ind";
        }

        if (publishAsElement) {
            Element indicatorElement = new TopLevelElement();

            indicatorElement.setName(indicatorName);
            indicatorElement.setType(XsdCodegenUtils.XSD_BOOLEAN_TYPE);
            indicatorElement.setMinOccurs(BigInteger.ZERO);
            indicatorElement.setMaxOccurs(BigInteger.ONE.toString());
            indicator = indicatorElement;

        } else { // publish as attribute (default)
            Attribute indicatorAttr = new Attribute();

            indicatorAttr.setName(indicatorName);
            indicatorAttr.setType(XsdCodegenUtils.XSD_BOOLEAN_TYPE);
            indicatorAttr.setUse("optional");
            indicator = indicatorAttr;
        }

        // Generate the documentation BLOCK (if required)
        TLDocumentation sourceDoc = DocumentationFinder.getDocumentation( source );
        
        if (sourceDoc != null) {
            ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer =
            		getTransformerFactory().getTransformer(sourceDoc, Annotation.class);

            indicator.setAnnotation(docTransformer.transform(sourceDoc));
        }
        return indicator;
    }

}
