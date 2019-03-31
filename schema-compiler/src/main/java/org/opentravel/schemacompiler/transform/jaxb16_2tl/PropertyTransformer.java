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

import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_06.Example;
import org.opentravel.ns.ota2.librarymodel_v01_06.Property;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Property</code> type to the <code>TLProperty</code> type.
 * 
 * @author S. Livezey
 */
public class PropertyTransformer extends BaseTransformer<Property,TLProperty,DefaultTransformerContext> {

    @Override
    public TLProperty transform(Property source) {
        ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
            getTransformerFactory().getTransformer( Equivalent.class, TLEquivalent.class );
        ObjectTransformer<Example,TLExample,DefaultTransformerContext> exampleTransformer =
            getTransformerFactory().getTransformer( Example.class, TLExample.class );
        final TLProperty property = new TLProperty();
        String propertyTypeName = source.getType();

        property.setName( trimString( source.getName() ) );
        property.setRepeat( convertRepeatValue( trimString( source.getRepeat() ) ) );
        property.setMandatory( (source.isMandatory() != null) && source.isMandatory() );
        property.setReference( (source.isReference() != null) && source.isReference() );
        property.setTypeName( trimString( propertyTypeName ) );

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( Documentation.class, TLDocumentation.class );

            property.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
            property.addEquivalent( equivTransformer.transform( sourceEquiv ) );
        }

        for (Example sourceExample : source.getExample()) {
            property.addExample( exampleTransformer.transform( sourceExample ) );
        }

        return property;
    }

    /**
     * If the string represents an integer value, that value will be returned. A string value of "unlimited" will be
     * result in a -1 return value. Any other non-numeric strings will result in a zero return value.
     * 
     * @param repeatStr the repeat string value to convert
     * @return int
     */
    protected static int convertRepeatValue(String repeatStr) {
        int result = 0;

        if (repeatStr != null) {
            if (repeatStr.equalsIgnoreCase( UNLIMITED_TOKEN )) {
                result = -1;
            } else {
                try {
                    result = Integer.parseInt( repeatStr );

                } catch (NumberFormatException e) {
                    // Ignore - method will return zero
                }
            }
        }
        return result;
    }

}
