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
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.FieldDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.NamedEntityDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlAttr;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.markup.StringContent;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AbstractFieldInfoWriter<T extends NamedEntityDocumentationBuilder<?>,
    S extends FieldDocumentationBuilder<?>> extends AbstractInheritedInfoWriter<T,S> {

    /**
     * @param writer the writer for which to create an info-writer
     * @param owner the owner of the new info-writer
     */
    public AbstractFieldInfoWriter(SubWriterHolderWriter writer, T owner) {
        super( writer, owner );
    }

    protected void addFieldName(S field, Content tdSummary) {
        Content strong = HtmlTree.strong( new RawHtml( field.getName() ) );
        Content code = HtmlTree.code( strong );
        tdSummary.addContent( code );
    }

    protected Content getInfo(S field, int counter, boolean addCollapse) {
        HtmlTree tdName = new HtmlTree( HtmlTag.TD );
        tdName.setStyle( HtmlStyle.COL_FIRST );
        addFieldName( field, tdName );
        String lcName = field.getName().toLowerCase();
        if (addCollapse) {
            addCollapseTrigger( tdName, lcName + "Detail", lcName );
        }
        HtmlTree tdDescription = new HtmlTree( HtmlTag.TD );
        setInfoColumnStyle( tdDescription );

        writer.addSummaryComment( field, tdDescription );
        HtmlTree tr = HtmlTree.tr( tdName );
        tr.addContent( tdDescription );
        addRowStyle( tr, counter );
        return tr;
    }

    protected String getDetailId(S field) {
        return field.getName().toLowerCase() + "Detail";
    }

    protected Content getDetailedInfo(S field) {
        Content detailTable = getDetailTableTree();
        addDetailFieldType( field, detailTable, 0 );
        addValue( "Example Value", field.getExampleValue(), detailTable, 1 );
        addValue( "Required", String.valueOf( field.isRequired() ), detailTable, 0 );
        addValue( "Pattern", field.getPattern(), detailTable, 1 );
        addValue( "Max Occurrences", String.valueOf( field.getMaxOcurrences() ), detailTable, 0 );
        HtmlTree td = HtmlTree.td( HtmlStyle.COL_ONE, detailTable );
        td.addAttr( HtmlAttr.COLSPAN, "2" );
        HtmlTree tr = HtmlTree.tr( td );
        makeCollapsible( tr, getDetailId( field ) );
        return tr;
    }

    private void addValue(String name, String value, Content detailTable, int counter) {
        HtmlTree tdName = new HtmlTree( HtmlTag.TD );
        tdName.setStyle( HtmlStyle.COL_FIRST );
        tdName.addContent( name );
        HtmlTree tdDescription = new HtmlTree( HtmlTag.TD );
        setInfoColumnStyle( tdDescription );
        tdDescription.addContent( value == null ? "N/A" : value );
        HtmlTree tr = HtmlTree.tr( tdName );
        tr.addContent( tdDescription );
        addRowStyle( tr, counter );
        detailTable.addContent( tr );
    }

    protected void addDetailFieldType(S field, Content detailTable, int counter) {
        HtmlTree tdName = new HtmlTree( HtmlTag.TD );
        tdName.setStyle( HtmlStyle.COL_FIRST );
        tdName.addContent( "Type" );
        HtmlTree tdType = new HtmlTree( HtmlTag.TD );
        setInfoColumnStyle( tdType );
        DocumentationBuilder type = field.getType();
        Content typeName;
        if (type != null) {
            typeName =
                new RawHtml( writer.getLink( new LinkInfoImpl( LinkInfoImpl.CONTEXT_SUMMARY_RETURN_TYPE, type ) ) );
        } else {
            typeName = new StringContent( field.getTypeName() );
        }
        tdType.addContent( typeName );
        HtmlTree tr = HtmlTree.tr( tdName );
        tr.addContent( tdType );
        addRowStyle( tr, counter );
        detailTable.addContent( tr );
    }

    protected Content getDetailTableTree() {
        Content table =
            HtmlTree.table( HtmlStyle.OVERVIEW_SUMMARY, 0, 3, 0, getDetailInfoTableSummary(), HtmlTree.EMPTY );
        table.addContent( getDetailTableHeader() );
        return table;
    }

    /**
     * Get detail table header.
     *
     * @return a content tree for the header
     */
    protected Content getDetailTableHeader() {
        HtmlTree th = HtmlTree.th( HtmlStyle.COL_ONE, "row", writer.getResource( "doclet.Details" ) );
        th.addAttr( HtmlAttr.COLSPAN, "2" );
        return new HtmlTree( HtmlTag.TR, th );
    }

    protected abstract String getDetailInfoTableSummary();

    /**
     * Adds the summary content.
     *
     * @param field the field for which the summary will be generated
     * @param htmltree the documentation tree to which the summary will be added
     */
    protected void addFieldComment(S field, Content htmltree) {
        String desc = field.getDescription() == null ? "" : field.getDescription();
        Content div;
        Content result = new RawHtml( desc );
        div = HtmlTree.div( HtmlStyle.BLOCK, result );
        htmltree.addContent( div );
        if (desc.length() == 0) {
            htmltree.addContent( RawHtml.nbsp );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInfoWriter#getInfoTableHeader()
     */
    @Override
    protected String[] getInfoTableHeader() {
        Configuration config = writer.newConfiguration();
        return new String[] {config.getText( "doclet.Name" ), config.getText( "doclet.Description" )};
    }

}
