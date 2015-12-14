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
import org.opentravel.ns.ota2.librarymodel_v01_05.ValueWithAttributes;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLValueWithAttributes</code> type to the
 * <code>ValueWithAttributes</code> type.
 * 
 * @author S. Livezey
 */
public class TLValueWithAttributesTransformer extends
        TLComplexTypeTransformer<TLValueWithAttributes, ValueWithAttributes> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public ValueWithAttributes transform(TLValueWithAttributes source) {
        ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                .getTransformer(TLDocumentation.class, Documentation.class);
        ObjectTransformer<TLEquivalent, Equivalent, SymbolResolverTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(TLEquivalent.class, Equivalent.class);
        ObjectTransformer<TLExample, Example, SymbolResolverTransformerContext> exTransformer = getTransformerFactory()
                .getTransformer(TLExample.class, Example.class);
        NamedEntity parentType = source.getParentType();
        ValueWithAttributes simpleType = new ValueWithAttributes();

        simpleType.setName(trimString(source.getName(), false));
        simpleType.getAttribute().addAll(transformAttributes(source.getAttributes()));
        simpleType.getIndicator().addAll(transformIndicators(source.getIndicators()));

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            simpleType.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        if ((source.getValueDocumentation() != null) && !source.getValueDocumentation().isEmpty()) {
            simpleType.setValueDocumentation(docTransformer.transform(source
                    .getValueDocumentation()));
        }

        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
            simpleType.getEquivalent().add(equivTransformer.transform(sourceEquiv));
        }

        for (TLExample sourceEx : source.getExamples()) {
            simpleType.getExample().add(exTransformer.transform(sourceEx));
        }

        if (parentType != null) {
            simpleType.setType(context.getSymbolResolver().buildEntityName(
                    parentType.getNamespace(), parentType.getLocalName()));
        } else {
            simpleType.setType(trimString(source.getParentTypeName(), false));
        }
        return simpleType;
    }

}
