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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;

/**
 * Contextual facet definition for complex types that may or may not reside within
 * the same library (or namespace) as their facet owners.
 */
public class TLContextualFacet extends TLFacet implements LibraryMember, TLFacetOwner {
	
    private ContextualFacetListManager childFacetManager = new ContextualFacetListManager(
    		this, null, ModelEventType.CHILD_FACET_ADDED, ModelEventType.CHILD_FACET_REMOVED);
    private AbstractLibrary owningLibrary;
    private String owningEntityName;
    private String name;
    private String facetNamespace;
    
	/**
	 * @see org.opentravel.schemacompiler.model.TLAbstractFacet#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owningLibrary == null) ? null : owningLibrary.getOwningModel();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLAbstractFacet#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		AbstractLibrary library = owningLibrary;
		
    	if ((library == null) && !OTM16Upgrade.otm16Enabled) {
    		TLFacetOwner owner = getOwningEntity();
    		library = (owner == null) ? null : owner.getOwningLibrary();
    	}
		return library;
	}
	
    /**
     * @see org.opentravel.schemacompiler.model.LibraryMember#setOwningLibrary(org.opentravel.schemacompiler.model.AbstractLibrary)
     */
	@Override
    public void setOwningLibrary(AbstractLibrary owningLibrary) {
        this.owningLibrary = owningLibrary;
    }
	
	/**
	 * Returns true if this facet's owner resides within the same library to which this facet
	 * is assigned.
	 * 
	 * @return boolean
	 */
	public boolean isLocalFacet() {
		Set<TLFacetOwner> visitedOwners = new HashSet<>();
		TLFacetOwner owner = getOwningEntity();
    	
		while (owner instanceof TLContextualFacet) {
			if (visitedOwners.contains( owner )) {
				owner = null; // circular references are not considered "local" facets
				
			} else {
				visitedOwners.add( owner );
				owner = ((TLContextualFacet) owner).getOwningEntity();
			}
		}
		return (owner != null) && (owner.getOwningLibrary() == owningLibrary);
	}

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
     */
    @Override
    public String getLocalName() {
        StringBuilder localName = new StringBuilder();
        
        buildLocalName( localName, new HashSet<TLContextualFacet>() );
        return localName.toString();
    }
    
    /**
     * Recursive method that calculates the local name of a facet while avoiding infinite
     * loops caused by circular references.
     * 
     * @param localName  the local name of the facet being constructed
     * @param visitedFacets  the list of contextual facets already included in the local name
     */
    private void buildLocalName(StringBuilder localName, Set<TLContextualFacet> visitedFacets) {
    	if (!visitedFacets.contains( this )) {
    		visitedFacets.add( this );
            TLFacetOwner owningEntity = getOwningEntity();
            
            if (owningEntity instanceof TLContextualFacet) {
            	((TLContextualFacet) owningEntity).buildLocalName( localName, visitedFacets );
                localName.append("_").append(name);
                
            } else {
                TLFacetType facetType = getFacetType();
            	
                if (owningEntity != null) {
                    localName.append(owningEntity.getLocalName()).append('_');
                } else {
                	localName.append("UNKNOWN_");
                }
                if (facetType != null) {
                    localName.append(facetType.getIdentityName(name));
                } else {
                    localName.append(name);
                }
            }
            
    	} else { // Assume unknown top-level owner in the case of circular references
        	localName.append("UNKNOWN");
    	}
    }

	/**
	 * @see org.opentravel.schemacompiler.model.TLFacet#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owningLibrary != null) {
            identity.append(owningLibrary.getValidationIdentity()).append(" : ");
        }
        buildLocalName( identity, new HashSet<TLContextualFacet>() );
        return identity.toString();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLAbstractFacet#getNamespace()
	 */
	@Override
	public String getNamespace() {
		AbstractLibrary library = getOwningLibrary();
		return (library == null) ? null : library.getNamespace();
	}
	
    /**
	 * @see org.opentravel.schemacompiler.model.TLAbstractFacet#setFacetType(org.opentravel.schemacompiler.model.TLFacetType)
	 */
	@Override
	public void setFacetType(TLFacetType facetType) {
		childFacetManager.setChildFacetType( facetType );
		super.setFacetType( facetType );
	}

    /**
     * Returns the value of the 'owningEntityName' field.
     * 
     * @return String
     */
    public String getOwningEntityName() {
        return owningEntityName;
    }

    /**
     * Assigns the value of the 'owningEntityName' field.
     * 
     * @param owningEntityName  the field value to assign
     */
    public void setOwningEntityName(String owningEntityName) {
        this.owningEntityName = owningEntityName;
    }

