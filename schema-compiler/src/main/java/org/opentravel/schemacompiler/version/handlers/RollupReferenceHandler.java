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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Handles the adjustments of attribute and element assignments when types are rolled
 * up to new minor and major versions.
 * 
 * @author S. Livezey
 */
public class RollupReferenceHandler {
	
    private RollupReferenceInfo rollupReferences;
    
    /**
     * Constructor that specifies the collection(s) of libraries that are being rolled up as
     * part of the current operation.
     * 
     * @param rollupLibraries  the collection(s) of libraries being rolled up
     */
    @SafeVarargs
	public RollupReferenceHandler(Collection<TLLibrary>... rollupLibraries) {
    	rollupReferences = new RollupReferenceInfo( rollupLibraries );
    }

    /**
     * Constructor that specifies a single library that is being rolled up as part of the
     * current operation.
     * 
     * @param rollupLibrary  the library being rolled up
     */
    @SafeVarargs
	public RollupReferenceHandler(TLLibrary rollupLibrary, Collection<TLLibrary>... rollupLibraries) {
    	rollupReferences = new RollupReferenceInfo( rollupLibrary, rollupLibraries );
    }

    /**
     * Captures model references to the given entity so that they may be adjusted
     * when that entity is "rolled up" to a higer level major or minor version library.
     * 
     * @param entity  the entity for which to capture roll-up references
     */
    public void captureRollupReferences(LibraryElement entity) {
        if (entity == null) {
            return; // simple case - nothing to capture
        }
        if (entity instanceof TLAttribute) {
            TLAttribute attribute = (TLAttribute) entity;

            if ((attribute.getType() != null)
                    && rollupReferences.isRollupLibrary(attribute.getType().getOwningLibrary())) {
                rollupReferences.addReference(attribute, (TLModelElement) attribute.getType());
            }

        } else if (entity instanceof TLProperty) {
            TLProperty element = (TLProperty) entity;

            if ((element.getType() != null)
                    && rollupReferences.isRollupLibrary( element.getType().getOwningLibrary() )) {
                rollupReferences.addReference( element, (TLModelElement) element.getType() );
            }

        } else if (entity instanceof TLExtension) {
            TLExtension extension = (TLExtension) entity;

            if ((extension.getExtendsEntity() != null)
                    && rollupReferences.isRollupLibrary( extension.getExtendsEntity().getOwningLibrary() )) {
                rollupReferences.addReference( extension, (TLModelElement) extension.getExtendsEntity() );
            }

        } else if (entity instanceof TLValueWithAttributes) {
            TLValueWithAttributes vwa = (TLValueWithAttributes) entity;

            if ((vwa.getParentType() != null)
                    && rollupReferences.isRollupLibrary( vwa.getParentType().getOwningLibrary() )) {
                rollupReferences.addReference( vwa, (TLModelElement) vwa.getParentType() );
            }
            for (TLAttribute newAttribute : vwa.getAttributes()) {
            	captureRollupReferences( vwa.getAttribute( newAttribute.getName() ) );
            }

        } else if (entity instanceof TLSimple) {
            TLSimple simple = (TLSimple) entity;

            if ((simple.getParentType() != null)
                    && rollupReferences.isRollupLibrary( simple.getParentType().getOwningLibrary() )) {
                rollupReferences.addReference( simple, (TLModelElement) simple.getParentType() );
            }

        } else if (entity instanceof TLSimpleFacet) {
            TLSimpleFacet simpleFacet = (TLSimpleFacet) entity;

            if ((simpleFacet.getSimpleType() != null)
                    && rollupReferences.isRollupLibrary( simpleFacet.getSimpleType().getOwningLibrary() )) {
                rollupReferences.addReference( simpleFacet, (TLModelElement) simpleFacet.getSimpleType() );
            }

        } else if (entity instanceof TLBusinessObject) {
            TLBusinessObject bo = (TLBusinessObject) entity;

            captureRollupReferences( bo.getExtension() );
            captureRollupReferences( bo.getIdFacet() );
            captureRollupReferences( bo.getSummaryFacet() );
            captureRollupReferences( bo.getDetailFacet() );

            for (TLFacet newFacet : bo.getCustomFacets()) {
            	captureRollupReferences( newFacet );
            }
            for (TLFacet newFacet : bo.getQueryFacets()) {
            	captureRollupReferences( newFacet );
            }

        } else if (entity instanceof TLCoreObject) {
            TLCoreObject core = (TLCoreObject) entity;

            captureRollupReferences( core.getExtension() );
            captureRollupReferences( core.getSimpleFacet() );
            captureRollupReferences( core.getSummaryFacet() );
            captureRollupReferences( core.getDetailFacet() );

        } else if (entity instanceof TLChoiceObject) {
        	TLChoiceObject choice = (TLChoiceObject) entity;

            captureRollupReferences( choice.getExtension() );
            captureRollupReferences( choice.getSharedFacet() );

            for (TLFacet newFacet : choice.getChoiceFacets()) {
            	captureRollupReferences( newFacet );
            }
            
        } else if (entity instanceof TLOperation) {
            TLOperation op = (TLOperation) entity;

            captureRollupReferences( op.getExtension() );
            captureRollupReferences( op.getRequest() );
            captureRollupReferences( op.getResponse() );
            captureRollupReferences( op.getNotification() );

        } else if (entity instanceof TLFacet) {
            TLFacet facet = (TLFacet) entity;

            for (TLAttribute newAttribute : facet.getAttributes()) {
            	captureRollupReferences( newAttribute );
            }
            for (TLProperty newElement : facet.getElements()) {
            	captureRollupReferences( newElement );
            }
            
        } else if (entity instanceof TLResource) {
        	TLResource resource = (TLResource) entity;
            
            if ((resource.getBusinessObjectRef() != null)
                    && rollupReferences.isRollupLibrary( resource.getBusinessObjectRef().getOwningLibrary() )) {
                rollupReferences.addReference( resource, resource.getBusinessObjectRef() );
            }
            
            for (TLParamGroup paramGroup : resource.getParamGroups()) {
            	captureRollupReferences( paramGroup );
            }
            for (TLActionFacet facet : resource.getActionFacets()) {
            	captureRollupReferences( facet );
            }
            for (TLAction action : resource.getActions()) {
            	captureRollupReferences( action );
            }
            
        } else if (entity instanceof TLResourceParentRef) {
        	TLResourceParentRef parentRef = (TLResourceParentRef) entity;
            
            if ((parentRef.getParentParamGroup() != null)
                    && rollupReferences.isRollupLibrary( parentRef.getParentParamGroup().getOwningLibrary() )) {
                rollupReferences.addReference( parentRef, parentRef.getParentParamGroup() );
            }
            if ((parentRef.getParentResource() != null)
                    && rollupReferences.isRollupLibrary( parentRef.getParentResource().getOwningLibrary() )) {
                rollupReferences.addReference( parentRef, parentRef.getParentResource() );
            }
            
        } else if (entity instanceof TLParamGroup) {
        	TLParamGroup paramGroup = (TLParamGroup) entity;
            
            if ((paramGroup.getFacetRef() != null)
                    && rollupReferences.isRollupLibrary( paramGroup.getFacetRef().getOwningLibrary() )) {
                rollupReferences.addReference( paramGroup, paramGroup.getFacetRef() );
            }
            
            for (TLParameter parameter : paramGroup.getParameters()) {
            	captureRollupReferences( parameter );
            }
        	
        } else if (entity instanceof TLParameter) {
        	TLParameter parameter = (TLParameter) entity;
            
            if ((parameter.getFieldRef() != null)
                    && rollupReferences.isRollupLibrary( ((LibraryElement) parameter.getFieldRef()).getOwningLibrary() )) {
                rollupReferences.addReference( parameter, (TLModelElement) parameter.getFieldRef() );
            }
            
        } else if (entity instanceof TLAction) {
        	TLAction action = (TLAction) entity;
            
        	captureRollupReferences( action.getRequest() );
        	
            for (TLActionResponse response : action.getResponses()) {
            	captureRollupReferences( response );
            }
            
        } else if (entity instanceof TLActionRequest) {
        	TLActionRequest request = (TLActionRequest) entity;
            
            if ((request.getParamGroup() != null)
                    && rollupReferences.isRollupLibrary( request.getParamGroup().getOwningLibrary() )) {
                rollupReferences.addReference( request, request.getParamGroup() );
            }
            if ((request.getPayloadType() != null)
                    && rollupReferences.isRollupLibrary( request.getPayloadType().getOwningLibrary() )) {
                rollupReferences.addReference( request, request.getPayloadType() );
            }
            
        } else if (entity instanceof TLActionResponse) {
        	TLActionResponse response = (TLActionResponse) entity;
        	
            if ((response.getPayloadType() != null)
                    && rollupReferences.isRollupLibrary( response.getPayloadType().getOwningLibrary() )) {
                rollupReferences.addReference( response, response.getPayloadType() );
            }
        }
    }
    
