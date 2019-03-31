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

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Verifies the operation of the <code>ResourceFilenameBuilder</code>.
 */
public class TestResourceFilenameBuilders {

    private static final String TEST_NS1_V0 = "http://www.opentravel.org/schemas/ns1/v0";
    private static final String TEST_NS1_V1 = "http://www.opentravel.org/schemas/ns1/v1";
    private static final String TEST_NS2_V0 = "http://www.opentravel.org/schemas/ns2/v0";

    @Test
    public void testResourceFilenameBuilder() throws Exception {
        CodeGenerationFilenameBuilder<TLResource> filenameBuilder = new ResourceFilenameBuilder();
        TLModel testModel = createTestModel( false, false );
        Set<String> resourceFilenames = new HashSet<>();

        for (TLLibrary library : testModel.getUserDefinedLibraries()) {
            for (TLResource resource : library.getResourceTypes()) {
                resourceFilenames.add( filenameBuilder.buildFilename( resource, "swagger" ) );
            }
        }

        Assert.assertEquals( 4, resourceFilenames.size() );
        Assert.assertTrue( resourceFilenames.contains( "ResourceA_0_0_0.swagger" ) );
        Assert.assertTrue( resourceFilenames.contains( "ResourceA_1_0_0.swagger" ) );
        Assert.assertTrue( resourceFilenames.contains( "ResourceB_0_0_0.swagger" ) );
        Assert.assertTrue( resourceFilenames.contains( "ResourceX_0_0_0.swagger" ) );
    }

    @Test
    public void testResourceFilenameBuilder_duplicateLibraries() throws Exception {
        CodeGenerationFilenameBuilder<TLResource> filenameBuilder = new ResourceFilenameBuilder();
        TLModel testModel = createTestModel( true, false );
        Set<String> resourceFilenames = new HashSet<>();

        for (TLLibrary library : testModel.getUserDefinedLibraries()) {
            for (TLResource resource : library.getResourceTypes()) {
                resourceFilenames.add( filenameBuilder.buildFilename( resource, "swagger" ) );
            }
        }

        Assert.assertEquals( 4, resourceFilenames.size() );
        Assert.assertTrue( resourceFilenames.contains( "ResourceA_0_0_0.swagger" ) );
        Assert.assertTrue( resourceFilenames.contains( "ResourceA_1_0_0.swagger" ) );
        Assert.assertTrue( resourceFilenames.contains( "ResourceB_0_0_0.swagger" ) );
        Assert.assertTrue( resourceFilenames.contains( "ResourceX_0_0_0.swagger" ) );
    }

    @Test
    public void testResourceFilenameBuilder_duplicateResources() throws Exception {
        CodeGenerationFilenameBuilder<TLResource> filenameBuilder = new ResourceFilenameBuilder();
        TLModel testModel = createTestModel( true, true );
        Set<String> resourceFilenames = new HashSet<>();

        for (TLLibrary library : testModel.getUserDefinedLibraries()) {
            for (TLResource resource : library.getResourceTypes()) {
                resourceFilenames.add( filenameBuilder.buildFilename( resource, "swagger" ) );
            }
        }

        Assert.assertEquals( 4, resourceFilenames.size() );
        Assert.assertTrue( resourceFilenames.contains( "ResourceA_ns1_0_0_0.swagger" ) );
        Assert.assertTrue( resourceFilenames.contains( "ResourceA_1_0_0.swagger" ) );
        Assert.assertTrue( resourceFilenames.contains( "ResourceB_0_0_0.swagger" ) );
        Assert.assertTrue( resourceFilenames.contains( "ResourceA_ns2_0_0_0.swagger" ) );
    }

    private TLModel createTestModel(boolean includeDuplicateLibraryNames, boolean includeDuplicateResourceNames)
        throws Exception {
        TLModel testModel = new TLModel();
        TLLibrary libraryA1v0 = createLibrary( TEST_NS1_V0, "LibraryA", "a", "ResourceA" );
        TLLibrary libraryA1v1 = createLibrary( TEST_NS1_V1, "LibraryA", "a", "ResourceA" );
        TLLibrary libraryB = createLibrary( TEST_NS2_V0, "ResourceB", "b", "ResourceB" );
        TLLibrary libraryAX2 = createLibrary( TEST_NS2_V0, includeDuplicateLibraryNames ? "LibraryA" : "LibraryX", "a",
            includeDuplicateResourceNames ? "ResourceA" : "ResourceX" );

        testModel.addLibrary( libraryA1v0 );
        testModel.addLibrary( libraryA1v1 );
        testModel.addLibrary( libraryB );
        testModel.addLibrary( libraryAX2 );
        return testModel;
    }

    private TLLibrary createLibrary(String namespace, String name, String prefix, String resourceName)
        throws Exception {
        TLLibrary library = new TLLibrary();
        TLResource resource = new TLResource();

        library.setLibraryUrl( File.createTempFile( "test", ".otm" ).toURI().toURL() );
        library.setNamespace( namespace );
        library.setName( name );
        library.setPrefix( prefix );

        if (resourceName != null) {
            resource.setName( resourceName );
            library.addNamedMember( resource );
        }
        return library;
    }

}
