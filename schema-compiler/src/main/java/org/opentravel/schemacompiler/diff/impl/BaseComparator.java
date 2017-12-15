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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.diff.ModelCompareOptions;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Base class for all components used to compare an aspect of the OTM model.
 */
public abstract class BaseComparator {
	
	protected static VersionSchemeFactory vsFactory = VersionSchemeFactory.getInstance();
	
	private Map<String,String> namespaceMappings = new HashMap<>();
	private ModelCompareOptions compareOptions;
	
	/**
	 * Constructor that initializes the comparison options and namespace mappings
	 * for the comparator.
	 * 
	 * @param compareOptions  the model comparison options to apply during processing
	 * @param namespaceMappings  the initial namespace mappings
	 */
	protected BaseComparator(ModelCompareOptions compareOptions, Map<String,String> namespaceMappings) {
		this.compareOptions = (compareOptions == null) ? ModelCompareOptions.getDefaultOptions() : compareOptions;
		
		if (namespaceMappings != null) {
			this.namespaceMappings.putAll( namespaceMappings );
		}
	}
	
	/**
	 * Returns the namespace mappings for this comparator.
	 * 
	 * @return Map<String,String>
	 */
	public Map<String,String> getNamespaceMappings() {
		return Collections.unmodifiableMap( namespaceMappings );
	}
	
	/**
	 * Adds a mapping the forces the namespace of a old version libraries to be evaluated
	 * as if it were assigned to the new version namespace.
	 *  
	 * @param oldVersionNS  the old version namespace to be mapped
	 * @param newVersionNS  the new version namespace to which the old version is mapped
	 */
	public void addNamespaceMapping(String oldVersionNS, String newVersionNS) {
		namespaceMappings.put( oldVersionNS, newVersionNS );
	}
	
	/**
	 * Returns the model comparison options that should be applied during processing.
	 * 
	 * @return ModelCompareOptions
	 */
	public ModelCompareOptions getCompareOptions() {
		return compareOptions;
	}
	
	/**
	 * Returns true if the old value and new object values are different.
	 * 
	 * @param oldValue  the old value to compare
	 * @param newValue  the new value to compare
	 * @return boolean
	 */
	protected boolean valueChanged(Object oldValue, Object newValue) {
		boolean result = (oldValue == null) ? (newValue != null) : !oldValue.equals( newValue );
		return result;
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
			changed = ((newValue != null) && (newValue.length() > 0));
			
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
	 * Returns true if the old and new of the entity names differ only by their version identifiers.
	 * 
	 * @param oldVersionName  the old version qualified name
	 * @param newVersionName  the new version qualified name
	 * @return boolean
	 */
	protected boolean isVersionChange(QName oldVersionName, QName newVersionName, String versionScheme) {
		boolean result = false;
		try {
			if (versionScheme == null) {
				versionScheme = VersionSchemeFactory.getInstance().getDefaultVersionScheme();
			}
			if ((oldVersionName != null) && (newVersionName != null) && (oldVersionName.getLocalPart() != null)
					&& oldVersionName.getLocalPart().equals( newVersionName.getLocalPart() )) {
				VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
				String oldVersionNS = oldVersionName.getNamespaceURI();
				String newVersionNS = newVersionName.getNamespaceURI();
				String oldVersionBaseNS = (oldVersionNS == null) ? null : vScheme.getBaseNamespace( oldVersionNS );
				String newVersionBaseNS = (newVersionNS == null) ? null : vScheme.getBaseNamespace( newVersionNS );
				
				return (oldVersionBaseNS == null) ? false : oldVersionBaseNS.equals( newVersionBaseNS );
			}
			
		} catch (VersionSchemeException e) {
			// Do not throw an exception - just return false
		}
		return result;
	}
	
	/**
	 * Returns true if the new entity is a later minor version of the old one.
	 * 
	 * @param oldEntity  the old version of the entity
	 * @param newEntity  the new version of the entity
	 * @return boolean
	 */
	protected boolean isMinorVersionCompare(EntityComparisonFacade oldEntity, EntityComparisonFacade newEntity,
			String versionScheme) {
		NamedEntity _oldEntity = oldEntity.getEntity();
		NamedEntity _newEntity = newEntity.getEntity();
		boolean localNamesMatch = _oldEntity.getLocalName().equals( _newEntity.getLocalName() );
		boolean result = false;
		
		// Make sure the local names match and that both entities are versioned and of the same type
		if (localNamesMatch && (_oldEntity instanceof Versioned) && (_newEntity instanceof Versioned)
				&& _oldEntity.getClass().equals( _newEntity.getClass() )) {
			try {
				VersionScheme vScheme = vsFactory.getVersionScheme( versionScheme );
				String oldNS = _oldEntity.getNamespace();
				String newNS = _newEntity.getNamespace();
				String oldVersionId = vScheme.getVersionIdentifier( oldNS );
				String newVersionId = vScheme.getVersionIdentifier( newNS );
				
				// Base namespaces and major version identifiers must match
				if (vScheme.getBaseNamespace( oldNS ).equals( vScheme.getBaseNamespace( newNS ) )
						&& vScheme.getMajorVersion( oldVersionId ).equals( vScheme.getMajorVersion( newVersionId ))) {
					List<Versioned> versionList = Arrays.asList( (Versioned) _oldEntity, (Versioned) _newEntity );
					
					// We have a minor version, but we need to make sure the earlier minor
					// version is our old entity
					Collections.sort( versionList, vScheme.getComparator( true ) );
					result = (versionList.get( 0 ) == _oldEntity);
				}
				
			} catch (VersionSchemeException e) {
				// Should never happen, but ignore and return false if it does
			}
		}
		return result;
	}
	
	/**
	 * Returns the version scheme for the library that owns the given entity.
	 * 
	 * @param entity  the entity for which to return the version scheme
	 * @return String
	 */
	protected String getVersionScheme(NamedEntity entity) {
		String versionScheme = null;
		
		if (entity != null) {
			AbstractLibrary owningLibrary = entity.getOwningLibrary();
			
			if (owningLibrary != null) {
				versionScheme = owningLibrary.getVersionScheme();
			}
		}
		return versionScheme;
	}
	
	/**
	 * Returns the version of the library that owns the given entity.
	 * 
	 * @param entity  the entity for which to return the version identifier
	 * @return String
	 */
	protected String getVersion(NamedEntity entity) {
		String version = null;
		
		if (entity != null) {
			AbstractLibrary owningLibrary = entity.getOwningLibrary();
			
			if (owningLibrary != null) {
				version = owningLibrary.getVersion();
			}
		}
		return version;
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
		QName entityName = null;
		
		if (entity != null) {
			String ns = namespaceMappings.get( entity.getNamespace() );
			String localName = null;
			
			if (ns == null) {
				ns = entity.getNamespace();
			}
			if (entity instanceof TLOperation) {
				localName = ((TLOperation) entity).getName();
				
			} else {
				localName = entity.getLocalName();
			}
			if (localName == null) {
				localName = "UNKNOWN";
			}
			entityName = new QName( ns, localName );
		}
		return entityName;
	}
	
}
