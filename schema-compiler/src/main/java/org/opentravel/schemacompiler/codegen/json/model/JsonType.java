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
package org.opentravel.schemacompiler.codegen.json.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.XSDSimpleType;

/**
 * Enumeration that represents the available types and data formats for a
 * JSON schema.
 */
public enum JsonType {
	
	jsonInteger  ( "integer", "int32"     ),
	jsonLong     ( "integer", "int64"     ),
	jsonFloat    ( "number",  "float"     ),
	jsonDouble   ( "number",  "double"    ),
	jsonString   ( "string",   null       ),
	jsonByte     ( "string",  "byte"      ),
	jsonBinary   ( "string",  "binary"    ),
	jsonBoolean  ( "boolean",  null       ),
	jsonDate     ( "string",  "date"      ),
	jsonDateTime ( "string",  "date-time" ),
	jsonPassword ( "string",  "password"  ),
	jsonArray    ( "array",    null       ),
	jsonObject   ( "object",   null       ),
	jsonNull     ( "null",     null       );
	
	private static Map<String,JsonType> xsdSimpleMap;
	private String schemaType;
	private String format;
	
	/**
	 * Constructor that specifies the JSON schema type and format for the value.
	 * 
	 * @param schemaType  the JSON schema type
	 * @param format  the JSON schema format (may be null)
	 */
	private JsonType(String schemaType, String format) {
		this.schemaType = schemaType;
		this.format = format;
	}
	
	/**
	 * Returns the the JSON schema type value.
	 *
	 * @return String
	 */
	public String getSchemaType() {
		return schemaType;
	}
	
	/**
	 * Returns the the JSON schema format value (may be null).
	 *
	 * @return String
	 */
	public String getFormat() {
		return format;
	}
	
	/**
	 * If the given named entity is an XSD simple type from the OTM model, this
	 * method will return the equivalent <code>JsonType</code>.  For non-XSD simples,
	 * this method will return null.
	 * 
	 * @param entity  the named entity for which to return a JSON simple type
	 * @return JsonType
	 */
	public static JsonType valueOf(NamedEntity entity) {
		JsonType type = null;
		
		if ((entity instanceof XSDSimpleType) &&
				entity.getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
			type = xsdSimpleMap.get( entity.getLocalName() );
		}
		return type;
	}
	
	/**
	 * Initializes the map of XSD simple types to JSON simples.
	 */
	static {
		try {
			Map<String,JsonType> typeMappings = new HashMap<>();
			
			typeMappings.put( "anyURI", jsonString );
			typeMappings.put( "boolean", jsonBoolean );
			typeMappings.put( "date", jsonDate );
			typeMappings.put( "dateTime", jsonDateTime );
			typeMappings.put( "decimal", jsonFloat );
			typeMappings.put( "double", jsonDouble );
			typeMappings.put( "float", jsonFloat );
			typeMappings.put( "duration", jsonString );
			typeMappings.put( "ID", jsonString );
			typeMappings.put( "IDREF", jsonString );
			typeMappings.put( "IDREFS", jsonString );
			typeMappings.put( "long", jsonLong );
			typeMappings.put( "int", jsonInteger );
			typeMappings.put( "integer", jsonInteger );
			typeMappings.put( "positiveInteger", jsonInteger );
			typeMappings.put( "QName", jsonString );
			typeMappings.put( "string", jsonString );
			typeMappings.put( "time", jsonString );
			typeMappings.put( "language", jsonString );
			typeMappings.put( "base64Binary", jsonString );
			
			xsdSimpleMap = Collections.unmodifiableMap( typeMappings );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
