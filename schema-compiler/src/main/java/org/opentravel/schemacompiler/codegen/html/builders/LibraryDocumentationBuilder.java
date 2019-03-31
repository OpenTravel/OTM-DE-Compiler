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

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.LibraryWriter;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Eric.Bronson
 *
 */
public class LibraryDocumentationBuilder implements DocumentationBuilder {

    private final TLLibrary library;

    private LibraryDocumentationBuilder next;

    private LibraryDocumentationBuilder prev;

    private List<DocumentationBuilder> businessObjects = new ArrayList<>();
    private List<DocumentationBuilder> coreObjects = new ArrayList<>();
    private List<DocumentationBuilder> choiceObjects = new ArrayList<>();
    private List<DocumentationBuilder> vwaObjects = new ArrayList<>();
    private List<DocumentationBuilder> serviceObjects = new ArrayList<>();
    private List<DocumentationBuilder> enumObjects = new ArrayList<>();
    private List<DocumentationBuilder> simpleObjects = new ArrayList<>();
    private List<DocumentationBuilder> allObjects = new ArrayList<>();

    /**
     * @param library the library for which to create a builder
     */
    public LibraryDocumentationBuilder(TLLibrary library) {
        this.library = library;
        for (TLBusinessObject bo : library.getBusinessObjectTypes()) {
            businessObjects.add( DocumentationBuilderFactory.getInstance().getDocumentationBuilder( bo ) );
        }
        for (TLCoreObject bo : library.getCoreObjectTypes()) {
            coreObjects.add( DocumentationBuilderFactory.getInstance().getDocumentationBuilder( bo ) );
        }
        for (TLChoiceObject co : library.getChoiceObjectTypes()) {
            choiceObjects.add( DocumentationBuilderFactory.getInstance().getDocumentationBuilder( co ) );
        }
        for (TLClosedEnumeration bo : library.getClosedEnumerationTypes()) {
            enumObjects.add( DocumentationBuilderFactory.getInstance().getDocumentationBuilder( bo ) );
        }
        for (TLOpenEnumeration bo : library.getOpenEnumerationTypes()) {
            enumObjects.add( DocumentationBuilderFactory.getInstance().getDocumentationBuilder( bo ) );
        }
        for (TLValueWithAttributes bo : library.getValueWithAttributesTypes()) {
            vwaObjects.add( DocumentationBuilderFactory.getInstance().getDocumentationBuilder( bo ) );
        }
        for (TLSimple bo : library.getSimpleTypes()) {
            simpleObjects.add( DocumentationBuilderFactory.getInstance().getDocumentationBuilder( bo ) );
        }
        TLService service = library.getService();
        if (service != null) {
            serviceObjects.add( DocumentationBuilderFactory.getInstance().getDocumentationBuilder( service ) );
        }
        allObjects.addAll( businessObjects );
        allObjects.addAll( coreObjects );
        allObjects.addAll( choiceObjects );
        allObjects.addAll( enumObjects );
        allObjects.addAll( vwaObjects );
        allObjects.addAll( serviceObjects );
        allObjects.addAll( simpleObjects );
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.travelport.otm.sourcegen.documentation.DocumentationBuilder#getDocType ()
     */
    @Override
    public DocumentationBuilderType getDocType() {
        return DocumentationBuilderType.LIBRARY;
    }

    @Override
    public String getName() {
        return AbstractDocumentationBuilder.getLibraryName( library );
    }

    @Override
    public String getNamespace() {
        return library.getNamespace();
    }

    @Override
    public String getQualifiedName() {
        return null;
    }

    @Override
    public void setNext(DocumentationBuilder next) {
        if (next instanceof LibraryDocumentationBuilder) {
            this.next = (LibraryDocumentationBuilder) next;
        }
    }

    @Override
    public void setPrevious(DocumentationBuilder prev) {
        if (prev instanceof LibraryDocumentationBuilder) {
            this.prev = (LibraryDocumentationBuilder) prev;
        }
    }

    @Override
    public void build() throws CodeGenerationException {
        try {
            LibraryWriter writer = new LibraryWriter( Configuration.getInstance(), this, prev, next );
            Content content = writer.getHeader();
            Content libraryTree = writer.getContentHeader();
            writer.addObjectsSummary( libraryTree );
            content.addContent( libraryTree );
            writer.addFooter( content );
            writer.printDocument( content );
            writer.close();
            DocumentationBuilder p;
            DocumentationBuilder n;
            ListIterator<DocumentationBuilder> objectIter = allObjects.listIterator();

            while (objectIter.hasNext()) {
                p = objectIter.hasPrevious() ? allObjects.get( objectIter.previousIndex() ) : null;
                DocumentationBuilder builder = objectIter.next();
                n = objectIter.hasNext() ? allObjects.get( objectIter.nextIndex() ) : null;
                builder.setPrevious( p );
                builder.setNext( n );
                builder.build();
            }

        } catch (IOException e) {
            throw new CodeGenerationException( "Error creating doclet writer", e );
        }
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return library.getVersion();
    }

    /**
     * @return the businessObjects
     */
    public List<DocumentationBuilder> getBusinessObjects() {
        return businessObjects;
    }

    /**
     * @return the coreObjects
     */
    public List<DocumentationBuilder> getCoreObjects() {
        return coreObjects;
    }

    /**
     * @return the choiceObjects
     */
    public List<DocumentationBuilder> getChoiceObjects() {
        return choiceObjects;
    }

    /**
     * @return the vwaObjects
     */
    public List<DocumentationBuilder> getVwas() {
        return vwaObjects;
    }

    /**
     * @return the serviceObjects
     */
    public List<DocumentationBuilder> getServices() {
        return serviceObjects;
    }

    /**
     * @return the enumObjects
     */
    public List<DocumentationBuilder> getEnums() {
        return enumObjects;
    }

    /**
     * @return the simpleObjects
     */
    public List<DocumentationBuilder> getSimpleObjects() {
        return simpleObjects;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getOwningLibrary() {
        return null;
    }

}