	/**
	 * @see org.opentravel.schemacompiler.model.TLFacet#setOwningEntity(org.opentravel.schemacompiler.model.TLFacetOwner)
	 */
	@Override
	public void setOwningEntity(TLFacetOwner owningEntity) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.FACET_OWNER_MODIFIED, this)
                .setOldValue(this.getOwningEntity()).setNewValue(owningEntity).buildEvent();

		super.setOwningEntity(owningEntity);
        publishEvent(event);
	}

	/**
	 * Returns the name of this contextual facet.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the name of this contextual facet.
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
	 * @see org.opentravel.schemacompiler.model.TLFacet#getLabel()
	 */
	@Deprecated
	@Override
	public String getLabel() {
		return name;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLFacet#setLabel(java.lang.String)
	 */
	@Deprecated
	@Override
	public void setLabel(String label) {
		setName(label);
	}
	
	/**
	 * Returns the assigned namespace of the contextual facet.
	 * <p>
	 * NOTE: This field is intended for internal use by the OTM compiler and should not
	 * be used by external applications.  Normally, the namespace of a contextual facet
	 * is provided by its owning library; this field is only used when the contextual
	 * facet is stored as a standalone XML document in the OTM repository search index.
	 * 
	 * @return String
	 */
	public String getFacetNamespace() {
		return facetNamespace;
	}

	/**
	 * Assigns the assigned namespace of the contextual facet.
	 * <p>
	 * NOTE: This field is intended for internal use by the OTM compiler and should not
	 * be used by external applications.  Normally, the namespace of a contextual facet
	 * is provided by its owning library; this field is only used when the contextual
	 * facet is stored as a standalone XML document in the OTM repository search index.
	 *
	 * @param facetNamespace  the facet namespace URI to assign
	 */
	public void setFacetNamespace(String facetNamespace) {
		this.facetNamespace = facetNamespace;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLFacetOwner#getAllFacets()
	 */
	@Override
	public List<TLFacet> getAllFacets() {
		List<TLFacet> childFacets = new ArrayList<>();
		
		childFacets.addAll( childFacetManager.getChildren() );
		return Collections.unmodifiableList( childFacets );
	}
	
    /**
     * Returns list of children for this contextual facet.
     * 
     * @return List<TLContextualFacet>
     */
    public List<TLContextualFacet> getChildFacets() {
    	return childFacetManager.getChildren();
    }

    /**
     * Returns the child contextual facet with the specified name.
     * 
     * @param name  the name of the child facet to return
     * @return TLContextualFacet
     */
    public TLContextualFacet getChildFacet(String name) {
        return (TLContextualFacet) childFacetManager.getChild( name );
    }

    /**
     * In cases where multiple child facets of the same name are defined for
     * a contextual facet, this method provides a means of retrieving based on the
     * name and owning library of the child facet.
     * 
     * <p>NOTE: Under normal circumstances, this situation only arises after a new
     * library version has been created which contains non-local contextual facets.
     * 
     * @param facetName  the name of the facet to retrieve
     * @param facetLibrary  the owning library of the facet
     * @return TLContextualFacet
     */
	public TLContextualFacet getChildFacet(String facetName, AbstractLibrary facetLibrary) {
		TLContextualFacet childFacet = null;
		
		for (TLContextualFacet facet : getChildFacets()) {
			if (facetName.equals( facet.getName() ) && (facet.getOwningLibrary() == facetLibrary)) {
				childFacet = facet;
				break;
			}
		}
		return childFacet;
	}
	
    /**
     * Adds a child <code>TLContextualFacet</code> element to the current list.
     * 
     * @param childFacet  the child contextual facet to add
     */
    public void addChildFacet(TLContextualFacet childFacet) {
    	childFacetManager.addChild( childFacet );
    }

    /**
     * Removes the specified child <code>TLContextualFacet</code> from the current list.
     * 
     * @param childFacet  the child contextual facet to remove
     */
    public void removeChildFacet(TLContextualFacet childFacet) {
    	childFacetManager.removeChild( childFacet );
    }
    
    /**
     * Manages lists of <code>TLContextualFacet</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class ContextualFacetListManager extends ChildEntityListManager<TLContextualFacet, TLFacetOwner> {

        private TLFacetType childFacetType;

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner
         *            the owner of the underlying list of children
         * @param addEventType
         *            the type of event to publish when a child entity is added
         * @param removeEventType
         *            the type of event to publish when a child entity is removed
         */
        public ContextualFacetListManager(TLFacetOwner owner, TLFacetType childFacetType,
                ModelEventType addEventType, ModelEventType removeEventType) {
            super(owner, addEventType, removeEventType);
            this.childFacetType = childFacetType;
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLContextualFacet child) {
            return child.getName();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLContextualFacet child, TLFacetOwner owner) {
            child.setFacetType(childFacetType);
            child.setOwningEntity(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLFacetOwner owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

		/**
		 * Assigns the type of the child facets in the managed list.
		 *
		 * @param childFacetType  the facet type to assign
		 */
		public void setChildFacetType(TLFacetType childFacetType) {
			this.childFacetType = childFacetType;
		}

    }

}
