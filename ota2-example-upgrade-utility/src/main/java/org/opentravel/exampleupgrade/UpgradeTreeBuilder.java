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

import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.codegen.example.ExampleValueGenerator;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 * Utility class that handles the construction of the upgraded DOM tree structure
 * by comparing items from the OTM model and attempting to match them with the
 * original DOM example tree.
 */
public class UpgradeTreeBuilder {
	
	private Document upgradeDocument;
	private ExampleDocumentBuilder exampleBuilder;
    protected ExampleValueGenerator exampleValueGenerator;
	private Stack<UpgradeNodeContext> elementStack = new Stack<>();
	
	/**
	 * Constructor that specifies the options to use when generating unmatched sections
	 * of the upgraded example tree.
	 * 
	 * @param exampleOptions  the example generation options
	 */
	public UpgradeTreeBuilder(ExampleGeneratorOptions exampleOptions) {
		try {
			upgradeDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			this.exampleBuilder = new ExampleDocumentBuilder( exampleOptions );
	        this.exampleValueGenerator = ExampleValueGenerator.getInstance( null );
			
		} catch (ParserConfigurationException e) {
			throw new Error("Unable to create DOM document instance.");
		}
	}
	
	/**
	 * Constructs the upgraded DOM tree along with the <code>TreeItem</code> structure that will
	 * be displayed in the visual interface.
	 * 
	 * @param otmEntity  the OTM entity for which to construct the upgraded DOM tree
	 * @param originalRoot  the root element of the original DOM tree
	 * @return TreeItem<DOMTreeUpgradeNode>
	 */
	public TreeItem<DOMTreeUpgradeNode> buildUpgradeDOMTree(NamedEntity otmEntity, Element originalRoot) {
		ExampleMatchType matchType = getMatchType( otmEntity, originalRoot );
		DOMTreeUpgradeNode node = new DOMTreeUpgradeNode( otmEntity, createDomElement( otmEntity ), matchType );
		TreeItem<DOMTreeUpgradeNode> treeItem = new TreeItem<>( node );
		UpgradeModelVisitor visitor = new UMVisitor();
		UpgradeModelNavigator navigator = new UpgradeModelNavigator( visitor, otmEntity.getOwningModel() );
		
		if (matchType != ExampleMatchType.NONE) {
			elementStack.push( new UpgradeNodeContext( treeItem,  originalRoot ) );
			navigator.navigate( otmEntity );
			
		} else {
			// TODO: Build upgraded DOM tree with example generator
		}
		treeItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
		
		return treeItem;
	}
	
	/**
	 * Constructs a new item for the upgrade tree structure using the given OTM attribute.
	 * 
	 * @param otmAttribute  the OTM attribute from which to create the tree item
	 */
	private void buildAttributeTreeItem(TLAttribute otmAttribute) {
		System.out.println("buildAttributeTreeItem() : " + otmAttribute.getName());
		TreeItem<DOMTreeUpgradeNode> currentElementItem = elementStack.peek().getUpgradeItem();
		Element originalElement = elementStack.peek().getOriginalElement();
		Element upgradeElement = (Element) currentElementItem.getValue().getDomNode();
		Attr originalAttr = (originalElement == null) ?
				null : originalElement.getAttributeNode( otmAttribute.getName() );
		TreeItem<DOMTreeUpgradeNode> attributeItem;
		ExampleMatchType matchType;
		Attr upgradeAttr;
		
		if (originalAttr != null) {
			upgradeElement.setAttribute( otmAttribute.getName(), originalAttr.getValue() );
			matchType = ExampleMatchType.EXACT;
			
		} else {
			upgradeElement.setAttribute( otmAttribute.getName(),
					exampleValueGenerator.getExampleValue( otmAttribute,
							currentElementItem.getValue().getOtmEntity() ) );
			matchType = ExampleMatchType.NONE;
		}
		upgradeAttr = upgradeElement.getAttributeNode( otmAttribute.getName() );
		attributeItem = new TreeItem<DOMTreeUpgradeNode>(
				new DOMTreeUpgradeNode( otmAttribute, upgradeAttr, matchType ) );
		attributeItem.setGraphic( new ImageView( AbstractDOMTreeNode.attributeIcon ) );
		currentElementItem.getChildren().add( attributeItem );
	}
	
