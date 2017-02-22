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

package org.opentravel.exampleupgrade;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Tree node that encapsulates a single node from the a DOM tree structure.
 */
public class DOMTreeNode {
	
	private static final Image attributeIcon = new Image( DOMTreeNode.class.getResourceAsStream( "/images/nattrib.gif" ) );
	private static final Image elementIcon = new Image( DOMTreeNode.class.getResourceAsStream( "/images/nelem.gif" ) );
	
	private Node domNode;
	private String label;
	
	/**
	 * Constructor that supplies the DOM attribute or element to be displayed.
	 * 
	 * @param domNode  the DOM node instance
	 */
	public DOMTreeNode(Node domNode) {
		String nodeValue;
		
		if (domNode instanceof Attr) {
			nodeValue = ((Attr) domNode).getValue();
			
		} else { // must be an Element
			Node textNode = domNode.getFirstChild();
			
			while ((textNode != null) && !(textNode instanceof Text)) {
				textNode = textNode.getNextSibling();
			}
			nodeValue = (textNode == null) ? null : ((Text) textNode).getData();
			
			if ((nodeValue != null) && (nodeValue.trim().length() == 0)) {
				nodeValue = null;
			}
		}
		this.domNode = domNode;
		this.label = domNode.getNodeName() + ((nodeValue == null) ? "" : (" = " + nodeValue));
	}
	
	/**
	 * Constructs a new tree of <code>DOMTreeNode</code> objects based on the
	 * DOM node provided.
	 * 
	 * @param domNode  the DOM node instance for which to create a tree
	 * @return TreeItem<DOMTreeNode>
	 */
	public static TreeItem<DOMTreeNode> createTree(Node domNode) {
		DOMTreeNode odn = new DOMTreeNode( domNode );
		TreeItem<DOMTreeNode> treeItem = new TreeItem<DOMTreeNode>( odn );
		Node domChild = domNode.getFirstChild();
		Image nodeImage;
		
		if (domNode instanceof Element) {
			NamedNodeMap attrs = ((Element) domNode).getAttributes();
			int attrCount = attrs.getLength();
			
			for (int i = 0; i < attrCount; i++) {
				Attr domAttr = (Attr) attrs.item( i );
				
				if (!domAttr.getNodeName().startsWith("xmlns:")) {
					treeItem.getChildren().add( createTree( domAttr ) );
				}
			}
			nodeImage = elementIcon;
			
		} else { // must be an attribute
			nodeImage = attributeIcon;
		}
		treeItem.setGraphic( new ImageView( nodeImage ) );
		
		while (domChild != null) {
			if (domChild.getNodeType() == Node.ELEMENT_NODE) {
				treeItem.getChildren().add( createTree( domChild ) );
			}
			domChild = domChild.getNextSibling();
		}
		return treeItem;
	}
	
	/**
	 * Returns the DOM node instance.
	 *
	 * @return Node
	 */
	public Node getDomNode() {
		return domNode;
	}
	
	/**
	 * Returns the display label for this node.
	 *
	 * @return String
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return label;
	}
	
}
