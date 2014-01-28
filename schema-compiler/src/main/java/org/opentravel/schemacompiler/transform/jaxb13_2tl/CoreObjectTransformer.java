
package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import java.util.List;

import org.opentravel.ns.ota2.librarymodel_v01_03.CoreObject;
import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_03.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_03.SimpleFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>CoreObject</code> type to the
 * <code>TLCoreObject</code> type.
 *
 * @author S. Livezey
 */
public class CoreObjectTransformer extends BaseTransformer<CoreObject,TLCoreObject,DefaultTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
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
		coreObject.setNotExtendable( !(source.getExtendable() != null) );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			coreObject.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			coreObject.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		for (String roleName : trimStrings(source.getRoles())) {
			TLRole role = new TLRole();
			
			role.setName(roleName);
			coreObject.getRoleEnumeration().addRole(role);
		}
		if (source.getSimple() != null) {
			coreObject.setSimpleFacet( simpleFacetTransformer.transform(source.getSimple()) );
		}
		if (source.getSummary() != null) {
			TLFacet summaryFacet = facetTransformer.transform(source.getSummary());
			
			coreObject.setSummaryFacet( summaryFacet );
			buildFacetAliases(coreObject, summaryFacet, source.getSummary().getAliases());
		}
		if (source.getInfo() != null) {
			TLFacet detailFacet = facetTransformer.transform(source.getInfo());
			
			coreObject.setDetailFacet( detailFacet );
			buildFacetAliases(coreObject, detailFacet, source.getInfo().getAliases());
		}
		
		if (source.getExtends() != null) {
			TLExtension extension = new TLExtension();
			
			extension.setExtendsEntityName( source.getExtends() );
			coreObject.setExtension( extension );
		}
		
		return coreObject;
	}
	
	/**
	 * Attempts to construct core object aliases using the ones defined in the list provided.
	 * 
	 * @param coreObject  the core object that will receive the aliases
	 * @param facet  the model facet that corresponds to the JAXB facet where the alias names originated
	 * @param aliasNames  the list of facet alias names to migrate
	 */
	private void buildFacetAliases(TLCoreObject coreObject, TLFacet facet, List<String> aliasNames) {
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
			TLAlias existingAlias = coreObject.getAlias(candidateAlias);
			
			if (existingAlias == null) {
				TLAlias alias = new TLAlias();
				
				alias.setName(candidateAlias);
				coreObject.addAlias(alias);
			}
		}
	}
	
}
