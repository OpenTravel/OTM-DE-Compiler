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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.opentravel.schemacompiler.model.BuiltInLibrary.BuiltInType;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Verifies the functions of the <code>BuiltInLibrary</code> class.
 */
public class TestBuiltInLibrary extends AbstractModelTest {
	
	@Test
	public void testConstructor() throws Exception {
		BuiltInLibrary library = newBuiltIn();
		
		assertEquals( BuiltInType.TLLIBRARY_BUILTIN, library.getBuiltInType() );
		assertEquals( 2, library.getNamespaceImports().size() );
	}
	
	@Test
	public void testUnsupportedOperations() throws Exception {
		BuiltInLibrary library = newBuiltIn();
		
		testNegativeCase( library, l -> library.setLibraryUrl( null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.setName( null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.setNamespace( null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.getIncludes(), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.addInclude( null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.addInclude( 0, null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.removeInclude( null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.moveUp( (TLInclude) null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.moveDown( (TLInclude) null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.sortIncludes( null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.addNamespaceImport( null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.addNamespaceImport( 0, null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.addNamespaceImport( null, null, null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.removeNamespaceImport( (TLNamespaceImport) null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.removeNamespaceImport( (String) null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.moveUp( (TLNamespaceImport) null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.moveDown( (TLNamespaceImport) null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.sortNamespaceImports( null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.addNamedMember( null ), UnsupportedOperationException.class );
		testNegativeCase( library, l -> library.removeNamedMember( null ), UnsupportedOperationException.class );
	}
	
	private BuiltInLibrary newBuiltIn() throws Exception {
		File libraryFolder = new File( System.getProperty( "user.dir" ), "/src/test/resources/temp" );
		File libraryFile = new File( libraryFolder, "/TestBuiltIn.otm" );
		List<TLNamespaceImport> importList = new ArrayList<>();
		TLNamespaceImport nsImport1 = new TLNamespaceImport( "ns1", "http://www.opentravel.org/ns1" );
		TLNamespaceImport nsImport2 = new TLNamespaceImport( "ns2", "http://www.opentravel.org/ns2" );
		
		importList.add( nsImport1 );
		importList.add( nsImport2 );
		
		return new BuiltInLibrary( new QName( "http://www.opentravel.org/ns/builtin", "TestBuiltIn" ),
				URLUtils.toURL( libraryFile ), Collections.emptyList(), importList, null,
				VersionSchemeFactory.getInstance().getDefaultVersionScheme() );
	}
	
}
