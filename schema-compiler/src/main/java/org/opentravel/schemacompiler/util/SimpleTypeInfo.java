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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
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
	
	private static final String UTC_DATETIME_DESCRIPTION = "ISO date time type without UTC offset or Z for Zulu restriction indicating it is representing Local Time.  Example: 2010-12-31T11:55:00-06:00";
	private static final String UTC_DATE_DESCRIPTION = "ISO date type without UTC offset or Z for Zulu restriction indicating it is representing Local Time.  Example: 2010-12-31";
	private static final String UTC_TIME_DESCRIPTION = "ISO time type without UTC offset or Z for Zulu restriction indicating it is representing Local Time.  Example: 11:55:00-06:00";
	private static final String LOCAL_DATETIME_DESCRIPTION = "ISO date time type without UTC offset or Z for Zulu restriction indicating it is representing Local Time.  Example: 2010-12-31T11:55:00";
	private static final String LOCAL_DATE_DESCRIPTION = "ISO date type without UTC offset or Z for Zulu restriction indicating it is representing Local Time.  Example: 2010-12-31";
	private static final String LOCAL_TIME_DESCRIPTION = "ISO time type without UTC offset or Z for Zulu restriction indicating it is representing Local Time.  Example: 11:55:00";
	
	public static final JsonSchema UTC_DATETIME_JSON_SCHEMA   = newSchema( JsonType.jsonDateTime, UTC_DATETIME_DESCRIPTION, null, -1, -1 );
	public static final JsonSchema UTC_DATE_JSON_SCHEMA       = newSchema( JsonType.jsonDate, UTC_DATE_DESCRIPTION, null, -1, -1 );
	public static final JsonSchema UTC_TIME_JSON_SCHEMA       = newSchema( JsonType.jsonString, UTC_TIME_DESCRIPTION, "(([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)((:?)[0-5]\\d)?([\\.,]\\d+(?!:))?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?", -1, -1 );
	public static final JsonSchema LOCAL_DATETIME_JSON_SCHEMA = newSchema( JsonType.jsonString, LOCAL_DATETIME_DESCRIPTION, "(\\d{4}-\\d{2}-\\d{2})T(([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)((:?)[0-5]\\d)?([\\.,]\\d+(?!:))?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?", -1, -1 );
	public static final JsonSchema LOCAL_DATE_JSON_SCHEMA     = newSchema( JsonType.jsonString, LOCAL_DATE_DESCRIPTION, "(\\d{4}-\\d{2}-\\d{2})", -1, -1 );
	public static final JsonSchema LOCAL_TIME_JSON_SCHEMA     = newSchema( JsonType.jsonString, LOCAL_TIME_DESCRIPTION, "(([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)((:?)[0-5]\\d)?([\\.,]\\d+(?!:))?", -1, -1 );
	public static final JsonSchema ENUM_EXTENSION_SCHEMA      = newSchema( JsonType.jsonString, null, null, 1, 128 );
	
	private static Map<String,JsonSchema> xsdSimplePrimitives;
	
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
    private boolean listTypeInd = false;
    
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
					listTypeInd |= simple.isListTypeInd();
					
					findConstraints( simple.getParentType(),  visitedEntities );
					
					// Work-around for OTM-DE since it represents XSDSimple types as TLSimples (reason unknown)
					if (simple.getParentType() == null) {
						baseSimpleType = simple;
					}
					
				} else if (simpleType instanceof TLValueWithAttributes) {
					findConstraints( ((TLValueWithAttributes) simpleType).getParentType(), visitedEntities );
					
				} else if (simpleType instanceof TLCoreObject) {
					findConstraints( ((TLCoreObject) simpleType).getSimpleFacet(), visitedEntities );
					
				} else if (simpleType instanceof TLSimpleFacet) {
					findConstraints( ((TLSimpleFacet) simpleType).getSimpleType(), visitedEntities );
					
				} else if (simpleType instanceof TLListFacet) {
					TLListFacet listFacet = (TLListFacet) simpleType;
					
					if (listFacet.getFacetType() == TLFacetType.SIMPLE) {
						findConstraints( ((TLListFacet) simpleType).getItemFacet(), visitedEntities );
					}
					
				} else if (simpleType instanceof TLClosedEnumeration) {
					baseSimpleType = simpleType;
					
				} else if (simpleType instanceof XSDSimpleType) {
					JsonSchema simpleSchema = xsdSimplePrimitives.get( simpleType.getLocalName() );
					
					if (simpleSchema != null) {
						AbstractLibrary xsdLibrary = simpleType.getOwningModel().getLibrariesForNamespace(
								XMLConstants.W3C_XML_SCHEMA_NS_URI ).get( 0 );
						
						switch (simpleSchema.getType()) {
							case jsonDateTime:
								baseSimpleType = xsdLibrary.getNamedMember( "dateTime" );
								break;
							case jsonDate:
								baseSimpleType = xsdLibrary.getNamedMember( "date" );
								break;
							case jsonString:
								baseSimpleType = xsdLibrary.getNamedMember( "string" );
								break;
							default:
								baseSimpleType = simpleType;
								break;
						}
						
						if (pattern == null) {
							pattern = simpleSchema.getPattern();
						}
						if ((minLength < 0) && (simpleSchema.getMinLength() != null)) {
							minLength = simpleSchema.getMinLength();
						}
						if ((maxLength < 0) && (simpleSchema.getMaxLength() != null)) {
							maxLength = simpleSchema.getMaxLength();
						}
						
					} else {
						baseSimpleType = simpleType;
					}
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
	
	/**
	 * Returns true if the type is a simple list.
	 *
	 * @return boolean
	 */
	public boolean isListType() {
		return listTypeInd;
	}

	/**
	 * Constructs a new JSON type instance.
	 * 
	 * @param type  the JSON type assigned for the schema
	 * @param DESCRIPTION  the DESCRIPTION of the schema
	 * @param pattern  the regular expression pattern for the schema
	 * @param minLength  the minimum string length for the schema
	 * @param maxLength  the maximum string length for the schema
	 * @return JsonSchema
	 */
	private static JsonSchema newSchema(JsonType type, String description, String pattern, int minLength, int maxLength) {
		JsonSchema schema = new JsonSchema();
		
		if ((description != null) && (description.trim().length() > 0)) {
			JsonDocumentation doc = new JsonDocumentation();
			
			doc.setDescriptions( description.trim() );
			schema.setDocumentation( doc );
		}
		if ((pattern != null) && (pattern.trim().length() > 0)) {
			schema.setPattern( pattern.trim() );
		}
		if (minLength >= 0) {
			schema.setMinLength( minLength );
		}
		if (maxLength >= 0) {
			schema.setMaxLength( maxLength );
		}
		schema.setType( type );
		return schema;
	}
	
	/**
	 * Initializes the map of <code>XSDSimpleType</code> primitives.
	 */
	static {
		xsdSimplePrimitives = new HashMap<>();
		xsdSimplePrimitives.put( "UTCDateTime", UTC_DATETIME_JSON_SCHEMA );
		xsdSimplePrimitives.put( "UTCDate", UTC_DATE_JSON_SCHEMA );
		xsdSimplePrimitives.put( "UTCTime", UTC_TIME_JSON_SCHEMA );
		xsdSimplePrimitives.put( "LocalDateTime", LOCAL_DATETIME_JSON_SCHEMA );
		xsdSimplePrimitives.put( "LocalDate", LOCAL_DATE_JSON_SCHEMA );
		xsdSimplePrimitives.put( "LocalTime", LOCAL_TIME_JSON_SCHEMA );
	}
	
}
