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

import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_05.Example;
import org.opentravel.ns.ota2.librarymodel_v01_05.ParamLocation;
import org.opentravel.ns.ota2.librarymodel_v01_05.Parameter;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>Parameter</code> type to the <code>TLParameter</code> type.
 *
 * @author S. Livezey
 */
public class ParameterTransformer extends ComplexTypeTransformer<Parameter,TLParameter> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLParameter transform(Parameter source) {
        ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
            getTransformerFactory().getTransformer( Equivalent.class, TLEquivalent.class );
        ObjectTransformer<Example,TLExample,DefaultTransformerContext> exampleTransformer =
            getTransformerFactory().getTransformer( Example.class, TLExample.class );
        TLParameter param = new TLParameter();

        param.setFieldRefName( trimString( source.getFieldName() ) );

        if (source.getLocation() != null) {
            param.setLocation( transformLocation( source.getLocation() ) );
        }

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( Documentation.class, TLDocumentation.class );

            param.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
            param.addEquivalent( equivTransformer.transform( sourceEquiv ) );
        }

        for (Example sourceExample : source.getExample()) {
            param.addExample( exampleTransformer.transform( sourceExample ) );
        }

        return param;
    }

    /**
     * Transforms the parameter location value.
     * 
     * @param sourceLocation the JAXB location value
     * @return TLParamLocation
     */
    private TLParamLocation transformLocation(ParamLocation sourceLocation) {
        TLParamLocation location;

        if (sourceLocation != null) {
            switch (sourceLocation) {
                case HEADER:
                    location = TLParamLocation.HEADER;
                    break;
                case PATH:
                    location = TLParamLocation.PATH;
                    break;
                case QUERY:
                    location = TLParamLocation.QUERY;
                    break;
                default:
                    location = null;
            }
        } else {
            location = null;
        }
        return location;
    }
}
