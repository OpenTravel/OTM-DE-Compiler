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

package org.opentravel.schemacompiler.codegen.example;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry that handles lookups of <code>TLExtensionPointFacet</code>s for a particular facet.
 */
public class ExtensionPointRegistry {

    private static FacetCodegenDelegateFactory facetDelegateFactory = new FacetCodegenDelegateFactory( null );

    private Map<TLPatchableFacet,List<TLExtensionPointFacet>> registryMap;

    /**
     * Constructor that initializes the registry with content from the given model.
     * 
     * @param model the model from which to initialize the registry
     */
    public ExtensionPointRegistry(TLModel model) {
        this.registryMap = new HashMap<>();

        for (TLLibrary library : model.getUserDefinedLibraries()) {
            for (TLExtensionPointFacet xpFacet : library.getExtensionPointFacetTypes()) {
                TLExtension extension = xpFacet.getExtension();
                NamedEntity extendedEntity = (extension == null) ? null : extension.getExtendsEntity();

                if (extendedEntity instanceof TLPatchableFacet) {
                    TLPatchableFacet extendedFacet = (TLPatchableFacet) extendedEntity;
                    registryMap.computeIfAbsent( extendedFacet, f -> registryMap.put( f, new ArrayList<>() ) );
                    registryMap.get( extendedFacet ).add( xpFacet );
                }
            }
        }
    }

    /**
     * Returns the extension points from the model that reference the given entity facet. The resulting map is indexed
     * by the facet-type to which each <code>TLExtensionPointFacet</code> is associated. The lists of extension point
     * facets include those items that reference extended entities of the facet's owner.
     * 
     * @param facet the facet for which to return extension points
     * @return Map&lt;TLFacetType,List&lt;TLExtensionPointFacet&gt;&gt;
     */
    public Map<TLFacetType,List<TLExtensionPointFacet>> getExtensionPoints(TLPatchableFacet facet) {
        Map<TLFacetType,List<TLExtensionPointFacet>> result = new EnumMap<>( TLFacetType.class );
        MinorVersionHelper versionHelper = new MinorVersionHelper();

        // Lookup the extension point facets that reference the given entity facet
        if (registryMap != null) {
            List<TLPatchableFacet> facetHierarchy = new ArrayList<>();

            if (facet instanceof TLFacet) {
                facetHierarchy.addAll( FacetCodegenUtils.getLocalFacetHierarchy( (TLFacet) facet ) );
            } else {
                facetHierarchy.add( facet );
            }

            for (TLPatchableFacet hFacet : facetHierarchy) {
                TLFacetOwner facetOwner = hFacet.getOwningEntity();

                while ((hFacet != null) && (facetOwner != null)) {
                    addExtensionPointsForFacet( hFacet, result );
                    facetOwner = nextFacetOwner( facetOwner, versionHelper );

                    // Use the facet owner to identify the facet for our next
                    // cycle through the loop
                    hFacet = getFacetFromNextOwner( hFacet, facetOwner );
                }
            }
        }
        return result;
    }

    /**
     * Returns an equivalent facet to the current one from the next facet owner.
     * 
     * @param currentFacet the current facet to use when matching the type/name of the next one
     * @param nextFacetOwner the next owner from which to lookup the resulting facet
     * @return TLPatchableFacet
     */
    private TLPatchableFacet getFacetFromNextOwner(TLPatchableFacet currentFacet, TLFacetOwner nextFacetOwner) {
        if (nextFacetOwner == null) {
            currentFacet = null;
        } else {
            if (currentFacet instanceof TLFacet) {
                currentFacet = FacetCodegenUtils.getFacetOfType( nextFacetOwner, currentFacet.getFacetType(),
                    FacetCodegenUtils.getFacetName( (TLFacet) currentFacet ) );
            } else {
                currentFacet = FacetCodegenUtils.getFacetOfType( nextFacetOwner, currentFacet.getFacetType() );
            }
        }
        return currentFacet;
    }

    /**
     * Returns the next-higher facet owner in the inheritance hierarchy from the current one provided.
     * 
     * @param currentFacetOwner the current facet owner being iterated
     * @param versionHelper the version helper instance to use for detecting minor version extensions
     * @return TLFacetOwner
     */
    private TLFacetOwner nextFacetOwner(TLFacetOwner currentFacetOwner, MinorVersionHelper versionHelper) {
        TLFacetOwner origFacetOwner = currentFacetOwner;
        TLFacetOwner facetOwnerExtension;

        currentFacetOwner = facetOwnerExtension = FacetCodegenUtils.getFacetOwnerExtension( currentFacetOwner );

        if (currentFacetOwner instanceof Versioned) {
            Versioned priorMinorVersion;
            try {
                priorMinorVersion = versionHelper.getVersionExtension( (Versioned) origFacetOwner );

                // If the new facet owner is a minor version extension of the previous facet owner,
                // we should return null for the next owner. This is based on an assumption that minor
                // versions arleady have the patches from previous minor versions rolled up into them.
                // Therefore, the extension point is no longer relevant.
                if (facetOwnerExtension == priorMinorVersion) {
                    currentFacetOwner = null;
                }

            } catch (VersionSchemeException e) {
                // Ignore error and use the extension
            }
        }
        return currentFacetOwner;
    }

    /**
     * Adds all of the extension point that apply to the given patchable facet to the map provided.
     * 
     * @param facet the patchable facet for which to look up extension point
     * @param epFacetMap the map to which all discovered extension points should be added
     */
    private void addExtensionPointsForFacet(TLPatchableFacet facet,
        Map<TLFacetType,List<TLExtensionPointFacet>> epFacetMap) {
        List<TLExtensionPointFacet> hExtensionPoints = registryMap.get( facet );

        if (hExtensionPoints != null) {
            epFacetMap.computeIfAbsent( facet.getFacetType(), ft -> epFacetMap.put( ft, new ArrayList<>() ) );
            List<TLExtensionPointFacet> extensionPoints = epFacetMap.get( facet.getFacetType() );

            for (TLExtensionPointFacet xpFacet : hExtensionPoints) {
                extensionPoints.add( 0, xpFacet ); // add to beginning of list
            }
        }
    }

    /**
     * Returns an identity string for the given facet, based on the facet's type and name.
     * 
     * @param facet the facet for which to return an identity string
     * @return String
     */
    public String getFacetIdentity(TLFacet facet) {
        TLFacetType facetType = facet.getFacetType();
        String identity;

        if (facetType != null) {
            if (facet instanceof TLContextualFacet) {
                identity = facetType.getIdentityName( ((TLContextualFacet) facet).getName() );
            } else {
                identity = facetType.getIdentityName();
            }

        } else {
            identity = "UNKNOWN";
        }
        return identity;
    }

    /**
     * Returns true if the given facet should declare an extension point.
     * 
     * @param facet the facet for which an extension point element could be declared
     * @return boolean
     */
    public boolean hasExtensionPoint(TLFacet facet) {
        return (((TLFacetCodegenDelegate) facetDelegateFactory.getDelegate( facet ))
            .getExtensionPointElement() != null);
    }

}
