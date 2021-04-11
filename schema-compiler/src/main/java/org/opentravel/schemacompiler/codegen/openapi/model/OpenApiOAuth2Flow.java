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

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines an OAuth2 flow for an OpenAPI security scheme.
 */
public class OpenApiOAuth2Flow implements JsonModelObject {

    private OpenApiOAuth2FlowType type = OpenApiOAuth2FlowType.PASSWORD;
    private String authorizationUrl;
    private String tokenUrl;
    private String refreshUrl;
    private List<OpenApiSecurityScope> scopes = new ArrayList<>();

    /**
     * Returns the value of the 'type' field.
     *
     * @return OpenApiOAuth2FlowType
     */
    public OpenApiOAuth2FlowType getType() {
        return type;
    }

    /**
     * Assigns the value of the 'type' field.
     *
     * @param type the field value to assign
     */
    public void setType(OpenApiOAuth2FlowType type) {
        this.type = type;
    }

    /**
     * Returns the value of the 'authorizationUrl' field.
     *
     * @return String
     */
    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    /**
     * Assigns the value of the 'authorizationUrl' field.
     *
     * @param authorizationUrl the field value to assign
     */
    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    /**
     * Returns the value of the 'tokenUrl' field.
     *
     * @return String
     */
    public String getTokenUrl() {
        return tokenUrl;
    }

    /**
     * Assigns the value of the 'tokenUrl' field.
     *
     * @param tokenUrl the field value to assign
     */
    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    /**
     * Returns the value of the 'refreshUrl' field.
     *
     * @return String
     */
    public String getRefreshUrl() {
        return refreshUrl;
    }

    /**
     * Assigns the value of the 'refreshUrl' field.
     *
     * @param refreshUrl the field value to assign
     */
    public void setRefreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
    }

    /**
     * Returns the value of the 'scopes' field.
     *
     * @return List&lt;OpenApiSecurityScope&gt;
     */
    public List<OpenApiSecurityScope> getScopes() {
        return scopes;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        addProperty( json, "authorizationUrl", authorizationUrl );
        addProperty( json, "tokenUrl", tokenUrl );
        addProperty( json, "refreshUrl", refreshUrl );

        if (!scopes.isEmpty()) {
            JsonObject scopesJson = new JsonObject();

            scopes.forEach( s -> addProperty( scopesJson, s.getName(), s.getDescription() ) );
            json.add( "scopes", scopesJson );
        }
        return json;
    }

}
