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
import org.opentravel.schemacompiler.codegen.html.DocletConstants;

/**
 * Class for generating document type for HTML pages of javadoc output.
 */
public class DocType extends Content {

    private String docTypeHeader;

    private static DocType transitional;

    private static DocType frameset;

    private static DocType html5;

    /**
     * Constructor to construct a DocType object.
     *
     * @param type the doctype to be added
     * @param dtd the DTD reference for the markup document
     */
    public DocType(String type, String dtd) {
        docTypeHeader =
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 " + type + "//EN\" \"" + dtd + "\">" + DocletConstants.NL;
    }

    /**
     * Construct and return a HTML 4.01 transitional DocType content
     *
     * @return a content tree for transitional DocType
     */
    public static DocType newTransitional() {
        if (transitional == null) {
            transitional = new DocType( "Transitional", "http://www.w3.org/TR/html4/loose.dtd" );
        }
        return transitional;
    }

    /**
     * Construct and return a HTML 4.01 frameset DocType content
     *
     * @return a content tree for frameset DocType
     */
    public static DocType newFrameset() {
        if (frameset == null) {
            frameset = new DocType( "Frameset", "http://www.w3.org/TR/html4/frameset.dtd" );
        }
        return frameset;
    }

    /**
     * Construct and return a HTML 4.01 frameset DocType content
     *
     * @return a content tree for frameset DocType
     */
    public static DocType newHtml5() {
        if (html5 == null) {
            html5 = new DocType( null, null );
        }
        html5.docTypeHeader = "<!DOCTYPE HTML>" + DocletConstants.NL;
        return html5;
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
        return (docTypeHeader.length() == 0);
    }

    /**
     * {@inheritDoc}
     */
    public void write(StringBuilder contentBuilder) {
        contentBuilder.append( docTypeHeader );
    }
}
