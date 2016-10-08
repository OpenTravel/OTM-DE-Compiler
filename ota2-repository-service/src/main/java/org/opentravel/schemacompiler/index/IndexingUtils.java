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
package org.opentravel.schemacompiler.index;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.RepositoryItem;

/**
 * Static utility methods used during the repository indexing and search result processing.
 */
public class IndexingUtils {

	/**
	 * Returns the qualified identity key for the given OTM model entity.
	 * 
	 * @param entity  the named entity for which to return an identity key
	 * @return String
	 */
	public static String getIdentityKey(NamedEntity entity) {
		return getIdentityKey( entity, true );
	}

	/**
	 * Returns the qualified identity key for the given OTM model entity.  If the
	 * 'isSearchable' flag is true, the entity's index document will be included in
	 * the searchable index records.
	 * 
	 * @param entity  the named entity for which to return an identity key
	 * @param isSearchable  flag indicating whether the resulting index document should be searchable
	 * @return String
	 */
	public static String getIdentityKey(NamedEntity entity, boolean isSearchable) {
		StringBuilder identityKey = new StringBuilder();
		
		identityKey.append( entity.getNamespace() ).append(":");
		identityKey.append( entity.getLocalName() );
		if (!isSearchable) {
			identityKey.append(":meta-data");
		}
		return identityKey.toString();
	}
	
	/**
	 * Returns the searchable variant of the given identity key.  If the key is already
	 * searchable, the original string is returned.
	 * 
	 * @param identityKey  the identity key to process
	 * @return String
	 */
	public static String getSearchableIdentityKey(String identityKey) {
		String key = identityKey;
		
		if (key.endsWith(":meta-data")) {
			key = key.substring( 0, key.length() - 10 );
		}
		return key;
	}

	/**
	 * Returns the non-searchable variant of the given identity key.  If the key is already
	 * non-searchable, the original string is returned.
	 * 
	 * @param identityKey  the identity key to process
	 * @return String
	 */
	public static String getNonSearchableIdentityKey(String identityKey) {
		String key = identityKey;
		
		if (!key.endsWith(":meta-data")) {
			key += ":meta-data";
		}
		return key;
	}

	/**
	 * Returns the qualified identity key for the search index term.
	 * 
	 * @return String
	 */
	public static String getIdentityKey(RepositoryItem item) {
		StringBuilder identityKey = new StringBuilder();
		
		identityKey.append("LIB:");
		identityKey.append( item.getNamespace() ).append(":");
		identityKey.append( item.getLibraryName() );
		return identityKey.toString();
	}

	/**
	 * Returns the qualified identity key for the given library.
	 * 
	 * @return String
	 */
	public static String getIdentityKey(TLLibrary library) {
		StringBuilder identityKey = new StringBuilder();
		
		identityKey.append("LIB:");
		identityKey.append( library.getNamespace() ).append(":");
		identityKey.append( library.getName() );
		return identityKey.toString();
	}
	
}
