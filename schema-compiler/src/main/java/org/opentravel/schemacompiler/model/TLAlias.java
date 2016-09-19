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
 * Alias definition for library types.
 * 
 * @author S. Livezey
 */
public class TLAlias extends TLModelElement implements TLPropertyType {

    private TLAliasOwner owningEntity;
    private String name;
    
    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owningEntity != null) {
            identity.append(owningEntity.getValidationIdentity()).append(" : ");
        }
        if (getName() == null) {
            identity.append("[Unnamed Alias]");
        } else {
            identity.append(getName());
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public String getNamespace() {
        return (owningEntity == null) ? null : owningEntity.getNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
     */
    @Override
    public String getLocalName() {
        return name;
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (owningEntity == null) ? null : owningEntity.getOwningLibrary();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        return (owningLibrary == null) ? null : owningLibrary.getOwningModel();
    }

    /**
     * Returns the value of the 'owningEntity' field.
     * 
     * @return TLAliasOwner
     */
    public TLAliasOwner getOwningEntity() {
        return owningEntity;
    }

    /**
     * Assigns the value of the 'owningEntity' field.
     * 
     * @param owningEntity
     *            the field value to assign
     */
    public void setOwningEntity(TLAliasOwner aliasedEntity) {
        this.owningEntity = aliasedEntity;
    }

    /**
     * Moves this alias up by one position in the list of aliases maintained by its owner. If the
     * owner is null, or this aliases is already at the front of the list, this method has no
     * effect.
     */
    public void moveUp() {
        if (owningEntity != null) {
            owningEntity.moveUp(this);
        }
    }

    /**
     * Moves this alias down by one position in the list of aliases maintained by its owner. If the
     * owner is null, or this aliases is already at the end of the list, this method has no effect.
     */
    public void moveDown() {
        if (owningEntity != null) {
            owningEntity.moveDown(this);
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
    	String oldName = this.name;
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.NAME_MODIFIED, this)
                .setOldValue(oldName).setNewValue(name).buildEvent();
        ChildEntityListManager<TLAlias,?> listManager =
        		(owningEntity == null) ? null : owningEntity.getAliasListManager();
        
        this.name = name;
        
        if (listManager != null) {
            // Disable events while we update the derived alias names.  All references
        	// will be refreshed when the real event is broadcast at the end.
        	TLModel model = getOwningModel();
        	boolean eventsEnabled = (model != null) && model.isListenersEnabled();
        	
        	try {
            	if (model != null) model.setListenersEnabled(false);
            	listManager.notifyChildRenamed(this, oldName);
            	
        	} finally {
            	if (model != null) model.setListenersEnabled(eventsEnabled);
        	}
        }
        publishEvent(event);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof TLAlias) {
            TLAlias otherAlias = (TLAlias) obj;
            result = (this.owningEntity == otherAlias.owningEntity)
                    && ((otherAlias.name == null) ? (this.name == null) : otherAlias.name
                            .equals(this.name));
        }
        return result;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (name == null) ? 0 : name.hashCode();
    }

    /**
     * Manages lists of <code>TLAlias</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class AliasListManager extends ChildEntityListManager<TLAlias, TLAliasOwner> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner
         *            the owner of the underlying list of children
         */
        public AliasListManager(TLAliasOwner owner) {
            super(owner, ModelEventType.ALIAS_ADDED, ModelEventType.ALIAS_REMOVED);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLAlias child) {
            return child.getName();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLAlias child, TLAliasOwner owner) {
            child.setOwningEntity(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLAliasOwner owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
