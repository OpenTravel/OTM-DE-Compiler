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
package org.opentravel.schemacompiler.index.builder;

import java.net.URISyntaxException;

import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;

/**
 * Temporary extension to the repository manager that intercepts requests for downloads
 * to prevent the repeated download of the 'OTA_SimpleTypes' library from the OpenTravel
 * repository.
 */
public class IndexingRepositoryManager extends RepositoryManager {
	
	private static final RepositoryItem otaSimpleTypes;
	
	private boolean isOpenTravelRepository;
	
	/**
	 * Default constructor.
	 * 
	 * @param fileManager  the file manager for the repository
	 * @throws RepositoryException  thrown if an error occurs during initialization
	 */
	public IndexingRepositoryManager(RepositoryFileManager fileManager) throws RepositoryException {
		super( fileManager );
		isOpenTravelRepository = "Opentravel".equals( getId() );
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryManager#getRepositoryItem(java.lang.String, java.lang.String)
	 */
	@Override
	public RepositoryItem getRepositoryItem(String itemUri, String itemNamespace)
			throws RepositoryException, URISyntaxException {
		
		if (!isOpenTravelRepository && itemUri.equals( "otm://Opentravel/OTA_SimpleTypes_0_0_0.otm" )
				&& itemNamespace.equals( otaSimpleTypes.getNamespace() )) {
			return otaSimpleTypes;
			
		} else {
			return super.getRepositoryItem(itemUri, itemNamespace);
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryManager#getRepositoryItem(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public RepositoryItem getRepositoryItem(String baseNamespace, String filename, String versionIdentifier)
			throws RepositoryException {
		
		if (!isOpenTravelRepository && baseNamespace.equals( otaSimpleTypes.getBaseNamespace() )
				&& filename.equals( otaSimpleTypes.getFilename() )
				&& versionIdentifier.equals( otaSimpleTypes.getVersion() )) {
			return otaSimpleTypes;
			
		} else {
			return super.getRepositoryItem( baseNamespace, filename, versionIdentifier );
		}
	}

	/**
	 * Initializes the 'otaSimpleTypes' repository item.
	 */
	static {
		RepositoryItemImpl item = new RepositoryItemImpl();
		
		item.setBaseNamespace( "http://www.opentravel.org/OTM/Common" );
		item.setNamespace( "http://www.opentravel.org/OTM/Common/v0" );
		item.setFilename( "OTA_SimpleTypes_0_0_0.otm" );
		item.setLibraryName( "OTA_SimpleTypes" );
		item.setVersion( "0.0.0" );
		item.setStatus( TLLibraryStatus.FINAL );
		item.setState( RepositoryItemState.MANAGED_UNLOCKED );
		otaSimpleTypes = item;
	}
	
}
