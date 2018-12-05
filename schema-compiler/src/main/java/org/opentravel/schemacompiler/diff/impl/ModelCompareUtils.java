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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLContextualFacet;
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
import org.opentravel.schemacompiler.version.OTA2VersionComparator;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Static utility methods required for model comparisons.
 */
public class ModelCompareUtils {
	
	private static VersionSchemeFactory vsFactory = VersionSchemeFactory.getInstance();
	
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
	 * Returns a list of all EXAMPLE values for the field.
	 * 
	 * @param owner  the entity or field for which to return EXAMPLE values
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
		List<String> facetNames = new ArrayList<>();
		
		buildFacetNames( entity.getAllFacets(), facetNames,
				new FacetCodegenDelegateFactory( null ), new HashSet<TLFacet>() );
		return facetNames;
	}
	
	/**
	 * Recursive method that constructs the list of all facet names.
	 * 
	 * @param facets  the list of facets from which to collect names
	 * @param facetNames  the list of facet names being assembled
	 * @param factory  the facet delegate factory to use for facet analysis
	 * @param visitedFacets  the list of facets already visited (prevents infinite loops)
	 */
	private static void buildFacetNames(List<? extends TLFacet> facets, List<String> facetNames,
			FacetCodegenDelegateFactory factory, Set<TLFacet> visitedFacets) {
		for (TLFacet facet : facets) {
			if (!visitedFacets.contains( facet )) {
				visitedFacets.add( facet );
				
				if (factory.getDelegate( facet ).hasContent()) {
					facetNames.add( facet.getFacetType().getIdentityName(
							FacetCodegenUtils.getFacetName( facet ) ) );
				}
				if (facet instanceof TLContextualFacet) {
					buildFacetNames( ((TLContextualFacet) facet).getChildFacets(),
							facetNames, factory, visitedFacets );
				}
			}
		}
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
	
	/**
	 * Identifies all of the name versions in the given <code>QName</code> that match the
	 * specified name.
	 * 
	 * @param name  the name to which all resulting versions should be matched
	 * @param nameSet  the set of all names from which to extract matching versions
	 * @param versionScheme  the versioning scheme to use when comparing namespace URI's
	 * @return List<QName>
	 */
	public static List<QName> findMatchingVersions(QName name, Set<QName> nameSet, String versionScheme) {
		List<QName> matchingNames = new ArrayList<>();
		
		try {
			if ((name != null) && (name.getLocalPart() != null) && (name.getNamespaceURI() != null)) {
				VersionScheme vScheme = vsFactory.getVersionScheme( versionScheme );
				String targetBaseNS = getBaseNamespace( name.getNamespaceURI(), vScheme );
				
				for (QName testName : nameSet) {
					if (name.getLocalPart().equals( testName.getLocalPart() )) {
						String testBaseNS = getBaseNamespace( testName.getNamespaceURI(), vScheme );
						
						if (targetBaseNS.equals( testBaseNS )) {
							matchingNames.add( testName );
						}
					}
				}
			}
		} catch (VersionSchemeException e) {
			// Ignore and return no matching names
		}
		return matchingNames;
	}
	
	/**
	 * Returns the corresponding base namespace if the given one contains a version component.
	 * 
	 * @param ns  the namespace for which to return a base
	 * @param vScheme  the version scheme to use when analyzing the namespace URI
	 * @return String
	 */
	private static String getBaseNamespace(String ns, VersionScheme vScheme) {
		String baseNS;
		try {
			baseNS = vScheme.getBaseNamespace( ns );
			
		} catch (IllegalArgumentException e) {
			baseNS = ns;
		}
		return baseNS;
	}
	
	/**
	 * Returns the <code>QName</code> version from the given list that most closely matches
	 * the one provided.
	 * 
	 * @param name  the name to which the closest match should be returned
	 * @param matchingVersions  the list of matching versions from which the closest match should be identified
	 * @param versionScheme  the versioning scheme to use when comparing namespace URI's
	 * @return QName
	 */
	public static QName findClosestVersion(QName name, List<QName> matchingVersions, String versionScheme) {
		QName closestMatch = null;
		
		if (!matchingVersions.isEmpty()) {
			List<VersionedQName> nameVersions = new ArrayList<>();
			
			if (matchingVersions.contains( name )) { // Exact match found
				closestMatch = name;
				
			} else { // Look for closest match
				
				// Sort the list of all versions in ascending order
				for (QName matchingVersion : matchingVersions) {
					nameVersions.add( new VersionedQName( matchingVersion, versionScheme ) );
				}
				nameVersions.add( new VersionedQName( name, versionScheme ) );
				Collections.sort( nameVersions, new OTA2VersionComparator( true ) );
				
				// Locate the original name in the sorted list
				for (int i = 0; i < nameVersions.size(); i++) {
					VersionedQName vName = nameVersions.get( i );
					
					if (vName.name == name) {
						// If the name was the last item in the list, take the previous
						// version; otherwise, always assume that the next later version
						// is the closest match.
						if (i == (nameVersions.size() - 1)) {
							closestMatch = nameVersions.get( i - 1 ).name;
							
						} else {
							closestMatch = nameVersions.get( i + 1 ).name;
						}
						break;
					}
				}
			}
		}
		return closestMatch;
	}
	
	/**
	 * Versioned wrapper for qualified names used for sorting in version number order.
	 */
	private static class VersionedQName implements Versioned {
		
		public QName name;
		private String baseNS;
		private String versionSchemeId;
		private String versionId;
		
		/**
		 * Constructor that provides the qualified name and version scheme to use for
		 * processing.
		 * 
		 * @param name  the qualified name
		 * @param versionScheme  the version scheme to use for processing and comparisons
		 */
		public VersionedQName(QName name, String versionScheme) {
			this.name = name;
			this.versionSchemeId = versionScheme;
			
			try {
				VersionScheme vScheme = vsFactory.getVersionScheme( versionScheme );
				
				this.baseNS = vScheme.getBaseNamespace( name.getNamespaceURI() );
				this.versionId = vScheme.getVersionIdentifier( name.getNamespaceURI() );
				
			} catch (VersionSchemeException e) {
				// Ignore and assign default values
				this.baseNS = name.getNamespaceURI();
				this.versionId = "0.0.0";
			}
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getNamespace()
		 */
		@Override
		public String getNamespace() {
			return name.getNamespaceURI();
		}

		/**
		 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
		 */
		@Override
		public String getLocalName() {
			return name.getLocalPart();
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getVersion()
		 */
		@Override
		public String getVersion() {
			return versionId;
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getVersionScheme()
		 */
		@Override
		public String getVersionScheme() {
			return versionSchemeId;
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getBaseNamespace()
		 */
		@Override
		public String getBaseNamespace() {
			return baseNS;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
		 */
		@Override
		public AbstractLibrary getOwningLibrary() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement()
		 */
		@Override
		public LibraryElement cloneElement() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement(org.opentravel.schemacompiler.model.AbstractLibrary)
		 */
		@Override
		public LibraryElement cloneElement(AbstractLibrary namingContext) {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
		 */
		@Override
		public TLModel getOwningModel() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
		 */
		@Override
		public String getValidationIdentity() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#isLaterVersion(org.opentravel.schemacompiler.version.Versioned)
		 */
		@Override
		public boolean isLaterVersion(Versioned otherVersionedItem) {
			return false;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#addListener(org.opentravel.schemacompiler.event.ModelElementListener)
		 */
		@Override
		public void addListener(ModelElementListener listener) {
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#removeListener(org.opentravel.schemacompiler.event.ModelElementListener)
		 */
		@Override
		public void removeListener(ModelElementListener listener) {
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#getListeners()
		 */
		@Override
		public Collection<ModelElementListener> getListeners() {
			return Collections.emptyList();
		}
		
	}
	
}
