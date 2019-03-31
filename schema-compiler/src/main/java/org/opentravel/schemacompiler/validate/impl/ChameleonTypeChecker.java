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

package org.opentravel.schemacompiler.validate.impl;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.visitor.DependencyNavigator;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Component that recursively analyzes a library's dependencies in order to identify duplicate entities that are
 * included/imported by the various dependent libraries. Any duplicate symbols from different chameleon schemas are
 * reported by this checker, even if they are not directly referenced by any properties or attributes of the model. This
 * is because the duplicate entities will cause compile-time errors for binding tools (e.g. JAXB) when code generation
 * is attempted.
 * 
 * @author S. Livezey
 */
public class ChameleonTypeChecker {

    private Map<String,List<XSDLibrary>> chameleonSchemaMappings = new HashMap<>();
    private AbstractLibrary sourceLibrary;

    /**
     * Private constructor that assigns the library to be analyzed.
     * 
     * @param library the library to analyze
     */
    private ChameleonTypeChecker(AbstractLibrary library) {
        this.sourceLibrary = library;
    }

    /**
     * Performs the search and returns a list of duplicate chameleon symbols.
     * 
     * @return Collection&lt;String&gt;
     */
    private Collection<String> findDuplicateSymbols() {
        DependencyNavigator.navigate( sourceLibrary, new ChameleonVisitor() );
        return scanForDuplicateSymbols();
    }

    /**
     * Scans the dependencies of the given library to identify any duplicate symbols from chameleon schemas that are
     * assigned to the library's namespace.
     * 
     * @param library the library to analyze
     * @return Collection&lt;String&gt;
     */
    public static Collection<String> findDuplicateChameleonSymbols(AbstractLibrary library) {
        return new ChameleonTypeChecker( library ).findDuplicateSymbols();
    }

    /**
     * Analyzes the declarations of all chameleon schemas found during the dependency search and returns a list of
     * duplicate symbols.
     * 
     * @return Collection&lt;String&gt;
     */
    private Collection<String> scanForDuplicateSymbols() {
        Set<String> duplicateSymbols = new HashSet<>();

        for (Entry<String,List<XSDLibrary>> entry : chameleonSchemaMappings.entrySet()) {
            List<XSDLibrary> chameleonSchemas = entry.getValue();
            Set<String> chameleonTypes = new HashSet<>();
            Set<String> chameleonElements = new HashSet<>();
            Set<String> duplicateTypes = new HashSet<>();
            Set<String> duplicateElements = new HashSet<>();

            for (XSDLibrary chameleonSchema : chameleonSchemas) {
                for (LibraryMember member : chameleonSchema.getNamedMembers()) {
                    if ((member instanceof XSDSimpleType) || (member instanceof XSDComplexType)) {
                        checkForDuplicate( member, chameleonTypes, duplicateTypes );

                    } else if (member instanceof XSDElement) {
                        checkForDuplicate( member, chameleonElements, duplicateElements );
                    }
                }
            }
            duplicateSymbols.addAll( duplicateTypes );
            duplicateSymbols.addAll( duplicateElements );
        }
        return duplicateSymbols;
    }

    /**
     * Checks to see if the 'allMembers' set already contains a name that matches the given library member. If so, the
     * name will be added to the list of duplicates.
     * 
     * @param member the library member to check
     * @param allMembers the set of all member names encountered so far
     * @param duplicateMembers the set of duplicate member names discovered so far
     */
    private void checkForDuplicate(LibraryMember member, Set<String> allMembers, Set<String> duplicateMembers) {
        String memberName = member.getLocalName();

        if (allMembers.contains( memberName )) {
            if (!duplicateMembers.contains( memberName )) {
                duplicateMembers.add( memberName );
            }
        } else {
            allMembers.add( memberName );
        }
    }

    /**
     * Recursive method that scans the imports and includes (using 'schemaLocation' URL references) to identify
     * chameleon schemas that will be assigned to the same namespace as the original source library.
     * 
     * @param legacySchema the legacy schema whose dependencies are to be analyzed
     * @param referringNamespace the namespace of the library that imported or included this one
     * @param visitedLibraries the list of legacy schemas that have already been visited by this method
     */
    private void navigateXSDLibraryDependencies(XSDLibrary legacySchema, String referringNamespace,
        Collection<XSDLibrary> visitedLibraries) {
        String schemaNamespace = addChameleonSchema( legacySchema, referringNamespace, visitedLibraries );

        // Recursively analyze the imported/included schemas
        navigateXsdIncludes( legacySchema, schemaNamespace, visitedLibraries );
        navigateXsdImports( legacySchema, schemaNamespace, visitedLibraries );
    }

    /**
     * If the schema is a previously-undiscovered chameleon that will be assigned to the source namespace, add it to the
     * list of libraries to be checked for duplicate symbols
     * 
     * @param legacySchema the legacy schema to process
     * @param referringNamespace the namespace of the schema that is referencing the given one
     * @param visitedLibraries the list of libraries visited so far (prevents infinite loops)
     * @return String
     */
    private String addChameleonSchema(XSDLibrary legacySchema, String referringNamespace,
        Collection<XSDLibrary> visitedLibraries) {
        String schemaNamespace = legacySchema.getNamespace();

        if (((schemaNamespace == null) || schemaNamespace.equals( AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE ))) {
            chameleonSchemaMappings.computeIfAbsent( referringNamespace,
                ns -> chameleonSchemaMappings.put( ns, new ArrayList<>() ) );
            List<XSDLibrary> chameleonSchemas = chameleonSchemaMappings.get( referringNamespace );

            if (!chameleonSchemas.contains( legacySchema )) {
                chameleonSchemas.add( legacySchema );
            }
            schemaNamespace = referringNamespace;
        }
        visitedLibraries.add( legacySchema );
        return schemaNamespace;
    }

