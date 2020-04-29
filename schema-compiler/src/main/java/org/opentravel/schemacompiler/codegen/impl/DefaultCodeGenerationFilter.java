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

package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.XSDLibrary;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of the <code>CodeGenerationFilter</code> interface.
 * 
 * @author S. Livezey
 */
public class DefaultCodeGenerationFilter implements CodeGenerationFilter {

    private Set<LibraryMetaInfo> allowedLibraries = new HashSet<>();
    private Set<LibraryMetaInfo> extensionLibraries = new HashSet<>();
    private Set<EntityMetaInfo> allowedEntities = new HashSet<>();
    private Set<LibraryElement> allowedElements = new HashSet<>();

    /**
     * Adds the given library to the list of libraries that will be allowed by this filter.
     * 
     * @param library the library to allow
     */
    public void addProcessedLibrary(AbstractLibrary library) {
        if (library != null) {
            LibraryMetaInfo libraryInfo = new LibraryMetaInfo( library );

            if (!allowedLibraries.contains( libraryInfo )) {
                allowedLibraries.add( libraryInfo );
            }
        }
    }

    /**
     * Adds the given library to the list of schema extensions that will be allowed by this filter.
     * 
     * @param legacySchema the legacy schema for which an extension will be required
     */
    public void addExtensionLibrary(XSDLibrary legacySchema) {
        if (legacySchema != null) {
            LibraryMetaInfo libraryInfo = new LibraryMetaInfo( legacySchema );

            if (!extensionLibraries.contains( libraryInfo )) {
                extensionLibraries.add( libraryInfo );
            }
        }
    }

    /**
     * Adds the given library element to the list of entities that will be allowed by this filter.
     * 
     * @param entity the library element to allow
     */
    public void addProcessedElement(LibraryElement entity) {
        if (entity != null) {
            if (entity instanceof NamedEntity) {
                EntityMetaInfo entityInfo = new EntityMetaInfo( (NamedEntity) entity );

                if (!allowedEntities.contains( entityInfo )) {
                    allowedEntities.add( entityInfo );
                }

            } else {
                allowedElements.add( entity );
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processLibrary(org.opentravel.schemacompiler.model.AbstractLibrary)
     */
    @Override
    public boolean processLibrary(AbstractLibrary library) {
        return (library == null) ? false : allowedLibraries.contains( new LibraryMetaInfo( library ) );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processExtendedLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
     */
    @Override
    public boolean processExtendedLibrary(XSDLibrary legacySchema) {
        return (legacySchema == null) ? false : extensionLibraries.contains( new LibraryMetaInfo( legacySchema ) );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processEntity(org.opentravel.schemacompiler.model.LibraryElement)
     */
    @Override
    public boolean processEntity(LibraryElement entity) {
        boolean result;

        if (entity instanceof NamedEntity) {
            result = allowedEntities.contains( new EntityMetaInfo( (NamedEntity) entity ) );
        } else {
            result = allowedElements.contains( entity );
        }
        return result;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#addBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
     */
    @Override
    public void addBuiltInLibrary(BuiltInLibrary library) {
        addProcessedLibrary( library );
    }

    /**
     * Displays the contents of this filter to standard output for debugging purposes.
     */
    @SuppressWarnings("squid:S106") // Invalid Sonar finding since this method is only used for console debugging
    public void display() {
        display( System.out );
    }

    /**
     * Displays the contents of this filter for debugging purposes.
     * 
     * @param out the stream to which debugging output should be directed
     */
    public void display(PrintStream out) {
        out.println( "CODE GENERATION FILTER:" );

        for (LibraryMetaInfo l : allowedLibraries) {
            out.println( "  LIBRARY: " + l.getName() + " / " + l.getNamespace() );
        }
        for (EntityMetaInfo e : allowedEntities) {
            out.println( "  ENTITY : " + e.getIdentity() );
        }
        for (LibraryElement e : allowedElements) {
            out.println( "  ENTITY : " + e.getValidationIdentity() );
        }
    }

    /**
     * Provides a means of determining whether a library has been added to the filter without performing an
     * instance-level equality check.
     */
    private static class LibraryMetaInfo {

        private Class<?> libraryClass;
        private String namespace;
        private String name;

        /**
         * Constructor that creates a meta-info record for the given library.
         * 
         * @param library the library for which to create a meta-info record
         */
        public LibraryMetaInfo(AbstractLibrary library) {
            libraryClass = library.getClass();
            namespace = library.getNamespace();
            name = library.getName();
        }

        /**
         * Returns the namespace of the library.
         * 
         * @return String
         */
        public String getNamespace() {
            return namespace;
        }

        /**
         * Returns the name of the library.
         * 
         * @return String
         */
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            boolean isEqual = false;

            if (obj instanceof LibraryMetaInfo) {
                LibraryMetaInfo other = (LibraryMetaInfo) obj;

                isEqual = this.libraryClass.equals( other.libraryClass ) && isEqual( this.namespace, other.namespace )
                    && isEqual( this.name, other.name );
            }
            return isEqual;
        }

        @Override
        public int hashCode() {
            return (namespace == null) ? 0 : namespace.hashCode();
        }

    }

    /**
     * Provides a means of determining whether a <code>NamedEntity</code> has been added to the filter without
     * performing an instance-level equality check.
     */
    private static class EntityMetaInfo {

        private Class<?> entityClass;
        private String namespace;
        private String localName;
        private String identity;

        /**
         * Constructor that creates a meta-info record for the given library.
         * 
         * @param library the library for which to create a meta-info record
         */
        public EntityMetaInfo(NamedEntity entity) {
            entityClass = entity.getClass();
            namespace = entity.getNamespace();
            localName = entity.getLocalName();
            identity = entity.getValidationIdentity();
        }

        /**
         * Returns an identity name for the entity.
         * 
         * @return String
         */
        public String getIdentity() {
            return identity;
        }

        @Override
        public boolean equals(Object obj) {
            boolean isEqual = false;

            if (obj instanceof EntityMetaInfo) {
                EntityMetaInfo other = (EntityMetaInfo) obj;

                isEqual = this.entityClass.equals( other.entityClass ) && isEqual( this.namespace, other.namespace )
                    && isEqual( this.localName, other.localName );
            }
            return isEqual;
        }

        @Override
        public int hashCode() {
            return (namespace == null) ? 0 : namespace.hashCode();
        }

    }

    /**
     * Determines the equality of two strings, including null checks.
     * 
     * @param str1 the first string to be compared
     * @param str2 the second string to be compared
     * @return boolean
     */
    private static boolean isEqual(String str1, String str2) {
        return (str1 == null) ? (str2 == null) : str1.equals( str2 );
    }

}
