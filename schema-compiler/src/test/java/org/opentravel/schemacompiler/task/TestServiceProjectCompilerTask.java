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
package org.opentravel.schemacompiler.task;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opentravel.schemacompiler.codegen.impl.LibraryTrimmedFilenameBuilder;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.DefaultLibraryNamespaceResolver;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.task.ServiceCompilerTask;
import org.opentravel.schemacompiler.task.ServiceProjectCompilerTask;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestServiceProjectCompilerTask {

    private final String PACKAGE_SERVICE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-service_v1";

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void compileOutputShouldUseTrimedServiceSchamaInExamples() throws Exception {
        // given
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        modelLoader.setNamespaceResolver(new DefaultLibraryNamespaceResolver());
        modelLoader.loadLibraryModel(new LibraryStreamInputSource(new File(SchemaCompilerTestUtils
                .getBaseLibraryLocation(), "test-package-service/SimpleService.xml")));
        TLLibrary lib = getLibrary(modelLoader.getLibraryModel(), PACKAGE_SERVICE, "SimpleService");
        ServiceProjectCompilerTask serviceTask = new ServiceProjectCompilerTask("Test");
        ServiceCompilerTask options = new ServiceCompilerTask();
        options.setGenerateExamples(true);
        options.setOutputFolder(tmp.getRoot().getAbsolutePath());
        serviceTask.applyTaskOptions(options);

        // when
        serviceTask.compileOutput(Collections.singletonList(lib), new ArrayList<XSDLibrary>());

        // then
        File example = findFileWithString("RQ", serviceTask.getGeneratedFiles());
        Document exampleDom = loadXMLFileAsDom(example);
        String schemaDef = exampleDom.getFirstChild().getAttributes()
                .getNamedItem("xsi:schemaLocation").getNodeValue();
        String schemaLocation = schemaDef.split(" ")[1];
        String expectedSchemaLocation = "../"
                + new LibraryTrimmedFilenameBuilder(null).buildFilename(lib, "xsd");

        assertEquals(expectedSchemaLocation, schemaLocation);
    }

    private File findFileWithString(String pathSeg, Collection<File> files) {
        for (File f : files) {
            if (f.getAbsolutePath().contains(pathSeg))
                return f;
        }
        return null;
    }

    private Document loadXMLFileAsDom(File xmlFile) throws ParserConfigurationException,
            SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        return doc;
    }

    protected TLLibrary getLibrary(TLModel m, String namespace, String libraryName)
            throws Exception {
        TLLibrary library = null;

        for (AbstractLibrary lib : m.getLibrariesForNamespace(namespace)) {
            if (lib.getName().equals(libraryName)) {
                library = (TLLibrary) lib;
                break;
            }
        }
        return library;
    }
}
