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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
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
                rollupReferences.getReferences().put(attribute, attribute.getType());
            }

        } else if (entity instanceof TLProperty) {
            TLProperty element = (TLProperty) entity;

            if ((element.getType() != null)
                    && rollupReferences.isRollupLibrary( element.getType().getOwningLibrary() )) {
                rollupReferences.getReferences().put( element, element.getType() );
            }

        } else if (entity instanceof TLExtension) {
            TLExtension extension = (TLExtension) entity;

            if ((extension.getExtendsEntity() != null)
                    && rollupReferences.isRollupLibrary( extension.getExtendsEntity().getOwningLibrary() )) {
                rollupReferences.getReferences().put( extension, extension.getExtendsEntity() );
            }

        } else if (entity instanceof TLValueWithAttributes) {
            TLValueWithAttributes vwa = (TLValueWithAttributes) entity;

            if ((vwa.getParentType() != null)
                    && rollupReferences.isRollupLibrary( vwa.getParentType().getOwningLibrary() )) {
                rollupReferences.getReferences().put( vwa, vwa.getParentType() );
            }
            for (TLAttribute newAttribute : vwa.getAttributes()) {
            	captureRollupReferences( vwa.getAttribute( newAttribute.getName() ) );
            }

        } else if (entity instanceof TLSimple) {
            TLSimple simple = (TLSimple) entity;

            if ((simple.getParentType() != null)
                    && rollupReferences.isRollupLibrary( simple.getParentType().getOwningLibrary() )) {
                rollupReferences.getReferences().put( simple, simple.getParentType() );
            }

        } else if (entity instanceof TLSimpleFacet) {
            TLSimpleFacet simpleFacet = (TLSimpleFacet) entity;

            if ((simpleFacet.getSimpleType() != null)
                    && rollupReferences.isRollupLibrary( simpleFacet.getSimpleType().getOwningLibrary() )) {
                rollupReferences.getReferences().put( simpleFacet, simpleFacet.getSimpleType() );
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

        private Map<LibraryElement, NamedEntity> rollupReferences = new HashMap<>();
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
         * Returns the map of references that have been detected to entities contained within one of
         * the 'rollupLibraries' members.
         * 
         * @return Map<LibraryElement,NamedEntity>
         */
        public Map<LibraryElement, NamedEntity> getReferences() {
            return rollupReferences;
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
        private NamedEntity findSameNameEntity(NamedEntity originalEntity, TLLibrary library) {
            Object entity = symbols.getEntity( library.getNamespace(), originalEntity.getLocalName() );
            NamedEntity sameNameEntity = null;

            if (entity instanceof NamedEntity) {
                NamedEntity namedEntity = (NamedEntity) entity;

                if (namedEntity.getOwningLibrary() == library) {
                    sameNameEntity = namedEntity;
                }
            }
            return sameNameEntity;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
            if (rollupReferences.getReferences().containsKey( simple )) {
                NamedEntity sameNameEntity = findSameNameEntity(
                		rollupReferences.getReferences().get( simple ),
                		(TLLibrary) simple.getOwningLibrary() );

                if (sameNameEntity instanceof TLAttributeType) {
                    simple.setParentType( (TLAttributeType) sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes vwa) {
            if (rollupReferences.getReferences().containsKey( vwa )) {
                NamedEntity sameNameEntity = findSameNameEntity(
                		rollupReferences.getReferences().get( vwa ),
                		(TLLibrary) vwa.getOwningLibrary() );

                if (sameNameEntity instanceof TLAttributeType) {
                    vwa.setParentType( (TLAttributeType) sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            if (rollupReferences.getReferences().containsKey( simpleFacet )) {
                NamedEntity sameNameEntity = findSameNameEntity(
                		rollupReferences.getReferences().get(simpleFacet),
                		(TLLibrary) simpleFacet.getOwningLibrary() );

                if (sameNameEntity instanceof TLAttributeType) {
                    simpleFacet.setSimpleType( (TLAttributeType) sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
         */
        @Override
        public boolean visitAttribute(TLAttribute attribute) {
            if (rollupReferences.getReferences().containsKey( attribute )) {
                NamedEntity sameNameEntity = findSameNameEntity(
                		rollupReferences.getReferences().get(attribute),
                		(TLLibrary) attribute.getOwningLibrary() );

                if (sameNameEntity instanceof TLAttributeType) {
                    attribute.setType( (TLAttributeType) sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            if (rollupReferences.getReferences().containsKey( element )) {
                NamedEntity sameNameEntity = findSameNameEntity(
                		rollupReferences.getReferences().get(element),
                		(TLLibrary) element.getOwningLibrary() );

                if (sameNameEntity instanceof TLPropertyType) {
                    element.setType( (TLPropertyType) sameNameEntity );
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
         */
        @Override
        public boolean visitExtension(TLExtension extension) {
            if (rollupReferences.getReferences().containsKey( extension )) {
                NamedEntity sameNameEntity = findSameNameEntity(
                		rollupReferences.getReferences().get(extension),
                		(TLLibrary) extension.getOwningLibrary() );

                if (sameNameEntity != null) {
                    extension.setExtendsEntity( sameNameEntity );
                }
            }
            return true;
        }

    }

}
