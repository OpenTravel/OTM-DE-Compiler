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

package org.opentravel.schemacompiler.diff.impl;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * Facade used to compare the contents of two entities even though the two entities
 * may be defined as different OTM types (business object, core, choice, operation,
 * etc.).
 */
public class EntityComparisonFacade {
	
	private NamedEntity entity;
	private Class<?> entityType;
	private TLLibrary owningLibrary;
	private String name;
	private NamedEntity parentType;
	private NamedEntity extendsType;
	private NamedEntity simpleCoreType;
	private List<String> aliasNames = new ArrayList<>();
	private List<String> facetNames = new ArrayList<>();
	private List<String> roleNames = new ArrayList<>();
	private List<TLMemberField<?>> memberFields = new ArrayList<>();
	private List<String> enumValues = new ArrayList<>();
	private boolean simpleList;
	private String patternConstraint;
	private int minLengthConstraint;
	private int maxLengthConstraint;
	private int fractionDigitsConstraint;
	private int totalDigitsConstraint;
	private String minInclusiveConstraint;
	private String maxInclusiveConstraint;
	private String minExclusiveConstraint;
	private String maxExclusiveConstraint;
	private TLDocumentation documentation;
	private List<String> equivalents = new ArrayList<>();
	private List<String> examples = new ArrayList<>();
	
	/**
	 * Creates a comparison facade for the given simple type.
	 * 
	 * @param entity  the entity from which to create the facade
	 */
	public EntityComparisonFacade(NamedEntity entity) {
		this.entity = entity;
		
		if (entity instanceof TLSimple) {
			init( (TLSimple) entity );
			
		} else if (entity instanceof TLAbstractEnumeration) {
			init( (TLAbstractEnumeration) entity );
			
		} else if (entity instanceof TLValueWithAttributes) {
			init( (TLValueWithAttributes) entity );
			
		} else if (entity instanceof TLCoreObject) {
			init( (TLCoreObject) entity );
			
		} else if (entity instanceof TLChoiceObject) {
			init( (TLChoiceObject) entity );
			
		} else if (entity instanceof TLBusinessObject) {
			init( (TLBusinessObject) entity );
			
		} else if (entity instanceof TLOperation) {
			init( (TLOperation) entity );
			
		} else if (entity instanceof TLExtensionPointFacet) {
			init( (TLExtensionPointFacet) entity );
		}
	}
	
	/**
	 * Initializes this comparison facade using the given simple type.
	 * 
	 * @param entity  the entity from which to create the facade
	 */
	private void init(TLSimple entity) {
		this.entityType = entity.getClass();
		this.owningLibrary = (TLLibrary) entity.getOwningLibrary();
		this.name = entity.getName();
		this.parentType = entity.getParentType();
		this.simpleList = entity.isListTypeInd();
		this.patternConstraint = entity.getPattern();
		this.minLengthConstraint = entity.getMinLength();
		this.maxLengthConstraint = entity.getMaxLength();
		this.fractionDigitsConstraint = entity.getFractionDigits();
		this.totalDigitsConstraint = entity.getTotalDigits();
		this.minInclusiveConstraint = entity.getMinInclusive();
		this.maxInclusiveConstraint = entity.getMaxInclusive();
		this.minExclusiveConstraint = entity.getMinExclusive();
		this.maxExclusiveConstraint = entity.getMaxExclusive();
		this.examples = ModelCompareUtils.getExamples( entity );
		this.equivalents = ModelCompareUtils.getEquivalents( entity );
		this.documentation = entity.getDocumentation();
	}
	
	/**
	 * Initializes this comparison facade using the given open or closed enumeration type.
	 * 
	 * @param entity  the entity from which to create the facade
	 */
	private void init(TLAbstractEnumeration entity) {
		this.entityType = entity.getClass();
		this.owningLibrary = (TLLibrary) entity.getOwningLibrary();
		this.name = entity.getName();
		this.extendsType = ModelCompareUtils.getExtendedEntity( entity );
		this.enumValues = ModelCompareUtils.getEnumValues( entity );
		this.documentation = entity.getDocumentation();
	}
	
	/**
	 * Initializes this comparison facade using the given VWA type.
	 * 
	 * @param entity  the entity from which to create the facade
	 */
	private void init(TLValueWithAttributes entity) {
		this.entityType = entity.getClass();
		this.owningLibrary = (TLLibrary) entity.getOwningLibrary();
		this.name = entity.getName();
		this.parentType = entity.getParentType();
		this.examples = ModelCompareUtils.getExamples( entity );
		this.equivalents = ModelCompareUtils.getEquivalents( entity );
		this.memberFields.addAll( entity.getMemberFields() );
		this.documentation = entity.getDocumentation();
	}
	
