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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.XMLConstants;

import org.junit.Before;
import org.opentravel.schemacompiler.ic.ModelIntegrityChecker;
import org.opentravel.schemacompiler.util.URLUtils;


/**
 * Base class for all model unit tests.
 */
public abstract class AbstractModelTest {
	
	protected TLModel model;
	protected TLLibrary library1;
	protected TLLibrary library2;
	
	@Before
	public void setupTestLibraries() throws Exception {
		library1 = newLibrary( "http://www.opentravel.org/schemas/pkg1/v1", "TestLibrary1", "p1" );
		library2 = newLibrary( "http://www.opentravel.org/schemas/pkg2/v1", "TestLibrary2", "p2" );
		
		model = new TLModel();
		model.addListener( new ModelIntegrityChecker() );
		model.addLibrary( library1 );
		model.addLibrary( library2 );
	}
	
	protected void testAliasOwnerFunctions(TLAliasOwner owner) throws Exception {
		TLAlias alias1 = addAlias( "Alias1", owner );
		
		addAlias( "Alias2", owner );
		assertNotNull( owner.getAlias( "Alias1" ) );
		assertNotNull( owner.getAlias( "Alias2" ) );
		assertArrayEquals( new String[] { "Alias1", "Alias2" }, getNames( owner.getAliases(), a -> a.getName() ) );
		
		owner.moveDown( alias1 );
		assertArrayEquals( new String[] { "Alias2", "Alias1" }, getNames( owner.getAliases(), a -> a.getName() ) );
		
		owner.moveUp( alias1 );
		assertArrayEquals( new String[] { "Alias1", "Alias2" }, getNames( owner.getAliases(), a -> a.getName() ) );
		
		owner.removeAlias( alias1 );
		assertArrayEquals( new String[] { "Alias2" }, getNames( owner.getAliases(), a -> a.getName() ) );
	}
	
	protected void testAttributeOwnerFunctions(TLAttributeOwner owner) throws Exception {
		TLAttribute attr1 = addAttribute( "attr1", owner );
		
		addAttribute( "attr2", owner );
		assertNotNull( owner.getAttribute( "attr1" ) );
		assertNotNull( owner.getAttribute( "attr2" ) );
		assertArrayEquals( new String[] { "attr1", "attr2" }, getNames( owner.getAttributes(), a -> a.getName() ) );
		
		owner.moveDown( attr1 );
		assertArrayEquals( new String[] { "attr2", "attr1" }, getNames( owner.getAttributes(), a -> a.getName() ) );
		
		owner.moveUp( attr1 );
		assertArrayEquals( new String[] { "attr1", "attr2" }, getNames( owner.getAttributes(), a -> a.getName() ) );
		
		owner.removeAttribute( attr1 );
		assertArrayEquals( new String[] { "attr2" }, getNames( owner.getAttributes(), a -> a.getName() ) );
	}
	
	protected void testPropertyOwnerFunctions(TLPropertyOwner owner) throws Exception {
		TLProperty element1 = addElement( "element1", owner );
		
		addElement( "element2", owner );
		assertNotNull( owner.getElement( "element1" ) );
		assertNotNull( owner.getElement( "element2" ) );
		assertArrayEquals( new String[] { "element1", "element2" }, getNames( owner.getElements(), a -> a.getName() ) );
		
		owner.moveDown( element1 );
		assertArrayEquals( new String[] { "element2", "element1" }, getNames( owner.getElements(), a -> a.getName() ) );
		
		owner.moveUp( element1 );
		assertArrayEquals( new String[] { "element1", "element2" }, getNames( owner.getElements(), a -> a.getName() ) );
		
		owner.removeProperty( element1 );
		assertArrayEquals( new String[] { "element2" }, getNames( owner.getElements(), a -> a.getName() ) );
	}
	
	protected void testIndicatorOwnerFunctions(TLIndicatorOwner owner) throws Exception {
		TLIndicator indicator1 = addIndicator( "indicator1", owner );
		
		addIndicator( "indicator2", owner );
		assertNotNull( owner.getIndicator( "indicator1" ) );
		assertNotNull( owner.getIndicator( "indicator2" ) );
		assertArrayEquals( new String[] { "indicator1", "indicator2" }, getNames( owner.getIndicators(), a -> a.getName() ) );
		
		owner.moveDown( indicator1 );
		assertArrayEquals( new String[] { "indicator2", "indicator1" }, getNames( owner.getIndicators(), a -> a.getName() ) );
		
		owner.moveUp( indicator1 );
		assertArrayEquals( new String[] { "indicator1", "indicator2" }, getNames( owner.getIndicators(), a -> a.getName() ) );
		
		owner.removeIndicator( indicator1 );
		assertArrayEquals( new String[] { "indicator2" }, getNames( owner.getIndicators(), a -> a.getName() ) );
	}
	
