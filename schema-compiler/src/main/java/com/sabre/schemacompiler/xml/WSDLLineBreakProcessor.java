/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.xml;

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
public class WSDLLineBreakProcessor implements PrettyPrintLineBreakProcessor {

	private static final String[] LINE_BREAK_ELEMENTS = {
		"types", "message", "portType", "binding", "service"
	};
	
	private static final List<String> lineBreakElements = Arrays.asList(LINE_BREAK_ELEMENTS);
	
	/**
	 * @see com.sabre.schemacompiler.xml.PrettyPrintLineBreakProcessor#insertLineBreakTokens(org.w3c.dom.Document)
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
				topLevelNode.getParentNode().insertBefore(document.createComment(LINE_BREAK_TOKEN), topLevelNode);
			}
			topLevelNode = topLevelNode.getNextSibling();
		}
		rootElement.appendChild(document.createComment(LINE_BREAK_TOKEN));
	}
	
}
