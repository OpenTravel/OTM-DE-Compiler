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

package org.opentravel.schemacompiler.model;

/**
 * Enumeration that specified all of the supported MIME types for OTM REST request and response payloads.
 * 
 * @author S. Livezey
 */
public enum TLMimeType {

    APPLICATION_XML("application/xml"),
    TEXT_XML("text/xml"),
    APPLICATION_JSON("application/json"),
    TEXT_JSON("text/json");

    private String contentType;

    /**
     * Constructor that specifies the content type for each MIME type value.
     * 
     * @param contentType the W3C content type string
     */
    private TLMimeType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the W3C content type for this MIME type value.
     * 
     * @return String
     */
    public String toContentType() {
        return contentType;
    }

    /**
     * Returns the <code>TLMimeType</code> value associated with the given content type. If no such MIME type exists,
     * this method will return null.
     * 
     * @param contentType the content type string for which to return a value
     * @return TLMimeType
     */
    public TLMimeType fromContentType(String contentType) {
        TLMimeType value = null;

        for (TLMimeType mt : values()) {
            if (mt.toContentType().equals( contentType )) {
                value = mt;
                break;
            }
        }
        return value;
    }

}
