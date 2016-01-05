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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.version.VersionSchemeException;

/**
 * <code>VersionHandler</code> implementation for <code>TLCoreObject</code>
 * model entities.
 *
 * @author S. Livezey
 */
public class TLCoreObjectVersionHandler extends TLExtensionOwnerVersionHandler<TLCoreObject> {
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#createNewVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary)
	 */
	@Override
	public TLCoreObject createNewVersion(TLCoreObject origVersion, TLLibrary targetLibrary) {
		ModelElementCloner cloner = getCloner( origVersion );
        TLCoreObject newVersion = new TLCoreObject();
        
        newVersion.setName(origVersion.getName());
        newVersion.setDocumentation( cloner.clone( origVersion.getDocumentation() ) );
        newVersion.setSimpleFacet( cloner.clone( origVersion.getSimpleFacet() ) );
        setExtension( newVersion, origVersion );

        for (TLEquivalent equivalent : origVersion.getEquivalents()) {
            newVersion.addEquivalent( cloner.clone( equivalent ) );
        }
        targetLibrary.addNamedMember( newVersion );
        return newVersion;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary, org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
	 */
	@Override
	public void rollupMinorVersion(TLCoreObject minorVersion, TLLibrary majorVersionLibrary,
			RollupReferenceHandler referenceHandler) throws VersionSchemeException {
		TLCoreObject majorVersion = retrieveExistingVersion( minorVersion, majorVersionLibrary );
		
        if (majorVersion == null) {
        	majorVersion = getCloner( minorVersion ).clone( minorVersion );
            assignBaseExtension( majorVersion, minorVersion );
            majorVersionLibrary.addNamedMember( majorVersion );
            referenceHandler.captureRollupReferences( majorVersion );
        	
        } else if (majorVersion instanceof TLCoreObject) {
            rollupMinorVersion( minorVersion, majorVersion, referenceHandler );
        }
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
	 */
	@Override
	public void rollupMinorVersion(TLCoreObject minorVersion, TLCoreObject majorVersionTarget,
			RollupReferenceHandler referenceHandler) {
		VersionHandlerMergeUtils mergeUtils = new VersionHandlerMergeUtils( getFactory() );
        Map<String, TLFacet> targetFacets = new HashMap<String, TLFacet>();
        Map<String, TLFacet> sourceFacets = new HashMap<String, TLFacet>();
		
        mergeUtils.addToIdentityFacetMap( majorVersionTarget.getSummaryFacet(), targetFacets );
        mergeUtils.addToIdentityFacetMap( majorVersionTarget.getDetailFacet(), targetFacets );
        mergeUtils.addToIdentityFacetMap( minorVersion.getSummaryFacet(), sourceFacets );
        mergeUtils.addToIdentityFacetMap( minorVersion.getDetailFacet(), sourceFacets );
        mergeUtils.mergeFacets( targetFacets, sourceFacets, referenceHandler );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#getPatchableFacets(org.opentravel.schemacompiler.version.Versioned)
	 */
	@Override
	public List<TLPatchableFacet> getPatchableFacets(TLCoreObject entity) {
		List<TLPatchableFacet> facetList = new ArrayList<>();
		
		facetList.add(entity.getSummaryFacet());
		facetList.add(entity.getDetailFacet());
		return facetList;
	}
	
}
