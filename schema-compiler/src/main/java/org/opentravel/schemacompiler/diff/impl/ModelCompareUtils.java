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

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLRole;

/**
 * Static utility methods required for model comparisons.
 */
public class ModelCompareUtils {
	
	/**
	 * Returns the built-in model entity for the 'xsd:boolean' simple type.
	 * 
	 * @param model  the model from which to return the boolean type entity
	 * @return NamedEntity
	 */
	public static NamedEntity getXsdBooleanType(TLModel model) {
		NamedEntity xsdBoolean = null;
		
		for (BuiltInLibrary library : model.getBuiltInLibraries()) {
			if (library.getNamespace().equals( XMLConstants.W3C_XML_SCHEMA_NS_URI )) {
				if ((xsdBoolean = library.getNamedMember( "boolean" )) != null) {
					break;
				}
			}
		}
		return xsdBoolean;
	}
	
	/**
	 * Returns a list of all example values for the field.
	 * 
	 * @param owner  the entity or field for which to return example values
	 * @return List<String>
	 */
	public static List<String> getExamples(TLExampleOwner owner) {
		List<String> examples = new ArrayList<>();
		
		for (TLExample example : owner.getExamples()) {
			if (!examples.contains( example.getValue() )) {
				examples.add( example.getValue() );
			}
		}
		return examples;
	}
	
	/**
	 * Returns a list of all equivalent values for the field.
	 * 
	 * @param owner  the entity or field for which to return equivalent values
	 * @return List<String>
	 */
	public static List<String> getEquivalents(TLEquivalentOwner owner) {
		List<String> equivalents = new ArrayList<>();
		
		for (TLEquivalent equivalent : owner.getEquivalents()) {
			if (!equivalents.contains( equivalent.getDescription() )) {
				equivalents.add( equivalent.getDescription() );
			}
		}
		return equivalents;
	}
	
	/**
	 * Returns the entity from which the given one extends, or null if an
	 * extension is not defined.
	 * 
	 * @param entity  the entity for which to return the extended entity
	 * @return NamedEntity
	 */
	public static NamedEntity getExtendedEntity(TLExtensionOwner entity) {
		NamedEntity extendedEntity = null;
		
		if (entity.getExtension() != null) {
			extendedEntity = entity.getExtension().getExtendsEntity();
		}
		return extendedEntity;
	}
	
	/**
	 * Returns a list of all alias names for the entity.
	 * 
	 * @param entity  the entity for which to return alias names
	 * @return List<String>
	 */
	public static List<String> getAliasNames(TLAliasOwner entity) {
		List<String> aliasNames = new ArrayList<>();
		
		for (TLAlias alias : entity.getAliases()) {
			aliasNames.add( alias.getName() );
		}
		return aliasNames;
	}
	
	/**
	 * Returns a list of all facet names for the entity.
	 * 
	 * @param entity  the entity for which to return facet names
	 * @return List<String>
	 */
	public static List<String> getFacetNames(TLFacetOwner entity) {
		FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory( null );
		List<String> facetNames = new ArrayList<>();
		
		for (TLFacet facet : entity.getAllFacets()) {
			if (factory.getDelegate( facet ).hasContent()) {
				facetNames.add( facet.getFacetType().getIdentityName(
						facet.getContext(), facet.getLabel() ) );
			}
		}
		return facetNames;
	}
	
	/**
	 * Returns a list of all role names for the core object.
	 * 
	 * @param entity  the core for which to return role names
	 * @return List<String>
	 */
	public static List<String> getRoleNames(TLCoreObject entity) {
		List<String> roleNames = new ArrayList<>();
		
		for (TLRole role : entity.getRoleEnumeration().getRoles()) {
			roleNames.add( role.getName() );
		}
		return roleNames;
	}
	
	/**
	 * Returns a list of all literals for the enumeration.
	 * 
	 * @param entity  the enumeration for which to return literal values
	 * @return List<String>
	 */
	public static List<String> getEnumValues(TLAbstractEnumeration entity) {
		List<String> enumValues = new ArrayList<>();
		
		for (TLEnumValue value : entity.getValues()) {
			enumValues.add( value.getLiteral() );
		}
		return enumValues;
	}
	
}
