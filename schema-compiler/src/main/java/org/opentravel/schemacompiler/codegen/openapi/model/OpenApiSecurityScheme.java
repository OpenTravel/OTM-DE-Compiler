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

import org.opentravel.schemacompiler.codegen.json.model.JsonModelObject;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerScheme;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityScheme;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a security scheme for a OpenAPI specification document.
 */
public class OpenApiSecurityScheme implements JsonModelObject {

    private String name;
    private OpenApiSecurityType type;
    private String description;
    private OpenApiSecurityLocation in;
    private OpenApiScheme scheme;
    private String bearerFormat;
    private List<OpenApiOAuth2Flow> flows = new ArrayList<>();
    private String openIdConnectUrl;

    /**
     * Default constructor.
     */
    public OpenApiSecurityScheme() {}

    /**
     * Constructor that creates an OpenAPI security scheme from the given Swagger security scheme info.
     * 
     * @param swaggerSecurityScheme the Swagger security scheme instance
     * @param swaggerScheme the global scheme supported by the Swagger binding style
     */
    public OpenApiSecurityScheme(SwaggerSecurityScheme swaggerSecurityScheme, SwaggerScheme swaggerScheme) {
        this.name = swaggerSecurityScheme.getName();
        this.type = OpenApiSecurityType.fromSwaggerSecurityType( swaggerSecurityScheme.getType() );
        this.description = swaggerSecurityScheme.getDescription();
        this.in = OpenApiSecurityLocation.fromSwaggerSecurityLocation( swaggerSecurityScheme.getIn() );
        this.scheme = OpenApiScheme.fromSwaggerScheme( swaggerScheme );

        if (swaggerSecurityScheme.getFlow() != null) {
            OpenApiOAuth2Flow openapiFlow = new OpenApiOAuth2Flow();

            openapiFlow.setType( OpenApiOAuth2FlowType.fromSwaggerOAuth2Flow( swaggerSecurityScheme.getFlow() ) );
            openapiFlow.setAuthorizationUrl( swaggerSecurityScheme.getAuthorizationUrl() );
            openapiFlow.setTokenUrl( swaggerSecurityScheme.getTokenUrl() );
            swaggerSecurityScheme.getScopes()
                .forEach( s -> openapiFlow.getScopes().add( new OpenApiSecurityScope( s ) ) );
            this.flows.add( openapiFlow );
        }
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
     * Returns the value of the 'type' field.
     *
     * @return OpenApiSecurityType
     */
    public OpenApiSecurityType getType() {
        return type;
    }

    /**
     * Assigns the value of the 'type' field.
     *
     * @param type the field value to assign
     */
    public void setType(OpenApiSecurityType type) {
        this.type = type;
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

    /**
     * Returns the value of the 'in' field.
     *
     * @return OpenApiSecurityLocation
     */
    public OpenApiSecurityLocation getIn() {
        return in;
    }

    /**
     * Assigns the value of the 'in' field.
     *
     * @param in the field value to assign
     */
    public void setIn(OpenApiSecurityLocation in) {
        this.in = in;
    }

    /**
     * Returns the value of the 'scheme' field.
     *
     * @return OpenApiScheme
     */
    public OpenApiScheme getScheme() {
        return scheme;
    }

    /**
     * Assigns the value of the 'scheme' field.
     *
     * @param scheme the field value to assign
     */
    public void setScheme(OpenApiScheme scheme) {
        this.scheme = scheme;
    }

    /**
     * Returns the value of the 'bearerFormat' field.
     *
     * @return String
     */
    public String getBearerFormat() {
        return bearerFormat;
    }

    /**
     * Assigns the value of the 'bearerFormat' field.
     *
     * @param bearerFormat the field value to assign
     */
    public void setBearerFormat(String bearerFormat) {
        this.bearerFormat = bearerFormat;
    }

    /**
     * Returns the value of the 'flow' field.
     *
     * @return List&lt;OpenApiOAuth2Flow&gt;
     */
    public List<OpenApiOAuth2Flow> getFlows() {
        return flows;
    }

    /**
     * Returns the value of the 'openIdConnectUrl' field.
     *
     * @return String
     */
    public String getOpenIdConnectUrl() {
        return openIdConnectUrl;
    }

    /**
     * Assigns the value of the 'openIdConnectUrl' field.
     *
     * @param openIdConnectUrl the field value to assign
     */
    public void setOpenIdConnectUrl(String openIdConnectUrl) {
        this.openIdConnectUrl = openIdConnectUrl;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        if (type != null) {
            json.addProperty( "type", type.getDisplayValue() );
        }
        addProperty( json, "description", description );
        addProperty( json, "name", name );

        if (in != null) {
            json.addProperty( "in", in.getDisplayValue() );
        }
        if (scheme != null) {
            json.addProperty( "scheme", scheme.getDisplayValue() );
        }
        addProperty( json, "bearerFormat", bearerFormat );

        if (!flows.isEmpty()) {
            JsonObject flowsJson = new JsonObject();

            flows.forEach( f -> flowsJson.add( f.getType().getDisplayValue(), f.toJson() ) );
            json.add( "flows", flowsJson );
        }
        addProperty( json, "openIdConnectUrl", openIdConnectUrl );

        return json;
    }

}