	/**
	 * Constructs a new item for the upgrade tree structure using the given OTM indicator.
	 * 
	 * @param otmIndicator  the OTM indicator from which to create the tree item
	 */
	private void buildIndicatorTreeItem(TLIndicator otmIndicator) {
		System.out.println("buildIndicatorTreeItem() : " + otmIndicator.getName());
		TreeItem<DOMTreeUpgradeNode> currentElementItem = elementStack.peek().getUpgradeItem();
		Element originalElement = elementStack.peek().getOriginalElement();
		Element upgradeElement = (Element) currentElementItem.getValue().getDomNode();
		String indicatorName = otmIndicator.getName() + (otmIndicator.getName().endsWith( "Ind" ) ? "" : "Ind");
		Attr originalAttr = (originalElement == null) ?
				null : originalElement.getAttributeNode( indicatorName );
		TreeItem<DOMTreeUpgradeNode> attributeItem;
		ExampleMatchType matchType;
		Attr upgradeAttr;
		
		if (originalAttr != null) {
			upgradeElement.setAttribute( indicatorName, originalAttr.getValue() );
			matchType = ExampleMatchType.EXACT;
			
		} else {
			upgradeElement.setAttribute( indicatorName, "true" );
			matchType = ExampleMatchType.NONE;
		}
		upgradeAttr = upgradeElement.getAttributeNode( indicatorName );
		attributeItem = new TreeItem<DOMTreeUpgradeNode>(
				new DOMTreeUpgradeNode( otmIndicator, upgradeAttr, matchType ) );
		attributeItem.setGraphic( new ImageView( AbstractDOMTreeNode.attributeIcon ) );
		currentElementItem.getChildren().add( attributeItem );
	}
	
	private void buildElementTreeItem(TLProperty otmElement) {
		System.out.println("buildElementTreeItem() : " + otmElement.getName());
		TreeItem<DOMTreeUpgradeNode> currentElementItem = elementStack.peek().getUpgradeItem();
		Element upgradeElement = (Element) currentElementItem.getValue().getDomNode();
		NamedEntity elementType = getElementType( otmElement );
		QName elementName = XsdCodegenUtils.getGlobalElementName( elementType );
		TreeItem<DOMTreeUpgradeNode> childElementItem;
		
		// TODO: Handle repeating elements...
		
		if ((elementName == null) || ((elementType instanceof TLListFacet) &&
				(((TLListFacet) elementType).getItemFacet() instanceof TLSimpleFacet))) {
			// Type is a simple value, simple list facet, or VWA
			Element childUpgradeElement = upgradeDocument.createElementNS(
					otmElement.getOwningLibrary().getNamespace(), otmElement.getName() );
			Element originalChildElement = elementStack.peek().findNextOriginalChild( childUpgradeElement );
			ExampleMatchType matchType;
			String elementValue;
			
			if (originalChildElement != null) {
				matchType = getMatchType( otmElement, originalChildElement );
				elementValue = HelperUtils.getElementTextValue( originalChildElement );
				
				if (elementValue == null) {
					elementValue = exampleValueGenerator.getExampleValue(
							otmElement, currentElementItem.getValue().getOtmEntity() );
				}
				
			} else {
				matchType = ExampleMatchType.NONE;
				elementValue = exampleValueGenerator.getExampleValue(
						otmElement, currentElementItem.getValue().getOtmEntity() );
			}
			
			childUpgradeElement.appendChild( upgradeDocument.createTextNode( elementValue ) );
			upgradeElement.appendChild( childUpgradeElement );
			childElementItem = new TreeItem<DOMTreeUpgradeNode>(
					new DOMTreeUpgradeNode( otmElement, childUpgradeElement, matchType ) );
			childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
			currentElementItem.getChildren().add( childElementItem );
			elementStack.push( new UpgradeNodeContext( childElementItem, originalChildElement ) );
			
		} else { // Must be a global element (not a list facet)
			Element childUpgradeElement = upgradeDocument.createElementNS(
					elementName.getNamespaceURI(), elementName.getLocalPart() );
			Element originalChildElement = elementStack.peek().findNextOriginalChild( childUpgradeElement );
			ExampleMatchType matchType = getMatchType( elementType, originalChildElement );
			
			if (matchType != ExampleMatchType.NONE) {
				upgradeElement.appendChild( childUpgradeElement );
				
			} else {
				try {
					Document stubDocument = exampleBuilder.setModelElement( elementType ).buildTree();
					
					childUpgradeElement = (Element) upgradeDocument.adoptNode( stubDocument.getDocumentElement() );
					upgradeElement.appendChild( childUpgradeElement );
					
				} catch (ValidationException | CodeGenerationException e) {
					throw new RuntimeException(e); // TODO: Better exception handling...
				}
			}
			
			childElementItem = new TreeItem<DOMTreeUpgradeNode>(
					new DOMTreeUpgradeNode( elementType, childUpgradeElement, matchType ) );
			currentElementItem.getChildren().add( childElementItem );
			childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
			elementStack.push( new UpgradeNodeContext( childElementItem, originalChildElement ) );
		}
	}
	
