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

import org.opentravel.schemacompiler.model.BuiltInLibrary;

/**
 * Interface to be implemented by components that are capable of loading a single built-in library.
 * 
 * @author S. Livezey
 */
public interface BuiltInLibraryLoader {

    /**
     * Loads and returs a single built-in library to be incorporated in a new <code>TLModel</code> instance.
     * 
     * @return BuiltInLibrary
     * @throws LibraryLoaderException thrown if the built-in library cannot be loaded
     */
    public BuiltInLibrary loadBuiltInLibrary() throws LibraryLoaderException;

}
