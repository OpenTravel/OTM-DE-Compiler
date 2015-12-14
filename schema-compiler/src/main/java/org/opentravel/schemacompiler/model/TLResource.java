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
import org.opentravel.schemacompiler.model.TLAction.ActionListManager;
import org.opentravel.schemacompiler.model.TLActionFacet.ActionFacetListManager;
import org.opentravel.schemacompiler.model.TLParamGroup.ParamGroupListManager;
import org.opentravel.schemacompiler.model.TLResourceParentRef.ResourceParentRefListManager;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Encapsulates all aspects of a RESTful resource used to expose and manage
 * a particular business object.
 * 
 * @author S. Livezey
 */
public class TLResource extends LibraryMember implements TLFacetOwner, TLVersionedExtensionOwner, TLDocumentationOwner {
	
	private String name;
	private String basePath;
	private boolean _abstract;
	private boolean firstClass;
    private TLExtension extension;
	private TLBusinessObject businessObjectRef;
	private String businessObjectRefName;
    private TLDocumentation documentation;
	private ResourceParentRefListManager parentRefManager = new ResourceParentRefListManager( this );
	private ParamGroupListManager paramGroupManager = new ParamGroupListManager( this );
	private ActionFacetListManager actionFacetManager = new ActionFacetListManager( this );
	private ActionListManager actionManager = new ActionListManager( this );
    
