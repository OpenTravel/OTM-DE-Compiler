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
package org.opentravel.schemacompiler.codegen.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.xml.PrettyPrintLineBreakProcessor;
import org.opentravel.schemacompiler.xml.XMLPrettyPrinter;
import org.springframework.context.ApplicationContext;
import org.w3._2001.xmlschema.Schema;
import org.w3c.dom.Document;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TDocumented;
import org.xmlsoap.schemas.wsdl.TTypes;

/**
 * <code>CodeGenerator</code> base class that handles schema output generation using JAXB bindings
 * as a mechanism for producing the output content.
 * 
 * @param <S>
 *            the source type for which output content will be generated
 * @author S. Livezey
 */
public abstract class AbstractJaxbCodeGenerator<S extends ModelElement> extends
        AbstractCodeGenerator<S> {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private List<SchemaDeclaration> compileTimeDependencies = new ArrayList<>();

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(java.lang.Object,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    public void doGenerateOutput(S source, CodeGenerationContext context)
            throws CodeGenerationException {
        File outputFile = getOutputFile(source, context);
        
        try (OutputStream out = new FileOutputStream(outputFile)){
            Object jaxbObject = transformSourceObjectToJaxb(source, context);
            Marshaller marshaller = getMarshaller(source, getJaxbSchema(jaxbObject));
            Document domDocument = XMLPrettyPrinter.newDocument();

            marshaller.marshal(jaxbObject, domDocument);
            new XMLPrettyPrinter(getLineBreakProcessor()).formatDocument(domDocument, out);

            // Finish up by copying any dependencies that were identified during code generation
            if (context.getBooleanValue(CodeGenerationContext.CK_COPY_COMPILE_TIME_DEPENDENCIES)) {
                copyCompileTimeDependencies(context);
            }
            addGeneratedFile(outputFile);

        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Copies any schema documents that were identified at compile-time to the output folder if they
     * do not already exist.
     * 
     * @param context
     *            the code generation context
     * @throws CodeGenerationException
     *             thrown if one or more of the files cannot be copied
     */
    protected void copyCompileTimeDependencies(CodeGenerationContext context)
            throws CodeGenerationException {
        try {
            for (SchemaDeclaration schemaDeclaration : getCompileTimeDependencies()) {
                if (schemaDeclaration == SchemaDeclarations.SCHEMA_FOR_SCHEMAS) {
                    continue;
                }
                if (!schemaDeclaration.getFilename(
                		CodeGeneratorFactory.XSD_TARGET_FORMAT).endsWith(".xsd")) {
                    continue;
                }
                File outputFolder = getOutputFolder(context, null);
                String builtInFolder = getBuiltInSchemaOutputLocation(context);

                if (builtInFolder != null) {
                    outputFolder = new File(outputFolder, builtInFolder);
                    if (!outputFolder.exists())
                        outputFolder.mkdirs();
                }

                // Copy the contents of the file to the built-in folder location
                File outputFile = new File(outputFolder, schemaDeclaration.getFilename(
                		CodeGeneratorFactory.XSD_TARGET_FORMAT));

                if (!outputFile.exists()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            schemaDeclaration.getContent(CodeGeneratorFactory.XSD_TARGET_FORMAT)));
                    
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                        String line = null;

                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.write(LINE_SEPARATOR);
                        }
                    }
                    reader.close();
                }
                addGeneratedFile(outputFile); // count dependency as generated - even if it already
                                              // existed
            }
        } catch (IOException e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Returns the JAXB marshaller to use for code generation output.
     * 
     * @param source
     *            the meta-model element for which output is being generated
     * @param schema
     *            the XML schema document for which xmlns prefix declarations will be created (may
     *            be null)
     * @return Marshaller
     * @throws JAXBException
     *             thrown if the marshaller cannot be created
     */
    protected abstract Marshaller getMarshaller(S source, Schema schema) throws JAXBException;

    /**
     * Returns the <code>PrettyPrintLineBreakProcessor</code> to use when formatting output.
     * 
     * @return PrettyPrintLineBreakProcessor
     */
    protected abstract PrettyPrintLineBreakProcessor getLineBreakProcessor();

    /**
     * Performs the translation from meta-model element to a JAXB object that will be used to
     * generate the output content.
     * 
     * @param source
     *            the meta-model element to translate
     * @param context
     *            the code generation context
     * @return Object
     * @throws CodeGenerationException
     *             thrown if an error occurs during object translation
     */
    protected abstract Object transformSourceObjectToJaxb(S source, CodeGenerationContext context)
            throws CodeGenerationException;

    /**
     * Returns the <code>TransformerFactory</code> to be used for JAXB translations by the code
     * generator.
     * 
     * @param codegenContext
     *            the current context for the code generator
     * @return TransformerFactory<CodeGenerationTransformerContext>
     */
    protected abstract TransformerFactory<CodeGenerationTransformerContext> getTransformerFactory(
            CodeGenerationContext codegenContext);

    /**
     * Adds the given schema declaration to the list of compile-time schema dependencies. At the end
     * of the code generation process, these dependencies will be copied to the output folder.
     * 
     * @param compileTimeDependency
     *            the compile-time dependency to add
     */
    public void addCompileTimeDependency(SchemaDeclaration compileTimeDependency) {
        if (!compileTimeDependencies.contains(compileTimeDependency)) {
            compileTimeDependencies.add(compileTimeDependency);
        }
    }

    /**
     * Returns the list of compile-time schema dependencies that have been reported during code
     * generation.
     * 
     * @return Collection<SchemaDeclaration>
     */
    public Collection<SchemaDeclaration> getCompileTimeDependencies() {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
        Collection<SchemaDeclaration> dependencies = new ArrayList<>(compileTimeDependencies);

        for (SchemaDeclaration dependency : compileTimeDependencies) {
            resolveIndirectDependencies(dependency.getDependencies(), appContext, dependencies);
        }
        return dependencies;
    }

    /**
     * Resolves all of the indirect dependencies provided in the given list of bean ID's.
     * 
     * @param dependencyBeanIds
     *            the application context bean ID's of any indirectly-dependent item(s)
     * @param appContext
     *            the spring application context for the compiler
     * @param dependencyList
     *            the list of schema dependencies being constructed
     */
    private void resolveIndirectDependencies(List<String> dependencyBeanIds,
            ApplicationContext appContext, Collection<SchemaDeclaration> dependencyList) {
        for (String beanId : dependencyBeanIds) {
            if (appContext.containsBean(beanId)) {
                SchemaDeclaration dependency = (SchemaDeclaration) appContext.getBean(beanId);

                if (!dependencyList.contains(dependency)) {
                    dependencyList.add(dependency);
                    resolveIndirectDependencies(dependency.getDependencies(), appContext,
                            dependencyList);
                }
            }
        }
    }

    /**
     * Returns the XML schema associated with the given JAXB object. For schema code generators, the
     * JAXB object provided will typically be the schema instance. For WSDL code generators, the
     * JAXB schema will be nested inside the types section of the WSDL document.
     * 
     * @param jaxbObject
     *            the JAXB object to analyze
     * @return Schema
     */
    private Schema getJaxbSchema(Object jaxbObject) {
        Schema schema = null;

        if (jaxbObject instanceof JAXBElement) {
            jaxbObject = ((JAXBElement<?>) jaxbObject).getValue();
        }

        if (jaxbObject instanceof Schema) {
            schema = (Schema) jaxbObject;

        } else if (jaxbObject instanceof TDefinitions) {
            TDefinitions definitions = (TDefinitions) jaxbObject;

            for (TDocumented wsdlElement : definitions.getAnyTopLevelOptionalElement()) {
                if (wsdlElement instanceof TTypes) {
                    for (Object wsdlType : ((TTypes) wsdlElement).getAny()) {
                        if (wsdlType instanceof Schema) {
                            schema = (Schema) wsdlType;
                        }
                    }
                }
            }
        }
        return schema;
    }

}
