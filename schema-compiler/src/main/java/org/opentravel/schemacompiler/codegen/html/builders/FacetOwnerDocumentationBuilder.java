/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;

/**
 * @author Eric.Bronson
 *
 */
public abstract class FacetOwnerDocumentationBuilder<T extends TLFacetOwner & TLDocumentationOwner>
		extends NamedEntityDocumentationBuilder<T> {

	List<FacetDocumentationBuilder> facets = new ArrayList<FacetDocumentationBuilder>();

	/**
	 * @param manager
	 */
	public FacetOwnerDocumentationBuilder(T t) {
		super(t);
		initializeFacets(t);
	}

	protected abstract void initializeFacets(T t);
	
	protected boolean shouldAddFacet(TLFacet facet) {
		return facet != null && facet.declaresContent();
	}

	public List<FacetDocumentationBuilder> getFacets() {
		return facets;
	}
	
	@Override
	public void build() throws Exception {
		ListIterator<FacetDocumentationBuilder> facetIter = facets.listIterator();
		FacetDocumentationBuilder prev, next;
		while(facetIter.hasNext()){
			prev = facetIter.hasPrevious() ? facets.get(facetIter.previousIndex()) : null;
			FacetDocumentationBuilder builder = facetIter.next();
			next = facetIter.hasNext() ? facets.get(facetIter.nextIndex()) : null;
			builder.setPrevious(prev);
			builder.setNext(next);
			builder.build();
		}
	}

}
