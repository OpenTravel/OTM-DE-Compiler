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

package org.opentravel.schemacompiler.version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Handles access to library and entity version chains for an OTM model.
 */
public class VersionChainFactory {
	
	private static VersionSchemeFactory vsFactory = VersionSchemeFactory.getInstance();
	
	private Map<String,Map<String,VersionChain<TLLibrary>>> libraryChains = new HashMap<>();
	private Map<String,Map<String,VersionChain<Versioned>>> entityChains = new HashMap<>();
	private Map<String,SortedSet<MajorVersionEntityGroup>> entityGroups = new HashMap<>();
	private TLModel model;
	
	/**
	 * Constructs a version chain factory for the given model.
	 * 
	 * @param model  the model for which to construct a factory instance
	 */
	public VersionChainFactory(TLModel model) {
		this.model = model;
		refresh();
	}
	
	/**
	 * Returns a list of all base namespaces in the current model.
	 * 
	 * @return List<String>
	 */
	public List<String> getBaseNamespaces() {
		return new ArrayList<>( libraryChains.keySet() );
	}
	
	/**
	 * Returns the list of all library version chains in the OTM model.
	 * 
	 * @return List<VersionChain<TLLibrary>>
	 */
	public List<VersionChain<TLLibrary>> getLibraryChains() {
		List<VersionChain<TLLibrary>> chainList = new ArrayList<>();
		
		for (String baseNS : libraryChains.keySet()) {
			Map<String,VersionChain<TLLibrary>> librariesByName = libraryChains.get( baseNS );
			
			for (String name : librariesByName.keySet()) {
				chainList.add( librariesByName.get( name ) );
			}
		}
		return chainList;
	}
	
	/**
	 * Returns the version chain for the given library or null if no such
	 * chain exists.
	 * 
	 * @param library  the library for which to return a version chain
	 * @return VersionChain<TLLibrary>
	 */
	public VersionChain<TLLibrary> getVersionChain(TLLibrary library) {
		Map<String,VersionChain<TLLibrary>> nsChainsByName = libraryChains.get( library.getBaseNamespace() );
		VersionChain<TLLibrary> versionChain = null;
		
		if (nsChainsByName != null) {
			versionChain = nsChainsByName.get( library.getName() );
		}
		return versionChain;
	}
	
	/**
	 * Returns the list of all entity version chains in the OTM model.
	 * 
	 * @return List<VersionChain<Versioned>>
	 */
	public List<VersionChain<Versioned>> getEntityChains() {
		List<VersionChain<Versioned>> chainList = new ArrayList<>();
		
		for (String baseNS : entityChains.keySet()) {
			Map<String,VersionChain<Versioned>> entitiesByName = entityChains.get( baseNS );
			
			for (String name : entitiesByName.keySet()) {
				chainList.add( entitiesByName.get( name ) );
			}
		}
		return chainList;
	}
	
	/**
	 * Returns the version chain for the given versioned entity or null if no such
	 * chain exists.
	 * 
	 * @param entity  the entity for which to return a version chain
	 * @return VersionChain<Versioned>
	 */
	@SuppressWarnings("unchecked")
	public <T extends Versioned> VersionChain<T> getVersionChain(T entity) {
		Map<String,VersionChain<Versioned>> nsChainsByName = entityChains.get( entity.getBaseNamespace() );
		VersionChain<T> versionChain = null;
		
		if (nsChainsByName != null) {
			versionChain = (VersionChain<T>) nsChainsByName.get( entity.getLocalName() );
		}
		return versionChain;
	}
	
	/**
	 * Returns the ordered list of named entity groups for the given base
	 * namespace.  The list returnedd is sorted in ascending version order.
	 * 
	 * @param baseNS  the base namespace for which to return the collection of entity groups
	 * @return SortedSet<MajorVersionEntityGroup>
	 */
	public SortedSet<MajorVersionEntityGroup> getVersionGroups(String baseNS) {
		return entityGroups.get( baseNS );
	}
	
