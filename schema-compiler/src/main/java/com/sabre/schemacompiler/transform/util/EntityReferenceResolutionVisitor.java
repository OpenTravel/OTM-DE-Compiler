/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.util;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLAttributeType;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLPropertyType;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.transform.SymbolResolver;
import com.sabre.schemacompiler.validate.impl.TLModelSymbolResolver;
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Visitor that attempts to resolve null entity references discovered in a model after
 * new libraries are loaded.
 * 
 * @author S. Livezey
 */
public class EntityReferenceResolutionVisitor extends ModelElementVisitorAdapter {
	
	private SymbolResolver symbolResolver;
	
	/**
	 * Constructor that assigns the model being navigated.
	 * 
	 * @param model  the model from which all entity names will be obtained
	 */
	public EntityReferenceResolutionVisitor(TLModel model) {
		this( new TLModelSymbolResolver(model) );
	}
	
	/**
	 * Constructor that assigns the model being navigated.
	 * 
	 * @param symbolResolver  the symbol resolver to use for all named entity lookups
	 */
	public EntityReferenceResolutionVisitor(SymbolResolver symbolResolver) {
		this.symbolResolver = symbolResolver;
	}
	
	/**
	 * Assigns the context library for the symbol resolver.
	 * 
	 * @param library  the library to assign as the current lookup context
	 */
	public void assignContextLibrary(AbstractLibrary library) {
		symbolResolver.setPrefixResolver( new LibraryPrefixResolver(library) );
		symbolResolver.setAnonymousEntityFilter( new ChameleonFilter(library) );
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitBuiltInLibrary(com.sabre.schemacompiler.model.BuiltInLibrary)
	 */
	@Override
	public boolean visitBuiltInLibrary(BuiltInLibrary library) {
		assignContextLibrary(library);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(com.sabre.schemacompiler.model.XSDLibrary)
	 */
	@Override
	public boolean visitLegacySchemaLibrary(XSDLibrary library) {
		assignContextLibrary(library);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitUserDefinedLibrary(com.sabre.schemacompiler.model.TLLibrary)
	 */
	@Override
	public boolean visitUserDefinedLibrary(TLLibrary library) {
		assignContextLibrary(library);
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(com.sabre.schemacompiler.model.TLSimple)
	 */
	@Override
	public boolean visitSimple(TLSimple simple) {
		if ((simple.getParentType() == null) && (simple.getParentTypeName() != null)) {
			Object ref = symbolResolver.resolveEntity(simple.getParentTypeName());
			
			if (ref instanceof TLAttributeType) {
				simple.setParentType((TLAttributeType) ref);
			}
		}
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		if ((valueWithAttributes.getParentType() == null) && (valueWithAttributes.getParentTypeName() != null)) {
			Object ref = symbolResolver.resolveEntity(valueWithAttributes.getParentTypeName());
			
			if (ref instanceof TLAttributeType) {
				valueWithAttributes.setParentType((TLAttributeType) ref);
			}
		}
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(com.sabre.schemacompiler.model.TLExtension)
	 */
	@Override
	public boolean visitExtension(TLExtension extension) {
		if ((extension.getExtendsEntity() == null) && (extension.getExtendsEntityName() != null)) {
			Object ref;
			
			if (extension.getOwner() instanceof TLOperation) {
				ref = symbolResolver.resolveOperationEntity(extension.getExtendsEntityName());
				
			} else {
				ref = symbolResolver.resolveEntity(extension.getExtendsEntityName());
			}
			
			if (ref instanceof NamedEntity) {
				extension.setExtendsEntity((NamedEntity) ref);
			}
		}
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
	 */
	@Override
	public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
		if ((simpleFacet.getSimpleType() == null) && (simpleFacet.getSimpleTypeName() != null)) {
			Object ref = symbolResolver.resolveEntity(simpleFacet.getSimpleTypeName());
			
			if (ref instanceof NamedEntity) {
				simpleFacet.setSimpleType((NamedEntity) ref);
			}
		}
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(com.sabre.schemacompiler.model.TLAttribute)
	 */
	@Override
	public boolean visitAttribute(TLAttribute attribute) {
		if ((attribute.getType() == null) && (attribute.getTypeName() != null)) {
			Object ref = symbolResolver.resolveEntity(attribute.getTypeName());
			
			if (ref instanceof TLAttributeType) {
				attribute.setType((TLAttributeType) ref);
			}
		}
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(com.sabre.schemacompiler.model.TLProperty)
	 */
	@Override
	public boolean visitElement(TLProperty element) {
		if ((element.getType() == null) && (element.getTypeName() != null)) {
			Object ref = symbolResolver.resolveEntity(element.getTypeName());
			
			if (ref instanceof TLPropertyType) {
				element.setType((TLPropertyType) ref);
			}
		}
		return true;
	}
	
}
