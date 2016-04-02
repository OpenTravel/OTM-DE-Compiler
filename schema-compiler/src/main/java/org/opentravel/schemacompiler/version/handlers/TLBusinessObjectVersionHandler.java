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
package org.opentravel.schemacompiler.version.handlers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.version.Versioned;
import org.opentravel.schemacompiler.visitor.ModelElementVisitor;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * <code>VersionHandler</code> implementation for <code>TLBusinessObject</code>
 * model entities.
 *
 * @author S. Livezey
 */
public class TLBusinessObjectVersionHandler extends TLExtensionOwnerVersionHandler<TLBusinessObject> {
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#createNewVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary)
	 */
	@Override
	public TLBusinessObject createNewVersion(TLBusinessObject origVersion, TLLibrary targetLibrary) {
		ModelElementCloner cloner = getCloner( origVersion );
        TLBusinessObject newVersion = new TLBusinessObject();

        newVersion.setName(origVersion.getName());
        newVersion.setDocumentation( cloner.clone( origVersion.getDocumentation() ) );
        setExtension(newVersion, origVersion);

        for (TLEquivalent equivalent : origVersion.getEquivalents()) {
            newVersion.addEquivalent(cloner.clone( equivalent ) );
        }
        targetLibrary.addNamedMember( newVersion );
        updateResourceReferences( origVersion, newVersion );
        return newVersion;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary, org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
	 */
	@Override
	public void rollupMinorVersion(TLBusinessObject minorVersion, TLLibrary majorVersionLibrary,
			RollupReferenceHandler referenceHandler) throws VersionSchemeException {
		TLBusinessObject majorVersion = retrieveExistingVersion( minorVersion, majorVersionLibrary );
		
        if (majorVersion == null) {
        	majorVersion = getCloner( minorVersion ).clone( minorVersion );
            assignBaseExtension( majorVersion, minorVersion );
            majorVersionLibrary.addNamedMember( majorVersion );
            referenceHandler.captureRollupReferences( majorVersion );
        	
        } else if (majorVersion instanceof TLBusinessObject) {
            rollupMinorVersion( minorVersion, majorVersion, referenceHandler );
        }
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
	 */
	@Override
	public void rollupMinorVersion(TLBusinessObject minorVersion, TLBusinessObject majorVersionTarget,
			RollupReferenceHandler referenceHandler) {
		VersionHandlerMergeUtils mergeUtils = new VersionHandlerMergeUtils( getFactory() );
        Map<String, TLFacet> targetFacets = new HashMap<String, TLFacet>();
        Map<String, TLFacet> sourceFacets = new HashMap<String, TLFacet>();
		
        mergeUtils.addToIdentityFacetMap( majorVersionTarget.getSummaryFacet(), targetFacets );
        mergeUtils.addToIdentityFacetMap( majorVersionTarget.getDetailFacet(), targetFacets );
        mergeUtils.addToIdentityFacetMap( minorVersion.getSummaryFacet(), sourceFacets );
        mergeUtils.addToIdentityFacetMap( minorVersion.getDetailFacet(), sourceFacets );

        for (TLFacet sourceFacet : minorVersion.getCustomFacets()) {
            TLFacet targetFacet = majorVersionTarget.getCustomFacet(
            		sourceFacet.getContext(), sourceFacet.getLabel() );

            if (targetFacet == null) {
                targetFacet = new TLFacet();
                targetFacet.setContext( sourceFacet.getContext() );
                targetFacet.setLabel( sourceFacet.getLabel() );
                majorVersionTarget.addCustomFacet( targetFacet );
            }
            mergeUtils.addToIdentityFacetMap( targetFacet, targetFacets );
            mergeUtils.addToIdentityFacetMap( sourceFacet, sourceFacets );
        }

        for (TLFacet sourceFacet : minorVersion.getQueryFacets()) {
            TLFacet targetFacet = majorVersionTarget.getQueryFacet(
            		sourceFacet.getContext(), sourceFacet.getLabel() );

            if (targetFacet == null) {
                targetFacet = new TLFacet();
                targetFacet.setContext( sourceFacet.getContext() );
                targetFacet.setLabel( sourceFacet.getLabel() );
                majorVersionTarget.addQueryFacet( targetFacet );
            }
            mergeUtils.addToIdentityFacetMap( targetFacet, targetFacets );
            mergeUtils.addToIdentityFacetMap( sourceFacet, sourceFacets );
        }
        mergeUtils.mergeFacets( targetFacets, sourceFacets, referenceHandler );
	}

	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#getPatchableFacets(org.opentravel.schemacompiler.version.Versioned)
	 */
	@Override
	public List<TLPatchableFacet> getPatchableFacets(TLBusinessObject entity) {
		List<TLPatchableFacet> facetList = new ArrayList<>();
		
        for (TLFacet customFacet : entity.getCustomFacets()) {
        	facetList.add(customFacet);
        }
        for (TLFacet queryFacet : entity.getQueryFacets()) {
        	facetList.add(queryFacet);
        }
        facetList.add(entity.getSummaryFacet());
        facetList.add(entity.getDetailFacet());
		return facetList;
	}
	
	/**
	 * When a new minor version of a business object is created, all of the resources that
	 * reference the original version should be updated to reference the new version if it
	 * is legal for them to do so.
	 * 
	 * @param origVersion  the original version of the business object
	 * @param newVersion  the new version of the business object
	 */
	private void updateResourceReferences(final TLBusinessObject origVersion, final TLBusinessObject newVersion) {
		try {
			VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( origVersion.getVersionScheme() );
			final Comparator<Versioned> vComparator = vScheme.getComparator( true );
			ModelElementVisitor visitor = new ModelElementVisitorAdapter() {
				
				public boolean visitResource(TLResource resource) {
					if (resource.getBusinessObjectRef().equals( origVersion )) {
						if (vComparator.compare( resource, newVersion ) >= 0) {
							resource.setBusinessObjectRef( newVersion );
						}
					}
					return false;
				}
				
			};
			
			ModelNavigator.navigate( origVersion.getOwningModel(), visitor );
			
		} catch (VersionSchemeException e) {
			// No action - should never happen at this point in the processing
		}
	}
	
}
