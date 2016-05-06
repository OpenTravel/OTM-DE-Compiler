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
