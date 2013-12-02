/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.wsdl;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.context.ApplicationContext;

import com.sabre.schemacompiler.codegen.CodeGenerationContext;
import com.sabre.schemacompiler.codegen.CodeGenerationException;
import com.sabre.schemacompiler.codegen.CodeGenerationFilter;
import com.sabre.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.codegen.impl.CodegenNamespacePrefixMapper;
import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.ioc.SchemaDeclarations;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.TransformerFactory;
import com.sabre.schemacompiler.xml.PrettyPrintLineBreakProcessor;
import com.sabre.schemacompiler.xml.WSDLLineBreakProcessor;

/**
 * Abstract base class for all code generators capable of producing WSDL output.
 * 
 * @param <S>  the source type for which output content will be generated
 * @author S. Livezey
 */
public abstract class AbstractWsdlCodeGenerator<S extends LibraryMember> extends AbstractJaxbCodeGenerator<S> {
	
	private static final String DEFAULT_JAXB_PACKAGES =
		":org.xmlsoap.schemas.wsdl" +
		":org.w3._2001.xmlschema" +
		":org.opentravel.ns.ota2.appinfo_v01_00";
	
	private static Map<String,JAXBContext> contextCache = new HashMap<String,JAXBContext>();
	protected static Schema validationSchema;
	
	private List<AbstractLibrary> wsdlDependencies = new ArrayList<AbstractLibrary>();
	private TransformerFactory<CodeGenerationTransformerContext> transformerFactory;
	
	/**
	 * Default constructor.
	 */
	public AbstractWsdlCodeGenerator() {
		transformerFactory = TransformerFactory.getInstance(
				SchemaCompilerApplicationContext.WSDL_CODEGEN_TRANSFORMER_FACTORY, new CodeGenerationTransformerContext(this));
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getTransformerFactory(com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected TransformerFactory<CodeGenerationTransformerContext> getTransformerFactory(CodeGenerationContext codegenContext) {
		transformerFactory.getContext().setCodegenContext(codegenContext);
		return transformerFactory;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(java.lang.Object)
	 */
	@Override
	protected boolean isSupportedSourceObject(S source) {
		return (source != null);
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#transformSourceObjectToJaxb(java.lang.Object, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected Object transformSourceObjectToJaxb(S source, CodeGenerationContext context) throws CodeGenerationException {
		ObjectTransformer<S,?,CodeGenerationTransformerContext> transformer =
				getTransformerFactory(context).getTransformer(source, JAXBElement.class);
		AbstractLibrary library = getLibrary(source);
		
		if ((transformer != null) && (library != null)) {
			return transformer.transform(source);
			
		} else {
			String sourceType = (source == null) ? "UNKNOWN" : source.getClass().getSimpleName();
			throw new CodeGenerationException("No object transformer available for model element of type " + sourceType);
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(com.sabre.schemacompiler.model.TLModelElement, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected File getOutputFile(S source, CodeGenerationContext context) {
		if (source == null) {
			throw new NullPointerException("Source model element cannot be null.");
		}
		AbstractLibrary library = getLibrary(source);
		URL libraryUrl = (library == null) ? null : library.getLibraryUrl();
		File outputFolder = getOutputFolder(context, libraryUrl);
		String filename = getFilenameBuilder().buildFilename(source, "wsdl");
		
		return new File(outputFolder, filename);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getMarshaller(com.sabre.schemacompiler.model.TLModelElement, org.w3._2001.xmlschema.Schema)
	 */
	@Override
	protected Marshaller getMarshaller(S source, org.w3._2001.xmlschema.Schema schema) throws JAXBException {
		Marshaller m = getJaxbContext().createMarshaller();
		
		m.setSchema(validationSchema);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
				new CodegenNamespacePrefixMapper(getLibrary(source), true, this, schema));
		return m;
	}
	
	/**
	 * Adds the given library to the list of dependencies for the WSDL document.
	 * 
	 * @param wsdlDependency  the dependency to add
	 */
	protected void addWsdlDependency(AbstractLibrary wsdlDependency) {
		if (wsdlDependency != null) {
			wsdlDependencies.add( wsdlDependency );
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getFilter()
	 */
	@Override
	public CodeGenerationFilter getFilter() {
		final CodeGenerationFilter delegateFilter = super.getFilter();
		
		return new CodeGenerationFilter() {

			/**
			 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#processLibrary(com.sabre.schemacompiler.model.AbstractLibrary)
			 */
			@Override
			public boolean processLibrary(AbstractLibrary library) {
				return wsdlDependencies.contains(library) ||
						((delegateFilter != null) && delegateFilter.processLibrary(library));
			}

			/**
			 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#processExtendedLibrary(com.sabre.schemacompiler.model.XSDLibrary)
			 */
			@Override
			public boolean processExtendedLibrary(XSDLibrary legacySchema) {
				return (delegateFilter != null) && delegateFilter.processExtendedLibrary(legacySchema);
			}

			/**
			 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#processEntity(com.sabre.schemacompiler.model.LibraryElement)
			 */
			@Override
			public boolean processEntity(LibraryElement entity) {
				return (delegateFilter != null) && delegateFilter.processEntity(entity);
			}

			/**
			 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#addBuiltInLibrary(com.sabre.schemacompiler.model.BuiltInLibrary)
			 */
			@Override
			public void addBuiltInLibrary(BuiltInLibrary library) {
				if (delegateFilter != null) {
					delegateFilter.addBuiltInLibrary(library);
				}
			}
			
		};
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getLineBreakProcessor()
	 */
	@Override
	protected PrettyPrintLineBreakProcessor getLineBreakProcessor() {
		return new WSDLLineBreakProcessor();
	}

	/**
	 * Returns a JAXB context to use for marshalling output file content.
	 * 
	 * @return JAXBContext
	 * @throws JAXBException  thrown if the required context cannot be created
	 */
	private static JAXBContext getJaxbContext() throws JAXBException {
		ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
		StringBuilder jaxbPackages = new StringBuilder(DEFAULT_JAXB_PACKAGES);
		
		if (appContext.containsBean(SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS)) {
			CodeGenerationWsdlBindings wsdlBindings = (CodeGenerationWsdlBindings) appContext.getBean(
					SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS);
			
			for (String jaxbPackage : wsdlBindings.getJaxbContextPackages()) {
				jaxbPackages.append(":").append(jaxbPackage);
			}
		}
		String contextPath = jaxbPackages.toString();
		JAXBContext jaxbContext = contextCache.get(contextPath);
		
		if (jaxbContext == null) {
			jaxbContext = JAXBContext.newInstance(contextPath);
			contextCache.put(contextPath, jaxbContext);
		}
		return jaxbContext;
	}

	/**
	 * Initializes the validation schema and shared JAXB context.
	 */
	static {
		try {
    		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    		InputStream schemaStream = SchemaDeclarations.WSDL_SCHEMA.getContent();
    		
    		validationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
