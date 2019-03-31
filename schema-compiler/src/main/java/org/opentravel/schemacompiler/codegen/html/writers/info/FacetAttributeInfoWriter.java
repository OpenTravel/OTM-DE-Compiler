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

package org.opentravel.schemacompiler.codegen.html.writers.info;

import org.opentravel.schemacompiler.codegen.html.builders.FacetDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class FacetAttributeInfoWriter extends AbstractAttributeInfoWriter<FacetDocumentationBuilder> {

    /**
     * @param writer the writer for which to create an info-writer
     * @param owner the owner of the new info-writer
     */
    public FacetAttributeInfoWriter(SubWriterHolderWriter writer, FacetDocumentationBuilder owner) {
        super( writer, owner );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInheritedInfoWriter#getParent(org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder)
     */
    @Override
    protected FacetDocumentationBuilder getParent(FacetDocumentationBuilder classDoc) {
        return (FacetDocumentationBuilder) classDoc.getSuperType();
    }


}
