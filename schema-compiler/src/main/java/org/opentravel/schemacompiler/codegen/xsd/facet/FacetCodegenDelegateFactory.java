
package org.opentravel.schemacompiler.codegen.xsd.facet;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLSimpleFacet;

/**
 * Factory used to determine which <code>FacetCodegenDelegate</code> should be used to generate
 * artifacts for a particular facet instance.
 * 
 * @author S. Livezey
 */
public class FacetCodegenDelegateFactory {
	
	protected CodeGenerationTransformerContext transformerContext;
	
	/**
	 * Constructor that supplies the transformer factory and symbol resolve to be used by the code
	 * generation delegates produced by this factory instance.
	 * 
	 * @param transformerFactory  the transformer factory used to generate code artifacts for facet sub-elements
	 */
	public FacetCodegenDelegateFactory(CodeGenerationTransformerContext transformerContext) {
		this.transformerContext = transformerContext;
	}
	
	/**
	 * Returns a <code>FacetCodegenDelegate</code> to use for the generation of code artifacts from
	 * the given facet instance.
	 * 
	 * @param <F>  the type of facet for which the delegate will generate artifacts
	 * @param facetInstance  the facet instance for which to generate code artifacts
	 * @return FacetCodegenDelegate<F>
	 */
	@SuppressWarnings("unchecked")
	public <F extends TLAbstractFacet> FacetCodegenDelegate<F> getDelegate(F facetInstance) {
		TLFacetOwner facetOwner = facetInstance.getOwningEntity();
		FacetCodegenDelegate<F> delegate = null;
		
		if (facetOwner instanceof TLBusinessObject) {
			if (facetInstance instanceof TLFacet) {
				TLFacet facet = (TLFacet) facetInstance;
				
				switch (facetInstance.getFacetType()) {
					case ID:
						delegate = (FacetCodegenDelegate<F>) new BusinessObjectIDFacetCodegenDelegate(facet);
						break;
					case SUMMARY:
						delegate = (FacetCodegenDelegate<F>) new BusinessObjectSummaryFacetCodegenDelegate(facet);
						break;
					case DETAIL:
						delegate = (FacetCodegenDelegate<F>) new BusinessObjectDetailFacetCodegenDelegate(facet);
						break;
					case CUSTOM:
						delegate = (FacetCodegenDelegate<F>) new BusinessObjectCustomFacetCodegenDelegate(facet);
						break;
					case QUERY:
						delegate = (FacetCodegenDelegate<F>) new BusinessObjectQueryFacetCodegenDelegate(facet);
						break;
				}
			}
		} else if (facetOwner instanceof TLCoreObject) {
			if (facetInstance instanceof TLFacet) {
				TLFacet facet = (TLFacet) facetInstance;
				
				switch (facetInstance.getFacetType()) {
					case SUMMARY:
						delegate = (FacetCodegenDelegate<F>) new CoreObjectSummaryFacetCodegenDelegate(facet);
						break;
					case DETAIL:
						delegate = (FacetCodegenDelegate<F>) new CoreObjectDetailFacetCodegenDelegate(facet);
						break;
				}
			} else if (facetInstance instanceof TLListFacet) {
				TLListFacet facet = (TLListFacet) facetInstance;
				
				switch (facetInstance.getFacetType()) {
					case SIMPLE:
						delegate = (FacetCodegenDelegate<F>) new CoreObjectListSimpleFacetCodegenDelegate(facet);
						break;
					case SUMMARY:
					case DETAIL:
						delegate = (FacetCodegenDelegate<F>) new CoreObjectListFacetCodegenDelegate(facet);
						break;
				}
			} else if (facetInstance instanceof TLSimpleFacet) {
				TLSimpleFacet facet = (TLSimpleFacet) facetInstance;
				
				switch (facetInstance.getFacetType()) {
					case SIMPLE:
						delegate = (FacetCodegenDelegate<F>) new TLSimpleFacetCodegenDelegate(facet);
						break;
				}
			}
		} else if (facetOwner instanceof TLOperation) {
			if (facetInstance instanceof TLFacet) {
				TLFacet facet = (TLFacet) facetInstance;
				
				delegate = (FacetCodegenDelegate<F>) new OperationFacetCodegenDelegate(facet);
			}
		}
		
		if (delegate != null) {
			delegate.setTransformerContext(transformerContext);
		}
		return delegate;
	}
	
}
