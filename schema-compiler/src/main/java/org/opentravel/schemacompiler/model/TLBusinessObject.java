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

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.model.TLAlias.AliasListManager;
import org.opentravel.schemacompiler.model.TLFacet.FacetListManager;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Business object definition for a type library.
 * 
 * @author S. Livezey
 */
public class TLBusinessObject extends TLComplexTypeBase implements TLFacetOwner, TLAliasOwner {

    protected AliasListManager aliasManager = new AliasListManager(this);
    private FacetListManager customFacetManager = new FacetListManager(this, TLFacetType.CUSTOM,
            ModelEventType.CUSTOM_FACET_ADDED, ModelEventType.CUSTOM_FACET_REMOVED);
    private FacetListManager queryFacetManager = new FacetListManager(this, TLFacetType.QUERY,
            ModelEventType.QUERY_FACET_ADDED, ModelEventType.QUERY_FACET_REMOVED);
    private TLFacet idFacet;
    private TLFacet summaryFacet;
    private TLFacet detailFacet;

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
     * Returns the value of the 'idFacet' field.
     * 
     * @return TLFacet
     */
    public TLFacet getIdFacet() {
        if (idFacet == null) {
            idFacet = new TLFacet();
            idFacet.setFacetType(TLFacetType.ID);
            idFacet.setOwningEntity(this);
        }
        return idFacet;
    }

    /**
     * Assigns the value of the 'idFacet' field.
     * 
     * @param idFacet
     *            the field value to assign
     */
    public void setIdFacet(TLFacet idFacet) {
        if (idFacet != this.idFacet) {
            if (getOwningModel() != null) {
                throw new IllegalStateException(
                        "Facets cannot be modified once their owner has been assigned to a model.");
            }
            if (idFacet != null) {
                idFacet.setFacetType(TLFacetType.ID);
                idFacet.setOwningEntity(this);
            }
            if (this.idFacet != null) {
                this.idFacet.setFacetType(null);
                this.idFacet.setOwningEntity(null);
            }
            this.idFacet = idFacet;
        }
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
            }
            this.summaryFacet = summaryFacet;
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
            }
            this.detailFacet = detailFacet;
        }
    }

    /**
     * Returns the value of the 'customFacets' field.
     * 
     * @return List<TLFacet>
     */
    public List<TLFacet> getCustomFacets() {
        return customFacetManager.getChildren();
    }

    /**
     * Returns the custom facet with the specified context.
     * 
     * @param context
     *            the context of the custom facet to return
     * @return TLFacet
     */
    public TLFacet getCustomFacet(String context) {
        return getCustomFacet(context, null);
    }

    /**
     * Returns the custom facet with the specified context.
     * 
     * @param context
     *            the context of the custom facet to return
     * @param label
     *            the label of the custom facet to return
     * @return TLFacet
     */
    public TLFacet getCustomFacet(String context, String label) {
        StringBuilder contextualName = new StringBuilder();

        contextualName.append((context == null) ? "Unknown" : context);

        if ((label != null) && (label.length() > 0)) {
            contextualName.append(':').append(label);
        }
        return customFacetManager.getChild(contextualName.toString());
    }

    /**
     * Adds a custom <code>TLFacet</code> element to the current list.
     * 
     * @param customFacet
     *            the custom facet value to add
     */
    public void addCustomFacet(TLFacet customFacet) {
        customFacetManager.addChild(customFacet);
    }

    /**
     * Adds a custom <code>TLFacet</code> element to the current list.
     * 
     * @param index
     *            the index at which the given custom facet should be added
     * @param customFacet
     *            the custom facet value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addCustomFacet(int index, TLFacet customFacet) {
        customFacetManager.addChild(index, customFacet);
    }

    /**
     * Removes the specified custom <code>TLFacet</code> from the current list.
     * 
     * @param customFacet
     *            the custom facet value to remove
     */
    public void removeCustomFacet(TLFacet customFacet) {
        customFacetManager.removeChild(customFacet);
    }

    /**
     * Moves this custom facet up by one position in the list. If the custom facet is not owned by
     * this object or it is already at the front of the list, this method has no effect.
     * 
     * @param customFacet
     *            the custom facet to move
     */
    public void moveCustomFacetUp(TLFacet customFacet) {
        customFacetManager.moveUp(customFacet);
    }

    /**
     * Moves this custom facet down by one position in the list. If the custom facet is not owned by
     * this object or it is already at the end of the list, this method has no effect.
     * 
     * @param customFacet
     *            the custom facet to move
     */
    public void moveCustomFacetDown(TLFacet customFacet) {
        customFacetManager.moveDown(customFacet);
    }

    /**
     * Sorts the list of custom facets using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortCustomFacets(Comparator<TLFacet> comparator) {
        customFacetManager.sortChildren(comparator);
    }

    /**
     * Returns the value of the 'queryFacets' field.
     * 
     * @return List<TLFacet>
     */
    public List<TLFacet> getQueryFacets() {
        return queryFacetManager.getChildren();
    }

    /**
     * Returns the query facet with the specified name.
     * 
     * @param context
     *            the context of the query facet to return
     * @return TLFacet
     */
    public TLFacet getQueryFacet(String context) {
        return getQueryFacet(context, null);
    }

    /**
     * Returns the query facet with the specified name.
     * 
     * @param context
     *            the context of the query facet to return
     * @param label
     *            the label of the query facet to return
     * @return TLFacet
     */
    public TLFacet getQueryFacet(String context, String label) {
        StringBuilder contextualName = new StringBuilder();

        contextualName.append((context == null) ? "Unknown" : context);

        if ((label != null) && (label.length() > 0)) {
            contextualName.append(':').append(label);
        }
        return queryFacetManager.getChild(contextualName.toString());
    }

    /**
     * Adds a query <code>TLFacet</code> element to the current list.
     * 
     * @param queryFacet
     *            the query facet value to add
     */
    public void addQueryFacet(TLFacet queryFacet) {
        queryFacetManager.addChild(queryFacet);
    }

    /**
     * Adds a query <code>TLFacet</code> element to the current list.
     * 
     * @param index
     *            the index at which the given query facet should be added
     * @param queryFacet
     *            the query facet value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addQueryFacet(int index, TLFacet queryFacet) {
        queryFacetManager.addChild(index, queryFacet);
    }

    /**
     * Removes the specified query <code>TLFacet</code> from the current list.
     * 
     * @param queryFacet
     *            the query facet value to remove
     */
    public void removeQueryFacet(TLFacet queryFacet) {
        queryFacetManager.removeChild(queryFacet);
    }

    /**
     * Moves this query facet up by one position in the list. If the query facet is not owned by
     * this object or it is already at the front of the list, this method has no effect.
     * 
     * @param queryFacet
     *            the query facet to move
     */
    public void moveQueryFacetUp(TLFacet queryFacet) {
        queryFacetManager.moveUp(queryFacet);
    }

    /**
     * Moves this query facet down by one position in the list. If the query facet is not owned by
     * this object or it is already at the end of the list, this method has no effect.
     * 
     * @param queryFacet
     *            the query facet to move
     */
    public void moveQueryFacetDown(TLFacet queryFacet) {
        queryFacetManager.moveDown(queryFacet);
    }

    /**
     * Sorts the list of query facets using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortQueryFacets(Comparator<TLFacet> comparator) {
        queryFacetManager.sortChildren(comparator);
    }

}
