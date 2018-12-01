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
package org.opentravel.schemacompiler.transform.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Filter that allows resolution of "chameleon" entities from legacy XML schemas.
 * 
 * @author S. Livezey
 */
public class ChameleonFilter implements AnonymousEntityFilter {

    private Collection<String> anonymousLibraryUrls = new ArrayList<>();

    /**
     * Constructor that assigns the library that is to be considered the assigning entity for all
     * possible anonymous entity lookups.
     * 
     * @param contextLibrary
     *            the context library for anonymous assignments
     */
    public ChameleonFilter(AbstractLibrary contextLibrary) {
        if ((contextLibrary != null)
                && !AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE.equals(contextLibrary
                        .getNamespace())) {
            Collection<URL> chameleonUrls = new ArrayList<>();

            findChameleonLibraryUrls(contextLibrary, contextLibrary.getNamespace(), chameleonUrls,
                    new HashSet<AbstractLibrary>());
            init(chameleonUrls);
        }
    }

    /**
     * Constructor that assigns the list of library URL's from which anonymous entity references can
     * be resolved.
     * 
     * @param anonymousLibraryUrls
     *            the list of anonymous library URL's
     */
    public ChameleonFilter(Collection<URL> anonymousLibraryUrls) {
        init(anonymousLibraryUrls);
    }

    /**
     * @see org.opentravel.schemacompiler.transform.AnonymousEntityFilter#getAnonymousEntity(java.lang.String,
     *      org.opentravel.schemacompiler.transform.SymbolTable)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getAnonymousEntity(String localName, SymbolTable symbols) {
        Object anonymousEntity = symbols.getEntity(ANONYMOUS_PSEUDO_NAMESPACE, localName);
        Collection<Object> anonymousEntities;

        // First, obtain the list of possible anonymous entities and normalize the result into a
        // collection of zero or more objects
        if (anonymousEntity instanceof Collection) {
            anonymousEntities = (Collection<Object>) anonymousEntity;

        } else {
            anonymousEntities = new ArrayList<>();

            if (anonymousEntity != null) {
                anonymousEntities.add(anonymousEntity);
            }
        }

        // Search the candidate objects for an entity that is owned by an XSDLibrary whose URL
        // matches one of the valid anonymous library URL's provided in the constructor
        Object result = null;

        for (Object candidateEntity : anonymousEntities) {
            if (candidateEntity instanceof LibraryMember) {
                LibraryMember candidate = (LibraryMember) candidateEntity;
                AbstractLibrary owningLibrary = candidate.getOwningLibrary();

                if ((owningLibrary instanceof XSDLibrary)
                        && anonymousLibraryUrls.contains(owningLibrary.getLibraryUrl()
                                .toExternalForm())) {
                    result = candidate;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Recursive method that explores the include declarations of the given library to identify any
     * references to chameleon libraries that should be included in the filter.
     * 
     * @param library
     *            the library instance being explored
     * @param targetNamespace
     *            the namespace with which chameleon libraries will be associated
     * @param chameleonUrls
     *            the list of chameleon library URL's that have been identified
     * @param visitedLibraries
     *            the list of visited libraries
     */
    private void findChameleonLibraryUrls(AbstractLibrary library, String targetNamespace,
            Collection<URL> chameleonUrls, Set<AbstractLibrary> visitedLibraries) {
    	if (library != null) {
            if (isChameleon(library) && !chameleonUrls.contains(library.getLibraryUrl())) {
                chameleonUrls.add(library.getLibraryUrl());
            }
            visitedLibraries.add(library);

            // Built-in libraries do not support includes
            if (!(library instanceof BuiltInLibrary) && (library.getOwningModel() != null)) {
                TLModel model = library.getOwningModel();

                for (TLInclude include : library.getIncludes()) {
                    try {
                        URL includedUrl = URLUtils.getResolvedURL(include.getPath(),
                                URLUtils.getParentURL(library.getLibraryUrl()));
                        AbstractLibrary includedLibrary = model.getLibrary(includedUrl);

                        // Skip include URL's that do not resolve to a valid library in the model; these
                        // are most likely
                        // errors that will be detected by the validator.
                        if (includedLibrary == null) {
                            continue;
                        }

                        // Disregard non-chameleon libraries that are assigned to a different namespace.
                        // These are errors that will be picked up by the validator
                        if (!isChameleon(includedLibrary)
                                && !includedLibrary.getNamespace().equals(targetNamespace)) {
                            continue;
                        }

                        // If we have not seen the included library before, recurse to determine if it
                        // (or one of
                        // its includes) is a chameleon that should be considered.
                        if (!visitedLibraries.contains(includedLibrary)) {
                            findChameleonLibraryUrls(includedLibrary, targetNamespace, chameleonUrls,
                                    visitedLibraries);
                        }

                    } catch (MalformedURLException e) {
                        // No error - just skip and move on to the next include
                    }
                }
            }
    	}
    }

    /**
     * Returns true if the given library should be considered a chameleon.
     * 
     * @param library
     *            the library instance to analyze
     * @return boolean
     */
    private boolean isChameleon(AbstractLibrary library) {
        return (library != null)
                && ((library.getNamespace() == null) || library.getNamespace().equals(
                        AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE));
    }

    /**
     * Initializes this filter instance with the list of URL's for libraries that should be
     * considered eligible for anonymous references from the context library.
     * 
     * @param anonymousLibraryUrls
     *            the list of URLs for anonymous libraries to be considered by this filter
     */
    private void init(Collection<URL> anonymousLibraryUrls) {
        if (anonymousLibraryUrls != null) {
            for (URL libraryUrl : anonymousLibraryUrls) {
                this.anonymousLibraryUrls.add(libraryUrl.toExternalForm());
            }
        }
    }

}
