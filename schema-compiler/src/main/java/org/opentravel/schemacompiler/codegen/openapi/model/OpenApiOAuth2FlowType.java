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

import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerOAuth2Flow;

/**
 * Enumeration for the types of valid OAuth2 flows that can be defined for an OpenAPI specification.
 */
public enum OpenApiOAuth2FlowType {

    IMPLICIT("implicit"), PASSWORD("password"), APPLICATION("application"), ACCESS_CODE("accessCode");

    private String displayValue;

    /**
     * Constructor that specifies the display value for the enumeration.
     * 
     * @param displayValue the display value of the enumeration
     */
    private OpenApiOAuth2FlowType(String displayValue) {
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
     * @param displayValue the display value of the security flow to return
     * @return OpenApiOAuth2FlowType
     */
    public static OpenApiOAuth2FlowType fromDisplayValue(String displayValue) {
        OpenApiOAuth2FlowType flowType = null;

        if (displayValue != null) {
            for (OpenApiOAuth2FlowType f : values()) {
                if (displayValue.equalsIgnoreCase( f.getDisplayValue() )) {
                    flowType = f;
                    break;
                }
            }
        }
        return flowType;
    }

    /**
     * Returns the OpenAPI OAuth2 Flow type that corresponds to the Swagger value provided.
     * 
     * @param swaggerOAuth2Flow the Swagger OAuth2 flow type value
     * @return OpenApiOAuth2FlowType
     */
    public static OpenApiOAuth2FlowType fromSwaggerOAuth2Flow(SwaggerOAuth2Flow swaggerOAuth2Flow) {
        OpenApiOAuth2FlowType openapiType = null;

        if (swaggerOAuth2Flow != null) {
            switch (swaggerOAuth2Flow) {
                case IMPLICIT:
                    openapiType = IMPLICIT;
                    break;
                case PASSWORD:
                    openapiType = PASSWORD;
                    break;
                case APPLICATION:
                    openapiType = APPLICATION;
                    break;
                case ACCESS_CODE:
                    openapiType = ACCESS_CODE;
                    break;
                default:
                    break;
            }
        }
        return openapiType;
    }

}
