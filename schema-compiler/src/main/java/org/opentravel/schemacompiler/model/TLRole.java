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
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;

/**
 * Role definition for <code>TLCoreObject</code> library members.
 * 
 * @author S. Livezey
 */
public class TLRole extends TLModelElement implements TLDocumentationOwner {

    private TLRoleEnumeration owningEnum;
    private String name;
    private TLDocumentation documentation;

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        TLCoreObject owningCore = (owningEnum == null) ? null : owningEnum.getOwningEntity();
        StringBuilder identity = new StringBuilder();

        if (owningCore != null) {
            identity.append(owningCore.getValidationIdentity()).append(" : ");
        }
        if (getName() == null) {
            identity.append("[Unnamed Role]");
        } else {
            identity.append(getName());
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return (owningEnum == null) ? null : owningEnum.getOwningModel();
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (owningEnum == null) ? null : owningEnum.getOwningLibrary();
    }

    /**
     * Returns the role enumeration that owns this role instance.
     * 
     * @return TLRoleEnumeration
     */
    public TLRoleEnumeration getRoleEnumeration() {
        return owningEnum;
    }

    /**
     * Assigns the role enumeration that owns this role instance.
     * 
     * @param owningEnum
     *            the role enumeration owner to assign
     */
    public void setRoleEnumeration(TLRoleEnumeration owningEnum) {
        this.owningEnum = owningEnum;
    }

    /**
     * Moves this role up by one position in the list of roles maintained by its owner. If the owner
     * is null, or this role is already at the front of the list, this method has no effect.
     */
    public void moveUp() {
        if (owningEnum != null) {
            owningEnum.moveUp(this);
        }
    }

    /**
     * Moves this role down by one position in the list of roles maintained by its owner. If the
     * owner is null, or this role is already at the end of the list, this method has no effect.
     */
    public void moveDown() {
        if (owningEnum != null) {
            owningEnum.moveDown(this);
        }
    }

    /**
     * Returns the value of the 'name' field.
     * 
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns the value of the 'name' field.
     * 
     * @param name
     *            the field value to assign
     */
    public void setName(String name) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.NAME_MODIFIED, this)
                .setOldValue(this.name).setNewValue(name).buildEvent();

        this.name = name;
        publishEvent(event);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#getDocumentation()
     */
    public TLDocumentation getDocumentation() {
        return documentation;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#setDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
     */
    public void setDocumentation(TLDocumentation documentation) {
        if (documentation != this.documentation) {
            ModelEvent<?> event = new ModelEventBuilder(ModelEventType.DOCUMENTATION_MODIFIED, this)
                    .setOldValue(this.documentation).setNewValue(documentation).buildEvent();

            if (documentation != null) {
                documentation.setOwner(this);
            }
            if (this.documentation != null) {
                this.documentation.setOwner(null);
            }
            this.documentation = documentation;
            publishEvent(event);
        }
    }

    /**
     * Manages lists of <code>TLRole</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class RoleListManager extends
            ChildEntityListManager<TLRole, TLRoleEnumeration> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner
         *            the owner of the underlying list of children
         */
        public RoleListManager(TLRoleEnumeration owner) {
            super(owner, ModelEventType.ROLE_ADDED, ModelEventType.ROLE_REMOVED);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLRole child) {
            return child.getName();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLRole child, TLRoleEnumeration owner) {
            child.setRoleEnumeration(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLRoleEnumeration owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
