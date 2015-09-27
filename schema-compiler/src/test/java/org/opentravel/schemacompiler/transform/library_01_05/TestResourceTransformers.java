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
package org.opentravel.schemacompiler.transform.library_01_05;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_05.HttpMethod;
import org.opentravel.ns.ota2.librarymodel_v01_05.MimeType;
import org.opentravel.ns.ota2.librarymodel_v01_05.ReferenceType;
import org.opentravel.ns.ota2.librarymodel_v01_05.Resource;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLResource</code> objects.
 *
 * @author S. Livezey
 */
public class TestResourceTransformers extends Abstract_1_5_TestTransformers {
	
    @Test
    public void testResourceTransformer() throws Exception {
    	TLResource type = getResource(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleResource");
        
        assertNotNull(type);
        assertEquals("SampleResource", type.getName());
        assertNotNull(type.getDocumentation());
        assertNotNull(type.getDocumentation().getDescription());
        assertEquals("SampleResource-documentation-line_1", type.getDocumentation().getDescription());
        
        assertEquals(1, type.getParentRefs().size());
        assertNotNull(type.getParentRefs().get(0).getDocumentation());
        assertNotNull(type.getParentRefs().get(0).getDocumentation().getDescription());
        assertEquals("SampleResource-ParentRef-documentation-line_1",
        		type.getParentRefs().get(0).getDocumentation().getDescription());
        assertNotNull(type.getParentRefs().get(0).getParentResource());
        assertEquals("ParentResource", type.getParentRefs().get(0).getParentResource().getLocalName());
        assertEquals("ParentResource", type.getParentRefs().get(0).getParentResourceName());
        assertNotNull(type.getParentRefs().get(0).getParentParamGroup());
        assertEquals("IDParameters", type.getParentRefs().get(0).getParentParamGroup().getName());
        assertEquals("IDParameters", type.getParentRefs().get(0).getParentParamGroupName());
        
        assertNotNull(type.getExtension());
        assertNotNull(type.getExtension().getExtendsEntity());
        assertEquals("BaseResource", type.getExtension().getExtendsEntity().getLocalName());
        assertEquals("BaseResource", type.getExtension().getExtendsEntityName());
        
        assertEquals(2, type.getParamGroups().size());
        assertEquals("IDParameters", type.getParamGroups().get(0).getName());
        assertTrue(type.getParamGroups().get(0).isIdGroup());
        assertNotNull(type.getParamGroups().get(0).getDocumentation());
        assertNotNull(type.getParamGroups().get(0).getDocumentation().getDescription());
        assertEquals("IDParameters-documentation-line_1",
        		type.getParamGroups().get(0).getDocumentation().getDescription());
        assertNotNull(type.getParamGroups().get(0).getFacetRef());
        assertEquals(TLFacetType.ID, type.getParamGroups().get(0).getFacetRef().getFacetType());
        assertEquals("SampleBusinessObject_ID", type.getParamGroups().get(0).getFacetRefName());
        
        assertEquals(1, type.getParamGroups().get(0).getParameters().size());
        assertNotNull(type.getParamGroups().get(0).getParameters().get(0).getFieldRef());
        assertEquals("sample_oid", type.getParamGroups().get(0).getParameters().get(0).getFieldRef().getName());
        assertEquals("sample_oid", type.getParamGroups().get(0).getParameters().get(0).getFieldRefName());
        assertNotNull( type.getParamGroups().get(0).getParameters().get(0).getEquivalent("test"));
        assertEquals("Param-sample_oid-equivalent", 
        		type.getParamGroups().get(0).getParameters().get(0).getEquivalent("test").getDescription());
        assertNotNull( type.getParamGroups().get(0).getParameters().get(0).getExample("test"));
        assertEquals("sample_oid-ex",  type.getParamGroups().get(0).getParameters().get(0).getExample("test").getValue());
        
        assertEquals(3, type.getActionFacets().size());
        assertEquals("ObjectOnly", type.getActionFacets().get(0).getName());
        assertFalse(type.getActionFacets().get(0).isNotExtendable());
        assertNotNull(type.getActionFacets().get(0).getDocumentation());
        assertNotNull(type.getActionFacets().get(0).getDocumentation().getDescription());
        assertEquals("ActionFacet-ObjectOnly-documentation-line_1",
        		type.getActionFacets().get(0).getDocumentation().getDescription());
        assertEquals(TLReferenceType.REQUIRED, type.getActionFacets().get(0).getReferenceType());
        assertEquals("SUMMARY", type.getActionFacets().get(1).getReferenceFacetName());
        assertEquals(1000, type.getActionFacets().get(1).getReferenceRepeat());
        assertEquals(1, type.getActionFacets().get(2).getAttributes().size());
        assertEquals(1, type.getActionFacets().get(2).getElements().size());
        assertEquals(1, type.getActionFacets().get(2).getIndicators().size());
        
        assertEquals(3, type.getActions().size());
        assertEquals("Update", type.getActions().get(1).getActionId());
        assertEquals("/sample/{sample_oid}", type.getActions().get(1).getPathTemplate());
        
        assertEquals(TLHttpMethod.PUT, type.getActions().get(1).getRequest().getHttpMethod());
        assertNotNull(type.getActions().get(1).getRequest().getParamGroup());
        assertEquals("IDParameters", type.getActions().get(1).getRequest().getParamGroup().getName());
        assertEquals("IDParameters", type.getActions().get(1).getRequest().getParamGroupName());
        assertNotNull(type.getActions().get(1).getRequest().getActionFacet());
        assertEquals("ObjectOnly", type.getActions().get(1).getRequest().getActionFacet().getName());
        assertEquals("SampleResource_ObjectOnly", type.getActions().get(1).getRequest().getActionFacetName());
        assertEquals(2, type.getActions().get(1).getRequest().getMimeTypes().size());
        assertEquals(TLMimeType.APPLICATION_XML, type.getActions().get(1).getRequest().getMimeTypes().get(0));
        assertEquals(TLMimeType.APPLICATION_JSON, type.getActions().get(1).getRequest().getMimeTypes().get(1));
        assertNotNull(type.getActions().get(1).getRequest().getDocumentation());
        assertNotNull(type.getActions().get(1).getRequest().getDocumentation().getDescription());
        assertEquals("Action-Update-Request-documentation-line_1",
        		type.getActions().get(1).getRequest().getDocumentation().getDescription());
        
        assertEquals(2, type.getActions().get(2).getResponses().size());
        assertEquals(2, type.getActions().get(2).getResponses().get(1).getStatusCodes().size());
        assertEquals(403, type.getActions().get(2).getResponses().get(1).getStatusCodes().get(0).intValue());
        assertEquals(500, type.getActions().get(2).getResponses().get(1).getStatusCodes().get(1).intValue());
        assertNotNull(type.getActions().get(2).getResponses().get(0).getActionFacet());
        assertEquals("ObjectList", type.getActions().get(2).getResponses().get(0).getActionFacet().getName());
        assertEquals("SampleResource_ObjectList", type.getActions().get(2).getResponses().get(0).getActionFacetName());
        assertEquals(2, type.getActions().get(2).getResponses().get(0).getMimeTypes().size());
        assertEquals(TLMimeType.APPLICATION_XML, type.getActions().get(2).getResponses().get(0).getMimeTypes().get(0));
        assertEquals(TLMimeType.APPLICATION_JSON, type.getActions().get(2).getResponses().get(0).getMimeTypes().get(1));
        assertNotNull(type.getActions().get(2).getResponses().get(0).getDocumentation());
        assertNotNull(type.getActions().get(2).getResponses().get(0).getDocumentation().getDescription());
        assertEquals("Action-Search-Response1-documentation-line_1",
        		type.getActions().get(2).getResponses().get(0).getDocumentation().getDescription());
    }
    
    @Test
    public void testTLResourceTransformer() throws Exception {
    	Resource type = transformResource(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleResource");
    	
        assertNotNull(type);
        assertEquals("SampleResource", type.getName());
        assertNotNull(type.getDocumentation());
        assertNotNull(type.getDocumentation().getDescription());
        assertEquals("SampleResource-documentation-line_1", type.getDocumentation().getDescription().getValue());
        
        assertEquals(1, type.getResourceParentRef().size());
        assertNotNull(type.getResourceParentRef().get(0).getDocumentation());
        assertNotNull(type.getResourceParentRef().get(0).getDocumentation().getDescription());
        assertEquals("SampleResource-ParentRef-documentation-line_1",
        		type.getResourceParentRef().get(0).getDocumentation().getDescription().getValue());
        assertEquals("ParentResource", type.getResourceParentRef().get(0).getParent());
        assertNotNull(type.getResourceParentRef().get(0).getParentParamGroup());
        
        assertNotNull(type.getExtension());
        assertEquals("BaseResource", type.getExtension().getExtends());
        
        assertEquals(2, type.getParamGroup().size());
        assertEquals("IDParameters", type.getParamGroup().get(0).getName());
        assertTrue(type.getParamGroup().get(0).isIdGroup());
        assertNotNull(type.getParamGroup().get(0).getDocumentation());
        assertNotNull(type.getParamGroup().get(0).getDocumentation().getDescription());
        assertEquals("IDParameters-documentation-line_1",
        		type.getParamGroup().get(0).getDocumentation().getDescription().getValue());
        assertEquals("SampleBusinessObject_ID", type.getParamGroup().get(0).getFacetName());
        
        assertEquals(1, type.getParamGroup().get(0).getParameter().size());
        assertEquals("sample_oid", type.getParamGroup().get(0).getParameter().get(0).getFieldName());
        assertEquals(1, type.getParamGroup().get(0).getParameter().get(0).getEquivalent().size());
        assertEquals("Param-sample_oid-equivalent", 
        		type.getParamGroup().get(0).getParameter().get(0).getEquivalent().get(0).getValue());
        assertEquals(1, type.getParamGroup().get(0).getParameter().get(0).getExample().size());
        assertEquals("sample_oid-ex", 
        		type.getParamGroup().get(0).getParameter().get(0).getExample().get(0).getValue());
        
        assertEquals(3, type.getActionFacet().size());
        assertEquals("ObjectOnly", type.getActionFacet().get(0).getLabel());
        assertFalse(type.getActionFacet().get(0).isNotExtendable());
        assertNotNull(type.getActionFacet().get(0).getDocumentation());
        assertNotNull(type.getActionFacet().get(0).getDocumentation().getDescription());
        assertEquals("ActionFacet-ObjectOnly-documentation-line_1",
        		type.getActionFacet().get(0).getDocumentation().getDescription().getValue());
        assertEquals(ReferenceType.REQUIRED, type.getActionFacet().get(0).getReferenceType());
        assertEquals("SUMMARY", type.getActionFacet().get(1).getReferenceFacet());
        assertEquals("1000", type.getActionFacet().get(1).getReferenceRepeat());
        assertEquals(1, type.getActionFacet().get(2).getAttribute().size());
        assertEquals(1, type.getActionFacet().get(2).getElement().size());
        assertEquals(1, type.getActionFacet().get(2).getIndicator().size());
        
        assertEquals(3, type.getAction().size());
        assertEquals("Update", type.getAction().get(1).getActionId());
        assertEquals("/sample/{sample_oid}", type.getAction().get(1).getPathTemplate());
        
        assertEquals(HttpMethod.PUT, type.getAction().get(1).getActionRequest().getHttpMethod());
        assertEquals("IDParameters", type.getAction().get(1).getActionRequest().getParamGroup());
        assertEquals("SampleResource_ObjectOnly", type.getAction().get(1).getActionRequest().getActionFacet());
        assertEquals(2, type.getAction().get(1).getActionRequest().getMimeTypes().size());
        assertEquals(MimeType.APPLICATION_XML, type.getAction().get(1).getActionRequest().getMimeTypes().get(0));
        assertEquals(MimeType.APPLICATION_JSON, type.getAction().get(1).getActionRequest().getMimeTypes().get(1));
        assertNotNull(type.getAction().get(1).getActionRequest().getDocumentation());
        assertNotNull(type.getAction().get(1).getActionRequest().getDocumentation().getDescription());
        assertEquals("Action-Update-Request-documentation-line_1",
        		type.getAction().get(1).getActionRequest().getDocumentation().getDescription().getValue());
        
        assertEquals(2, type.getAction().get(2).getActionResponse().size());
        assertEquals(2, type.getAction().get(2).getActionResponse().get(1).getStatusCodes().size());
        assertEquals(403, type.getAction().get(2).getActionResponse().get(1).getStatusCodes().get(0).intValue());
        assertEquals(500, type.getAction().get(2).getActionResponse().get(1).getStatusCodes().get(1).intValue());
        assertEquals("SampleResource_ObjectList", type.getAction().get(2).getActionResponse().get(0).getActionFacet());
        assertEquals(2, type.getAction().get(2).getActionResponse().get(0).getMimeTypes().size());
        assertEquals(MimeType.APPLICATION_XML, type.getAction().get(2).getActionResponse().get(0).getMimeTypes().get(0));
        assertEquals(MimeType.APPLICATION_JSON, type.getAction().get(2).getActionResponse().get(0).getMimeTypes().get(1));
        assertNotNull(type.getAction().get(2).getActionResponse().get(0).getDocumentation());
        assertNotNull(type.getAction().get(2).getActionResponse().get(0).getDocumentation().getDescription());
        assertEquals("Action-Search-Response1-documentation-line_1",
        		type.getAction().get(2).getActionResponse().get(0).getDocumentation().getDescription().getValue());
    }
    
    private TLResource getResource(String namespace, String libraryName, String typeName)
            throws Exception {
        TLLibrary library = getLibrary(namespace, libraryName);

        return (library == null) ? null : library.getResourceType(typeName);
    }

    private Resource transformResource(String namespace, String libraryName, String typeName)
            throws Exception {
    	TLResource origType = getResource(namespace, libraryName, typeName);
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(origType.getOwningLibrary()));
        ObjectTransformer<TLResource, Resource, SymbolResolverTransformerContext> transformer =
        		factory.getTransformer(origType, Resource.class);

        return transformer.transform(origType);
    }

}
