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

import org.opentravel.schemacompiler.model.TLAlias.AliasListManager;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Core object definition for a type library.
 * 
 * @author S. Livezey
 */
public class TLCoreObject extends TLComplexTypeBase implements TLFacetOwner, TLAliasOwner, TLAttributeType {

    protected AliasListManager aliasManager = new AliasListManager(this);
    private TLRoleEnumeration roleEnumeration = new TLRoleEnumeration(this);
    private TLSimpleFacet simpleFacet;
    private TLFacet summaryFacet;
    private TLFacet detailFacet;
    private TLListFacet simpleListFacet;
    private TLListFacet summaryListFacet;
    private TLListFacet detailListFacet;

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
            identity.append("[Unnamed Core Object Type]");
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
     * @see org.opentravel.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
     */
    @Override
    public XSDFacetProfile getXSDFacetProfile() {
        return (simpleFacet == null) ? XSDFacetProfile.FP_unknown : simpleFacet.getXSDFacetProfile();
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
     * Returns the role enumeration entity associated with this core object instance.
     * 
     * @return TLRoleEnumeration
     */
    public TLRoleEnumeration getRoleEnumeration() {
        return roleEnumeration;
    }

    /**
     * Returns the value of the 'simpleFacet' field.
     * 
     * @return TLSimpleFacet
     */
    public TLSimpleFacet getSimpleFacet() {
        if (simpleFacet == null) {
            simpleFacet = new TLSimpleFacet();
            simpleFacet.setFacetType(TLFacetType.SIMPLE);
            simpleFacet.setOwningEntity(this);
            simpleListFacet = new TLListFacet(simpleFacet);
        }
        return simpleFacet;
    }

    /**
     * Assigns the value of the 'simpleFacet' field.
     * 
     * @param simpleFacet
     *            the field value to assign
     */
    public void setSimpleFacet(TLSimpleFacet simpleFacet) {
        if (simpleFacet != this.simpleFacet) {
            if (getOwningModel() != null) {
                throw new IllegalStateException(
                        "Facets cannot be modified once their owner has been assigned to a model.");
            }
            if (simpleFacet != null) {
                simpleFacet.setFacetType(TLFacetType.SIMPLE);
                simpleFacet.setOwningEntity(this);
            }
            if (this.simpleFacet != null) {
                this.simpleFacet.setFacetType(null);
                this.simpleFacet.setOwningEntity(null);
                this.simpleListFacet.setOwningEntity(null);
            }
            this.simpleFacet = simpleFacet;
            this.simpleListFacet = (simpleFacet == null) ? null : new TLListFacet(simpleFacet);
        }
    }

    /**
	 * @see org.opentravel.schemacompiler.model.TLFacetOwner#getAllFacets()
	 */
	@Override
	public List<TLFacet> getAllFacets() {
		List<TLFacet> facetList = new ArrayList<>();
		
		facetList.add( getSummaryFacet() );
		facetList.add( getDetailFacet() );
		return facetList;
	}

    /**
     * Returns the value of the 'summaryFacet' field.
     * 
     * @return TLFacet
     */
    public TLFacet getSummaryFacet() {
        if (summaryFacet == null) {
            summaryFacet = new TLFacet();
            summaryFacet.setFacetType(TLFacetType.SUMMARY);
            summaryFacet.setOwningEntity(this);
            summaryListFacet = new TLListFacet(summaryFacet);
        }
        return summaryFacet;
    }

    /**
     * Assigns the value of the 'summaryFacet' field.
     * 
     * @param summaryFacet
     *            the field value to assign
     */
    public void setSummaryFacet(TLFacet summaryFacet) {
        if (summaryFacet != this.summaryFacet) {
            if (getOwningModel() != null) {
                throw new IllegalStateException(
                        "Facets cannot be modified once their owner has been assigned to a model.");
            }
            if (summaryFacet != null) {
                summaryFacet.setFacetType(TLFacetType.SUMMARY);
                summaryFacet.setOwningEntity(this);
            }
            if (this.summaryFacet != null) {
                this.summaryFacet.setFacetType(null);
                this.summaryFacet.setOwningEntity(null);
                this.summaryListFacet.setOwningEntity(null);
            }
            this.summaryFacet = summaryFacet;
            this.summaryListFacet = (summaryFacet == null) ? null : new TLListFacet(summaryFacet);
        }
    }

    /**
     * Returns the value of the 'detailFacet' field.
     * 
     * @return TLFacet
     */
    public TLFacet getDetailFacet() {
        if (detailFacet == null) {
            detailFacet = new TLFacet();
            detailFacet.setFacetType(TLFacetType.DETAIL);
            detailFacet.setOwningEntity(this);
            detailListFacet = new TLListFacet(detailFacet);
        }
        return detailFacet;
    }

    /**
     * Assigns the value of the 'detailFacet' field.
     * 
     * @param detailFacet
     *            the field value to assign
     */
    public void setDetailFacet(TLFacet detailFacet) {
        if (detailFacet != this.detailFacet) {
            if (getOwningModel() != null) {
                throw new IllegalStateException(
                        "Facets cannot be modified once their owner has been assigned to a model.");
            }
            if (detailFacet != null) {
                detailFacet.setFacetType(TLFacetType.DETAIL);
                detailFacet.setOwningEntity(this);
            }
            if (this.detailFacet != null) {
                this.detailFacet.setFacetType(null);
                this.detailFacet.setOwningEntity(null);
                this.detailListFacet.setOwningEntity(null);
            }
            this.detailFacet = detailFacet;
            this.detailListFacet = (detailFacet == null) ? null : new TLListFacet(detailFacet);
        }
    }

    /**
     * Returns the value of the 'simpleListFacet' field.
     * 
     * @return TLListFacet
     */
    public TLListFacet getSimpleListFacet() {
        getSimpleFacet(); // forces auto-creation if null
        return simpleListFacet;
    }

    /**
     * Returns the value of the 'summaryListFacet' field.
     * 
     * @return TLListFacet
     */
    public TLListFacet getSummaryListFacet() {
        getSummaryFacet(); // forces auto-creation if null
        return summaryListFacet;
    }

    /**
     * Returns the value of the 'detailListFacet' field.
     * 
     * @return TLListFacet
     */
    public TLListFacet getDetailListFacet() {
        getDetailFacet(); // forces auto-creation if null
        return detailListFacet;
    }

}
