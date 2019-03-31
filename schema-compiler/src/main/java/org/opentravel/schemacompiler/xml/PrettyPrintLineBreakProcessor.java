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

package org.opentravel.schemacompiler.xml;

import org.w3c.dom.Document;

/**
 * Line break processor that inserts line-break tokens into a DOM tree, indicating where additional blank spaces should
 * be inserted during the XML formatting process.
 * 
 * @author S. Livezey
 */
public abstract class PrettyPrintLineBreakProcessor {

    public static final String LINE_BREAK_TOKEN = "__LINE_BREAK__";
    public static final String LINE_BREAK_COMMENT = "<!--" + LINE_BREAK_TOKEN + "-->";

    /**
     * Processes the content of the given DOM document, inserting <code>LINE_BREAK_TOKEN</code> comments at any position
     * where additional line breaks will be required during XML formatting.
     * 
     * @param document the DOM document to process
     */
    public abstract void insertLineBreakTokens(Document document);

}
