/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemacompiler.version;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;

/**
 * Comparator that can be used to sort <code>TLLibrary</code> instances according to their version
 * identifiers.
 * 
 * <p>
 * NOTE: At this time, sorting lists of libraries assigned to multiple version schemes is not
 * supported.
 * 
 * @author S. Livezey
 */
public class LibraryVersionComparator implements Comparator<TLLibrary> {

    private Comparator<Versioned> versionComparator;

    /**
     * Constructor that provides the version scheme under which the librarys' versions will be
     * compared.
     * 
     * @param sortAscending
     *            indicates the direction of the sort produced by the comparator (true = ascending,
     *            false = descending)
     * @param versionScheme
     *            the version scheme from which a version comparator should be obtained
     */
    public LibraryVersionComparator(VersionScheme versionScheme, boolean sortAscending) {
        this(versionScheme.getComparator(sortAscending));
    }

    /**
     * Constructor that provides the comparator for the librarys' version identifiers.
     * 
     * @param versionComparator
     *            the version comparator to use when comparing version identifier strings
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
            result = versionComparator.compare(new LibraryVersionedDecorator(library1),
                    new LibraryVersionedDecorator(library2));
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
         * @param library
         *            the user-defined library instance
         */
        private LibraryVersionedDecorator(TLLibrary library) {
            this.library = library;
        }

        /**
         * @see org.opentravel.schemacompiler.version.Versioned#getVersion()
         */
        @Override
        public String getVersion() {
            return library.getVersion();
        }

        /**
         * @see org.opentravel.schemacompiler.version.Versioned#getVersionScheme()
         */
        @Override
        public String getVersionScheme() {
            return library.getVersionScheme();
        }

        /**
         * @see org.opentravel.schemacompiler.version.Versioned#getNamespace()
         */
        @Override
        public String getNamespace() {
            return library.getNamespace();
        }

        /**
         * @see org.opentravel.schemacompiler.version.Versioned#getBaseNamespace()
         */
        @Override
        public String getBaseNamespace() {
            return library.getBaseNamespace();
        }

        /**
         * @see org.opentravel.schemacompiler.version.Versioned#isLaterVersion(org.opentravel.schemacompiler.version.Versioned)
         */
        @Override
        public boolean isLaterVersion(Versioned otherVersionedItem) {
            return ((library == null) || (otherVersionedItem == null)) ? false : library
                    .isLaterVersion(otherVersionedItem.getOwningLibrary());
        }

        /**
         * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
         */
        @Override
        public String getLocalName() {
            return null;
        }

        /**
         * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
         */
        @Override
        public AbstractLibrary getOwningLibrary() {
            return library;
        }

        /**
         * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
         */
        @Override
        public TLModel getOwningModel() {
            return library.getOwningModel();
        }

        /**
         * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
         */
        @Override
        public String getValidationIdentity() {
            return library.getValidationIdentity();
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

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#addListener(org.opentravel.schemacompiler.event.ModelElementListener)
		 */
		@Override
		public void addListener(ModelElementListener listener) {
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#removeListener(org.opentravel.schemacompiler.event.ModelElementListener)
		 */
		@Override
		public void removeListener(ModelElementListener listener) {
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#getListeners()
		 */
		@Override
		public Collection<ModelElementListener> getListeners() {
			return Collections.emptyList();
		}

    }

}
