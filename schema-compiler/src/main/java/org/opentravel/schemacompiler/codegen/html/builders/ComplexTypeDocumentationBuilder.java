/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;

/**
 * @author Eric.Bronson
 *
 */
public abstract class ComplexTypeDocumentationBuilder<T extends TLFacetOwner & TLDocumentationOwner & TLAliasOwner>
		extends FacetOwnerDocumentationBuilder<T> implements
		AliasOwnerDocumentationBuilder {

	protected List<String> aliases;

	/**
	 * @param element
	 */
	public ComplexTypeDocumentationBuilder(T element) {
		super(element);
		aliases = new ArrayList<String>();
		for (TLAlias alias : element.getAliases()) {
			aliases.add(alias.getName());
		}
	}

	public List<String> getAliases() {
		return Collections.unmodifiableList(aliases);
	}

	
	protected void addFacet(TLFacet facet) {
		FacetDocumentationBuilder facetBuilder = (FacetDocumentationBuilder) DocumentationBuilderFactory
				.getInstance().getDocumentationBuilder(facet);
		facets.add(facetBuilder);
		facetBuilder.setOwner(this);

	}

}