	private void finishElementTreeItem(TLProperty otmElement) {
		System.out.println("finishElementTreeItem() : name=" + otmElement.getName() + " / type=" + otmElement.getType().getLocalName());
		elementStack.pop();
	}
	
	private void buildExtensionPointFacetTreeItem(TLExtensionPointFacet otmExtensionPoint) {
		System.out.println("buildExtensionPointFacetTreeItem() : " + otmExtensionPoint.getLocalName());
	}
	
	private void finishExtensionPointFacetTreeItem(TLExtensionPointFacet otmExtensionPoint) {
		System.out.println("finishExtensionPointFacetTreeItem() : " + otmExtensionPoint.getLocalName());
	}
	
	/**
	 * Returns the type of match between the given OTM entity and the DOM element provided.
	 * 
	 * @param otmEntity  the OTM entity with which to compare the DOM element
	 * @param domElement  the DOM element to be compared
	 * @return ExampleMatchType
	 */
	private ExampleMatchType getMatchType(NamedEntity otmEntity, Element domElement) {
		QName entityName = XsdCodegenUtils.getGlobalElementName( otmEntity );
		ExampleMatchType matchType = ExampleMatchType.NONE;
		
		if (entityName == null) {
			throw new IllegalArgumentException(
					"OTM entity does not have an associated global XML element: " + otmEntity.getLocalName() );
		}
		
		if ((domElement != null) && entityName.getLocalPart().equals( domElement.getLocalName() )) {
			if (entityName.getNamespaceURI().equals( domElement.getNamespaceURI() )) {
				matchType = ExampleMatchType.EXACT;
				
			} else {
				String elementBaseNS = HelperUtils.getBaseNamespace( domElement.getNamespaceURI() );
				String entityBaseNS = HelperUtils.getBaseNamespace( entityName.getNamespaceURI() );
				
				if (elementBaseNS.equals( entityBaseNS )) {
					matchType = ExampleMatchType.PARTIAL;
				}
			}
		}
		return matchType;
	}
	
	/**
	 * Returns the type of match between the given OTM entity and the DOM element provided.
	 * 
	 * @param otmEntity  the OTM entity with which to compare the DOM element
	 * @param domElement  the DOM element to be compared
	 * @return ExampleMatchType
	 */
	private ExampleMatchType getMatchType(Element originalElement, Element upgradeElement) {
		QName entityName = new QName( upgradeElement.getNamespaceURI(), upgradeElement.getLocalName() );
		ExampleMatchType matchType = ExampleMatchType.NONE;
		
		if (entityName.getLocalPart().equals( originalElement.getLocalName() )) {
			if (entityName.getNamespaceURI().equals( originalElement.getNamespaceURI() )) {
				matchType = ExampleMatchType.EXACT;
				
			} else {
				String elementBaseNS = HelperUtils.getBaseNamespace( originalElement.getNamespaceURI() );
				String entityBaseNS = HelperUtils.getBaseNamespace( entityName.getNamespaceURI() );
				
				if (elementBaseNS.equals( entityBaseNS )) {
					matchType = ExampleMatchType.PARTIAL;
				}
			}
		}
		return matchType;
	}
	
