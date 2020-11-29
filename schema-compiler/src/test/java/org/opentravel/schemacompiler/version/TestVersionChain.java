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

package org.opentravel.schemacompiler.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;

import java.util.List;
import java.util.SortedSet;

/**
 * Validates the functions of the <code>VersionChain</code> class and its associated helper classes.
 */
public class TestVersionChain extends AbstractVersionHelperTests {

    private static TLModel testModel;
    private static VersionChainFactory chainFactory;

    @BeforeClass
    public static void setup() throws Exception {
        testModel = loadTestModel( "library_v01_02_02.xml" );
        chainFactory = new VersionChainFactory( testModel );
    }

    @Test
    public void testVersionChainFactory() throws Exception {
        assertEquals( 1, chainFactory.getBaseNamespaces().size() );
    }

    @Test
    public void testLibraryChains() throws Exception {
        TLLibrary library100 = (TLLibrary) testModel.getLibrary( NS_VERSION_1, TEST_LIBRARY_NAME );
        TLLibrary library120 = (TLLibrary) testModel.getLibrary( NS_VERSION_1_2, TEST_LIBRARY_NAME );
        TLLibrary library122 = (TLLibrary) testModel.getLibrary( NS_VERSION_1_2_2, TEST_LIBRARY_NAME );
        List<VersionChain<TLLibrary>> libraryChains = chainFactory.getLibraryChains();
        VersionChain<TLLibrary> chain;

        assertEquals( 1, libraryChains.size() );
        assertEquals( 5, (chain = libraryChains.get( 0 )).getVersions().size() );
        assertEquals( BASE_NS, chain.getBaseNS() );
        assertEquals( TEST_LIBRARY_NAME, chain.getName() );
        assertEquals( chain, chainFactory.getVersionChain( library120 ) );
        assertEquals( NS_VERSION_1_1, chain.getPreviousVersion( library120 ).getNamespace() );
        assertEquals( NS_VERSION_1_2_1, chain.getNextVersion( library120 ).getNamespace() );
        assertNull( chain.getPreviousVersion( library100 ) );
        assertNull( chain.getNextVersion( library122 ) );
    }

    @Test
    public void testEntityChains() throws Exception {
        TLLibrary library100 = (TLLibrary) testModel.getLibrary( NS_VERSION_1, TEST_LIBRARY_NAME );
        TLLibrary library120 = (TLLibrary) testModel.getLibrary( NS_VERSION_1_2, TEST_LIBRARY_NAME );
        TLBusinessObject bo100 = library100.getBusinessObjectType( "LaterMinorVersionBO" );
        TLBusinessObject bo120 = library120.getBusinessObjectType( "LaterMinorVersionBO" );

        List<VersionChain<Versioned>> entityChains = chainFactory.getEntityChains();
        VersionChain<Versioned> chain;

        assertEquals( 35, entityChains.size() );
        assertEquals( 2, (chain = entityChains.get( 0 )).getVersions().size() );
        assertEquals( BASE_NS, chain.getBaseNS() );
        assertEquals( "LaterMinorVersionBO", chain.getName() );
        assertEquals( chain, chainFactory.getVersionChain( bo120 ) );
        assertEquals( bo100, chain.getPreviousVersion( bo120 ) );
        assertEquals( bo120, chain.getNextVersion( bo100 ) );
        assertNull( chain.getPreviousVersion( bo100 ) );
        assertNull( chain.getNextVersion( bo120 ) );
    }

    @Test
    public void testMajorVersionEntityGroup() throws Exception {
        SortedSet<MajorVersionEntityGroup> entityGroups = chainFactory.getVersionGroups( BASE_NS );
        MajorVersionEntityGroup group;
        Versioned entity;

        assertEquals( 1, entityGroups.size() );
        assertEquals( 35, (group = entityGroups.first()).getMemberNames().size() );
        assertEquals( BASE_NS, group.getBaseNamespace() );
        assertEquals( NS_VERSION_1, group.getMajorVersionNamespace() );
        assertEquals( "1.0.0", group.getMajorVersion() );
        assertNotNull( entity = group.getNamedMember( "LaterMinorVersionBO" ) );
        assertEquals( NS_VERSION_1, entity.getNamespace() );
    }

}
