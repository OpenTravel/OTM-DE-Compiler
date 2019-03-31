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

package org.opentravel.schemacompiler.transform.jaxb15_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_05.Attribute;
import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_05.Example;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Attribute</code> type to the <code>TLAttribute</code> type.
 * 
 * @author S. Livezey
 */
public class AttributeTransformer extends BaseTransformer<Attribute,TLAttribute,DefaultTransformerContext> {

    @Override
    public TLAttribute transform(Attribute source) {
        ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
            getTransformerFactory().getTransformer( Equivalent.class, TLEquivalent.class );
        ObjectTransformer<Example,TLExample,DefaultTransformerContext> exampleTransformer =
            getTransformerFactory().getTransformer( Example.class, TLExample.class );
        String attributeTypeName = source.getType();
        final TLAttribute attribute = new TLAttribute();

        attribute.setName( trimString( source.getName() ) );
        attribute.setMandatory( (source.isMandatory() != null) && source.isMandatory() );
        attribute.setTypeName( trimString( attributeTypeName ) );

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( Documentation.class, TLDocumentation.class );

            attribute.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
            attribute.addEquivalent( equivTransformer.transform( sourceEquiv ) );
        }

        for (Example sourceExample : source.getExample()) {
            attribute.addExample( exampleTransformer.transform( sourceExample ) );
        }

        return attribute;
    }

}
