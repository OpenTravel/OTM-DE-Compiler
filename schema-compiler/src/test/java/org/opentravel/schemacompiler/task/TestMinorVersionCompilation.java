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

import org.junit.Test;
import org.opentravel.schemacompiler.codegen.CodeGeneratorTestAssertions;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;

/**
 * Verifies the correct compilation of minor versions for JSON schemas and Swagger specifications.
 */
public class TestMinorVersionCompilation {

    @Test
    public void testMinorVersionCompilation() throws Exception {
        File sourceFile =
            new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/../versions_1_6/versions_project.otp" );
        File targetFolder =
            new File( System.getProperty( "user.dir" ) + "/target/codegen-output/testMinorVersionCompilation" );
        CompileAllCompilerTask compilerTask = TaskFactory.getTask( CompileAllCompilerTask.class );

        compilerTask.setOutputFolder( targetFolder.getAbsolutePath() );
        compilerTask.setCompileSchemas( false );
        compilerTask.setCompileJsonSchemas( true );
        compilerTask.setCompileServices( false );
        compilerTask.setCompileSwagger( true );
        compilerTask.setCompileOpenApi( true );
        compilerTask.setCompileHtml( false );
        compilerTask.setGenerateExamples( true );
        compilerTask.setExampleMaxRepeat( 3 );
        compilerTask.setServiceEndpointUrl( "http://www.OpenTravel.org/services" );
        compilerTask.setResourceBaseUrl( "http://www.OpenTravel.org" );
        compilerTask.setSuppressOtmExtensions( false );
        CompilerExtensionRegistry.setActiveExtension( "OTA2" );

        ValidationFindings findings = compilerTask.compileOutput( sourceFile );

        SchemaCompilerTestUtils.printFindings( findings );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        CodeGeneratorTestAssertions.validateGeneratedFiles( compilerTask.getGeneratedFiles() );
    }

}
