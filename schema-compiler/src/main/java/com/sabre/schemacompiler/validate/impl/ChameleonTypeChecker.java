/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLInclude;
import com.sabre.schemacompiler.model.TLNamespaceImport;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDComplexType;
import com.sabre.schemacompiler.model.XSDElement;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.model.XSDSimpleType;
import com.sabre.schemacompiler.transform.AnonymousEntityFilter;
import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemacompiler.visitor.DependencyNavigator;
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Component that recursively analyzes a library's dependencies in order to identify duplicate
 * entities that are included/imported by the various dependent libraries.  Any duplicate symbols
 * from different chameleon schemas are reported by this checker, even if they are not directly
 * referenced by any properties or attributes of the model.  This is because the duplicate entities
 * will cause compile-time errors for binding tools (e.g. JAXB) when code generation is attempted.
 * 
 * @author S. Livezey
 */
public class ChameleonTypeChecker {
	
	private Map<String,List<XSDLibrary>> chameleonSchemaMappings = new HashMap<String,List<XSDLibrary>>();
	private AbstractLibrary sourceLibrary;
	
	/**
	 * Private constructor that assigns the library to be analyzed.
	 * 
	 * @param library  the library to analyze
	 */
	private ChameleonTypeChecker(AbstractLibrary library) {
		this.sourceLibrary = library;
	}
	
	/**
	 * Performs the search and returns a list of duplicate chameleon symbols.
	 * 
	 * @return Collection<String>
	 */
	private Collection<String> findDuplicateSymbols() {
		DependencyNavigator.navigate(sourceLibrary, new ChameleonVisitor());
		return scanForDuplicateSymbols();
	}
	
	/**
	 * Scans the dependencies of the given library to identify any duplicate symbols from
	 * chameleon schemas that are assigned to the library's namespace.
	 * 
	 * @param library  the library to analyze
	 * @return Collection<String>
	 */
	public static Collection<String> findDuplicateChameleonSymbols(AbstractLibrary library) {
		return new ChameleonTypeChecker(library).findDuplicateSymbols();
	}
	
	/**
	 * Analyzes the declarations of all chameleon schemas found during the dependency search
	 * and returns a list of duplicate symbols.
	 * 
	 * @return Collection<String>
	 */
	private Collection<String> scanForDuplicateSymbols() {
		Set<String> duplicateSymbols = new HashSet<String>();
		
		for (String namespace : chameleonSchemaMappings.keySet()) {
			List<XSDLibrary> chameleonSchemas = chameleonSchemaMappings.get(namespace);
			Set<String> chameleonTypes = new HashSet<String>();
			Set<String> chameleonElements = new HashSet<String>();
			Set<String> duplicateTypes = new HashSet<String>();
			Set<String> duplicateElements = new HashSet<String>();
			
			for (XSDLibrary chameleonSchema : chameleonSchemas) {
				for (LibraryMember member : chameleonSchema.getNamedMembers()) {
					if ((member instanceof XSDSimpleType) || (member instanceof XSDComplexType)) {
						checkForDuplicate(member, chameleonTypes, duplicateTypes);
						
					} else if (member instanceof XSDElement) {
						checkForDuplicate(member, chameleonElements, duplicateElements);
					}
				}
			}
			duplicateSymbols.addAll(duplicateTypes);
			duplicateSymbols.addAll(duplicateElements);
		}
		return duplicateSymbols;
	}
	
	/**
	 * Checks to see if the 'allMembers' set already contains a name that matches the given library
	 * member.  If so, the name will be added to the list of duplicates.
	 * 
	 * @param member  the library member to check
	 * @param allMembers  the set of all member names encountered so far
	 * @param duplicateMembers  the set of duplicate member names discovered so far
	 */
	private void checkForDuplicate(LibraryMember member, Set<String> allMembers, Set<String> duplicateMembers) {
		String memberName = member.getLocalName();
		
		if (allMembers.contains(memberName)) {
			if (!duplicateMembers.contains(memberName)) {
				duplicateMembers.add(memberName);
			}
		} else {
			allMembers.add(memberName);
		}
	}
	
	/**
	 * Recursive method that scans the imports and includes (using 'schemaLocation' URL references) to
	 * identify chameleon schemas that will be assigned to the same namespace as the original source library.
	 * 
	 * @param legacySchema  the legacy schema whose dependencies are to be analyzed
	 * @param referringNamespace  the namespace of the library that imported or included this one
	 * @param visitedLibraries  the list of legacy schemas that have already been visited by this method
	 */
	private void navigateXSDLibraryDependencies(XSDLibrary legacySchema, String referringNamespace, Collection<XSDLibrary> visitedLibraries) {
		String schemaNamespace = legacySchema.getNamespace();
		
		// If the schema is a previously-undiscovered chameleon that will be assigned to the source
		// namespace, add it to the list of libraries to be checked for duplicate symbols
		if ( ((schemaNamespace == null) || schemaNamespace.equals(AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE)) ) {
			List<XSDLibrary> chameleonSchemas = chameleonSchemaMappings.get(referringNamespace);
			
			if (chameleonSchemas == null) {
				chameleonSchemas = new ArrayList<XSDLibrary>();
				chameleonSchemaMappings.put(referringNamespace, chameleonSchemas);
			}
			if (!chameleonSchemas.contains(legacySchema)) {
				chameleonSchemas.add(legacySchema);
			}
			schemaNamespace = referringNamespace;
		}
		visitedLibraries.add(legacySchema);
		
		// Recursively analyze the imported/included schemas
		for (TLInclude include : legacySchema.getIncludes()) {
			if (include.getPath() != null) {
				URL includedUrl = getReferencedLibraryURL(include.getPath(), legacySchema);
				AbstractLibrary includedLibrary = legacySchema.getOwningModel().getLibrary(includedUrl);
				
				if ((includedLibrary != null) && (includedLibrary instanceof XSDLibrary)
						&& !visitedLibraries.contains(includedLibrary)) {
					navigateXSDLibraryDependencies((XSDLibrary) includedLibrary, schemaNamespace, visitedLibraries);
				}
			}
		}
		
		for (TLNamespaceImport nsImport : legacySchema.getNamespaceImports()) {
			if (nsImport.getFileHints() != null) {
				for (String fileHint : nsImport.getFileHints()) {
					URL importedUrl = getReferencedLibraryURL(fileHint, legacySchema);
					AbstractLibrary importedLibrary = legacySchema.getOwningModel().getLibrary(importedUrl);
					
					if ((importedLibrary != null) && (importedLibrary instanceof XSDLibrary)
							&& !visitedLibraries.contains(importedLibrary)) {
						navigateXSDLibraryDependencies((XSDLibrary) importedLibrary, schemaNamespace, visitedLibraries);
					}
				}
			}
		}
	}
	
