
package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import java.util.List;

import org.opentravel.ns.ota2.librarymodel_v01_03.BusinessObject;
import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_03.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_03.FacetContextual;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>BusinessObject</code> type to the
 * <code>TLBusinessObject</code> type.
 *
 * @author S. Livezey
 */
public class BusinessObjectTransformer extends ComplexTypeTransformer<BusinessObject,TLBusinessObject> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLBusinessObject transform(BusinessObject source) {
		ObjectTransformer<Facet,TLFacet,DefaultTransformerContext> facetTransformer =
				getTransformerFactory().getTransformer(Facet.class, TLFacet.class);
		ObjectTransformer<FacetContextual,TLFacet,DefaultTransformerContext> facetContextualTransformer =
				getTransformerFactory().getTransformer(FacetContextual.class, TLFacet.class);
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		TLBusinessObject businessObject = new TLBusinessObject();
		
		businessObject.setName( trimString(source.getName()) );
		businessObject.setNotExtendable( !(source.getExtendable() != null) );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			businessObject.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			businessObject.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		for (String aliasName : trimStrings(source.getAliases())) {
			TLAlias alias = new TLAlias();
			
			alias.setName(aliasName);
			businessObject.addAlias(alias);
		}
		
		if (source.getID() != null) {
			TLFacet idFacet = facetTransformer.transform(source.getID());
			
			businessObject.setIdFacet( idFacet );
			buildFacetAliases(businessObject, idFacet, source.getID().getAliases());
		}
		if (source.getSummary() != null) {
			TLFacet summaryFacet = facetTransformer.transform(source.getSummary());
			
			businessObject.setSummaryFacet( summaryFacet );
			buildFacetAliases(businessObject, summaryFacet, source.getSummary().getAliases());
		}
		if (source.getDetail() != null) {
			TLFacet detailFacet = facetTransformer.transform(source.getDetail());
			
			businessObject.setDetailFacet( detailFacet );
			buildFacetAliases(businessObject, detailFacet, source.getDetail().getAliases());
		}
		
		if (source.getCustom() != null) {
			for (FacetContextual sourceFacet : source.getCustom()) {
				TLFacet customFacet = facetContextualTransformer.transform(sourceFacet);
				
				businessObject.addCustomFacet( customFacet );
				buildFacetAliases(businessObject, customFacet, sourceFacet.getAliases());
			}
		}
		if (source.getQuery() != null) {
			for (FacetContextual sourceFacet : source.getQuery()) {
				TLFacet queryFacet = facetContextualTransformer.transform(sourceFacet);
				
				businessObject.addQueryFacet( queryFacet );
				buildFacetAliases(businessObject, queryFacet, sourceFacet.getAliases());
			}
		}
		
		if (source.getExtends() != null) {
			TLExtension extension = new TLExtension();
			
			extension.setExtendsEntityName( source.getExtends() );
			businessObject.setExtension( extension );
		}
		
		return businessObject;
	}
	
	/**
	 * Attempts to construct business object aliases using the ones defined in the list provided.
	 * 
	 * @param businessObject  the business object that will receive the aliases
	 * @param facet  the model facet that corresponds to the JAXB facet where the alias names originated
	 * @param aliasNames  the list of facet alias names to migrate
	 */
	private void buildFacetAliases(TLBusinessObject businessObject, TLFacet facet, List<String> aliasNames) {
		String facetSuffix = facet.getFacetType().getIdentityName(facet.getContext(), facet.getLabel());
		
		for (String aliasName : aliasNames) {
			String candidateAlias;
			
			// Construct the name of a candidate alias
			if (aliasName.endsWith(facetSuffix)) {
				candidateAlias = aliasName.replaceAll(facetSuffix, "");
			} else {
				candidateAlias = aliasName;
			}
			
			// Create the alias if it does not already exist
			TLAlias existingAlias = businessObject.getAlias(candidateAlias);
			
			if (existingAlias == null) {
				TLAlias alias = new TLAlias();
				
				alias.setName(candidateAlias);
				businessObject.addAlias(alias);
			}
		}
	}
	
}
