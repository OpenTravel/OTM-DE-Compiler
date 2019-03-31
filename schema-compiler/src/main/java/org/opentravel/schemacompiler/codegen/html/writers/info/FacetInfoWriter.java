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

import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.LinkInfoImpl;
import org.opentravel.schemacompiler.codegen.html.builders.FacetDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.FacetOwnerDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;
import org.opentravel.schemacompiler.model.TLFacetType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric.Bronson
 *
 */
public class FacetInfoWriter
    extends AbstractInheritedInfoWriter<FacetOwnerDocumentationBuilder<?>,FacetDocumentationBuilder> {

    /**
     * @param writer the writer for which to create an info-writer
     * @param owner the owner of the new info-writer
     */
    public FacetInfoWriter(SubWriterHolderWriter writer, FacetOwnerDocumentationBuilder<?> owner) {
        super( writer, owner );
        title = writer.getResource( "doclet.Facet_Summary" );
        caption = writer.newConfiguration().getText( "doclet.Facets" );
    }

    /**
     * Build the member summary for the given members.
     *
     * @param memberTree content trees to which the documentation will be added
     */
    protected void addInfoSummary(Content memberTree) {
        Content label = getInfoLabel();
        memberTree.addContent( label );
        List<FacetDocumentationBuilder> facets = source.getFacets();
        if (!facets.isEmpty()) {
            Content tableTree = getTableTree();
            for (FacetDocumentationBuilder fdb : facets) {
                Content facetSummary = getInfo( fdb, facets.indexOf( fdb ), false );
                tableTree.addContent( facetSummary );
            }
            memberTree.addContent( tableTree );
        }
    }

    /**
     * Add the member summary for the given class.
     *
     * @param member the member being documented
     * @param counter the counter for determing style for the table row
     * @param addCollapse flag indicating whether the content will be in a collapsable display
     */
    protected Content getInfo(FacetDocumentationBuilder member, int counter, boolean addCollapse) {
        HtmlTree tdFacetName = new HtmlTree( HtmlTag.TD );
        tdFacetName.setStyle( HtmlStyle.COL_FIRST );
        addFacet( member, tdFacetName );
        HtmlTree tdSummary = new HtmlTree( HtmlTag.TD );
        setInfoColumnStyle( tdSummary );
        addFacetType( member, tdSummary );
        HtmlTree tr = HtmlTree.tr( tdFacetName );
        tr.addContent( tdSummary );
        addRowStyle( tr, counter );
        return tr;
    }

    /**
     * {@inheritDoc}
     */
    protected void addFacetType(FacetDocumentationBuilder member, Content tdSummary) {
        Content strong = HtmlTree.strong( new RawHtml( member.getType().name() ) );
        Content code = HtmlTree.code( strong );
        tdSummary.addContent( code );
    }

    /**
     * {@inheritDoc}
     */
    protected void addFacet(FacetDocumentationBuilder member, Content tdSummaryType) {
        HtmlTree code = new HtmlTree( HtmlTag.CODE );
        code.addContent(
            new RawHtml( writer.getLink( new LinkInfoImpl( LinkInfoImpl.CONTEXT_SUMMARY_RETURN_TYPE, member ) ) ) );
        Content strong = HtmlTree.strong( code );
        tdSummaryType.addContent( strong );
    }

    /**
     * Build the inherited member summary for the given methods.
     *
     * @param summaryTree tree to which the documentation will be added
     */
    protected void addInheritedInfoSummary(Content summaryTree) {
        FacetOwnerDocumentationBuilder<?> ext = getParent( source );

        while (ext != null) {
            List<FacetDocumentationBuilder> extFacets = ext.getFacets();
            List<FacetDocumentationBuilder> inheritedFacets = new ArrayList<>();

            for (FacetDocumentationBuilder fdb : extFacets) {
                if (!hasFacet( ext, fdb )) {
                    inheritedFacets.add( fdb );
                }
            }

            if (!inheritedFacets.isEmpty()) {
                addInheritedInfoHeader( ext, summaryTree, "doclet.Facets_Inherited_From" );
                addInheritedInfo( inheritedFacets, ext, summaryTree );
            }

            ext = getParent( ext );
        }
    }

    private boolean hasFacet(FacetOwnerDocumentationBuilder<?> fodb, FacetDocumentationBuilder fdb) {
        boolean hasFacet = false;

        for (FacetDocumentationBuilder fdb2 : source.getFacets()) {
            TLFacetType extFacetType = fdb.getType();

            if (extFacetType.equals( TLFacetType.ID ) || extFacetType.equals( TLFacetType.SUMMARY )
                || extFacetType.equals( TLFacetType.DETAIL )) {
                if (fdb2.getType().equals( extFacetType )) {
                    hasFacet = true;
                }
            } else { // should be a custom or query
                String extFacetName = fdb.getName();
                // get the custom part
                String extName = extFacetName.substring( fodb.getName().length() );

                if (fdb2.getName().contains( extName )) {
                    hasFacet = true;
                }
            }
            if (hasFacet) {
                break;
            }
        }
        return hasFacet;
    }


    protected FacetOwnerDocumentationBuilder<?> getParent(FacetOwnerDocumentationBuilder<?> classDoc) {
        return (FacetOwnerDocumentationBuilder<?>) classDoc.getSuperType();
    }

    /**
     * {@inheritDoc}
     */
    protected void addInheritedInfoAnchor(FacetOwnerDocumentationBuilder<?> parent, Content inheritedTree) {
        inheritedTree
            .addContent( writer.getMarkerAnchor( "facets_inherited_from_object_" + parent.getQualifiedName() ) );
    }


    protected String getInfoTableSummary() {
        Configuration config = writer.newConfiguration();
        return config.getText( "doclet.Facet_Table_Summary", config.getText( "doclet.Facet_Summary" ),
            config.getText( "doclet.facets" ) );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInfoWriter#getInfoTableHeader()
     */
    @Override
    protected String[] getInfoTableHeader() {
        Configuration config = writer.newConfiguration();

        return new String[] {config.getText( "doclet.Name" ), config.getText( "doclet.Type" )};
    }

}
