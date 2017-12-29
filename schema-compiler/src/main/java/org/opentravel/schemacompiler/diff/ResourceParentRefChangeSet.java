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

package org.opentravel.schemacompiler.diff;

import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

/**
 * Container for all change items identified during the comparison of two resource
 * parent references.
 */
public class ResourceParentRefChangeSet extends ChangeSet<TLResourceParentRef,ResourceChangeItem> {
	
	/**
	 * Constructor that assigns the old and new version of a parent reference that was modified.
	 * 
	 * @param oldParentRef  the old version of the parent reference
	 * @param newParentRef  the new version of the parent reference
	 */
	public ResourceParentRefChangeSet(TLResourceParentRef oldParentRef, TLResourceParentRef newParentRef) {
		super( oldParentRef, newParentRef );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.diff.ChangeSet#getBookmarkId()
	 */
	@Override
	public String getBookmarkId() {
		TLResourceParentRef parentRef = (getNewVersion() != null) ? getNewVersion() : getOldVersion();
		TLResource owner = (parentRef == null) ? null : parentRef.getOwner();
		String parentName = (parentRef == null) ? null : parentRef.getParentResourceName();
		
		return getBookmarkId( owner ) + "$pref$" + ((parentName == null) ? "UNKNOWN_PARENTREF" : parentName);
	}
	
}
