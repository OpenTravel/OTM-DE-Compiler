/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.CoreObject;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_04.Role;
import org.opentravel.ns.ota2.librarymodel_v01_04.RoleList;
import org.opentravel.ns.ota2.librarymodel_v01_04.SimpleFacet;

import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLCoreObject</code> type to the
 * <code>CoreObject</code> type.
 *
 * @author S. Livezey
 */
public class TLCoreObjectTransformer extends TLComplexTypeTransformer<TLCoreObject,CoreObject> {
	
	@Override
	public CoreObject transform(TLCoreObject source) {
		ObjectTransformer<TLSimpleFacet,SimpleFacet,SymbolResolverTransformerContext> simpleFacetTransformer =
				getTransformerFactory().getTransformer(TLSimpleFacet.class, SimpleFacet.class);
		ObjectTransformer<TLFacet,Facet,SymbolResolverTransformerContext> facetTransformer =
				getTransformerFactory().getTransformer(TLFacet.class, Facet.class);
		ObjectTransformer<TLRole,Role,SymbolResolverTransformerContext> roleTransformer =
				getTransformerFactory().getTransformer(TLRole.class, Role.class);
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		CoreObject coreObject = new CoreObject();
		
		coreObject.setName( trimString(source.getName(), false) );
		coreObject.setNotExtendable( source.isNotExtendable() );
		coreObject.getAliases().addAll( getAliasNames(source.getAliases()) );
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			coreObject.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		if (source.getRoleEnumeration().getRoles().size() > 0) {
			RoleList roleList = new RoleList();
			
			for (TLRole sourceRole : source.getRoleEnumeration().getRoles()) {
				roleList.getRole().add( roleTransformer.transform(sourceRole) );
			}
			coreObject.setRoles(roleList);
		}
		
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			coreObject.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		
		coreObject.setSimple( simpleFacetTransformer.transform(source.getSimpleFacet()) );
		coreObject.setSummary( facetTransformer.transform(source.getSummaryFacet()) );
		coreObject.setDetail( facetTransformer.transform(source.getDetailFacet()) );
		
		if (source.getExtension() != null) {
			ObjectTransformer<TLExtension,Extension,SymbolResolverTransformerContext> extensionTransformer =
					getTransformerFactory().getTransformer(TLExtension.class, Extension.class);
			
			coreObject.setExtension( extensionTransformer.transform(source.getExtension()) );
		}
		
		return coreObject;
	}
	
}
