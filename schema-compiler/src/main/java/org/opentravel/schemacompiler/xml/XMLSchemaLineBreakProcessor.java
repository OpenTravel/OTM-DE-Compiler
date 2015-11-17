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
public class XMLSchemaLineBreakProcessor implements PrettyPrintLineBreakProcessor {

    private static final String[] LINE_BREAK_ELEMENTS = { "annotation", "complexType",
            "simpleType", "group", "attributeGroup" };

    private static final List<String> lineBreakElements = Arrays.asList(LINE_BREAK_ELEMENTS);

    /**
     * @see org.opentravel.schemacompiler.xml.PrettyPrintLineBreakProcessor#insertLineBreakTokens(org.w3c.dom.Document)
     */
    @Override
    public void insertLineBreakTokens(Document document) {
        Element rootElement = (Element) document.getFirstChild();
        Node topLevelNode = rootElement.getFirstChild();
        boolean importOrIncludeBreakAdded = false;
        boolean lastTokenWasElement = false;
        boolean lastElementHadSubGrp = false;

        while (topLevelNode != null) {
            if (topLevelNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            boolean addLineBreakToken = false;
            String elementName = topLevelNode.getNodeName();
            int colonIdx = elementName.indexOf(':');

            if (colonIdx >= 0) {
                elementName = elementName.substring(colonIdx + 1);
            }

            if (lineBreakElements.contains(elementName)) {
                addLineBreakToken = true;

            } else if (!importOrIncludeBreakAdded
                    && (elementName.equals("import") || elementName.equals("include"))) {
                addLineBreakToken = true;
                importOrIncludeBreakAdded = true;
            }

            if (elementName.equals("element")) {
                boolean elementHasSubGrp = (topLevelNode.getAttributes().getNamedItem("substitutionGroup") != null);
                
                addLineBreakToken = !lastTokenWasElement;
                lastTokenWasElement = true;

                if (!addLineBreakToken) {
                    Element element = (Element) topLevelNode;
                    String nameAttr = element.getAttribute("name");

                    // Also add a line break before elements that are the head of
                    // a substitution group
                    if ((nameAttr != null) && !elementHasSubGrp && lastElementHadSubGrp) {
                        addLineBreakToken = true;
                    }
                }
                lastElementHadSubGrp = elementHasSubGrp;
                
            } else {
                lastTokenWasElement = false;
                lastElementHadSubGrp = false;
            }

            if (addLineBreakToken) {
                topLevelNode.getParentNode().insertBefore(document.createComment(LINE_BREAK_TOKEN),
                        topLevelNode);
            }
            topLevelNode = topLevelNode.getNextSibling();
        }
        rootElement.appendChild(document.createComment(LINE_BREAK_TOKEN));
    }

}
