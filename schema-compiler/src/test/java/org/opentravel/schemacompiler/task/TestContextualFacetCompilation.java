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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.schemacompiler.codegen.CodeGeneratorTestAssertions;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verifies the compilation of contextual facets in various project configurations.
 */
public class TestContextualFacetCompilation {

    public static final String NAMESPACE_BASELIB =
        "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-facets/base/v1";
    public static final String NAMESPACE_FACETS1 =
        "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-facets/facets1/v1";
    public static final String NAMESPACE_FACETS2 =
        "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-facets/facets2/v1";
    public static final String LIBNAME_BASELIB = "base_library";
    public static final String LIBNAME_FACETS1 = "facets1_library";
    public static final String LIBNAME_FACETS2 = "facets2_library";

    @Test
    public void testFacetCompilation_baseLibrary() throws Exception {
        File projectFile =
            new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-facets/project_base.otp" );
        File targetFolder =
            new File( System.getProperty( "user.dir" ) + "/target/codegen-output/testFacetCompilation_baseLibrary" );
        CompileAllCompilerTask compilerTask = TaskFactory.getTask( CompileAllCompilerTask.class );
        ValidationFindings findings;

        configureTask( compilerTask, targetFolder );
        findings = compilerTask.compileOutput( projectFile );

        SchemaCompilerTestUtils.printFindings( findings );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        CodeGeneratorTestAssertions.validateGeneratedFiles( compilerTask.getGeneratedFiles() );
    }

    @Test
    public void testFacetCompilation_facets1() throws Exception {
        File projectFile =
            new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-facets/project_facets1.otp" );
        File targetFolder =
            new File( System.getProperty( "user.dir" ) + "/target/codegen-output/testFacetCompilation_facets1" );
        CompileAllCompilerTask compilerTask = TaskFactory.getTask( CompileAllCompilerTask.class );
        ValidationFindings findings;

        configureTask( compilerTask, targetFolder );
        findings = compilerTask.compileOutput( projectFile );

        SchemaCompilerTestUtils.printFindings( findings );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        CodeGeneratorTestAssertions.validateGeneratedFiles( compilerTask.getGeneratedFiles() );
    }

    @Test
    public void testFacetCompilation_facets_all() throws Exception {
        File projectFile = new File(
            SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-facets/project_facets_all.otp" );
        File targetFolder =
            new File( System.getProperty( "user.dir" ) + "/target/codegen-output/testFacetCompilation_facets_all" );
        CompileAllCompilerTask compilerTask = TaskFactory.getTask( CompileAllCompilerTask.class );
        ValidationFindings findings;

        configureTask( compilerTask, targetFolder );
        findings = compilerTask.compileOutput( projectFile );

        SchemaCompilerTestUtils.printFindings( findings );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        CodeGeneratorTestAssertions.validateGeneratedFiles( compilerTask.getGeneratedFiles() );
    }

    @Test
    public void testGhostFacets() throws Exception {
        File projectFile =
            new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-facets/project_base.otp" );
        ProjectManager projectManager = new ProjectManager( false );
        ValidationFindings findings = new ValidationFindings();

        projectManager.loadProject( projectFile, findings );
        SchemaCompilerTestUtils.printFindings( findings );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );

        TLModel model = projectManager.getModel();
        TLLibrary library = (TLLibrary) model.getLibrary( NAMESPACE_BASELIB, LIBNAME_BASELIB );
        TLBusinessObject bo = (library == null) ? null : library.getBusinessObjectType( "ExtFacetTestBO" );
        List<TLContextualFacet> boGhostFacets = FacetCodegenUtils.findGhostFacets( bo, TLFacetType.CUSTOM );
        List<TLContextualFacet> nestedGhostFacets;

        assertEquals( 1, boGhostFacets.size() );
        assertEquals( "ExtFacetTestBO_CustomF1", boGhostFacets.get( 0 ).getLocalName() );
        assertTrue( boGhostFacets.get( 0 ).isLocalFacet() );
        nestedGhostFacets = FacetCodegenUtils.findGhostFacets( boGhostFacets.get( 0 ), TLFacetType.CUSTOM );
        assertEquals( 1, nestedGhostFacets.size() );
        assertEquals( "ExtFacetTestBO_CustomF1_CustomF1A", nestedGhostFacets.get( 0 ).getLocalName() );
        assertTrue( nestedGhostFacets.get( 0 ).isLocalFacet() );
    }

    @Test
    public void testNonLocalGhostFacets() throws Exception {
        File projectFile = new File(
            SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-facets/project_facets_all.otp" );
        ProjectManager projectManager = new ProjectManager( false );
        ValidationFindings findings = new ValidationFindings();

        projectManager.loadProject( projectFile, findings );
        SchemaCompilerTestUtils.printFindings( findings );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );

        TLModel model = projectManager.getModel();
        TLLibrary libraryF1 = (TLLibrary) model.getLibrary( NAMESPACE_FACETS1, LIBNAME_FACETS1 );
        TLLibrary libraryF2 = (TLLibrary) model.getLibrary( NAMESPACE_FACETS2, LIBNAME_FACETS2 );
        List<TLContextualFacet> nonLocalGhostFacets1 = FacetCodegenUtils.findNonLocalGhostFacets( libraryF1 );
        List<TLContextualFacet> nonLocalGhostFacets2 = FacetCodegenUtils.findNonLocalGhostFacets( libraryF2 );

        assertEquals( 4, nonLocalGhostFacets1.size() );
        assertContainsFacets( nonLocalGhostFacets1, "ExtFacetTestChoice_ChoiceF2_ChoiceF2A",
            "ExtFacetTestBO_CustomF2_CustomF2A", "ExtFacetTestBO_Query_QueryF2_QueryF2A",
            "ExtFacetTestBO_Update_UpdateF2_UpdateF2A" );

        assertEquals( 4, nonLocalGhostFacets2.size() );
        assertContainsFacets( nonLocalGhostFacets2, "ExtFacetTestChoice_ChoiceF2_ChoiceF2B",
            "ExtFacetTestBO_CustomF2_CustomF2B", "ExtFacetTestBO_Query_QueryF2_QueryF2B",
            "ExtFacetTestBO_Update_UpdateF2_UpdateF2B" );
    }

