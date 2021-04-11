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

package org.opentravel.schemacompiler.codegen.json.model;

/**
 * Provides information about a JSON discriminator property used for type inheritance and polymorphism.
 */
public class JsonDiscriminator {

    /**
     * Enumeration that specifies whether the discriminator should be rendered in Swagger or OpenAPI format.
     */
    public static enum DiscriminatorFormat {
        SWAGGER, OPENAPI
    };

    private String propertyName;
    private DiscriminatorFormat format = DiscriminatorFormat.OPENAPI;

    /**
     * Returns the value of the 'propertyName' field.
     *
     * @return String
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Assigns the value of the 'propertyName' field.
     *
     * @param propertyName the field value to assign
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Returns the value of the 'format' field.
     *
     * @return DiscriminatorFormat
     */
    public DiscriminatorFormat getFormat() {
        return format;
    }

    /**
     * Assigns the value of the 'format' field.
     *
     * @param format the field value to assign
     */
    public void setFormat(DiscriminatorFormat format) {
        this.format = format;
    }

}
