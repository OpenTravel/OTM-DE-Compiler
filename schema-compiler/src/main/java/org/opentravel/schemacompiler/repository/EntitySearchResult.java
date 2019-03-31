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

package org.opentravel.schemacompiler.repository;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.EntityInfoType;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;

/**
 * Search result from a remote repository that encapsulates an OTM entity that matched the search criteria.
 */
public class EntitySearchResult extends RepositorySearchResult {

    private String entityName;
    private Class<?> entityType;

    /**
     * Constructor that initializes the search result item using the information provided.
     * 
     * @param entityInfo entity meta-data returned from the remote repository
     * @param manager the repository manager for the local environment
     */
    public EntitySearchResult(EntityInfoType entityInfo, RepositoryManager manager) {
        super( RepositoryUtils.createRepositoryItem( manager, entityInfo ) );
        checkItemState( manager );

        try {
            this.entityName = entityInfo.getEntityName();
            this.entityType = Class.forName( entityInfo.getEntityType() );

        } catch (ClassNotFoundException e) {
            this.entityType = NamedEntity.class; // Ignore and return a generic entity type
        }
    }

    /**
     * Returns the local name of the entity associated with this search result.
     *
     * @return String
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns the Java type for the entity associated with this search result.
     *
     * @return Class&lt;?&gt;
     */
    public Class<?> getEntityType() {
        return entityType;
    }

    /**
     * Returns the named entity associated with this search result item from the given model. If no matching entity
     * exists in the model provided, this method will return null.
     * 
     * @param model the model from which to retrieve the matching entity
     * @return NamedEntity
     */
    public NamedEntity findEntity(TLModel model) {
        RepositoryItem item = getRepositoryItem();
        AbstractLibrary library = model.getLibrary( item.getNamespace(), item.getLibraryName() );

        return (library == null) ? null : library.getNamedMember( entityName );
    }

}
