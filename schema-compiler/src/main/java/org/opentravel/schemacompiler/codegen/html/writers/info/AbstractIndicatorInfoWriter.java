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
import org.opentravel.schemacompiler.codegen.html.builders.AttributeOwnerDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.IndicatorDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

import java.util.List;

/**
 * @author Eric.Bronson
 */
public abstract class AbstractIndicatorInfoWriter<T extends AttributeOwnerDocumentationBuilder<?>>
    extends AbstractFieldInfoWriter<T,IndicatorDocumentationBuilder> {

    /**
     * @param writer the writer for which to create an info-writer
     * @param owner the owner of the new info-writer
     */
    public AbstractIndicatorInfoWriter(SubWriterHolderWriter writer, T owner) {
        super( writer, owner );
        title = writer.getResource( "doclet.Indicator_Summary" );
        caption = writer.newConfiguration().getText( "doclet.Indicators" );
    }


    /**
     * @see org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInfoWriter#addInfoSummary(org.opentravel.schemacompiler.codegen.html.Content)
     */
    @Override
    protected void addInfoSummary(Content summaryTree) {
        Content label = getInfoLabel();
        summaryTree.addContent( label );
        List<IndicatorDocumentationBuilder> indicators = source.getIndicators();
        if (!indicators.isEmpty()) {
            Content tableTree = getTableTree();
            for (IndicatorDocumentationBuilder idb : indicators) {
                Content propSummary = getInfo( idb, indicators.indexOf( idb ), false );
                tableTree.addContent( propSummary );
            }
            summaryTree.addContent( tableTree );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInheritedInfoWriter#addInheritedInfoSummary(org.opentravel.schemacompiler.codegen.html.Content)
     */
    @Override
    protected void addInheritedInfoSummary(Content summaryTree) {
        T parent = getParent( source );
        while (parent != null) {
            if (!parent.getIndicators().isEmpty()) {
                addInheritedInfoHeader( parent, summaryTree, "doclet.Indicators_Inherited_From" );
                addInheritedInfo( parent.getIndicators(), parent, summaryTree );
            }
            parent = getParent( parent );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInheritedInfoWriter#addInheritedInfoAnchor(org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder,
     *      org.opentravel.schemacompiler.codegen.html.Content)
     */
    @Override
    protected void addInheritedInfoAnchor(T parent, Content inheritedTree) {
        inheritedTree.addContent( writer.getMarkerAnchor( "indicators_inherited_from_" + parent.getQualifiedName() ) );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInfoWriter#getInfoTableSummary()
     */
    @Override
    protected String getInfoTableSummary() {
        Configuration config = writer.newConfiguration();
        return config.getText( "doclet.Indicator_Table_Summary", config.getText( "doclet.Indicator_Summary" ),
            config.getText( "doclet.indicators" ) );
    }

    protected String getDetailInfoTableSummary() {
        return "";
    }

    @Override
    protected Content getDetailedInfo(IndicatorDocumentationBuilder field) {
        return HtmlTree.EMPTY;
    }

}
