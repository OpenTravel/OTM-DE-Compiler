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

package org.opentravel.schemacompiler.ioc;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModel;

import java.util.Collection;
import java.util.Collections;

/**
 * Adapter that allows a <code>SchemaDependency</code> object to behave as an OTA2.0 <code>NamedEntity</code>.
 * 
 * @author S. Livezey
 */
public class SchemaDependencyNamedEntityAdapter implements NamedEntity {

    private SchemaDependency schemaDependency;

    public SchemaDependencyNamedEntityAdapter(SchemaDependency schemaDependency) {
        this.schemaDependency = schemaDependency;
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getNamespace()
     */
    public String getNamespace() {
        return schemaDependency.getSchemaDeclaration().getNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
     */
    public String getLocalName() {
        return schemaDependency.getLocalName();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    public TLModel getOwningModel() {
        return null;
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
     */
    public AbstractLibrary getOwningLibrary() {
        return null;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    public String getValidationIdentity() {
        return null;
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement()
     */
    public LibraryElement cloneElement() {
        return null;
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement(org.opentravel.schemacompiler.model.AbstractLibrary)
     */
    public LibraryElement cloneElement(AbstractLibrary namingContext) {
        return null;
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#addListener(org.opentravel.schemacompiler.event.ModelElementListener)
     */
    @Override
    public void addListener(ModelElementListener listener) {
        // No action required - listeners not supported
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#removeListener(org.opentravel.schemacompiler.event.ModelElementListener)
     */
    @Override
    public void removeListener(ModelElementListener listener) {
        // No action required - listeners not supported
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getListeners()
     */
    @Override
    public Collection<ModelElementListener> getListeners() {
        return Collections.emptyList();
    }

}
