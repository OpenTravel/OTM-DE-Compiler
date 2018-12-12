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
 * Abstract base class for all symbol table populators that build symbols from <code>TLModel</code>
 * OTM entities.
 * 
 * @author S. Livezey
 */
public abstract class AbstractTLSymbolTablePopulator<S> implements SymbolTablePopulator<S> {

	private static final String LIST_SUFFIX = "_List";

	/**
     * Configures the given symbol table by registering derived entity factories for the list facets
     * of core objects.
     * 
     * @param symbols
     *            the symbol table instance to configure
     */
    protected void configureSymbolTable(SymbolTable symbols) {
        symbols.addDerivedEntityFactory(new DerivedEntityFactory<TLCoreObject>() {
            public boolean isOriginatingEntity(Object originatingEntity) {
                return (originatingEntity instanceof TLCoreObject);
            }

            public void registerDerivedEntity(TLCoreObject originatingEntity,
                    String entityNamespace, SymbolTable symbols) {
                symbols.addEntity(entityNamespace, originatingEntity.getLocalName() + "_"
                        + TLFacetType.SIMPLE.getIdentityName() + LIST_SUFFIX,
                        originatingEntity.getSimpleListFacet());
            }
        });

        symbols.addDerivedEntityFactory(new DerivedEntityFactory<TLCoreObject>() {
            public boolean isOriginatingEntity(Object originatingEntity) {
                return (originatingEntity instanceof TLCoreObject);
            }

            public void registerDerivedEntity(TLCoreObject originatingEntity,
                    String entityNamespace, SymbolTable symbols) {
                symbols.addEntity(entityNamespace, originatingEntity.getLocalName() + "_"
                        + TLFacetType.SUMMARY.getIdentityName() + LIST_SUFFIX,
                        originatingEntity.getSummaryListFacet());
            }
        });

        symbols.addDerivedEntityFactory(new DerivedEntityFactory<TLCoreObject>() {
            public boolean isOriginatingEntity(Object originatingEntity) {
                return (originatingEntity instanceof TLCoreObject);
            }

            public void registerDerivedEntity(TLCoreObject originatingEntity,
                    String entityNamespace, SymbolTable symbols) {
                symbols.addEntity(entityNamespace, originatingEntity.getLocalName() + "_"
                        + TLFacetType.DETAIL.getIdentityName() + LIST_SUFFIX,
                        originatingEntity.getDetailListFacet());
            }
        });
    }

    /**
     * Populates the symbol table with all available names from the given library.
     * 
     * @param library
     *            the library from which to load symbols
     * @param symbols
     *            the symbol table to be populated
     */
    protected void populateLibrarySymbols(AbstractLibrary library, SymbolTable symbols) {
        String namespace = library.getNamespace();

        for (NamedEntity libraryMember : library.getNamedMembers()) {
            if ((libraryMember instanceof XSDComplexType) &&
            			(((XSDComplexType) libraryMember).getIdentityAlias() != null)) {
                // Complex types that have an identity alias assigned should be ignored in favor
                // of the global XSD element with the same name
                continue;
            }
            if (!(libraryMember instanceof TLService)) {
                symbols.addEntity(namespace, libraryMember.getLocalName(), libraryMember);
            }

            if (libraryMember instanceof TLBusinessObject) {
                TLBusinessObject owner = (TLBusinessObject) libraryMember;

                if (owner.getIdFacet() != null) {
                    addFacetEntries(owner.getIdFacet(), symbols);
                }
                if (owner.getSummaryFacet() != null) {
                    addFacetEntries(owner.getSummaryFacet(), symbols);
                }
                if (owner.getDetailFacet() != null) {
                    addFacetEntries(owner.getDetailFacet(), symbols);
                }
                for (TLFacet customFacet : owner.getCustomFacets()) {
                    addFacetEntries(customFacet, symbols);
                }
                for (TLFacet queryFacet : owner.getQueryFacets()) {
                    addFacetEntries(queryFacet, symbols);
                }
                addAliasEntries(owner, symbols);
                
            } else if (libraryMember instanceof TLCoreObject) {
                TLCoreObject owner = (TLCoreObject) libraryMember;

                if (owner.getSimpleFacet() != null) {
                    addFacetEntries(owner.getSimpleFacet(), symbols);
                }
                if (owner.getSummaryFacet() != null) {
                    addFacetEntries(owner.getSummaryFacet(), symbols);
                }
                if (owner.getSummaryListFacet() != null) {
                    addFacetEntries(owner.getSummaryListFacet(), symbols);
                }
                if (owner.getDetailFacet() != null) {
                    addFacetEntries(owner.getDetailFacet(), symbols);
                }
                if (owner.getDetailListFacet() != null) {
                    addFacetEntries(owner.getDetailListFacet(), symbols);
                }
                addAliasEntries(owner, symbols);
                symbols.addEntity(namespace,owner.getRoleEnumeration().getLocalName(),
                        owner.getRoleEnumeration());

            } else if (libraryMember instanceof TLChoiceObject) {
            	TLChoiceObject owner = (TLChoiceObject) libraryMember;

                if (owner.getSharedFacet() != null) {
                    addFacetEntries(owner.getSharedFacet(), symbols);
                }
                for (TLFacet choiceFacet : owner.getChoiceFacets()) {
                    addFacetEntries(choiceFacet, symbols);
                }
                addAliasEntries(owner, symbols);
                
            } else if (libraryMember instanceof TLContextualFacet) {
            	addAliasEntries((TLContextualFacet) libraryMember, symbols);
            	
            } else if (libraryMember instanceof TLResource) {
            	TLResource owner = (TLResource) libraryMember;
            	
                for (TLActionFacet actionFacet : owner.getActionFacets()) {
                    symbols.addEntity(namespace, actionFacet.getLocalName(), actionFacet);
                }
                
            } else if (libraryMember instanceof TLService) {
                TLService service = (TLService) libraryMember;

                for (TLOperation operation : service.getOperations()) {
                    if (operation.getRequest() != null) {
                        addFacetEntries(operation.getRequest(), symbols);
                    }
                    if (operation.getResponse() != null) {
                        addFacetEntries(operation.getResponse(), symbols);
                    }
                    if (operation.getNotification() != null) {
                        addFacetEntries(operation.getNotification(), symbols);
                    }
                    symbols.addOperationEntity(namespace, operation.getLocalName(), operation);
                }
            }
        }
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
     * @param entity  the alias owner entity to process
     * @param symbols  the symbol table being constructed
     */
    private static void addFacetEntries(TLAbstractFacet facet, SymbolTable symbols) {
        symbols.addEntity(facet.getNamespace(), facet.getLocalName(), facet);
        
        if (facet instanceof TLAliasOwner) {
            addAliasEntries((TLAliasOwner) facet, symbols);
        }
    }
    
    /**
     * Adds the aliases for the given entity to the symbol table.
     * 
     * @param entity  the alias owner entity to process
     * @param symbols  the symbol table being constructed
     */
    private static void addAliasEntries(TLAliasOwner entity, SymbolTable symbols) {
        String namespace = entity.getNamespace();
        
        for (TLAlias alias : entity.getAliases()) {
            symbols.addEntity(namespace, alias.getLocalName(), alias);
        }
    }

}
