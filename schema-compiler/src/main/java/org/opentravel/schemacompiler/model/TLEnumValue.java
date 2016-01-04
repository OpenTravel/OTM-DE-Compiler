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

/**
 * Values defined in an owning <code>EnumerationWithDoc</code> type.
 * 
 * @author S. Livezey
 */
public class TLEnumValue extends TLModelElement implements TLDocumentationOwner, TLEquivalentOwner {

    private TLAbstractEnumeration owningEnum;
    private String literal;
    private String label;
    private TLDocumentation documentation;
    private EquivalentListManager equivalentManager = new EquivalentListManager(this);

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (owningEnum == null) ? null : owningEnum.getOwningLibrary();
    }

    /**
     * Returns the value of the 'owningEnum' field.
     * 
     * @return LibraryMember
     */
    public TLAbstractEnumeration getOwningEnum() {
        return owningEnum;
    }

    /**
     * Assigns the value of the 'owningEnum' field.
     * 
     * @param owningEnum
     *            the field value to assign
     */
    public void setOwningEnum(TLAbstractEnumeration owningEnum) {
        this.owningEnum = owningEnum;
    }

    /**
     * Moves this value up by one position in the list of values maintained by its owner. If the
     * owner is null, or this value is already at the front of the list, this method has no effect.
     */
    public void moveUp() {
        if (owningEnum != null) {
            owningEnum.moveUp(this);
        }
    }

    /**
     * Moves this value down by one position in the list of values maintained by its owner. If the
     * owner is null, or this value is already at the end of the list, this method has no effect.
     */
    public void moveDown() {
        if (owningEnum != null) {
            owningEnum.moveDown(this);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owningEnum != null) {
            identity.append(owningEnum.getValidationIdentity()).append("/");
        }
        if (literal == null) {
            identity.append("[Unnamed Enum Literal]");
        } else {
            identity.append(literal);
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
     * Returns the value of the 'literal' field.
     * 
     * @return String
     */
    public String getLiteral() {
        return literal;
    }

    /**
     * Assigns the value of the 'literal' field.
     * 
     * @param literal
     *            the field value to assign
     */
    public void setLiteral(String literal) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.ENUM_LITERAL_MODIFIED, this)
                .setOldValue(this.literal).setNewValue(literal).buildEvent();

        this.literal = literal;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'label' field.
     * 
     * @return String
     */
    public String getLabel() {
        return label;
    }

    /**
     * Assigns the value of the 'label' field.
     * 
     * @param label
     *            the field value to assign
     */
    public void setLabel(String label) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.ENUM_LABEL_MODIFIED, this)
                .setOldValue(this.label).setNewValue(label).buildEvent();

        this.label = label;
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
     * Manages lists of <code>TLEnumValue</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class EnumValueListManager extends
            ChildEntityListManager<TLEnumValue, TLAbstractEnumeration> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner
         *            the owner of the underlying list of children
         */
        public EnumValueListManager(TLAbstractEnumeration owner) {
            super(owner, ModelEventType.ENUM_VALUE_ADDED, ModelEventType.ENUM_VALUE_REMOVED);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLEnumValue child) {
            return child.getLiteral();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLEnumValue child, TLAbstractEnumeration owner) {
            child.setOwningEnum(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLAbstractEnumeration owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
