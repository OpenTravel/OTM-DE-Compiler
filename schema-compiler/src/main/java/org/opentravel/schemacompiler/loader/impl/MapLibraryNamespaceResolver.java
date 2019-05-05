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

import org.opentravel.schemacompiler.util.URLUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>LibraryNamespaceResolver</code> implementation backed by a simple map.
 * 
 * @author S. Livezey
 */
public class MapLibraryNamespaceResolver extends DefaultLibraryNamespaceResolver {

    private Map<URI,List<URL>> namespaceMappings = Collections.synchronizedMap( new HashMap<URI,List<URL>>() );

    /**
     * @see org.opentravel.schemacompiler.loader.impl.DefaultLibraryNamespaceResolver#resovleLibraryImport(java.net.URI,
     *      java.lang.String, java.lang.String[])
     */
    @Override
    public Collection<URL> resovleLibraryImport(URI libraryNamespace, String versionScheme, String[] fileHints) {
        Collection<URL> libraryUrls;

        if (namespaceMappings.containsKey( libraryNamespace )) {
            libraryUrls = namespaceMappings.get( libraryNamespace );
        } else {
            libraryUrls = super.resovleLibraryImport( libraryNamespace, versionScheme, fileHints );
        }
        return libraryUrls;
    }

    /**
     * Adds a library module namespace mapping to the catalog.
     * 
     * @param libraryNamespace the library module namespace URI
     * @param libraryUrl the URL of the library module
     */
    public void addNamespaceMapping(URI libraryNamespace, URL libraryUrl) {
        if (libraryNamespace == null) {
            throw new NullPointerException( "Library namespace URI cannot be null." );
        }
        if (libraryUrl == null) {
            throw new NullPointerException( "Library URL cannot be null." );
        }
        List<URL> urlList = namespaceMappings.computeIfAbsent( libraryNamespace, ns -> new ArrayList<>() );

        if (!urlList.contains( libraryUrl )) {
            urlList.add( libraryUrl );
        }
    }

    /**
     * Adds a library module namespace mapping to the catalog.
     * 
     * @param libraryNamespace the library module namespace URI
     * @param libraryUrl the URL of the library module
     * @throws URISyntaxException thrown if the namespace URI is badly-formed
     * @throws MalformedURLException thrown if the module URL is badly-formed
     */
    public void addNamespaceMapping(String libraryNamespace, String libraryUrl)
        throws URISyntaxException, MalformedURLException {
        if ((libraryUrl != null) && (libraryUrl.length() > 0)) {
            if (libraryUrl.startsWith( "." )) {
                addNamespaceMapping( new URI( libraryNamespace ),
                    URLUtils.getResolvedURL( libraryUrl, getRelativeUrlBase() ) );

            } else {
                addNamespaceMapping( new URI( libraryNamespace ), URLUtils.normalizeUrl( new URL( libraryUrl ) ) );
            }
        }
    }

    /**
     * Returns the base file path to use for resolving relative URL's. The default implementation returns the URL
     * location identified by the "user.dir" system property. Sub-classes may override if an alternate base path is
     * required.
     * 
     * @return URL
     */
    protected URL getRelativeUrlBase() {
        return URLUtils.toURL( new File( System.getProperty( "user.dir" ) ) );
    }

}
