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
import org.opentravel.schemacompiler.model.TLDocumentationOwner;

/**
 * @author Eric.Bronson
 *
 */
public abstract class NamedEntityDocumentationBuilder<T extends NamedEntity & TLDocumentationOwner>
    extends AbstractDocumentationBuilder<T> {

    protected DocumentationBuilder superType;

    /**
     * @param element the named entity for which to create a builder
     */
    public NamedEntityDocumentationBuilder(T element) {
        super( element );
        name = element.getLocalName();
        namespace = element.getNamespace();
        // prevent cyclic dependencies
        DocumentationBuilderFactory.addDocumentationBuilder( this, namespace, name );
    }

    public DocumentationBuilder getSuperType() {
        return superType;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals( obj );
    }

}
