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

import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityLocation;

/**
 * Enumeration that specifies the location of security parameters for an OpenAPI API specification. The types that are
 * defined for this enumeration are the only values allowed by the OpenAPI specification.
 */
public enum OpenApiSecurityLocation {

    QUERY("query"), HEADER("header"), COOKIE("cookie");

    private String displayValue;

    /**
     * Constructor that specifies the display value for the enumeration.
     * 
     * @param displayValue the display value of the enumeration
     */
    private OpenApiSecurityLocation(String displayValue) {
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
     * @param displayValue the display value of the location to return
     * @return SwaggerSecurityLocation
     */
    public static OpenApiSecurityLocation fromDisplayValue(String displayValue) {
        OpenApiSecurityLocation location = null;

        if (displayValue != null) {
            for (OpenApiSecurityLocation loc : values()) {
                if (displayValue.equalsIgnoreCase( loc.getDisplayValue() )) {
                    location = loc;
                    break;
                }
            }
        }
        return location;
    }

    /**
     * Returns the OpenAPI security location that corresponds to the Swagger value provided.
     * 
     * @param swaggerLocation the Swagger security location value
     * @return OpenApiSecurityLocation
     */
    public static OpenApiSecurityLocation fromSwaggerSecurityLocation(SwaggerSecurityLocation swaggerLocation) {
        OpenApiSecurityLocation openapiLocation = null;

        if (swaggerLocation != null) {
            switch (swaggerLocation) {
                case QUERY:
                    openapiLocation = QUERY;
                    break;
                case HEADER:
                    openapiLocation = HEADER;
                    break;
                default:
                    break;
            }
        }
        return openapiLocation;
    }

}
