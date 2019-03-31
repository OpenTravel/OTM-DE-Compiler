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

package org.opentravel.schemacompiler.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import javax.xml.XMLConstants;

/**
 * Verifies the functions of the <code>TLSimpleFacet</code> class.
 */
public class TestTLSimpleFacet extends AbstractModelTest {

    @Test
    public void testIdentityFunctions() throws Exception {
        TLCoreObject core = addCore( "TestObject", library1 );
        TLSimpleFacet facet = core.getSimpleFacet();

        assertEquals( library1.getNamespace(), facet.getNamespace() );
        assertEquals( core.getName() + "_Simple", facet.getLocalName() );
        assertEquals( "TestLibrary1.otm : TestObject/Simple", facet.getValidationIdentity() );
    }

    @Test
    public void testFacetFunctions() throws Exception {
        TLCoreObject core = addCore( "TestObject", library1 );
        TLCoreObject orphanCore = new TLCoreObject();
        TLSimpleFacet origSimple = core.getSimpleFacet();
        TLSimpleFacet orphanSimple = orphanCore.getSimpleFacet();

        core.setSimpleFacet( origSimple );
        assertEquals( origSimple, core.getSimpleFacet() );
        testNegativeCase( core, e -> core.setSimpleFacet( null ), IllegalStateException.class );
        orphanCore.setSimpleFacet( null );
        assertNull( orphanCore.getSimpleFacet() );
        assertNull( orphanSimple.getFacetType() );
        assertNull( orphanSimple.getOwningEntity() );
    }

    @Test
    public void testTypeAssignment() throws Exception {
        TLCoreObject core = addCore( "TestObject", library1 );
        TLSimpleFacet facet = core.getSimpleFacet();

        assertTrue( facet.isEmptyType() );
        assertFalse( facet.declaresContent() );

        facet.setSimpleType( (TLAttributeType) findEntity( XMLConstants.W3C_XML_SCHEMA_NS_URI, "string" ) );
        assertFalse( facet.isEmptyType() );
        assertTrue( facet.declaresContent() );
    }

    @Test
    public void testDocumentationFunctions() throws Exception {
        testDocumentationFunctions( addCore( "TestObject", library1 ).getSimpleFacet() );
    }

    @Test
    public void testEquivalentFunctions() throws Exception {
        testEquivalentFunctions( addCore( "TestObject", library1 ).getSimpleFacet() );
    }

    @Test
    public void testExampleFunctions() throws Exception {
        testExampleFunctions( addCore( "TestObject", library1 ).getSimpleFacet() );
    }

}
