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
import org.opentravel.ns.ota2.librarymodel_v01_05.ParamGroup;
import org.opentravel.ns.ota2.librarymodel_v01_05.Parameter;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLParamGroup</code> type to the <code>ParamGroup</code> type.
 *
 * @author S. Livezey
 */
public class TLParamGroupTransformer extends TLComplexTypeTransformer<TLParamGroup,ParamGroup> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public ParamGroup transform(TLParamGroup source) {
        ObjectTransformer<TLParameter,Parameter,SymbolResolverTransformerContext> paramTransformer =
            getTransformerFactory().getTransformer( TLParameter.class, Parameter.class );
        TLFacet sourceFacetRef = source.getFacetRef();
        ParamGroup paramGroup = new ParamGroup();

        paramGroup.setName( trimString( source.getName(), false ) );
        paramGroup.setIdGroup( source.isIdGroup() );

        if (sourceFacetRef != null) {
            paramGroup.setFacetName( context.getSymbolResolver().buildEntityName( sourceFacetRef.getNamespace(),
                sourceFacetRef.getLocalName() ) );
        }
        if (paramGroup.getFacetName() == null) {
            paramGroup.setFacetName( trimString( source.getFacetRefName(), false ) );
        }

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( TLDocumentation.class, Documentation.class );

            paramGroup.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        for (TLParameter sourceParam : source.getParameters()) {
            paramGroup.getParameter().add( paramTransformer.transform( sourceParam ) );
        }

        return paramGroup;
    }

}
