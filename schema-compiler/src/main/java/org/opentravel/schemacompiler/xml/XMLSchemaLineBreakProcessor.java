
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

	private static final String[] LINE_BREAK_ELEMENTS = {
		"annotation", "complexType", "simpleType", "group", "attributeGroup"
	};
	
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
				
			} else if (!importOrIncludeBreakAdded &&
					(elementName.equals("import") || elementName.equals("include"))) {
				addLineBreakToken = true;
				importOrIncludeBreakAdded = true;
			}
			
			if (elementName.equals("element")) {
				addLineBreakToken = !lastTokenWasElement;
				lastTokenWasElement = true;
				
				if (!addLineBreakToken) {
					Element element = (Element) topLevelNode;
					String nameAttr = element.getAttribute("name");
					
					// Also add a line break before "_SubGrp" elements
					if ((nameAttr != null) && nameAttr.endsWith("SubGrp")) {
						addLineBreakToken = true;
					}
				}
			} else {
				lastTokenWasElement = false;
			}
			
			if (addLineBreakToken) {
				topLevelNode.getParentNode().insertBefore(document.createComment(LINE_BREAK_TOKEN), topLevelNode);
			}
			topLevelNode = topLevelNode.getNextSibling();
		}
		rootElement.appendChild(document.createComment(LINE_BREAK_TOKEN));
	}
	
}
