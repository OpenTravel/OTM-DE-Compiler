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
import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.model.TLAlias.AliasListManager;
import org.opentravel.schemacompiler.model.TLContextualFacet.ContextualFacetListManager;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Choice object definition for a type library.
 * 
 * @author S. Livezey
 */
public class TLChoiceObject extends TLComplexTypeBase implements TLFacetOwner, TLAliasOwner {
	
    protected AliasListManager aliasManager = new AliasListManager(this);
    private ContextualFacetListManager choiceFacetManager = new ContextualFacetListManager(this, TLFacetType.CHOICE,
            ModelEventType.CHOICE_FACET_ADDED, ModelEventType.CHOICE_FACET_REMOVED);
    private TLFacet sharedFacet;
    
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
        if (getName() == null) {
            identity.append("[Unnamed Business Object Type]");
        } else {
            identity.append(getName());
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
	 * @see org.opentravel.schemacompiler.model.TLLibraryMember#setOwningLibrary(org.opentravel.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public void setOwningLibrary(AbstractLibrary owningLibrary) {
    	if (!OTM16Upgrade.otm16Enabled) {
    		for (TLContextualFacet facet : getChoiceFacets()) {
    			if (owningLibrary != null) {
        			owningLibrary.addNamedMember( facet );
    			} else {
    				this.getOwningLibrary().removeNamedMember( facet );
    			}
    		}
    	}
		super.setOwningLibrary(owningLibrary);
	}

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#getAliases()
     */
    public List<TLAlias> getAliases() {
        return aliasManager.getChildren();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#getAlias(java.lang.String)
     */
    public TLAlias getAlias(String aliasName) {
        return aliasManager.getChild(aliasName);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#addAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    public void addAlias(TLAlias alias) {
        aliasManager.addChild(alias);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#addAlias(int,
     *      org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void addAlias(int index, TLAlias alias) {
        aliasManager.addChild(index, alias);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#removeAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    public void removeAlias(TLAlias alias) {
        aliasManager.removeChild(alias);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#moveUp(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void moveUp(TLAlias alias) {
        aliasManager.moveUp(alias);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#moveDown(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void moveDown(TLAlias alias) {
        aliasManager.moveDown(alias);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#sortAliases(java.util.Comparator)
     */
    @Override
    public void sortAliases(Comparator<TLAlias> comparator) {
        aliasManager.sortChildren(comparator);
    }

    /**
	 * @see org.opentravel.schemacompiler.model.TLAliasOwner#getAliasListManager()
	 */
	@Override
	public ChildEntityListManager<TLAlias, ?> getAliasListManager() {
		return aliasManager;
	}

    /**
	 * @see org.opentravel.schemacompiler.model.TLFacetOwner#getAllFacets()
	 */
	@Override
	public List<TLFacet> getAllFacets() {
		List<TLFacet> facetList = new ArrayList<>();
		
		facetList.add( getSharedFacet() );
		
		for (TLFacet facet : getChoiceFacets()) {
			facetList.add( facet );
		}
		return facetList;
	}

    /**
     * Returns the value of the 'sharedFacet' field.
     * 
     * @return TLFacet
     */
    public TLFacet getSharedFacet() {
        if (sharedFacet == null) {
        	sharedFacet = new TLFacet();
            sharedFacet.setFacetType(TLFacetType.SHARED);
            sharedFacet.setOwningEntity(this);
        }
        return sharedFacet;
    }

    /**
     * Assigns the value of the 'sharedFacet' field.
     * 
     * @param sharedFacet  the field value to assign
     */
    public void setSharedFacet(TLFacet sharedFacet) {
        if (sharedFacet != this.sharedFacet) {
            if (getOwningModel() != null) {
                throw new IllegalStateException(
                        "Facets cannot be modified once their owner has been assigned to a model.");
            }
            if (sharedFacet != null) {
            	sharedFacet.setFacetType(TLFacetType.SHARED);
            	sharedFacet.setOwningEntity(this);
            }
            if (this.sharedFacet != null) {
                this.sharedFacet.setFacetType(null);
                this.sharedFacet.setOwningEntity(null);
            }
            this.sharedFacet = sharedFacet;
        }
    }

    /**
     * Returns the list of choice facets for this choice object.
     * 
     * @return List<TLContextualFacet>
     */
    public List<TLContextualFacet> getChoiceFacets() {
        return choiceFacetManager.getChildren();
    }

    /**
     * Returns the choice facet with the specified name.
     * 
     * @param name  the context of the choice facet to return
     * @return TLContextualFacet
     */
    public TLContextualFacet getChoiceFacet(String name) {
        return choiceFacetManager.getChild(name);
    }

    /**
     * Returns the choice facet with the specified name.
     * 
     * @param context  the context of the choice facet to return
     * @param label  the label of the choice facet to return
     * @return TLContextualFacet
     * @deprecated  Use the {@link #getChoiceFacet(String)} method instead
     */
    @Deprecated
    public TLContextualFacet getChoiceFacet(String context, String label) {
        StringBuilder contextualName = new StringBuilder();

        contextualName.append((context == null) ? "Unknown" : context);

        if ((label != null) && (label.length() > 0)) {
            contextualName.append(':').append(label);
        }
        return choiceFacetManager.getChild(contextualName.toString());
    }

    /**
     * Adds a choice <code>TLContextualFacet</code> element to the current list.
     * 
     * @param choiceFacet  the choice facet value to add
     */
    public void addChoiceFacet(TLContextualFacet choiceFacet) {
    	contextualFacetAdded(choiceFacet);
    	choiceFacetManager.addChild(choiceFacet);
    }

    /**
     * In cases where multiple choice facets of the same name are defined for
     * a choice object, this method provides a means of retrieving based on the
     * name and owning library of the facet.
     * 
     * <p>NOTE: Under normal circumstances, this situation only arises after a new
     * library version has been created which contains non-local contextual facets.
     * 
     * @param facetName  the name of the facet to retrieve
     * @param facetLibrary  the owning library of the facet
     * @return TLContextualFacet
     */
	public TLContextualFacet getChoiceFacet(String facetName, AbstractLibrary facetLibrary) {
		TLContextualFacet childFacet = null;
		
		for (TLContextualFacet facet : getChoiceFacets()) {
			if (facetName.equals( facet.getName() ) && (facet.getOwningLibrary() == facetLibrary)) {
				childFacet = facet;
				break;
			}
		}
		return childFacet;
	}
	
    /**
     * Adds a choice <code>TLContextualFacet</code> element to the current list.
     * 
     * @param index  the index at which the given choice facet should be added
     * @param choiceFacet  the choice facet value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addChoiceFacet(int index, TLContextualFacet choiceFacet) {
    	contextualFacetAdded(choiceFacet);
    	choiceFacetManager.addChild(index, choiceFacet);
    }

    /**
     * Removes the specified choice <code>TLContextualFacet</code> from the current list.
     * 
     * @param choiceFacet  the choice facet value to remove
     */
    public void removeChoiceFacet(TLContextualFacet choiceFacet) {
    	contextualFacetRemoved(choiceFacet);
    	choiceFacetManager.removeChild(choiceFacet);
    }

    /**
     * Moves this choice facet up by one position in the list. If the choice facet is not owned by
     * this object or it is already at the front of the list, this method has no effect.
     * 
     * @param choiceFacet  the choice facet to move
     */
    public void moveChoiceFacetUp(TLContextualFacet choiceFacet) {
    	choiceFacetManager.moveUp(choiceFacet);
    }

    /**
     * Moves this choice facet down by one position in the list. If the choice facet is not owned by
     * this object or it is already at the end of the list, this method has no effect.
     * 
     * @param choiceFacet  the choice facet to move
     */
    public void moveChoiceFacetDown(TLContextualFacet choiceFacet) {
    	choiceFacetManager.moveDown(choiceFacet);
    }

    /**
     * Sorts the list of choice facets using the comparator provided.
     * 
     * @param comparator  the comparator to use when sorting the list
     */
    public void sortChoiceFacets(Comparator<TLContextualFacet> comparator) {
    	choiceFacetManager.sortChildren(comparator);
    }

}