    /**
     * If any same-library references have been captured by this handler, those references are
     * adjusted to same-named entities in the new owning library (assuming a same-name match can
     * be found).
     * 
     * @param newLibrary  the new library whose internal references are to be adjusted
     */
    public void adjustSameLibraryReferences(TLLibrary newLibrary) {
        ModelNavigator.navigate( newLibrary,
        		new RollupReferenceAdjustmentVisitor( newLibrary ) );
    }

    /**
     * Encapsulates all of the information needed to identify and process entity references within
     * the same set of libraries that are being rolled up.
     */
    private class RollupReferenceInfo {

        private Map<LibraryElement,List<Object>> rollupReferences = new HashMap<>();
        private Set<TLLibrary> rollupLibraries = new HashSet<>();

        /**
         * Constructor that specifies the collection(s) of libraries that are being rolled up as
         * part of the current operation.
         * 
         * @param rollupLibraries  the collection(s) of libraries being rolled up
         */
        @SafeVarargs
		public RollupReferenceInfo(Collection<TLLibrary>... rollupLibraries) {
        	this( null, rollupLibraries );
        }

        /**
         * Constructor that specifies a single library that is being rolled up as part of the
         * current operation.
         * 
         * @param rollupLibrary  the library being rolled up
         */
        @SafeVarargs
		public RollupReferenceInfo(TLLibrary rollupLibrary, Collection<TLLibrary>... rollupLibraries) {
            for (Collection<TLLibrary> libraries : rollupLibraries) {
                if (libraries != null) {
                    this.rollupLibraries.addAll( libraries );
                }
            }
            
            if (rollupLibrary != null) {
                this.rollupLibraries.add( rollupLibrary );
            }
        }
        
