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
/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.opentravel.schemacompiler.codegen.html.writers;

import java.io.IOException;

import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.HtmlDocletWriter;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;


/**
 * This abstract class exists to provide functionality needed in the
 * the formatting of member information.  Since AbstractSubWriter and its
 * subclasses control this, they would be the logical place to put this.
 * However, because each member type has its own subclass, subclassing
 * can not be used effectively to change formatting.  The concrete
 * class subclass of this class can be subclassed to change formatting.
 *
 * @see AbstractMemberWriter
 * @see BusinessObjectWriter
 *
 * @author Robert Field
 * @author Atul M Dambalkar
 * @author Bhavesh Patel (Modified)
 */
public class SubWriterHolderWriter extends HtmlDocletWriter {

    public SubWriterHolderWriter(Configuration configuration,
                                 String filename) throws IOException {
        super(configuration, filename);
    }


    public SubWriterHolderWriter(Configuration configuration,
                                 String path, String filename, String relpath)
                                 throws IOException {
        super(configuration, path, filename, relpath);
    }


    /**
     * Get the document content header tree
     *
     * @return a content tree the document content header
     */
    public Content getContentHeader() {
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.setStyle(HtmlStyle.contentContainer);
        return div;
    }

    /**
     * Get the member header tree
     *
     * @return a content tree the member header
     */
    public Content getMemberTreeHeader() {
        HtmlTree li = new HtmlTree(HtmlTag.LI);
        li.setStyle(HtmlStyle.blockList);
        return li;
    }

    /**
     * Get the member tree
     *
     * @param contentTree the tree used to generate the complete member tree
     * @return a content tree for the member
     */
    public Content getMemberTree(Content contentTree) {
        Content ul = HtmlTree.UL(HtmlStyle.blockList, contentTree);
        return ul;
    }

}
