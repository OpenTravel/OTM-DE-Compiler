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
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines the meta-model for an OpenAPI document.
 */
public class OpenApiDocument implements JsonModelObject {

    public static final String OPENAPI_SPEC_V3 = "3.0.0";

    private String specVersion = OPENAPI_SPEC_V3;
    private OpenApiOtmResource otmResource;
    private OpenApiInfo info;
    private List<OpenApiServer> servers = new ArrayList<>();
    private List<OpenApiPathItem> pathItems = new ArrayList<>();
    private OpenApiComponents components = new OpenApiComponents();

    /**
     * Returns the value of the 'specVersion' field.
     *
     * @return String
     */
    public String getSpecVersion() {
        return specVersion;
    }

    /**
     * Assigns the value of the 'specVersion' field.
     *
     * @param specVersion the field value to assign
     */
    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    /**
     * Returns the value of the 'otmResource' field.
     *
     * @return OpenApiOtmResource
     */
    public OpenApiOtmResource getOtmResource() {
        return otmResource;
    }

    /**
     * Assigns the value of the 'otmResource' field.
     *
     * @param otmResource the field value to assign
     */
    public void setOtmResource(OpenApiOtmResource otmResource) {
        this.otmResource = otmResource;
    }

    /**
     * Returns the value of the 'info' field.
     *
     * @return OpenApiInfo
     */
    public OpenApiInfo getInfo() {
        return info;
    }

    /**
     * Assigns the value of the 'info' field.
     *
     * @param info the field value to assign
     */
    public void setInfo(OpenApiInfo info) {
        this.info = info;
    }

    /**
     * Returns the value of the 'servers' field.
     *
     * @return List<OpenApiServer>
     */
    public List<OpenApiServer> getServers() {
        return servers;
    }

    /**
     * Returns the value of the 'pathItems' field.
     *
     * @return List<OpenApiPathItem>
     */
    public List<OpenApiPathItem> getPathItems() {
        return pathItems;
    }

    /**
     * Returns the value of the 'components' field.
     *
     * @return OpenApiComponents
     */
    public OpenApiComponents getComponents() {
        return components;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    public JsonObject toJson() {
        JsonObject pathsJson = new JsonObject();
        JsonObject json = new JsonObject();

        addProperty( json, "openapi", specVersion );
        addJsonProperty( json, "x-otm-resource", otmResource );
        addJsonProperty( json, "info", (info != null) ? info : new SwaggerInfo() );

        if (!servers.isEmpty()) {
            JsonArray serversJson = new JsonArray();

            servers.forEach( s -> serversJson.add( s.toJson() ) );
            json.add( "servers", serversJson );
        }

        for (OpenApiPathItem pathItem : pathItems) {
            pathsJson.add( pathItem.getPathTemplate(), pathItem.toJson() );
        }
        json.add( "paths", pathsJson );

        if (!components.isEmpty()) {
            json.add( "components", components.toJson() );
        }
        return json;
    }

}
