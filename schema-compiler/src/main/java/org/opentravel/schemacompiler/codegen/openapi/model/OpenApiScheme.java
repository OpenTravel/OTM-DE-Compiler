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

import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerScheme;

/**
 * Enumeration that specifies the transfer protocols supported for a Swagger API specification document. The schemes
 * that are defined for this enumeration are the only values allowed by the Swagger specification.
 */
public enum OpenApiScheme {

    HTTP("http"), HTTPS("https"), WS("ws"), WSS("wss");

    private String displayValue;

    /**
     * Constructor that specifies the display value for the enumeration.
     * 
     * @param displayValue the display value of the enumeration
     */
    private OpenApiScheme(String displayValue) {
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
     * @param displayValue the display value of the scheme to return
     * @return SwaggerScheme
     */
    public static OpenApiScheme fromDisplayValue(String displayValue) {
        OpenApiScheme scheme = null;

        if (displayValue != null) {
            for (OpenApiScheme s : values()) {
                if (displayValue.equalsIgnoreCase( s.getDisplayValue() )) {
                    scheme = s;
                    break;
                }
            }
        }
        return scheme;
    }

    /**
     * Returns the OpenAPI scheme that corresponds to the Swagger value provided.
     * 
     * @param swaggerScheme the Swagger scheme value
     * @return OpenApiScheme
     */
    public static OpenApiScheme fromSwaggerScheme(SwaggerScheme swaggerScheme) {
        OpenApiScheme openapiScheme = null;

        if (swaggerScheme != null) {
            switch (swaggerScheme) {
                case HTTP:
                    openapiScheme = HTTP;
                    break;
                case HTTPS:
                    openapiScheme = HTTPS;
                    break;
                case WS:
                    openapiScheme = WS;
                    break;
                case WSS:
                    openapiScheme = WSS;
                    break;
                default:
                    break;
            }
        }
        return openapiScheme;
    }

}
