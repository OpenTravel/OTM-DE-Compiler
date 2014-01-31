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
package org.opentravel.schemacompiler.ota2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.CodeGeneratorTestAssertions;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.loader.LoaderValidationMessageKeys;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.ota2.OTA2CompilerConstants;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.task.TaskFactory;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Validates the operation of the schema compiler task default implementations.
 * 
 * @author S. Livezey
 */
public class TestSchemaCompilerTask {

    @BeforeClass
    public static void setupOTA2Context() {
        try {
            SchemaCompilerApplicationContext.getContext(); // Force a load of
                                                           // the default
                                                           // context before
                                                           // switching to STL2
            CompilerExtensionRegistry
                    .setActiveExtension(OTA2CompilerConstants.OTA2_COMPILER_EXTENSION_ID);

        } catch (Throwable t) {
            t.printStackTrace(System.out);
            throw new RuntimeException(t);
        }
    }

    @Test
    public void testSTL2_ContextComponents() throws Exception {
        // Lookup an application context component that only exists in the ota2
        // extensions
        assertTrue(SchemaCompilerApplicationContext.getContext().containsBean("ota2CommonSchema"));
        assertTrue(SchemaCompilerApplicationContext.getContext().containsBean("ota2MessageSchema"));
        assertTrue(SchemaCompilerApplicationContext.getContext().containsBean("soapEnvelopeSchema"));
    }

    @Test
    public void testSTL2_BuiltInLibraries() throws Exception {
        List<String> builtInNames = new ArrayList<String>();
        TLModel model = new TLModel();

        for (BuiltInLibrary lib : model.getBuiltInLibraries()) {
            builtInNames.add(lib.getName());
        }

        assertEquals(3, builtInNames.size());
        assertTrue(builtInNames.contains("XMLSchema"));
        assertTrue(builtInNames.contains("OTA2_BuiltIns_v2.0.0"));
        assertTrue(builtInNames.contains("OTA_SimpleTypes"));
    }

    @Test
    public void testSTL2_SchemaCompilerTask() throws Exception {
        File sourceFile = new File(System.getProperty("user.dir"),
                "/src/test/resources/libraries_1_4/test-package_v2/library_1_p2.xml");
        File targetFolder = new File(System.getProperty("user.dir")
                + "/target/codegen-output/testSchemaCompilerTask");
        CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);

        compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
        compilerTask.setCompileSchemas(true);
        compilerTask.setCompileServices(true);
        compilerTask.setGenerateExamples(true);
        compilerTask.setServiceEndpointUrl("http://www.OpenTravel.org/services");

        ValidationFindings findings = compilerTask.compileOutput(sourceFile);

        SchemaCompilerTestUtils.printFindings(findings);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        // Assert XML schema documents are valid
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testSchemaCompilerTask/schemas/library_1_p1_1_0_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testSchemaCompilerTask/schemas/library_1_p2_2_0_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testSchemaCompilerTask/schemas/library_2_p1_1_0_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testSchemaCompilerTask/schemas/library_2_p2_2_0_0.xsd"));

        // Assert WSDL and trimmed schema documents are valid
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testSchemaCompilerTask/services/SampleService_v2/SampleService_Trim_library_1_p1_1_0_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testSchemaCompilerTask/services/SampleService_v2/SampleService_Trim_library_1_p2_2_0_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testSchemaCompilerTask/services/SampleService_v2/SampleService_Trim_library_2_p1_1_0_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testSchemaCompilerTask/services/SampleService_v2/SampleService_Trim_library_2_p2_2_0_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidWsdl(getTestOutput("testSchemaCompilerTask/services/SampleService_v2/SampleService.wsdl"));
    }

    @Test
    public void testSTL2_BuiltInDependencies() throws Exception {
        File sourceFile = new File(System.getProperty("user.dir"),
                "/src/test/resources/libraries/stl-test-library.xml");
        File targetFolder = new File(System.getProperty("user.dir")
                + "/target/codegen-output/testBuiltInDependencies");
        CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);

        compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
        compilerTask.setCompileServices(false);
        compilerTask.setCompileSchemas(true);

        ValidationFindings findings = compilerTask.compileOutput(sourceFile);

        assertTrue(findings.hasFinding());
        boolean hasUnresolvedStlWarning = false;
        for (ValidationFinding f : findings.getAllFindingsAsList()) {
            if (LoaderValidationMessageKeys.WARNING_UNRESOLVED_LIBRARY_NAMESPACE.equals(f
                    .getMessageKey())) {
                hasUnresolvedStlWarning = true;
            }
        }
        assertTrue(hasUnresolvedStlWarning);
    }

    // @Test
    public void testSTL2_SchemaCompilerTask_manualTest() throws Exception {
        File sourceFile = new File(System.getProperty("user.dir"),
                "../../../temp/schemas/test/testA.otm");
        File targetFolder = new File(System.getProperty("user.dir")
                + "/target/codegen-output/testSchemaCompilerTask_manualTest");
        CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);

        compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
        compilerTask.setCompileServices(true);
        compilerTask.setCompileSchemas(true);
        compilerTask.setServiceEndpointUrl("http://www.OpenTravel.org/services");

        ValidationFindings findings = compilerTask.compileOutput(sourceFile);

        if (findings.hasFinding()) {
            System.out.println("Errors / Warnings:");

            for (String message : findings
                    .getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
                System.out.println("  " + message);
            }
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
    }

    // @Test
    public void testSTL2_SchemaCompilerTask_manualProjectTest() throws Exception {
        File sourceFile = new File(System.getProperty("user.dir"),
                "../../../temp/schemas/test/ThirdLibrary.otm");
        File projectFile = new File(sourceFile.getParentFile(), "/test_project.otp");
        ValidationFindings findings = new ValidationFindings();
        ProjectManager projectManager = new ProjectManager(false);
        Project project = projectManager.loadProject(projectFile, findings);

        SchemaCompilerTestUtils.printFindings(findings, FindingType.ERROR);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        findings = new ValidationFindings();
        projectManager.addUnmanagedProjectItems(Arrays.asList(new File[] { sourceFile }), project,
                findings);

        SchemaCompilerTestUtils.printFindings(findings, FindingType.ERROR);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        for (TLLibrary library : projectManager.getModel().getUserDefinedLibraries()) {
            System.out.println("Library: " + library.getName() + " / " + library.getNamespace());
        }
    }

    private File getTestOutput(String filename) {
        return new File(System.getProperty("user.dir") + "/target/codegen-output/" + filename);
    }

}
