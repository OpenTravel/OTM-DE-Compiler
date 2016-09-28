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
package org.opentravel.schemacompiler.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.zip.CRC32;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.loader.impl.MultiVersionLibraryModuleLoader;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentationType;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Calculates the CRC value for user-defined libraries.
 * 
 * @author S. Livezey
 */
public class LibraryCrcCalculator {

    /**
     * Returns true if a CRC value is required for the given library. CRC's are required for all
     * libraries that are assigned a status later than 'DRAFT' in the library status lifecycle.
     * 
     * @param library
     *            the library instance to check
     * @return boolean
     */
    public static boolean isCrcRequired(TLLibrary library) {
        boolean crcRequired = false;

        if (library != null) {
        	TLLibraryStatus status = library.getStatus();
        	
        	crcRequired = (status != null)
        			&& (status.getRank() > TLLibraryStatus.DRAFT.getRank());
        }
        return crcRequired;
    }

    /**
     * Returns the CRC value for the given JAXB library if one is define. If a CRC is not specified
     * in the library content, this method will return null.
     * 
     * @param jaxbLibrary
     *            the JAXB library instance
     * @return Long
     */
    public static Long getLibraryCrcValue(Object jaxbLibrary) {
        Long crcValue = null;

        if (jaxbLibrary instanceof org.opentravel.ns.ota2.librarymodel_v01_06.Library) {
            crcValue = ((org.opentravel.ns.ota2.librarymodel_v01_06.Library) jaxbLibrary).getCrcValue();
            
        } else if (jaxbLibrary instanceof org.opentravel.ns.ota2.librarymodel_v01_05.Library) {
            crcValue = ((org.opentravel.ns.ota2.librarymodel_v01_05.Library) jaxbLibrary).getCrcValue();
            
        } else if (jaxbLibrary instanceof org.opentravel.ns.ota2.librarymodel_v01_04.Library) {
            crcValue = ((org.opentravel.ns.ota2.librarymodel_v01_04.Library) jaxbLibrary).getCrcValue();
        }
        return crcValue;
    }

    /**
     * Forces a recalculation of the library's CRC values and saves the file.
     * 
     * @param libraryFile
     *            the library file to be updated with a new CRC value
     * @throws LibraryLoaderException
     *             thrown if an unexpected exception occurs while attempting to load the contents of
     *             the library
     * @throws LibrarySaveException
     *             thrown if the content of the library cannot be re-saved
     * @throws IOException
     *             thrown if the file cannot be read from or updated
     */
    public static void recalculateLibraryCrc(File libraryFile) throws LibraryLoaderException,
            LibrarySaveException, IOException {
        if (!libraryFile.exists()) {
            throw new FileNotFoundException("The specified library file does not exist: "
                    + libraryFile.getAbsolutePath());
        }

        // Load the library as a JAXB data structure
        LibraryModuleLoader<InputStream> loader = new MultiVersionLibraryModuleLoader();
        LibraryModuleInfo<Object> moduleInfo = loader.loadLibrary(
        		new LibraryStreamInputSource(libraryFile), new ValidationFindings());
        Object jaxbLibrary = moduleInfo.getJaxbArtifact();

        if (jaxbLibrary == null) {
            throw new LibraryLoaderException(
                    "The specified file does not follow a valid OTM library file format.");
        }

        // Transform the JAXB data structure to a TLLibrary
        TransformerFactory<DefaultTransformerContext> transformerFactory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY,
                        new DefaultTransformerContext());
        ObjectTransformer<Object, TLLibrary, DefaultTransformerContext> transformer = transformerFactory
                .getTransformer(jaxbLibrary, TLLibrary.class);
        TLLibrary library = transformer.transform(jaxbLibrary);

        library.setLibraryUrl(URLUtils.toURL(libraryFile));

