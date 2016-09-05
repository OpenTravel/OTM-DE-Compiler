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
package org.opentravel.schemacompiler.model;

/**
 * Default implementation of the <code>LibraryMember</code> interface.
 */
public abstract class LibraryMemberImpl extends TLModelElement implements LibraryMember {
	
    private AbstractLibrary owningLibrary;

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return (owningLibrary == null) ? null : owningLibrary.getOwningModel();
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return owningLibrary;
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryMember#setOwningLibrary(org.opentravel.schemacompiler.model.AbstractLibrary)
     */
    @Override
    public void setOwningLibrary(AbstractLibrary owningLibrary) {
        this.owningLibrary = owningLibrary;
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getNamespace()
     */
    @Override
    public String getNamespace() {
        return (owningLibrary == null) ? null : owningLibrary.getNamespace();
    }

}
