/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.zip.CRC32;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.loader.LibraryLoaderException;
import com.sabre.schemacompiler.loader.LibraryModuleInfo;
import com.sabre.schemacompiler.loader.LibraryModuleLoader;
import com.sabre.schemacompiler.loader.impl.LibraryStreamInputSource;
import com.sabre.schemacompiler.loader.impl.MultiVersionLibraryModuleLoader;
import com.sabre.schemacompiler.model.TLAdditionalDocumentationItem;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLDocumentationItem;
import com.sabre.schemacompiler.model.TLDocumentationType;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLFacetType;
import com.sabre.schemacompiler.model.TLInclude;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.model.TLNamespaceImport;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.saver.LibraryModelSaver;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.TransformerFactory;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;
import com.sabre.schemacompiler.visitor.ModelNavigator;

/**
 * Calculates the CRC value for user-defined libraries.
 * 
 * @author S. Livezey
 */
public class LibraryCrcCalculator {
	
	/**
	 * Returns true if a CRC value is required for the given library.  CRC's are required for
	 * all libraries that are a member of a protected namespace, or are assigned a status of
	 * 'FINAL' (in any namespace).
	 * 
	 * @param library  the library instance to check
	 * @return boolean
	 */
	public static boolean isCrcRequired(TLLibrary library) {
		boolean crcRequired = false;
		
		if (library != null) {
			if (library.getStatus() == TLLibraryStatus.FINAL) {
				crcRequired = true;
				
			} else {
				crcRequired = ProtectedNamespaceRegistry.getInstance().
						isProtectedNamespace(library.getNamespace());
			}
		}
		return crcRequired;
	}
	
	/**
	 * Returns the CRC value for the given JAXB library if one is define.  If a CRC is not specified
	 * in the library content, this method will return null.
	 * 
	 * @param jaxbLibrary  the JAXB library instance
	 * @return Long
	 */
	public static Long getLibraryCrcValue(Object jaxbLibrary) {
		Long crcValue = null;
		
		if (jaxbLibrary instanceof org.opentravel.ns.ota2.librarymodel_v01_04.Library) {
			crcValue = ((org.opentravel.ns.ota2.librarymodel_v01_04.Library) jaxbLibrary).getCrcValue();
		}
		return crcValue;
	}
	
	/**
	 * Forces a recalculation of the library's CRC values and saves the file.
	 * 
	 * @param libraryFile  the library file to be updated with a new CRC value
	 * @throws LibraryLoaderException  thrown if an unexpected exception occurs while attempting
	 *								   to load the contents of the library
	 * @throws LibrarySaveException  thrown if the content of the library cannot be re-saved
	 * @throws IOException  thrown if the file cannot be read from or updated
	 */
	public static void recalculateLibraryCrc(File libraryFile) throws LibraryLoaderException, LibrarySaveException, IOException {
		if (!libraryFile.exists()) {
			throw new FileNotFoundException("The specified library file does not exist: " + libraryFile.getAbsolutePath());
		}
		
		// Load the library as a JAXB data structure
		LibraryModuleLoader<InputStream> loader = new MultiVersionLibraryModuleLoader();
		LibraryModuleInfo<Object> moduleInfo = loader.loadLibrary(new LibraryStreamInputSource(libraryFile), new ValidationFindings());
		Object jaxbLibrary = moduleInfo.getJaxbArtifact();
		
		if (jaxbLibrary == null) {
			throw new LibraryLoaderException("The specified file does not follow a valid OTM library file format.");
		}
		
		// Transform the JAXB data structure to a TLLibrary
		TransformerFactory<DefaultTransformerContext> transformerFactory =
				TransformerFactory.getInstance(SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY,
						new DefaultTransformerContext());
		ObjectTransformer<Object,TLLibrary,DefaultTransformerContext> transformer =
				transformerFactory.getTransformer(jaxbLibrary, TLLibrary.class);
		TLLibrary library = transformer.transform(jaxbLibrary);
		
		library.setLibraryUrl( URLUtils.toURL(libraryFile) );
		
		// Re-save the file (forces re-calculation of the CRC)
		new LibraryModelSaver().saveLibrary(library);
	}
	
