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

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLPatchableFacet;

/**
 * <code>VersionHandler</code> implementation for <code>TLOpenEnumeration</code>
 * model entities.
 *
 * @author S. Livezey
 */
public class TLOpenEnumerationVersionHandler extends TLAbstractEnumerationVersionHandler<TLOpenEnumeration> {
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#createNewVersion(org.opentravel.schemacompiler.version.Versioned, org.opentravel.schemacompiler.model.TLLibrary)
	 */
	@Override
	public TLOpenEnumeration createNewVersion(TLOpenEnumeration origVersion, TLLibrary targetLibrary) {
		TLOpenEnumeration newVersion = new TLOpenEnumeration();

        newVersion.setName( origVersion.getName() );
        newVersion.setDocumentation( getCloner( origVersion ).clone( origVersion.getDocumentation() ) );
        setExtension( newVersion, origVersion );
        targetLibrary.addNamedMember( newVersion );
        
        return newVersion;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#getPatchableFacets(org.opentravel.schemacompiler.version.Versioned)
	 */
	@Override
	public List<TLPatchableFacet> getPatchableFacets(TLOpenEnumeration entity) {
		return Collections.emptyList();
	}
	
}
