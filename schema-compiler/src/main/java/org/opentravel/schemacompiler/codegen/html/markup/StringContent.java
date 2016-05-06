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
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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
package org.opentravel.schemacompiler.codegen.html.markup;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.DocletAbortException;
import org.opentravel.schemacompiler.codegen.html.Util;


/**
 * Class for generating string content for HTML tags of javadoc output.
 *
 * @author Bhavesh Patel
 */
public class StringContent extends Content{

    private StringBuilder stringContent;

    /**
     * Constructor to construct StringContent object.
     */
    public StringContent() {
        stringContent = new StringBuilder();
    }

    /**
     * Constructor to construct StringContent object with some initial content.
     *
     * @param initialContent initial content for the object
     */
    public StringContent(String initialContent) {
        stringContent = new StringBuilder(
                Util.escapeHtmlChars(nullCheck(initialContent)));
    }

    /**
     * This method is not supported by the class.
     *
     * @param content content that needs to be added
     * @throws DocletAbortException this method will always throw a
     *                              DocletAbortException because it
     *                              is not supported.
     */
    public void addContent(Content content) {
        throw new DocletAbortException();
    }

    /**
     * Adds content for the StringContent object.  The method escapes
     * HTML characters for the string content that is added.
     *
     * @param strContent string content to be added
     */
    public void addContent(String strContent) {
        stringContent.append(Util.escapeHtmlChars(nullCheck(strContent)));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return (stringContent.length() == 0);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return stringContent.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void write(StringBuilder contentBuilder) {
        contentBuilder.append(stringContent);
    }
}