	/**
	 * Returns the full URL that is referenced by the specified relative URL path.
	 * 
	 * @param relativeUrl  the relative URL path to resolve
	 * @param referringLibrary  the library that is the 
	 * @return
	 */
	private URL getReferencedLibraryURL(String relativeUrl, AbstractLibrary referringLibrary) {
		URL resolvedUrl = null;
		try {
			URL libraryFolderUrl = URLUtils.getParentURL(referringLibrary.getLibraryUrl());
			resolvedUrl = URLUtils.getResolvedURL(relativeUrl, libraryFolderUrl);
			
		} catch (MalformedURLException e) {
			// no error - return a null URL
		}
		return resolvedUrl;
	}
	
	/**
	 * Visitor that captures all of the chameleon libraries that will be included in the
	 * current library's namespace
	 *
	 * @author S. Livezey
	 */
	private class ChameleonVisitor extends ModelElementVisitorAdapter {
		
		private String referringNamespace;
		
		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(com.sabre.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			referringNamespace = simple.getNamespace();
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
		 */
		@Override
		public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
			referringNamespace = valueWithAttributes.getNamespace();
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(com.sabre.schemacompiler.model.TLCoreObject)
		 */
		@Override
		public boolean visitCoreObject(TLCoreObject coreObject) {
			referringNamespace = coreObject.getNamespace();
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(com.sabre.schemacompiler.model.TLBusinessObject)
		 */
		@Override
		public boolean visitBusinessObject(TLBusinessObject businessObject) {
			referringNamespace = businessObject.getNamespace();
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(com.sabre.schemacompiler.model.TLOperation)
		 */
		@Override
		public boolean visitOperation(TLOperation operation) {
			referringNamespace = operation.getNamespace();
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(com.sabre.schemacompiler.model.TLExtensionPointFacet)
		 */
		@Override
		public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
			referringNamespace = extensionPointFacet.getNamespace();
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(com.sabre.schemacompiler.model.TLFacet)
		 */
		@Override
		public boolean visitFacet(TLFacet facet) {
			referringNamespace = facet.getNamespace();
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
		 */
		@Override
		public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
			referringNamespace = simpleFacet.getNamespace();
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(com.sabre.schemacompiler.model.TLAttribute)
		 */
		@Override
		public boolean visitAttribute(TLAttribute attribute) {
			if (attribute.getAttributeOwner() != null) {
				referringNamespace = attribute.getAttributeOwner().getNamespace();
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(com.sabre.schemacompiler.model.TLProperty)
		 */
		@Override
		public boolean visitElement(TLProperty element) {
			if (element.getPropertyOwner() != null) {
				referringNamespace = element.getPropertyOwner().getNamespace();
			}
			return true;
		}
		
		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(com.sabre.schemacompiler.model.XSDLibrary)
		 */
		@Override
		public boolean visitLegacySchemaLibrary(XSDLibrary library) {
			return false; // Do not navigate - we are replacing this with the chameleon processing routine
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDSimpleType(com.sabre.schemacompiler.model.XSDSimpleType)
		 */
		@Override
		public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
			analyzeLegacySchemaDependencies(xsdSimple);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDComplexType(com.sabre.schemacompiler.model.XSDComplexType)
		 */
		@Override
		public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
			analyzeLegacySchemaDependencies(xsdComplex);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDElement(com.sabre.schemacompiler.model.XSDElement)
		 */
		@Override
		public boolean visitXSDElement(XSDElement xsdElement) {
			analyzeLegacySchemaDependencies(xsdElement);
			return true;
		}
		
		/**
		 * If the owning library of the given entity is a legacy schema, launch the recusive analysis
		 * of its dependencies.
		 * 
		 * @param member  the library member to analyze
		 */
		private void analyzeLegacySchemaDependencies(LibraryMember member) {
			if (referringNamespace != null) {
				AbstractLibrary library = member.getOwningLibrary();
				
				if (library instanceof XSDLibrary) {
					navigateXSDLibraryDependencies((XSDLibrary) library, referringNamespace, new HashSet<XSDLibrary>());
				}
			}
		}
		
	}
	
}
