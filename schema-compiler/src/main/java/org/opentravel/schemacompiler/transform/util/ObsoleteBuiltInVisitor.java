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

package org.opentravel.schemacompiler.transform.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.ic.ImportManagementIntegrityChecker;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.transform.symbols.AbstractSymbolResolver;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visitor that identifies any attribute or element references to the <code>OTA_SimpleTypes</code>
 * library that have not yet been resolved.  If any such references are discovered, the visitor also
 * contains a method to load the legacy library from the OpenTravel repository and assign all of the
 * missing references.
 */
public class ObsoleteBuiltInVisitor extends ModelElementVisitorAdapter {
	
    protected static final String OBSOLETE_BUILTIN_NAME = "OTA_SimpleTypes";
    protected static final String OBSOLETE_BUILTIN_NS   = "http://www.opentravel.org/OTM/Common/v0";
    protected static final String OBSOLETE_BUILTIN_URL  = "otm://Opentravel/OTA_SimpleTypes_0_0_0.otm";
    protected static final String OTA_REPOSITORY_ID     = "Opentravel";
    protected static final String OTA_REPOSITORY_URL    = "http://www.opentravelmodel.net";
    
    private static Logger log = LoggerFactory.getLogger( ObsoleteBuiltInVisitor.class );
    
    private static List<String> obsoleteTypeNames = Arrays.asList(
    		"String_AlphaNumeric", "String_Long", "String", "String_Short", "String_Tiny",
    		"String_Character_One", "String_UpperCaseAlpha", "String_UpperCaseAlphaNumeric",
    		"String_Text", "Code_Airline_IATA", "Code_Airline_ICAO", "Code_Airport_IATA",
    		"Code_Airport_ICAO", "Code_Agency_IATA_Number", "Code_Country", "Code_Country_ISO_3",
    		"FlightNumber_Suffix", "DateTime", "DateTime_WithTimeZone", "Code_Language",
    		"Code_Language_ISO_3", "DateTime_Local", "Code_Currency", "Code_Railway_Station_IATA",
    		"Enum_DayOfWeek", "Enum_MonthOfYear", "FlightNumber_With_Suffix", "Code_Agency_IATA"
    	);
    
