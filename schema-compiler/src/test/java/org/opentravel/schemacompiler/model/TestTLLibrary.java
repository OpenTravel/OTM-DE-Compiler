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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Verifies the functions of the <code>TLLibrary</code> class.
 */
public class TestTLLibrary extends AbstractModelTest {
	
	@Test
	public void testNamespaceAndVersionFunctions() throws Exception {
		TLLibrary errorLibrary = new TLLibrary();
		String baseNS = library1.getBaseNamespace();
		String version = library1.getVersion();
		
		library1.setNamespaceAndVersion( baseNS, version );
		assertEquals( baseNS, library1.getBaseNamespace() );
		assertEquals( version, library1.getVersion() );
		
		library1.setVersion( "2.0.0" );
		assertEquals( baseNS + "/v2", library1.getNamespace() );
		testNegativeCase( library1, l -> library1.setVersion( "a.b.c" ), IllegalArgumentException.class );
		
		library1.setVersionScheme( null );
		assertNull( library1.getVersion() );
		testNegativeCase( library1, l -> library1.setVersion( "1.0.0" ), IllegalStateException.class );
		testNegativeCase( library1, l -> library1.setVersion( null ), IllegalStateException.class );
		
		// Not an error to assign an invalid version scheme - should behave as if one
		// is not assigned
		errorLibrary.setVersionScheme( "INVALID" );
		assertEquals( "INVALID", errorLibrary.getVersionScheme() );
		testNegativeCase( errorLibrary, l -> errorLibrary.setVersion( "1.0.0" ), IllegalStateException.class );
		testNegativeCase( errorLibrary, l -> errorLibrary.setVersion( null ), IllegalStateException.class );
		
		errorLibrary.setVersionScheme( null );
		testNegativeCase( errorLibrary, l -> errorLibrary.setVersion( "1.0.0" ), IllegalStateException.class );
		testNegativeCase( errorLibrary, l -> errorLibrary.setVersion( null ), IllegalStateException.class );
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testNegativeAddMember() throws Exception {
		library1.addNamedMember( new XSDSimpleType( "error", null ) );
	}
	
	@Test
	public void testNegativeMoveScenarios() throws Exception {
		TLCoreObject entity1 = addCore( "TestObject1", library1 );
		TLCoreObject entity2 = addCore( "TestObject2", library2 );
		TLService service1 = addService( "TestService1", library1 );
		TLService service2 = addService( "TestService2", library2 );
		TLOperation op1 = addOperation( "TestOperation", service1 );
		
		addOperation( "TestOperation", service2 );
		
		// Negative case of a non-move (but does not throw an exception)
		library1.moveNamedMember( entity1, library1 );
		assertEquals( library1, entity1.getOwningLibrary() );
		
		testNegativeCase( library1, l -> library1.moveNamedMember( null, library2 ), NullPointerException.class );
		testNegativeCase( library1, l -> library1.moveNamedMember( entity1, null ), NullPointerException.class );
		testNegativeCase( library1, l -> library1.moveNamedMember( entity2, library2 ), IllegalArgumentException.class );
		testNegativeCase( library1, l -> library1.moveNamedMember( entity1.getSummaryFacet(), library2 ), IllegalArgumentException.class );
		testNegativeCase( library1, l -> library1.moveNamedMember( service1, library2 ), IllegalStateException.class );
		testNegativeCase( library1, l -> library1.moveNamedMember( op1, library2 ), IllegalStateException.class );
	}
	
	@Test
	public void testMoveEntity() throws Exception {
		TLCoreObject entity = addCore( "TestObject", library1 );
		
		library1.moveNamedMember( entity, library2 );
		assertEquals( library2, entity.getOwningLibrary() );
	}
	
	@Test
	public void testMoveService() throws Exception {
		TLService service = addService( "TestService", library1 );
		
		library1.moveNamedMember( service, library2 );
		assertEquals( library2, service.getOwningLibrary() );
	}
	
	@Test
	public void testMoveOperationToExistingService() throws Exception {
		TLService service1 = addService( "TestService1", library1 );
		TLService service2 = addService( "TestService2", library2 );
		TLOperation operation = addOperation( "TestOperation", service1 );
		
		library1.moveNamedMember( operation, library2 );
		assertEquals( service2, operation.getOwningService() );
		assertEquals( library2, operation.getOwningLibrary() );
	}
	
	@Test
	public void testMoveOperationToNewService() throws Exception {
		TLService service1 = addService( "TestService1", library1 );
		TLOperation operation = addOperation( "TestOperation", service1 );
		TLService service2;
		
		library1.moveNamedMember( operation, library2 );
		service2 = library2.getService();
		
		assertNotNull( service2 );
		assertEquals( service1.getName(), service2.getName() );
		assertEquals( service2, operation.getOwningService() );
		assertEquals( library2, operation.getOwningLibrary() );
	}
	
	@Test
	public void testLibraryStatus() throws Exception {
		org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus[] rStatuses =
				org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus.values();
		TLLibraryStatus[] lStatuses = TLLibraryStatus.values();
		
		for (int i = 0; i < rStatuses.length; i++) {
			assertEquals( lStatuses[i], TLLibraryStatus.fromRepositoryStatus( rStatuses[i] ) );
			assertEquals( rStatuses[i], lStatuses[i].toRepositoryStatus() );
		}
		
		assertEquals( null, TLLibraryStatus.DRAFT.previousStatus() );
		assertEquals( TLLibraryStatus.DRAFT, TLLibraryStatus.UNDER_REVIEW.previousStatus() );
		assertEquals( TLLibraryStatus.UNDER_REVIEW, TLLibraryStatus.FINAL.previousStatus() );
		assertEquals( TLLibraryStatus.FINAL, TLLibraryStatus.OBSOLETE.previousStatus() );
		
		assertEquals( TLLibraryStatus.UNDER_REVIEW, TLLibraryStatus.DRAFT.nextStatus() );
		assertEquals( TLLibraryStatus.FINAL, TLLibraryStatus.UNDER_REVIEW.nextStatus() );
		assertEquals( TLLibraryStatus.OBSOLETE, TLLibraryStatus.FINAL.nextStatus() );
		assertEquals( null, TLLibraryStatus.OBSOLETE.nextStatus() );
	}
	
	@Test
	public void testContextFunctions() throws Exception {
		TLContext context1 = addContext( "context1", "http://www.opentravel.org/context1", library1 );
		TLContext context2 = addContext( "context2", "http://www.opentravel.org/context2", library1 );
		
		assertEquals( context1, library1.getContext( "context1" ) );
		assertEquals( context2, library1.getContext( "context2" ) );
		assertEquals( context1, library1.getContextByApplicationContext( context1.getApplicationContext() ) );
		assertArrayEquals( new String[] { "context1", "context2" }, getNames( library1.getContexts(), c -> c.getContextId() ) );
		
		context1.moveDown();
		assertArrayEquals( new String[] { "context2", "context1" }, getNames( library1.getContexts(), c -> c.getContextId() ) );
		
		library1.sortContexts( (c1, c2) -> c1.getContextId().compareTo( c2.getContextId() ) );
		assertArrayEquals( new String[] { "context1", "context2" }, getNames( library1.getContexts(), c -> c.getContextId() ) );
		
		context2.moveUp();
		assertArrayEquals( new String[] { "context2", "context1" }, getNames( library1.getContexts(), c -> c.getContextId() ) );
		
		library1.removeContext( context1 );
		assertArrayEquals( new String[] { "context2" }, getNames( library1.getContexts(), c -> c.getContextId() ) );
	}
	
	protected TLContext addContext(String contextId, String applicationContext, TLLibrary library) {
		TLContext context = new TLContext();
		
		context.setContextId( contextId );
		context.setApplicationContext( applicationContext );
		library.addContext( context );
		return context;
	}
	
}
