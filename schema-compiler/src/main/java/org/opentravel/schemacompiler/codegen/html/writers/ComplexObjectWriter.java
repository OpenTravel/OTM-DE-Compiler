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

package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.builders.ComplexTypeDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.writers.info.AliasInfoWriter;

import java.io.IOException;
import java.util.List;

/**
 * @author Eric.Bronson
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ComplexObjectWriter<T extends ComplexTypeDocumentationBuilder<?>> extends FacetOwnerWriterImpl<T>
    implements AliasOwnerWriter {



    public ComplexObjectWriter(T member, DocumentationBuilder prev, DocumentationBuilder next) throws IOException {
        super( member, prev, next );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemacompiler.codegen.html.writers.AliasOwnerWriter#addAliasInfo(org.opentravel.schemacompiler.
     * codegen.html.Content)
     */
    @Override
    public void addAliasInfo(Content aliasTree) {
        List<String> aliasList = member.getAliases();

        if ((aliasList != null) && !aliasList.isEmpty()) {
            AliasInfoWriter aliasWriter = new AliasInfoWriter( this, member );
            aliasWriter.addInfo( aliasTree );
        }
    }

}
