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
 * Parameter definition that can be used to reference an existing attribute,
 * element, or indicator.
 * 
 * @author S. Livezey
 */
public class TLParameter extends TLModelElement implements TLDocumentationOwner, TLEquivalentOwner,
		TLExampleOwner {
	
	private TLParamGroup owner;
	private String fieldRefName;
	private TLParamLocation location;
    private TLDocumentation documentation;
    private EquivalentListManager equivalentManager = new EquivalentListManager(this);
    private ExampleListManager exampleManager = new ExampleListManager(this);
	
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();
        String fieldName = null;
        
        if (owner != null) {
            identity.append( owner.getValidationIdentity() ).append("/");
        }
        if (fieldName == null) {
        	fieldName = fieldRefName;
        }
        if (fieldName == null) {
            identity.append( "[Unnamed Parameter]" );
        } else {
            identity.append( fieldName );
        }
        return identity.toString();
	}

    /**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return (owner == null) ? null : owner.getOwningLibrary();
	}

	/**
	 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owner == null) ? null : owner.getOwningModel();
	}

	/**
	 * Returns the value of the 'owner' field.
	 *
	 * @return TLParamGroup
	 */
	public TLParamGroup getOwner() {
		return owner;
	}

	/**
	 * Assigns the value of the 'owner' field.
	 *
	 * @param owner  the field value to assign
	 */
	public void setOwner(TLParamGroup owner) {
		this.owner = owner;
	}

	/**
	 * Returns the value of the 'fieldRefName' field.
	 *
	 * @return String
	 */
	public String getFieldRefName() {
		return fieldRefName;
	}

	/**
	 * Assigns the value of the 'fieldRefName' field.
	 *
	 * @param fieldRefName  the field value to assign
	 */
	public void setFieldRefName(String fieldRefName) {
		this.fieldRefName = fieldRefName;
	}

	/**
	 * Returns the value of the 'location' field.
	 *
	 * @return TLParamLocation
	 */
	public TLParamLocation getLocation() {
		return location;
	}

	/**
	 * Assigns the value of the 'location' field.
	 *
	 * @param location  the field value to assign
	 */
	public void setLocation(TLParamLocation location) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.LOCATION_MODIFIED, this)
				.setOldValue(this.location).setNewValue(location).buildEvent();
        
		this.location = location;
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
     * Manages lists of <code>TLParameter</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class ParameterListManager extends
            ChildEntityListManager<TLParameter, TLParamGroup> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner
         *            the owner of the underlying list of children
         */
        public ParameterListManager(TLParamGroup owner) {
            super(owner, ModelEventType.PARAMETER_ADDED, ModelEventType.PARAMETER_REMOVED);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLParameter child) {
        	String childName = null;
        	
        	if (childName == null) {
        		childName = child.getFieldRefName();
        	}
            return childName;
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLParameter child, TLParamGroup owner) {
            child.setOwner(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLParamGroup owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
