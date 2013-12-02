/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.example;

import java.util.Collection;

import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLAttributeType;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLRoleEnumeration;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDComplexType;
import com.sabre.schemacompiler.model.XSDElement;

/**
 * Visitor interface that defines the callback methods invoked by the <code>ExampleNavigator</code>
 * component.
 * 
 * @author S. Livezey
 */
public interface ExampleVisitor {
	
	/**
	 * After example navigation is complete, this method will return the list of namespaces that were
	 * identified (bound) to the example output during navigation/visitation processing.
	 * 
	 * @return Collection<String>
	 */
	public Collection<String> getBoundNamespaces();
	
	/**
	 * Called when a <code>TLAttributeType</code> (i.e. simple type) is encountered during navigation.
	 * 
	 * @param simpleType  the simple type model element to be visited
	 */
	public void visitSimpleType(TLAttributeType simpleType);
	
	/**
	 * Called when a <code>TLFacet</code> instance is first encountered during example navigation.
	 * 
	 * @param facet  the model element to be visited
	 */
	public void startFacet(TLFacet facet);
	
	/**
	 * Called when a <code>TLFacet</code> instance has completed processing during example navigation.
	 * 
	 * @param facet  the model element to be visited
	 */
	public void endFacet(TLFacet facet);
	
	/**
	 * Called when a <code>TLListFacet</code> instance is first encountered during example navigation.
	 * 
	 * @param listFacet  the model element to be visited
	 * @param role  the role for this particular list facet instance
	 */
	public void startListFacet(TLListFacet listFacet, TLRole role);
	
	/**
	 * Called when a <code>TLListFacet</code> instance has completed processing during example navigation.
	 * 
	 * @param listFacet  the model element to be visited
	 * @param role  the role for this particular list facet instance
	 */
	public void endListFacet(TLListFacet listFacet, TLRole role);
	
	/**
	 * Called when a <code>TLAlias</code> instance is first encountered during example navigation.
	 * 
	 * @param alias  the model element to be visited
	 */
	public void startAlias(TLAlias alias);
	
	/**
	 * Called when a <code>TLAlias</code> instance has completed processing during example navigation.
	 * 
	 * @param alias  the model element to be visited
	 */
	public void endAlias(TLAlias alias);
	
	/**
	 * Called when a <code>TLAttribute</code> instance is first encountered during example navigation.
	 * 
	 * @param attribute  the model element to be visited
	 */
	public void startAttribute(TLAttribute attribute);
	
	/**
	 * Called when a <code>TLAttribute</code> instance has completed processing during example navigation.
	 * 
	 * @param attribute  the model element to be visited
	 */
	public void endAttribute(TLAttribute attribute);
	
	/**
	 * Called when a <code>TLProperty</code> instance is first encountered during example navigation, and
	 * the property's assigned type is a simple one.
	 * 
	 * @param element  the model element to be visited
	 */
	public void startElement(TLProperty element);
	
	/**
	 * Called when a <code>TLProperty</code> instance has completed processing during example navigation, and
	 * the property's assigned type is a simple one.
	 * 
	 * @param element  the model element to be visited
	 */
	public void endElement(TLProperty element);
	
	/**
	 * Called when a <code>TLIndicator</code> attribute instance is first encountered during example navigation.
	 * 
	 * @param indicator  the model element to be visited
	 */
	public void startIndicatorAttribute(TLIndicator indicator);
	
	/**
	 * Called when a <code>TLIndicator</code> attribute instance has completed processing during example navigation.
	 * 
	 * @param indicator  the model element to be visited
	 */
	public void endIndicatorAttribute(TLIndicator indicator);
	
	/**
	 * Called when a <code>TLIndicator</code> element instance is first encountered during example navigation.
	 * 
	 * @param indicator  the model element to be visited
	 */
	public void startIndicatorElement(TLIndicator indicator);
	
	/**
	 * Called when a <code>TLIndicator</code> element instance has completed processing during example navigation.
	 * 
	 * @param indicator  the model element to be visited
	 */
	public void endIndicatorElement(TLIndicator indicator);
	
	/**
	 * Called when a <code>TLOpenEnumeration</code> instance is first encountered during example navigation.
	 * 
	 * @param openEnum  the model element to be visited
	 */
	public void startOpenEnumeration(TLOpenEnumeration openEnum);
	
	/**
	 * Called when a <code>TLOpenEnumeration</code> instance has completed processing during example navigation.
	 * 
	 * @param openEnum  the model element to be visited
	 */
	public void endOpenEnumeration(TLOpenEnumeration openEnum);
	
	/**
	 * Called when a <code>TLRoleEnumeration</code> instance is first encountered during example navigation.
	 * 
	 * @param roleEnum  the model element to be visited
	 */
	public void startRoleEnumeration(TLRoleEnumeration roleEnum);
	
	/**
	 * Called when a <code>TLRoleEnumeration</code> instance has completed processing during example navigation.
	 * 
	 * @param roleEnum  the model element to be visited
	 */
	public void endRoleEnumeration(TLRoleEnumeration roleEnum);
	
	/**
	 * Called when a <code>TLValueWithAttributes</code> instance is first encountered during example navigation.
	 * 
	 * @param valueWithAttributes  the model element to be visited
	 */
	public void startValueWithAttributes(TLValueWithAttributes valueWithAttributes);
	
	/**
	 * Called when a <code>TLValueWithAttributes</code> instance has completed processing during example navigation.
	 * 
	 * @param valueWithAttributes  the model element to be visited
	 */
	public void endValueWithAttributes(TLValueWithAttributes valueWithAttributes);
	
	/**
	 * Called when a series of facet extension points is about to be inserted into the model.
	 * 
	 * @param facet  the facet to which the extension(s) apply
	 */
	public void startExtensionPoint(TLFacet facet);
	
	/**
	 * Called when the navigation of a series of facet extension points has been completed.
	 * 
	 * @param facet  the facet to which the extension(s) apply
	 */
	public void endExtensionPoint(TLFacet facet);
	
	/**
	 * Called when a <code>TLExtensionPointFacet</code> instance is first encountered during example navigation.
	 * 
	 * @param facet  the model element to be visited
	 */
	public void startExtensionPointFacet(TLExtensionPointFacet facet);
	
	/**
	 * Called when a <code>TLExtensionPointFacet</code> instance has completed processing during example navigation.
	 * 
	 * @param facet  the model element to be visited
	 */
	public void endExtensionPointFacet(TLExtensionPointFacet facet);
	
	/**
	 * Called when a <code>XSDComplexType</code> instance is first encountered during example navigation.
	 * 
	 * @param xsdComplexType  the model element to be visited
	 */
	public void startXsdComplexType(XSDComplexType xsdComplexType);
	
	/**
	 * Called when a <code>XSDComplexType</code> instance has completed processing during example navigation.
	 * 
	 * @param xsdComplexType  the model element to be visited
	 */
	public void endXsdComplexType(XSDComplexType xsdComplexType);
	
	/**
	 * Called when a <code>XSDElement</code> instance is first encountered during example navigation.
	 * 
	 * @param xsdElement  the model element to be visited
	 */
	public void startXsdElement(XSDElement xsdElement);
	
	/**
	 * Called when a <code>XSDElement</code> instance has completed processing during example navigation.
	 * 
	 * @param xsdElement  the model element to be visited
	 */
	public void endXsdElement(XSDElement xsdElement);
	
}
