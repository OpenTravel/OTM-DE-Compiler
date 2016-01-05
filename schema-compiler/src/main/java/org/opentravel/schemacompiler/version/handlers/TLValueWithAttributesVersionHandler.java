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

import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.version.VersionSchemeException;

/**
 * <code>VersionHandler</code> implementation for <code>TLValueWithAttributes</code>
 * model entities.
 *
 * @author S. Livezey
 */
public class TLValueWithAttributesVersionHandler extends VersionHandler<TLValueWithAttributes> {
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#getExtendedEntity(org.opentravel.schemacompiler.version.Versioned)
	 */
	@Override
	protected NamedEntity getExtendedEntity(TLValueWithAttributes entity) {
		return (entity == null) ? null : entity.getParentType();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#setExtension(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.version.Versioned)
	 */
	@Override
	public void setExtension(TLValueWithAttributes laterVersion, TLValueWithAttributes earlierVersion) {
		if (laterVersion != null) {
			laterVersion.setParentType( earlierVersion );
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#getBaseExtension(org.opentravel.schemacompiler.version.Versioned)
	 */
	@Override
	protected NamedEntity getBaseExtension(TLValueWithAttributes entity) throws VersionSchemeException {
		NamedEntity baseExtension = null;
		
		if (entity != null) {
	    	List<TLValueWithAttributes> priorMinorVersions = getAllVersionExtensions( entity );
	    	TLValueWithAttributes earliestMinorVersion = priorMinorVersions.isEmpty() ?
	    			null : priorMinorVersions.get(priorMinorVersions.size() - 1);
			
	    	if (earliestMinorVersion != null) {
	    		baseExtension = getExtendedEntity( earliestMinorVersion );
	    		
	    	} else {
	    		baseExtension = entity.getParentType();
	    	}
		}
		return baseExtension;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#createNewVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary)
	 */
	@Override
	public TLValueWithAttributes createNewVersion(TLValueWithAttributes origVersion, TLLibrary targetLibrary) {
        TLValueWithAttributes newVersion = new TLValueWithAttributes();
		ModelElementCloner cloner = getCloner( origVersion );

        newVersion.setName( origVersion.getName() );
        newVersion.setDocumentation( cloner.clone( origVersion.getDocumentation() ) );
        setExtension( newVersion, origVersion );

        for (TLEquivalent equivalent : origVersion.getEquivalents()) {
            newVersion.addEquivalent( cloner.clone( equivalent ) );
        }
        for (TLExample example : origVersion.getExamples()) {
            newVersion.addExample( cloner.clone( example ) );
        }
        targetLibrary.addNamedMember( newVersion );
        return newVersion;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary, org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
	 */
	@Override
	public void rollupMinorVersion(TLValueWithAttributes minorVersion, TLLibrary majorVersionLibrary,
			RollupReferenceHandler referenceHandler) throws VersionSchemeException {
		TLValueWithAttributes majorVersion = retrieveExistingVersion( minorVersion, majorVersionLibrary );
		
        if (majorVersion == null) {
        	majorVersion = getCloner( minorVersion ).clone( minorVersion );
        	majorVersion.setParentType( (TLAttributeType) getBaseExtension( minorVersion ) );
            majorVersionLibrary.addNamedMember( majorVersion );
            referenceHandler.captureRollupReferences( majorVersion );
        	
        } else if (majorVersion instanceof TLValueWithAttributes) {
            rollupMinorVersion( minorVersion, majorVersion, referenceHandler );
        }
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
	 */
	@Override
	public void rollupMinorVersion(TLValueWithAttributes minorVersion, TLValueWithAttributes majorVersionTarget,
			RollupReferenceHandler referenceHandler) {
        new VersionHandlerMergeUtils( getFactory() )
        		.mergeAttributes( majorVersionTarget, minorVersion.getAttributes(), referenceHandler );
        new VersionHandlerMergeUtils( getFactory() )
        		.mergeIndicators( majorVersionTarget, minorVersion.getIndicators() );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#getPatchableFacets(org.opentravel.schemacompiler.version.Versioned)
	 */
	@Override
	public List<TLPatchableFacet> getPatchableFacets(TLValueWithAttributes entity) {
		return Collections.emptyList();
	}
	
}