    /**
     * Navigates the includes of the given legacy schema.
     * 
     * @param legacySchema the legacy schema whose includes are to be navigated
     * @param schemaNamespace the resolved namespace of the legacy schema
     * @param visitedLibraries the list of libraries visited so far (prevents infinite loops)
     */
    private void navigateXsdIncludes(XSDLibrary legacySchema, String schemaNamespace,
        Collection<XSDLibrary> visitedLibraries) {
        for (TLInclude include : legacySchema.getIncludes()) {
            if (include.getPath() != null) {
                URL includedUrl = getReferencedLibraryURL( include.getPath(), legacySchema );
                AbstractLibrary includedLibrary = legacySchema.getOwningModel().getLibrary( includedUrl );

                if ((includedLibrary instanceof XSDLibrary) && !visitedLibraries.contains( includedLibrary )) {
                    navigateXSDLibraryDependencies( (XSDLibrary) includedLibrary, schemaNamespace, visitedLibraries );
                }
            }
        }
    }

    /**
     * Navigates the imports of the given legacy schema.
     * 
     * @param legacySchema the legacy schema whose imports are to be navigated
     * @param schemaNamespace the resolved namespace of the legacy schema
     * @param visitedLibraries the list of libraries visited so far (prevents infinite loops)
     */
    private void navigateXsdImports(XSDLibrary legacySchema, String schemaNamespace,
        Collection<XSDLibrary> visitedLibraries) {
        for (TLNamespaceImport nsImport : legacySchema.getNamespaceImports()) {
            if (nsImport.getFileHints() != null) {
                for (String fileHint : nsImport.getFileHints()) {
                    URL importedUrl = getReferencedLibraryURL( fileHint, legacySchema );
                    AbstractLibrary importedLibrary = legacySchema.getOwningModel().getLibrary( importedUrl );

                    if ((importedLibrary instanceof XSDLibrary) && !visitedLibraries.contains( importedLibrary )) {
                        navigateXSDLibraryDependencies( (XSDLibrary) importedLibrary, schemaNamespace,
                            visitedLibraries );
                    }
                }
            }
        }
    }

    /**
     * Returns the full URL that is referenced by the specified relative URL path.
     * 
     * @param relativeUrl the relative URL path to resolve
     * @param referringLibrary the library that is the
     * @return URL
     */
    private URL getReferencedLibraryURL(String relativeUrl, AbstractLibrary referringLibrary) {
        URL resolvedUrl = null;
        try {
            URL libraryFolderUrl = URLUtils.getParentURL( referringLibrary.getLibraryUrl() );
            resolvedUrl = URLUtils.getResolvedURL( relativeUrl, libraryFolderUrl );

        } catch (MalformedURLException e) {
            // no error - return a null URL
        }
        return resolvedUrl;
    }

    /**
     * Visitor that captures all of the chameleon libraries that will be included in the current library's namespace
     * 
     * @author S. Livezey
     */
    private class ChameleonVisitor extends ModelElementVisitorAdapter {

        private String referringNamespace;

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
            referringNamespace = simple.getNamespace();
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
            referringNamespace = valueWithAttributes.getNamespace();
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(org.opentravel.schemacompiler.model.TLCoreObject)
         */
        @Override
        public boolean visitCoreObject(TLCoreObject coreObject) {
            referringNamespace = coreObject.getNamespace();
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(org.opentravel.schemacompiler.model.TLBusinessObject)
         */
        @Override
        public boolean visitBusinessObject(TLBusinessObject businessObject) {
            referringNamespace = businessObject.getNamespace();
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(org.opentravel.schemacompiler.model.TLOperation)
         */
        @Override
        public boolean visitOperation(TLOperation operation) {
            referringNamespace = operation.getNamespace();
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
         */
        @Override
        public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
            referringNamespace = extensionPointFacet.getNamespace();
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
         */
        @Override
        public boolean visitFacet(TLFacet facet) {
            referringNamespace = facet.getNamespace();
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            referringNamespace = simpleFacet.getNamespace();
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionFacet(org.opentravel.schemacompiler.model.TLActionFacet)
         */
        @Override
        public boolean visitActionFacet(TLActionFacet facet) {
            referringNamespace = facet.getNamespace();
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
         */
        @Override
        public boolean visitAttribute(TLAttribute attribute) {
            if (attribute.getOwner() != null) {
                referringNamespace = attribute.getOwner().getNamespace();
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            if (element.getOwner() != null) {
                referringNamespace = element.getOwner().getNamespace();
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
         */
        @Override
        public boolean visitLegacySchemaLibrary(XSDLibrary library) {
            return false; // Do not navigate - we are replacing this with the chameleon processing routine
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDSimpleType(org.opentravel.schemacompiler.model.XSDSimpleType)
         */
        @Override
        public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
            analyzeLegacySchemaDependencies( xsdSimple );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
         */
        @Override
        public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
            analyzeLegacySchemaDependencies( xsdComplex );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDElement(org.opentravel.schemacompiler.model.XSDElement)
         */
        @Override
        public boolean visitXSDElement(XSDElement xsdElement) {
            analyzeLegacySchemaDependencies( xsdElement );
            return true;
        }

        /**
         * If the owning library of the given entity is a legacy schema, launch the recusive analysis of its
         * dependencies.
         * 
         * @param member the library member to analyze
         */
        private void analyzeLegacySchemaDependencies(LibraryMember member) {
            if (referringNamespace != null) {
                AbstractLibrary library = member.getOwningLibrary();

                if (library instanceof XSDLibrary) {
                    navigateXSDLibraryDependencies( (XSDLibrary) library, referringNamespace,
                        new HashSet<XSDLibrary>() );
                }
            }
        }

    }

}