	/**
	 * Refreshes the set of version chains managed by this factory.
	 */
	public void refresh() {
		Map<String,Map<String,MajorVersionEntityGroup>> entityGroupMap = new HashMap<>();
		
		libraryChains.clear();
		entityChains.clear();
		entityGroups.clear();
		
		// Collate libraries and entities by base namespace and name
		for (TLLibrary library : model.getUserDefinedLibraries()) {
			Map<String,VersionChain<TLLibrary>> librariesByName = libraryChains.get( library.getBaseNamespace() );
			Map<String,VersionChain<Versioned>> entitiesByName = entityChains.get( library.getBaseNamespace() );
			VersionScheme vScheme = getVersionScheme( library );
			VersionChain<TLLibrary> libraryChain;
			
			// Add library to index
			if (librariesByName == null) {
				librariesByName = new TreeMap<>();
				libraryChains.put( library.getBaseNamespace(), librariesByName );
			}
			libraryChain = librariesByName.get( library.getName() );
			
			if (libraryChain == null) {
				libraryChain = new VersionChain<>( library.getBaseNamespace(),
						library.getName(), new LibraryVersionComparator( vScheme, true ) );
				librariesByName.put( library.getName(), libraryChain );
			}
			libraryChain.addVersion( library );
			
			// Find/build the entity group for the members of this library
			MajorVersionEntityGroup entityGroup = null;
			
			if (!vScheme.isPatchVersion( library.getNamespace() )) {
				Map<String,MajorVersionEntityGroup> nsGroupMap = entityGroupMap.get( library.getBaseNamespace() );
				String majorVersionNamespace = vScheme.getMajorVersionNamespace( library.getNamespace() );
				
				if (nsGroupMap == null) {
					nsGroupMap = new HashMap<>();
					entityGroupMap.put( library.getBaseNamespace(), nsGroupMap );
				}
				entityGroup = nsGroupMap.get( majorVersionNamespace );
				
				if (entityGroup == null) {
					entityGroup = new MajorVersionEntityGroup( majorVersionNamespace, vScheme );
					nsGroupMap.put( majorVersionNamespace, entityGroup );
				}
			}
			
			// Add each versioned entity in the library to the index
			List<NamedEntity> allMembers = new ArrayList<>();
			
			allMembers.addAll( library.getNamedMembers() );
			
			for (TLResource resource : library.getResourceTypes()) {
				for (TLActionFacet af : resource.getActionFacets()) {
					allMembers.add( af );
				}
			}
			
			for (NamedEntity entity : allMembers) {
				if (entity instanceof Versioned) {
					VersionChain<Versioned> entityChain;
					
					if (entitiesByName == null) {
						entitiesByName = new TreeMap<>();
						entityChains.put( library.getBaseNamespace(), entitiesByName );
					}
					entityChain = entitiesByName.get( entity.getLocalName() );
					
					if (entityChain == null) {
						entityChain = new VersionChain<>( library.getBaseNamespace(),
								entity.getLocalName(), vScheme.getComparator( true ) );
						entitiesByName.put( entity.getLocalName(), entityChain );
					}
					entityChain.addVersion( (Versioned) entity );
					
					if (entityGroup != null) {
						entityGroup.addNamedMember( (Versioned) entity );
					}
				}
			}
		}
		
		// Final step is to construct the registry of named entity groups
		for (String baseNS : entityGroupMap.keySet()) {
			Map<String,MajorVersionEntityGroup> nsGroupMap = entityGroupMap.get( baseNS );
			SortedSet<MajorVersionEntityGroup> groupSet = new TreeSet<>();
			
			groupSet.addAll( nsGroupMap.values() );
			entityGroups.put( baseNS, groupSet );
		}
	}
	
	/**
	 * Returns the version scheme for the given library.
	 * 
	 * @param library  the library for which to return a version scheme
	 * @return VersionScheme
	 */
	private VersionScheme getVersionScheme(AbstractLibrary library) {
		VersionScheme vScheme = null;
		try {
			vScheme = vsFactory.getVersionScheme( library.getVersionScheme() );
			
		} catch (VersionSchemeException e) {
			try {
				vScheme = vsFactory.getVersionScheme( vsFactory.getDefaultVersionScheme() );
				
			} catch (VersionSchemeException e1) {
				throw new Error("Error - Default version scheme could not be identified.");
			}
		}
		return vScheme;
	}
	
}
