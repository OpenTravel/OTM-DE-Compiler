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

package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.ServiceWriter;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric.Bronson
 *
 */
public class ServiceDocumentationBuilder extends NamedEntityDocumentationBuilder<TLService> {

    private List<OperationDocumentationBuilder> operations = new ArrayList<>();

    private String endpointURL;

    /**
     * @param t the service for which to create a builder
     */
    public ServiceDocumentationBuilder(TLService t) {
        super( t );
        for (TLOperation operation : t.getOperations()) {
            OperationDocumentationBuilder operationBuilder = new OperationDocumentationBuilder( operation );
            operations.add( operationBuilder );
        }
    }


    public List<OperationDocumentationBuilder> getOperations() {
        return operations;
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    @Override
    public DocumentationBuilderType getDocType() {
        return DocumentationBuilderType.SERVICE;
    }

    @Override
    public void build() throws CodeGenerationException {
        try {
            ServiceWriter writer = new ServiceWriter( this, prev, next );
            Content contentTree = writer.getHeader();
            Content classContentTree = writer.getContentHeader();
            Content tree = writer.getMemberTree( classContentTree );

            Content classInfoTree = writer.getMemberInfoItemTree();
            writer.addDocumentationInfo( classInfoTree );
            tree.addContent( classInfoTree );

            classInfoTree = writer.getMemberInfoItemTree();
            writer.addOperationInfo( classInfoTree );
            tree.addContent( classInfoTree );

            classContentTree.addContent( tree );
            contentTree.addContent( classContentTree );
            writer.addFooter( contentTree );
            writer.printDocument( contentTree );
            writer.close();
            for (OperationDocumentationBuilder opBuilder : operations) {
                opBuilder.build();
            }

        } catch (IOException e) {
            throw new CodeGenerationException( "Error creating doclet writer", e );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals( obj );
    }

}
