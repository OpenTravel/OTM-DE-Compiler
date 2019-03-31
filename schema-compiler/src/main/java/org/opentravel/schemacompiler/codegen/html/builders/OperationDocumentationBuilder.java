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
import org.opentravel.schemacompiler.codegen.html.writers.OperationWriter;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;

import java.io.IOException;

/**
 * @author Eric.Bronson
 *
 */
public class OperationDocumentationBuilder extends FacetOwnerDocumentationBuilder<TLOperation> {

    /**
     * @param t the operation for which to create a builder
     */
    public OperationDocumentationBuilder(TLOperation t) {
        super( t );
        TLExtension tle = t.getExtension();
        if (tle != null) {
            superType = new OperationDocumentationBuilder( (TLOperation) tle.getExtendsEntity() );
        }
    }

    @Override
    public void build() throws CodeGenerationException {
        try {
            OperationWriter writer = new OperationWriter( this, prev, next );
            Content contentTree = writer.getHeader();
            Content classContentTree = writer.getContentHeader();
            Content tree = writer.getMemberTree( classContentTree );

            Content classInfoTree = writer.getMemberInfoItemTree();
            writer.addDocumentationInfo( classInfoTree );
            tree.addContent( classInfoTree );

            classInfoTree = writer.getMemberInfoItemTree();
            writer.addFacetInfo( classInfoTree );
            tree.addContent( classInfoTree );

            classContentTree.addContent( tree );
            contentTree.addContent( classContentTree );
            writer.addFooter( contentTree );
            writer.printDocument( contentTree );
            writer.close();
            super.build();

        } catch (IOException e) {
            throw new CodeGenerationException( "Error creating doclet writer", e );
        }
    }

    @Override
    protected void initializeFacets(TLOperation t) {
        for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType( element, TLFacetType.RESPONSE )) {
            if (shouldAddFacet( facet )) {
                FacetDocumentationBuilder facetBuilder = new FacetDocumentationBuilder( facet );
                facets.add( facetBuilder );
                facetBuilder.setOwner( this );
            }
        }

        for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType( element, TLFacetType.REQUEST )) {
            if (shouldAddFacet( facet )) {
                FacetDocumentationBuilder facetBuilder = new FacetDocumentationBuilder( facet );
                facets.add( facetBuilder );
                facetBuilder.setOwner( this );
            }
        }

        for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType( element, TLFacetType.NOTIFICATION )) {
            if (shouldAddFacet( facet )) {
                FacetDocumentationBuilder facetBuilder = new FacetDocumentationBuilder( facet );
                facets.add( facetBuilder );
                facetBuilder.setOwner( this );
            }
        }
    }

    @Override
    public DocumentationBuilderType getDocType() {
        return DocumentationBuilderType.OPERATION;
    }

}
