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
import org.opentravel.ns.ota2.librarymodel_v01_06.DocumentationPatch;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationPatch;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>DocumentationPatch</code> type to the
 * <code>TLDocumentationPatch</code> type.
 *
 * @author S. Livezey
 */
public class DocumentationPatchTransformer
    extends BaseTransformer<DocumentationPatch,TLDocumentationPatch,DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLDocumentationPatch transform(DocumentationPatch source) {
        TLDocumentationPatch target = new TLDocumentationPatch();

        target.setPatchedVersion( source.getPatchedVersion() );
        target.setDocPath( source.getDocPath() );

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( Documentation.class, TLDocumentation.class );

            target.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }
        return target;
    }

}