        /**
         * Adds the given reference to the current collection of rollup references.
         * 
         * @param referencingEntity  the entity that holds the reference
         * @param referencedEntity  the entity that is being referenced
         */
        public void addReference(LibraryElement referencingEntity, Object referencedEntity) {
        	if (referencedEntity != null) {
            	List<Object> referencedEntities = rollupReferences.get(referencingEntity);
            	
            	if (referencedEntities == null) {
            		referencedEntities = new ArrayList<>();
            		rollupReferences.put(referencingEntity, referencedEntities);
            	}
            	referencedEntities.add(referencedEntity);
        	}
        }
        
        /**
         * Returns true if the given entity has rollup references registered.
         * 
         * @param referencingEntity  the referencing entity to check
         * @return boolean
         */
        public boolean hasReference(LibraryElement referencingEntity) {
        	return rollupReferences.containsKey(referencingEntity);
        }
        
        /**
         * Returns the entity referenced by the one provided.  If multiple references exist, this
         * method will search for the one that matches the specified type.
         * 
         * @param referencingEntity  the entity that holds the original reference
         * @param referencedType  the required type for any reference that is returned
         * @return TLModelElement
         */
        @SuppressWarnings("unchecked")
		public <T> T getReference(LibraryElement referencingEntity, Class<T> referencedType) {
        	List<Object> referencedEntities = rollupReferences.get(referencingEntity);
        	T referencedEntity = null;
        	
        	if (referencedEntities != null) {
        		for (Object entity : referencedEntities) {
        			if (referencedType.isAssignableFrom(entity.getClass())) {
        				referencedEntity = (T) entity;
        			}
        		}
        	}
        	return referencedEntity;
        }
        
        /**
         * Returns true if the given library is a member of the rollup collection currently being
         * processed.
         * 
         * @param library
         *            the library to analyze
         * @return boolean
         */
        public boolean isRollupLibrary(AbstractLibrary library) {
            return rollupLibraries.contains(library);
        }

    }

    /**
     * Visitor that adjusts references to same-named entities in the new owning library if a
     * same-name match can be found.
     */
    private class RollupReferenceAdjustmentVisitor extends ModelElementVisitorAdapter {

        private SymbolTable symbols;

        /**
         * Constructor that assigns the map of roll-up references that were captured during the
         * entity cloning process.
         * 
         * @param newLibrary  the new library that owns all references to be adjusted
         */
        public RollupReferenceAdjustmentVisitor(TLLibrary newLibrary) {
            this.symbols = SymbolTableFactory.newSymbolTableFromEntity( newLibrary );
        }

        /**
         * Searches the given library for an named entity member with the same name as the original
         * entity provided.
         * 
         * @param originalEntity  the original entity whose local name should be used in the search
         * @param library  the library to search
         * @return NamedEntity
         */
        @SuppressWarnings("unchecked")
        private <T extends NamedEntity> T findSameNameEntity(T originalEntity, TLLibrary library) {
            Object entity = symbols.getEntity( library.getNamespace(), originalEntity.getLocalName() );
            T sameNameEntity = null;

            if (originalEntity.getClass().equals(entity.getClass())) {
				T namedEntity = (T) entity;

                if (namedEntity.getOwningLibrary() == library) {
                    sameNameEntity = namedEntity;
                }
            }
            return sameNameEntity;
        }

