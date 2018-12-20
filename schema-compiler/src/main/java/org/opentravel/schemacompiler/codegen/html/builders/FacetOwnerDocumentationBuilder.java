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
package org.opentravel.schemacompiler.codegen.html.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;

/**
 * @author Eric.Bronson
 *
 */
public abstract class FacetOwnerDocumentationBuilder<T extends TLFacetOwner & TLDocumentationOwner>
		extends NamedEntityDocumentationBuilder<T> {

	List<FacetDocumentationBuilder> facets = new ArrayList<>();

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
	public void build() throws CodeGenerationException {
		ListIterator<FacetDocumentationBuilder> facetIter = facets.listIterator();
		FacetDocumentationBuilder prev;
		FacetDocumentationBuilder next;
		
		while(facetIter.hasNext()){
			prev = facetIter.hasPrevious() ? facets.get(facetIter.previousIndex()) : null;
			FacetDocumentationBuilder builder = facetIter.next();
			next = facetIter.hasNext() ? facets.get(facetIter.nextIndex()) : null;
			builder.setPrevious(prev);
			builder.setNext(next);
			builder.build();
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
