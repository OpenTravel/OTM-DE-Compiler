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
 * Adapter class that provides empty implementations for all methods of the
 * <code>ModelElementVisitor</code> interface.
 * 
 * @author S. Livezey
 */
public class ModelElementVisitorAdapter implements ModelElementVisitor {
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitBuiltInLibrary(com.sabre.schemacompiler.model.BuiltInLibrary)
	 */
	@Override
	public boolean visitBuiltInLibrary(BuiltInLibrary library) {
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitLegacySchemaLibrary(com.sabre.schemacompiler.model.XSDLibrary)
	 */
	@Override
	public boolean visitLegacySchemaLibrary(XSDLibrary library) {
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitUserDefinedLibrary(com.sabre.schemacompiler.model.TLLibrary)
	 */
	@Override
	public boolean visitUserDefinedLibrary(TLLibrary library) {
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitContext(com.sabre.schemacompiler.model.TLContext)
	 */
	@Override
	public boolean visitContext(TLContext context) {
		return false;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitSimple(com.sabre.schemacompiler.model.TLSimple)
	 */
	@Override
	public boolean visitSimple(TLSimple simple) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitClosedEnumeration(com.sabre.schemacompiler.model.TLClosedEnumeration)
	 */
	@Override
	public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitOpenEnumeration(com.sabre.schemacompiler.model.TLOpenEnumeration)
	 */
	@Override
	public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitEnumValue(com.sabre.schemacompiler.model.TLEnumValue)
	 */
	@Override
	public boolean visitEnumValue(TLEnumValue enumValue) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitCoreObject(com.sabre.schemacompiler.model.TLCoreObject)
	 */
	@Override
	public boolean visitCoreObject(TLCoreObject coreObject) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitRole(com.sabre.schemacompiler.model.TLRole)
	 */
	@Override
	public boolean visitRole(TLRole role) {
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitBusinessObject(com.sabre.schemacompiler.model.TLBusinessObject)
	 */
	@Override
	public boolean visitBusinessObject(TLBusinessObject businessObject) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitService(com.sabre.schemacompiler.model.TLService)
	 */
	@Override
	public boolean visitService(TLService service) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitOperation(com.sabre.schemacompiler.model.TLOperation)
	 */
	@Override
	public boolean visitOperation(TLOperation operation) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitExtensionPointFacet(com.sabre.schemacompiler.model.TLExtensionPointFacet)
	 */
	@Override
	public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitXSDSimpleType(com.sabre.schemacompiler.model.XSDSimpleType)
	 */
	@Override
	public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitXSDComplexType(com.sabre.schemacompiler.model.XSDComplexType)
	 */
	@Override
	public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitXSDElement(com.sabre.schemacompiler.model.XSDElement)
	 */
	@Override
	public boolean visitXSDElement(XSDElement xsdElement) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitFacet(com.sabre.schemacompiler.model.TLFacet)
	 */
	@Override
	public boolean visitFacet(TLFacet facet) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
	 */
	@Override
	public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitListFacet(com.sabre.schemacompiler.model.TLListFacet)
	 */
	@Override
	public boolean visitListFacet(TLListFacet listFacet) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitAlias(com.sabre.schemacompiler.model.TLAlias)
	 */
	@Override
	public boolean visitAlias(TLAlias alias) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitAttribute(com.sabre.schemacompiler.model.TLAttribute)
	 */
	@Override
	public boolean visitAttribute(TLAttribute attribute) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitElement(com.sabre.schemacompiler.model.TLProperty)
	 */
	@Override
	public boolean visitElement(TLProperty element) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitIndicator(com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public boolean visitIndicator(TLIndicator indicator) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitExtension(com.sabre.schemacompiler.model.TLExtension)
	 */
	@Override
	public boolean visitExtension(TLExtension extension) {
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitNamespaceImport(com.sabre.schemacompiler.model.TLNamespaceImport)
	 */
	@Override
	public boolean visitNamespaceImport(TLNamespaceImport nsImport) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitInclude(com.sabre.schemacompiler.model.TLInclude)
	 */
	@Override
	public boolean visitInclude(TLInclude include) {
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitEquivalent(com.sabre.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public boolean visitEquivalent(TLEquivalent equivalent) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitExample(com.sabre.schemacompiler.model.TLExample)
	 */
	@Override
	public boolean visitExample(TLExample example) {
		return false;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitDocumentation(com.sabre.schemacompiler.model.TLDocumentation)
	 */
	@Override
	public boolean visitDocumentation(TLDocumentation documentation) {
		return true;
	}

}
