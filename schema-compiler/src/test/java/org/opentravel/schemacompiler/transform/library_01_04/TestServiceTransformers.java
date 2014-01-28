/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.library_01_04;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_04.Service;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLService</code> objects.
 *
 * @author S. Livezey
 */
public class TestServiceTransformers extends Abstract_1_4_TestTransformers {
	
	@Test
	public void testServiceTransformer() throws Exception {
		TLService service = getService(PACKAGE_2_NAMESPACE, "library_1_p2");
		
		assertNotNull(service);
		assertEquals("SampleService", service.getName());
		assertEquals(PACKAGE_2_NAMESPACE, service.getNamespace());
		assertNotNull(service.getEquivalents());
		assertEquals(1, service.getEquivalents().size());
		assertEquals("test", service.getEquivalents().get(0).getContext());
		assertEquals("SampleService-equivalent", service.getEquivalents().get(0).getDescription());
		assertNotNull(service.getDocumentation());
		
		assertNotNull(service.getOperations());
		assertEquals(2, service.getOperations().size());
		
		assertEquals("RequestResponseOperation", service.getOperations().get(0).getName());
		assertNotNull(service.getOperations().get(0).getEquivalents());
		assertEquals(1, service.getOperations().get(0).getEquivalents().size());
		assertEquals("test", service.getOperations().get(0).getEquivalents().get(0).getContext());
		assertEquals("RequestResponseOperation-equivalent", service.getOperations().get(0).getEquivalents().get(0).getDescription());
		assertTrue(service.getOperations().get(0).getRequest().declaresContent());
		assertTrue(service.getOperations().get(0).getResponse().declaresContent());
		assertFalse(service.getOperations().get(0).getNotification().declaresContent());
		
		assertEquals("NotificationOperation", service.getOperations().get(1).getName());
		assertNotNull(service.getOperations().get(1).getEquivalents());
		assertEquals(1, service.getOperations().get(1).getEquivalents().size());
		assertEquals("test", service.getOperations().get(1).getEquivalents().get(0).getContext());
		assertEquals("NotificationOperation-equivalent", service.getOperations().get(1).getEquivalents().get(0).getDescription());
		assertTrue(service.getOperations().get(1).getRequest().declaresContent());
		assertFalse(service.getOperations().get(1).getResponse().declaresContent());
		assertTrue(service.getOperations().get(1).getNotification().declaresContent());
	}
	
	@Test
	public void testTLServiceTransformer() throws Exception {
		Service service = transformService(PACKAGE_2_NAMESPACE, "library_1_p2");
		
		assertNotNull(service);
		assertEquals("SampleService", service.getName());
		assertEquals(1, service.getEquivalent().size());
		assertEquals("test", service.getEquivalent().get(0).getContext());
		assertEquals("SampleService-equivalent", service.getEquivalent().get(0).getValue());
		assertNotNull(service.getDocumentation());
		
		assertNotNull(service.getOperation());
		assertEquals(2, service.getOperation().size());
		
		assertEquals("RequestResponseOperation", service.getOperation().get(0).getName());
		assertEquals(1, service.getOperation().get(0).getEquivalent().size());
		assertEquals("test", service.getOperation().get(0).getEquivalent().get(0).getContext());
		assertEquals("RequestResponseOperation-equivalent", service.getOperation().get(0).getEquivalent().get(0).getValue());
		assertNotNull(service.getOperation().get(0).getRequest());
		assertNotNull(service.getOperation().get(0).getResponse());
		assertNotNull(service.getOperation().get(0).getNotification());
		
		assertEquals("NotificationOperation", service.getOperation().get(1).getName());
		assertEquals(1, service.getOperation().get(1).getEquivalent().size());
		assertEquals("test", service.getOperation().get(1).getEquivalent().get(0).getContext());
		assertEquals("NotificationOperation-equivalent", service.getOperation().get(1).getEquivalent().get(0).getValue());
		assertNotNull(service.getOperation().get(1).getRequest());
		assertNotNull(service.getOperation().get(1).getResponse());
		assertNotNull(service.getOperation().get(1).getNotification());
	}
	
	private TLService getService(String namespace, String libraryName) throws Exception {
		TLLibrary library = getLibrary(namespace, libraryName);
		
		return (library == null) ? null : library.getService();
	}
	
	private Service transformService(String namespace, String libraryName) throws Exception {
		TLService origService = getService(namespace, libraryName);
		TransformerFactory<SymbolResolverTransformerContext> factory =
				TransformerFactory.getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
						getContextJAXBTransformation(origService.getOwningLibrary()));
		ObjectTransformer<TLService,Service,SymbolResolverTransformerContext> transformer =
				factory.getTransformer(origService, Service.class);
		
		return transformer.transform(origService);
	}
	
}
