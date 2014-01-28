/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.BusinessObject;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_04.FacetContextual;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLBusinessObject</code> type to the
 * <code>BusinessObject</code> type.
 *
 * @author S. Livezey
 */
public class TLBusinessObjectTransformer extends TLComplexTypeTransformer<TLBusinessObject,BusinessObject> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public BusinessObject transform(TLBusinessObject source) {
		ObjectTransformer<TLFacet,Facet,SymbolResolverTransformerContext> facetTransformer =
				getTransformerFactory().getTransformer(TLFacet.class, Facet.class);
		ObjectTransformer<TLFacet,FacetContextual,SymbolResolverTransformerContext> facetContextualTransformer =
				getTransformerFactory().getTransformer(TLFacet.class, FacetContextual.class);
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		BusinessObject businessObject = new BusinessObject();
		
		businessObject.setName( trimString(source.getName(), false) );
		businessObject.setNotExtendable( source.isNotExtendable() );
		businessObject.getAliases().addAll( getAliasNames(source.getAliases()) );
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			businessObject.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			businessObject.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		
		businessObject.setID(facetTransformer.transform(source.getIdFacet()));
		businessObject.setSummary(facetTransformer.transform(source.getSummaryFacet()));
		businessObject.setDetail(facetTransformer.transform(source.getDetailFacet()));
		
		for (TLFacet customFacet : source.getCustomFacets()) {
			businessObject.getCustom().add( facetContextualTransformer.transform(customFacet) );
		}
		for (TLFacet queryFacet : source.getQueryFacets()) {
			businessObject.getQuery().add( facetContextualTransformer.transform(queryFacet) );
		}
		
		if (source.getExtension() != null) {
			ObjectTransformer<TLExtension,Extension,SymbolResolverTransformerContext> extensionTransformer =
					getTransformerFactory().getTransformer(TLExtension.class, Extension.class);
			
			businessObject.setExtension( extensionTransformer.transform(source.getExtension()) );
		}
		
		return businessObject;
	}
	
}
