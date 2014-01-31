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
package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_03.Operation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Service;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Service</code> type to the
 * <code>TLService</code> type.
 * 
 * @author S. Livezey
 */
public class ServiceTransformer extends
        BaseTransformer<Service, TLService, DefaultTransformerContext> {

    @Override
    public TLService transform(Service source) {
        ObjectTransformer<Operation, TLOperation, DefaultTransformerContext> operationTransformer = getTransformerFactory()
                .getTransformer(Operation.class, TLOperation.class);
        ObjectTransformer<Equivalent, TLEquivalent, DefaultTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(Equivalent.class, TLEquivalent.class);
        TLService service = new TLService();

        service.setName(trimString(source.getName()));

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            service.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        for (Equivalent sourceEquiv : source.getEquivalent()) {
            service.addEquivalent(equivTransformer.transform(sourceEquiv));
        }
        if (source.getOperation() != null) {
            for (Operation jaxbOperation : source.getOperation()) {
                service.addOperation(operationTransformer.transform(jaxbOperation));
            }
        }
        return service;
    }

}
