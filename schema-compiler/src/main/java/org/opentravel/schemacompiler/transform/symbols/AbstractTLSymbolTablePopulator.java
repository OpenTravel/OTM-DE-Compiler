/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.transform.symbols;

import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;
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
	
	/**
	 * Configures the given symbol table by registering derived entity factories for the list facets
	 * of core objects.
	 * 
	 * @param symbols  the symbol table instance to configure
	 */
	protected void configureSymbolTable(SymbolTable symbols) {
		symbols.addDerivedEntityFactory(new DerivedEntityFactory<TLCoreObject>() {
			public boolean isOriginatingEntity(Object originatingEntity) {
				return (originatingEntity instanceof TLCoreObject);
			}
			public void registerDerivedEntity(TLCoreObject originatingEntity, String entityNamespace, SymbolTable symbols) {
				symbols.addEntity(entityNamespace,
						originatingEntity.getLocalName() + "_" + TLFacetType.SIMPLE.getIdentityName() + "_List",
						originatingEntity.getSimpleListFacet());
			}
		});
		
		symbols.addDerivedEntityFactory(new DerivedEntityFactory<TLCoreObject>() {
			public boolean isOriginatingEntity(Object originatingEntity) {
				return (originatingEntity instanceof TLCoreObject);
			}
			public void registerDerivedEntity(TLCoreObject originatingEntity, String entityNamespace, SymbolTable symbols) {
				symbols.addEntity(entityNamespace,
						originatingEntity.getLocalName() + "_" + TLFacetType.SUMMARY.getIdentityName() + "_List",
						originatingEntity.getSummaryListFacet());
			}
		});
		
		symbols.addDerivedEntityFactory(new DerivedEntityFactory<TLCoreObject>() {
			public boolean isOriginatingEntity(Object originatingEntity) {
				return (originatingEntity instanceof TLCoreObject);
			}
			public void registerDerivedEntity(TLCoreObject originatingEntity, String entityNamespace, SymbolTable symbols) {
				symbols.addEntity(entityNamespace,
						originatingEntity.getLocalName() + "_" + TLFacetType.DETAIL.getIdentityName() + "_List",
						originatingEntity.getDetailListFacet());
			}
		});
	}
	
	/**
	 * Populates the symbol table with all available names from the given library.
	 * 
	 * @param library  the library from which to load symbols
	 * @param symbols  the symbol table to be populated
	 */
	protected void populateLibrarySymbols(AbstractLibrary library, SymbolTable symbols) {
		String namespace = library.getNamespace();
		
		for (NamedEntity libraryMember : library.getNamedMembers()) {
			if (libraryMember instanceof XSDComplexType) {
				// Complex types that have an identity alias assigned should be ignored in favor
				// of the global XSD element with the same name
				if ( ((XSDComplexType) libraryMember).getIdentityAlias() != null) {
					continue;
				}
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
				for (TLAlias alias : owner.getAliases()) {
					symbols.addEntity(namespace, alias.getLocalName(), alias);
				}
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
				for (TLAlias alias : owner.getAliases()) {
					symbols.addEntity(namespace, alias.getLocalName(), alias);
				}
				symbols.addEntity(namespace, owner.getRoleEnumeration().getLocalName(),
						owner.getRoleEnumeration());
				
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
	 * Adds the required symbol table entries for the given facet.
	 * 
	 * <p>NOTE:  For contextual facets and facet aliases, we also need to add a second name to the symbol
	 * table that will represent the "old style" of local names.  This will prevent us from dropping references
	 * in existing OTM files that were authored before the change in naming conventions was implemented.
	 * 
	 * @param facet  the facet instance to process
	 * @param symbols  the symbol table being constructed
	 */
	private static void addFacetEntries(TLAbstractFacet facet, SymbolTable symbols) {
		String namespace = facet.getNamespace();
		TLFacet contextualFacet = null;
		
		if ((facet instanceof TLFacet) && ((TLFacet) facet).getFacetType().isContextual()) {
			contextualFacet = (TLFacet) facet;
		}
		
		if (contextualFacet != null) {
			symbols.addEntity(namespace, getContextualFacetLegacyName(contextualFacet, null), facet);
		}
		symbols.addEntity(namespace, facet.getLocalName(), facet);
		
		if (facet instanceof TLAliasOwner) {
			for (TLAlias alias : ((TLAliasOwner) facet).getAliases()) {
				if (contextualFacet != null) {
					symbols.addEntity(namespace, getContextualFacetLegacyName(contextualFacet, alias), facet);
				}
				symbols.addEntity(namespace, alias.getLocalName(), alias);
			}
		}
		
	}
	
	/**
	 * Returns the legacy "old style" name for the given contextual facet (or facet alias).
	 * 
	 * @param contextualFacet  the contextual facet for which to return an "old style" name
	 * @param facetAlias  the alias of the given contextual facet (may be null)
	 * @return String
	 */
	private static String getContextualFacetLegacyName(TLFacet contextualFacet, TLAlias facetAlias) {
		TLFacetOwner facetOwner = contextualFacet.getOwningEntity();
		StringBuilder facetName = new StringBuilder();
		
		if (facetAlias == null) {
			facetName.append(facetOwner.getLocalName()).append("_");
			
		} else {
			TLAlias ownerAlias = AliasCodegenUtils.getOwnerAlias(facetAlias);
			
			if (ownerAlias != null) {
				facetName.append(ownerAlias.getLocalName()).append("_");
			} else {
				facetName.append(facetAlias.getName()).append("_"); // invalid name, but this should never happen
			}
		}
		facetName.append(contextualFacet.getFacetType().getIdentityName());
		
		if (contextualFacet.getContext() != null) {
			facetName.append("_").append(contextualFacet.getContext());
		}
		if (contextualFacet.getLabel() != null) {
			facetName.append("_").append(contextualFacet.getLabel());
		}
		return facetName.toString();
	}
	
}
