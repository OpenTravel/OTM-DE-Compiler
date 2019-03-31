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
import org.opentravel.ns.ota2.librarymodel_v01_04.ValueWithAttributes;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>ValueWithAttributes</code> type to the
 * <code>TLValueWithAttributes</code> type.
 * 
 * @author S. Livezey
 */
public class ValueWithAttributesTransformer extends ComplexTypeTransformer<ValueWithAttributes,TLValueWithAttributes> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLValueWithAttributes transform(ValueWithAttributes source) {
        ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
            getTransformerFactory().getTransformer( Documentation.class, TLDocumentation.class );
        ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
            getTransformerFactory().getTransformer( Equivalent.class, TLEquivalent.class );
        ObjectTransformer<Example,TLExample,DefaultTransformerContext> exampleTransformer =
            getTransformerFactory().getTransformer( Example.class, TLExample.class );
        final TLValueWithAttributes simpleType = new TLValueWithAttributes();

        simpleType.setName( trimString( source.getName() ) );
        simpleType.setParentTypeName( trimString( source.getType() ) );

        if (source.getDocumentation() != null) {
            simpleType.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }
        if (source.getValueDocumentation() != null) {
            simpleType.setValueDocumentation( docTransformer.transform( source.getValueDocumentation() ) );
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
            simpleType.addEquivalent( equivTransformer.transform( sourceEquiv ) );
        }

        for (Example sourceExample : source.getExample()) {
            simpleType.addExample( exampleTransformer.transform( sourceExample ) );
        }

        for (TLAttribute attribute : transformAttributes( source.getAttribute() )) {
            simpleType.addAttribute( attribute );
        }

        for (TLIndicator indicator : transformIndicators( source.getIndicator() )) {
            simpleType.addIndicator( indicator );
        }

        return simpleType;
    }

}
