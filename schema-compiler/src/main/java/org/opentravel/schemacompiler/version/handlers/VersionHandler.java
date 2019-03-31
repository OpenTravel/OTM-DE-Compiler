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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.version.Versioned;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all helper functions that are specific to a particular <code>Versioned</code> entity type.
 * 
 * @param <V> the versioned type to which the handler will be applied
 * @author S. Livezey
 */
public abstract class VersionHandler<V extends Versioned> {

    private VersionHandlerFactory factory;

    /**
     * Returns the factory that created this handler.
     * 
     * @return VersionHandlerFactory
     */
    public VersionHandlerFactory getFactory() {
        return factory;
    }

    /**
     * Assigns a reference to the factory that created this handler.
     * 
     * @param factory the factory that created this handler instance
     */
    public void setFactory(VersionHandlerFactory factory) {
        this.factory = factory;
    }

    /**
     * If the given versioned entity is a minor version of an entity defined in a prior version, this method will return
     * that prior version.
     * 
     * @param entity the versioned entity for which to return the previous version
     * @return V
     * @throws VersionSchemeException thrown if the entity's version scheme is not recognized
     */
    @SuppressWarnings("unchecked")
    public V getVersionExtension(V entity) throws VersionSchemeException {
        NamedEntity extendedEntity = getExtendedEntity( entity );
        V extendedVersion = null;

        // Determine whether the extended entity is a minor version of the one that
        // was passed to this method
        if ((extendedEntity != null) && extendedEntity.getClass().equals( entity.getClass() )
            && extendedEntity.getLocalName().equals( entity.getLocalName() )) {
            String origBaseNamespace = entity.getBaseNamespace();
            String extendedBaseNamespace = ((Versioned) extendedEntity).getBaseNamespace();

            if ((origBaseNamespace != null) && origBaseNamespace.equals( extendedBaseNamespace )) {
                VersionScheme versionScheme = getVersionScheme( entity );
                List<String> versionChain = versionScheme.getMajorVersionChain( entity.getNamespace() );

                if (versionChain.contains( extendedEntity.getNamespace() )) {
                    extendedVersion = (V) extendedEntity;
                }
            }
        }
        return extendedVersion;
    }

    /**
     * If the given versioned entity is a minor version of an entity defined in a prior version, this method will return
     * an ordered list of all the previous versions. The list is sorted in descending order (i.e. the latest prior
     * version will be the first element in the list).
     * 
     * @param versionedEntity the versioned entity for which to return the previous version
     * @return List&lt;V&gt;
     * @throws VersionSchemeException thrown if the entity's version scheme is not recognized
     */
    public List<V> getAllVersionExtensions(V versionedEntity) throws VersionSchemeException {
        List<V> extendedVersions = new ArrayList<>();
        V extendedVersion = versionedEntity;

        while ((extendedVersion = getVersionExtension( extendedVersion )) != null) {
            extendedVersions.add( extendedVersion );
        }
        Collections.sort( extendedVersions, getVersionScheme( versionedEntity ).getComparator( false ) );
        return extendedVersions;
    }

    /**
     * Returns the entity that is extended by the given one, or null if no extension is present.
     * 
     * @param entity the versioned entity for which to return an extension
     * @return NamedEntity
     */
    protected abstract NamedEntity getExtendedEntity(V entity);

    /**
     * Assigns the earlier version as an extension of the later version.
     * 
     * @param laterVersion the later version of the entity
     * @param earlierVersion the earlier version of the entity
     */
    public abstract void setExtension(V laterVersion, V earlierVersion);

    /**
     * Returns the base extension of the given entity. The base extension is the extended type from the earliest minor
     * version in the entity's major version chain. For this reason, the base extension will never be a prior minor
     * version of the entity.
     * 
     * @param entity the entity for which to return the base extension
     * @return NamedEntity
     * @throws VersionSchemeException thrown if the entity's version scheme is not recognized
     */
    protected abstract NamedEntity getBaseExtension(V entity) throws VersionSchemeException;

