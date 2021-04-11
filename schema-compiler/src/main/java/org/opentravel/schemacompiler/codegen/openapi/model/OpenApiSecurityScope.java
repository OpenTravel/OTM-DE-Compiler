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

import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityScope;

/**
 * Defines the scope and requirements of an OpenAPI OAuth2 security flow.
 */
public class OpenApiSecurityScope {

    private String name;
    private String description;

    /**
     * Default constructor.
     */
    public OpenApiSecurityScope() {}

    /**
     * Full constructor.
     * 
     * @param name the name of the security scope
     * @param description a brief description of the security scope
     */
    public OpenApiSecurityScope(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Constructor that initializes values from the Swagger scope provided.
     * 
     * @param swaggerScope the swagger security scope instance
     */
    public OpenApiSecurityScope(SwaggerSecurityScope swaggerScope) {
        this( swaggerScope.getName(), swaggerScope.getDescription() );
    }

    /**
     * Returns the value of the 'name' field.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns the value of the 'name' field.
     *
     * @param name the field value to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the value of the 'description' field.
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Assigns the value of the 'description' field.
     *
     * @param description the field value to assign
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
