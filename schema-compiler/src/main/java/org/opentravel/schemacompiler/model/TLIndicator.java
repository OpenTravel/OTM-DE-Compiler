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
import org.opentravel.schemacompiler.model.TLEquivalent.EquivalentListManager;

import java.util.Comparator;
import java.util.List;

/**
 * Indicator definition for complex library types.
 * 
 * @author S. Livezey
 */
public class TLIndicator extends TLModelElement
    implements TLMemberField<TLIndicatorOwner>, TLDocumentationOwner, TLEquivalentOwner {

    private TLIndicatorOwner owner;
    private String name;
    private boolean publishAsElement;
    private TLDocumentation documentation;
    private EquivalentListManager equivalentManager = new EquivalentListManager( this );

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owner != null) {
            identity.append( owner.getValidationIdentity() ).append( "/" );
        }
        if (name == null) {
            identity.append( "[Unnamed Indicator]" );
        } else {
            identity.append( name );
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        TLModel owningModel = null;

        if (owner != null) {
            AbstractLibrary owningLibrary = owner.getOwningLibrary();

            if (owningLibrary != null) {
                owningModel = owningLibrary.getOwningModel();
            }
        }
        return owningModel;
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
     * @return TLIndicatorOwner
     */
    public TLIndicatorOwner getOwner() {
        return owner;
    }

    /**
     * Assigns the value of the 'owner' field.
     * 
     * @param owner the field value to assign
     */
    public void setOwner(TLIndicatorOwner owner) {
        this.owner = owner;
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
     * @param name the field value to assign
     */
    public void setName(String name) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.NAME_MODIFIED, this ).setOldValue( this.name )
            .setNewValue( name ).buildEvent();

        this.name = name;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'publishAsElement' field.
     * 
     * @return boolean
     */
    public boolean isPublishAsElement() {
        return publishAsElement;
    }

    /**
     * Assigns the value of the 'publishAsElement' field.
     * 
     * @param publishAsElement the field value to assign
     */
    public void setPublishAsElement(boolean publishAsElement) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.PUBLISH_AS_ELEMENT_MODIFIED, this )
            .setOldValue( this.publishAsElement ).setNewValue( publishAsElement ).buildEvent();

        this.publishAsElement = publishAsElement;
        publishEvent( event );
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
            ModelEvent<?> event = new ModelEventBuilder( ModelEventType.DOCUMENTATION_MODIFIED, this )
                .setOldValue( this.documentation ).setNewValue( documentation ).buildEvent();

            if (documentation != null) {
                documentation.setOwner( this );
            }
            if (this.documentation != null) {
                this.documentation.setOwner( null );
            }
            this.documentation = documentation;
            publishEvent( event );
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
        return equivalentManager.getChild( context );
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void addEquivalent(TLEquivalent equivalent) {
        equivalentManager.addChild( equivalent );
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(int,
     *      org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void addEquivalent(int index, TLEquivalent equivalent) {
        equivalentManager.addChild( index, equivalent );
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#removeEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void removeEquivalent(TLEquivalent equivalent) {
        equivalentManager.removeChild( equivalent );
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#sortEquivalents(java.util.Comparator)
     */
    @Override
    public void sortEquivalents(Comparator<TLEquivalent> comparator) {
        equivalentManager.sortChildren( comparator );
    }

    /**
     * Moves this indicator up by one position in the list of indicators maintained by its owner. If the owner is null,
     * or this indicator is already at the front of the list, this method has no effect.
     */
    public void moveUp() {
        if (owner != null) {
            owner.moveUp( this );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#moveUp(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void moveUp(TLEquivalent equivalent) {
        equivalentManager.moveUp( equivalent );
    }

    /**
     * Moves this indicator down by one position in the list of indicators maintained by its owner. If the owner is
     * null, or this indicator is already at the end of the list, this method has no effect.
     */
    public void moveDown() {
        if (owner != null) {
            owner.moveDown( this );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#moveDown(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void moveDown(TLEquivalent equivalent) {
        equivalentManager.moveDown( equivalent );
    }

    /**
     * Manages lists of <code>TLIndicator</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class IndicatorListManager extends ChildEntityListManager<TLIndicator,TLIndicatorOwner> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner the owner of the underlying list of children
         */
        public IndicatorListManager(TLIndicatorOwner owner) {
            super( owner, ModelEventType.PROPERTY_ADDED, ModelEventType.PROPERTY_REMOVED );
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLIndicator child) {
            return child.getName();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLIndicator child, TLIndicatorOwner owner) {
            child.setOwner( owner );
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLIndicatorOwner owner, ModelEvent<?> event) {
            if (owner != null) {
                AbstractLibrary owningLibrary = owner.getOwningLibrary();

                if (owningLibrary != null) {
                    owningLibrary.publishEvent( event );
                }
            }
        }

    }

}
