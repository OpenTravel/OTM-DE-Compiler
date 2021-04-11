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

package org.opentravel.schemacompiler.codegen.openapi.model;

import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityType;

/**
 * Enumeration that specifies the type of an OpenAPI security scheme. The types that are defined for this enumeration
 * are the only values allowed by the OpenAPI specification.
 */
public enum OpenApiSecurityType {

    BASIC("basic"), API_KEY("apiKey"), OAUTH2("oauth2"), OPENID_CONNECT("openIdConnect");

    private String displayValue;

    /**
     * Constructor that specifies the display value for the enumeration.
     * 
     * @param displayValue the display value of the enumeration
     */
    private OpenApiSecurityType(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the display value of the enumeration.
     * 
     * @return String
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Returns the correct enumeration for the given display value or null if an invalid scheme is provided.
     * 
     * @param displayValue the display value of the type to return
     * @return SwaggerSecurityType
     */
    public static OpenApiSecurityType fromDisplayValue(String displayValue) {
        OpenApiSecurityType type = null;

        if (displayValue != null) {
            for (OpenApiSecurityType t : values()) {
                if (displayValue.equalsIgnoreCase( t.getDisplayValue() )) {
                    type = t;
                    break;
                }
            }
        }
        return type;
    }

    /**
     * Returns the OpenAPI security type that corresponds to the Swagger value provided.
     * 
     * @param swaggerType the Swagger security type value
     * @return OpenApiSecurityType
     */
    public static OpenApiSecurityType fromSwaggerSecurityType(SwaggerSecurityType swaggerType) {
        OpenApiSecurityType openapiType = null;

        if (swaggerType != null) {
            switch (swaggerType) {
                case API_KEY:
                    openapiType = API_KEY;
                    break;
                case BASIC:
                    openapiType = BASIC;
                    break;
                case OAUTH2:
                    openapiType = OAUTH2;
                    break;
                default:
                    break;
            }
        }
        return openapiType;
    }

}
