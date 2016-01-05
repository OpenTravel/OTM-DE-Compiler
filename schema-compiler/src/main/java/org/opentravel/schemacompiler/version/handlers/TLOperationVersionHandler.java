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

import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.version.VersionSchemeException;

/**
 * <code>VersionHandler</code> implementation for <code>TLOperation</code>
 * model entities.
 *
 * @author S. Livezey
 */
public class TLOperationVersionHandler extends TLExtensionOwnerVersionHandler<TLOperation> {
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#createNewVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary)
	 */
	@Override
	public TLOperation createNewVersion(TLOperation origVersion, TLLibrary targetLibrary) {
        TLService newVersionService = getTargetServiceVersion( origVersion.getOwningService(), targetLibrary );
		ModelElementCloner cloner = getCloner( origVersion );
        TLOperation newVersion = new TLOperation();
        
        newVersion.setName( origVersion.getName() );
        newVersion.setDocumentation( cloner.clone( origVersion.getDocumentation() ) );
        setExtension( newVersion, origVersion );

        for (TLEquivalent equivalent : origVersion.getEquivalents()) {
            newVersion.addEquivalent( cloner.clone( equivalent ) );
        }
        newVersionService.addOperation( newVersion );
        return newVersion;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#retrieveExistingVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary)
	 */
	@Override
	public TLOperation retrieveExistingVersion(TLOperation origVersion, TLLibrary targetLibrary) {
		TLOperation existingVersion = null;
        
        if (origVersion != null) {
            TLService service = targetLibrary.getService();
            existingVersion = (service == null) ? null : service.getOperation( origVersion.getName() );
        }
        return existingVersion;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary, org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
	 */
	@Override
	public void rollupMinorVersion(TLOperation minorVersion, TLLibrary majorVersionLibrary,
			RollupReferenceHandler referenceHandler) throws VersionSchemeException {
		TLOperation majorVersion = retrieveExistingVersion( minorVersion, majorVersionLibrary );
		
        if (majorVersion == null) {
            TLService newVersionService = getTargetServiceVersion( minorVersion.getOwningService(), majorVersionLibrary );
            
        	majorVersion = getCloner( minorVersion ).clone( minorVersion );
            assignBaseExtension( majorVersion, minorVersion );
        	newVersionService.addOperation( majorVersion );
            referenceHandler.captureRollupReferences( majorVersion );
        	
        } else if (majorVersion instanceof TLOperation) {
            rollupMinorVersion( minorVersion, majorVersion, referenceHandler );
        }
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
	 */
	@Override
	public void rollupMinorVersion(TLOperation minorVersion, TLOperation majorVersionTarget,
			RollupReferenceHandler referenceHandler) {
		VersionHandlerMergeUtils mergeUtils = new VersionHandlerMergeUtils( getFactory() );
        Map<String, TLFacet> targetFacets = new HashMap<String, TLFacet>();
        Map<String, TLFacet> sourceFacets = new HashMap<String, TLFacet>();
		
        mergeUtils.addToIdentityFacetMap( majorVersionTarget.getRequest(), targetFacets );
        mergeUtils.addToIdentityFacetMap( majorVersionTarget.getResponse(), targetFacets );
        mergeUtils.addToIdentityFacetMap( majorVersionTarget.getNotification(), targetFacets );
        mergeUtils.addToIdentityFacetMap( minorVersion.getRequest(), sourceFacets );
        mergeUtils.addToIdentityFacetMap( minorVersion.getResponse(), sourceFacets );
        mergeUtils.addToIdentityFacetMap( minorVersion.getNotification(), sourceFacets );
        mergeUtils.mergeFacets( targetFacets, sourceFacets, referenceHandler );
	}
	
	/**
	 * Returns the service version from the target library.  If a service does not yet exist
	 * in the target library, one will be created automatically using the original version
	 * as a template.
	 * 
	 * @param origVersion  the original version of the service
	 * @param targetLibrary  the target library from which the new service version will be returned
	 * @return TLService
	 */
	private TLService getTargetServiceVersion(TLService origVersion, TLLibrary targetLibrary) {
		TLService newVersion = null;
		
		if (origVersion.getOwningLibrary() == targetLibrary) {
			newVersion = origVersion;
			
		} else if ((newVersion = targetLibrary.getService()) == null) {
			newVersion = new TLService();
			newVersion.setName( origVersion.getName() );
			newVersion.setDocumentation( getCloner( origVersion ).clone( origVersion.getDocumentation() ) );
            targetLibrary.setService( newVersion );
		}
		return newVersion;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#getPatchableFacets(org.opentravel.schemacompiler.version.Versioned)
	 */
	@Override
	public List<TLPatchableFacet> getPatchableFacets(TLOperation entity) {
		List<TLPatchableFacet> facetList = new ArrayList<>();
		
		facetList.add(entity.getRequest());
		facetList.add(entity.getResponse());
		facetList.add(entity.getNotification());
		return facetList;
	}
	
}
