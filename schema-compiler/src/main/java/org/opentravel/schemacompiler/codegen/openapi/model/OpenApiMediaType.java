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
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;

import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for an OpenAPI Media-Type object used to represent a request or response payload
 * type.
 */
public class OpenApiMediaType implements JsonModelObject {

    private String mediaType;
    private JsonSchemaReference requestType;

    /**
     * Returns the value of the 'mediaType' field.
     *
     * @return String
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Assigns the value of the 'mediaType' field.
     *
     * @param mediaType the field value to assign
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Returns the value of the 'requestType' field.
     *
     * @return JsonSchemaReference
     */
    public JsonSchemaReference getRequestType() {
        return requestType;
    }

    /**
     * Assigns the value of the 'requestType' field.
     *
     * @param requestType the field value to assign
     */
    public void setRequestType(JsonSchemaReference requestType) {
        this.requestType = requestType;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    @Override
    public JsonObject toJson() {
        boolean isXmlMedia = "application/xml".equalsIgnoreCase( mediaType );
        JsonObject json = new JsonObject();

        if (requestType != null) {
            if (isXmlMedia) {
                JsonObject xmlSchemaRef = new JsonObject();

                json.add( "x-xml-schema", xmlSchemaRef );
                xmlSchemaRef.addProperty( "$xml-ref", requestType.getSchemaPath() );

            } else {
                json.add( "schema", requestType.toJson() );
            }
        }
        return json;
    }

}