	protected <T> void testNegativeCase(T obj, Consumer<T> action, Class<? extends Exception> expectedException) throws Exception {
		try {
			action.accept( obj );
			fail( "Expected exception not thrown: " + expectedException.getSimpleName() );
			
		} catch (Exception e) {
			if (!expectedException.isAssignableFrom( e.getClass() )) {
				fail( "Unexpected exception encountered: " + e.getClass().getSimpleName() +
						"(expecting "+ expectedException.getSimpleName() + ")");
			}
		}
	}
	
	protected TLLibrary newLibrary(String ns, String name, String prefix) {
		File libraryFolder = new File( System.getProperty( "user.dir" ), "/src/test/resources/temp" );
		File libraryFile = new File( libraryFolder, "/" + name + ".otm" );
		TLLibrary library = new TLLibrary();
		
		library.setLibraryUrl( URLUtils.toURL( libraryFile ) );
		library.setNamespace( ns );
		library.setName( name );
		library.setPrefix( prefix );
		return library;
	}
	
	protected TLBusinessObject addBusinessObject(String entityName, TLLibrary library) {
		TLBusinessObject bo = new TLBusinessObject();
		
		bo.setName( entityName );
		addAttribute( "testAttr", bo.getIdFacet() );
		library.addNamedMember( bo );
		return bo;
	}
	
	protected TLChoiceObject addChoice(String entityName, TLLibrary library) {
		TLChoiceObject choice = new TLChoiceObject();
		
		choice.setName( entityName );
		addAttribute( "testAttr", choice.getSharedFacet() );
		library.addNamedMember( choice );
		return choice;
	}
	
	protected TLCoreObject addCore(String entityName, TLLibrary library) {
		TLCoreObject core = new TLCoreObject();
		
		core.setName( entityName );
		addAttribute( "testAttr", core.getSummaryFacet() );
		library.addNamedMember( core );
		return core;
	}
	
	protected TLService addService(String serviceName, TLLibrary library) {
		TLService service = new TLService();
		
		service.setName( serviceName );
		library.addNamedMember( service );
		return service;
	}
	
	protected TLOperation addOperation(String opName, TLService service) {
		TLOperation operation = new TLOperation();
		
		operation.setName( opName );
		service.addOperation( operation );
		return operation;
	}
	
	protected TLAlias addAlias(String aliasName, TLAliasOwner owner) {
		TLAlias alias = new TLAlias();
		
		alias.setName( aliasName );
		owner.addAlias( alias );
		return alias;
	}
	
	protected TLAttribute addAttribute(String attributeName, TLAttributeOwner facet) {
		TLAttribute attribute = new TLAttribute();
		
		attribute.setName( attributeName );
		attribute.setType( (TLPropertyType) findEntity( XMLConstants.W3C_XML_SCHEMA_NS_URI, "string" ) );
		facet.addAttribute( attribute );
		return attribute;
	}
	
	protected TLProperty addElement(String elementName, TLPropertyOwner facet) {
		TLProperty element = new TLProperty();
		
		element.setName( elementName );
		element.setType( (TLPropertyType) findEntity( XMLConstants.W3C_XML_SCHEMA_NS_URI, "string" ) );
		facet.addElement( element );
		return element;
	}
	
	protected TLIndicator addIndicator(String indicatorName, TLIndicatorOwner facet) {
		TLIndicator indicator = new TLIndicator();
		
		indicator.setName( indicatorName );
		facet.addIndicator( indicator );
		return indicator;
	}
	
	protected TLContextualFacet newContextualFacet(String name, TLFacetType type, TLLibrary library) {
		TLContextualFacet facet = new TLContextualFacet();
		
		facet.setName( name );
		facet.setFacetType( type );
		library.addNamedMember( facet );
		return facet;
	}
	
	protected NamedEntity findEntity(String ns, String name) {
		NamedEntity entity = null;
		
		for (AbstractLibrary library : model.getLibrariesForNamespace( ns )) {
			entity = library.getNamedMember( name );
			if (entity != null) break;
		}
		assertNotNull( entity );
		return entity;
	}
	
	protected <T> String[] getNames(List<T> objectList, Function<T,String> nameFunction) {
		List<String> names = new ArrayList<>();
		
		objectList.forEach( obj -> names.add( nameFunction.apply( obj ) ) );
		return names.toArray( new String[ names.size() ] );
	}
	
}
