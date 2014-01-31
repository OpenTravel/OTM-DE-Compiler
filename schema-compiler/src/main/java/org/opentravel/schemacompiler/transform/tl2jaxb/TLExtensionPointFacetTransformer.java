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
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.ExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLExtensionPointFacet</code> type to the
 * <code>ExtensionPointFacet</code> type.
 * 
 * @author S. Livezey
 */
public class TLExtensionPointFacetTransformer extends
        TLComplexTypeTransformer<TLExtensionPointFacet, ExtensionPointFacet> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public ExtensionPointFacet transform(TLExtensionPointFacet source) {
        ExtensionPointFacet facet = new ExtensionPointFacet();

        if (source.getExtension() != null) {
            ObjectTransformer<TLExtension, Extension, SymbolResolverTransformerContext> extensionTransformer = getTransformerFactory()
                    .getTransformer(TLExtension.class, Extension.class);

            facet.setExtension(extensionTransformer.transform(source.getExtension()));
        }

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            facet.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }

        facet.getAttribute().addAll(transformAttributes(source.getAttributes()));
        facet.getElement().addAll(transformElements(source.getElements()));
        facet.getIndicator().addAll(transformIndicators(source.getIndicators()));

        return facet;
    }

}
