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

package org.opentravel.schemacompiler.diff.impl;

import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;

/**
 * Base class for all components used to compare an aspect of the OTM model.
 */
public abstract class BaseComparator {
	
	/**
	 * Returns true if the old value and new object values are different.
	 * 
	 * @param oldValue  the old value to compare
	 * @param newValue  the new value to compare
	 * @return boolean
	 */
	protected boolean valueChanged(Object oldValue, Object newValue) {
		return (oldValue == null) ? (newValue == null) : oldValue.equals( newValue );
	}
	
	/**
	 * Returns true if the old value and new value are different.  Under this comparison,
	 * empty strings and null values evaluate as being equivalent.
	 * 
	 * @param oldValue  the old value to compare
	 * @param newValue  the new value to compare
	 * @return boolean
	 */
	protected boolean valueChanged(String oldValue, String newValue) {
		boolean changed;
		
		if ((oldValue == null) || (oldValue.length() == 0)) {
			changed = ((newValue == null) || (newValue.length() == 0));
			
		} else {
			changed = !oldValue.equals( newValue );
		}
		return changed;
	}
	
	/**
	 * Returns true if the old and new versions of the documentation are different.
	 * 
	 * @param oldDoc  the old documentation to compare
	 * @param newDoc  the new documentation to compare
	 * @return boolean
	 */
	protected boolean valueChanged(TLDocumentation oldDoc, TLDocumentation newDoc) {
		boolean changed = false;
		
		if ((oldDoc == null) || (newDoc == null)) {
			changed = (oldDoc == null) != (newDoc == null);
			
		} else {
			changed = valueChanged( oldDoc.getDescription(), newDoc.getDescription() );
			changed |= valueChanged( oldDoc.getDeprecations(), newDoc.getDeprecations() );
			changed |= valueChanged( oldDoc.getReferences(), newDoc.getReferences() );
			changed |= valueChanged( oldDoc.getImplementers(), newDoc.getImplementers() );
			changed |= valueChanged( oldDoc.getMoreInfos(), newDoc.getMoreInfos() );
			changed |= valueChanged( oldDoc.getOtherDocs(), newDoc.getOtherDocs() );
		}
		return changed;
	}
	
	/**
	 * Returns true if one or more of the items in the given list have been modified.
	 * 
	 * @param oldItems  the old list of documentation items to compare
	 * @param newItems  the new list of documentation items to compare
	 * @return boolean
	 */
	private boolean valueChanged(List<TLDocumentationItem> oldItems, List<TLDocumentationItem> newItems) {
		int oldSize = (oldItems == null) ? 0 : oldItems.size();
		int newSize = (newItems == null) ? 0 : newItems.size();
		boolean changed = false;
		
		if ((oldSize > 0) || (newSize > 0)) {
			if (oldSize != newSize) {
				changed = true;
				
			} else {
				for (int i = 0; i < newSize; i++) {
					TLDocumentationItem oldItem = oldItems.get( i );
					TLDocumentationItem newItem = newItems.get( i );
					String oldContext = (oldItem instanceof TLAdditionalDocumentationItem) ?
							((TLAdditionalDocumentationItem) oldItem).getContext() : null;
					String newContext = (oldItem instanceof TLAdditionalDocumentationItem) ?
							((TLAdditionalDocumentationItem) newItem).getContext() : null;
					
					if (valueChanged( oldContext, newContext ) || valueChanged( oldItem.getText(), newItem.getText() )) {
						changed = true;
						break;
					}
				}
			}
		}
		return changed;
	}
	
	/**
	 * Returns the qualified name of the given library.
	 * 
	 * @param library  the library for which to return the qualified name
	 * @return QName
	 */
	protected QName getLibraryName(TLLibrary library) {
		return (library == null) ? null : new QName( library.getNamespace(), library.getName() );
	}
	
	/**
	 * Returns the qualified name of the given entity.
	 * 
	 * @param entity  the entity for which to return the qualified name
	 * @return QName
	 */
	protected QName getEntityName(NamedEntity entity) {
		String localName = null;
		
		if (entity instanceof TLOperation) {
			localName = ((TLOperation) entity).getName();
			
		} else if (entity != null) {
			localName = entity.getLocalName();
		}
		return (entity == null) ? null : new QName( entity.getNamespace(), localName );
	}
	
}
