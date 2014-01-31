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

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.validate.Validatable;

/**
 * Include directive for model libraries.
 * 
 * @author S. Livezey
 */
public class TLInclude implements Validatable {

    private AbstractLibrary owningLibrary;
    private String path;

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owningLibrary != null) {
            identity.append(owningLibrary.getValidationIdentity()).append(" : ");
        }
        if (path == null) {
            identity.append("[Undefined Library Include]");
        } else {
            identity.append(path);
        }
        return identity.toString();
    }

    /**
     * Returns the value of the 'owningLibrary' field.
     * 
     * @return AbstractLibrary
     */
    public AbstractLibrary getOwningLibrary() {
        return owningLibrary;
    }

    /**
     * Assigns the value of the 'owningLibrary' field.
     * 
     * @param owningLibrary
     *            the field value to assign
     */
    public void setOwningLibrary(AbstractLibrary owningLibrary) {
        this.owningLibrary = owningLibrary;
    }

    /**
     * Returns the value of the 'path' field.
     * 
     * @return String
     */
    public String getPath() {
        return path;
    }

    /**
     * Assigns the value of the 'path' field.
     * 
     * @param path
     *            the field value to assign
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof TLInclude) {
            TLInclude otherInclude = (TLInclude) obj;
            result = (otherInclude.path == null) ? (this.path == null) : otherInclude.path
                    .equals(this.path);
        }
        return result;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (path == null) ? 0 : path.hashCode();
    }

    /**
     * Manages lists of <code>TLInclude</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class IncludeListManager extends
            ChildEntityListManager<TLInclude, AbstractLibrary> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner
         *            the owner of the underlying list of children
         */
        public IncludeListManager(AbstractLibrary owner) {
            super(owner, ModelEventType.INCLUDE_ADDED, ModelEventType.INCLUDE_REMOVED);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLInclude child) {
            return child.getPath();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLInclude child, AbstractLibrary owner) {
            child.setOwningLibrary(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(AbstractLibrary owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
