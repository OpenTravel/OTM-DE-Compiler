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
 * Model element representing a context declaration for a <code>TLLibrary</code> instance.
 * 
 * @author S. Livezey
 */
public class TLContext extends TLModelElement implements TLDocumentationOwner {

    private TLLibrary owningLibrary;
    private String contextId;
    private String applicationContext;
    private TLDocumentation documentation;

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        AbstractLibrary owningLib = getOwningLibrary();
        StringBuilder identity = new StringBuilder();

        if (owningLib != null) {
            identity.append(owningLib.getValidationIdentity()).append(" : ");
        }
        if (contextId == null) {
            identity.append("[Unnamed Context Declaration]");
        } else {
            identity.append(contextId);
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return (owningLibrary == null) ? null : owningLibrary.getOwningModel();
    }

    /**
     * Returns the library instance that owns this model element.
     * 
     * @return TLLibrary
     */
    public TLLibrary getOwningLibrary() {
        return owningLibrary;
    }

    /**
     * Assigns the library instance that owns this model element.
     * 
     * @param owningLibrary
     *            the owning library instance to assign
     */
    public void setOwningLibrary(TLLibrary owningLibrary) {
        this.owningLibrary = owningLibrary;
    }

    /**
     * Moves this attribute up by one position in the list of attributes maintained by its owner. If
     * the owner is null, or this attribute is already at the front of the list, this method has no
     * effect.
     */
    public void moveUp() {
        if (owningLibrary != null) {
            owningLibrary.moveUp(this);
        }
    }

    /**
     * Moves this attribute down by one position in the list of attributes maintained by its owner.
     * If the owner is null, or this attribute is already at the end of the list, this method has no
     * effect.
     */
    public void moveDown() {
        if (owningLibrary != null) {
            owningLibrary.moveDown(this);
        }
    }

    /**
     * Returns the value of the 'contextId' field.
     * 
     * @return String
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Assigns the value of the 'contextId' field.
     * 
     * @param contextId
     *            the field value to assign
     */
    public void setContextId(String contextId) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.CONTEXT_MODIFIED, this)
                .setOldValue(this.contextId).setNewValue(contextId).buildEvent();

        this.contextId = contextId;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'applicationContext' field.
     * 
     * @return String
     */
    public String getApplicationContext() {
        return applicationContext;
    }

    /**
     * Assigns the value of the 'applicationContext' field.
     * 
     * @param applicationContext
     *            the field value to assign
     */
    public void setApplicationContext(String applicationContext) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.APPLICATION_CONTEXT_MODIFIED,
                this).setOldValue(this.applicationContext).setNewValue(applicationContext)
                .buildEvent();

        this.applicationContext = applicationContext;
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
     * Manages lists of <code>TLContext</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class ContextListManager extends ChildEntityListManager<TLContext, TLLibrary> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner
         *            the owner of the underlying list of children
         */
        public ContextListManager(TLLibrary owner) {
            super(owner, ModelEventType.CONTEXT_ADDED, ModelEventType.CONTEXT_REMOVED);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLContext child) {
            return child.getContextId();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLContext child, TLLibrary owner) {
            child.setOwningLibrary(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLLibrary owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
