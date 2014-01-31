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
package org.opentravel.schemacompiler.codegen.wsdl;

import org.opentravel.schemacompiler.codegen.xsd.AbstractXsdTransformer;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.xmlsoap.schemas.wsdl.TDocumentation;

/**
 * Performs the translation from <code>TLDocumentation</code> objects to the JAXB nodes used to
 * produce the WSDL output.
 * 
 * @author S. Livezey
 */
public class TLDocumentationWsdlCodegenTransformer extends
        AbstractXsdTransformer<TLDocumentation, TDocumentation> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TDocumentation transform(TLDocumentation source) {
        String description = trimString(source.getDescription());
        TDocumentation documentation = null;

        if (description != null) {
            documentation = new TDocumentation();
            documentation.getContent().add(description);
        }
        return documentation;
    }

}
