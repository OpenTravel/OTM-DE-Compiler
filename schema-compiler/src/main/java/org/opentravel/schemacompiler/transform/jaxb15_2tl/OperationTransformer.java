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
import org.opentravel.ns.ota2.librarymodel_v01_05.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_05.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_05.Operation;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Operation</code> type to the
 * <code>TLOperation</code> type.
 * 
 * @author S. Livezey
 */
public class OperationTransformer extends
        BaseTransformer<Operation, TLOperation, DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLOperation transform(Operation source) {
        ObjectTransformer<Equivalent, TLEquivalent, DefaultTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(Equivalent.class, TLEquivalent.class);
        ObjectTransformer<Facet, TLFacet, DefaultTransformerContext> facetTransformer = getTransformerFactory()
                .getTransformer(Facet.class, TLFacet.class);
        final TLOperation operation = new TLOperation();

        operation.setName(trimString(source.getName()));
        operation.setNotExtendable((source.isNotExtendable() == null) ? false : source.isNotExtendable());

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            operation.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }

        if (source.getExtension() != null) {
            ObjectTransformer<Extension, TLExtension, DefaultTransformerContext> extensionTransformer = getTransformerFactory()
                    .getTransformer(Extension.class, TLExtension.class);

            operation.setExtension(extensionTransformer.transform(source.getExtension()));
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
            operation.addEquivalent(equivTransformer.transform(sourceEquiv));
        }
        
        if (source.getRequest() != null) {
            operation.setRequest(facetTransformer.transform(source.getRequest()));
        }
        if (source.getResponse() != null) {
            operation.setResponse(facetTransformer.transform(source.getResponse()));
        }
        if (source.getNotification() != null) {
            operation.setNotification(facetTransformer.transform(source.getNotification()));
        }

        return operation;
    }

}
