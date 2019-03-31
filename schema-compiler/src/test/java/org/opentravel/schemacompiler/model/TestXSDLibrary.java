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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.util.URLUtils;

import java.io.File;

/**
 * Verifies the functions of the <code>XSDLibrary</code> class and member types.
 */
public class TestXSDLibrary extends AbstractModelTest {

    @Test
    public void testIdentityFunctions() throws Exception {
        XSDLibrary library = newXsdLibrary();
        XSDSimpleType simple = library.getSimpleType( "TestSimple" );
        XSDComplexType complex = library.getComplexType( "TestComplex" );
        XSDElement element = library.getElement( "TestElement" );

        assertEquals( simple.getNamespace(), simple.getNamespace() );
        assertEquals( simple.getName(), simple.getLocalName() );
        assertEquals( "TestLibrary.xsd : TestSimple", simple.getValidationIdentity() );

        assertEquals( complex.getNamespace(), complex.getNamespace() );
        assertEquals( complex.getName(), complex.getLocalName() );
        assertEquals( "TestLibrary.xsd : TestComplex", complex.getValidationIdentity() );

        assertEquals( element.getNamespace(), element.getNamespace() );
        assertEquals( element.getName(), element.getLocalName() );
        assertEquals( "TestLibrary.xsd : TestElement", element.getValidationIdentity() );
    }

    @Test
    public void testLibraryMemberAccessors() throws Exception {
        XSDLibrary library = newXsdLibrary();

        assertEquals( "TestLibrary", library.getName() );
        assertTrue( library.isChameleon() );

        assertNotNull( library.getSimpleType( "TestSimple" ) );
        assertNotNull( library.getComplexType( "TestComplex" ) );
        assertNotNull( library.getElement( "TestElement" ) );
    }

    @Test
    public void testComplexTypeAliases() throws Exception {
        XSDLibrary library = newXsdLibrary();
        XSDComplexType complex = library.getComplexType( "TestComplex" );
        XSDElement identityElement = new XSDElement( "IdentityElement", null );
        XSDElement aliasElement = new XSDElement( "AliasElement", null );

        complex.setIdentityAlias( identityElement );
        complex.addAlias( aliasElement );

        assertEquals( identityElement, complex.getIdentityAlias() );
        assertEquals( 1, complex.getAliases().size() );
        assertTrue( complex.getAliases().contains( aliasElement ) );

        complex.setIdentityAlias( null );
        complex.removeAlias( aliasElement );

        assertNull( complex.getIdentityAlias() );
        assertEquals( 0, complex.getAliases().size() );
    }

    private XSDLibrary newXsdLibrary() {
        File libraryFolder = new File( System.getProperty( "user.dir" ), "/src/test/resources/temp" );
        File libraryFile = new File( libraryFolder, "/TestLibrary.xsd" );
        XSDLibrary library = new XSDLibrary();
        XSDSimpleType simple = new XSDSimpleType( "TestSimple", null );
        XSDComplexType complex = new XSDComplexType( "TestComplex", null );
        XSDElement element = new XSDElement( "TestElement", null );

        library.setLibraryUrl( URLUtils.toURL( libraryFile ) );
        library.setNamespace( AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE );
        model.addLibrary( library );

        library.addNamedMember( simple );
        library.addNamedMember( complex );
        library.addNamedMember( element );

        return library;
    }

}
