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

import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlWriter;

import java.io.IOException;


/**
 * This abstract class exists to provide functionality needed in the the formatting of member information. Since
 * AbstractSubWriter and its subclasses control this, they would be the logical place to put this. However, because each
 * member type has its own subclass, subclassing can not be used effectively to change formatting. The concrete class
 * subclass of this class can be subclassed to change formatting.
 *
 * @author Robert Field
 * @author Atul M Dambalkar
 * @author Bhavesh Patel (Modified)
 */
public class SubWriterHolderWriter extends HtmlWriter {

    public SubWriterHolderWriter(Configuration configuration, String filename) throws IOException {
        super( configuration, filename );
    }


    public SubWriterHolderWriter(Configuration configuration, String path, String filename, String relpath)
        throws IOException {
        super( configuration, path, filename, relpath );
    }


    /**
     * Get the document content header tree
     *
     * @return a content tree the document content header
     */
    public Content getContentHeader() {
        HtmlTree div = new HtmlTree( HtmlTag.DIV );
        div.setStyle( HtmlStyle.CONTENT_CONTAINER );
        return div;
    }

    /**
     * Get the member header tree
     *
     * @return a content tree the member header
     */
    public Content getMemberTreeHeader() {
        HtmlTree li = new HtmlTree( HtmlTag.LI );
        li.setStyle( HtmlStyle.BLOCK_LIST );
        return li;
    }

    /**
     * Get the member tree
     *
     * @param contentTree the tree used to generate the complete member tree
     * @return a content tree for the member
     */
    public Content getMemberTree(Content contentTree) {
        return HtmlTree.ul( HtmlStyle.BLOCK_LIST, contentTree );
    }

}
