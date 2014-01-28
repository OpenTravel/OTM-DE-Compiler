/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.util;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Visitor implementation that counts the number of references to a library's members from from model
 * entities that are not defined in that same library.
 * 
 * @author S. Livezey
 */
public class ReferenceCountVisitor extends ModelElementVisitorAdapter {
	
	private AbstractLibrary targetLibrary;
	private int referenceCount = 0;
	
	/**
	 * Constructor that assigns the target library for which references should be counted.
	 * 
	 * @param targetLibrary  the library whose references are to be counted
	 */
	public ReferenceCountVisitor(AbstractLibrary targetLibrary) {
		this.targetLibrary = targetLibrary;
	}
	
	/**
	 * Returns the total reference count after model traversal has been completed.
	 * 
	 * @return int
	 */
	public int getReferenceCount() {
		return referenceCount;
	}
	
	/**
	 * If the given entity is a member of the target library, the reference count value is incremented
	 * by one.
	 * 
	 * @param referencedEntity  the referenced entity to count
	 */
	protected void countReference(NamedEntity referencedEntity) {
		if ((referencedEntity != null) && (referencedEntity.getOwningLibrary() == targetLibrary)) {
			referenceCount++;
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
	 */
	@Override
	public boolean visitBuiltInLibrary(BuiltInLibrary library) {
		return (library != targetLibrary);
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
	 */
	@Override
	public boolean visitLegacySchemaLibrary(XSDLibrary library) {
		return (library != targetLibrary);
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitUserDefinedLibrary(org.opentravel.schemacompiler.model.TLLibrary)
	 */
	@Override
	public boolean visitUserDefinedLibrary(TLLibrary library) {
		return (library != targetLibrary);
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
	 */
	@Override
	public boolean visitSimple(TLSimple simple) {
		countReference(simple.getParentType());
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		countReference(valueWithAttributes.getParentType());
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
	 */
	@Override
	public boolean visitExtension(TLExtension extension) {
		if (extension.getExtendsEntity() != null) {
			countReference(extension.getExtendsEntity());
		}
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
	 */
	@Override
	public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
		countReference(simpleFacet.getSimpleType());
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
	 */
	@Override
	public boolean visitAttribute(TLAttribute attribute) {
		countReference(attribute.getType());
		return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
	 */
	@Override
	public boolean visitElement(TLProperty element) {
		countReference(element.getType());
		return true;
	}
	
}
