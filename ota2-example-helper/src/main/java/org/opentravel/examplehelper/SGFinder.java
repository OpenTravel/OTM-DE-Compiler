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
package org.opentravel.examplehelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Provides a static utility method for discovering substitution group references
 * within an OTM <code>NamedEntity</code>.
 */
public class SGFinder {
	
	private List<QName> visitedEntities = new ArrayList<>();
	private Set<TLFacetOwner> sgReferences = new HashSet<>();
	
	/**
	 * Private constructor.
	 */
	private SGFinder() {}
	
	/**
	 * Returns a list of the substitution group type references within the structure of
	 * the given named entity.
	 * 
	 * @param entity  the OTM object for which to return substitution group references
	 * @return Set<TLFacetOwner>
	 */
	public static Set<TLFacetOwner> findSubstitutionGroupReferences(NamedEntity entity) {
		SGFinder sgFinder = new SGFinder();
		
		if (entity instanceof TLPropertyType) {
			sgFinder.visit( (TLPropertyType) entity );
			
		} else if (entity instanceof TLActionFacet) {
			sgFinder.visit( (TLActionFacet) entity );
		}
		return sgFinder.sgReferences;
	}
	
	/**
	 * Inspects the given entity and its member properties for substitution group
	 * references.
	 * 
	 * @param entity  the OTM object to visit
	 */
	private void visit(TLPropertyType entity) {
		QName entityName = new QName( entity.getNamespace(), entity.getLocalName() );
		
		if (!visitedEntities.contains( entityName )) {
			visitedEntities.add( entityName );
			
			if (entity instanceof TLAlias) {
				entity = (TLPropertyType) ((TLAlias) entity).getOwningEntity();
			}
			if (entity instanceof TLBusinessObject) {
				visit( (TLBusinessObject) entity );
				
			} else if (entity instanceof TLCoreObject) {
				visit( (TLCoreObject) entity );
				
			} else if (entity instanceof TLChoiceObject) {
				visit( (TLChoiceObject) entity );
				
			} else if (entity instanceof TLFacet) {
				visit( (TLFacet) entity );
			}
		}
	}
	
	/**
	 * Inspects the given business object and its member properties for substitution group
	 * references.
	 * 
	 * @param entity  the OTM object to visit
	 */
	private void visit(TLBusinessObject entity) {
		sgReferences.add( entity );
		visit( entity.getIdFacet() );
		visit( entity.getSummaryFacet() );
		visit( entity.getDetailFacet() );
		
		for (TLFacet cFacet : entity.getCustomFacets()) {
			visit( cFacet );
		}
	}
	
	/**
	 * Inspects the given core object and its member properties for substitution group
	 * references.
	 * 
	 * @param entity  the OTM object to visit
	 */
	private void visit(TLCoreObject entity) {
		sgReferences.add( entity );
		visit( entity.getSummaryFacet() );
		visit( entity.getDetailFacet() );
	}
	
	/**
	 * Inspects the given choice object and its member properties for substitution group
	 * references.
	 * 
	 * @param entity  the OTM object to visit
	 */
	private void visit(TLChoiceObject entity) {
		sgReferences.add( entity );
		visit( entity.getSharedFacet() );
		
		for (TLFacet cFacet : entity.getChoiceFacets()) {
			visit( cFacet );
		}
	}
	
	/**
	 * Inspects the given action facet and its member properties for substitution group
	 * references.
	 * 
	 * @param entity  the OTM object to visit
	 */
	private void visit(TLActionFacet entity) {
		NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( entity );
		
		if ((entity.getReferenceType() != TLReferenceType.NONE)
				&& ((entity.getReferenceFacetName() == null) || (entity.getReferenceFacetName().length() == 0))) {
			TLResource owningResource = entity.getOwningResource();
			TLBusinessObject boRef = (owningResource == null) ? null : owningResource.getBusinessObjectRef();
			
			if (boRef != null) {
				visit( boRef );
			}
		}
		if (payloadType instanceof TLCoreObject) {
			visit( (TLCoreObject) payloadType );
			
		} else if (payloadType instanceof TLChoiceObject) {
			visit( (TLChoiceObject) payloadType );
		}
	}
	
	/**
	 * Inspects the given facet and its member properties for substitution group
	 * references.
	 * 
	 * @param entity  the OTM object to visit
	 */
	private void visit(TLFacet entity) {
		List<TLProperty> facetElements = PropertyCodegenUtils.getInheritedFacetProperties( entity );
		
		for (TLProperty element : facetElements) {
			visit( element.getType() );
		}
	}
	
}
