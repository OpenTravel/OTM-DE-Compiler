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

import javax.xml.bind.JAXBElement;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.NoFixedFacet;

/**
 * Base class for enumeration transformers that provides common methods and functions.
 * 
 * @param <S>
 *            the source type of the object transformation
 * @param <T>
 *            the target type of the object transformation
 * @author S. Livezey
 */
public abstract class TLBaseEnumerationCodegenTransformer<S, T> extends
        AbstractXsdTransformer<S, T> {

    public static final String OPEN_ENUM_VALUE = "Other_";

    /**
     * Constructs an XML schema representation of the given meta-model enumeration value.
     * 
     * @param modelEnum
     *            the enumeration value from the compiler meta-model
     * @return JAXBElement<NoFixedFacet>
     */
    protected JAXBElement<NoFixedFacet> createEnumValue(TLEnumValue modelEnum) {
        NoFixedFacet facet = new NoFixedFacet();

        facet.setValue(modelEnum.getLiteral());

        if (modelEnum.getDocumentation() != null) {
            ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(modelEnum.getDocumentation(), Annotation.class);

            facet.setAnnotation(docTransformer.transform(modelEnum.getDocumentation()));
        }
        return jaxbObjectFactory.createEnumeration(facet);
    }

}
