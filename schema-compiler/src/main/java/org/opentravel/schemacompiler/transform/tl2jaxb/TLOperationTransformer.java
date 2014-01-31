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

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_04.Operation;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLOperation</code> type to the
 * <code>Operation</code> type.
 * 
 * @author S. Livezey
 */
public class TLOperationTransformer extends
        BaseTransformer<TLOperation, Operation, SymbolResolverTransformerContext> {

    @Override
    public Operation transform(TLOperation source) {
        ObjectTransformer<TLFacet, Facet, SymbolResolverTransformerContext> facetTransformer = getTransformerFactory()
                .getTransformer(TLFacet.class, Facet.class);
        ObjectTransformer<TLEquivalent, Equivalent, SymbolResolverTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(TLEquivalent.class, Equivalent.class);
        Operation operation = new Operation();

        operation.setName(trimString(source.getName(), false));
        operation.setNotExtendable(source.isNotExtendable());

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            operation.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
            operation.getEquivalent().add(equivTransformer.transform(sourceEquiv));
        }

        operation.setRequest(facetTransformer.transform(source.getRequest()));
        operation.setResponse(facetTransformer.transform(source.getResponse()));
        operation.setNotification(facetTransformer.transform(source.getNotification()));

        if (source.getExtension() != null) {
            ObjectTransformer<TLExtension, Extension, SymbolResolverTransformerContext> extensionTransformer = getTransformerFactory()
                    .getTransformer(TLExtension.class, Extension.class);

            operation.setExtension(extensionTransformer.transform(source.getExtension()));
        }

        return operation;
    }

}