    protected TLModel model;
    private List<UnresolvedObsoleteReference> unresolvedReferences = new ArrayList<>();
    
	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
	 */
	@Override
	public boolean visitSimple(TLSimple simple) {
		if (simple.getParentType() == null) {
			checkUnresolvedReference( simple.getOwningLibrary(), simple, simple.getParentTypeName() );
		}
		return true;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public boolean visitValueWithAttributes(TLValueWithAttributes vwa) {
		if (vwa.getParentType() == null) {
			checkUnresolvedReference( vwa.getOwningLibrary(), vwa, vwa.getParentTypeName() );
		}
		return true;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
	 */
	@Override
	public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
		if (simpleFacet.getSimpleType() == null) {
			checkUnresolvedReference( simpleFacet.getOwningLibrary(), simpleFacet, simpleFacet.getSimpleTypeName() );
		}
		return true;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
	 */
	@Override
	public boolean visitAttribute(TLAttribute attribute) {
		if (attribute.getType() == null) {
			checkUnresolvedReference( attribute.getOwningLibrary(), attribute, attribute.getTypeName() );
		}
		return true;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
	 */
	@Override
	public boolean visitElement(TLProperty element) {
		if (element.getType() == null) {
			checkUnresolvedReference( element.getOwningLibrary(), element, element.getTypeName() );
		}
		return true;
	}
    
	/**
	 * Inspects the given unresolved reference to determine if it is one of the types from the
	 * obsolete built-in library.  If such a reference is found, a new <code>UnresolvedObsoleteReference</code>
	 * is created for this visitor.
	 * 
	 * @param modelElement  the OTM model element to be checked
	 * @param referenceName  the fully-qualified reference string to be resolved
	 */
	private void checkUnresolvedReference(AbstractLibrary owningLibrary, ModelElement modelElement, String referenceName) {
		if ((owningLibrary != null) && (modelElement != null) && (referenceName != null) && (referenceName.length() > 0)) {
			String[] referenceParts = AbstractSymbolResolver.parseEntityName( referenceName );
			String prefix = referenceParts[0];
			String localName = referenceParts[1];
			String ns = (prefix == null) ? owningLibrary.getNamespace() : owningLibrary.getNamespaceForPrefix( prefix );
			
			if ((ns != null) && ns.equals( OBSOLETE_BUILTIN_NS ) && obsoleteTypeNames.contains( localName )) {
				unresolvedReferences.add( new UnresolvedObsoleteReference( owningLibrary, modelElement, localName ) );
			}
			if (model == null) {
				model = owningLibrary.getOwningModel();
			}
		}
	}
	
	/**
	 * If any references to the unresolved built-in library were discovered, this method attempts to
	 * load the managed library containing those types from the OpenTravel repository.  Once loaded,
	 * all of the unresolved references are assigned.
	 */
	public void resolveObsoleteBuiltInReferences() {
		if (!unresolvedReferences.isEmpty()) {
			AbstractLibrary managedLib = loadObsoleteBuiltIn();
			
			if (managedLib != null) {
				Set<AbstractLibrary> affectedLibraries = new HashSet<>();
				
				for (UnresolvedObsoleteReference ref : unresolvedReferences) {
					ref.resolveReference( managedLib );
					affectedLibraries.add( ref.getAffectedLibrary() );
				}
				for (AbstractLibrary library : affectedLibraries) {
					if (library instanceof TLLibrary) {
						ImportManagementIntegrityChecker.verifyReferencedLibraries( (TLLibrary) library );
					}
				}
			}
		}
	}
	
	/**
	 * Attempts to load the obsolete built-in library from the OpenTravel repository.  If
	 * the load is successful, the managed library is returned; if unsuccessful this method
	 * will return null.
	 */
	protected AbstractLibrary loadObsoleteBuiltIn() {
		AbstractLibrary managedLib = null;
		
		try {
			RepositoryManager manager = RepositoryManager.getDefault();
			Repository otaRepository = manager.getRepository( OTA_REPOSITORY_ID );
			
			if (otaRepository == null) {
				otaRepository = manager.addRemoteRepository( OTA_REPOSITORY_URL );
			}
			RepositoryItem item = otaRepository.getRepositoryItem( OBSOLETE_BUILTIN_URL, OBSOLETE_BUILTIN_NS );
			
			if (item != null) {
				LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>( model );
				File libraryFile = URLUtils.toFile( manager.getContentLocation( item ) );
		        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource( libraryFile );
				
				modelLoader.loadLibraryModel( libraryInput );
				managedLib = model.getLibrary( OBSOLETE_BUILTIN_NS, OBSOLETE_BUILTIN_NAME );
			}
			
		} catch (Throwable t) {
			log.warn("Error loading obsolete built-in dependency from the OpenTravel repository.", t);
		}
		return managedLib;
	}
	
	/**
	 * Encapsulates all information required to resolve and assign a type reference to
	 * an obsolete built-in type that is now managed in the OpenTravel repository.
	 */
	private static class UnresolvedObsoleteReference {
		
		private AbstractLibrary owningLibrary;
		private ModelElement modelElement;
		private String referenceLocalName;
		
		/**
		 * Constructor that specifies the OTM model element to be resolved as well as the
		 * local name of the entity from the obsolete built-in that should be assigned.
		 * 
		 * @param owningLibrary  the owning library of the given model element
		 * @param modelElement  the OTM model element to be resolved
		 * @param referenceLocalName  the local name of the entity to be assigned
		 */
		public UnresolvedObsoleteReference(AbstractLibrary owningLibrary, ModelElement modelElement,
				String referenceLocalName) {
			this.owningLibrary = owningLibrary;
			this.modelElement = modelElement;
			this.referenceLocalName = referenceLocalName;
		}
		
		/**
		 * Returns the library that contains the unresolved reference.
		 * 
		 * @return AbstractLibrary
		 */
		public AbstractLibrary getAffectedLibrary() {
			return owningLibrary;
		}
		
		/**
		 * Resolves the reference from the obsolete built-in library provided.
		 * 
		 * @param obsoleteBuiltInLibrary  the managed library containing all of the obsolete built-in types
		 */
		public void resolveReference(AbstractLibrary obsoleteBuiltInLibrary) {
			NamedEntity entity = obsoleteBuiltInLibrary.getNamedMember( referenceLocalName );
			
			if ((obsoleteBuiltInLibrary != null) && (entity != null)) {
				if (modelElement instanceof TLSimple) {
					((TLSimple) modelElement).setParentType( (TLAttributeType) entity );
					
				} else if (modelElement instanceof TLValueWithAttributes) {
					((TLValueWithAttributes) modelElement).setParentType( (TLAttributeType) entity );
					
				} else if (modelElement instanceof TLSimpleFacet) {
					((TLSimpleFacet) modelElement).setSimpleType( entity );
					
				} else if (modelElement instanceof TLAttribute) {
					((TLAttribute) modelElement).setType( (TLPropertyType) entity );
					
				} else if (modelElement instanceof TLProperty) {
					((TLProperty) modelElement).setType( (TLPropertyType) entity );
				}
				
			} else {
				log.warn("Unable to resolve built-in reference '" +
						referenceLocalName + "' in library " + owningLibrary.getName());
			}
		}
		
	}
	
}
