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
 * Model element that indicates an "extends" between the extension owner and the entity to which the
 * extension refers.
 * 
 * @author S. Livezey
 */
public class TLExtension extends TLModelElement implements TLDocumentationOwner {

    private TLExtensionOwner owner;
    private NamedEntity extendsEntity;
    private String extendsEntityName;
    private TLDocumentation documentation;

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owner != null) {
            identity.append(owner.getValidationIdentity()).append(" : ");
        } else if (extendsEntity == null) {
            identity.append("[Unspecified Extension]");
        } else {
            identity.append("Extension:").append(extendsEntity.getLocalName());
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return (owner == null) ? null : owner.getOwningModel();
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (owner == null) ? null : owner.getOwningLibrary();
    }

    /**
     * Returns the value of the 'owner' field.
     * 
     * @return TLExtensionOwner
     */
    public TLExtensionOwner getOwner() {
        return owner;
    }

    /**
     * Assigns the value of the 'owner' field.
     * 
     * @param owner
     *            the field value to assign
     */
    public void setOwner(TLExtensionOwner owner) {
        this.owner = owner;
    }

    /**
     * Returns the value of the 'extendsEntity' field.
     * 
     * @return NamedEntity
     */
    public NamedEntity getExtendsEntity() {
        return extendsEntity;
    }

    /**
     * Assigns the value of the 'extendsEntity' field.
     * 
     * @param extendsEntity
     *            the field value to assign
     */
    public void setExtendsEntity(NamedEntity extendsEntity) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.EXTENDS_ENTITY_MODIFIED, this)
                .setOldValue(this.extendsEntity).setNewValue(extendsEntity).buildEvent();

        this.extendsEntity = extendsEntity;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'extendsEntityName' field.
     * 
     * @return String
     */
    public String getExtendsEntityName() {
        return extendsEntityName;
    }

    /**
     * Assigns the value of the 'extendsEntityName' field.
     * 
     * @param extendsEntityName
     *            the field value to assign
     */
    public void setExtendsEntityName(String extendsEntityName) {
        this.extendsEntityName = extendsEntityName;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#getDocumentation()
     */
    @Override
    public TLDocumentation getDocumentation() {
        return documentation;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#setDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
     */
    @Override
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

}
