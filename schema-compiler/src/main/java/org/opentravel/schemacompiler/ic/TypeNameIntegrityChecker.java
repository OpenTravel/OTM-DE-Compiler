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
package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.transform.util.ChameleonFilter;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;
import org.opentravel.schemacompiler.util.ClassSpecificAssignment;
import org.opentravel.schemacompiler.util.SchemaCompilerRuntimeException;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;

/**
 * Model integrity check listener, that ensures all 'typeName' fields are synchronized with the type instances that are
 * assigned to the referencing model object.
 * 
 * @author S. Livezey
 */
public class TypeNameIntegrityChecker
		extends AbstractIntegrityChecker<ValueChangeEvent<TLModelElement,TLModelElement>,TLModelElement> {
	
	private static ClassSpecificAssignment<String> assignTypeNameFunction = new ClassSpecificAssignment<String>()
		.addAssignment( TLSimple.class, (s, v) -> s.setParentTypeName( v ) )
		.addAssignment( TLValueWithAttributes.class, (s, v) -> s.setParentTypeName( v ) )
		.addAssignment( TLSimpleFacet.class, (s, v) -> s.setSimpleTypeName( v ) )
		.addAssignment( TLProperty.class, (s, v) -> s.setTypeName( v ) )
		.addAssignment( TLAttribute.class, (s, v) -> s.setTypeName( v ) )
		.addAssignment( TLExtension.class, (s, v) -> s.setExtendsEntityName( v ) )
		.addAssignment( TLContextualFacet.class, (s, v) -> s.setOwningEntityName( v ) )
		.addAssignment( TLResource.class, (s, v) -> s.setBusinessObjectRefName( v ) )
		.addAssignment( TLResourceParentRef.class, (s, v) -> s.setParentResourceName( v ) )
		.addAssignment( TLParamGroup.class, (s, v) -> s.setFacetRefName( v ) )
		.addAssignment( TLActionFacet.class, (s, v) -> s.setBasePayloadName( v ) )
		.addAssignment( TLActionRequest.class, (s, v) -> s.setPayloadTypeName( v ) )
		.addAssignment( TLActionResponse.class, (s, v) -> s.setPayloadTypeName( v ) );
	
	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(ValueChangeEvent<TLModelElement,TLModelElement> event) {
		TLModelElement sourceObject = event.getSource();
		
		if ((event.getType() == ModelEventType.TYPE_ASSIGNMENT_MODIFIED)
				|| (event.getType() == ModelEventType.EXTENDS_ENTITY_MODIFIED)
				|| (event.getType() == ModelEventType.FACET_OWNER_MODIFIED)
				|| (event.getType() == ModelEventType.BO_REFERENCE_MODIFIED)
				|| (event.getType() == ModelEventType.PARENT_RESOURCE_MODIFIED)
				|| (event.getType() == ModelEventType.BASE_PAYLOAD_MODIFIED)
				|| (event.getType() == ModelEventType.FACET_REF_MODIFIED)
				|| (event.getType() == ModelEventType.PAYLOAD_TYPE_MODIFIED)) {
			String entityName = buildEntityName( (NamedEntity) event.getNewValue(), sourceObject );
			
			if (assignTypeNameFunction.canApply( sourceObject )) {
				assignTypeNameFunction.apply( sourceObject, entityName );
				
			} else {
				String className = (sourceObject == null) ? "[NULL]" : sourceObject.getClass().getName();
				
				throw new SchemaCompilerRuntimeException(
						"No type name assignment function defined for class: " + className );
			}
			
		} else if ((event.getType() == ModelEventType.PARAM_GROUP_MODIFIED)
				&& (event.getNewValue() instanceof TLParamGroup) && (sourceObject instanceof TLActionRequest)) {
			// TLParamGroup is the only entity reference we need to handle that is not
			// a named entity.
			((TLActionRequest) sourceObject).setParamGroupName( ((TLParamGroup) event.getNewValue()).getName() );
		}
	}
	
	/**
	 * Returns the name of the given entity as either 'prefix:localName' or simple 'localName' (if the entity is
	 * assigned to the local namespace provided).
	 * 
	 * @param assignedEntity the entity whose name is to be returned
	 * @param sourceObject the object to which the named entity was assigned
	 * @return String
	 */
	private String buildEntityName(NamedEntity assignedEntity, TLModelElement sourceObject) {
		String entityName = null;
		
		if (assignedEntity != null) {
			AbstractLibrary owningLibrary = getOwningLibrary( sourceObject );
			SymbolResolver symbolResolver = new TLModelSymbolResolver( sourceObject.getOwningModel() );
			
			symbolResolver.setPrefixResolver( new LibraryPrefixResolver( owningLibrary ) );
			symbolResolver.setAnonymousEntityFilter( new ChameleonFilter( owningLibrary ) );
			entityName = symbolResolver.buildEntityName( assignedEntity.getNamespace(), assignedEntity.getLocalName() );
		}
		return entityName;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#getEventClass()
	 */
	@Override
	public Class<?> getEventClass() {
		return ValueChangeEvent.class;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#getSourceObjectClass()
	 */
	@Override
	public Class<TLModelElement> getSourceObjectClass() {
		return TLModelElement.class;
	}
	
}
