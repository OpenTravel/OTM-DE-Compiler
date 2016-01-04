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

import java.util.HashSet;
import java.util.Set;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * Locates an appropriate <code>TLDocumentation</code> instance for a model element.  If
 * the entity does not declare its own documentation, a search algorithm will attempt to
 * locate an appropriate alternative documentation item (e.g. from the simple type assigned
 * to an attribute).
 * 
 * @author S. Livezey
 */
public class DocumentationFinder {
	
	/**
	 * Returns an appropriate documentation item for the given entity.  If no documentation
	 * can be located, this method will return null.
	 * 
	 * @param entity  the entity for which to return a documentation item
	 * @return TLDocumentation
	 */
	public static TLDocumentation getDocumentation(TLDocumentationOwner entity) {
		return getDocumentation( entity, new HashSet<String>() );
	}
	
	/**
	 * Recursive method used to search for documentation.  This method includes protection
	 * from infinite loops due to circular references.
	 * 
	 * @param entity  the entity for which to return a documentation item
	 * @param visitedEntities  the list of visited entity names
	 * @return TLDocumentation
	 */
	private static TLDocumentation getDocumentation(TLDocumentationOwner entity, Set<String> visitedEntities) {
		TLDocumentation doc = entity.getDocumentation();
		
		// Not found if documentation is null or empty
		if ((doc != null) && doc.isEmpty()) {
			doc = null;
		}
		
		if (doc == null) {
			TLDocumentationOwner nextEntity = null;
			
			if (entity instanceof TLSimple) {
				TLAttributeType parentType = ((TLSimple) entity).getParentType();
				
				if (parentType instanceof TLDocumentationOwner) {
					nextEntity = (TLDocumentationOwner) parentType;
				}
				
			} else if (entity instanceof TLAbstractEnumeration) {
				TLAbstractEnumeration enumeration = (TLAbstractEnumeration) entity;
				
				if (enumeration.getExtension() instanceof TLDocumentationOwner) {
					nextEntity = (TLDocumentationOwner) enumeration.getExtension().getExtendsEntity();
				}
				
			} else if (entity instanceof TLValueWithAttributes) {
				TLAttributeType parentType = ((TLValueWithAttributes) entity).getParentType();
				
				if (parentType instanceof TLDocumentationOwner) {
					nextEntity = (TLDocumentationOwner) parentType;
				}
				
			} else if (entity instanceof TLComplexTypeBase) { // cores and BO's
				TLComplexTypeBase coreOrBO = (TLComplexTypeBase) entity;
				
				if (coreOrBO.getExtension() instanceof TLDocumentationOwner) {
					nextEntity = (TLDocumentationOwner) coreOrBO.getExtension().getExtendsEntity();
				}
				
			} else if (entity instanceof TLOperation) {
				TLOperation operation = (TLOperation) entity;
				
				if (operation.getExtension() instanceof TLDocumentationOwner) {
					nextEntity = (TLDocumentationOwner) operation.getExtension().getExtendsEntity();
				}
				
			} else if (entity instanceof TLFacet) {
				nextEntity = (TLDocumentationOwner) ((TLFacet) entity).getOwningEntity();
				
			} else if (entity instanceof TLListFacet) {
				nextEntity = (TLDocumentationOwner) ((TLListFacet) entity).getItemFacet();
				
			} else if (entity instanceof TLSimpleFacet) {
				NamedEntity simpleType = ((TLSimpleFacet) entity).getSimpleType();
				
				if (simpleType instanceof TLDocumentationOwner) {
					nextEntity = (TLDocumentationOwner) simpleType;
				}
				
			} else if (entity instanceof TLAttribute) {
				NamedEntity attrType = ((TLAttribute) entity).getType();
				
				if (attrType instanceof TLDocumentationOwner) {
					nextEntity = (TLDocumentationOwner) attrType;
				}
				
			} else if (entity instanceof TLProperty) {
				NamedEntity elementType = ((TLProperty) entity).getType();
				
				if (elementType instanceof TLDocumentationOwner) {
					nextEntity = (TLDocumentationOwner) elementType;
				}
			}
			
			if (nextEntity != null) {
				String entityId = getEntityId( nextEntity );
				
				if (!visitedEntities.contains( entityId )) {
					visitedEntities.add( entityId );
					doc = getDocumentation( nextEntity, visitedEntities );
				}
			}
		}
		return doc;
	}
	
	/**
	 * Returns a unique ID for the given model entity.
	 * 
	 * @param entity  the entity for which to return a unique ID
	 * @return String
	 */
	private static String getEntityId(TLDocumentationOwner entity) {
		StringBuilder entityId = new StringBuilder();
		
		if (entity instanceof NamedEntity) {
			NamedEntity e = (NamedEntity) entity;
			entityId.append(e.getNamespace()).append(":").append(e.getLocalName());
			
		} else if (entity instanceof TLAttribute) {
			TLAttribute e = (TLAttribute) entity;
			entityId.append(getEntityId(
					(TLDocumentationOwner) e.getAttributeOwner())).append(":").append(e.getName());
			
		} else if (entity instanceof TLProperty) {
			TLProperty e = (TLProperty) entity;
			entityId.append(getEntityId(
					(TLDocumentationOwner) e.getPropertyOwner())).append(":").append(e.getName());
			
		} else {
			entityId.append("unknown");
		}
		return entityId.toString();
	}
	
}