        /**
         * Searches the given library for a field with the same owner and field name as the original
         * field provided.
         * 
         * @param originalField  the original field whose name and owner should be used in the search
         * @param library  the library to search
         * @return TLMemberField<?>
         */
        private TLMemberField<?> findSameNameField(TLMemberField<?> originalField, TLLibrary library) {
        	NamedEntity originalOwner = (NamedEntity) originalField.getOwner();
        	NamedEntity sameNameOwner = findSameNameEntity(originalOwner, library);
    		String fieldName = originalField.getName();
        	TLMemberField<?> sameNameField = null;
        	
        	if ((sameNameOwner != null) && (fieldName != null)) {
        		List<? extends TLMemberField<?>> inheritedMembers = null;
        		
        		if (originalField instanceof TLAttribute) {
        			if (sameNameOwner instanceof TLFacet) {
        				inheritedMembers = PropertyCodegenUtils.getInheritedFacetAttributes((TLFacet) sameNameOwner);
        				
        			} else if (sameNameOwner instanceof TLValueWithAttributes) {
        				inheritedMembers = PropertyCodegenUtils.getInheritedAttributes((TLValueWithAttributes) sameNameOwner);
        			}
        			
        		} else if (originalField instanceof TLProperty) {
        			if (sameNameOwner instanceof TLFacet) {
        				inheritedMembers = PropertyCodegenUtils.getInheritedFacetProperties((TLFacet) sameNameOwner);
        			}
        			
        		} else if (originalField instanceof TLIndicator) {
        			if (sameNameOwner instanceof TLFacet) {
        				inheritedMembers = PropertyCodegenUtils.getInheritedFacetIndicators((TLFacet) sameNameOwner);
        				
        			} else if (sameNameOwner instanceof TLValueWithAttributes) {
        				inheritedMembers = PropertyCodegenUtils.getInheritedIndicators((TLValueWithAttributes) sameNameOwner);
        			}
        		}
        		
        		if (inheritedMembers != null) {
        			for (TLMemberField<?> memberField : inheritedMembers) {
        				if (fieldName.equals(memberField.getName())) {
        					sameNameField = memberField;
        					break;
        				}
        			}
        		}
        	}
        	return sameNameField;
        }
        
