
package org.opentravel.schemacompiler.repository.impl;

import java.util.Comparator;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.version.Versioned;

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
	 * @see org.opentravel.schemacompiler.version.Versioned#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return item.getNamespace();
	}

	/**
	 * @see org.opentravel.schemacompiler.version.Versioned#getBaseNamespace()
	 */
	@Override
	public String getBaseNamespace() {
		return item.getBaseNamespace();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.Versioned#isLaterVersion(org.opentravel.schemacompiler.version.Versioned)
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
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
		return item.getLibraryName();
	}

	/**
	 * @see org.opentravel.schemacompiler.version.Versioned#getVersion()
	 */
	@Override
	public String getVersion() {
		return item.getVersion();
	}

	/**
	 * @see org.opentravel.schemacompiler.version.Versioned#getVersionScheme()
	 */
	@Override
	public String getVersionScheme() {
		return versionScheme;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return null;
	}


	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement()
	 */
	@Override
	public LibraryElement cloneElement() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement(org.opentravel.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public LibraryElement cloneElement(AbstractLibrary namingContext) {
		return null;
	}
}
