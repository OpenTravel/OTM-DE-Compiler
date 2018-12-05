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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Registry that handles lookups of <code>TLExtensionPointFacet</code>s for a particular facet.
 */
public class ExtensionPointRegistry {
	
	private static FacetCodegenDelegateFactory facetDelegateFactory = new FacetCodegenDelegateFactory(null);

    private Map<TLPatchableFacet,List<TLExtensionPointFacet>> registryMap;
	
    /**
     * Constructor that initializes the registry with content from the given model.
     * 
     * @param model  the model from which to initialize the registry
     */
	public ExtensionPointRegistry(TLModel model) {
        this.registryMap = new HashMap<>();

        for (TLLibrary library : model.getUserDefinedLibraries()) {
            for (TLExtensionPointFacet xpFacet : library.getExtensionPointFacetTypes()) {
                TLExtension extension = xpFacet.getExtension();
                NamedEntity extendedEntity = (extension == null) ? null : extension
                        .getExtendsEntity();

                if (extendedEntity instanceof TLPatchableFacet) {
                	TLPatchableFacet extendedFacet = (TLPatchableFacet) extendedEntity;
                    List<TLExtensionPointFacet> extensionPoints = registryMap.get(extendedFacet);

                    if (extensionPoints == null) {
                        extensionPoints = new ArrayList<>();
                        registryMap.put(extendedFacet, extensionPoints);
                    }
                    extensionPoints.add(xpFacet);
                }
            }
        }
	}
	
    /**
     * Returns the extension points from the model that reference the given entity facet. The
     * resulting map is indexed by the facet-type to which each <code>TLExtensionPointFacet</code>
     * is associated. The lists of extension point facets include those items that reference
     * extended entities of the facet's owner.
     * 
     * @param facet  the facet for which to return extension points
     * @return Map<TLFacetType,List<TLExtensionPointFacet>>
     */
    public Map<TLFacetType, List<TLExtensionPointFacet>> getExtensionPoints(TLPatchableFacet facet) {
        Map<TLFacetType, List<TLExtensionPointFacet>> result = new EnumMap<>( TLFacetType.class );
        MinorVersionHelper versionHelper = new MinorVersionHelper();

        // Lookup the extension point facets that reference the given entity facet
        if (registryMap != null) {
            List<TLPatchableFacet> facetHierarchy = new ArrayList<>();
            
            if (facet instanceof TLFacet) {
            	facetHierarchy.addAll( FacetCodegenUtils.getLocalFacetHierarchy((TLFacet) facet) );
            } else {
            	facetHierarchy.add( facet );
            }

            for (TLPatchableFacet hFacet : facetHierarchy) {
                TLFacetOwner facetOwner = hFacet.getOwningEntity();

                while ((hFacet != null) && (facetOwner != null)) {
                    List<TLExtensionPointFacet> hExtensionPoints = registryMap.get(hFacet);

                    if (hExtensionPoints != null) {
                        List<TLExtensionPointFacet> extensionPoints = result.get(hFacet.getFacetType());

                        if (extensionPoints == null) {
                            extensionPoints = new ArrayList<>();
                            result.put(hFacet.getFacetType(), extensionPoints);
                        }
                        for (TLExtensionPointFacet xpFacet : hExtensionPoints) {
                            extensionPoints.add(0, xpFacet); // add to beginning of list
                        }
                    }
                    
                    // If the new facet owner is a minor version extension of the previous facet owner,
                    // we need to break out of the loop.  This is based on an assumption that minor versions
                    // arleady have the patches from previous minor versions rolled up into them; therefore,
                    // the extension point is no longer relevant.
                    TLFacetOwner origFacetOwner = facetOwner;
                    TLFacetOwner facetOwnerExtension;
                    
                    facetOwner = facetOwnerExtension = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
                    
                    if (facetOwner instanceof Versioned) {
                    	Versioned priorMinorVersion;
						try {
							priorMinorVersion = versionHelper.getVersionExtension( (Versioned) origFacetOwner );
	                    	
	                    	if (facetOwnerExtension == priorMinorVersion) {
	                            facetOwner = null;
	                    	}
	                    	
						} catch (VersionSchemeException e) {
							// Ignore error and use the extension
						}
                    }
                    
                    // Use the facet owner to identify the facet for our next cycle through the loop
                    if (facetOwner == null) {
                    	hFacet = null;
                    } else {
                        if (hFacet instanceof TLFacet) {
                            hFacet = FacetCodegenUtils.getFacetOfType( facetOwner, hFacet.getFacetType(),
                            		FacetCodegenUtils.getFacetName((TLFacet) hFacet));
                        } else {
                            hFacet = FacetCodegenUtils.getFacetOfType( facetOwner, hFacet.getFacetType());
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Returns an identity string for the given facet, based on the facet's type and name.
     * 
     * @param facet  the facet for which to return an identity string
     * @return
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
     * @param facet  the facet for which an extension point element could be declared
     * @return boolean
     */
    public boolean hasExtensionPoint(TLFacet facet) {
    	return (((TLFacetCodegenDelegate) facetDelegateFactory.getDelegate( facet ))
    			.getExtensionPointElement() != null);
    }
    
}
