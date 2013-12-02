/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.visitor;

import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLInclude;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLNamespaceImport;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDComplexType;
import com.sabre.schemacompiler.model.XSDElement;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.model.XSDSimpleType;

/**
 * Interface to be implemented by components that will be notified via callback when model
 * elements are encountered during navigation.
 * 
 * @author S. Livezey
 */
public interface ModelElementVisitor {
	
	/**
	 * Called when a <code>BuiltInLibrary</code> instance is encountered during model navigation.
	 * 
	 * @param library  the library to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitBuiltInLibrary(BuiltInLibrary library);
	
	/**
	 * Called when a <code>XSDLibrary</code> instance is encountered during model navigation.
	 * 
	 * @param library  the library to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitLegacySchemaLibrary(XSDLibrary library);
	
	/**
	 * Called when a <code>TLLibrary</code> instance is encountered during model navigation.
	 * 
	 * @param library  the library to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitUserDefinedLibrary(TLLibrary library);
	
	/**
	 * Called when a <code>TLContext</code> instance is encountered during model navigation.
	 * 
	 * @param context  the simple context to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitContext(TLContext context);
	
	/**
	 * Called when a <code>TLSimple</code> instance is encountered during model navigation.
	 * 
	 * @param simple  the simple entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitSimple(TLSimple simple);
	
	/**
	 * Called when a <code>TLValueWithAttributes</code> instance is encountered during model navigation.
	 * 
	 * @param valueWithAttributes  the simple entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes);
	
	/**
	 * Called when a <code>TLClosedEnumeration</code> instance is encountered during model navigation.
	 * 
	 * @param enumeration  the enumeration entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitClosedEnumeration(TLClosedEnumeration enumeration);
	
	/**
	 * Called when a <code>TLOpenEnumeration</code> instance is encountered during model navigation.
	 * 
	 * @param enumeration  the enumeration entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitOpenEnumeration(TLOpenEnumeration enumeration);
	
	/**
	 * Called when a <code>TLEnumValue</code> instance is encountered during model navigation.
	 * 
	 * @param enumValue  the enumeration value to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitEnumValue(TLEnumValue enumValue);
	
	/**
	 * Called when a <code>TLCoreObject</code> instance is encountered during model navigation.
	 * 
	 * @param coreObject  the core object entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitCoreObject(TLCoreObject coreObject);
	
	/**
	 * Called when a <code>TLRole</code> instance is encountered during model navigation.
	 * 
	 * @param role  the core object role to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitRole(TLRole role);
	
	/**
	 * Called when a <code>TLBusinessObject</code> instance is encountered during model navigation.
	 * 
	 * @param businessObject  the business object entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitBusinessObject(TLBusinessObject businessObject);
	
	/**
	 * Called when a <code>TLService</code> instance is encountered during model navigation.
	 * 
	 * @param service  the service entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitService(TLService service);
	
	/**
	 * Called when a <code>TLOperation</code> instance is encountered during model navigation.
	 * 
	 * @param operation  the operation entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitOperation(TLOperation operation);
	
	/**
	 * Called when a <code>TLExtensionPointFacet</code> instance is encountered during model navigation.
	 * 
	 * @param service  the service entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet);
	
	/**
	 * Called when a <code>XSDSimpleType</code> instance is encountered during model navigation.
	 * 
	 * @param xsdSimple  the XSD simple-type entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitXSDSimpleType(XSDSimpleType xsdSimple);
	
	/**
	 * Called when a <code>XSDComplexType</code> instance is encountered during model navigation.
	 * 
	 * @param xsdComplex  the XSD complex-type entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitXSDComplexType(XSDComplexType xsdComplex);
	
	/**
	 * Called when a <code>XSDElement</code> instance is encountered during model navigation.
	 * 
	 * @param xsdElement  the XSD element entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitXSDElement(XSDElement xsdElement);
	
	/**
	 * Called when a <code>TLFacet</code> instance is encountered during model navigation.
	 * 
	 * @param facet  the facet entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitFacet(TLFacet facet);
	
	/**
	 * Called when a <code>TLSimpleFacet</code> instance is encountered during model navigation.
	 * 
	 * @param simpleFacet  the simple facet entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitSimpleFacet(TLSimpleFacet simpleFacet);
	
	/**
	 * Called when a <code>TLListFacet</code> instance is encountered during model navigation.
	 * 
	 * @param listFacet  the list facet entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitListFacet(TLListFacet listFacet);
	
	/**
	 * Called when a <code>TLAlias</code> instance is encountered during model navigation.
	 * 
	 * @param alias  the alias entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitAlias(TLAlias alias);
	
	/**
	 * Called when a <code>TLAttribute</code> instance is encountered during model navigation.
	 * 
	 * @param attribute  the attribute entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitAttribute(TLAttribute attribute);
	
	/**
	 * Called when a <code>TLProperty</code> instance is encountered during model navigation.
	 * 
	 * @param element  the element entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitElement(TLProperty element);
	
	/**
	 * Called when a <code>TLIndicator</code> instance is encountered during model navigation.
	 * 
	 * @param indicator  the indicator entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitIndicator(TLIndicator indicator);
	
	/**
	 * Called when a <code>TLExtension</code> instance is encountered during model navigation.
	 * 
	 * @param extension  the extension entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitExtension(TLExtension extension);
	
	/**
	 * Called when a <code>TLNamespaceImport</code> instance is encountered during model navigation.
	 * 
	 * @param nsImport  the namespace import entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitNamespaceImport(TLNamespaceImport nsImport);
	
	/**
	 * Called when a <code>TLInclude</code> instance is encountered during model navigation.
	 * 
	 * @param include  the include entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitInclude(TLInclude include);
	
	/**
	 * Called when a <code>TLEquivalent</code> instance is encountered during model navigation.
	 * 
	 * @param equivalent  the equivalent entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitEquivalent(TLEquivalent equivalent);
	
	/**
	 * Called when a <code>TLExample</code> instance is encountered during model navigation.
	 * 
	 * @param example  the example entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitExample(TLExample example);
	
	/**
	 * Called when a <code>TLDocumentation</code> instance is encountered during model navigation.
	 * 
	 * @param documentation  the documentation entity to visit
	 * @return  boolean flag indicating whether to traverse child elements (if any exist)
	 */
	public boolean visitDocumentation(TLDocumentation documentation);
	
}
