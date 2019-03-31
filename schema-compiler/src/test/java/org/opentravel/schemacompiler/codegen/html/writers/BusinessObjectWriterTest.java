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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.codegen.html.builders.BusinessObjectDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLDocumentation;

/**
 * @author Eric.Bronson
 *
 */
public class BusinessObjectWriterTest extends AbstractWriterTest {

    private BusinessObjectDocumentationBuilder builder;
    private BusinessObjectWriter writer;
    private TLBusinessObject bo;



    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (writer != null) {
            writer.close();
        }
    }

    @Test
    public void testItShouldAddFacetsToTheContent() throws Exception {
        bo = TestLibraryProvider.getBusinessObject( "ExampleBusinessObject" );
        builder = new BusinessObjectDocumentationBuilder( bo );
        writer = new BusinessObjectWriter( builder, null, null );
        HtmlTree div = new HtmlTree( HtmlTag.DIV );
        writer.addFacetInfo( div );
        String content = div.toString();
        assertTrue( "No facet.", content.contains( bo.getIdFacet().getLocalName() ) );
        assertTrue( "No facet.", content.contains( bo.getDetailFacet().getLocalName() ) );
        assertTrue( "No facet.", content.contains( bo.getSummaryFacet().getLocalName() ) );
        assertTrue( "No facet type.", content.contains( bo.getIdFacet().getFacetType().name() ) );
        assertTrue( "No facet.", content.contains( bo.getDetailFacet().getFacetType().name() ) );
        assertTrue( "No facet.", content.contains( bo.getSummaryFacet().getFacetType().name() ) );
        assertTrue( "Incorrect header", content.contains( "Facets" ) );
    }

    @Test
    public void testItShouldAddInheritedFacetsToTheContent() throws Exception {
        bo = TestLibraryProvider.getBusinessObject( "ExtendedBusinessObject" );
        builder = new BusinessObjectDocumentationBuilder( bo );
        writer = new BusinessObjectWriter( builder, null, null );
        HtmlTree div = new HtmlTree( HtmlTag.DIV );
        writer.addFacetInfo( div );
        String content = div.toString();
        assertTrue( "No facet.", content.contains( bo.getCustomFacet( "ExtendedCustomFacet" ).getLocalName() ) );
    }

    @Test
    public void testItShouldAddAliasesToTheContent() throws Exception {
        bo = TestLibraryProvider.getBusinessObject( "SampleBusinessObjectWithAliases" );
        builder = new BusinessObjectDocumentationBuilder( bo );
        writer = new BusinessObjectWriter( builder, null, null );
        HtmlTree div = new HtmlTree( HtmlTag.DIV );
        writer.addAliasInfo( div );
        String content = div.toString();
        assertTrue( "Incorrect header", content.contains( "Aliases" ) );
        for (TLAlias alias : bo.getAliases()) {
            assertTrue( "No alias.", content.contains( alias.getLocalName() ) );
        }
    }

    @Test
    public void testAll() throws Exception {
        bo = TestLibraryProvider.getBusinessObject( "ExampleBusinessObject" );
        builder = new BusinessObjectDocumentationBuilder( bo );
        writer = new BusinessObjectWriter( builder, null, null );
        Content contentTree = writer.getHeader();
        Content classContentTree = writer.getContentHeader();
        Content tree = writer.getMemberTree( classContentTree );
        Content classInfoTree = writer.getMemberInfoItemTree();
        writer.addDocumentationInfo( classInfoTree );
        tree.addContent( classInfoTree );

        classInfoTree = writer.getMemberInfoItemTree();
        writer.addFacetInfo( classInfoTree );
        tree.addContent( classInfoTree );

        classInfoTree = writer.getMemberInfoItemTree();
        writer.addAliasInfo( classInfoTree );
        tree.addContent( classInfoTree );
        classContentTree.addContent( tree );
        contentTree.addContent( classContentTree );
        writer.addFooter( contentTree );
        writer.printDocument( contentTree );
    }

    @Test
    public void testAllExtended() throws Exception {
        bo = TestLibraryProvider.getBusinessObject( "ExtendedBusinessObject" );
        builder = new BusinessObjectDocumentationBuilder( bo );
        writer = new BusinessObjectWriter( builder, null, null );
        Content contentTree = writer.getHeader();
        writer.addMemberInheritanceTree( contentTree );
        Content classContentTree = writer.getContentHeader();
        Content tree = writer.getMemberTree( classContentTree );

        Content classInfoTree = writer.getMemberInfoItemTree();
        writer.addDocumentationInfo( classInfoTree );
        tree.addContent( classInfoTree );

        classInfoTree = writer.getMemberInfoItemTree();
        writer.addFacetInfo( classInfoTree );
        tree.addContent( classInfoTree );

        classInfoTree = writer.getMemberInfoItemTree();
        writer.addAliasInfo( classInfoTree );
        tree.addContent( classInfoTree );

        Content desc = writer.getMemberInfoTree( tree );
        writer.addAliasInfo( desc );
        classContentTree.addContent( desc );
        contentTree.addContent( classContentTree );
        writer.addFooter( contentTree );
        writer.printDocument( contentTree );
    }

    @Test
    public void testItShouldAddDocumentation() throws Exception {
        bo = TestLibraryProvider.getBusinessObject( "ExtendedBusinessObject" );
        builder = new BusinessObjectDocumentationBuilder( bo );
        TLDocumentation doc = getTestDocumentation();
        bo.setDocumentation( doc );
        Content classInfoTree = new HtmlTree( HtmlTag.DIV );
        writer = new BusinessObjectWriter( builder, null, null );
        writer.addDocumentationInfo( classInfoTree );
        String html = classInfoTree.toString();
        String title = config.getText( "doclet.Description" );
        assertTrue( "No Description header.", html.contains( title ) );
        assertTrue( "No Description.", html.contains( doc.getDescription() ) );
        assertTrue( "No Implementers.", html.contains( doc.getImplementers().get( 0 ).getText() ) );
        assertTrue( "No Deprecations.", html.contains( doc.getDeprecations().get( 0 ).getText() ) );
        assertTrue( "No References.", html.contains( doc.getReferences().get( 0 ).getText() ) );
        assertTrue( "No More infos.", html.contains( doc.getMoreInfos().get( 0 ).getText() ) );
    }

}
