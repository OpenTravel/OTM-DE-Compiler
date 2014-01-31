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
package org.opentravel.schemacompiler.loader.impl;

import java.net.URL;

import org.opentravel.schemacompiler.validate.Validatable;

/**
 * Source object wrapper for URL objects used for compatability with the validation framework.
 * 
 * @author S. Livezey
 */
public class URLValidationSource implements Validatable {

    private URL url;

    /**
     * Constructor that specifies the URL instance to be wrapped.
     * 
     * @param url
     *            the URL instance
     */
    public URLValidationSource(URL url) {
        this.url = url;
    }

    /**
     * Returns the underlying URL instance.
     * 
     * @return URL
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        if (url == null) {
            return "[MISSING URL]";
        } else {
            String urlPath = url.getPath();
            int idx = urlPath.lastIndexOf('/');

            if (!urlPath.endsWith("/") && (idx >= 0)) {
                urlPath = urlPath.substring(idx + 1);
            }
            return urlPath;
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof URLValidationSource) {
            result = (((URLValidationSource) obj).url == this.url);
        }
        return result;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (url == null) ? 0 : url.hashCode();
    }

}
