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

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.LinkInfoImpl;
import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

import java.util.List;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AbstractInheritedInfoWriter<T extends AbstractDocumentationBuilder<?>,
    S extends AbstractDocumentationBuilder<?>> extends AbstractInfoWriter<T> {

    /**
     * @param writer the writer for which to create an info-writer
     * @param owner the owner of the new info-writer
     */
    public AbstractInheritedInfoWriter(SubWriterHolderWriter writer, T owner) {
        super( writer, owner );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.writers.info.InfoWriter#addInfo(org.opentravel.schemacompiler.codegen.html.Content)
     */
    @Override
    public void addInfo(Content memberTree) {
        Content infoTree = getInfoTree(); // ul
        Content content = getInfoTreeHeader(); // li
        addInfoSummary( content );
        infoTree.addContent( content );
        memberTree.addContent( infoTree );
        infoTree = getInfoTree(); // ul
        content = getInfoTreeHeader(); // li
        addInheritedInfoSummary( content );
        infoTree.addContent( content );
        memberTree.addContent( infoTree );
    }


    /**
     * Build the inherited member summary for the given fields.
     *
     * @param summaryTree list of content trees to which the documentation will be added
     */
    protected abstract void addInheritedInfoSummary(Content summaryTree);

    protected abstract void addInheritedInfoAnchor(T parent, Content inheritedTree);

    protected abstract T getParent(T classDoc);

    protected abstract Content getInfo(S field, int counter, boolean addCollapse);

    /**
     * Add the inherited member summary.
     *
     * @param inherited the list of members member being documented
     * @param parent the parent of the inherited members
     * @param linksTree the content tree to which the summary will be added
     */
    protected void addInheritedInfo(List<S> inherited, T parent, Content linksTree) {
        for (S pdb : inherited) {
            if (inherited.indexOf( pdb ) != 0) {
                linksTree.addContent( ", " );
            }
            addInheritedInfoLink( parent, pdb, linksTree );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void addInheritedInfoLabel(T parent, Content inheritedTree, String labelKey) {
        Content classLink =
            new RawHtml( writer.getPreQualifiedMemberLink( LinkInfoImpl.CONTEXT_MEMBER, parent, false ) );
        Content label = writer.getResource( (labelKey) );
        Content labelHeading = HtmlTree.heading( HtmlConstants.INHERITED_SUMMARY_HEADING, label );
        labelHeading.addContent( writer.getSpace() );
        labelHeading.addContent( classLink );
        inheritedTree.addContent( labelHeading );
    }

    protected void addInheritedInfoLink(T cd, S member, Content linksTree) {
        linksTree.addContent(
            new RawHtml( writer.getDocLink( LinkInfoImpl.CONTEXT_MEMBER, cd, member, member.getName(), false ) ) );
    }

    /**
     * Add the inherited summary header.
     *
     * @param parent the parent of the inherited members
     * @param inheritedTree the content tree to which the inherited summary header will be added
     * @param labelKey the key for the header label
     */
    protected void addInheritedInfoHeader(T parent, Content inheritedTree, String labelKey) {
        addInheritedInfoAnchor( parent, inheritedTree );
        addInheritedInfoLabel( parent, inheritedTree, labelKey );
    }


}
