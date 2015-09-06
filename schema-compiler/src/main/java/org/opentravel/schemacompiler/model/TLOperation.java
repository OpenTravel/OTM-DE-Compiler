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

import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.model.TLEquivalent.EquivalentListManager;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Operation element defined within a service definition.
 * 
 * @author S. Livezey
 */
public class TLOperation extends TLModelElement implements NamedEntity, TLFacetOwner, TLVersionedExtensionOwner,
		TLDocumentationOwner, TLEquivalentOwner {

    private TLService owningService;
    private String name;
    private TLDocumentation documentation;
    private EquivalentListManager equivalentManager = new EquivalentListManager(this);
    private TLFacet request;
    private TLFacet response;
    private TLFacet notification;
    private boolean notExtendable;
    private TLExtension extension;

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return (owningService == null) ? null : owningService.getOwningModel();
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (owningService == null) ? null : owningService.getOwningLibrary();
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owningService != null) {
            identity.append(owningService.getValidationIdentity()).append(".");
        }
        if (name == null) {
            identity.append("[Unnamed Operation]");
        } else {
            identity.append(name);
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.version.Versioned#getVersion()
     */
    @Override
    public String getVersion() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        String version = null;

        if (owningLibrary instanceof TLLibrary) {
            version = ((TLLibrary) owningLibrary).getVersion();
        }
        return version;
    }

    /**
     * @see org.opentravel.schemacompiler.version.Versioned#getVersionScheme()
     */
    @Override
    public String getVersionScheme() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        String versionScheme = null;

        if (owningLibrary instanceof TLLibrary) {
            versionScheme = ((TLLibrary) owningLibrary).getVersionScheme();
        }
        return versionScheme;
    }

    /**
     * @see org.opentravel.schemacompiler.version.Versioned#getBaseNamespace()
     */
    @Override
    public String getBaseNamespace() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        String baseNamespace;

        if (owningLibrary instanceof TLLibrary) {
            baseNamespace = ((TLLibrary) owningLibrary).getBaseNamespace();
        } else {
            baseNamespace = getNamespace();
        }
        return baseNamespace;
    }

    /**
     * @see org.opentravel.schemacompiler.version.Versioned#isLaterVersion(org.opentravel.schemacompiler.version.Versioned)
     */
    @Override
    public boolean isLaterVersion(Versioned otherVersionedItem) {
        boolean result = false;

        if ((otherVersionedItem != null) && otherVersionedItem.getClass().equals(this.getClass())
                && (this.getOwningLibrary() != null)
                && (otherVersionedItem.getOwningLibrary() != null) && (this.getLocalName() != null)
                && this.getLocalName().equals(otherVersionedItem.getLocalName())) {
            result = this.getOwningLibrary().isLaterVersion(otherVersionedItem.getOwningLibrary());
        }
        return result;
    }

    /**
     * Returns the value of the 'owningService' field.
     * 
     * @return TLService
     */
    public TLService getOwningService() {
        return owningService;
    }

    /**
     * Assigns the value of the 'owningService' field.
     * 
     * @param owningService
     *            the field value to assign
     */
    public void setOwningService(TLService owningService) {
        this.owningService = owningService;
    }

    /**
     * Moves this operation up by one position in the list of operations maintained by its owner. If
     * the owner is null, or this operation is already at the front of the list, this method has no
     * effect.
     */
    public void moveUp() {
        if (owningService != null) {
            owningService.moveUp(this);
        }
    }

    /**
     * Moves this operation down by one position in the list of operations maintained by its owner.
     * If the owner is null, or this operation is already at the end of the list, this method has no
     * effect.
     */
    public void moveDown() {
        if (owningService != null) {
            owningService.moveDown(this);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getNamespace()
     */
    @Override
    public String getNamespace() {
        return (owningService == null) ? null : owningService.getNamespace();
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
     * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
     */
    @Override
    public String getLocalName() {
        String serviceName = (owningService == null) ? "UnknownService" : owningService
                .getLocalName();
        return serviceName + "_" + name;
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
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#getEquivalents()
     */
    @Override
    public List<TLEquivalent> getEquivalents() {
        return equivalentManager.getChildren();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#getEquivalent(java.lang.String)
     */
    @Override
    public TLEquivalent getEquivalent(String context) {
        return equivalentManager.getChild(context);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void addEquivalent(TLEquivalent equivalent) {
        equivalentManager.addChild(equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(int,
     *      org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void addEquivalent(int index, TLEquivalent equivalent) {
        equivalentManager.addChild(index, equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#removeEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void removeEquivalent(TLEquivalent equivalent) {
        equivalentManager.removeChild(equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#moveUp(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void moveUp(TLEquivalent equivalent) {
        equivalentManager.moveUp(equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#moveDown(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void moveDown(TLEquivalent equivalent) {
        equivalentManager.moveDown(equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#sortEquivalents(java.util.Comparator)
     */
    @Override
    public void sortEquivalents(Comparator<TLEquivalent> comparator) {
        equivalentManager.sortChildren(comparator);
    }

    /**
     * Returns the value of the 'request' field.
     * 
     * @return TLFacet
     */
    public TLFacet getRequest() {
        if (request == null) {
            request = new TLFacet();
            request.setFacetType(TLFacetType.REQUEST);
            request.setOwningEntity(this);
        }
        return request;
    }

    /**
     * Assigns the value of the 'request' field.
     * 
     * @param request
     *            the field value to assign
     */
    public void setRequest(TLFacet request) {
        if (request != this.request) {
            if (getOwningModel() != null) {
                throw new IllegalStateException(
                        "Facets cannot be modified once their owner has been assigned to a model.");
            }
            if (request != null) {
                request.setFacetType(TLFacetType.REQUEST);
                request.setOwningEntity(this);
            }
            if (this.request != null) {
                this.request.setFacetType(null);
                this.request.setOwningEntity(null);
            }
            this.request = request;
        }
    }

    /**
     * Returns the value of the 'response' field.
     * 
     * @return TLFacet
     */
    public TLFacet getResponse() {
        if (response == null) {
            response = new TLFacet();
            response.setFacetType(TLFacetType.RESPONSE);
            response.setOwningEntity(this);
        }
        return response;
    }

    /**
     * Assigns the value of the 'response' field.
     * 
     * @param response
     *            the field value to assign
     */
    public void setResponse(TLFacet response) {
        if (response != this.response) {
            if (getOwningModel() != null) {
                throw new IllegalStateException(
                        "Facets cannot be modified once their owner has been assigned to a model.");
            }
            if (response != null) {
                response.setFacetType(TLFacetType.RESPONSE);
                response.setOwningEntity(this);
            }
            if (this.response != null) {
                this.response.setFacetType(null);
                this.response.setOwningEntity(null);
            }
            this.response = response;
        }
    }

    /**
     * Returns the value of the 'notification' field.
     * 
     * @return TLFacet
     */
    public TLFacet getNotification() {
        if (notification == null) {
            notification = new TLFacet();
            notification.setFacetType(TLFacetType.NOTIFICATION);
            notification.setOwningEntity(this);
        }
        return notification;
    }

    /**
     * Assigns the value of the 'notification' field.
     * 
     * @param notification
     *            the field value to assign
     */
    public void setNotification(TLFacet notification) {
        if (notification != this.notification) {
            if (getOwningModel() != null) {
                throw new IllegalStateException(
                        "Facets cannot be modified once their owner has been assigned to a model.");
            }
            if (notification != null) {
                notification.setFacetType(TLFacetType.NOTIFICATION);
                notification.setOwningEntity(this);
            }
            if (this.notification != null) {
                this.notification.setFacetType(null);
                this.notification.setOwningEntity(null);
            }
            this.notification = notification;
        }
    }

    /**
     * Returns the value of the 'notExtendable' field.
     * 
     * @return boolean
     */
    public boolean isNotExtendable() {
        return notExtendable;
    }

    /**
     * Assigns the value of the 'notExtendable' field.
     * 
     * @param notExtendable
     *            the field value to assign
     */
    public void setNotExtendable(boolean notExtendable) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.NOT_EXTENDABLE_FLAG_MODIFIED,
                this).setOldValue(this.notExtendable).setNewValue(notExtendable).buildEvent();

        this.notExtendable = notExtendable;
        publishEvent(event);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExtensionOwner#getExtension()
     */
    @Override
    public TLExtension getExtension() {
        return extension;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExtensionOwner#setExtension(org.opentravel.schemacompiler.model.TLExtension)
     */
    @Override
    public void setExtension(TLExtension extension) {
        if (extension != this.extension) {
            // Even though there is only one extension, send to events so that all extension owners
            // behave
            // the same (as if there is a list of multiple extensions).
            if (this.extension != null) {
                ModelEvent<?> event = new ModelEventBuilder(ModelEventType.EXTENDS_REMOVED, this)
                        .setAffectedItem(this.extension).buildEvent();

                this.extension.setOwner(null);
                this.extension = null;
                publishEvent(event);
            }
            if (extension != null) {
                ModelEvent<?> event = new ModelEventBuilder(ModelEventType.EXTENDS_ADDED, this)
                        .setAffectedItem(extension).buildEvent();

                extension.setOwner(this);
                this.extension = extension;
                publishEvent(event);
            }
        }
    }

    /**
     * Manages lists of <code>TLOperation</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class OperationListManager extends
            ChildEntityListManager<TLOperation, TLService> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner
         *            the owner of the underlying list of children
         */
        public OperationListManager(TLService owner) {
            super(owner, ModelEventType.OPERATION_ADDED, ModelEventType.OPERATION_REMOVED);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLOperation child) {
            return child.getName();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLOperation child, TLService owner) {
            child.setOwningService(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLService owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
