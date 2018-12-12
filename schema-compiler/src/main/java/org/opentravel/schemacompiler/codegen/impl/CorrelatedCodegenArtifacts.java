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
package org.opentravel.schemacompiler.codegen.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.model.NamedEntity;

/**
 * Collection of generated artifacts that can be segregated by object type and
 * correlated according to the OTM entity from which they were generated.
 */
public class CorrelatedCodegenArtifacts {
	
	private List<NamedEntity> entityList = new ArrayList<>();
	private Map<NamedEntity,CodegenArtifacts> entityArtifactMap = new HashMap<>();
	
	/**
	 * Adds an artifact that is associated with the given named entity.
	 * 
	 * @param entity  the entity with which the artifact is to be associated
	 * @param artifact  the artifact to add (null is ignored without error)
	 */
	public void addArtifact(NamedEntity entity, Object artifact) {
		if (!entityList.contains( entity )) {
			entityList.add( entity );
		}
		
		if (artifact != null) {
			entityArtifactMap.computeIfAbsent( entity, e -> entityArtifactMap.put( e, new CodegenArtifacts() ) );
			entityArtifactMap.get( entity ).addArtifact( artifact );
		}
	}
	
	/**
	 * Adds all of the given artifacts and associates them with the given named
	 * entity.
	 * 
	 * @param entity  the entity with which the artifacts are to be associated
	 * @param artifacts  the artifacts to add (null is ignored without error)
	 */
	public void addAllArtifacts(NamedEntity entity, CodegenArtifacts artifacts) {
		if (artifacts != null) {
			for (Object artifact : artifacts.getAllArtifacts()) {
				addArtifact( entity, artifact );
			}
		}
	}
	
	/**
	 * Adds all of the given correlated artifacts to this collection.  The entities
	 * in the given collection will be assumed to have been added after all of the entities
	 * in this collection.
	 * 
	 * @param correlatedArtifacts  the collection of correlated artifacts to add (null is ignored without error)
	 */
	public void addAllArtifacts(CorrelatedCodegenArtifacts correlatedArtifacts) {
		if (correlatedArtifacts != null) {
			for (NamedEntity entity : correlatedArtifacts.getEntities()) {
				addAllArtifacts( entity, correlatedArtifacts.getArtifacts( entity ) );
			}
		}
	}
	
	/**
	 * Returns the list of named entities for which artifacts were added to this
	 * collection.  Entities are returned in the order they were originally added.
	 * 
	 * @return List<NamedEntity>
	 */
	public List<NamedEntity> getEntities() {
		return Collections.unmodifiableList( entityList );
	}
	
	/**
	 * Returns the collection of artifacts that were associated with the given named
	 * entity.
	 * 
	 * @param entity  the entity for which to return the associated artifacts
	 * @return CodegenArtifacts
	 */
	public CodegenArtifacts getArtifacts(NamedEntity entity) {
		return entityArtifactMap.containsKey( entity ) ?
				entityArtifactMap.get( entity ) : new CodegenArtifacts();
	}
	
	/**
	 * Returns a consolidated list of all artifacts that have been correlated in the order
	 * of the enities with which they are associated.
	 * 
	 * @return CodegenArtifacts
	 */
	public CodegenArtifacts getConsolidatedArtifacts() {
		CodegenArtifacts allArtifacts = new CodegenArtifacts();
		
		for (NamedEntity entity : entityList) {
			allArtifacts.addAllArtifacts( getArtifacts( entity ) );
		}
		return allArtifacts;
	}
	
}
