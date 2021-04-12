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

import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParamType;

/**
 * Enumeration of valid parameter types for OpenAPI documents.
 */
public enum OpenApiParamType {

    QUERY("query"), HEADER("header"), PATH("path"), COOKIE("cookie");

    private String inValue;

    /**
     * Constructor that specifies the 'in' value that should be included in the JSON Swagger document.
     * 
     * @param inValue the 'in' value for the Swagger document
     */
    private OpenApiParamType(String inValue) {
        this.inValue = inValue;
    }

    /**
     * Returns the 'in' value that should be included in the JSON Swagger document.
     *
     * @return String
     */
    public String getInValue() {
        return inValue;
    }

    /**
     * Returns the corresponding OpenAPI parameter type for the given Swagger type. For any types that do not directly
     * map, the <code>QUERY</code> type will be returned.
     * 
     * @param swaggerParamType the Swagger parameter type value
     * @return OpenApiParamType
     */
    public static OpenApiParamType fromSwaggerParamType(SwaggerParamType swaggerParamType) {
        OpenApiParamType openapiType = null;

        if (swaggerParamType != null) {
            switch (swaggerParamType) {
                case QUERY:
                    openapiType = QUERY;
                    break;
                case PATH:
                    openapiType = PATH;
                    break;
                case HEADER:
                    openapiType = HEADER;
                    break;
                default:
                    break;
            }
        }
        return openapiType;
    }

}
