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
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLService;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Verifies the operation of the <code>LibraryFilenameBuilder</code> and <code>LibraryTrimmedFilenameBuilder</code>
 * components.
 */
public class TestLibraryFilenameBuilders {

    private static final String TEST_NS1_V0 = "http://www.opentravel.org/schemas/ns1/v0";
    private static final String TEST_NS1_V1 = "http://www.opentravel.org/schemas/ns1/v1";
    private static final String TEST_NS2_V0 = "http://www.opentravel.org/schemas/ns2/v0";

    @Test
    public void testLibraryFilenameBuilder() throws Exception {
        CodeGenerationFilenameBuilder<TLLibrary> filenameBuilder = new LibraryFilenameBuilder<>();
        TLModel testModel = createTestModel( false );
        Set<String> libraryFilenames = new HashSet<>();

        for (TLLibrary library : testModel.getUserDefinedLibraries()) {
            libraryFilenames.add( filenameBuilder.buildFilename( library, "xsd" ) );
        }

        Assert.assertEquals( 3, libraryFilenames.size() );
        Assert.assertTrue( libraryFilenames.contains( "LibraryA_0_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "LibraryA_1_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "LibraryB_0_0_0.xsd" ) );
    }

    @Test
    public void testLibraryFilenameBuilder_duplicateLibraryNames() throws Exception {
        CodeGenerationFilenameBuilder<TLLibrary> filenameBuilder = new LibraryFilenameBuilder<>();
        TLModel testModel = createTestModel( true );
        Set<String> libraryFilenames = new HashSet<>();

        for (TLLibrary library : testModel.getUserDefinedLibraries()) {
            libraryFilenames.add( filenameBuilder.buildFilename( library, "xsd" ) );
        }

        Assert.assertEquals( 4, libraryFilenames.size() );
        Assert.assertTrue( libraryFilenames.contains( "LibraryA_ns1_0_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "LibraryA_1_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "LibraryA_ns2_0_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "LibraryB_0_0_0.xsd" ) );
    }

    @Test
    public void testLibraryTrimmedFilenameBuilder() throws Exception {
        TLModel testModel = createTestModel( false );
        Set<String> libraryFilenames = new HashSet<>();

        for (TLLibrary library : testModel.getUserDefinedLibraries()) {
            CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder =
                new LibraryTrimmedFilenameBuilder( library.getService() );

            libraryFilenames.add( filenameBuilder.buildFilename( library, "xsd" ) );
        }

        Assert.assertEquals( 3, libraryFilenames.size() );
        Assert.assertTrue( libraryFilenames.contains( "ServiceA_Trim_LibraryA_0_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "ServiceA_Trim_LibraryA_1_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "ServiceB_Trim_LibraryB_0_0_0.xsd" ) );
    }

    @Test
    public void testLibraryTrimmedFilenameBuilder_duplicateLibraryNames() throws Exception {
        TLModel testModel = createTestModel( true );
        Set<String> libraryFilenames = new HashSet<>();

        for (TLLibrary library : testModel.getUserDefinedLibraries()) {
            CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder =
                new LibraryTrimmedFilenameBuilder( library.getService() );

            libraryFilenames.add( filenameBuilder.buildFilename( library, "xsd" ) );
        }

        Assert.assertEquals( 4, libraryFilenames.size() );
        Assert.assertTrue( libraryFilenames.contains( "ServiceA_Trim_LibraryA_ns1_0_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "ServiceA_Trim_LibraryA_1_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "ServiceA_Trim_LibraryA_ns2_0_0_0.xsd" ) );
        Assert.assertTrue( libraryFilenames.contains( "ServiceB_Trim_LibraryB_0_0_0.xsd" ) );
    }

    private TLModel createTestModel(boolean includeDuplicateLibraryNames) throws Exception {
        TLModel testModel = new TLModel();
        TLLibrary libraryA1v0 = createLibrary( TEST_NS1_V0, "LibraryA", "a", "ServiceA" );
        TLLibrary libraryA1v1 = createLibrary( TEST_NS1_V1, "LibraryA", "a", "ServiceA" );
        TLLibrary libraryB = createLibrary( TEST_NS2_V0, "LibraryB", "b", "ServiceB" );

        testModel.addLibrary( libraryA1v0 );
        testModel.addLibrary( libraryA1v1 );
        testModel.addLibrary( libraryB );

        if (includeDuplicateLibraryNames) {
            TLLibrary libraryA2 = createLibrary( TEST_NS2_V0, "LibraryA", "a", "ServiceA" );
            testModel.addLibrary( libraryA2 );
        }
        return testModel;
    }

    private TLLibrary createLibrary(String namespace, String name, String prefix, String serviceName) throws Exception {
        TLLibrary library = new TLLibrary();
        TLService service = new TLService();

        library.setLibraryUrl( File.createTempFile( "test", ".otm" ).toURI().toURL() );
        library.setNamespace( namespace );
        library.setName( name );
        library.setPrefix( prefix );

        service.setName( serviceName );
        library.setService( service );
        return library;
    }

}
