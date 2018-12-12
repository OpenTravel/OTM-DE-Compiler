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

import java.util.HashSet;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.XSDFacetProfile;

/**
 * Implements a recursive routine to locate the <code>XSDFacetProfile</code> for certain
 * simple types that obtain their profile from other related model elements.
 */
public class XSDFacetProfileLocator {
	
	/**
	 * Returns the facet profile for the given entity type.
	 * 
	 * @param entity  the entity for which to return the facet profile
	 * @return XSDFacetProfile
	 */
	public static XSDFacetProfile getXSDFacetProfile(TLAttributeType entity) {
		return getXSDFacetProfile( entity, new HashSet<String>() );
	}
	
	/**
	 * Recursive routine that searches for a concrete <code>XSDFacetProfile</code> while
	 * avoiding infinite loops due to circular references.
	 * 
	 * @param entity  the entity for which to return a facet profile
	 * @param visitedEntities  the set of qualified names of entities aready visited
	 * @return XSDFacetProfile
	 */
	private static XSDFacetProfile getXSDFacetProfile(TLAttributeType entity, HashSet<String> visitedEntities) {
		XSDFacetProfile facetProfile;
		
		if (entity != null) {
			String entityKey = entity.getNamespace() + ":" + entity.getLocalName();
			
			if (visitedEntities.contains( entityKey )) {
				facetProfile = XSDFacetProfile.FP_UNKNOWN;
				
			} else if (entity instanceof TLSimple) {
				TLAttributeType parentType = ((TLSimple) entity).getParentType();
				
				if (parentType != null) {
					visitedEntities.add( entityKey );
					facetProfile = getXSDFacetProfile( parentType, visitedEntities );
					
				} else {
					facetProfile = XSDFacetProfile.FP_UNKNOWN;
				}
			} else if (entity instanceof TLSimpleFacet) {
				NamedEntity simpleType = ((TLSimpleFacet) entity).getSimpleType();
				
				if (simpleType instanceof TLAttributeType) {
					visitedEntities.add( entityKey );
					facetProfile = getXSDFacetProfile( (TLAttributeType) simpleType, visitedEntities );
					
				} else {
					facetProfile = XSDFacetProfile.FP_UNKNOWN;
				}
				
			} else if (entity instanceof TLListFacet) {
				NamedEntity itemFacet = ((TLListFacet) entity).getItemFacet();
				
				if (itemFacet instanceof TLAttributeType) {
					visitedEntities.add( entityKey );
					facetProfile = getXSDFacetProfile( (TLAttributeType) itemFacet, visitedEntities );
					
				} else {
					facetProfile = XSDFacetProfile.FP_UNKNOWN;
				}
				
			} else {
				facetProfile = entity.getXSDFacetProfile();
			}
		} else {
			facetProfile = XSDFacetProfile.FP_UNKNOWN;
		}
		return facetProfile;
	}
}
