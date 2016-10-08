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

import org.opentravel.ns.ota2.librarymodel_v01_06.Attribute;
import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_06.Example;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLAttribute</code> type to the
 * <code>Attribute</code> type.
 * 
 * @author S. Livezey
 */
public class TLAttributeTransformer extends
        BaseTransformer<TLAttribute, Attribute, SymbolResolverTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public Attribute transform(TLAttribute source) {
        ObjectTransformer<TLEquivalent, Equivalent, SymbolResolverTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(TLEquivalent.class, Equivalent.class);
        ObjectTransformer<TLExample, Example, SymbolResolverTransformerContext> exTransformer = getTransformerFactory()
                .getTransformer(TLExample.class, Example.class);
        TLPropertyType attributeType = source.getType();
        Attribute attribute = new Attribute();

        attribute.setName(trimString(source.getName(), false));
        attribute.setMandatory(source.isMandatory() ? Boolean.TRUE : null);
        attribute.setReference(source.isReference() ? Boolean.TRUE : null);
        attribute.setReferenceRepeat(TLPropertyTransformer.convertRepeatValue(source.getReferenceRepeat()));

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            attribute.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }

        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
            attribute.getEquivalent().add(equivTransformer.transform(sourceEquiv));
        }

        for (TLExample sourceEx : source.getExamples()) {
            attribute.getExample().add(exTransformer.transform(sourceEx));
        }

        if (source.getType() != null) {
            attribute.setType(context.getSymbolResolver().buildEntityName(
                    attributeType.getNamespace(), attributeType.getLocalName()));
        }
        if (attribute.getType() == null) {
            attribute.setType(trimString(source.getTypeName(), false));
        }
        return attribute;
    }

}