    @Test
    public void testResourceFacetCompilation() throws Exception {
        File projectFile =
            new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-facets/project_resource.otp" );
        File targetFolder =
            new File( System.getProperty( "user.dir" ) + "/target/codegen-output/testResourceFacetCompilation" );
        File baseSwaggerFile =
            new File( targetFolder + "/swagger/facets_resource_library_FacetResource/FacetResource_1.defs.swagger" );
        File extSwaggerFile = new File(
            targetFolder + "/swagger/facets_resource_library_ExtFacetResource/ExtFacetResource_1.defs.swagger" );
        CompileAllCompilerTask compilerTask = TaskFactory.getTask( CompileAllCompilerTask.class );
        ValidationFindings findings;

        configureTask( compilerTask, targetFolder );
        compilerTask.setCompileSchemas( true );
        compilerTask.setCompileJsonSchemas( true );
        compilerTask.setCompileServices( false );
        compilerTask.setCompileSwagger( true );
        compilerTask.setCompileOpenApi( true );
        findings = compilerTask.compileOutput( projectFile );

        SchemaCompilerTestUtils.printFindings( findings );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertTrue( baseSwaggerFile.exists() );
        assertTrue( extSwaggerFile.exists() );
        CodeGeneratorTestAssertions.validateGeneratedFiles( compilerTask.getGeneratedFiles() );

        // Validate that the non-local ghost facets exist in the definitions section of the swagger document
        JsonNode baseSwaggerNode = JsonLoader.fromFile( baseSwaggerFile );
        JsonNode extSwaggerNode = JsonLoader.fromFile( extSwaggerFile );
        JsonNode baseDefsNode = baseSwaggerNode.get( "definitions" );
        JsonNode extDefsNode = extSwaggerNode.get( "definitions" );

        // Ensure all of the contextual ghost facets were generated for the extended business object
        assertFalse( extDefsNode.has( "ExtFacetTestChoiceChoiceF1" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOCustomF1" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOQueryQueryF1" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOUpdateUpdateF1" ) );
        assertFalse( extDefsNode.has( "ExtFacetTestChoiceChoiceF1ChoiceF1A" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOCustomF1CustomF1A" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOQueryQueryF1QueryF1A" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOUpdateUpdateF1UpdateF1A" ) );
        assertFalse( extDefsNode.has( "ExtFacetTestChoiceChoiceF2ChoiceF2A" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOCustomF2CustomF2A" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOQueryQueryF2QueryF2A" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOUpdateUpdateF2UpdateF2A" ) );
        assertFalse( extDefsNode.has( "ExtFacetTestChoiceChoiceF2ChoiceF2B" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOCustomF2CustomF2B" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOQueryQueryF2QueryF2B" ) );
        assertTrue( extDefsNode.has( "ExtFacetTestBOUpdateUpdateF2UpdateF2B" ) );

        // Ensure all extension point facet types were generated for the base business object
        assertTrue( baseDefsNode.has( "ExtensionPoint_FacetTestBO_CustomF1" ) );
        assertTrue( baseDefsNode.has( "ExtensionPoint_FacetTestBO_Query_QueryF1" ) );
        assertTrue( baseDefsNode.has( "ExtensionPoint_FacetTestBO_Update_UpdateF1" ) );
    }

    private void assertContainsFacets(List<TLContextualFacet> facetList, String... localNames) {
        Set<String> facetNames = new HashSet<>();

        for (TLContextualFacet facet : facetList) {
            facetNames.add( facet.getLocalName() );
        }
        for (String localName : localNames) {
            assertTrue( facetNames.contains( localName ) );
        }
    }

    void printGhostFacets(TLFacetOwner facetOwner, TLFacetType facetType, String indent) {
        List<TLContextualFacet> ghostFacets = FacetCodegenUtils.findGhostFacets( facetOwner, facetType );

        for (TLContextualFacet facet : ghostFacets) {
            System.out.println( indent + facet.getLocalName() );
            printGhostFacets( facet, facetType, indent + "  " );
        }
    }

    private void configureTask(CompileAllCompilerTask task, File outputFolder) {
        task.setOutputFolder( outputFolder.getAbsolutePath() );
        task.setCompileSchemas( true );
        task.setCompileJsonSchemas( true );
        task.setCompileServices( true );
        task.setGenerateExamples( true );
        task.setCompileHtml( false );
        task.setExampleMaxRepeat( 3 );
        task.setServiceEndpointUrl( "http://www.OpenTravel.org/services" );
        task.setResourceBaseUrl( "http://www.OpenTravel.org/resources" );
    }

}
