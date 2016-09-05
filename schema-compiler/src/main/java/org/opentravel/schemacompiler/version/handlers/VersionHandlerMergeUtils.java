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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.EnumCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.util.ModelElementCloner;

/**
 * Utility methods used to merge the contents of various types of versioned entities.
 * 
 * @author S. Livezey
 */
public class VersionHandlerMergeUtils {
	
	private VersionHandlerFactory factory;
	
	/**
	 * Returns the factory associated with the current merge operation.
	 * 
	 * @param factory  the version handler factory instance
	 */
	public VersionHandlerMergeUtils(VersionHandlerFactory factory) {
		this.factory = factory;
	}
	
    /**
     * Merges the contents of the given list into the specified target entity. If any attributes
     * with the same name already exist in the target, the merge item(s) will be ignored.
     * 
     * @param target  the entity that will receive new attributes from the merge
     * @param attributesToMerge  the list of attributes to merge into the target
     * @param referenceHandler  handler that stores reference information for the libraries being rolled up
     */
    public void mergeAttributes(TLAttributeOwner target, List<TLAttribute> attributesToMerge,
    		RollupReferenceHandler referenceHandler) {
		ModelElementCloner cloner = getCloner( target );
        Set<String> existingAttributeNames = new HashSet<String>();
        List<TLAttribute> existingAttributes = null;

        if (target instanceof TLValueWithAttributes) {
            existingAttributes = PropertyCodegenUtils
                    .getInheritedAttributes((TLValueWithAttributes) target);
        } else if (target instanceof TLFacet) {
            existingAttributes = PropertyCodegenUtils.getInheritedAttributes((TLFacet) target);
        }
        if (existingAttributes != null) {
            for (TLAttribute attr : existingAttributes) {
                existingAttributeNames.add(attr.getName());
            }
        }

        for (TLAttribute sourceAttribute : attributesToMerge) {
            if (!existingAttributeNames.contains(sourceAttribute.getName())) {
                TLAttribute clone = cloner.clone(sourceAttribute);

                target.addAttribute(clone);
                referenceHandler.captureRollupReferences( clone );
            }
        }
    }

    /**
     * Merges the contents of the given list into the specified target entity. If any properties
     * with the same name already exist in the target (or, in the case of complex types, properties
     * from the same substitution group), the merge item(s) will be ignored.
     * 
     * @param target  the entity that will receive new properties from the merge
     * @param propertiesToMerge  the list of properties to merge into the target
     * @param referenceHandler  handler that stores reference information for the libraries being rolled up
     */
    public void mergeProperties(TLPropertyOwner target, List<TLProperty> propertiesToMerge,
    		RollupReferenceHandler referenceHandler) {
		ModelElementCloner cloner = getCloner( target );
        Set<NamedEntity> existingSubstitutionGroups = new HashSet<NamedEntity>();
        Set<String> existingPropertyNames = new HashSet<String>();

        if (target instanceof TLFacet) {
            List<TLProperty> existingProperties = PropertyCodegenUtils
                    .getInheritedProperties( (TLFacet) target );

            for (TLProperty property : existingProperties) {
                TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(
                		target, property.getType() );
                NamedEntity substitutionGroup = PropertyCodegenUtils
                        .getInheritanceRoot( propertyType );

                if (substitutionGroup != null) {
                    existingSubstitutionGroups.add( substitutionGroup );
                }
                if (property.getName() != null) {
                    existingPropertyNames.add( property.getName() );
                }
            }
        }

        for (TLProperty sourceProperty : propertiesToMerge) {
            if (!existingPropertyNames.contains( sourceProperty.getName() )) {
                TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(
                		target, sourceProperty.getType() );
                NamedEntity substitutionGroup = PropertyCodegenUtils
                        .getInheritanceRoot( propertyType );

                if (!existingSubstitutionGroups.contains( substitutionGroup )) {
                    TLProperty clone = cloner.clone( sourceProperty );

                    target.addElement(clone);
                    referenceHandler.captureRollupReferences( clone );
                }
            }
        }
    }

    /**
     * Merges the contents of the given list into the specified target entity. If any indicators
     * with the same name already exist in the target, the merge item(s) will be ignored.
     * 
     * @param target  the entity that will receive new indicators from the merge
     * @param indicatorsToMerge  the list of indicators to merge into the target
     */
    public void mergeIndicators(TLIndicatorOwner target, List<TLIndicator> indicatorsToMerge) {
		ModelElementCloner cloner = getCloner( target );
        Set<String> existingIndicatorNames = new HashSet<String>();
        List<TLIndicator> existingIndicators = null;

        if (target instanceof TLValueWithAttributes) {
            existingIndicators = PropertyCodegenUtils
                    .getInheritedIndicators( (TLValueWithAttributes) target );
            
        } else if (target instanceof TLFacet) {
            existingIndicators = PropertyCodegenUtils.getInheritedIndicators( (TLFacet) target );
        }
        
        if (existingIndicators != null) {
            for (TLIndicator indicator : existingIndicators) {
                existingIndicatorNames.add( indicator.getName() );
            }
        }

        for (TLIndicator sourceIndicator : indicatorsToMerge) {
            if (!existingIndicatorNames.contains(sourceIndicator.getName())) {
                target.addIndicator( cloner.clone( sourceIndicator ) );
            }
        }
    }

