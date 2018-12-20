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

import org.opentravel.schemacompiler.model.TLAlias.AliasListManager;
import org.opentravel.schemacompiler.util.XSDFacetProfileLocator;

/**
 * Facet that provides a named entity reference for a list whose type is determined by the existence
 * of another <code>TLFacet</code> entity.
 * 
 * <p>
 * NOTE: The aliases of a <code>TLListFacet</code> are not intended to be manipulated by external
 * components or services. Instead, they are synchronized with the aliases of the facet from which
 * the facet list is derived. Any direct manipulation is likely to result in synchronization
 * problems between the two lists.
 * 
 * @author S. Livezey
 */
public class TLListFacet extends TLAbstractFacet implements TLAttributeType, TLAliasOwner {

	private static final String FACET_OPERATION_NOT_SUPPORTED = "Operation not supported for list facets.";
	
	private AliasListManager aliasManager = new AliasListManager(this);
    private TLAbstractFacet itemFacet;

    /**
     * Constructor that specifies the single-item facet from which this facet is derived.
     * 
     * @param itemFacet
     *            the single-item facet that implies the existence of this list facet
     */
    public TLListFacet(TLAbstractFacet itemFacet) {
        if (itemFacet == null) {
            throw new NullPointerException("The 'itemFacet' for a TLListFacet cannot be null.");
        }
        this.itemFacet = itemFacet;

        if (itemFacet instanceof TLFacet) {
            AliasListManager itemFacetAliasManager = ((TLFacet) itemFacet).getAliasManager();
            FacetListAliasManager facetListAliasManager = new FacetListAliasManager();

            itemFacetAliasManager.addDerivedListManager(facetListAliasManager);
        }
    }

    /**
     * Returns the single-item facet that represents the element type of items in this facet's list.
     * 
     * @return TLAbstractFacet
     */
    public TLAbstractFacet getItemFacet() {
        return itemFacet;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
     */
    @Override
    public XSDFacetProfile getXSDFacetProfile() {
        return (itemFacet instanceof TLSimpleFacet)
        		? XSDFacetProfileLocator.getXSDFacetProfile((TLSimpleFacet) itemFacet)
        				: XSDFacetProfile.FP_UNKNOWN;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAbstractFacet#declaresContent()
     */
    @Override
    public boolean declaresContent() {
        return (itemFacet != null) && itemFacet.declaresContent();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAbstractFacet#getOwningEntity()
     */
    @Override
    public TLFacetOwner getOwningEntity() {
        return (itemFacet == null) ? null : itemFacet.getOwningEntity();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return (itemFacet == null) ? null : itemFacet.getOwningModel();
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (itemFacet == null) ? null : itemFacet.getOwningLibrary();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAbstractFacet#getNamespace()
     */
    @Override
    public String getNamespace() {
        return (itemFacet == null) ? null : itemFacet.getNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAbstractFacet#getFacetType()
     */
    @Override
    public TLFacetType getFacetType() {
        return (itemFacet == null) ? null : itemFacet.getFacetType();
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if ((itemFacet == null) || (itemFacet.getOwningEntity() == null)) {
            identity.append("[Unnamed List Facet]");
        } else {
            identity.append(itemFacet.getValidationIdentity()).append("-List");
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
     */
    @Override
    public String getLocalName() {
        return (itemFacet == null) ? null : (itemFacet.getLocalName() + "_List");
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAbstractFacet#setFacetType(org.opentravel.schemacompiler.model.TLFacetType)
     */
    @Override
    public void setFacetType(TLFacetType facetType) {
        throw new UnsupportedOperationException(FACET_OPERATION_NOT_SUPPORTED);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAbstractFacet#setDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
     */
    @Override
    public void setDocumentation(TLDocumentation documentation) {
        throw new UnsupportedOperationException(FACET_OPERATION_NOT_SUPPORTED);
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
        throw new UnsupportedOperationException(FACET_OPERATION_NOT_SUPPORTED);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#addAlias(int,
     *      org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void addAlias(int index, TLAlias alias) {
        throw new UnsupportedOperationException(FACET_OPERATION_NOT_SUPPORTED);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#removeAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    public void removeAlias(TLAlias alias) {
        throw new UnsupportedOperationException(FACET_OPERATION_NOT_SUPPORTED);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#moveUp(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void moveUp(TLAlias alias) {
        throw new UnsupportedOperationException(FACET_OPERATION_NOT_SUPPORTED);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#moveDown(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void moveDown(TLAlias alias) {
        throw new UnsupportedOperationException(FACET_OPERATION_NOT_SUPPORTED);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAliasOwner#sortAliases(java.util.Comparator)
     */
    @Override
    public void sortAliases(Comparator<TLAlias> comparator) {
        throw new UnsupportedOperationException(FACET_OPERATION_NOT_SUPPORTED);
    }

    /**
	 * @see org.opentravel.schemacompiler.model.TLAliasOwner#getAliasListManager()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ChildEntityListManager<TLAlias,TLAliasOwner> getAliasListManager() {
		return aliasManager;
	}

    /**
     * List manager that synchronizes the list of derived aliases with those of the corresponding
     * 'itemFacet'.
     * 
     * @author S. Livezey
     */
    private class FacetListAliasManager extends DerivedChildEntityListManager<TLAlias, TLAlias> {

        /**
         * Default constructor.
         */
        public FacetListAliasManager() {
            super(aliasManager);
        }

        /**
         * @see org.opentravel.schemacompiler.model.DerivedChildEntityListManager#getOriginalEntityName(java.lang.Object)
         */
        @Override
        protected String getOriginalEntityName(TLAlias originalEntity) {
            return (originalEntity == null) ? null : originalEntity.getName();
        }

        /**
         * @see org.opentravel.schemacompiler.model.DerivedChildEntityListManager#getDerivedEntityName(java.lang.String)
         */
        @Override
        protected String getDerivedEntityName(String originalEntityName) {
            return (originalEntityName == null) ? null : (originalEntityName + "_List");
        }

        /**
		 * @see org.opentravel.schemacompiler.model.DerivedChildEntityListManager#setDerivedEntityName(java.lang.Object, java.lang.String)
		 */
		@Override
		protected void setDerivedEntityName(TLAlias derivedEntity, String derivedEntityName) {
			if (derivedEntity != null) {
				derivedEntity.setName(derivedEntityName);
			}
		}

		/**
         * @see org.opentravel.schemacompiler.model.DerivedChildEntityListManager#createDerivedEntity(java.lang.Object)
         */
        @Override
        protected TLAlias createDerivedEntity(TLAlias originalEntity) {
            TLAlias derivedAlias = new TLAlias();

            derivedAlias.setName(getDerivedEntityName(getOriginalEntityName(originalEntity)));
            return derivedAlias;
        }

    }

}
