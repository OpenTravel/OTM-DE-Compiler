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
import org.opentravel.schemacompiler.model.TLExample.ExampleListManager;

/**
 * Attribute definition for library types.
 * 
 * @author S. Livezey
 */
public class TLAttribute extends TLModelElement implements TLMemberField<TLAttributeOwner>,
		TLDocumentationOwner, TLEquivalentOwner, TLExampleOwner {

    private TLAttributeOwner attributeOwner;
    private String name;
    private TLAttributeType type;
    private String typeName;
    private boolean mandatory;
    private TLDocumentation documentation;
    private EquivalentListManager equivalentManager = new EquivalentListManager(this);
    private ExampleListManager exampleManager = new ExampleListManager(this);

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (attributeOwner != null) {
            identity.append(attributeOwner.getValidationIdentity()).append("/");
        }
        if (name == null) {
            identity.append("[Unnamed Attribute]");
        } else {
            identity.append(name);
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        TLModel owningModel = null;

        if ((attributeOwner != null) && (attributeOwner instanceof TLModelElement)) {
            owningModel = ((TLModelElement) attributeOwner).getOwningModel();
        }
        return owningModel;
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (attributeOwner == null) ? null : attributeOwner.getOwningLibrary();
    }

    /**
     * Returns the value of the 'attributeOwner' field.
     * 
     * @return TLAttributeOwner
     */
    public TLAttributeOwner getOwner() {
        return attributeOwner;
    }

    /**
     * Assigns the value of the 'attributeOwner' field.
     * 
     * @param attributeOwner
     *            the field value to assign
     */
    public void setOwner(TLAttributeOwner attributeOwner) {
        this.attributeOwner = attributeOwner;
    }

    /**
     * Moves this attribute up by one position in the list of attributes maintained by its owner. If
     * the owner is null, or this attribute is already at the front of the list, this method has no
     * effect.
     */
    public void moveUp() {
        if (attributeOwner != null) {
            attributeOwner.moveUp(this);
        }
    }

    /**
     * Moves this attribute down by one position in the list of attributes maintained by its owner.
     * If the owner is null, or this attribute is already at the end of the list, this method has no
     * effect.
     */
    public void moveDown() {
        if (attributeOwner != null) {
            attributeOwner.moveDown(this);
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
     * Returns the value of the 'type' field.
     * 
     * @return TLAttributeType
     */
    public TLAttributeType getType() {
        return type;
    }

    /**
     * Assigns the value of the 'type' field.
     * 
     * @param type
     *            the field value to assign
     */
    public void setType(TLAttributeType type) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.TYPE_ASSIGNMENT_MODIFIED, this)
                .setOldValue(this.type).setNewValue(type).buildEvent();

        this.type = type;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'typeName' field.
     * 
     * @return String
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Assigns the value of the 'typeName' field.
     * 
     * @param typeName
     *            the field value to assign
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Returns the value of the 'manditory' field.
     * 
     * @return boolean
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Assigns the value of the 'mandatory' field.
     * 
     * @param mandatory
     *            the field value to assign
     */
    public void setMandatory(boolean mandatory) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.MANDATORY_FLAG_MODIFIED, this)
                .setOldValue(this.mandatory).setNewValue(mandatory).buildEvent();

        this.mandatory = mandatory;
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
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#getExamples()
     */
    public List<TLExample> getExamples() {
        return exampleManager.getChildren();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#getExample(java.lang.String)
     */
    public TLExample getExample(String contextId) {
        return exampleManager.getChild(contextId);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#addExample(org.opentravel.schemacompiler.model.TLExample)
     */
    public void addExample(TLExample example) {
        exampleManager.addChild(example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#addExample(int,
     *      org.opentravel.schemacompiler.model.TLExample)
     */
    public void addExample(int index, TLExample example) {
        exampleManager.addChild(index, example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#removeExample(org.opentravel.schemacompiler.model.TLExample)
     */
    public void removeExample(TLExample example) {
        exampleManager.removeChild(example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#moveUp(org.opentravel.schemacompiler.model.TLExample)
     */
    public void moveUp(TLExample example) {
        exampleManager.moveUp(example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#moveDown(org.opentravel.schemacompiler.model.TLExample)
     */
    public void moveDown(TLExample example) {
        exampleManager.moveDown(example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#sortExamples(java.util.Comparator)
     */
    public void sortExamples(Comparator<TLExample> comparator) {
        exampleManager.sortChildren(comparator);
    }

    /**
     * Manages lists of <code>TLAttribute</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class AttributeListManager extends
            ChildEntityListManager<TLAttribute, TLAttributeOwner> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner
         *            the owner of the underlying list of children
         */
        public AttributeListManager(TLAttributeOwner owner) {
            super(owner, ModelEventType.ATTRIBUTE_ADDED, ModelEventType.ATTRIBUTE_REMOVED);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLAttribute child) {
            return child.getName();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLAttribute child, TLAttributeOwner owner) {
            child.setOwner(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLAttributeOwner owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
