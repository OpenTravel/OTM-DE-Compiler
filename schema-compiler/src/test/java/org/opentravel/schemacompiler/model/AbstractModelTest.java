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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.XMLConstants;

import org.junit.After;
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
	protected ModelIntegrityChecker integrityChecker;
	
	@Before
	public void setupTestLibraries() throws Exception {
		library1 = newLibrary( "http://www.opentravel.org/schemas/pkg1/v1", "TestLibrary1", "p1" );
		library2 = newLibrary( "http://www.opentravel.org/schemas/pkg2/v1", "TestLibrary2", "p2" );
		integrityChecker = new ModelIntegrityChecker();
		
		model = new TLModel();
		model.addListener( integrityChecker );
		model.addLibrary( library1 );
		model.addLibrary( library2 );
	}
	
	@After
	public void tearDownModel() throws Exception {
		model.removeListener( integrityChecker );
	}
	
	protected void testExtensionFunctions(TLExtensionOwner owner) throws Exception {
		TLExtension currentExtension = owner.getExtension();
		TLExtension altExtension = addExtension( owner, currentExtension.getExtendsEntity() );
		
		assertEquals( altExtension, owner.getExtension() );
		assertNull( currentExtension.getOwner() );
		
		owner.setExtension( null );
		assertNull( owner.getExtension() );
		assertNull( altExtension.getOwner() );
		
		owner.setExtension( currentExtension );
		assertEquals( owner, currentExtension.getOwner() );
	}
	
	protected void testAliasFunctions(TLAliasOwner owner) throws Exception {
		TLAlias alias1 = addAlias( "Alias1", owner );
		TLAlias alias2 = addAlias( "Alias2", owner );
		
		assertNotNull( owner.getAlias( "Alias1" ) );
		assertNotNull( owner.getAlias( "Alias2" ) );
		assertArrayEquals( new String[] { "Alias1", "Alias2" }, getNames( owner.getAliases(), a -> a.getName() ) );
		
		alias1.moveDown();
		assertArrayEquals( new String[] { "Alias2", "Alias1" }, getNames( owner.getAliases(), a -> a.getName() ) );
		
		owner.sortAliases( (a1, a2) -> a1.getName().compareTo( a2.getName() ) );
		assertArrayEquals( new String[] { "Alias1", "Alias2" }, getNames( owner.getAliases(), a -> a.getName() ) );
		
		alias2.moveUp();
		assertArrayEquals( new String[] { "Alias2", "Alias1" }, getNames( owner.getAliases(), a -> a.getName() ) );
		
		owner.removeAlias( alias1 );
		assertArrayEquals( new String[] { "Alias2" }, getNames( owner.getAliases(), a -> a.getName() ) );
	}
	
	protected void testAttributeFunctions(TLAttributeOwner owner) throws Exception {
		TLAttribute attr1 = addAttribute( "attr1", owner );
		TLAttribute attr2 = addAttribute( "attr2", owner );
		
		assertEquals( attr1, owner.getAttribute( "attr1" ) );
		assertEquals( attr2, owner.getAttribute( "attr2" ) );
		assertEquals( attr2, owner.getMemberField( "attr2" ) );
		assertMemberFieldNames( owner, "attr1", "attr2" );
		assertArrayEquals( new String[] { "attr1", "attr2" }, getNames( owner.getAttributes(), a -> a.getName() ) );
		
		attr1.moveDown();
		assertArrayEquals( new String[] { "attr2", "attr1" }, getNames( owner.getAttributes(), a -> a.getName() ) );
		
		owner.sortAttributes( (a1, a2) -> a1.getName().compareTo( a2.getName() ) );
		assertArrayEquals( new String[] { "attr1", "attr2" }, getNames( owner.getAttributes(), a -> a.getName() ) );
		
		attr2.moveUp();
		assertArrayEquals( new String[] { "attr2", "attr1" }, getNames( owner.getAttributes(), a -> a.getName() ) );
		
		owner.removeAttribute( attr1 );
		assertArrayEquals( new String[] { "attr2" }, getNames( owner.getAttributes(), a -> a.getName() ) );
	}
	
	protected void testPropertyFunctions(TLPropertyOwner owner) throws Exception {
		TLProperty element1 = addElement( "element1", owner );
		TLProperty element2 = addElement( "element2", owner );
		
		assertEquals( element1, owner.getElement( "element1" ) );
		assertEquals( element2, owner.getElement( "element2" ) );
		assertEquals( element2, owner.getMemberField( "element2" ) );
		assertMemberFieldNames( owner, "element1", "element2" );
		assertArrayEquals( new String[] { "element1", "element2" }, getNames( owner.getElements(), a -> a.getName() ) );
		
		element1.moveDown();
		assertArrayEquals( new String[] { "element2", "element1" }, getNames( owner.getElements(), a -> a.getName() ) );
		
		owner.sortElements( (e1, e2) -> e1.getName().compareTo( e2.getName() ) );
		assertArrayEquals( new String[] { "element1", "element2" }, getNames( owner.getElements(), a -> a.getName() ) );
		
		element2.moveUp();
		assertArrayEquals( new String[] { "element2", "element1" }, getNames( owner.getElements(), a -> a.getName() ) );
		
		owner.removeProperty( element1 );
		assertArrayEquals( new String[] { "element2" }, getNames( owner.getElements(), a -> a.getName() ) );
	}
	
	protected void testIndicatorFunctions(TLIndicatorOwner owner) throws Exception {
		TLIndicator indicator1 = addIndicator( "indicator1", owner );
		TLIndicator indicator2 = addIndicator( "indicator2", owner );
		
		assertEquals( indicator1, owner.getIndicator( "indicator1" ) );
		assertEquals( indicator2, owner.getIndicator( "indicator2" ) );
		assertEquals( indicator2, owner.getMemberField( "indicator2" ) );
		assertMemberFieldNames( owner, "indicator1", "indicator2" );
		assertArrayEquals( new String[] { "indicator1", "indicator2" }, getNames( owner.getIndicators(), a -> a.getName() ) );
		
		indicator1.moveDown();
		assertArrayEquals( new String[] { "indicator2", "indicator1" }, getNames( owner.getIndicators(), a -> a.getName() ) );
		
		owner.sortIndicators( (i1, i2) -> i1.getName().compareTo( i2.getName() ) );
		assertArrayEquals( new String[] { "indicator1", "indicator2" }, getNames( owner.getIndicators(), a -> a.getName() ) );
		
		indicator2.moveUp();
		assertArrayEquals( new String[] { "indicator2", "indicator1" }, getNames( owner.getIndicators(), a -> a.getName() ) );
		
		owner.removeIndicator( indicator1 );
		assertArrayEquals( new String[] { "indicator2" }, getNames( owner.getIndicators(), a -> a.getName() ) );
	}
	
	protected void testDocumentationFunctions(TLDocumentationOwner owner) throws Exception {
		TLDocumentation doc1 = new TLDocumentation();
		TLDocumentation doc2 = new TLDocumentation();
		
		owner.setDocumentation( doc1 );
		assertEquals( owner, doc1.getOwner() );
		
		owner.setDocumentation( doc2 );
		assertEquals( owner, doc2.getOwner() );
		assertNull( doc1.getOwner() );
		
		owner.setDocumentation( null );
		assertNull( doc2.getOwner() );
	}
	
	protected void testEquivalentFunctions(TLEquivalentOwner owner) throws Exception {
		TLEquivalent equiv1 = addEquivalent( "context1", owner );
		TLEquivalent equiv2 = addEquivalent( "context2", owner );
		
		assertEquals( equiv1, owner.getEquivalent( "context1" ) );
		assertEquals( equiv2, owner.getEquivalent( "context2" ) );
		assertArrayEquals( new String[] { "context1", "context2" }, getNames( owner.getEquivalents(), e -> e.getContext() ) );
		
		equiv1.moveDown();
		assertArrayEquals( new String[] { "context2", "context1" }, getNames( owner.getEquivalents(), e -> e.getContext() ) );
		
		owner.sortEquivalents( (e1, e2) -> e1.getContext().compareTo( e2.getContext() ) );
		assertArrayEquals( new String[] { "context1", "context2" }, getNames( owner.getEquivalents(), e -> e.getContext() ) );
		
		equiv2.moveUp();
		assertArrayEquals( new String[] { "context2", "context1" }, getNames( owner.getEquivalents(), e -> e.getContext() ) );
		
		owner.removeEquivalent( equiv1 );
		assertArrayEquals( new String[] { "context2" }, getNames( owner.getEquivalents(), e -> e.getContext() ) );
	}
	
	protected void testExampleFunctions(TLExampleOwner owner) throws Exception {
		TLExample example1 = addExample( "context1", owner );
		TLExample example2 = addExample( "context2", owner );
		
		assertEquals( example1, owner.getExample( "context1" ) );
		assertEquals( example2, owner.getExample( "context2" ) );
		assertArrayEquals( new String[] { "context1", "context2" }, getNames( owner.getExamples(), e -> e.getContext() ) );
		
		example1.moveDown();
		assertArrayEquals( new String[] { "context2", "context1" }, getNames( owner.getExamples(), e -> e.getContext() ) );
		
		owner.sortExamples( (e1, e2) -> e1.getContext().compareTo( e2.getContext() ) );
		assertArrayEquals( new String[] { "context1", "context2" }, getNames( owner.getExamples(), e -> e.getContext() ) );
		
		example2.moveUp();
		assertArrayEquals( new String[] { "context2", "context1" }, getNames( owner.getExamples(), e -> e.getContext() ) );
		
		owner.removeExample( example1 );
		assertArrayEquals( new String[] { "context2" }, getNames( owner.getExamples(), e -> e.getContext() ) );
	}
	
	private boolean assertMemberFieldNames(TLMemberFieldOwner owner, String... expectedFieldNames) {
		List<String> fieldNames = new ArrayList<>();
		boolean allFieldsFound = true;
		
		owner.getMemberFields().forEach( f -> fieldNames.add( f.getName() ));
		
		for (String expectedName : expectedFieldNames) {
			allFieldsFound &= fieldNames.contains( expectedName );
		}
		return allFieldsFound;
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
		assertEquals( bo, library.getBusinessObjectType( entityName ) );
		assertTrue( library.getNamedMembers().contains( bo ) );
		return bo;
	}
	
	protected TLChoiceObject addChoice(String entityName, TLLibrary library) {
		TLChoiceObject choice = new TLChoiceObject();
		
		choice.setName( entityName );
		addAttribute( "testAttr", choice.getSharedFacet() );
		library.addNamedMember( choice );
		assertEquals( choice, library.getChoiceObjectType( entityName ) );
		assertTrue( library.getNamedMembers().contains( choice ) );
		return choice;
	}
	
	protected TLCoreObject addCore(String entityName, TLLibrary library) {
		TLCoreObject core = new TLCoreObject();
		
		core.setName( entityName );
		addAttribute( "testAttr", core.getSummaryFacet() );
		library.addNamedMember( core );
		assertEquals( core, library.getCoreObjectType( entityName ) );
		assertTrue( library.getNamedMembers().contains( core ) );
		return core;
	}
	
	protected TLValueWithAttributes addVWA(String entityName, TLLibrary library) {
		TLValueWithAttributes vwa = new TLValueWithAttributes();
		
		vwa.setName( entityName );
		vwa.setParentType( (TLAttributeType) findEntity( XMLConstants.W3C_XML_SCHEMA_NS_URI, "string" ) );
		library.addNamedMember( vwa );
		assertEquals( vwa, library.getValueWithAttributesType( entityName ) );
		assertTrue( library.getNamedMembers().contains( vwa ) );
		return vwa;
	}
	
	protected TLSimple addSimple(String entityName, TLLibrary library) {
		TLSimple simple = new TLSimple();
		
		simple.setName( entityName );
		simple.setParentType( (TLAttributeType) findEntity( XMLConstants.W3C_XML_SCHEMA_NS_URI, "string" ) );
		library.addNamedMember( simple );
		assertEquals( simple, library.getSimpleType( entityName ) );
		assertTrue( library.getNamedMembers().contains( simple ) );
		return simple;
	}
	
	protected TLService addService(String serviceName, TLLibrary library) {
		TLService service = new TLService();
		
		service.setName( serviceName );
		library.addNamedMember( service );
		assertEquals( service, library.getService() );
		assertTrue( library.getNamedMembers().contains( service ) );
		return service;
	}
	
	protected TLOperation addOperation(String opName, TLService service) {
		TLOperation operation = new TLOperation();
		
		operation.setName( opName );
		service.addOperation( operation );
		assertEquals( operation, service.getOperation( opName ) );
		assertTrue( service.getOperations().contains( operation ) );
		return operation;
	}
	
	protected TLAlias addAlias(String aliasName, TLAliasOwner owner) {
		TLAlias alias = new TLAlias();
		
		alias.setName( aliasName );
		
		if (owner.getAliases().isEmpty()) {
			owner.addAlias( 0, alias );
			
		} else {
			owner.addAlias( alias );
		}
		return alias;
	}
	
	protected TLExtension addExtension(TLExtensionOwner owner, NamedEntity extendedEntity) {
		TLExtension extension = new TLExtension();
		
		owner.setExtension( extension );
		extension.setExtendsEntity( extendedEntity );
		return extension;
	}
	
	protected TLAttribute addAttribute(String attributeName, TLAttributeOwner facet) {
		TLAttribute attribute = new TLAttribute();
		
		attribute.setName( attributeName );
		attribute.setType( (TLPropertyType) findEntity( XMLConstants.W3C_XML_SCHEMA_NS_URI, "string" ) );
		
		if (facet.getAttributes().isEmpty()) {
			
		} else {
			
		}
		facet.addAttribute( attribute );
		return attribute;
	}
	
	protected TLProperty addElement(String elementName, TLPropertyOwner facet) {
		TLProperty element = new TLProperty();
		
		element.setName( elementName );
		element.setType( (TLPropertyType) findEntity( XMLConstants.W3C_XML_SCHEMA_NS_URI, "string" ) );
		
		if (facet.getElements().isEmpty()) {
			facet.addElement( 0, element );
			
		} else {
			facet.addElement( element );
		}
		return element;
	}
	
	protected TLIndicator addIndicator(String indicatorName, TLIndicatorOwner facet) {
		TLIndicator indicator = new TLIndicator();
		
		indicator.setName( indicatorName );
		
		if (facet.getIndicators().isEmpty()) {
			facet.addIndicator( 0, indicator );
			
		} else {
			facet.addIndicator( indicator );
		}
		return indicator;
	}
	
	protected TLEquivalent addEquivalent(String contextId, TLEquivalentOwner owner) {
		TLEquivalent equiv = new TLEquivalent();
		
		equiv.setContext( contextId );
		equiv.setDescription( contextId + " : Description" );
		
		if (owner.getEquivalents().isEmpty()) {
			owner.addEquivalent( 0, equiv );
			
		} else {
			owner.addEquivalent( equiv );
		}
		return equiv;
	}
	
	protected TLExample addExample(String contextId, TLExampleOwner owner) {
		TLExample example = new TLExample();
		
		example.setContext( contextId );
		example.setValue( contextId + "_value" );
		
		if (owner.getExamples().isEmpty()) {
			owner.addExample( 0, example );
			
		} else {
			owner.addExample( example );
		}
		return example;
	}
	
	protected TLContextualFacet newContextualFacet(String name, TLFacetType type, TLLibrary library) {
		TLContextualFacet facet = new TLContextualFacet();
		
		facet.setName( name );
		facet.setFacetType( type );
		library.addNamedMember( facet );
		return facet;
	}
	
	protected TLResource addResource(String resourceName, TLLibrary library) {
		TLBusinessObject bo = addBusinessObject( resourceName + "BO", library );
		TLResource resource = new TLResource();
		
		resource.setName( resourceName );
		resource.setBusinessObjectRef( bo );
		resource.setFirstClass( true );
		library.addNamedMember( resource );
		assertEquals( resource, library.getResourceType( resourceName ) );
		assertTrue( library.getNamedMembers().contains( resource ) );
		return resource;
	}
	
	protected TLResourceParentRef addParentRef(TLResource parentResource, TLParamGroup parentParamGroup,
			TLResource resource) {
		TLResourceParentRef parentRef = new TLResourceParentRef();
		
		parentRef.setParentResource( parentResource );
		parentRef.setParentParamGroup( parentParamGroup );
		resource.addParentRef( resource.getParentRefs().size(), parentRef );
		assertEquals( parentRef, resource.getParentRef( parentResource.getName() + "/" + parentParamGroup.getName() ) );
		assertTrue( resource.getParentRefs().contains( parentRef ) );
		return parentRef;
	}
	
	protected TLParamGroup addParamGroup(String paramGroupName, TLFacet facetRef, TLResource resource) {
		TLParamGroup paramGroup = new TLParamGroup();
		
		paramGroup.setName( paramGroupName );
		paramGroup.setFacetRef( facetRef );
		resource.addParamGroup( resource.getParamGroups().size(), paramGroup );
		assertEquals( paramGroup, resource.getParamGroup( paramGroupName ) );
		assertTrue( resource.getParamGroups().contains( paramGroup ) );
		return paramGroup;
	}
	
	protected TLParameter addParameter(TLMemberField<?> fieldRef, TLParamLocation location, TLParamGroup paramGroup) {
		TLParameter parameter = new TLParameter();
		
		parameter.setFieldRef( fieldRef );
		parameter.setLocation( location );
		paramGroup.addParameter( paramGroup.getParameters().size(), parameter );
		assertEquals( parameter, paramGroup.getParameter( fieldRef.getName() ) );
		assertTrue( paramGroup.getParameters().contains( parameter ) );
		return parameter;
	}
	
	protected TLActionFacet addActionFacet(String actionFacetName, TLReferenceType refType, String refFacetName,
			int refFacetRepeat, NamedEntity basePayload, TLResource resource) {
		TLActionFacet actionFacet = new TLActionFacet();
		
		actionFacet.setName( actionFacetName );
		actionFacet.setReferenceType( refType );
		actionFacet.setReferenceFacetName( refFacetName );
		actionFacet.setReferenceRepeat( refFacetRepeat );
		actionFacet.setBasePayload( basePayload );
		resource.addActionFacet( resource.getActionFacets().size(), actionFacet );
		assertEquals( actionFacet, resource.getActionFacet( actionFacetName ) );
		assertTrue( resource.getActionFacets().contains( actionFacet ) );
		return actionFacet;
	}
	
	protected TLAction addAction(String actionId, TLResource resource) {
		TLAction action = new TLAction();
		
		action.setActionId( actionId );
		action.setCommonAction( false );
		resource.addAction( action );
		assertEquals( action, resource.getAction( actionId ) );
		assertTrue( resource.getActions().contains( action ) );
		return action;
	}
	
	protected TLActionRequest addActionRequest(String pathTemplate, TLParamGroup paramGroup,
			TLHttpMethod method, TLActionFacet payloadType, TLAction action) {
		TLActionRequest request = new TLActionRequest();
		
		request.setPathTemplate( pathTemplate );
		request.setParamGroup( paramGroup );
		request.setHttpMethod( method );
		request.setPayloadType( payloadType );
		request.setMimeTypes( Arrays.asList( TLMimeType.APPLICATION_XML, TLMimeType.APPLICATION_JSON ) );
		action.setRequest( request );
		return request;
	}
	
	protected TLActionResponse addActionResponse(Integer statusCode, TLActionFacet payloadType, TLAction action) {
		TLActionResponse response = new TLActionResponse();
		
		response.setStatusCodes( Arrays.asList( statusCode ) );
		response.setPayloadType( payloadType );
		response.setMimeTypes( Arrays.asList( TLMimeType.APPLICATION_XML, TLMimeType.APPLICATION_JSON ) );
		action.addResponse( action.getResponses().size(), response );
		assertTrue( action.getResponses().contains( response ) );
		return response;
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