	/**
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
		return name;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        StringBuilder identity = new StringBuilder();

        if (owningLibrary != null) {
            identity.append(owningLibrary.getValidationIdentity()).append(" : ");
        }
        if (name == null) {
            identity.append("[Unnamed Resource Type]");
        } else {
            identity.append(name);
        }
        return identity.toString();
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
	 * @param name  the field value to assign
	 */
	public void setName(String name) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.NAME_MODIFIED, this)
        		.setOldValue(this.name).setNewValue(name).buildEvent();

		this.name = name;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'basePath' field.
	 *
	 * @return String
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * Assigns the value of the 'basePath' field.
	 *
	 * @param basePath  the field value to assign
	 */
	public void setBasePath(String basePath) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.BASE_PATH_MODIFIED, this)
        		.setOldValue(this.basePath).setNewValue(basePath).buildEvent();

		this.basePath = basePath;
        publishEvent(event);
	}

	/**
	 * Returns the value of the '_abstract' field.
	 *
	 * @return boolean
	 */
	public boolean isAbstract() {
		return _abstract;
	}

	/**
	 * Assigns the value of the '_abstract' field.
	 *
	 * @param _abstract  the field value to assign
	 */
	public void setAbstract(boolean _abstract) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.ABSTRACT_FLAG_MODIFIED, this)
				.setOldValue(this._abstract).setNewValue(_abstract).buildEvent();

		this._abstract = _abstract;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'firstClass' field.
	 *
	 * @return boolean
	 */
	public boolean isFirstClass() {
		return firstClass;
	}

	/**
	 * Assigns the value of the 'firstClass' field.
	 *
	 * @param firstClass  the field value to assign
	 */
	public void setFirstClass(boolean firstClass) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.FIRST_CLASS_FLAG_MODIFIED, this)
        		.setOldValue(this.firstClass).setNewValue(firstClass).buildEvent();

		this.firstClass = firstClass;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'businessObjectRef' field.
	 *
	 * @return TLBusinessObject
	 */
	public TLBusinessObject getBusinessObjectRef() {
		return businessObjectRef;
	}

	/**
	 * Assigns the value of the 'businessObjectRef' field.
	 *
	 * @param businessObjectRef  the field value to assign
	 */
	public void setBusinessObjectRef(TLBusinessObject businessObjectRef) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.BO_REFERENCE_MODIFIED, this)
        		.setOldValue(this.businessObjectRef).setNewValue(businessObjectRef).buildEvent();

		this.businessObjectRef = businessObjectRef;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'businessObjectRefName' field.
	 *
	 * @return String
	 */
	public String getBusinessObjectRefName() {
		return businessObjectRefName;
	}

	/**
	 * Assigns the value of the 'businessObjectRefName' field.
	 *
	 * @param businessObjectRefName  the field value to assign
	 */
	public void setBusinessObjectRefName(String businessObjectRefName) {
		this.businessObjectRefName = businessObjectRefName;
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
     * Returns the value of the 'parentRefs' field.
     * 
     * @return List<TLResourceParentRef>
     */
    public List<TLResourceParentRef> getParentRefs() {
        return parentRefManager.getChildren();
    }

    /**
     * Returns the parent reference with the specified resource name.
     * 
     * @param resourceName  the name of the parent reference to return
     * @return TLResourceParentRef
     */
    public TLResourceParentRef getParentRef(String resourceName) {
        return parentRefManager.getChild(resourceName);
    }

    /**
     * Adds a <code>TLResourceParentRef</code> element to the current list.
     * 
     * @param parentRef  the parent reference to add
     */
    public void addParentRef(TLResourceParentRef parentRef) {
    	parentRefManager.addChild(parentRef);
    }

    /**
     * Adds a <code>TLResourceParentRef</code> element to the current list.
     * 
     * @param index  the index at which the given parent reference should be added
     * @param parentRef  the parent reference to add
     */
    public void addParentRef(int index, TLResourceParentRef parentRef) {
    	parentRefManager.addChild(index, parentRef);
    }

    /**
     * Removes the specified <code>TLResourceParentRef</code> from the current list.
     * 
     * @param parentRef  the parent reference value to remove
     */
    public void removeParentRef(TLResourceParentRef parentRef) {
    	parentRefManager.removeChild(parentRef);
    }

    /**
     * Moves this parent reference up by one position in the list. If the parent reference
     * is not owned by this object or it is already at the front of the list, this method
     * has no effect.
     * 
     * @param parentRef  the parameter to move
     */
    public void moveUp(TLResourceParentRef parentRef) {
    	parentRefManager.moveUp(parentRef);
    }

    /**
     * Moves this parent reference down by one position in the list. If the parent reference
     * is not owned by this object or it is already at the end of the list, this method has
     * no effect.
     * 
     * @param parentRef  the parameter to move
     */
    public void moveDown(TLResourceParentRef parentRef) {
    	parentRefManager.moveDown(parentRef);
    }

    /**
     * Sorts the list of parent references using the comparator provided.
     * 
     * @param comparator  the comparator to use when sorting the list
     */
    public void sortParentRefs(Comparator<TLResourceParentRef> comparator) {
    	parentRefManager.sortChildren(comparator);
    }

    /**
     * Returns the value of the 'paramGroups' field.
     * 
     * @return List<TLParamGroup>
     */
    public List<TLParamGroup> getParamGroups() {
        return paramGroupManager.getChildren();
    }

    /**
     * Returns the parameter group with the specified name.
     * 
     * @param name  the field name of the parameter group to return
     * @return TLParamGroup
     */
    public TLParamGroup getParamGroup(String name) {
        return paramGroupManager.getChild(name);
    }

    /**
     * Adds a <code>TLParamGroup</code> element to the current list.
     * 
     * @param paramGroup  the parameter group to add
     */
    public void addParamGroup(TLParamGroup paramGroup) {
    	paramGroupManager.addChild(paramGroup);
    }

    /**
     * Adds a <code>TLParamGroup</code> element to the current list.
     * 
     * @param index  the index at which the given parameter group should be added
     * @param paramGroup  the parameter group to add
     */
    public void addParamGroup(int index, TLParamGroup paramGroup) {
    	paramGroupManager.addChild(index, paramGroup);
    }

    /**
     * Removes the specified <code>TLParamGroup</code> from the current list.
     * 
     * @param paramGroup  the parameter group value to remove
     */
    public void removeParamGroup(TLParamGroup paramGroup) {
    	paramGroupManager.removeChild(paramGroup);
    }

    /**
     * Moves this parameter group up by one position in the list. If the parameter group
     * is not owned by this object or it is already at the front of the list, this method
     * has no effect.
     * 
     * @param paramGroup  the parameter group to move
     */
    public void moveUp(TLParamGroup paramGroup) {
    	paramGroupManager.moveUp(paramGroup);
    }

    /**
     * Moves this parameter group down by one position in the list. If the parameter group
     * is not owned by this object or it is already at the end of the list, this method has
     * no effect.
     * 
     * @param paramGroup  the parameter group to move
     */
    public void moveDown(TLParamGroup paramGroup) {
    	paramGroupManager.moveDown(paramGroup);
    }

    /**
     * Sorts the list of parameter groups using the comparator provided.
     * 
     * @param comparator  the comparator to use when sorting the list
     */
    public void sortParamGroups(Comparator<TLParamGroup> comparator) {
    	paramGroupManager.sortChildren(comparator);
    }

    /**
     * Returns the value of the 'actionFacets' field.
     * 
     * @return List<TLActionFacet>
     */
    public List<TLActionFacet> getActionFacets() {
        return actionFacetManager.getChildren();
    }

    /**
     * Returns the action facet with the specified name.
     * 
     * @param name  the name of the action facet to return
     * @return TLActionFacet
     */
    public TLActionFacet getActionFacet(String name) {
        return actionFacetManager.getChild(name);
    }

    /**
     * Adds a <code>TLActionFacet</code> element to the current list.
     * 
     * @param facet  the action facet to add
     */
    public void addActionFacet(TLActionFacet facet) {
    	actionFacetManager.addChild(facet);
    }

    /**
     * Adds a <code>TLActionFacet</code> element to the current list.
     * 
     * @param index  the index at which the given action facet should be added
     * @param facet  the action facet to add
     */
    public void addActionFacet(int index, TLActionFacet facet) {
    	actionFacetManager.addChild(index, facet);
    }

    /**
     * Removes the specified <code>TLActionFacet</code> from the current list.
     * 
     * @param facet  the action facet value to remove
     */
    public void removeActionFacet(TLActionFacet facet) {
    	actionFacetManager.removeChild(facet);
    }

    /**
     * Moves this action facet up by one position in the list. If the action facet is
     * not owned by this object or it is already at the front of the list, this method
     * has no effect.
     * 
     * @param facet  the action facet to move
     */
    public void moveUp(TLActionFacet facet) {
    	actionFacetManager.moveUp(facet);
    }

    /**
     * Moves this action facet down by one position in the list. If the action facet is
     * not owned by this object or it is already at the end of the list, this method has
     * no effect.
     * 
     * @param facet  the action facet to move
     */
    public void moveDown(TLActionFacet facet) {
    	actionFacetManager.moveDown(facet);
    }

    /**
     * Sorts the list of parameters using the comparator provided.
     * 
     * @param comparator  the comparator to use when sorting the list
     */
    public void sortActionFacets(Comparator<TLActionFacet> comparator) {
    	actionFacetManager.sortChildren(comparator);
    }

    /**
     * Returns the value of the 'actions' field.
     * 
     * @return List<TLAction>
     */
    public List<TLAction> getActions() {
        return actionManager.getChildren();
    }

    /**
     * Returns the action with the specified field name.
     * 
     * @param actionId  the ID of the action to return
     * @return TLAction
     */
    public TLAction getAction(String actionId) {
        return actionManager.getChild(actionId);
    }

    /**
     * Adds a <code>TLAction</code> element to the current list.
     * 
     * @param action  the action to add
     */
    public void addAction(TLAction action) {
    	actionManager.addChild(action);
    }

    /**
     * Adds a <code>TLAction</code> element to the current list.
     * 
     * @param index  the index at which the given action should be added
     * @param action  the action to add
     */
    public void addAction(int index, TLAction action) {
    	actionManager.addChild(index, action);
    }

    /**
     * Removes the specified <code>TLAction</code> from the current list.
     * 
     * @param action  the action value to remove
     */
    public void removeAction(TLAction action) {
    	actionManager.removeChild(action);
    }

    /**
     * Moves this action up by one position in the list. If the action is not owned by this
     * object or it is already at the front of the list, this method has no effect.
     * 
     * @param action  the action to move
     */
    public void moveUp(TLAction action) {
    	actionManager.moveUp(action);
    }

    /**
     * Moves this action down by one position in the list. If the action is not owned by this
     * object or it is already at the end of the list, this method has no effect.
     * 
     * @param action  the action to move
     */
    public void moveDown(TLAction action) {
    	actionManager.moveDown(action);
    }

    /**
     * Sorts the list of parameters using the comparator provided.
     * 
     * @param comparator  the comparator to use when sorting the list
     */
    public void sortActions(Comparator<TLAction> comparator) {
    	actionManager.sortChildren(comparator);
    }

}