	/**
	 * Initializes this comparison facade using the given core type.
	 * 
	 * @param entity  the entity from which to create the facade
	 */
	private void init(TLCoreObject entity) {
		this.entityType = entity.getClass();
		this.owningLibrary = (TLLibrary) entity.getOwningLibrary();
		this.name = entity.getName();
		this.extendsType = ModelCompareUtils.getExtendedEntity( entity );
		this.simpleCoreType = entity.getSimpleFacet().getSimpleType();
		this.aliasNames = ModelCompareUtils.getAliasNames( entity );
		this.facetNames = ModelCompareUtils.getFacetNames( entity );
		this.roleNames = ModelCompareUtils.getRoleNames( entity );
		this.equivalents = ModelCompareUtils.getEquivalents( entity );
		this.memberFields.addAll( entity.getSummaryFacet().getMemberFields() );
		this.memberFields.addAll( entity.getDetailFacet().getMemberFields() );
		this.documentation = entity.getDocumentation();
	}
	
	/**
	 * Initializes this comparison facade using the given choice type.
	 * 
	 * @param entity  the entity from which to create the facade
	 */
	private void init(TLChoiceObject entity) {
		this.entityType = entity.getClass();
		this.owningLibrary = (TLLibrary) entity.getOwningLibrary();
		this.name = entity.getName();
		this.extendsType = ModelCompareUtils.getExtendedEntity( entity );
		this.aliasNames = ModelCompareUtils.getAliasNames( entity );
		this.facetNames = ModelCompareUtils.getFacetNames( entity );
		this.equivalents = ModelCompareUtils.getEquivalents( entity );
		this.documentation = entity.getDocumentation();
		this.memberFields.addAll( entity.getSharedFacet().getMemberFields() );
		
		for (TLContextualFacet facet : entity.getChoiceFacets()) {
			if (facet.isLocalFacet()) {
				this.memberFields.addAll( facet.getMemberFields() );
			}
		}
	}
	
	/**
	 * Initializes this comparison facade using the given business object type.
	 * 
	 * @param entity  the entity from which to create the facade
	 */
	private void init(TLBusinessObject entity) {
		this.entityType = entity.getClass();
		this.owningLibrary = (TLLibrary) entity.getOwningLibrary();
		this.name = entity.getName();
		this.extendsType = ModelCompareUtils.getExtendedEntity( entity );
		this.aliasNames = ModelCompareUtils.getAliasNames( entity );
		this.facetNames = ModelCompareUtils.getFacetNames( entity );
		this.equivalents = ModelCompareUtils.getEquivalents( entity );
		this.documentation = entity.getDocumentation();
		this.memberFields.addAll( entity.getIdFacet().getMemberFields() );
		this.memberFields.addAll( entity.getSummaryFacet().getMemberFields() );
		this.memberFields.addAll( entity.getDetailFacet().getMemberFields() );
		
		for (TLContextualFacet facet : entity.getCustomFacets()) {
			if (facet.isLocalFacet()) {
				this.memberFields.addAll( facet.getMemberFields() );
			}
		}
		for (TLContextualFacet facet : entity.getQueryFacets()) {
			if (facet.isLocalFacet()) {
				this.memberFields.addAll( facet.getMemberFields() );
			}
		}
		// TODO: Add support for nested local contextual facets
		// TODO: Add diff support for update facet comparisons
	}
	
	/**
	 * Initializes this comparison facade using the given operation type.
	 * 
	 * @param entity  the entity from which to create the facade
	 */
	private void init(TLOperation entity) {
		this.entityType = entity.getClass();
		this.owningLibrary = (TLLibrary) entity.getOwningLibrary();
		this.name = entity.getName();
		this.extendsType = ModelCompareUtils.getExtendedEntity( entity );
		this.facetNames = ModelCompareUtils.getFacetNames( entity );
		this.equivalents = ModelCompareUtils.getEquivalents( entity );
		this.documentation = entity.getDocumentation();
		this.memberFields.addAll( entity.getRequest().getMemberFields() );
		this.memberFields.addAll( entity.getResponse().getMemberFields() );
		this.memberFields.addAll( entity.getNotification().getMemberFields() );
	}

