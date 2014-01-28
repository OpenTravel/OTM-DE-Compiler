/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.wsdl;

import javax.xml.bind.JAXBElement;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.LibraryMemberTrimmedFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryTrimmedFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TDocumentation;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TPortType;

/**
 * Performs the translation from <code>TLService</code> objects to the JAXB nodes used
 * to produce the WSDL output.
 * 
 * @author S. Livezey
 */
public class TLServiceCodegenTransformer extends AbstractWsdlTransformer<TLService,JAXBElement<?>> {

	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public JAXBElement<?> transform(TLService source) {
		ObjectTransformer<TLOperation,CodegenArtifacts,CodeGenerationTransformerContext> opTransformer =
				getTransformerFactory().getTransformer(TLOperation.class, CodegenArtifacts.class);
		CodeGenerationFilenameBuilder<LibraryMember> filenameBuilder;
		CodeGenerationContext cgContext = context.getCodegenContext();
		CodegenArtifacts operationArtifacts = new CodegenArtifacts();
		TDefinitions definitions = new TDefinitions();
		
		if (cgContext.getValue(CodeGenerationContext.CK_PROJECT_FILENAME) != null) {
			filenameBuilder = new ProjectLibraryMemberTrimmedFilenameBuilder();
		} else {
			filenameBuilder = new LibraryMemberTrimmedFilenameBuilder<LibraryMember>(source);
		}
		
		definitions.setName(source.getName());
		definitions.setTargetNamespace( getTargetNamespace(source) );
		definitions.getAnyTopLevelOptionalElement().add( createTypes(source, filenameBuilder) );
		
		// Collect the artifacts for the operations of this service, and insert them into the
		// appropriate part of the WSDL document
		String portTypeName = source.getName() + "PortType";
		TPortType portType = new TPortType();
		
		for (TLOperation operation : getInheritedOperations(source)) {
			operationArtifacts.addAllArtifacts( opTransformer.transform(operation) );
		}
		for (TMessage message : operationArtifacts.getArtifactsOfType(TMessage.class)) {
			definitions.getAnyTopLevelOptionalElement().add(message);
		}
		for (TOperation operation : operationArtifacts.getArtifactsOfType(TOperation.class)) {
			portType.getOperation().add(operation);
		}
		if (source.getDocumentation() != null) {
			ObjectTransformer<TLDocumentation,TDocumentation,CodeGenerationTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, TDocumentation.class);
			
			portType.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		portType.setName(portTypeName);
		definitions.getAnyTopLevelOptionalElement().add(portType);
		
		// Create the binding and service definition for the WSDL document
		addBindingAndService(definitions, portType, operationArtifacts.getArtifactsOfType(TMessage.class),
				context.getCodegenContext());
		
		return wsdlObjectFactory.createDefinitions(definitions);
	}
	
	/**
	 * Handles the generation of library filenames for WSDL imports.
	 */
	private class ProjectLibraryMemberTrimmedFilenameBuilder  implements CodeGenerationFilenameBuilder<LibraryMember> {
		
		private CodeGenerationFilenameBuilder<AbstractLibrary> delegateFilenameBuilder;
		
		/**
		 * Default constructor.
		 */
		public ProjectLibraryMemberTrimmedFilenameBuilder() {
			delegateFilenameBuilder = new LibraryTrimmedFilenameBuilder( null );
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(java.lang.Object, java.lang.String)
		 */
		@Override
		public String buildFilename(LibraryMember item, String fileExtension) {
			return delegateFilenameBuilder.buildFilename(item.getOwningLibrary(), fileExtension);
		}
		
	}
	
}
