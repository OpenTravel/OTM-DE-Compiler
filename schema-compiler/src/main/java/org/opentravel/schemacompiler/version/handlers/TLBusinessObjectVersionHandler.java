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

import java.util.HashMap;
import java.util.Map;

import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.version.VersionSchemeException;

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
	
}
