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

package org.opentravel.schemacompiler.util;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLOperation;

/**
 * Wraps a <code>TLFacet</code> to provide additional information required during JSP
 * page rendering.
 * 
 * @author S. Livezey
 */
public class FacetIdentityWrapper {
	
	private String identity;
	private TLFacet facet;
	private String contributedFrom;
	
	/**
	 * Constructor that specifies the facet to be wrapped.
	 * 
	 * @param facet  the facet to be wrapped by this instance
	 */
	public FacetIdentityWrapper(TLFacet facet) {
		if (facet instanceof TLContextualFacet) {
			TLContextualFacet cFacet = (TLContextualFacet) facet;
			QName ownerName = IndexingUtils.getContextualFacetOwnerQName( cFacet );
			
			if ((ownerName != null) &&
					!ownerName.getNamespaceURI().equals( cFacet.getFacetNamespace() )) {
				this.contributedFrom = cFacet.getFacetNamespace();
			}
			this.identity = facet.getFacetType().getIdentityName( cFacet.getName() );
			
		} else if (facet.getOwningEntity() instanceof TLOperation) {
			switch (facet.getFacetType()) {
				case REQUEST:
					this.identity = "Request";
					break;
				case RESPONSE:
					this.identity = "Response";
					break;
				case NOTIFICATION:
					this.identity = "Notification";
					break;
				default:
			}
		} else {
			this.identity = facet.getFacetType().getIdentityName();
		}
		this.facet = facet;
	}

	/**
	 * Returns the identity name of the facet.
	 *
	 * @return String
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Returns the underlying facet instance.
	 *
	 * @return TLFacet
	 */
	public TLFacet getFacet() {
		return facet;
	}

	/**
	 * Returns the namespace in which the facet was declared if different from the
	 * owning entity's namespace.
	 *
	 * @return String
	 */
	public String getContributedFrom() {
		return contributedFrom;
	}
	
}
