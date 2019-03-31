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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.XSDSimpleType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

/**
 * Enumeration that represents the available types and data formats for a JSON schema.
 */
@java.lang.SuppressWarnings("squid:S1192") // Ignore constant warnings - enumerations cannot be refactored in this way
public enum JsonType {

    JSON_INTEGER("integer", "int32"),
    JSON_LONG("integer", "int64"),
    JSON_FLOAT("number", "float"),
    JSON_DOUBLE("number", "double"),
    JSON_STRING("string", null),
    JSON_BYTE("string", "byte"),
    JSON_BINARY("string", "binary"),
    JSON_BOOLEAN("boolean", null),
    JSON_DATE("string", "date"),
    JSON_DATETIME("string", "date-time"),
    JSON_PASS("string", "password"),
    JSON_ARRAY("array", null),
    JSON_OBJECT("object", null),
    JSON_NULL("null", null),
    JSON_REFS("array", null);

    private static Map<String,JsonType> xsdSimpleMap;
    private String schemaType;
    private String format;

    /**
     * Constructor that specifies the JSON schema type and format for the value.
     * 
     * @param schemaType the JSON schema type
     * @param format the JSON schema format (may be null)
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
     * If the given named entity is an XSD simple type from the OTM model, this method will return the equivalent
     * <code>JsonType</code>. For non-XSD simples, this method will return null.
     * 
     * @param entity the named entity for which to return a JSON simple type
     * @return JsonType
     */
    public static JsonType valueOf(NamedEntity entity) {
        JsonType type = null;

        if (((entity instanceof XSDSimpleType) || (entity instanceof TLSimple))
            && entity.getNamespace().equals( XMLConstants.W3C_XML_SCHEMA_NS_URI )) {
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

            typeMappings.put( "anyURI", JSON_STRING );
            typeMappings.put( "boolean", JSON_BOOLEAN );
            typeMappings.put( "date", JSON_DATE );
            typeMappings.put( "dateTime", JSON_DATETIME );
            typeMappings.put( "decimal", JSON_FLOAT );
            typeMappings.put( "double", JSON_DOUBLE );
            typeMappings.put( "float", JSON_FLOAT );
            typeMappings.put( "duration", JSON_STRING );
            typeMappings.put( "ID", JSON_STRING );
            typeMappings.put( "IDREF", JSON_STRING );
            typeMappings.put( "IDREFS", JSON_REFS );
            typeMappings.put( "long", JSON_LONG );
            typeMappings.put( "int", JSON_INTEGER );
            typeMappings.put( "integer", JSON_INTEGER );
            typeMappings.put( "positiveInteger", JSON_INTEGER );
            typeMappings.put( "QName", JSON_STRING );
            typeMappings.put( "string", JSON_STRING );
            typeMappings.put( "time", JSON_STRING );
            typeMappings.put( "language", JSON_STRING );
            typeMappings.put( "base64Binary", JSON_STRING );

            xsdSimpleMap = Collections.unmodifiableMap( typeMappings );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