        /**
         * Searches the given library for a parameter group with the same owner and field name as the original
         * group provided.
         * 
         * @param originalParamGroup  the original parameter group whose name and owner should be used in the search
         * @param library  the library to search
         * @return TLParamGroup
         */
        private TLParamGroup findSameNameParamGroup(TLParamGroup originalParamGroup, TLLibrary library) {
        	TLResource originalOwner = originalParamGroup.getOwner();
        	NamedEntity sameNameOwner = findSameNameEntity(originalOwner, library);
    		String groupName = originalParamGroup.getName();
    		TLParamGroup sameNameParamGroup = null;
        	
        	if ((sameNameOwner instanceof TLResource) && (groupName != null)) {
        		for (TLParamGroup paramGroup : ((TLResource) sameNameOwner).getParamGroups()) {
        			if (groupName.equals(paramGroup.getName())) {
        				sameNameParamGroup = paramGroup;
        				break;
        			}
        		}
        	}
        	return sameNameParamGroup;
        }
        
        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
            if (rollupReferences.hasReference( simple )) {
            	TLAttributeType sameNameEntity = findSameNameEntity(
                		rollupReferences.getReference( simple, TLAttributeType.class ),
                		(TLLibrary) simple.getOwningLibrary() );

                if (sameNameEntity != null) {
                    simple.setParentType( sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes vwa) {
            if (rollupReferences.hasReference( vwa )) {
            	TLAttributeType sameNameEntity = findSameNameEntity(
                		rollupReferences.getReference( vwa, TLAttributeType.class ),
                		(TLLibrary) vwa.getOwningLibrary() );

                if (sameNameEntity != null) {
                    vwa.setParentType( sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            if (rollupReferences.hasReference( simpleFacet )) {
            	TLAttributeType sameNameEntity = findSameNameEntity(
                		rollupReferences.getReference( simpleFacet, TLAttributeType.class ),
                		(TLLibrary) simpleFacet.getOwningLibrary() );

                if (sameNameEntity != null) {
                    simpleFacet.setSimpleType( sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
         */
        @Override
        public boolean visitAttribute(TLAttribute attribute) {
            if (rollupReferences.hasReference( attribute )) {
            	TLAttributeType sameNameEntity = findSameNameEntity(
                		rollupReferences.getReference( attribute, TLAttributeType.class ),
                		(TLLibrary) attribute.getOwningLibrary() );

                if (sameNameEntity != null) {
                    attribute.setType( sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            if (rollupReferences.hasReference( element )) {
            	TLPropertyType sameNameEntity = findSameNameEntity(
                		rollupReferences.getReference( element, TLPropertyType.class ),
                		(TLLibrary) element.getOwningLibrary() );

                if (sameNameEntity != null) {
                    element.setType( sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
         */
        @Override
        public boolean visitExtension(TLExtension extension) {
            if (rollupReferences.hasReference( extension )) {
                NamedEntity sameNameEntity = findSameNameEntity(
                		rollupReferences.getReference( extension, NamedEntity.class ),
                		(TLLibrary) extension.getOwningLibrary() );

                if (sameNameEntity != null) {
                    extension.setExtendsEntity( sameNameEntity );
                }
            }
            return true;
        }

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
		 */
		@Override
		public boolean visitResource(TLResource resource) {
            if (rollupReferences.hasReference( resource )) {
            	TLBusinessObject sameNameEntity = findSameNameEntity(
                		rollupReferences.getReference( resource, TLBusinessObject.class ),
                		(TLLibrary) resource.getOwningLibrary() );

                if (sameNameEntity != null) {
                	resource.setBusinessObjectRef( sameNameEntity );
                }
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
		 */
		@Override
		public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
            if (rollupReferences.hasReference( parentRef )) {
                TLParamGroup sameNameParamGroup = findSameNameParamGroup(
                		rollupReferences.getReference( parentRef, TLParamGroup.class ),
                		(TLLibrary) parentRef.getOwningLibrary() );
                TLResource sameNameResource = findSameNameEntity(
                		rollupReferences.getReference( parentRef, TLResource.class ),
                		(TLLibrary) parentRef.getOwningLibrary() );

                if (sameNameParamGroup != null) {
                	parentRef.setParentParamGroup( sameNameParamGroup );
                }
                if (sameNameResource != null) {
                	parentRef.setParentResource( sameNameResource );
                }
            }
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
		 */
		@Override
		public boolean visitParamGroup(TLParamGroup paramGroup) {
            if (rollupReferences.hasReference( paramGroup )) {
            	TLFacet sameNameEntity = findSameNameEntity(
                		rollupReferences.getReference( paramGroup, TLFacet.class ),
                		(TLLibrary) paramGroup.getOwningLibrary() );

                if (sameNameEntity != null) {
                	paramGroup.setFacetRef( sameNameEntity );
                }
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParameter(org.opentravel.schemacompiler.model.TLParameter)
		 */
		@Override
		public boolean visitParameter(TLParameter parameter) {
            if (rollupReferences.hasReference( parameter )) {
                TLMemberField<?> sameNameField = findSameNameField(
                		rollupReferences.getReference( parameter, TLMemberField.class ),
                		(TLLibrary) parameter.getOwningLibrary() );

                if (sameNameField != null) {
                	parameter.setFieldRef( sameNameField );
                }
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionRequest(org.opentravel.schemacompiler.model.TLActionRequest)
		 */
		@Override
		public boolean visitActionRequest(TLActionRequest actionRequest) {
            if (rollupReferences.hasReference( actionRequest )) {
                TLParamGroup sameNameParamGroup = findSameNameParamGroup(
                		rollupReferences.getReference( actionRequest, TLParamGroup.class ),
                		(TLLibrary) actionRequest.getOwningLibrary() );
                TLActionFacet sameNamePayloadType = findSameNameEntity(
                		rollupReferences.getReference( actionRequest, TLActionFacet.class ),
                		(TLLibrary) actionRequest.getOwningLibrary() );

                if (sameNameParamGroup != null) {
                	actionRequest.setParamGroup( sameNameParamGroup );
                }
                if (sameNamePayloadType != null) {
                	actionRequest.setPayloadType( sameNamePayloadType );
                }
            }
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
		 */
		@Override
		public boolean visitActionResponse(TLActionResponse actionResponse) {
            if (rollupReferences.hasReference( actionResponse )) {
                TLActionFacet sameNamePayloadType = findSameNameEntity(
                		rollupReferences.getReference( actionResponse, TLActionFacet.class ),
                		(TLLibrary) actionResponse.getOwningLibrary() );

                if (sameNamePayloadType != null) {
                	actionResponse.setPayloadType( sameNamePayloadType );
                }
            }
			return true;
		}

    }

}
