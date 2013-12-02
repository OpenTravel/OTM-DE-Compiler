/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.ioc;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLModel;

/**
 * Adapter that allows a <code>SchemaDependency</code> object to behave as an
 * OTA2.0 <code>NamedEntity</code>.
 *
 * @author S. Livezey
 */
public class SchemaDependencyNamedEntityAdapter implements NamedEntity {
	
	private SchemaDependency schemaDependency;
	
	public SchemaDependencyNamedEntityAdapter(SchemaDependency schemaDependency) {
		this.schemaDependency = schemaDependency;
	}

	/**
	 * @see com.sabre.schemacompiler.model.NamedEntity#getNamespace()
	 */
	public String getNamespace() {
		return schemaDependency.getSchemaDeclaration().getNamespace();
	}

	/**
	 * @see com.sabre.schemacompiler.model.NamedEntity#getLocalName()
	 */
	public String getLocalName() {
		return schemaDependency.getLocalName();
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.ModelElement#getOwningModel()
	 */
	public TLModel getOwningModel() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	public AbstractLibrary getOwningLibrary() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	public String getValidationIdentity() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#cloneElement()
	 */
	public LibraryElement cloneElement() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#cloneElement(com.sabre.schemacompiler.model.AbstractLibrary)
	 */
	public LibraryElement cloneElement(AbstractLibrary namingContext) {
		return null;
	}

}
