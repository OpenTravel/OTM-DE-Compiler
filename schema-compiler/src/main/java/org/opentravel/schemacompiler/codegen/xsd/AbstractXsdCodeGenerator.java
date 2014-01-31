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
package org.opentravel.schemacompiler.codegen.xsd;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.DependencyFilterBuilder;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.xml.PrettyPrintLineBreakProcessor;
import org.opentravel.schemacompiler.xml.XMLSchemaLineBreakProcessor;

/**
 * Abstract base class for all code generators capable of producing XSD output.
 * 
 * @param <S>
 *            the source type for which output content will be generated
 * @author S. Livezey
 */
public abstract class AbstractXsdCodeGenerator<S extends TLModelElement> extends
        AbstractJaxbCodeGenerator<S> {

    public static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.appinfo_v01_00";

    protected static Schema validationSchema;
    protected static JAXBContext jaxbContext;

    private TransformerFactory<CodeGenerationTransformerContext> transformerFactory;
    private ImportSchemaLocations importSchemaLocations;

    /**
     * Default constructor.
     */
    public AbstractXsdCodeGenerator() {
        transformerFactory = TransformerFactory.getInstance(
                SchemaCompilerApplicationContext.XSD_CODEGEN_TRANSFORMER_FACTORY,
                new CodeGenerationTransformerContext(this));
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#generateOutput(org.opentravel.schemacompiler.model.TLModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    public Collection<File> generateOutput(final S source, CodeGenerationContext context)
            throws ValidationException, CodeGenerationException {
        // If a filter has not already been defined, create one that will allow processing of all
        // members and only
        // those libraries that are directly required by the members of the current source library
        if (getFilter() == null) {
            final AbstractLibrary sourceLibrary = getLibrary(source);
            final CodeGenerationFilter libraryFilter;

            if (source instanceof LibraryMember) {
                libraryFilter = new DependencyFilterBuilder((LibraryMember) source).buildFilter();
            } else {
                libraryFilter = new DependencyFilterBuilder(sourceLibrary).buildFilter();
            }

            setFilter(new CodeGenerationFilter() {

                @Override
                public boolean processEntity(LibraryElement entity) {
                    return true;
                }

                @Override
                public boolean processExtendedLibrary(XSDLibrary legacySchema) {
                    return libraryFilter.processExtendedLibrary(legacySchema);
                }

                @Override
                public boolean processLibrary(AbstractLibrary library) {
                    return (library == sourceLibrary) || libraryFilter.processLibrary(library);
                }

                /**
                 * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilter#addBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
                 */
                @Override
                public void addBuiltInLibrary(BuiltInLibrary library) {
                    libraryFilter.addBuiltInLibrary(library);
                }

            });
        }
        return super.generateOutput(source, context);
    }

    /**
     * Returns the schema locations to use when generating import declarations for the output
     * schema. If no schema locations are explicitly assigned for the code generator, default
     * locations will be assumed for each of the import declarations.
     * 
     * @return ImportSchemaLocations
     */
    public ImportSchemaLocations getImportSchemaLocations() {
        return importSchemaLocations;
    }

    /**
     * Assigns the schema locations to use when generating import declarations for the output
     * schema. If no schema locations are explicitly assigned for the code generator, default
     * locations will be assumed for each of the import declarations.
     * 
     * @param importSchemaLocations
     *            the collection of import schema locations
     */
    public void setImportSchemaLocations(ImportSchemaLocations importSchemaLocations) {
        this.importSchemaLocations = importSchemaLocations;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(java.lang.Object)
     */
    @Override
    protected boolean isSupportedSourceObject(S source) {
        return (source != null);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getTransformerFactory(org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected TransformerFactory<CodeGenerationTransformerContext> getTransformerFactory(
            CodeGenerationContext codegenContext) {
        transformerFactory.getContext().setCodegenContext(codegenContext);
        return transformerFactory;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#canGenerateOutput(org.opentravel.schemacompiler.model.TLModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected boolean canGenerateOutput(S source, CodeGenerationContext context) {
        CodeGenerationFilter filter = getFilter();

        return super.canGenerateOutput(source, context)
                && ((filter == null) || filter.processLibrary(getLibrary(source)));
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#transformSourceObjectToJaxb(java.lang.Object,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected Object transformSourceObjectToJaxb(S source, CodeGenerationContext context)
            throws CodeGenerationException {
        ObjectTransformer<S, ?, CodeGenerationTransformerContext> transformer = getTransformerFactory(
                context).getTransformer(source, org.w3._2001.xmlschema.Schema.class);

        if (transformer != null) {
            return transformer.transform(source);

        } else {
            String sourceType = (source == null) ? "UNKNOWN" : source.getClass().getSimpleName();
            throw new CodeGenerationException(
                    "No object transformer available for model element of type " + sourceType);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.TLModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected File getOutputFile(S source, CodeGenerationContext context) {
        File outputFolder = getOutputFolder(context, getLibrary(source).getLibraryUrl());
        String filename = context.getValue(CodeGenerationContext.CK_SCHEMA_FILENAME);

        if ((filename == null) || filename.trim().equals("")) {
            filename = getFilenameBuilder().buildFilename(source, "xsd");
        }
        return new File(outputFolder, filename);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getLineBreakProcessor()
     */
    @Override
    protected PrettyPrintLineBreakProcessor getLineBreakProcessor() {
        return new XMLSchemaLineBreakProcessor();
    }

    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream schemaStream = SchemaDeclarations.SCHEMA_FOR_SCHEMAS.getContent();

            schemaFactory.setResourceResolver(new ClasspathResourceResolver());
            validationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
