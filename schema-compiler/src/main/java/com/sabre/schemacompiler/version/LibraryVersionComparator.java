/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.version;

import java.util.Comparator;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModel;

/**
 * Comparator that can be used to sort <code>TLLibrary</code> instances according to
 * their version identifiers.
 * 
 * <p>NOTE: At this time, sorting lists of libraries assigned to multiple version schemes
 * is not supported.
 * 
 * @author S. Livezey
 */
public class LibraryVersionComparator implements Comparator<TLLibrary> {
	
	private Comparator<Versioned> versionComparator;
	
	/**
	 * Constructor that provides the version scheme under which the librarys' versions will be
	 * compared.
	 * 
	 * @param sortAscending  indicates the direction of the sort produced by the comparator (true = ascending, false = descending)
	 * @param versionScheme  the version scheme from which a version comparator should be obtained
	 */
	public LibraryVersionComparator(VersionScheme versionScheme, boolean sortAscending) {
		this( versionScheme.getComparator(sortAscending) );
	}

	/**
	 * Constructor that provides the comparator for the librarys' version identifiers.
	 * 
	 * @param versionComparator  the version comparator to use when comparing version identifier strings
	 */
	public LibraryVersionComparator(Comparator<Versioned> versionComparator) {
		this.versionComparator = versionComparator;
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(TLLibrary library1, TLLibrary library2) {
		int result;
		
		if ((library1 == null) || (library2 == null)) {
			result = (library1 == null) ? ((library2 == null) ? 0 : 1) : -1;
		} else {
			result = versionComparator.compare(new LibraryVersionedDecorator(library1), new LibraryVersionedDecorator(library2));
		}
		return result;
	}
	
	/**
	 * Wrapper class used to apply the <code>Versioned</code> interface to a <code>TLLibrary</code>.
	 */
	private class LibraryVersionedDecorator implements Versioned {
		
		private TLLibrary library;
		
		/**
		 * Constructor that provides the <code>TLLibrary</code> instance to be decorated.
		 * 
		 * @param library  the user-defined library instance
		 */
		private LibraryVersionedDecorator(TLLibrary library) {
			this.library = library;
		}
		
		/**
		 * @see com.sabre.schemacompiler.version.Versioned#getVersion()
		 */
		@Override
		public String getVersion() {
			return library.getVersion();
		}

		/**
		 * @see com.sabre.schemacompiler.version.Versioned#getVersionScheme()
		 */
		@Override
		public String getVersionScheme() {
			return library.getVersionScheme();
		}

		/**
		 * @see com.sabre.schemacompiler.version.Versioned#getNamespace()
		 */
		@Override
		public String getNamespace() {
			return library.getNamespace();
		}

		/**
		 * @see com.sabre.schemacompiler.version.Versioned#getBaseNamespace()
		 */
		@Override
		public String getBaseNamespace() {
			return library.getBaseNamespace();
		}

		/**
		 * @see com.sabre.schemacompiler.version.Versioned#isLaterVersion(com.sabre.schemacompiler.version.Versioned)
		 */
		@Override
		public boolean isLaterVersion(Versioned otherVersionedItem) {
			return ((library == null) || (otherVersionedItem == null)) ?
					false : library.isLaterVersion(otherVersionedItem.getOwningLibrary());
		}

		/**
		 * @see com.sabre.schemacompiler.model.NamedEntity#getLocalName()
		 */
		@Override
		public String getLocalName() {
			return null;
		}

		/**
		 * @see com.sabre.schemacompiler.model.LibraryElement#getOwningLibrary()
		 */
		@Override
		public AbstractLibrary getOwningLibrary() {
			return library;
		}

		/**
		 * @see com.sabre.schemacompiler.model.ModelElement#getOwningModel()
		 */
		@Override
		public TLModel getOwningModel() {
			return library.getOwningModel();
		}

		/**
		 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
		 */
		@Override
		public String getValidationIdentity() {
			return library.getValidationIdentity();
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
	
}
