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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Abstract base class that provides common methods used to manage library imports and includes.
 * 
 * @param <E>
 *            the event type that this listener is designed to process
 * @param <S>
 *            the source object type for the events to be processed by this listener
 * @author S. Livezey
 */
public abstract class ImportManagementIntegrityChecker<E extends ModelEvent<S>, S> extends
        AbstractIntegrityChecker<E, S> {

    /**
     * Verifies the imports and includes of the originating library, ensuring that imports or
     * includes are defined for all referenced libraries. Any existing imports/includes that are no
     * longer required are deleted by this method.
     * 
     * @param originatingLibrary
     *            the originating library whose imports and includes will be verified
     */
    public static void verifyReferencedLibraries(TLLibrary originatingLibrary) {
        verifyReferencedLibraries(originatingLibrary, new HashMap<AbstractLibrary, URL>());
    }

    /**
     * Verifies the imports and includes of the originating library, ensuring that imports or
     * includes are defined for all referenced libraries. Any existing imports/includes that are no
     * longer required are deleted by this method.
     * 
     * <p>
     * NOTE: The 'libraryUrlOverride' map that is passed to this method allows the caller to
     * override the current location of zero or more libraries in the current model. This is
     * sometimes necessary when the locations of these libraries are about to change (e.g. just
     * before one or more libraries are published to a repository).
     * 
     * @param originatingLibrary
     *            the originating library whose imports and includes will be verified
     * @param libraryUrlOverrides
     *            map that allows the caller to override the current URL location of various
     *            libraries
     */
    public static void verifyReferencedLibraries(TLLibrary originatingLibrary,
            Map<AbstractLibrary, URL> libraryUrlOverrides) {
        List<AbstractLibrary> referencedLibraries = getReferencedLibraries((TLLibrary) originatingLibrary);
        Map<String, List<String>> importedFileHints = new HashMap<String, List<String>>();
        List<AbstractLibrary> includedLibs = new ArrayList<AbstractLibrary>();

        // First, check each existing import/include to make sure we still need it
        for (AbstractLibrary referencedLib : referencedLibraries) {
            if (referencedLib.getNamespace() != null) {
                if (referencedLib.getNamespace().equals(originatingLibrary.getNamespace())) {
                    includedLibs.add(referencedLib);
                } else {
                    List<String> importList = importedFileHints.get(referencedLib.getNamespace());

                    if (importList == null) {
                        importList = new ArrayList<String>();
                        importedFileHints.put(referencedLib.getNamespace(), importList);
                    }
                    importList.add(getReferenceUrl(originatingLibrary, referencedLib,
                            libraryUrlOverrides));
                }
            }
        }
        removeUnneededImports(originatingLibrary, importedFileHints);
        removeUnneededIncludes(originatingLibrary, includedLibs, libraryUrlOverrides);

        // Next, check each library to make sure we have an import (or include) to reference it
        for (AbstractLibrary referencedLib : referencedLibraries) {
            if (referencedLib.getNamespace() != null) {
                if (referencedLib.getNamespace().equals(originatingLibrary.getNamespace())) {
                    verifyLibraryInclude(originatingLibrary, referencedLib, libraryUrlOverrides);
                } else {
                    verifyLibraryImport(originatingLibrary, referencedLib, libraryUrlOverrides);
                }
            }
        }
    }

    /**
     * Verifies that the owning library defines a namespace import (and file hint) for the specified
     * reference library. If one does not exist, it is added automatically.
     * 
     * @param owningLibrary
     *            the owning library whose imports are to be verified
     * @param referencedLibrary
     *            the referenced library that requires an import declaration
     * @param libraryUrlOverrides
     *            map that allows the caller to override the current URL location of various
     *            libraries
     */
    private static void verifyLibraryImport(TLLibrary owningLibrary,
            AbstractLibrary referencedLibrary, Map<AbstractLibrary, URL> libraryUrlOverrides) {
        String referencedNamespace = referencedLibrary.getNamespace();

        if (referencedNamespace != null) {
            TLNamespaceImport importReference = null;

            // If we don't have an existing import for the namespace, create one automatically
            for (TLNamespaceImport nsImport : owningLibrary.getNamespaceImports()) {
                if (referencedNamespace.equals(nsImport.getNamespace())) {
                    importReference = nsImport;
                    break;
                }
            }
            if (importReference == null) { // Add a new namespace for this import
                importReference = new TLNamespaceImport(getUniquePrefix(owningLibrary,
                        referencedLibrary), referencedNamespace);
                owningLibrary.addNamespaceImport(importReference);
            }

            // Check to make sure we have a file hint for the referenced library
            if (!(referencedLibrary instanceof BuiltInLibrary)) {
                String relativeUrl = getReferenceUrl(owningLibrary, referencedLibrary,
                        libraryUrlOverrides);
                boolean hasFileHint = false;

                for (String fileHint : importReference.getFileHints()) {
                    if (relativeUrl.equals(fileHint)) {
                        hasFileHint = true;
                        break;
                    }
                }
                if (!hasFileHint) {
                    importReference.getFileHints().add(relativeUrl);
                }
            }
        }
    }

    /**
     * Verifies that the owning library defines an include (and file hint) for the specified
     * reference library. If one does not exist, it is added automatically.
     * 
     * @param owningLibrary
     *            the owning library whose includes are to be verified
     * @param referencedLibrary
     *            the referenced library that requires an include declaration
     * @param libraryUrlOverrides
     *            map that allows the caller to override the current URL location of various
     *            libraries
     */
    private static void verifyLibraryInclude(TLLibrary owningLibrary,
            AbstractLibrary referencedLibrary, Map<AbstractLibrary, URL> libraryUrlOverrides) {
        String relativeUrl = getReferenceUrl(owningLibrary, referencedLibrary, libraryUrlOverrides);
        boolean hasExistingImport = false;

        for (TLInclude include : owningLibrary.getIncludes()) {
            if (relativeUrl.equals(include.getPath())) {
                hasExistingImport = true;
                break;
            }
        }
        if (!hasExistingImport) {
            TLInclude include = new TLInclude();

            include.setPath(relativeUrl);
            owningLibrary.addInclude(include);
        }
    }

    /**
     * Removes all namespace imports and file hints that do not reference one of the given
     * namespaces and libraries.
     * 
     * @param owningLibrary
     *            the owning library whose imports are to be processed
     * @param importedFileHints
     *            the map of imported namespaces to the relative URL's of the libararies assigned to
     *            those namespaces
     */
    private static void removeUnneededImports(TLLibrary owningLibrary,
            Map<String, List<String>> importedFileHints) {
        List<TLNamespaceImport> importList = new ArrayList<TLNamespaceImport>(
                owningLibrary.getNamespaceImports());

        for (TLNamespaceImport nsImport : importList) {
            if (importedFileHints.containsKey(nsImport.getNamespace())) {
                // The import is needed, now verify that the file hint is still required
                List<String> requiredFileHints = importedFileHints.get(nsImport.getNamespace());
                Iterator<String> iterator = nsImport.getFileHints().iterator();

                while (iterator.hasNext()) {
                    String fileHint = iterator.next();

                    if (!requiredFileHints.contains(fileHint)) {
                        iterator.remove();
                    }
                }
            } else {
                // The namespace import is no longer required
                owningLibrary.removeNamespaceImport(nsImport);
            }

        }
    }

    /**
     * Removes all library includes that do not reference one of the given libraries.
     * 
     * @param owningLibrary
     *            the owning library whose includes are to be processed
     * @param includedLibs
     *            the list of included libraries assigned to the same namespace as the owning
     *            library
     * @param libraryUrlOverrides
     *            map that allows the caller to override the current URL location of various
     *            libraries
     */
    private static void removeUnneededIncludes(TLLibrary owningLibrary,
            List<AbstractLibrary> includedLibs, Map<AbstractLibrary, URL> libraryUrlOverrides) {
        List<TLInclude> includeList = new ArrayList<TLInclude>(owningLibrary.getIncludes());
        List<String> requiredIncludeUrls = new ArrayList<String>();

        // First build the list of required library URL's
        for (AbstractLibrary includedLib : includedLibs) {
            requiredIncludeUrls
                    .add(getReferenceUrl(owningLibrary, includedLib, libraryUrlOverrides));
        }

        // Ensure each include is still required and remove if it is not
        for (TLInclude include : includeList) {
            if (!requiredIncludeUrls.contains(include.getPath())) {
                owningLibrary.removeInclude(include);
            }
        }
    }

    /**
     * Returns a list of all libraries whose entities are referenced by entities in the given
     * user-defined library.
     * 
     * @param library
     *            the library to analyze
     * @return List<AbstractLibrary>
     */
    public static List<AbstractLibrary> getReferencedLibraries(TLLibrary library) {
        ReferencedLibraryVisitor visitor = new ReferencedLibraryVisitor(library);

        new ModelNavigator(visitor).navigateUserDefinedLibrary(library);
        return visitor.getReferencedLibraries();
    }

    /**
     * Constructs a URL that can be used by the originating library to reference the target library
     * as an import or include location. If the referenced library is repository-managed, the
     * resulting URL will be an OTM repository URI. If the referenced library is unmanaged, the URL
     * will be a relative file path.
     * 
     * @param originatingLibrary
     *            the library whose location is the originating point of the relative reference
     * @param referencedLibrary
     *            the library that is being imported or included by the originator
     * @param libraryUrlOverrides
     *            map that allows the caller to override the current URL location of various
     *            libraries
     * @return String
     */
    private static String getReferenceUrl(TLLibrary originatingLibrary,
            AbstractLibrary referencedLibrary, Map<AbstractLibrary, URL> libraryUrlOverrides) {
        ProjectManager projectManager = ProjectManager.getProjectManager(originatingLibrary
                .getOwningModel());
        URL originatingLibraryUrl, referencedLibraryUrl;
        String referenceUrl = null;

        // Lookup the correct URLs for the originating and referenced libraries
        if (libraryUrlOverrides.containsKey(originatingLibrary)) {
            originatingLibraryUrl = libraryUrlOverrides.get(originatingLibrary);
        } else {
            originatingLibraryUrl = originatingLibrary.getLibraryUrl();
        }

        if (libraryUrlOverrides.containsKey(referencedLibrary)) {
            referencedLibraryUrl = libraryUrlOverrides.get(referencedLibrary);
        } else {
            referencedLibraryUrl = referencedLibrary.getLibraryUrl();
        }

        // Hints for managed files are repository item URI's
        if ((projectManager != null) && projectManager.isRepositoryUrl(referencedLibraryUrl)) {
            ProjectItem item = projectManager.getProjectItem(referencedLibrary);

            if (item != null) {
                referenceUrl = RepositoryUtils.newURI(item, false).toString();
            }
        }

        // Hints for unmanaged files are relative URL's from the originating library's location
        if (referenceUrl == null) {
            referenceUrl = URLUtils.getRelativeURL(originatingLibraryUrl, referencedLibraryUrl,
                    true);
        }
        return referenceUrl;
    }

    /**
     * Calculates a unique prefix to identify the reference library's namespace within the owning
     * library.
     * 
     * @param owningLibrary
     *            the owning library to which the namespace import will be added
     * @param referencedLibrary
     *            the referenced library for which to create a prefix
     * @return String
     */
    private static String getUniquePrefix(TLLibrary owningLibrary, AbstractLibrary referencedLibrary) {
        // Find the preferred prefix for the imported library's namespace
        String prefix = referencedLibrary.getPrefix();

        if (prefix == null) {
            prefix = "ns1";
        }

        // Check to see if this prefix is already used for an existing import
        boolean isUnique = isUniquePrefix(prefix, owningLibrary);

        if (!isUnique) {
            String basePrefix = getBasePrefix(prefix);
            int counter = 1;

            while (!isUnique) {
                prefix = basePrefix + counter;
                isUnique = isUniquePrefix(prefix, owningLibrary);
                counter++;
            }
        }
        return prefix;
    }

    /**
     * Returns the base prefix by removing any trailing numbers from the string provided.
     * 
     * @param prefix
     *            the prefix string to process
     * @return String
     */
    private static String getBasePrefix(String prefix) {
        String basePrefix = prefix;

        while ((basePrefix.length() > 0)
                && Character.isDigit(basePrefix.charAt(basePrefix.length() - 1))) {
            basePrefix = basePrefix.substring(0, basePrefix.length() - 1);
        }
        if (basePrefix.length() == 0) {
            basePrefix = "ns1";
        }
        return basePrefix;
    }

    /**
     * Returns true if the given prefix is not used for any of the namespace imports defined in the
     * given library.
     * 
     * @param prefix
     *            the prefix to search for
     * @param library
     *            the library whose imports are to be searched
     * @return boolean
     */
    private static boolean isUniquePrefix(String prefix, TLLibrary library) {
        boolean isUnique = true;

        for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
            if (prefix.equals(nsImport.getPrefix())) {
                isUnique = false;
                break;
            }
        }
        return isUnique;
    }

    /**
     * Visitor that collects a list of all libraries whose entities are referenced by members of the
     * referencing library.
     * 
     * @author S. Livezey
     */
    private static class ReferencedLibraryVisitor extends ModelElementVisitorAdapter {

        private List<AbstractLibrary> referencedLibraries = new ArrayList<AbstractLibrary>();
        private TLLibrary referencingLibrary;

        /**
         * Constructor that specifies the referencing library to be anlayzed.
         * 
         * @param referencingLibrary
         */
        public ReferencedLibraryVisitor(TLLibrary referencingLibrary) {
            this.referencingLibrary = referencingLibrary;
        }

        /**
         * Returns the list of libraries returned during the traversal of the referencing library.
         * 
         * @return List<AbstractLibrary>
         */
        public List<AbstractLibrary> getReferencedLibraries() {
            return referencedLibraries;
        }

        /**
         * Ensures that the given library has been added to the list (as long as it is not the
         * owning library instance).
         * 
         * @param referencedLibrary
         *            the referenced library to add
         */
        private void addReferencedLibrary(AbstractLibrary referencedLibrary) {
            if ((referencedLibrary != null) && (referencedLibrary != referencingLibrary)
                    && !referencedLibraries.contains(referencedLibrary)) {
                referencedLibraries.add(referencedLibrary);
            }
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
            if (simple.getParentType() != null) {
                addReferencedLibrary(simple.getParentType().getOwningLibrary());
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
            if (valueWithAttributes.getParentType() != null) {
                addReferencedLibrary(valueWithAttributes.getParentType().getOwningLibrary());
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
         */
        @Override
        public boolean visitExtension(TLExtension extension) {
            if (extension.getExtendsEntity() != null) {
                addReferencedLibrary(extension.getExtendsEntity().getOwningLibrary());
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            if (simpleFacet.getSimpleType() != null) {
                addReferencedLibrary(simpleFacet.getSimpleType().getOwningLibrary());
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
         */
        @Override
        public boolean visitAttribute(TLAttribute attribute) {
            if (attribute.getType() != null) {
                addReferencedLibrary(attribute.getType().getOwningLibrary());
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            if (element.getType() != null) {
                addReferencedLibrary(element.getType().getOwningLibrary());
            }
            return true;
        }

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
		 */
		@Override
		public boolean visitResource(TLResource resource) {
            if (resource.getBusinessObjectRef() != null) {
                addReferencedLibrary(resource.getBusinessObjectRef().getOwningLibrary());
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
		 */
		@Override
		public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
            if (parentRef.getParentResource() != null) {
                addReferencedLibrary(parentRef.getParentResource().getOwningLibrary());
            }
            if (parentRef.getParentParamGroup() != null) {
                addReferencedLibrary(parentRef.getParentParamGroup().getOwningLibrary());
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
		 */
		@Override
		public boolean visitParamGroup(TLParamGroup paramGroup) {
            if (paramGroup.getFacetRef() != null) {
                addReferencedLibrary(paramGroup.getFacetRef().getOwningLibrary());
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParameter(org.opentravel.schemacompiler.model.TLParameter)
		 */
		@Override
		public boolean visitParameter(TLParameter parameter) {
            if (parameter.getFieldRef() != null) {
                addReferencedLibrary(((LibraryMember) parameter.getFieldRef()).getOwningLibrary());
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionRequest(org.opentravel.schemacompiler.model.TLActionRequest)
		 */
		@Override
		public boolean visitActionRequest(TLActionRequest actionRequest) {
            if (actionRequest.getParamGroup() != null) {
                addReferencedLibrary(actionRequest.getParamGroup().getOwningLibrary());
            }
            if (actionRequest.getActionFacet() != null) {
                addReferencedLibrary(actionRequest.getActionFacet().getOwningLibrary());
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
		 */
		@Override
		public boolean visitActionResponse(TLActionResponse actionResponse) {
            if (actionResponse.getActionFacet() != null) {
                addReferencedLibrary(actionResponse.getActionFacet().getOwningLibrary());
            }
            return true;
		}

    }

}
