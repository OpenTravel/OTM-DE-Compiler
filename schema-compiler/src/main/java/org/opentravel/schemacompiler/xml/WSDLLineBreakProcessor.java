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

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The line-break processor used to format XML Schema documents.
 * 
 * @author S. Livezey
 */
public class WSDLLineBreakProcessor extends PrettyPrintLineBreakProcessor {

    private static final String[] LINE_BREAK_ELEMENTS = { "types", "message", "portType", "binding", "service" };

    private static final List<String> lineBreakElements = Arrays.asList(LINE_BREAK_ELEMENTS);

    /**
     * @see org.opentravel.schemacompiler.xml.PrettyPrintLineBreakProcessor#insertLineBreakTokens(org.w3c.dom.Document)
     */
    @Override
    public void insertLineBreakTokens(Document document) {
        Element rootElement = (Element) document.getFirstChild();
        Node topLevelNode = rootElement.getFirstChild();

        while (topLevelNode != null) {
            if (topLevelNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String elementName = topLevelNode.getNodeName();
            int colonIdx = elementName.indexOf(':');

            if (colonIdx >= 0) {
                elementName = elementName.substring(colonIdx + 1);
            }
            if (lineBreakElements.contains(elementName)) {
                topLevelNode.getParentNode().insertBefore(document.createComment(LINE_BREAK_TOKEN),
                        topLevelNode);
            }
            topLevelNode = topLevelNode.getNextSibling();
        }
        rootElement.appendChild(document.createComment(LINE_BREAK_TOKEN));
    }

}
