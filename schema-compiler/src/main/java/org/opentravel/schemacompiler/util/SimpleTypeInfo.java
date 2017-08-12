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
import java.util.Set;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDFacetProfile;
import org.opentravel.schemacompiler.model.XSDSimpleType;

/**
 * Encapsulates the constraints of an OTM simple type, including the constraints
 * inherited from other base simple types.
 */
public class SimpleTypeInfo {
	
	private XSDFacetProfile facetProfile;
	private NamedEntity originalSimpleType;
	private NamedEntity baseSimpleType;
    private String pattern;
    private int minLength = -1;
    private int maxLength = -1;
    private int fractionDigits = -1;
    private int totalDigits = -1;
    private String minInclusive;
    private String maxInclusive;
    private String minExclusive;
    private String maxExclusive;
    
	/**
	 * Constructs the list of all constraints for the given simple type.
	 * 
	 * @param simpleType  the simple type for which to discover constraints
	 */
	private SimpleTypeInfo(TLAttributeType simpleType) {
		this.originalSimpleType = simpleType;
		this.facetProfile = XSDFacetProfileLocator.getXSDFacetProfile( simpleType );
		findConstraints( simpleType, new HashSet<String>() );
	}
	
	/**
	 * Returns a new <code>SimpleTypeInfo</code> instance for the given entity
	 * or null if the entity is not a qualifying simple type.
	 * 
	 * @param entity  the entity for which to return the simple type information
	 * @return SimpleTypeInfo
	 */
	public static SimpleTypeInfo newInstance(NamedEntity entity) {
		SimpleTypeInfo simpleInfo = null;
		
		if (entity instanceof TLAttributeType) {
			simpleInfo = new SimpleTypeInfo( (TLAttributeType) entity );
		}
		return simpleInfo;
	}
	
	/**
	 * Searches the type hierarchy of the given simple type an initializes this
	 * object with any constraint values that are discovered.
	 * 
	 * @param simpleType  the simple type for which to discover constraints
	 * @param visitedEntities  the set of qualified names of entities aready visited
	 */
	private void findConstraints(NamedEntity simpleType, Set<String> visitedEntities) {
		if (simpleType != null) {
			String simpleTypeKey = simpleType.getNamespace() + ":" + simpleType.getLocalName();
			
			if (!visitedEntities.contains( simpleTypeKey )) {
				if (simpleType instanceof TLSimple) {
					TLSimple simple = (TLSimple) simpleType;
					
					if ((pattern == null) && (simple.getPattern() != null)) {
						pattern = simple.getPattern();
					}
					if ((minLength < 0) && (simple.getMinLength() >= 0)) {
						minLength = simple.getMinLength();
					}
					if ((maxLength < 0) && (simple.getMaxLength() >= 0)) {
						maxLength = simple.getMaxLength();
					}
					if ((fractionDigits < 0) && (simple.getFractionDigits() >= 0)) {
						fractionDigits = simple.getFractionDigits();
					}
					if ((totalDigits < 0) && (simple.getTotalDigits() >= 0)) {
						totalDigits = simple.getTotalDigits();
					}
					if ((minInclusive == null) && (simple.getMinInclusive() != null)) {
						minInclusive = simple.getMinInclusive();
					}
					if ((maxInclusive == null) && (simple.getMaxInclusive() != null)) {
						maxInclusive = simple.getMaxInclusive();
					}
					if ((minExclusive == null) && (simple.getMinExclusive() != null)) {
						minExclusive = simple.getMinExclusive();
					}
					if ((maxExclusive == null) && (simple.getMaxExclusive() != null)) {
						maxExclusive = simple.getMaxExclusive();
					}
					findConstraints( simple.getParentType(),  visitedEntities );
					
				} else if (simpleType instanceof TLValueWithAttributes) {
					findConstraints( ((TLValueWithAttributes) simpleType).getParentType(), visitedEntities );
					
				} else if (simpleType instanceof TLSimpleFacet) {
					findConstraints( ((TLSimpleFacet) simpleType).getSimpleType(), visitedEntities );
					
				} else if (simpleType instanceof TLListFacet) {
					TLListFacet listFacet = (TLListFacet) simpleType;
					
					if (listFacet.getFacetType() == TLFacetType.SIMPLE) {
						findConstraints( ((TLListFacet) simpleType).getItemFacet(), visitedEntities );
					}
					
				} else if (simpleType instanceof XSDSimpleType) {
					baseSimpleType = simpleType;
				}
				visitedEntities.add( simpleTypeKey );
			}
		}
	}
	
	/**
	 * Returns the value of the 'facetProfile' field.
	 *
	 * @return XSDFacetProfile
	 */
	public XSDFacetProfile getFacetProfile() {
		return facetProfile;
	}

	/**
	 * Returns the value of the 'originalSimpleType' field.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getOriginalSimpleType() {
		return originalSimpleType;
	}

	/**
	 * Returns the value of the 'baseSimpleType' field.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getBaseSimpleType() {
		return baseSimpleType;
	}

	/**
	 * Returns the value of the 'pattern' field.
	 *
	 * @return String
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Returns the value of the 'minLength' field.
	 *
	 * @return int
	 */
	public int getMinLength() {
		return minLength;
	}

	/**
	 * Returns the value of the 'maxLength' field.
	 *
	 * @return int
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * Returns the value of the 'fractionDigits' field.
	 *
	 * @return int
	 */
	public int getFractionDigits() {
		return fractionDigits;
	}

	/**
	 * Returns the value of the 'totalDigits' field.
	 *
	 * @return int
	 */
	public int getTotalDigits() {
		return totalDigits;
	}

	/**
	 * Returns the value of the 'minInclusive' field.
	 *
	 * @return String
	 */
	public String getMinInclusive() {
		return minInclusive;
	}

	/**
	 * Returns the value of the 'maxInclusive' field.
	 *
	 * @return String
	 */
	public String getMaxInclusive() {
		return maxInclusive;
	}

	/**
	 * Returns the value of the 'minExclusive' field.
	 *
	 * @return String
	 */
	public String getMinExclusive() {
		return minExclusive;
	}

	/**
	 * Returns the value of the 'maxExclusive' field.
	 *
	 * @return String
	 */
	public String getMaxExclusive() {
		return maxExclusive;
	}
	
}
