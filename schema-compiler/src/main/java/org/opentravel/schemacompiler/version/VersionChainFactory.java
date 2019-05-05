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

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.util.SchemaCompilerRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
     * @param model the model for which to construct a factory instance
     */
    public VersionChainFactory(TLModel model) {
        this.model = model;
        refresh();
    }

    /**
     * Returns a list of all base namespaces in the current model.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getBaseNamespaces() {
        return new ArrayList<>( libraryChains.keySet() );
    }

    /**
     * Returns the list of all library version chains in the OTM model.
     * 
     * @return List&lt;VersionChain&lt;TLLibrary&gt;&gt;
     */
    public List<VersionChain<TLLibrary>> getLibraryChains() {
        List<VersionChain<TLLibrary>> chainList = new ArrayList<>();

        for (Entry<String,Map<String,VersionChain<TLLibrary>>> entry : libraryChains.entrySet()) {
            chainList.addAll( entry.getValue().values() );
        }
        return chainList;
    }

    /**
     * Returns the version chain for the given library or null if no such chain exists.
     * 
     * @param library the library for which to return a version chain
     * @return VersionChain&lt;TLLibrary&gt;
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
     * Returns the version chain for the given versioned entity or null if no such chain exists.
     * 
     * @param entity the entity for which to return a version chain
     * @param <T> the type of the versioned entity
     * @return VersionChain&lt;Versioned&gt;
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
     * Returns the list of all entity version chains in the OTM model.
     * 
     * @return List&lt;VersionChain&lt;Versioned&gt;&gt;
     */
    public List<VersionChain<Versioned>> getEntityChains() {
        List<VersionChain<Versioned>> chainList = new ArrayList<>();

        for (Entry<String,Map<String,VersionChain<Versioned>>> entry : entityChains.entrySet()) {
            chainList.addAll( entry.getValue().values() );
        }
        return chainList;
    }

    /**
     * Returns the ordered list of named entity groups for the given base namespace. The list returnedd is sorted in
     * ascending version order.
     * 
     * @param baseNS the base namespace for which to return the collection of entity groups
     * @return SortedSet&lt;MajorVersionEntityGroup&gt;
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
            MajorVersionEntityGroup entityGroup;

            // Add library to index
            addLibraryToChainIndex( library, librariesByName, vScheme );

            // Find/build the entity group for the members of this library
            entityGroup = buildEntityGroup( library, entityGroupMap, vScheme );

            // Add each versioned entity in the library to the index
            addVersionEntitiesToIndex( library, entitiesByName, entityGroup, vScheme );
        }

        // Final step is to construct the registry of named entity groups
        for (Entry<String,Map<String,MajorVersionEntityGroup>> entry : entityGroupMap.entrySet()) {
            String baseNS = entry.getKey();
            Map<String,MajorVersionEntityGroup> nsGroupMap = entry.getValue();
            SortedSet<MajorVersionEntityGroup> groupSet = new TreeSet<>();

            groupSet.addAll( nsGroupMap.values() );
            entityGroups.put( baseNS, groupSet );
        }
    }

    /**
     * Add each versioned entity from the given library to the index.
     * 
     * @param library the library that owns all of the entities to be added
     * @param entitiesByName the map that organizes each entity's version chain by entity name
     * @param entityGroup the major version entity group for the entities
     * @param vScheme the version scheme of the library
     */
    private void addVersionEntitiesToIndex(TLLibrary library, Map<String,VersionChain<Versioned>> entitiesByName,
        MajorVersionEntityGroup entityGroup, VersionScheme vScheme) {
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
                    entityChain = new VersionChain<>( library.getBaseNamespace(), entity.getLocalName(),
                        vScheme.getComparator( true ) );
                    entitiesByName.put( entity.getLocalName(), entityChain );
                }
                entityChain.addVersion( (Versioned) entity );

                if (entityGroup != null) {
                    entityGroup.addNamedMember( (Versioned) entity );
                }
            }
        }
    }

    /**
     * Find/build the entity group for the members of the givven library.
     * 
     * @param library the library for which to build the entity group
     * @param entityGroupMap the entity group map being constructed
     * @param vScheme the version scheme of the library
     * @return MajorVersionEntityGroup
     */
    private MajorVersionEntityGroup buildEntityGroup(TLLibrary library,
        Map<String,Map<String,MajorVersionEntityGroup>> entityGroupMap, VersionScheme vScheme) {
        MajorVersionEntityGroup entityGroup = null;

        if (!vScheme.isPatchVersion( library.getNamespace() )) {
            String majorVersionNamespace = vScheme.getMajorVersionNamespace( library.getNamespace() );
            String baseNS = library.getBaseNamespace();
            Map<String,MajorVersionEntityGroup> nsGroupMap;

            nsGroupMap = entityGroupMap.computeIfAbsent( baseNS, ns -> new HashMap<>() );
            entityGroup = nsGroupMap.computeIfAbsent( majorVersionNamespace,
                ns -> new MajorVersionEntityGroup( majorVersionNamespace, vScheme ) );
        }
        return entityGroup;
    }

    /**
     * Adds the library to the version chain index and the 'librariesByName' map.
     * 
     * @param library the library to be added to the index
     * @param librariesByName the map to which the library will be added that collates by name
     * @param vScheme the version scheme of the library
     */
    private void addLibraryToChainIndex(TLLibrary library, Map<String,VersionChain<TLLibrary>> librariesByName,
        VersionScheme vScheme) {
        VersionChain<TLLibrary> libraryChain;

        if (librariesByName == null) {
            librariesByName = new TreeMap<>();
            libraryChains.put( library.getBaseNamespace(), librariesByName );
        }
        libraryChain = librariesByName.get( library.getName() );

        if (libraryChain == null) {
            libraryChain = new VersionChain<>( library.getBaseNamespace(), library.getName(),
                new LibraryVersionComparator( vScheme, true ) );
            librariesByName.put( library.getName(), libraryChain );
        }
        libraryChain.addVersion( library );
    }

    /**
     * Returns the version scheme for the given library.
     * 
     * @param library the library for which to return a version scheme
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
                throw new SchemaCompilerRuntimeException( "Error - Default version scheme could not be identified." );
            }
        }
        return vScheme;
    }

}