	/**
	 * Returns the type of match between the given OTM entity and the DOM element provided.
	 * 
	 * @param otmField  the OTM entity with which to compare the DOM element
	 * @param domElement  the DOM element to be compared
	 * @return ExampleMatchType
	 */
	private ExampleMatchType getMatchType(TLMemberField<?> otmField, Element domElement) {
		ExampleMatchType matchType;
		
		if (otmField.getName().equals( domElement.getLocalName() )) {
			matchType = ExampleMatchType.EXACT;
		} else {
			matchType = ExampleMatchType.NONE;
		}
		return matchType;
	}
	
	/**
	 * Creates a new DOM element for the given entity.
	 * 
	 * @param entity  the OTM entity for which to create a DOM element
	 * @return Element
	 */
	private Element createDomElement(NamedEntity entity) {
		QName entityName = XsdCodegenUtils.getGlobalElementName( entity );
		Element domElement;
		
		if (entityName != null) {
			domElement = upgradeDocument.createElementNS(
					entityName.getNamespaceURI(), entityName.getLocalPart() );
			
		} else {
			throw new IllegalArgumentException("The OTM entity does not define a global XML element.");
		}
		return domElement;
	}
	
	/**
	 * Creates a new DOM element for the given simple OTM element.
	 * 
	 * @param simpleElement  the OTM element for which to create a DOM element
	 * @return Element
	 */
	private Element createDomElement(TLProperty simpleElement) {
		QName entityName = XsdCodegenUtils.getGlobalElementName( simpleElement.getType() );
		Element domElement;
		
		if (entityName == null) {
			domElement = upgradeDocument.createElementNS(
					simpleElement.getOwningLibrary().getNamespace(), simpleElement.getName() );
			
		} else {
			throw new IllegalArgumentException("The OTM entity cannot define a global XML element.");
		}
		return domElement;
	}
	
