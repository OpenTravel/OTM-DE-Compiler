/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.ic;

import java.util.Collection;
import java.util.HashSet;

import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDComplexType;
import com.sabre.schemacompiler.model.XSDElement;
import com.sabre.schemacompiler.model.XSDSimpleType;
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Visitor used to collect all of the named entities contained in the library that was
 * removed from the model.
 *
 * @author S. Livezey
 */
public class ModelElementCollector extends ModelElementVisitorAdapter {
	
	private Collection<NamedEntity> libraryEntities = new HashSet<NamedEntity>();
	
	/**
	 * Returns the list of named entities that were collected during library navigation.
	 * 
	 * @return Collection<NamedEntity>
	 */
	public Collection<NamedEntity> getLibraryEntities() {
		return libraryEntities;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(com.sabre.schemacompiler.model.TLSimple)
	 */
	@Override
	public boolean visitSimple(TLSimple simple) {
		libraryEntities.add(simple);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		libraryEntities.add(valueWithAttributes);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(com.sabre.schemacompiler.model.TLClosedEnumeration)
	 */
	@Override
	public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
		libraryEntities.add(enumeration);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitOpenEnumeration(com.sabre.schemacompiler.model.TLOpenEnumeration)
	 */
	@Override
	public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
		libraryEntities.add(enumeration);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(com.sabre.schemacompiler.model.TLCoreObject)
	 */
	@Override
	public boolean visitCoreObject(TLCoreObject coreObject) {
		libraryEntities.add(coreObject);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(com.sabre.schemacompiler.model.TLBusinessObject)
	 */
	@Override
	public boolean visitBusinessObject(TLBusinessObject businessObject) {
		libraryEntities.add(businessObject);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(com.sabre.schemacompiler.model.TLOperation)
	 */
	@Override
	public boolean visitOperation(TLOperation operation) {
		libraryEntities.add(operation);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDSimpleType(com.sabre.schemacompiler.model.XSDSimpleType)
	 */
	@Override
	public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
		libraryEntities.add(xsdSimple);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDComplexType(com.sabre.schemacompiler.model.XSDComplexType)
	 */
	@Override
	public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
		libraryEntities.add(xsdComplex);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDElement(com.sabre.schemacompiler.model.XSDElement)
	 */
	@Override
	public boolean visitXSDElement(XSDElement xsdElement) {
		libraryEntities.add(xsdElement);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(com.sabre.schemacompiler.model.TLFacet)
	 */
	@Override
	public boolean visitFacet(TLFacet facet) {
		libraryEntities.add(facet);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
	 */
	@Override
	public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
		libraryEntities.add(simpleFacet);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitListFacet(com.sabre.schemacompiler.model.TLListFacet)
	 */
	@Override
	public boolean visitListFacet(TLListFacet listFacet) {
		libraryEntities.add(listFacet);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitAlias(com.sabre.schemacompiler.model.TLAlias)
	 */
	@Override
	public boolean visitAlias(TLAlias alias) {
		libraryEntities.add(alias);
		return true;
	}
	
}
