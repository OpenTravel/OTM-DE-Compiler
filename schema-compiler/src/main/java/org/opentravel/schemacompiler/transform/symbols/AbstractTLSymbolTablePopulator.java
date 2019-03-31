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

package org.opentravel.schemacompiler.transform.symbols;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.transform.DerivedEntityFactory;
import org.opentravel.schemacompiler.transform.SymbolTable;

/**
 * Abstract base class for all symbol table populators that build symbols from <code>TLModel</code> OTM entities.
 * 
 * @author S. Livezey
 */
public abstract class AbstractTLSymbolTablePopulator<S> implements SymbolTablePopulator<S> {

    private static final String LIST_SUFFIX = "_List";

    /**
     * Configures the given symbol table by registering derived entity factories for the list facets of core objects.
     * 
     * @param symbols the symbol table instance to configure
     */
    protected void configureSymbolTable(SymbolTable symbols) {
        symbols.addDerivedEntityFactory( new DerivedEntityFactory<TLCoreObject>() {
            public boolean isOriginatingEntity(Object originatingEntity) {
                return (originatingEntity instanceof TLCoreObject);
            }

            public void registerDerivedEntity(TLCoreObject originatingEntity, String entityNamespace,
                SymbolTable symbols) {
                symbols.addEntity( entityNamespace,
                    originatingEntity.getLocalName() + "_" + TLFacetType.SIMPLE.getIdentityName() + LIST_SUFFIX,
                    originatingEntity.getSimpleListFacet() );
            }
        } );

        symbols.addDerivedEntityFactory( new DerivedEntityFactory<TLCoreObject>() {
            public boolean isOriginatingEntity(Object originatingEntity) {
                return (originatingEntity instanceof TLCoreObject);
            }

            public void registerDerivedEntity(TLCoreObject originatingEntity, String entityNamespace,
                SymbolTable symbols) {
                symbols.addEntity( entityNamespace,
                    originatingEntity.getLocalName() + "_" + TLFacetType.SUMMARY.getIdentityName() + LIST_SUFFIX,
                    originatingEntity.getSummaryListFacet() );
            }
        } );

        symbols.addDerivedEntityFactory( new DerivedEntityFactory<TLCoreObject>() {
            public boolean isOriginatingEntity(Object originatingEntity) {
                return (originatingEntity instanceof TLCoreObject);
            }

            public void registerDerivedEntity(TLCoreObject originatingEntity, String entityNamespace,
                SymbolTable symbols) {
                symbols.addEntity( entityNamespace,
                    originatingEntity.getLocalName() + "_" + TLFacetType.DETAIL.getIdentityName() + LIST_SUFFIX,
                    originatingEntity.getDetailListFacet() );
            }
        } );
    }

    /**
     * Populates the symbol table with all available names from the given library.
     * 
     * @param library the library from which to load symbols
     * @param symbols the symbol table to be populated
     */
    protected void populateLibrarySymbols(AbstractLibrary library, SymbolTable symbols) {
        String namespace = library.getNamespace();

        for (NamedEntity libraryMember : library.getNamedMembers()) {
            addMemberEntries( libraryMember, namespace, symbols );
        }
    }

    /**
     * Adds all required symbol table entries for the given library member.
     * 
     * @param libraryMember the library member for which entries are to be added
     * @param namespace the namespace of the entries to add
     * @param symbols the symbol table to which the entries will be added
     */
    private void addMemberEntries(NamedEntity libraryMember, String namespace, SymbolTable symbols) {
        if ((libraryMember instanceof XSDComplexType)
            && (((XSDComplexType) libraryMember).getIdentityAlias() != null)) {
            return;
        }
        if (!(libraryMember instanceof TLService)) {
            symbols.addEntity( namespace, libraryMember.getLocalName(), libraryMember );
        }

        if (libraryMember instanceof TLBusinessObject) {
            addBusinessObjectEntries( (TLBusinessObject) libraryMember, symbols );

        } else if (libraryMember instanceof TLCoreObject) {
            addCoreObjectEntries( (TLCoreObject) libraryMember, namespace, symbols );

        } else if (libraryMember instanceof TLChoiceObject) {
            addChoiceObjectEntries( (TLChoiceObject) libraryMember, symbols );

        } else if (libraryMember instanceof TLContextualFacet) {
            addAliasEntries( (TLContextualFacet) libraryMember, symbols );

        } else if (libraryMember instanceof TLResource) {
            TLResource owner = (TLResource) libraryMember;

            for (TLActionFacet actionFacet : owner.getActionFacets()) {
                symbols.addEntity( namespace, actionFacet.getLocalName(), actionFacet );
            }

        } else if (libraryMember instanceof TLService) {
            TLService service = (TLService) libraryMember;

            for (TLOperation operation : service.getOperations()) {
                addOperationFacets( operation, namespace, symbols );
            }
        }
    }

