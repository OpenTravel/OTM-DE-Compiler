/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository.impl;

import java.util.Comparator;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.version.VersionScheme;
import com.sabre.schemacompiler.version.VersionSchemeException;
import com.sabre.schemacompiler.version.VersionSchemeFactory;
import com.sabre.schemacompiler.version.Versioned;

/**
 * Wrapper used to sort repository items in versioned order.
 * 
 * @author S. Livezey
 */
public class RepositoryItemVersionedWrapper implements Versioned {
	
	private String versionScheme = VersionSchemeFactory.getInstance().getDefaultVersionScheme();
	private RepositoryItem item;
	
	/**
	 * Constructor that provides the repository item to be wrapped.
	 * 
	 * @param item  the repository item to be wrapped by this instance
	 */
	public RepositoryItemVersionedWrapper(RepositoryItem item) {
		this.item = item;
	}
	
	/**
	 * Returns the repository item that is wrapped by this instance.
	 * 
	 * @return RepositoryItem
	 */
	public RepositoryItem getItem() {
		return item;
	}

	/**
	 * @see com.sabre.schemacompiler.version.Versioned#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return item.getNamespace();
	}

	/**
	 * @see com.sabre.schemacompiler.version.Versioned#getBaseNamespace()
	 */
	@Override
	public String getBaseNamespace() {
		return item.getBaseNamespace();
	}
	
	/**
	 * @see com.sabre.schemacompiler.version.Versioned#isLaterVersion(com.sabre.schemacompiler.version.Versioned)
	 */
	@Override
	public boolean isLaterVersion(Versioned otherVersionedItem) {
		boolean result = false;
		
		if ((versionScheme != null) && (otherVersionedItem instanceof RepositoryItemVersionedWrapper)) {
			RepositoryItemVersionedWrapper otherRepositoryItem = (RepositoryItemVersionedWrapper) otherVersionedItem;
			
			if (versionScheme.equals(otherRepositoryItem.getVersionScheme())) {
				try {
					VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme(versionScheme);
					String thisBaseNamespace = getBaseNamespace();
					
					if ((vScheme != null) && (thisBaseNamespace != null)
							&& thisBaseNamespace.equals(otherRepositoryItem.getBaseNamespace())) {
						Comparator<Versioned> comparator = vScheme.getComparator(false);
						
						result = (comparator.compare(otherRepositoryItem, this) > 0);
					}
				} catch (VersionSchemeException e) {
					// No error - ignore and return false
				}
			}
		}
		return result;
	}

	/**
	 * @see com.sabre.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
		return item.getLibraryName();
	}

	/**
	 * @see com.sabre.schemacompiler.version.Versioned#getVersion()
	 */
	@Override
	public String getVersion() {
		return item.getVersion();
	}

	/**
	 * @see com.sabre.schemacompiler.version.Versioned#getVersionScheme()
	 */
	@Override
	public String getVersionScheme() {
		return versionScheme;
	}

	/**
	 * @see com.sabre.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return null;
	}


	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#cloneElement()
	 */
	@Override
	public LibraryElement cloneElement() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#cloneElement(com.sabre.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public LibraryElement cloneElement(AbstractLibrary namingContext) {
		return null;
	}
}
