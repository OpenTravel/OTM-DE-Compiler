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

import java.net.URI;
import java.net.URL;
import java.util.Collection;

import org.opentravel.schemacompiler.model.TLModel;
import org.w3._2001.xmlschema.Schema;

/**
 * Resolves library module references by returning the library resource URL(s) associated with the
 * library modules' namespace. Because multiple libraries can be associated with a single namespace,
 * the 'resolveLibraryNamespace()' returns a collection of URL's.
 * 
 * @author S. Livezey
 */
public interface LibraryNamespaceResolver {

    /**
     * Resolves the library import using the namespace URI and/or file hints provided, returning a
     * collection of URLs that can be used to obtain the content of each library module.
     * 
     * @param libraryNamespace
     *            the library module namespace to resolve
     * @param versionScheme
     *            the version scheme to apply when interpreting the namespace provided (may be null)
     * @param fileHints
     *            the list of file hints that should be consindered when attempting to identify the
     *            resource
     * @return Collection<URL>
     */
    public Collection<URL> resovleLibraryImport(URI libraryNamespace, String versionScheme,
            String[] fileHints);

    /**
     * Resolves the given library using the namespace URI and/or file hints provided, returning a
     * URL that can be used to obtain the library module's content.
     * 
     * @param libraryNamespace
     *            the library module namespace to resolve
     * @param versionScheme
     *            the version scheme to apply when interpreting the namespace provided (may be null)
     * @param includePath
     *            the path of the included the resource to be resolved
     * @return URL
     */
    public URL resovleLibraryInclude(URI libraryNamespace, String includePath);

    /**
     * Assigns the model from which imports and includes are being resolved.
     * 
     * @param model
     *            the model instance to assign
     */
    public void setModel(TLModel model);

    /**
     * Assigns the library instance that should be considered as the contextual reference for this
     * <code>LibrayNamespaceResolver</code>.
     * 
     * @param contextLibrary
     *            the contextual reference used to resolve namespace-to-URL mappings
     * @param libraryUrl
     *            the URL of the context library passed to this method
     */
    public void setContextLibrary(LibraryModuleInfo<Object> contextLibrary, URL libraryUrl);

    /**
     * Assigns the schema instance that should be considered as the contextual reference for this
     * <code>LibrayNamespaceResolver</code>.
     * 
     * @param contextSchema
     *            the contextual reference used to resolve namespace-to-URL mappings
     * @param schemaUrl
     *            the URL of the context schema passed to this method
     */
    public void setContextSchema(LibraryModuleInfo<Schema> contextSchema, URL schemaUrl);
    
    /**
     * Assigns the URL location of the library with the given repository file URI.  This
     * is included as an optimization to prevent repeated downloads of file content during
     * the namespace resolution phase of a load.
     * 
     * @param repositoryUri  the URI of the repository item
     * @param libraryUrl  the resolved URL of the library
     */
    public void setRepositoryLocation(String repositoryUri, String namespace, String libraryUrl);

}