        // Re-save the file (forces re-calculation of the CRC)
        new LibraryModelSaver().saveLibrary(library);
    }

    /**
     * Calculates the CRC using the contents of the given library.
     * 
     * @param library
     *            the library for which to calculate the CRC
     * @return long
     */
    public static long calculate(TLLibrary library) {
        CrcVisitor visitor = new CrcVisitor();
        CRC32 crcCalculator = new CRC32();

        ModelNavigator.navigate(library, visitor);
        crcCalculator.update(visitor.getCrcData());
        return crcCalculator.getValue();
    }

    /**
     * Visitor implementation that compiles a concatenated string value that represents the contents
     * of the library for use with the CRC calculation.
     * 
     * @author S. Livezey
     */
    private static class CrcVisitor extends ModelElementVisitorAdapter {

        private StringBuilder crcData = new StringBuilder();

        /**
         * Returns the byte data collected from the library during navigation.
         * 
         * @return byte[]
         */
        public byte[] getCrcData() {
            byte[] crcBytes = null;
            try {
                crcBytes = crcData.toString().getBytes("UTF-8");

            } catch (UnsupportedEncodingException e) {
                // No error - UTF-8 encoding is supported by all platforms
            }
            return crcBytes;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitUserDefinedLibrary(org.opentravel.schemacompiler.model.TLLibrary)
         */
        @Override
        public boolean visitUserDefinedLibrary(TLLibrary library) {
            URL credentialsUrl = library.getAlternateCredentialsUrl();
            TLLibraryStatus status = library.getStatus();

            crcData.append(library.getName()).append('|');
            crcData.append(library.getNamespace()).append('|');
            crcData.append(library.getPrefix()).append('|');
            crcData.append(library.getVersionScheme()).append('|');
            crcData.append("0").append('|'); // backward-compatibility for the 'patchLevel' property
                                             // that was deleted
            crcData.append(library.getPreviousVersionUri()).append('|');
            crcData.append((credentialsUrl == null) ? null : credentialsUrl.toExternalForm());
            crcData.append((status == null) ? null : status.toString());
            crcData.append(library.getComments()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitContext(org.opentravel.schemacompiler.model.TLContext)
         */
        @Override
        public boolean visitContext(TLContext context) {
            crcData.append(context.getContextId()).append('|');
            crcData.append(context.getApplicationContext()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
            crcData.append(simple.getName()).append('|');
            crcData.append(simple.getParentTypeName()).append('|');
            crcData.append(simple.isListTypeInd()).append('|');
            crcData.append(simple.getPattern()).append('|');
            crcData.append(simple.getMinLength()).append('|');
            crcData.append(simple.getMaxLength()).append('|');
            crcData.append(simple.getFractionDigits()).append('|');
            crcData.append(simple.getTotalDigits()).append('|');
            crcData.append(simple.getMinInclusive()).append('|');
            crcData.append(simple.getMaxInclusive()).append('|');
            crcData.append(simple.getMinExclusive()).append('|');
            crcData.append(simple.getMaxExclusive()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
            crcData.append(valueWithAttributes.getName()).append('|');
            crcData.append(valueWithAttributes.getParentTypeName()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitClosedEnumeration(org.opentravel.schemacompiler.model.TLClosedEnumeration)
         */
        @Override
        public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
            crcData.append(enumeration.getName()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
         */
        @Override
        public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
            crcData.append(enumeration.getName()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitEnumValue(org.opentravel.schemacompiler.model.TLEnumValue)
         */
        @Override
        public boolean visitEnumValue(TLEnumValue enumValue) {
            crcData.append(enumValue.getLiteral()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitCoreObject(org.opentravel.schemacompiler.model.TLCoreObject)
         */
        @Override
        public boolean visitCoreObject(TLCoreObject coreObject) {
            crcData.append(coreObject.getName()).append('|');
            crcData.append(coreObject.isNotExtendable()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitRole(org.opentravel.schemacompiler.model.TLRole)
         */
        @Override
        public boolean visitRole(TLRole role) {
            crcData.append(role.getName()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitBusinessObject(org.opentravel.schemacompiler.model.TLBusinessObject)
         */
        @Override
        public boolean visitBusinessObject(TLBusinessObject businessObject) {
            crcData.append(businessObject.getName()).append('|');
            crcData.append(businessObject.isNotExtendable()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitService(org.opentravel.schemacompiler.model.TLService)
         */
        @Override
        public boolean visitService(TLService service) {
            crcData.append(service.getName()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitOperation(org.opentravel.schemacompiler.model.TLOperation)
         */
        @Override
        public boolean visitOperation(TLOperation operation) {
            crcData.append(operation.getName()).append('|');
            crcData.append(operation.isNotExtendable()).append('|');
            return true;
        }

        /**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitChoiceObject(org.opentravel.schemacompiler.model.TLChoiceObject)
		 */
		@Override
		public boolean visitChoiceObject(TLChoiceObject choiceObject) {
            crcData.append(choiceObject.getName()).append('|');
            crcData.append(choiceObject.isNotExtendable()).append('|');
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
		 */
		@Override
		public boolean visitResource(TLResource resource) {
            crcData.append(resource.getName()).append('|');
            crcData.append(resource.getBasePath()).append('|');
            crcData.append(resource.isAbstract()).append('|');
            crcData.append(resource.isFirstClass()).append('|');
            crcData.append(resource.getBusinessObjectRefName()).append('|');
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
		 */
		@Override
		public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
            crcData.append(parentRef.getParentResourceName()).append('|');
            crcData.append(parentRef.getParentParamGroupName()).append('|');
            crcData.append(parentRef.getPathTemplate()).append('|');
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
		 */
		@Override
		public boolean visitParamGroup(TLParamGroup paramGroup) {
            crcData.append(paramGroup.getName()).append('|');
            crcData.append(paramGroup.isIdGroup()).append('|');
            crcData.append(paramGroup.getFacetRefName()).append('|');
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParameter(org.opentravel.schemacompiler.model.TLParameter)
		 */
		@Override
		public boolean visitParameter(TLParameter parameter) {
            crcData.append(parameter.getFieldRefName()).append('|');
            crcData.append(parameter.getLocation()).append('|');
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAction(org.opentravel.schemacompiler.model.TLAction)
		 */
		@Override
		public boolean visitAction(TLAction action) {
            crcData.append(action.getActionId()).append('|');
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionRequest(org.opentravel.schemacompiler.model.TLActionRequest)
		 */
		@Override
		public boolean visitActionRequest(TLActionRequest actionRequest) {
            crcData.append(actionRequest.getHttpMethod()).append('|');
            crcData.append(actionRequest.getPathTemplate()).append('|');
            crcData.append(actionRequest.getParamGroupName()).append('|');
            crcData.append(actionRequest.getPayloadTypeName()).append('|');
            
            for (TLMimeType mimeType : actionRequest.getMimeTypes()) {
                crcData.append(mimeType).append('|');
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
		 */
		@Override
		public boolean visitActionResponse(TLActionResponse actionResponse) {
			for (Integer statusCode : actionResponse.getStatusCodes()) {
                crcData.append(statusCode).append('|');
			}
            crcData.append(actionResponse.getPayloadTypeName()).append('|');
            
            for (TLMimeType mimeType : actionResponse.getMimeTypes()) {
                crcData.append(mimeType).append('|');
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionFacet(org.opentravel.schemacompiler.model.TLActionFacet)
		 */
		@Override
		public boolean visitActionFacet(TLActionFacet facet) {
            crcData.append(facet.getName()).append('|');
            crcData.append(facet.getReferenceType()).append('|');
            crcData.append(facet.getReferenceFacetName()).append('|');
            crcData.append(facet.getReferenceRepeat()).append('|');
            crcData.append(facet.getBasePayloadName()).append('|');
            return true;
		}

		/**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
         */
        @Override
        public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
            crcData.append("TLExtensionPointFacet").append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
         */
		@Override
        public boolean visitFacet(TLFacet facet) {
            TLFacetType facetType = facet.getFacetType();

            crcData.append((facetType == null) ? null : facetType.toString()).append('|');
            crcData.append(facet.isNotExtendable()).append('|');
            return true;
        }

        /**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitContextualFacet(org.opentravel.schemacompiler.model.TLContextualFacet)
		 */
		@SuppressWarnings("deprecation")
		@Override
		public boolean visitContextualFacet(TLContextualFacet facet) {
            TLFacetType facetType = facet.getFacetType();

            crcData.append((facetType == null) ? null : facetType.toString()).append('|');
            crcData.append(facet.isNotExtendable()).append('|');
            crcData.append(facet.getContext()).append('|'); // required for backwards-compatibility
            crcData.append(facet.getName()).append('|');
            return true;
		}

		/**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            TLFacetType facetType = simpleFacet.getFacetType();

            crcData.append((facetType == null) ? null : facetType.toString()).append('|');
            crcData.append(simpleFacet.getSimpleTypeName()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitAlias(org.opentravel.schemacompiler.model.TLAlias)
         */
        @Override
        public boolean visitAlias(TLAlias alias) {
            crcData.append(alias.getName()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
         */
        @Override
        public boolean visitAttribute(TLAttribute attribute) {
            crcData.append(attribute.getName()).append('|');
            crcData.append(attribute.getTypeName()).append('|');
            crcData.append(attribute.isMandatory()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            crcData.append(element.getName()).append('|');
            crcData.append(element.getTypeName()).append('|');
            crcData.append(element.isReference()).append('|');
            crcData.append(element.getRepeat()).append('|');
            crcData.append(element.isMandatory()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitIndicator(org.opentravel.schemacompiler.model.TLIndicator)
         */
        @Override
        public boolean visitIndicator(TLIndicator indicator) {
            crcData.append(indicator.getName()).append('|');
            crcData.append(indicator.isPublishAsElement()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
         */
        @Override
        public boolean visitExtension(TLExtension extension) {
            crcData.append(extension.getExtendsEntityName()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitNamespaceImport(org.opentravel.schemacompiler.model.TLNamespaceImport)
         */
        @Override
        public boolean visitNamespaceImport(TLNamespaceImport nsImport) {
            crcData.append(nsImport.getPrefix()).append('|');
            crcData.append(nsImport.getNamespace()).append('|');

            if (nsImport.getFileHints() != null) {
                for (String fileHint : nsImport.getFileHints()) {
                    crcData.append(fileHint).append('|');
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitInclude(org.opentravel.schemacompiler.model.TLInclude)
         */
        @Override
        public boolean visitInclude(TLInclude include) {
            crcData.append(include.getPath()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
         */
        @Override
        public boolean visitEquivalent(TLEquivalent equivalent) {
            crcData.append(equivalent.getContext()).append('|');
            crcData.append(equivalent.getDescription()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitExample(org.opentravel.schemacompiler.model.TLExample)
         */
        @Override
        public boolean visitExample(TLExample example) {
            crcData.append(example.getContext()).append('|');
            crcData.append(example.getValue()).append('|');
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
         */
        @Override
        public boolean visitDocumentation(TLDocumentation documentation) {
            crcData.append(documentation.getDescription()).append('|');

            for (TLDocumentationItem docItem : documentation.getDeprecations()) {
                visitDocumentationItem(docItem);
            }
            for (TLDocumentationItem docItem : documentation.getReferences()) {
                visitDocumentationItem(docItem);
            }
            for (TLDocumentationItem docItem : documentation.getImplementers()) {
                visitDocumentationItem(docItem);
            }
            for (TLDocumentationItem docItem : documentation.getMoreInfos()) {
                visitDocumentationItem(docItem);
            }
            for (TLAdditionalDocumentationItem docItem : documentation.getOtherDocs()) {
                visitDocumentationItem(docItem);
                crcData.append(docItem.getContext()).append('|');
            }
            return true;
        }

        /**
         * Adds information to the CRC data for the given documentation item.
         * 
         * @param docItem
         *            the documentation item to process
         */
        private void visitDocumentationItem(TLDocumentationItem docItem) {
            TLDocumentationType docType = docItem.getType();

            crcData.append((docType == null) ? null : docType.toString()).append('|');
            crcData.append(docItem.getText()).append('|');
        }

    }

}