    /**
     * Merges the contents of each source facet into the corresponding target facet.
     * 
     * @param targetFacets  the collection of named target facets to which source content will be merged
     * @param sourceFacets  the collection of named source facets to be merged
     * @param referenceHandler  handler that stores reference information for the libraries being rolled up
     */
    public void mergeFacets(Map<String,TLFacet> targetFacets, Map<String,TLFacet> sourceFacets,
    		RollupReferenceHandler referenceHandler) {
    	ModelElementCloner cloner = null;
    	
        for (String facetIdentity : sourceFacets.keySet()) {
            TLFacet sourceFacet = sourceFacets.get( facetIdentity );
            TLFacet targetFacet = targetFacets.get( facetIdentity );
            
            if (targetFacet.getDocumentation() == null) {
                if (cloner == null) {
                	cloner = getCloner( targetFacet ); // Initialize the first time through
                }
                targetFacet.setDocumentation( cloner.clone( sourceFacet.getDocumentation() ) );
            }
            mergeAttributes( targetFacet, sourceFacet.getAttributes(), referenceHandler );
            mergeProperties( targetFacet, sourceFacet.getElements(), referenceHandler );
            mergeIndicators( targetFacet, sourceFacet.getIndicators() );
        }
    }
    
    /**
     * Adds the given facet to the identity map, using the full identity string of the facet as the
     * key.
     * 
     * @param facet  the facet instance to add
     * @param identityFacetMap  the map that will receive the facet
     */
    public void addToIdentityFacetMap(TLFacet facet, Map<String,TLFacet> identityFacetMap) {
        if (facet != null) {
        	if (facet instanceof TLContextualFacet) {
                identityFacetMap.put( facet.getFacetType().getIdentityName(
                		((TLContextualFacet) facet).getName() ), facet);
        		
        	} else {
                identityFacetMap.put( facet.getFacetType().getIdentityName(), facet);
        	}
        }
    }

    /**
     * Merges the contents of the given list into the specified target entity. If any enumerated
     * values with the same literal already exist in the target, the merge item(s) will be ignored.
     * 
     * @param target  the entity that will receive new enumerated values from the merge
     * @param valuesToMerge  the list of enumerated values to merge into the target
     */
    public void mergeEnumeratedValues(TLAbstractEnumeration target, List<TLEnumValue> valuesToMerge) {
		ModelElementCloner cloner = getCloner( target );
        List<TLEnumValue> existingValues = EnumCodegenUtils.getInheritedValues( target );
    	Set<String> existingValueLiterals = new HashSet<String>();
        
        for (TLEnumValue existingValue : existingValues) {
        	existingValueLiterals.add( existingValue.getLiteral() );
        }
        
        for (TLEnumValue sourceValue : valuesToMerge) {
        	if (!existingValueLiterals.contains(sourceValue.getLiteral())) {
        		target.addValue( cloner.clone( sourceValue ) );
        	}
        }
    }
    
    /**
     * Merges the constraints of the given source entity into the target.  If any constraint
     * is already defined in the target entity, that source item constraint will be ignored.
     * 
     * @param target  the entity that will receive new simple constraints from the merge
     * @param source  the entity whose constraints will be merged into the target
     */
    public void mergeSimpleConstraints(TLSimple target, TLSimple source) {
    	if ((target.getPattern() == null) || (target.getPattern().length() == 0)) {
    		target.setPattern( source.getPattern() );
    	}
    	if (target.getMinLength() < 0) {
    		target.setMinLength( source.getMinLength() );
    	}
    	if (target.getMaxLength() < 0) {
    		target.setMaxLength( source.getMaxLength() );
    	}
    	if (target.getFractionDigits() < 0) {
    		target.setFractionDigits( source.getFractionDigits() );
    	}
    	if (target.getTotalDigits() < 0) {
    		target.setTotalDigits( source.getTotalDigits() );
    	}
    	if ((target.getMinInclusive() == null) || (target.getMinInclusive().length() == 0)) {
    		target.setMinInclusive( source.getMinInclusive() );
    	}
    	if ((target.getMaxInclusive() == null) || (target.getMaxInclusive().length() == 0)) {
    		target.setMaxInclusive( source.getMaxInclusive() );
    	}
    	if ((target.getMinExclusive() == null) || (target.getMinExclusive().length() == 0)) {
    		target.setMinExclusive( source.getMinExclusive() );
    	}
    	if ((target.getMaxExclusive() == null) || (target.getMaxExclusive().length() == 0)) {
    		target.setMaxExclusive( source.getMaxExclusive() );
    	}
    }
    
    /**
     * Returns a <code>ModelElementCloner</code> that can be used to create a deep clone
     * of the given entity.
     * 
     * @param entity  the versioned entity for which to return a cloner
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
