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

package org.opentravel.schemacompiler.codegen.html.markup;



import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.DocletAbortException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for generating an HTML document for javadoc output.
 *
 * @author Bhavesh Patel
 */
public class HtmlDocument extends Content {

    private List<Content> docContent = Collections.<Content>emptyList();

    /**
     * Constructor to construct an HTML document.
     *
     * @param docType document type for the HTML document
     * @param docComment comment for the document
     * @param htmlTree HTML tree of the document
     */
    public HtmlDocument(Content docType, Content docComment, Content htmlTree) {
        docContent = new ArrayList<>();
        addContent( nullCheck( docType ) );
        addContent( nullCheck( docComment ) );
        addContent( nullCheck( htmlTree ) );
    }

    /**
     * Constructor to construct an HTML document.
     *
     * @param docType document type for the HTML document
     * @param htmlTree HTML tree of the document
     */
    public HtmlDocument(Content docType, Content htmlTree) {
        docContent = new ArrayList<>();
        addContent( nullCheck( docType ) );
        addContent( nullCheck( htmlTree ) );
    }

    /**
     * Adds content for the HTML document.
     *
     * @param htmlContent html content to be added
     */
    public void addContent(Content htmlContent) {
        if (htmlContent.isValid()) {
            docContent.add( htmlContent );
        }
    }

    /**
     * This method is not supported by the class.
     *
     * @param stringContent string content that needs to be added
     * @throws DocletAbortException this method will always throw a DocletAbortException because it is not supported.
     */
    public void addContent(String stringContent) {
        throw new DocletAbortException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return (docContent.isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    public void write(StringBuilder contentBuilder) {
        for (Content c : docContent) {
            c.write( contentBuilder );
        }
    }
}