    /**
     * Constructs a new version of the given entity that is an exact copy except that it does not contain any child
     * members (e.g. for a business object, the facets will not contain any attributes, elements, or indicators. The new
     * version returned by this method is configured to extend the old version provided.
     * 
     * @param origVersion the original version from which to construct the new version instance
     * @param targetLibrary the library to which the new version should be assigned
     * @return V
     */
    public abstract V createNewVersion(V origVersion, TLLibrary targetLibrary);

    /**
     * Returns a newer version of the given entity from the target library. If a new version of the entity does not yet
     * exist in the library, a new version instance is created automatically.
     * 
     * @param origVersion the original version from which to construct the new version instance
     * @param targetLibrary the library to which the new version should be assigned
     * @return V
     */
    public V createOrRetrieveNewVersion(V origVersion, TLLibrary targetLibrary) {
        V newVersion = retrieveExistingVersion( origVersion, targetLibrary );

        if (newVersion == null) {
            newVersion = createNewVersion( origVersion, targetLibrary );
        }
        return newVersion;
    }

    /**
     * Returns an existing version of the given entity from the target library. If a version with the original version's
     * name is not defined in the library, this method will return null.
     * 
     * @param origVersion the original version of the entity
     * @param targetLibrary the target library from which a related entity version will be returned
     * @return V
     */
    @SuppressWarnings("unchecked")
    public V retrieveExistingVersion(V origVersion, TLLibrary targetLibrary) {
        V existingVersion = null;

        if (origVersion != null) {
            NamedEntity targetEntity = targetLibrary.getNamedMember( origVersion.getLocalName() );

            if ((targetEntity != null) && origVersion.getClass().equals( targetEntity.getClass() )) {
                existingVersion = (V) targetEntity;
            }
        }
        return existingVersion;
    }

    /**
     * Rolls up the contents of the given minor entity version into a major version of the same entity in the library
     * provided. If a major version of the entity does not yet exist in the library, one will be created automatically.
     * 
     * @param minorVersion the minor entity version to be rolled up
     * @param majorVersionLibrary the library that contains (or will contain) the new major entity version
     * @param referenceHandler handler that stores reference information for the libraries being rolled up
     * @return V
     * @throws VersionSchemeException thrown if the entity's version scheme is not recognized
     */
    public abstract V rollupMinorVersion(V minorVersion, TLLibrary majorVersionLibrary,
        RollupReferenceHandler referenceHandler) throws VersionSchemeException;

    /**
     * Rolls up the contents of the given minor version into the new major version. A roll-up is essentially a merge of
     * the contents (i.e. attributes, properties, indicators, etc.) from the minor version.
     * 
     * @param minorVersion the minor version of the entity whose items will be the source of the roll-up
     * @param majorVersionTarget the major version of the entity that will receive any rolled up items
     * @param referenceHandler handler that stores reference information for the libraries being rolled up
     */
    public abstract void rollupMinorVersion(V minorVersion, V majorVersionTarget,
        RollupReferenceHandler referenceHandler);

    /**
     * Returns the list of patchable facets for the given versioned entity.
     * 
     * @param entity the entity for which to return patchable facets
     * @return List&lt;TLPatchableFacet&gt;
     */
    public abstract List<TLPatchableFacet> getPatchableFacets(V entity);

    /**
     * Returns the <code>VersionScheme</code> associated with the given versioned entity.
     * 
     * @param entity the versioned entity for which to return the scheme
     * @return VersionScheme
     * @throws VersionSchemeException thrown if the entity's version scheme is not recognized
     */
    protected VersionScheme getVersionScheme(Versioned entity) throws VersionSchemeException {
        VersionSchemeFactory f = VersionSchemeFactory.getInstance();
        return (entity == null) ? null : f.getVersionScheme( entity.getVersionScheme() );
    }

    /**
     * Returns a <code>ModelElementCloner</code> that can be used to create a deep clone of the given entity.
     * 
     * @param entity the versioned entity for which to return a cloner
     * @return ModelElementCloner
     */
    protected ModelElementCloner getCloner(NamedEntity entity) {
        ModelElementCloner cloner = null;

        if ((entity != null) && (factory != null)) {
            cloner = factory.getCloner( entity.getOwningModel() );
        }
        return cloner;
    }

}
