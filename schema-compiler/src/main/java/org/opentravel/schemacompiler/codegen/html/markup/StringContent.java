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
import org.opentravel.schemacompiler.codegen.html.Util;


/**
 * Class for generating string content for HTML tags of javadoc output.
 *
 * @author Bhavesh Patel
 */
public class StringContent extends Content {

    private StringBuilder content;

    /**
     * Constructor to construct StringContent object.
     */
    public StringContent() {
        content = new StringBuilder();
    }

    /**
     * Constructor to construct StringContent object with some initial content.
     *
     * @param initialContent initial content for the object
     */
    public StringContent(String initialContent) {
        content = new StringBuilder( Util.escapeHtmlChars( nullCheck( initialContent ) ) );
    }

    /**
     * This method is not supported by the class.
     *
     * @param content content that needs to be added
     * @throws DocletAbortException this method will always throw a DocletAbortException because it is not supported.
     */
    public void addContent(Content content) {
        throw new DocletAbortException();
    }

    /**
     * Adds content for the StringContent object. The method escapes HTML characters for the string content that is
     * added.
     *
     * @param strContent string content to be added
     */
    public void addContent(String strContent) {
        content.append( Util.escapeHtmlChars( nullCheck( strContent ) ) );
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return (content.length() == 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return content.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void write(StringBuilder contentBuilder) {
        contentBuilder.append( content );
    }
}
