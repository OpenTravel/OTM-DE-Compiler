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
package org.opentravel.schemacompiler.loader;

import java.net.URL;

/**
 * Input source used to access the content of a file-based type library module.
 * 
 * @param <C>
 *            the content type that will be returned by the input source
 * @author S. Livezey
 */
public interface LibraryInputSource<C> {

    /**
     * Returns the URL of the library module being accessed with this input source.
     * 
     * @return URL
     */
    public URL getLibraryURL();

    /**
     * Resolves the library module namespace provided and returns an input stream that can be used
     * to load the module's XML content. If the content of the module is not accessible, this method
     * will return null.
     * 
     * @param libraryContentURL
     *            the resource locator that can be used to obtain the module's content
     * @return C
     */
    public C getLibraryContent();

}
