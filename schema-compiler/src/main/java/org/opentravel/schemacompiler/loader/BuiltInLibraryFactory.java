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

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory used to construct the list of built-in libraries that should be included with every model instance that is
 * processed by the loader.
 * 
 * @author S. Livezey
 */
public final class BuiltInLibraryFactory {

    private static final Logger log = LoggerFactory.getLogger( BuiltInLibraryFactory.class );

    private List<BuiltInLibraryLoader> loaders;

    /**
     * Returns the default factory instance.
     * 
     * @return BuiltInLibraryFactory
     */
    public static BuiltInLibraryFactory getInstance() {
        return (BuiltInLibraryFactory) SchemaCompilerApplicationContext.getContext()
            .getBean( SchemaCompilerApplicationContext.BUILT_IN_LIBRARY_FACTORY );
    }

    /**
     * Returns the list of built-in library loaders to be used by this factory.
     * 
     * @return List&lt;BuiltInLibraryLoader&gt;
     */
    public List<BuiltInLibraryLoader> getLoaders() {
        return loaders;
    }

    /**
     * Assigns the list of built-in library loaders to be used by this factory.
     * 
     * @param loaders the list of built-in laoders
     */
    public void setLoaders(List<BuiltInLibraryLoader> loaders) {
        this.loaders = loaders;
    }

    /**
     * Returns a list of built-in library instances to be included in a new model.
     * 
     * @return List&lt;BuiltInLibrary&gt;
     */
    public List<BuiltInLibrary> getBuiltInLibraries() {
        List<BuiltInLibrary> builtIns = new ArrayList<>();

        for (BuiltInLibraryLoader loader : loaders) {
            try {
                BuiltInLibrary library = loader.loadBuiltInLibrary();

                if (library != null) {
                    builtIns.add( library );
                }
            } catch (LibraryLoaderException e) {
                log.error( "Error loading built-in library", e );
            }
        }
        return builtIns;
    }

}
