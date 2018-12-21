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
import org.opentravel.schemacompiler.util.OTM16Upgrade;

/**
 * Abstract class that represents the common fields for complex object types such as Core and
 * Business Objects.
 * 
 * @author S. Livezey
 */
public abstract class TLComplexTypeBase extends TLLibraryMember implements TLPropertyType,
        TLVersionedExtensionOwner, TLDocumentationOwner, TLEquivalentOwner {

	protected static final String FACETS_CANNOT_BE_MODIFIED = "Facets cannot be modified once their owner has been assigned to a model.";
	
    private String name;
    private boolean notExtendable;
    private TLExtension extension;
    private EquivalentListManager equivalentManager = new EquivalentListManager(this);
    private TLDocumentation documentation;

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
        return getName();
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
            // behave the same (as if there is a list of multiple extensions).
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
     * Called when a contextual facet is added to this entity, regardless of
     * its facet type.
     * 
     * @param facet  the contextual facet that was added
     */
    protected void contextualFacetAdded(TLContextualFacet facet) {
    	if (!OTM16Upgrade.otm16Enabled) {
    		AbstractLibrary owningLibrary = getOwningLibrary();
    		
    		if (owningLibrary != null) {
    			owningLibrary.addNamedMember( facet );
    		}
    		facet.setOwningEntityName( getLocalName() );
    	}
    }
    
    /**
     * Called when a contextual facet is removed from this entity, regardless of
     * its facet type.
     * 
     * @param facet  the contextual facet that was removed
     */
    protected void contextualFacetRemoved(TLContextualFacet facet) {
    	if (!OTM16Upgrade.otm16Enabled) {
    		AbstractLibrary owningLibrary = getOwningLibrary();
    		
    		if (owningLibrary != null) {
    			owningLibrary.removeNamedMember( facet );
    		}
    	}
    }
    
}
