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

package org.opentravel.schemacompiler.codegen.wsdl;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenNamespacePrefixMapper;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.xml.PrettyPrintLineBreakProcessor;
import org.opentravel.schemacompiler.xml.WSDLLineBreakProcessor;
import org.springframework.context.ApplicationContext;

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

/**
 * Abstract base class for all code generators capable of producing WSDL output.
 * 
 * @param <S> the source type for which output content will be generated
 * @author S. Livezey
 */
public abstract class AbstractWsdlCodeGenerator<S extends LibraryMember> extends AbstractJaxbCodeGenerator<S> {

    private static final String DEFAULT_JAXB_PACKAGES =
        ":org.xmlsoap.schemas.wsdl" + ":org.w3._2001.xmlschema" + ":org.opentravel.ns.ota2.appinfo_v01_00";

    private static Map<String,JAXBContext> contextCache = new HashMap<>();
    protected static Schema validationSchema;

    private List<AbstractLibrary> wsdlDependencies = new ArrayList<>();
    private TransformerFactory<CodeGenerationTransformerContext> transformerFactory;

    /**
     * Default constructor.
     */
    public AbstractWsdlCodeGenerator() {
        transformerFactory =
            TransformerFactory.getInstance( SchemaCompilerApplicationContext.WSDL_CODEGEN_TRANSFORMER_FACTORY,
                new CodeGenerationTransformerContext( this ) );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getTransformerFactory(org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected TransformerFactory<CodeGenerationTransformerContext> getTransformerFactory(
        CodeGenerationContext codegenContext) {
        transformerFactory.getContext().setCodegenContext( codegenContext );
        return transformerFactory;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(org.opentravel.schemacompiler.model.ModelElement)
     */
    @Override
    protected boolean isSupportedSourceObject(S source) {
        return (source != null);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#transformSourceObjectToJaxb(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected Object transformSourceObjectToJaxb(S source, CodeGenerationContext context)
        throws CodeGenerationException {
        ObjectTransformer<S,?,CodeGenerationTransformerContext> transformer =
            getTransformerFactory( context ).getTransformer( source, JAXBElement.class );
        AbstractLibrary library = getLibrary( source );

        if ((transformer != null) && (library != null)) {
            return transformer.transform( source );

        } else {
            String sourceType = (source == null) ? "UNKNOWN" : source.getClass().getSimpleName();
            throw new CodeGenerationException(
                "No object transformer available for model element of type " + sourceType );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected File getOutputFile(S source, CodeGenerationContext context) {
        if (source == null) {
            throw new NullPointerException( "Source model element cannot be null." );
        }
        AbstractLibrary library = getLibrary( source );
        URL libraryUrl = (library == null) ? null : library.getLibraryUrl();
        File outputFolder = getOutputFolder( context, libraryUrl );
        String filename = getFilenameBuilder().buildFilename( source, "wsdl" );

        return new File( outputFolder, filename );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getMarshaller(org.opentravel.schemacompiler.model.ModelElement,
     *      org.w3._2001.xmlschema.Schema)
     */
    @Override
    protected Marshaller getMarshaller(S source, org.w3._2001.xmlschema.Schema schema) throws JAXBException {
        Marshaller m = getJaxbContext().createMarshaller();

        m.setSchema( validationSchema );
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.setProperty( "com.sun.xml.bind.namespacePrefixMapper",
            new CodegenNamespacePrefixMapper( getLibrary( source ), true, this, schema ) );
        return m;
    }

    /**
     * Adds the given library to the list of dependencies for the WSDL document.
     * 
     * @param wsdlDependency the dependency to add
     */
    protected void addWsdlDependency(AbstractLibrary wsdlDependency) {
        if (wsdlDependency != null) {
            wsdlDependencies.add( wsdlDependency );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getFilter()
     */
    @Override
    public CodeGenerationFilter getFilter() {
        final CodeGenerationFilter delegateFilter = super.getFilter();

        return new CodeGenerationFilter() {

            /**
             * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processLibrary(org.opentravel.schemacompiler.model.AbstractLibrary)
             */
            @Override
            public boolean processLibrary(AbstractLibrary library) {
                return wsdlDependencies.contains( library )
                    || ((delegateFilter != null) && delegateFilter.processLibrary( library ));
            }

            /**
             * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processExtendedLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
             */
            @Override
            public boolean processExtendedLibrary(XSDLibrary legacySchema) {
                return (delegateFilter != null) && delegateFilter.processExtendedLibrary( legacySchema );
            }

            /**
             * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#processEntity(org.opentravel.schemacompiler.model.LibraryElement)
             */
            @Override
            public boolean processEntity(LibraryElement entity) {
                return (delegateFilter != null) && delegateFilter.processEntity( entity );
            }

            /**
             * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#addBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
             */
            @Override
            public void addBuiltInLibrary(BuiltInLibrary library) {
                if (delegateFilter != null) {
                    delegateFilter.addBuiltInLibrary( library );
                }
            }

        };
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getLineBreakProcessor()
     */
    @Override
    protected PrettyPrintLineBreakProcessor getLineBreakProcessor() {
        return new WSDLLineBreakProcessor();
    }

    /**
     * Returns a JAXB context to use for marshalling output file content.
     * 
     * @return JAXBContext
     */
    private static JAXBContext getJaxbContext() {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
        StringBuilder jaxbPackages = new StringBuilder( DEFAULT_JAXB_PACKAGES );

        if (appContext.containsBean( SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS )) {
            CodeGenerationWsdlBindings wsdlBindings = (CodeGenerationWsdlBindings) appContext
                .getBean( SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS );

            for (String jaxbPackage : wsdlBindings.getJaxbContextPackages()) {
                jaxbPackages.append( ":" ).append( jaxbPackage );
            }
        }
        return contextCache.computeIfAbsent( jaxbPackages.toString(), p -> newContext( p ) );
    }

    /**
     * Constructs a new JAXB context for the given context path. If the context cannot be constructed, an runtime
     * exception will be thrown.
     * 
     * @param contextPath the path for which to create the context
     * @return JAXBContext
     */
    private static JAXBContext newContext(String contextPath) {
        try {
            return JAXBContext.newInstance( contextPath );

        } catch (JAXBException e) {
            throw new IllegalArgumentException( "Error creating JAXB context for path: " + contextPath, e );
        }
    }

    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            InputStream schemaStream =
                SchemaDeclarations.WSDL_SCHEMA.getContent( CodeGeneratorFactory.XSD_TARGET_FORMAT );

            validationSchema = schemaFactory.newSchema( new StreamSource( schemaStream ) );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
