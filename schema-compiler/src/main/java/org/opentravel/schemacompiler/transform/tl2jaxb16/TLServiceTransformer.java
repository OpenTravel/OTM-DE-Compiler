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

import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_06.Operation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Service;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLService</code> type to the <code>Service</code> type.
 * 
 * @author S. Livezey
 */
public class TLServiceTransformer extends BaseTransformer<TLService,Service,SymbolResolverTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public Service transform(TLService source) {
        ObjectTransformer<TLOperation,Operation,SymbolResolverTransformerContext> operationTransformer =
            getTransformerFactory().getTransformer( TLOperation.class, Operation.class );
        ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
            getTransformerFactory().getTransformer( TLEquivalent.class, Equivalent.class );
        Service service = new Service();

        service.setName( trimString( source.getName(), false ) );

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( TLDocumentation.class, Documentation.class );

            service.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
            service.getEquivalent().add( equivTransformer.transform( sourceEquiv ) );
        }

        for (TLOperation modelOperation : source.getOperations()) {
            service.getOperation().add( operationTransformer.transform( modelOperation ) );
        }
        return service;
    }

}