	/**
	 * Calculates the CRC using the contents of the given library.
	 * 
	 * @param library  the library for which to calculate the CRC
	 * @return long
	 */
	public static long calculate(TLLibrary library) {
		CrcVisitor visitor = new CrcVisitor();
		CRC32 crcCalculator = new CRC32();
		
		ModelNavigator.navigate(library, visitor);
		crcCalculator.update( visitor.getCrcData() );
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
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitUserDefinedLibrary(com.sabre.schemacompiler.model.TLLibrary)
		 */
		@Override
		public boolean visitUserDefinedLibrary(TLLibrary library) {
			URL credentialsUrl = library.getAlternateCredentialsUrl();
			TLLibraryStatus status = library.getStatus();
			
			crcData.append( library.getName() ).append('|');
			crcData.append( library.getNamespace() ).append('|');
			crcData.append( library.getPrefix() ).append('|');
			crcData.append( library.getVersionScheme() ).append('|');
			crcData.append( "0" ).append('|'); // backward-compatibility for the 'patchLevel' property that was deleted
			crcData.append( library.getPreviousVersionUri() ).append('|');
			crcData.append( (credentialsUrl == null) ? null : credentialsUrl.toExternalForm() );
			crcData.append( (status == null) ? null : status.toString() );
			crcData.append( library.getComments() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitContext(com.sabre.schemacompiler.model.TLContext)
		 */
		@Override
		public boolean visitContext(TLContext context) {
			crcData.append( context.getContextId() ).append('|');
			crcData.append( context.getApplicationContext() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitSimple(com.sabre.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			crcData.append( simple.getName() ).append('|');
			crcData.append( simple.getParentTypeName() ).append('|');
			crcData.append( simple.isListTypeInd() ).append('|');
			crcData.append( simple.getPattern() ).append('|');
			crcData.append( simple.getMinLength() ).append('|');
			crcData.append( simple.getMaxLength() ).append('|');
			crcData.append( simple.getFractionDigits() ).append('|');
			crcData.append( simple.getTotalDigits() ).append('|');
			crcData.append( simple.getMinInclusive() ).append('|');
			crcData.append( simple.getMaxInclusive() ).append('|');
			crcData.append( simple.getMinExclusive() ).append('|');
			crcData.append( simple.getMaxExclusive() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
		 */
		@Override
		public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
			crcData.append( valueWithAttributes.getName() ).append('|');
			crcData.append( valueWithAttributes.getParentTypeName() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitClosedEnumeration(com.sabre.schemacompiler.model.TLClosedEnumeration)
		 */
		@Override
		public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
			crcData.append( enumeration.getName() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitOpenEnumeration(com.sabre.schemacompiler.model.TLOpenEnumeration)
		 */
		@Override
		public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
			crcData.append( enumeration.getName() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitEnumValue(com.sabre.schemacompiler.model.TLEnumValue)
		 */
		@Override
		public boolean visitEnumValue(TLEnumValue enumValue) {
			crcData.append( enumValue.getLiteral() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitCoreObject(com.sabre.schemacompiler.model.TLCoreObject)
		 */
		@Override
		public boolean visitCoreObject(TLCoreObject coreObject) {
			crcData.append( coreObject.getName() ).append('|');
			crcData.append( coreObject.isNotExtendable() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitRole(com.sabre.schemacompiler.model.TLRole)
		 */
		@Override
		public boolean visitRole(TLRole role) {
			crcData.append( role.getName() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitBusinessObject(com.sabre.schemacompiler.model.TLBusinessObject)
		 */
		@Override
		public boolean visitBusinessObject(TLBusinessObject businessObject) {
			crcData.append( businessObject.getName() ).append('|');
			crcData.append( businessObject.isNotExtendable() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitService(com.sabre.schemacompiler.model.TLService)
		 */
		@Override
		public boolean visitService(TLService service) {
			crcData.append( service.getName() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitOperation(com.sabre.schemacompiler.model.TLOperation)
		 */
		@Override
		public boolean visitOperation(TLOperation operation) {
			crcData.append( operation.getName() ).append('|');
			crcData.append( operation.isNotExtendable() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitExtensionPointFacet(com.sabre.schemacompiler.model.TLExtensionPointFacet)
		 */
		@Override
		public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
			crcData.append( "TLExtensionPointFacet" ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitFacet(com.sabre.schemacompiler.model.TLFacet)
		 */
		@Override
		public boolean visitFacet(TLFacet facet) {
			TLFacetType facetType = facet.getFacetType();
			
			crcData.append( (facetType == null) ? null : facetType.toString() ).append('|');
			crcData.append( facet.isNotExtendable() ).append('|');
			
			if ((facetType != null) && facetType.isContextual()) {
				crcData.append( facet.getContext() ).append('|');
				crcData.append( facet.getLabel() ).append('|');
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
		 */
		@Override
		public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
			TLFacetType facetType = simpleFacet.getFacetType();
			
			crcData.append( (facetType == null) ? null : facetType.toString() ).append('|');
			crcData.append( simpleFacet.getSimpleTypeName() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitAlias(com.sabre.schemacompiler.model.TLAlias)
		 */
		@Override
		public boolean visitAlias(TLAlias alias) {
			crcData.append( alias.getName() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitAttribute(com.sabre.schemacompiler.model.TLAttribute)
		 */
		@Override
		public boolean visitAttribute(TLAttribute attribute) {
			crcData.append( attribute.getName() ).append('|');
			crcData.append( attribute.getTypeName() ).append('|');
			crcData.append( attribute.isMandatory() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitElement(com.sabre.schemacompiler.model.TLProperty)
		 */
		@Override
		public boolean visitElement(TLProperty element) {
			crcData.append( element.getName() ).append('|');
			crcData.append( element.getTypeName() ).append('|');
			crcData.append( element.isReference() ).append('|');
			crcData.append( element.getRepeat() ).append('|');
			crcData.append( element.isMandatory() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitIndicator(com.sabre.schemacompiler.model.TLIndicator)
		 */
		@Override
		public boolean visitIndicator(TLIndicator indicator) {
			crcData.append( indicator.getName() ).append('|');
			crcData.append( indicator.isPublishAsElement() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitExtension(com.sabre.schemacompiler.model.TLExtension)
		 */
		@Override
		public boolean visitExtension(TLExtension extension) {
			crcData.append( extension.getExtendsEntityName() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitNamespaceImport(com.sabre.schemacompiler.model.TLNamespaceImport)
		 */
		@Override
		public boolean visitNamespaceImport(TLNamespaceImport nsImport) {
			crcData.append( nsImport.getPrefix() ).append('|');
			crcData.append( nsImport.getNamespace() ).append('|');
			
			if (nsImport.getFileHints() != null) {
				for (String fileHint : nsImport.getFileHints()) {
					crcData.append( fileHint ).append('|');
				}
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitInclude(com.sabre.schemacompiler.model.TLInclude)
		 */
		@Override
		public boolean visitInclude(TLInclude include) {
			crcData.append( include.getPath() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitEquivalent(com.sabre.schemacompiler.model.TLEquivalent)
		 */
		@Override
		public boolean visitEquivalent(TLEquivalent equivalent) {
			crcData.append( equivalent.getContext() ).append('|');
			crcData.append( equivalent.getDescription() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitExample(com.sabre.schemacompiler.model.TLExample)
		 */
		@Override
		public boolean visitExample(TLExample example) {
			crcData.append( example.getContext() ).append('|');
			crcData.append( example.getValue() ).append('|');
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitor#visitDocumentation(com.sabre.schemacompiler.model.TLDocumentation)
		 */
		@Override
		public boolean visitDocumentation(TLDocumentation documentation) {
			crcData.append( documentation.getDescription() ).append('|');
			
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
				crcData.append( docItem.getContext() ).append('|');
			}
			return true;
		}
		
		/**
		 * Adds information to the CRC data for the given documentation item.
		 * 
		 * @param docItem  the documentation item to process
		 */
		private void visitDocumentationItem(TLDocumentationItem docItem) {
			TLDocumentationType docType = docItem.getType();
			
			crcData.append( (docType == null) ? null : docType.toString() ).append('|');
			crcData.append( docItem.getText() ).append('|');
		}
		
	}
	
}