    /**
     * Adds entries for all facets of the given business object.
     * 
     * @param bo the business object whose facets are to be added
     * @param symbols the symbol table to which the entries will be added
     */
    private void addBusinessObjectEntries(TLBusinessObject bo, SymbolTable symbols) {
        if (bo.getIdFacet() != null) {
            addFacetEntries( bo.getIdFacet(), symbols );
        }
        if (bo.getSummaryFacet() != null) {
            addFacetEntries( bo.getSummaryFacet(), symbols );
        }
        if (bo.getDetailFacet() != null) {
            addFacetEntries( bo.getDetailFacet(), symbols );
        }
        for (TLFacet customFacet : bo.getCustomFacets()) {
            addFacetEntries( customFacet, symbols );
        }
        for (TLFacet queryFacet : bo.getQueryFacets()) {
            addFacetEntries( queryFacet, symbols );
        }
        addAliasEntries( bo, symbols );
    }

    /**
     * Adds entries for all facets of the given core object.
     * 
     * @param core the core object whose facets are to be added
     * @param namespace the namespace of the entries to add
     * @param symbols the symbol table to which the entries will be added
     */
    private void addCoreObjectEntries(TLCoreObject core, String namespace, SymbolTable symbols) {
        if (core.getSimpleFacet() != null) {
            addFacetEntries( core.getSimpleFacet(), symbols );
        }
        if (core.getSummaryFacet() != null) {
            addFacetEntries( core.getSummaryFacet(), symbols );
        }
        if (core.getSummaryListFacet() != null) {
            addFacetEntries( core.getSummaryListFacet(), symbols );
        }
        if (core.getDetailFacet() != null) {
            addFacetEntries( core.getDetailFacet(), symbols );
        }
        if (core.getDetailListFacet() != null) {
            addFacetEntries( core.getDetailListFacet(), symbols );
        }
        addAliasEntries( core, symbols );
        symbols.addEntity( namespace, core.getRoleEnumeration().getLocalName(), core.getRoleEnumeration() );
    }

    /**
     * Adds entries for all facets of the given choice object.
     * 
     * @param choice the choice object whose facets are to be added
     * @param symbols the symbol table to which the entries will be added
     */
    private void addChoiceObjectEntries(TLChoiceObject choice, SymbolTable symbols) {
        if (choice.getSharedFacet() != null) {
            addFacetEntries( choice.getSharedFacet(), symbols );
        }
        for (TLFacet choiceFacet : choice.getChoiceFacets()) {
            addFacetEntries( choiceFacet, symbols );
        }
        addAliasEntries( choice, symbols );
    }

    /**
     * Adds entries for all facets of the given operation.
     * 
     * @param operation the operation whose facets are to be added
     * @param namespace the namespace of the entries to add
     * @param symbols the symbol table to which the entries will be added
     */
    private void addOperationFacets(TLOperation operation, String namespace, SymbolTable symbols) {
        if (operation.getRequest() != null) {
            addFacetEntries( operation.getRequest(), symbols );
        }
        if (operation.getResponse() != null) {
            addFacetEntries( operation.getResponse(), symbols );
        }
        if (operation.getNotification() != null) {
            addFacetEntries( operation.getNotification(), symbols );
        }
        symbols.addOperationEntity( namespace, operation.getLocalName(), operation );
    }

    /**
     * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#getLocalName(java.lang.Object)
     */
    @Override
    public String getLocalName(Object sourceObject) {
        String localName = null;

        if (sourceObject instanceof NamedEntity) {
            localName = ((NamedEntity) sourceObject).getLocalName();
        }
        return localName;
    }

    /**
     * Adds the given facet entries to the symbol table.
     * 
     * @param entity the alias owner entity to process
     * @param symbols the symbol table being constructed
     */
    private static void addFacetEntries(TLAbstractFacet facet, SymbolTable symbols) {
        symbols.addEntity( facet.getNamespace(), facet.getLocalName(), facet );

        if (facet instanceof TLAliasOwner) {
            addAliasEntries( (TLAliasOwner) facet, symbols );
        }
    }

    /**
     * Adds the aliases for the given entity to the symbol table.
     * 
     * @param entity the alias owner entity to process
     * @param symbols the symbol table being constructed
     */
    private static void addAliasEntries(TLAliasOwner entity, SymbolTable symbols) {
        String namespace = entity.getNamespace();

        for (TLAlias alias : entity.getAliases()) {
            symbols.addEntity( namespace, alias.getLocalName(), alias );
        }
    }

}
