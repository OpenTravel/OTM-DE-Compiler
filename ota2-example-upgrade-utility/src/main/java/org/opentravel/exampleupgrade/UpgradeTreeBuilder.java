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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.codegen.example.ExampleValueGenerator;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
    private static final Logger log = LoggerFactory.getLogger(UpgradeTreeBuilder.class);

    private Document upgradeDocument;
    private ExampleValueGenerator exampleValueGenerator;
    private ExampleGeneratorOptions exampleOptions;
	private Stack<UpgradeNodeContext> elementStack = new Stack<>();
	
	/**
	 * Constructor that specifies the options to use when generating unmatched sections
	 * of the upgraded example tree.
	 */
	public UpgradeTreeBuilder(ExampleGeneratorOptions exampleOptions) {
		try {
			upgradeDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	        this.exampleValueGenerator = ExampleValueGenerator.getInstance( null );
			this.exampleOptions = exampleOptions;
			
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
		UpgradeModelNavigator navigator = new UpgradeModelNavigator( visitor, otmEntity.getOwningModel(), exampleOptions );
		boolean foundMatch = (matchType != ExampleMatchType.NONE);
		
		elementStack.push( new UpgradeNodeContext( treeItem,  originalRoot, otmEntity, !foundMatch ) );
		navigator.navigate( otmEntity );
		
		// TODO: Ensure all namespace declarations exist on the root element
		treeItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
		
		return treeItem;
	}
	
	/**
	 * Constructs a new item for the upgrade tree structure using the given OTM attribute.
	 * 
	 * @param otmAttribute  the OTM attribute from which to create the tree item
	 */
	private void buildAttributeTreeItem(TLAttribute otmAttribute) {
		log.debug("buildAttributeTreeItem() : " + otmAttribute.getName());
		TreeItem<DOMTreeUpgradeNode> currentElementItem = elementStack.peek().getUpgradeItem();
		Element originalElement = elementStack.peek().getOriginalElement();
		Element upgradeElement = (Element) currentElementItem.getValue().getDomNode();
		Attr originalAttr = (originalElement == null) ?
				null : originalElement.getAttributeNode( otmAttribute.getName() );
		ExampleMatchType matchType = null;
		
		if (originalAttr != null) {
			upgradeElement.setAttribute( otmAttribute.getName(), originalAttr.getValue() );
			matchType = ExampleMatchType.EXACT;
			
		} else if (otmAttribute.isMandatory() || elementStack.peek().isAutogenNode()) {
			upgradeElement.setAttribute( otmAttribute.getName(),
					exampleValueGenerator.getExampleValue( otmAttribute,
							currentElementItem.getValue().getOtmEntity() ) );
			matchType = ExampleMatchType.NONE;
		}
		
		if (matchType != null) {
			Attr upgradeAttr = upgradeElement.getAttributeNode( otmAttribute.getName() );
			TreeItem<DOMTreeUpgradeNode> attributeItem = new TreeItem<DOMTreeUpgradeNode>(
					new DOMTreeUpgradeNode( otmAttribute, upgradeAttr, matchType ) );
			attributeItem.setGraphic( new ImageView( AbstractDOMTreeNode.attributeIcon ) );
			currentElementItem.getChildren().add( attributeItem );
		}
	}
	
	/**
	 * Constructs a new item for the upgrade tree structure using the given OTM indicator.
	 * 
	 * @param otmIndicator  the OTM indicator from which to create the tree item
	 */
	private void buildIndicatorTreeItem(TLIndicator otmIndicator) {
		log.debug("buildIndicatorTreeItem() : " + otmIndicator.getName());
		String indicatorName = otmIndicator.getName() + (otmIndicator.getName().endsWith( "Ind" ) ? "" : "Ind");
		TreeItem<DOMTreeUpgradeNode> currentElementItem = elementStack.peek().getUpgradeItem();
		Element originalElement = elementStack.peek().getOriginalElement();
		Element upgradeElement = (Element) currentElementItem.getValue().getDomNode();
		
		if (otmIndicator.isPublishAsElement()) {
			Element indicatorUpgradeElement = upgradeDocument.createElementNS(
					otmIndicator.getOwningLibrary().getNamespace(), indicatorName );
			Element originalIndicatorElement = elementStack.peek().findNextOriginalChild( indicatorUpgradeElement );
			
			if ((originalIndicatorElement != null) || elementStack.peek().isAutogenNode())  {
				String indicatorValue = HelperUtils.getElementTextValue( originalIndicatorElement );
				TreeItem<DOMTreeUpgradeNode> elementItem;
				
				if (indicatorValue == null) {
					indicatorValue = "false";
				}
				indicatorUpgradeElement.appendChild( upgradeDocument.createTextNode( indicatorValue ) );
				upgradeElement.appendChild( indicatorUpgradeElement );
				elementItem = new TreeItem<DOMTreeUpgradeNode>(
						new DOMTreeUpgradeNode( otmIndicator, indicatorUpgradeElement, ExampleMatchType.EXACT ) );
				elementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
				currentElementItem.getChildren().add( elementItem );
			}
			
		} else { // publish as attribute
			Attr originalAttr = (originalElement == null) ?
					null : originalElement.getAttributeNode( indicatorName );
			TreeItem<DOMTreeUpgradeNode> attributeItem;
			Attr upgradeAttr;
			
			if ((originalAttr != null) || elementStack.peek().isAutogenNode()) {
				upgradeElement.setAttribute( indicatorName, originalAttr.getValue() );
				
				upgradeAttr = upgradeElement.getAttributeNode( indicatorName );
				attributeItem = new TreeItem<DOMTreeUpgradeNode>(
						new DOMTreeUpgradeNode( otmIndicator, upgradeAttr, ExampleMatchType.EXACT ) );
				attributeItem.setGraphic( new ImageView( AbstractDOMTreeNode.attributeIcon ) );
				currentElementItem.getChildren().add( attributeItem );
			}
		}
	}
	
	/**
	 * Constructs a tree item for the given OTM element.  If the element is not generated
	 * for the upgrade tree, this method will return false; true otherwise.
	 * 
	 * @param otmElement  the OTM element for which to construct a tree item
	 * @return boolean
	 */
	private boolean buildElementTreeItem(TLProperty otmElement) {
		log.debug("buildElementTreeItem() : " + otmElement.getName());
		TreeItem<DOMTreeUpgradeNode> currentElementItem = elementStack.peek().getUpgradeItem();
		Element upgradeElement = (Element) currentElementItem.getValue().getDomNode();
		Element simpleChildUpgradeElement = upgradeDocument.createElementNS(
				otmElement.getOwningLibrary().getNamespace(), otmElement.getName() );
		Element originalChildElement = elementStack.peek().findNextOriginalChild( simpleChildUpgradeElement );
		boolean navigateChildren = true;
		boolean isAutoGen = false;
		NamedEntity elementType;
		
		// Find the next child element that matches the name/type of the OTM element
		if (originalChildElement != null) {
			elementType = otmElement.getType();
			
		} else {
			List<NamedEntity> candidateTypes = new ArrayList<>();
			NamedEntity otmElementType = otmElement.getType();
			
			if (otmElementType instanceof TLComplexTypeBase) {
				candidateTypes.addAll( FacetCodegenUtils.getAvailableFacets( (TLComplexTypeBase) otmElementType ) );
				
			} else if (isFacetOwnerAlias( otmElementType )) {
				candidateTypes.addAll( FacetCodegenUtils.getAvailableFacetAliases( (TLAlias) otmElementType ) );
				
			} else {
				candidateTypes.add( otmElementType );
			}
			elementType = candidateTypes.get( 0 ); // Default, just in case we cannot find a match
			
			for (NamedEntity candidateType : candidateTypes) {
				originalChildElement = elementStack.peek().findNextOriginalChild( candidateType );
				
				if (originalChildElement != null) {
					elementType = candidateType;
					break;
				}
			}
		}
		
		// Now we can process the element; differently depending on whether it is a globally-
		// defined or a simple type (or VWA)
		QName elementName = XsdCodegenUtils.getGlobalElementName( elementType );
		TreeItem<DOMTreeUpgradeNode> childElementItem;
		
		if ((elementName == null) || otmElement.isReference() ||
				((elementType instanceof TLListFacet) &&
				(((TLListFacet) elementType).getItemFacet() instanceof TLSimpleFacet))) {
			// Type is a simple value, simple list facet, open/closed enumeration, or VWA
			ExampleMatchType matchType;
			String elementValue;
			
			if (originalChildElement != null) {
				matchType = getMatchType( otmElement, originalChildElement );
				elementValue = HelperUtils.getElementTextValue( originalChildElement );
				
				if (elementValue == null) {
					elementValue = exampleValueGenerator.getExampleValue(
							otmElement, currentElementItem.getValue().getOtmEntity() );
				}
				
			} else if (otmElement.isMandatory() || elementStack.peek().isAutogenNode()) {
				// Only create if a required element is missing from the original document
				matchType = ExampleMatchType.NONE;
				elementValue = exampleValueGenerator.getExampleValue(
						otmElement, currentElementItem.getValue().getOtmEntity() );
				isAutoGen = true;
				
			} else {
				matchType = null;
				elementValue = null;
				navigateChildren = false;
			}
			
			if (navigateChildren) {
				simpleChildUpgradeElement.appendChild( upgradeDocument.createTextNode( elementValue ) );
				upgradeElement.appendChild( simpleChildUpgradeElement );
				childElementItem = new TreeItem<DOMTreeUpgradeNode>(
						new DOMTreeUpgradeNode( otmElement, simpleChildUpgradeElement, matchType ) );
				childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
				currentElementItem.getChildren().add( childElementItem );
				elementStack.peek().nextOriginalChild();
				elementStack.push( new UpgradeNodeContext( childElementItem, originalChildElement, elementType, isAutoGen ) );
			}
			return navigateChildren;
			
		} else { // Must be a global element (not a list facet)
			ExampleMatchType matchType = getMatchType( elementType, originalChildElement );
			Element childUpgradeElement;
			
			if (matchType != ExampleMatchType.NONE) { // Found a match in the original document
				if ((matchType == ExampleMatchType.EXACT_SUBSTITUTABLE) ||
						(matchType == ExampleMatchType.PARTIAL_SUBSTITUTABLE)) {
					// Handle special case for substitutable element matches
					if (elementType instanceof TLFacet) {
						QName substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLFacet) elementType );
						
						childUpgradeElement = upgradeDocument.createElementNS(
								substitutableName.getNamespaceURI(), substitutableName.getLocalPart() );
						
					} else if (isFacetAlias( elementType )) {
						QName substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLAlias) elementType );
						
						childUpgradeElement = upgradeDocument.createElementNS(
								substitutableName.getNamespaceURI(), substitutableName.getLocalPart() );
						
					} else {
						childUpgradeElement = upgradeDocument.createElementNS(
								elementName.getNamespaceURI(), elementName.getLocalPart() );
					}
					
				} else {
					childUpgradeElement = upgradeDocument.createElementNS(
							elementName.getNamespaceURI(), elementName.getLocalPart() );
				}
				upgradeElement.appendChild( childUpgradeElement );
				
			} else if (otmElement.isMandatory() || elementStack.peek().isAutogenNode()) {
				// No match from original document, so we must auto-generate the element
				System.out.println("MIS-MATCH: " + elementName);
				if (elementType instanceof TLComplexTypeBase) {
					QName substitutableName;
					
					elementType = getPreferredFacet( elementType );
					substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLFacet) elementType );
					childUpgradeElement = upgradeDocument.createElementNS(
							substitutableName.getNamespaceURI(), substitutableName.getLocalPart() );
					
				} else if (isFacetOwnerAlias( elementType )) {
					QName substitutableName;
					
					elementType = getPreferredFacet( elementType );
					substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLAlias) elementType );
					childUpgradeElement = upgradeDocument.createElementNS(
							substitutableName.getNamespaceURI(), substitutableName.getLocalPart() );
					
				} else {
					childUpgradeElement = upgradeDocument.createElementNS(
							elementName.getNamespaceURI(), elementName.getLocalPart() );
				}
				upgradeElement.appendChild( childUpgradeElement );
				isAutoGen = true;
				
			} else {
				childUpgradeElement = null;
				navigateChildren = false;
			}
			
			if (childUpgradeElement != null) {
				childElementItem = new TreeItem<DOMTreeUpgradeNode>(
						new DOMTreeUpgradeNode( elementType, childUpgradeElement, matchType ) );
				currentElementItem.getChildren().add( childElementItem );
				childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
				elementStack.peek().nextOriginalChild();
				elementStack.push( new UpgradeNodeContext( childElementItem, originalChildElement, elementType, isAutoGen ) );
			}
			return navigateChildren;
		}
	}
	
	/**
	 * Completes the processing of the given OTM element.
	 * 
	 * @param otmElement  the OTM element being navigated
	 */
	private void finishElementTreeItem(TLProperty otmElement) {
		log.debug("finishElementTreeItem() : name=" + otmElement.getName() + " / type=" + otmElement.getType().getLocalName());
		elementStack.pop();
	}
	
	/**
	 * Constructs a tree item for an extension point group of the given type.
	 * 
	 * @param extensionPointType  the facet type of the extension point group
	 */
	public boolean buildExtensionPointGroupTreeItemStart(TLFacetType extensionPointType) {
		System.out.println("buildExtensionPointGroupTreeItemStart() : " + extensionPointType);
		boolean processChildren = false;
		QName elementName;
		
		switch (extensionPointType) {
			case SUMMARY:
				elementName = SchemaDependency.getExtensionPointSummaryElement().toQName();
				break;
			case SHARED:
				elementName = SchemaDependency.getExtensionPointSharedElement().toQName();
				break;
			default:
				elementName = SchemaDependency.getExtensionPointElement().toQName();
				break;
		}
		Element childUpgradeElement = upgradeDocument.createElementNS(
				elementName.getNamespaceURI(), elementName.getLocalPart() );
		Element originalChildElement = elementStack.peek().findNextOriginalChild( childUpgradeElement );
		
		if (originalChildElement != null) {
			TreeItem<DOMTreeUpgradeNode> childElementItem = new TreeItem<DOMTreeUpgradeNode>(
					new DOMTreeUpgradeNode( (NamedEntity) null, childUpgradeElement, ExampleMatchType.EXACT ) );
			
			childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
			elementStack.push( new UpgradeNodeContext( childElementItem, originalChildElement, null, false ) );
			processChildren = true;
		}
		return processChildren;
	}

	/**
	 * Completes the tree item for an extension point group of the given type.  The
	 * extension point group element is only added to the parent DOM element if one
	 * or more extension point child elements were actually created.
	 * 
	 * @param extensionPointType  the facet type of the extension point group
	 */
	public void buildExtensionPointGroupTreeItemEnd(TLFacetType extensionPointType) {
		log.debug("buildExtensionPointGroupTreeItemEnd() : " + extensionPointType);
		TreeItem<DOMTreeUpgradeNode> extensionPointElementItem = elementStack.pop().getUpgradeItem();
		TreeItem<DOMTreeUpgradeNode> parentElementItem = elementStack.peek().getUpgradeItem();
		
		if (extensionPointElementItem.getChildren().size() > 0) {
			Element extensionPointElement = (Element) extensionPointElementItem.getValue().getDomNode();
			Element parentElement = (Element) parentElementItem.getValue().getDomNode();
			
			parentElement.appendChild( extensionPointElement );
			parentElementItem.getChildren().add( extensionPointElementItem );
		}
	}
	
	/**
	 * Constructs a tree item for the given OTM extension point facet.  If the
	 * extension point facet is not generated for the upgrade tree, this method
	 * will return false; true otherwise.
	 * 
	 * @param otmExtensionPoint  the OTM extension point facet for which to construct a tree item
	 * @return boolean
	 */
	private boolean buildExtensionPointFacetTreeItem(TLExtensionPointFacet otmExtensionPoint) {
		log.debug("buildExtensionPointFacetTreeItem() : " + otmExtensionPoint.getLocalName());
		TreeItem<DOMTreeUpgradeNode> extensionPointElementItem = elementStack.peek().getUpgradeItem();
		QName elementName = XsdCodegenUtils.getGlobalElementName( otmExtensionPoint );
		Element childUpgradeElement = upgradeDocument.createElementNS(
				elementName.getNamespaceURI(), elementName.getLocalPart() );
		Node originalChildNode = elementStack.peek().getOriginalElement().getFirstChild();
		boolean isAutoGen = elementStack.peek().isAutogenNode();
		ExampleMatchType matchType = null;
		boolean processChildren = false;
		Element originalChild = null;
		
		
		// Since extension points are unordered, we need to search the entire list instead
		// of treating it as a sequence (as with normal elements)
		while (originalChildNode != null) {
			if (originalChildNode.getNodeType() == Node.ELEMENT_NODE) {
				matchType = getMatchType( childUpgradeElement, (Element) originalChildNode );
				
				if (matchType != ExampleMatchType.NONE) {
					originalChild = (Element) originalChildNode;
					break;
				}
			}
			originalChildNode = originalChildNode.getNextSibling();
		}
		
		// Only create a new element if we find a match in the original
		// document (or we are auto-generating)
		if ((originalChild != null) || isAutoGen) {
			TreeItem<DOMTreeUpgradeNode> childElementItem = new TreeItem<DOMTreeUpgradeNode>(
					new DOMTreeUpgradeNode( otmExtensionPoint, childUpgradeElement, matchType ) );
			
			childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
			extensionPointElementItem.getValue().getDomNode().appendChild( childUpgradeElement );
			extensionPointElementItem.getChildren().add( childElementItem );
			elementStack.push( new UpgradeNodeContext( childElementItem, originalChild, otmExtensionPoint, isAutoGen ) );
			processChildren = true;
		}
		return processChildren;
	}
	
	/**
	 * Completes the processing of the given OTM extension point facet.
	 * 
	 * @param otmExtensionPoint  the OTM extension point facet being navigated
	 */
	private void finishExtensionPointFacetTreeItem(TLExtensionPointFacet otmExtensionPoint) {
		log.debug("finishExtensionPointFacetTreeItem() : " + otmExtensionPoint.getLocalName());
		elementStack.pop();
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
		
		if ((entityName != null) && (domElement != null)) {
			// First, attempt to match against the non-substitutable element name
			if (entityName.getLocalPart().equals( domElement.getLocalName() )) {
				matchType = getMatchType( entityName.getNamespaceURI(), domElement.getNamespaceURI() );
			}
			
			// If no match was found yet, check the non-substitutable element name (if one exists for the entity)
			if (matchType == ExampleMatchType.NONE) {
				QName substitutableName = null;
				
				if (otmEntity instanceof TLFacet) {
					substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLFacet) otmEntity );
					
				} else if (isFacetAlias( otmEntity )) {
					substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLAlias) otmEntity );
				}
				
				if ((substitutableName != null) &&
						(substitutableName.getLocalPart().equals( domElement.getLocalName() ) ) ) {
					matchType = getMatchType( substitutableName.getNamespaceURI(), domElement.getNamespaceURI() );
					
					if (matchType == ExampleMatchType.EXACT) {
						matchType = ExampleMatchType.EXACT_SUBSTITUTABLE;
						
					} else if (matchType == ExampleMatchType.PARTIAL) {
						matchType = ExampleMatchType.PARTIAL_SUBSTITUTABLE;
					}
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
			matchType = getMatchType( entityName.getNamespaceURI(), originalElement.getNamespaceURI() );
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
	 * Returns the match type between the two namespace URI's provided.  A partial match
	 * will result if the two base namespaces are the same, but versions are different.
	 * 
	 * @param ns1  the first namespace URI
	 * @param ns2  the second namespace URI
	 * @return ExampleMatchType
	 */
	private ExampleMatchType getMatchType(String ns1, String ns2) {
		ExampleMatchType matchType = ExampleMatchType.NONE;
		
		if (ns1.equals( ns2 )) {
			matchType = ExampleMatchType.EXACT;
			
		} else {
			String baseNS1 = HelperUtils.getBaseNamespace( ns1 );
			String baseNS2 = HelperUtils.getBaseNamespace( ns2 );
			
			if (baseNS1.equals( baseNS2 )) {
				matchType = ExampleMatchType.PARTIAL;
			}
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
	 * Returns true if the given OTM entity is an alias of a <code>TLFacet</code> object.
	 * 
	 * @param otmEntity  the OTM entity to check
	 * @return boolean
	 */
	private boolean isFacetAlias(NamedEntity otmEntity) {
		return (otmEntity instanceof TLAlias) &&
				(((TLAlias) otmEntity).getOwningEntity() instanceof TLFacet);
	}
	
	/**
	 * Returns true if the given OTM entity is an alias of a <code>TLComplexTypeBase</code> object.
	 * 
	 * @param otmEntity  the OTM entity to check
	 * @return boolean
	 */
	private boolean isFacetOwnerAlias(NamedEntity otmEntity) {
		return (otmEntity instanceof TLAlias) &&
				(((TLAlias) otmEntity).getOwningEntity() instanceof TLComplexTypeBase);
	}
	
	/**
	 * For an OTM parent (facet owner), the "preferred" or default facet will be returned.
	 * 
	 * @param otmParentEntity  the OTM parent entity that owns the facet to be returned
	 * @return NamedEntity
	 */
	private NamedEntity getPreferredFacet(NamedEntity otmParentEntity) {
		TLComplexTypeBase owner = null;
		TLAlias ownerAlias = null;
		TLFacet preferredFacet;
		
		if (otmParentEntity instanceof TLAlias) {
			ownerAlias = (TLAlias) otmParentEntity;
			otmParentEntity = ownerAlias.getOwningEntity();
		}
		if (otmParentEntity instanceof TLComplexTypeBase) {
			owner = (TLComplexTypeBase) otmParentEntity;
		} else {
			throw new IllegalArgumentException("Entity must be an OTM complex type or an alias of one.");
		}
		
		// TODO: Replace default lookups with preferred facet selections by user
		if (owner instanceof TLBusinessObject) {
			preferredFacet = ((TLBusinessObject) owner).getSummaryFacet();
			
		} else if (owner instanceof TLChoiceObject) {
			TLChoiceObject choice = (TLChoiceObject) owner;
			
			preferredFacet = (choice.getChoiceFacets().size() == 0) ?
					choice.getSharedFacet() : choice.getChoiceFacets().get( 0 );
					
		} else if (owner instanceof TLCoreObject) {
			preferredFacet = ((TLCoreObject) owner).getSummaryFacet();
			
		} else {
			throw new IllegalArgumentException("Unknown complex type: " + owner.getClass().getSimpleName());
		}
		
		if (ownerAlias != null) {
			return AliasCodegenUtils.getFacetAlias( ownerAlias, preferredFacet.getFacetType() );
			
		} else {
			return preferredFacet;
		}
	}
	
	/**
	 * Context that captures the pairing between the upgrade tree node and the original
	 * DOM element (if any).
	 */
	private class UpgradeNodeContext {
		
		private TreeItem<DOMTreeUpgradeNode> upgradeItem;
		private Element originalElement;
		private Node nextOriginalChild;
		private NamedEntity otmElementType;
		private boolean autogenNode = false;
		
		/**
		 * Full constructor.
		 * 
		 * @param upgradeItem  the tree item for the upgrade tree
		 * @param originalElement  the original DOM element from which the upgrade item was created (may be null)
		 * @param otmElementType  the actual type of the OTM element
		 * @param autoGenNode flag value indicating whethe the node's context was created from an auto-generated example
		 */
		public UpgradeNodeContext(TreeItem<DOMTreeUpgradeNode> upgradeItem,
				Element originalElement, NamedEntity otmElementType, boolean autoGenNode) {
			this.upgradeItem = upgradeItem;
			this.originalElement = originalElement;
			this.otmElementType = otmElementType;
			this.autogenNode = autoGenNode;
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
		 * Returns the actual type of the OTM element (may be different than the
		 * model element due to substitution group selections in the original DOM
		 * document).
		 *
		 * @return NamedEntity
		 */
		public NamedEntity getOtmElementType() {
			return otmElementType;
		}

		/**
		 * Returns true if the node's context was created from an auto-generated
		 * example.
		 *
		 * @return boolean
		 */
		public boolean isAutogenNode() {
			return autogenNode;
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
		 * Returns the next original child in the list which matches (either partial or exact)
		 * the OTM entity provided.  If a matching element is found, it will be assigned as
		 * the next-original-child.
		 * 
		 * @param upgradeElement  the upgraded element for which to return a matching child
		 * @return Element
		 */
		public Element findNextOriginalChild(NamedEntity otmEntity) {
			Element nextChild = peekNextOriginalChild();
			
			while (nextChild != null) {
				Node nextChildNode = nextChild;
				
				if (getMatchType( otmEntity, nextChild ) != ExampleMatchType.NONE) {
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
		 * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#getResolvedElementType()
		 */
		@Override
		public NamedEntity getResolvedElementType() {
			return elementStack.peek().getOtmElementType();
		}

		/**
		 * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#canRepeat(org.opentravel.schemacompiler.model.TLProperty, org.opentravel.schemacompiler.model.NamedEntity)
		 */
		@Override
		public boolean canRepeat(TLProperty otmElement, NamedEntity resolvedElementType) {
			Element nextOriginalChild = elementStack.peek().peekNextOriginalChild();
			boolean repeatAllowed = false;
			
			if (nextOriginalChild != null) {
				boolean hasGlobalElement = (XsdCodegenUtils.getGlobalElementName( resolvedElementType ) != null);
				
				if (hasGlobalElement) {
					repeatAllowed = (getMatchType( resolvedElementType, nextOriginalChild) != ExampleMatchType.NONE);
					
				} else { // check for same element name
					repeatAllowed = (getMatchType( otmElement, nextOriginalChild) != ExampleMatchType.NONE);
				}
			}
			return repeatAllowed;
		}

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
			return buildElementTreeItem( element );
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
		 * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#visitExtensionPointGroupStart(org.opentravel.schemacompiler.model.TLFacetType)
		 */
		@Override
		public boolean visitExtensionPointGroupStart(TLFacetType extensionPointType) {
			return buildExtensionPointGroupTreeItemStart( extensionPointType );
		}

		/**
		 * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#visitExtensionPointGroupEnd(org.opentravel.schemacompiler.model.TLFacetType)
		 */
		@Override
		public void visitExtensionPointGroupEnd(TLFacetType extensionPointType) {
			buildExtensionPointGroupTreeItemEnd( extensionPointType );
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
		 */
		@Override
		public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
			return buildExtensionPointFacetTreeItem( extensionPointFacet );
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
