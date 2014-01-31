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
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.ns.ota2.librarymodel_v01_04.Simple;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Simple</code> type to the
 * <code>TLSimple</code> type.
 * 
 * @author S. Livezey
 */
public class SimpleTransformer extends BaseTransformer<Simple, TLSimple, DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLSimple transform(Simple source) {
        ObjectTransformer<Equivalent, TLEquivalent, DefaultTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(Equivalent.class, TLEquivalent.class);
        ObjectTransformer<Example, TLExample, DefaultTransformerContext> exampleTransformer = getTransformerFactory()
                .getTransformer(Example.class, TLExample.class);
        final TLSimple simpleType = new TLSimple();

        simpleType.setName(trimString(source.getName()));
        simpleType.setPattern(trimString(source.getPattern()));
        simpleType.setMinLength((source.getMinLength() == null) ? -1 : source.getMinLength()
                .intValue());
        simpleType.setMaxLength((source.getMaxLength() == null) ? -1 : source.getMaxLength()
                .intValue());
        simpleType.setFractionDigits((source.getFractionDigits() == null) ? -1 : source
                .getFractionDigits().intValue());
        simpleType.setTotalDigits((source.getTotalDigits() == null) ? -1 : source.getTotalDigits()
                .intValue());
        simpleType.setMinInclusive(trimString(source.getMinInclusive()));
        simpleType.setMaxInclusive(trimString(source.getMaxInclusive()));
        simpleType.setMinExclusive(trimString(source.getMinExclusive()));
        simpleType.setMaxExclusive(trimString(source.getMaxExclusive()));
        simpleType.setParentTypeName(trimString(source.getType()));
        simpleType.setListTypeInd((source.isListTypeInd() != null) && source.isListTypeInd());

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            simpleType.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
            simpleType.addEquivalent(equivTransformer.transform(sourceEquiv));
        }

        for (Example sourceExample : source.getExample()) {
            simpleType.addExample(exampleTransformer.transform(sourceExample));
        }

        return simpleType;
    }

}
