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

package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.SymbolTable;

/**
 * @author Eric.Bronson
 */
public class DocumentationBuilderFactory {

    private static SymbolTable table = new SymbolTable();

    private DocumentationBuilderFactory() {}

    /**
     * Returns the default singleton instance.
     * 
     * @return DocumentationBuilderFactory
     */
    public static DocumentationBuilderFactory getInstance() {
        return DocumentationManagerSingleton.INSTANCE;
    }

    private static class DocumentationManagerSingleton {
        private static final DocumentationBuilderFactory INSTANCE = new DocumentationBuilderFactory();
    }

    /**
     * Returns a documentation builder for the named entity provided.
     * 
     * @param element the named entity for which to return a builder
     * @return DocumentationBuilder
     */
    public DocumentationBuilder getDocumentationBuilder(final NamedEntity element) {
        String namespace = element.getNamespace();
        String localName = element.getLocalName();
        DocumentationBuilder builder = (DocumentationBuilder) table.getEntity( namespace, localName );

        if (null == builder) {
            if (element instanceof TLValueWithAttributes) {
                builder = new VWADocumentationBuilder( (TLValueWithAttributes) element );
            } else if (element instanceof TLSimple) {
                builder = new SimpleDocumentationBuilder( (TLSimple) element );
            } else if (element instanceof TLAbstractEnumeration) {
                builder = new EnumerationDocumentationBuilder( (TLAbstractEnumeration) element );
            } else if (element instanceof TLService) {
                builder = new ServiceDocumentationBuilder( (TLService) element );
            } else if (element instanceof TLOperation) {
                builder = new OperationDocumentationBuilder( (TLOperation) element );
            } else if (element instanceof TLFacet) {
                builder = new FacetDocumentationBuilder( (TLFacet) element );
            } else if (element instanceof TLBusinessObject) {
                builder = new BusinessObjectDocumentationBuilder( (TLBusinessObject) element );
            } else if (element instanceof TLCoreObject) {
                builder = new CoreObjectDocumentationBuilder( (TLCoreObject) element );
            } else if (element instanceof TLChoiceObject) {
                builder = new ChoiceObjectDocumentationBuilder( (TLChoiceObject) element );
            }
        }
        return builder;
    }

    /**
     * Returns a documentation builder for the named entity provided.
     * 
     * @param element the named entity for which to return a builder
     * @param prev the previous named entity in the list
     * @param next the next named entity in the list
     * @return DocumentationBuilder
     */
    public DocumentationBuilder getDocumentationBuilder(NamedEntity element, NamedEntity prev, NamedEntity next) {
        DocumentationBuilder builder = getDocumentationBuilder( element );
        DocumentationBuilder nextBuilder = null;
        DocumentationBuilder prevBuilder = null;
        if (prev != null) {
            prevBuilder = getDocumentationBuilder( prev );
        }
        if (next != null) {
            nextBuilder = getDocumentationBuilder( next );
        }
        builder.setNext( nextBuilder );
        builder.setPrevious( prevBuilder );
        return builder;
    }

    /**
     * Returns a documentation builder for the library provided.
     * 
     * @param lib the library for which to return a builder
     * @param prev the previous library in the list
     * @param next the next library in the list
     * @return DocumentationBuilder
     */
    public DocumentationBuilder getLibraryDocumentationBuilder(TLLibrary lib, TLLibrary prev, TLLibrary next) {
        DocumentationBuilder builder = getLibraryBuilder( lib );

        if (builder != null) {
            DocumentationBuilder prevBuilder = getLibraryBuilder( prev );
            DocumentationBuilder nextBuilder = getLibraryBuilder( next );

            builder.setNext( nextBuilder );
            builder.setPrevious( prevBuilder );
        }
        return builder;
    }

    /**
     * Returns a documentation builder for the library provided.
     * 
     * @param lib the library for which to return a builder
     * @return DocumentationBuilder
     */
    private DocumentationBuilder getLibraryBuilder(TLLibrary lib) {
        DocumentationBuilder builder = null;
        if (lib != null) {
            String name = lib.getName();
            String namespace = lib.getNamespace();
            builder = (DocumentationBuilder) table.getEntity( namespace, name );
            if (null == builder) {
                builder = new LibraryDocumentationBuilder( lib );
                table.addEntity( namespace, name, builder );
            }
        }
        return builder;
    }

    /**
     * Adds a documentation builder to the default factory instance.
     * 
     * @param builder the builder instance to add
     * @param namespace the namespace of the builder's entity
     * @param localName the local name of the builder's entity
     */
    public static void addDocumentationBuilder(DocumentationBuilder builder, String namespace, String localName) {
        table.addEntity( namespace, localName, builder );
    }

}
