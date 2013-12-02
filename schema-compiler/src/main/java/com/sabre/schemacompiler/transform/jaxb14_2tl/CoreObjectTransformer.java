/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.CoreObject;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_04.Role;
import org.opentravel.ns.ota2.librarymodel_v01_04.SimpleFacet;

import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>CoreObject</code> type to the
 * <code>TLCoreObject</code> type.
 *
 * @author S. Livezey
 */
public class CoreObjectTransformer extends BaseTransformer<CoreObject,TLCoreObject,DefaultTransformerContext> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLCoreObject transform(CoreObject source) {
		ObjectTransformer<SimpleFacet,TLSimpleFacet,DefaultTransformerContext> simpleFacetTransformer =
				getTransformerFactory().getTransformer(SimpleFacet.class, TLSimpleFacet.class);
		ObjectTransformer<Facet,TLFacet,DefaultTransformerContext> facetTransformer =
				getTransformerFactory().getTransformer(Facet.class, TLFacet.class);
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		final TLCoreObject coreObject = new TLCoreObject();
		
		coreObject.setName( trimString(source.getName()) );
		coreObject.setNotExtendable( (source.isNotExtendable() == null) ? false : source.isNotExtendable() );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			coreObject.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		if (source.getExtension() != null) {
			ObjectTransformer<Extension,TLExtension,DefaultTransformerContext> extensionTransformer =
					getTransformerFactory().getTransformer(Extension.class, TLExtension.class);
			
			coreObject.setExtension( extensionTransformer.transform(source.getExtension()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			coreObject.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		if (source.getRoles() != null) {
			ObjectTransformer<Role,TLRole,DefaultTransformerContext> roleTransformer =
					getTransformerFactory().getTransformer(Role.class, TLRole.class);
			
			for (Role sourceRole : source.getRoles().getRole()) {
				coreObject.getRoleEnumeration().addRole( roleTransformer.transform(sourceRole) );
			}
		}
		
		for (String aliasName : trimStrings(source.getAliases())) {
			TLAlias alias = new TLAlias();
			
			alias.setName(aliasName);
			coreObject.addAlias(alias);
		}
		
		if (source.getSimple() != null) {
			coreObject.setSimpleFacet( simpleFacetTransformer.transform(source.getSimple()) );
		}
		if (source.getSummary() != null) {
			coreObject.setSummaryFacet( facetTransformer.transform(source.getSummary()) );
		}
		if (source.getDetail() != null) {
			coreObject.setDetailFacet( facetTransformer.transform(source.getDetail()) );
		}
		
		return coreObject;
	}
	
}