	/**
	 * Returns the OTM element type that should be used to generate example output for
	 * the upgraded DOM document.
	 * 
	 * @param otmElement  the OTM element to use for example generation
	 * @return NamedEntity
	 */
	private NamedEntity getElementType(TLProperty otmElement) {
		NamedEntity elementType = otmElement.getType();
		
		// TODO: Implement lookahead in original DOM document to find the facet we should be
		// using instead of defaulting to the summary/shared facet
		if (elementType instanceof TLBusinessObject) {
			elementType = ((TLBusinessObject) elementType).getSummaryFacet();
			
		} else if (elementType instanceof TLChoiceObject) {
			elementType = ((TLChoiceObject) elementType).getSharedFacet();
			
		} else if (elementType instanceof TLCoreObject) {
			elementType = ((TLCoreObject) elementType).getSummaryFacet();
			
		} else if (elementType instanceof TLAlias) {
			TLAlias alias = (TLAlias) elementType;
			TLAliasOwner aliasOwner = alias.getOwningEntity();
			
			if (aliasOwner instanceof TLBusinessObject) {
				elementType = AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SUMMARY );
				
			} else if (aliasOwner instanceof TLChoiceObject) {
				elementType = AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SHARED );
				
			} else if (aliasOwner instanceof TLCoreObject) {
				elementType = AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SUMMARY );
			}
		}
		return elementType;
	}
	
	/**
	 * Context that captures the pairing between the upgrade tree node and the original
	 * DOM element (if any).
	 */
	private class UpgradeNodeContext {
		
		private TreeItem<DOMTreeUpgradeNode> upgradeItem;
		private Element originalElement;
		private Node nextOriginalChild;
		
		/**
		 * Full constructor.
		 * 
		 * @param upgradeItem  the tree item for the upgrade tree
		 * @param originalElement  the original DOM element from which the upgrade item was created (may be null)
		 */
		public UpgradeNodeContext(TreeItem<DOMTreeUpgradeNode> upgradeItem, Element originalElement) {
			this.upgradeItem = upgradeItem;
			this.originalElement = originalElement;
			this.nextOriginalChild = (originalElement == null) ? null : originalElement.getFirstChild();
			advanceToNextOriginalChild();
		}
		
		/**
		 * Returns the tree item for the upgrade tree.
		 *
		 * @return TreeItem<DOMTreeUpgradeNode>
		 */
		public TreeItem<DOMTreeUpgradeNode> getUpgradeItem() {
			return upgradeItem;
		}
		
		/**
		 * Returns the original DOM element from which the upgrade item was created (may be null).
		 *
		 * @return Element
		 */
		public Element getOriginalElement() {
			return originalElement;
		}
		
		/**
		 * Returns the next DOM child element under the original child element and advances
		 * to the subsequent child.
		 *
		 * @return Element
		 */
		public Element nextOriginalChild() {
			Element nextChild = (Element) nextOriginalChild;
			advanceToNextOriginalChild();
			
			return nextChild;
		}
		
		/**
		 * Returns the next DOM child element under the original child element without
		 * advancing to the subsequent child.
		 *
		 * @return Element
		 */
		public Element peekNextOriginalChild() {
			return (Element) nextOriginalChild;
		}
		
		/**
		 * Returns the next original child in the list which matches (either partial or exact)
		 * the upgraded element provided.  If a matching element is found, it will be assigned as
		 * the next-original-child.
		 * 
		 * @param upgradeElement  the upgraded element for which to return a matching child
		 * @return Element
		 */
		public Element findNextOriginalChild(Element upgradeElement) {
			Element nextChild = peekNextOriginalChild();
			
			while (nextChild != null) {
				Node nextChildNode = nextChild;
				
				if (getMatchType( nextChild, upgradeElement ) != ExampleMatchType.NONE) {
					nextOriginalChild = nextChild;
					break;
				}
				
				nextChild = null;
				
				while (nextChildNode != null) {
					nextChildNode = nextChildNode.getNextSibling();
					
					if ((nextChildNode != null) &&
							(nextChildNode.getNodeType() == Node.ELEMENT_NODE)) {
						nextChild = (Element) nextChildNode;
						break;
					}
				}
			}
			return nextChild;
		}
		
		/**
		 * Advances to the next DOM element child of the original DOM element.  If the end
		 * of the child list has been reached, this method will return null.
		 */
		private void advanceToNextOriginalChild() {
			while (nextOriginalChild != null) {
				nextOriginalChild = nextOriginalChild.getNextSibling();
				
				if ((nextOriginalChild != null) &&
						(nextOriginalChild.getNodeType() == Node.ELEMENT_NODE)) {
					break;
				}
			}
		}
		
	}
	
	/**
	 * Implementation of the <code>UpgradeModelVisitor</code> that invokes callbacks
	 * to the private methods of this class.
	 */
	private class UMVisitor extends UpgradeModelVisitor {

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
		 */
		@Override
		public boolean visitAttribute(TLAttribute attribute) {
			buildAttributeTreeItem( attribute );
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitIndicator(org.opentravel.schemacompiler.model.TLIndicator)
		 */
		@Override
		public boolean visitIndicator(TLIndicator indicator) {
			buildIndicatorTreeItem( indicator );
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
		 */
		@Override
		public boolean visitElement(TLProperty element) {
			buildElementTreeItem( element );
			return true;
		}

		/**
		 * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#visitElementEnd(org.opentravel.schemacompiler.model.TLProperty)
		 */
		@Override
		public boolean visitElementEnd(TLProperty element) {
			finishElementTreeItem( element );
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
		 */
		@Override
		public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
			buildExtensionPointFacetTreeItem( extensionPointFacet );
			return true;
		}
		
		/**
		 * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#visitExtensionPointEnd(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
		 */
		@Override
		public boolean visitExtensionPointEnd(TLExtensionPointFacet extensionPointFacet) {
			finishExtensionPointFacetTreeItem( extensionPointFacet );
			return true;
		}

	}
	
}