	/**
	 * Initializes this comparison facade using the given extension point facet type.
	 * 
	 * @param entity  the entity from which to create the facade
	 */
	private void init(TLExtensionPointFacet entity) {
		this.entityType = entity.getClass();
		this.owningLibrary = (TLLibrary) entity.getOwningLibrary();
		this.extendsType = ModelCompareUtils.getExtendedEntity( entity );
		this.documentation = entity.getDocumentation();
		this.memberFields.addAll( entity.getMemberFields() );
	}
	
	/**
	 * Returns the value of the 'entity' field.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getEntity() {
		return entity;
	}

	/**
	 * Returns the value of the 'entityType' field.
	 *
	 * @return Class<?>
	 */
	public Class<?> getEntityType() {
		return entityType;
	}

	/**
	 * Returns the value of the 'owningLibrary' field.
	 *
	 * @return TLLibrary
	 */
	public TLLibrary getOwningLibrary() {
		return owningLibrary;
	}

	/**
	 * Returns the value of the 'name' field.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value of the 'parentType' field.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getParentType() {
		return parentType;
	}

	/**
	 * Returns the value of the 'extendsType' field.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getExtendsType() {
		return extendsType;
	}

	/**
	 * Returns the value of the 'simpleCoreType' field.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getSimpleCoreType() {
		return simpleCoreType;
	}

	/**
	 * Returns the value of the 'aliasNames' field.
	 *
	 * @return List<String>
	 */
	public List<String> getAliasNames() {
		return aliasNames;
	}

	/**
	 * Returns the value of the 'facetNames' field.
	 *
	 * @return List<String>
	 */
	public List<String> getFacetNames() {
		return facetNames;
	}

	/**
	 * Returns the value of the 'roleNames' field.
	 *
	 * @return List<String>
	 */
	public List<String> getRoleNames() {
		return roleNames;
	}

	/**
	 * Returns the value of the 'memberFields' field.
	 *
	 * @return List<TLMemberField<?>>
	 */
	public List<TLMemberField<?>> getMemberFields() {
		return memberFields;
	}

	/**
	 * Returns the value of the 'enumValues' field.
	 *
	 * @return List<String>
	 */
	public List<String> getEnumValues() {
		return enumValues;
	}

	/**
	 * Returns the value of the 'simpleList' field.
	 *
	 * @return boolean
	 */
	public boolean isSimpleList() {
		return simpleList;
	}

	/**
	 * Returns the value of the 'patternConstraint' field.
	 *
	 * @return String
	 */
	public String getPatternConstraint() {
		return patternConstraint;
	}

	/**
	 * Returns the value of the 'minLengthConstraint' field.
	 *
	 * @return int
	 */
	public int getMinLengthConstraint() {
		return minLengthConstraint;
	}

	/**
	 * Returns the value of the 'maxLengthConstraint' field.
	 *
	 * @return int
	 */
	public int getMaxLengthConstraint() {
		return maxLengthConstraint;
	}

	/**
	 * Returns the value of the 'fractionDigitsConstraint' field.
	 *
	 * @return int
	 */
	public int getFractionDigitsConstraint() {
		return fractionDigitsConstraint;
	}

	/**
	 * Returns the value of the 'totalDigitsConstraint' field.
	 *
	 * @return int
	 */
	public int getTotalDigitsConstraint() {
		return totalDigitsConstraint;
	}

	/**
	 * Returns the value of the 'minInclusiveConstraint' field.
	 *
	 * @return String
	 */
	public String getMinInclusiveConstraint() {
		return minInclusiveConstraint;
	}

	/**
	 * Returns the value of the 'maxInclusiveConstraint' field.
	 *
	 * @return String
	 */
	public String getMaxInclusiveConstraint() {
		return maxInclusiveConstraint;
	}

	/**
	 * Returns the value of the 'minExclusiveConstraint' field.
	 *
	 * @return String
	 */
	public String getMinExclusiveConstraint() {
		return minExclusiveConstraint;
	}

	/**
	 * Returns the value of the 'maxExclusiveConstraint' field.
	 *
	 * @return String
	 */
	public String getMaxExclusiveConstraint() {
		return maxExclusiveConstraint;
	}

	/**
	 * Returns the value of the 'documentation' field.
	 *
	 * @return TLDocumentation
	 */
	public TLDocumentation getDocumentation() {
		return documentation;
	}

	/**
	 * Returns the value of the 'equivalents' field.
	 *
	 * @return List<String>
	 */
	public List<String> getEquivalents() {
		return equivalents;
	}

	/**
	 * Returns the value of the 'examples' field.
	 *
	 * @return List<String>
	 */
	public List<String> getExamples() {
		return examples;
	}
	
}
