
package org.opentravel.schemacompiler.ic;

import java.util.Collection;
import java.util.HashSet;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

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
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
	 */
	@Override
	public boolean visitSimple(TLSimple simple) {
		libraryEntities.add(simple);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		libraryEntities.add(valueWithAttributes);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(org.opentravel.schemacompiler.model.TLClosedEnumeration)
	 */
	@Override
	public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
		libraryEntities.add(enumeration);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
	 */
	@Override
	public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
		libraryEntities.add(enumeration);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(org.opentravel.schemacompiler.model.TLCoreObject)
	 */
	@Override
	public boolean visitCoreObject(TLCoreObject coreObject) {
		libraryEntities.add(coreObject);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(org.opentravel.schemacompiler.model.TLBusinessObject)
	 */
	@Override
	public boolean visitBusinessObject(TLBusinessObject businessObject) {
		libraryEntities.add(businessObject);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(org.opentravel.schemacompiler.model.TLOperation)
	 */
	@Override
	public boolean visitOperation(TLOperation operation) {
		libraryEntities.add(operation);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDSimpleType(org.opentravel.schemacompiler.model.XSDSimpleType)
	 */
	@Override
	public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
		libraryEntities.add(xsdSimple);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
	 */
	@Override
	public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
		libraryEntities.add(xsdComplex);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDElement(org.opentravel.schemacompiler.model.XSDElement)
	 */
	@Override
	public boolean visitXSDElement(XSDElement xsdElement) {
		libraryEntities.add(xsdElement);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
	 */
	@Override
	public boolean visitFacet(TLFacet facet) {
		libraryEntities.add(facet);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
	 */
	@Override
	public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
		libraryEntities.add(simpleFacet);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitListFacet(org.opentravel.schemacompiler.model.TLListFacet)
	 */
	@Override
	public boolean visitListFacet(TLListFacet listFacet) {
		libraryEntities.add(listFacet);
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAlias(org.opentravel.schemacompiler.model.TLAlias)
	 */
	@Override
	public boolean visitAlias(TLAlias alias) {
		libraryEntities.add(alias);
		return true;
	}
	
}
