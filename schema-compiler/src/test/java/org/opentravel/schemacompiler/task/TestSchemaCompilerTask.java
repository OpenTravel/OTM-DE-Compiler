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

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.Test;
import org.opentravel.schemacompiler.codegen.CodeGeneratorTestAssertions;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.task.TaskFactory;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Validates the operation of the schema compiler task default implementations.
 * 
 * @author S. Livezey
 */
public class TestSchemaCompilerTask {

    @Test
    public void testSchemaCompilerTask() throws Exception {
        File catalogFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation()
                + "/library-catalog.xml");
        File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation()
                + "/test-package_v2/library_1_p2.xml");
        File targetFolder = new File(System.getProperty("user.dir")
                + "/target/codegen-output/testSchemaCompilerTask");
        CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);

        compilerTask.setCatalogLocation(catalogFile.getAbsolutePath());
        compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
        compilerTask.setCompileSchemas(true);
        compilerTask.setCompileJsonSchemas(true);
        compilerTask.setCompileServices(true);
        compilerTask.setGenerateExamples(true);
        compilerTask.setExampleMaxRepeat( 3 );
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
    public void testSchemaGenerationForInheritance() throws Exception {
        File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation()
                + "/test-package-inheritance/extended_service.xml");
        File targetFolder = new File(System.getProperty("user.dir")
                + "/target/codegen-output/testSchemaGenerationForInheritance");
        CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);

        compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
        compilerTask.setCompileSchemas(true);
        compilerTask.setCompileJsonSchemas(true);
        compilerTask.setCompileServices(true);
        compilerTask.setGenerateExamples(true);
        compilerTask.setServiceEndpointUrl("http://www.OpenTravel.org/services");

        ValidationFindings findings = compilerTask.compileOutput(sourceFile);

        SchemaCompilerTestUtils.printFindings(findings);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        // No automated assertions - for now, the schemas must be checked manually
    }

    @Test
    public void testSchemaGenerationForProjects() throws Exception {
        File projectTestFolder = new File(SchemaCompilerTestUtils.getBaseProjectLocation());
        File sourceFile = new File(projectTestFolder, "/project_3.otp");
        File targetFolder = new File(System.getProperty("user.dir")
                + "/target/codegen-output/testSchemaGenerationForProjects");
        CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);

        compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
        compilerTask.setCompileSchemas(true);
        compilerTask.setCompileJsonSchemas(true);
        compilerTask.setCompileServices(true);
        compilerTask.setGenerateExamples(true);
        compilerTask.setServiceEndpointUrl("http://www.OpenTravel.org/services");

        ValidationFindings findings = compilerTask.compileOutput(sourceFile);

        SchemaCompilerTestUtils.printFindings(findings);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        // No automated assertions - for now, the schemas must be checked manually
    }

    @Test
    public void testServiceGenerationForVersions() throws Exception {
        File catalogFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation()
                + "/../versions/version-catalog.xml");
        File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation()
                + "/../versions/library_v01_02_02.xml");
        File targetFolder = new File(System.getProperty("user.dir")
                + "/target/codegen-output/testServiceGenerationForVersions");
        CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);

        compilerTask.setCatalogLocation(catalogFile.getAbsolutePath());
        compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
        compilerTask.setCompileSchemas(true);
        compilerTask.setCompileJsonSchemas(true);
        compilerTask.setCompileServices(true);
        compilerTask.setGenerateExamples(true);
        compilerTask.setServiceEndpointUrl("http://www.OpenTravel.org/services");

        ValidationFindings findings = compilerTask.compileOutput(sourceFile);

        SchemaCompilerTestUtils.printFindings(findings);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        // Assert XML schema documents are valid
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testServiceGenerationForVersions/schemas/test_library_1_0_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testServiceGenerationForVersions/schemas/test_library_1_1_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testServiceGenerationForVersions/schemas/test_library_1_2_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testServiceGenerationForVersions/schemas/test_library_1_2_1.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testServiceGenerationForVersions/schemas/test_library_1_2_2.xsd"));

        // Assert WSDL and trimmed schema documents are valid
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testServiceGenerationForVersions/services/VersionedService_v1/VersionedService_Trim_test_library_1_0_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testServiceGenerationForVersions/services/VersionedService_v1/VersionedService_Trim_test_library_1_1_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidXsd(getTestOutput("testServiceGenerationForVersions/services/VersionedService_v1/VersionedService_Trim_test_library_1_2_0.xsd"));
        CodeGeneratorTestAssertions
                .assertValidWsdl(getTestOutput("testServiceGenerationForVersions/services/VersionedService_v1/VersionedService.wsdl"));
    }

    // @Test
    public void testSchemaCompilerTask_manualTest() throws Exception {
        System.setProperty("ota2.legacyFacetNaming", "true");
        File sourceFile = new File(System.getProperty("user.dir"),
                "../../../temp/amtrak/_ARWS_Model/libraries/Amtrak_Solution_v01_00/v67/Amtrak_Solution.otm");
        File targetFolder = new File(System.getProperty("user.dir")
                + "/target/codegen-output/testSchemaCompilerTask_manualTest");
        CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);

        compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
        compilerTask.setCompileSchemas(true);
        compilerTask.setCompileJsonSchemas(true);
        compilerTask.setCompileServices(true);
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

    private File getTestOutput(String filename) {
        return new File(System.getProperty("user.dir") + "/target/codegen-output/" + filename);
    }

}
