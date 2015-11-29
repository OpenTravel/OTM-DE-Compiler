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
	
}
