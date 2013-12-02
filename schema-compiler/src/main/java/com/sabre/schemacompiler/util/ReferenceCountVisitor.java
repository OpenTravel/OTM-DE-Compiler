/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.util;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;

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
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitBuiltInLibrary(com.sabre.schemacompiler.model.BuiltInLibrary)
	 */
	@Override
	public boolean visitBuiltInLibrary(BuiltInLibrary library) {
		return (library != targetLibrary);
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(com.sabre.schemacompiler.model.XSDLibrary)
	 */
	@Override
	public boolean visitLegacySchemaLibrary(XSDLibrary library) {
		return (library != targetLibrary);
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitUserDefinedLibrary(com.sabre.schemacompiler.model.TLLibrary)
	 */
	@Override
	public boolean visitUserDefinedLibrary(TLLibrary library) {
		return (library != targetLibrary);
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(com.sabre.schemacompiler.model.TLSimple)
	 */
	@Override
	public boolean visitSimple(TLSimple simple) {
		countReference(simple.getParentType());
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		countReference(valueWithAttributes.getParentType());
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(com.sabre.schemacompiler.model.TLExtension)
	 */
	@Override
	public boolean visitExtension(TLExtension extension) {
		if (extension.getExtendsEntity() != null) {
			countReference(extension.getExtendsEntity());
		}
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
	 */
	@Override
	public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
		countReference(simpleFacet.getSimpleType());
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(com.sabre.schemacompiler.model.TLAttribute)
	 */
	@Override
	public boolean visitAttribute(TLAttribute attribute) {
		countReference(attribute.getType());
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(com.sabre.schemacompiler.model.TLProperty)
	 */
	@Override
	public boolean visitElement(TLProperty element) {
		countReference(element.getType());
		return true;
	}
	
}
