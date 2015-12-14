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
package org.opentravel.schemacompiler.transform.library_01_04;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.XMLConstants;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_05.CoreObject;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLCoreObject</code> objects.
 * 
 * @author S. Livezey
 */
public class TestCoreObjectTransformers extends Abstract_1_4_TestTransformers {

    @Test
    public void testCoreObjectTransformerNoBaseType() throws Exception {
        TLCoreObject type = getCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2",
                "NoSimpleBaseTypeCore");

        assertNotNull(type);
        assertEquals("NoSimpleBaseTypeCore", type.getName());
        assertFalse(type.getSimpleFacet().declaresContent());
    }

    @Test
    public void testCoreObjectTransformerBuiltInBaseType() throws Exception {
        TLCoreObject type = getCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore");

        assertNotNull(type);
        assertEquals("SampleCore", type.getName());
        assertFalse(type.isNotExtendable());
        assertNotNull(type.getEquivalents());
        assertEquals(1, type.getEquivalents().size());
        assertEquals("test", type.getEquivalents().get(0).getContext());
        assertEquals("SampleCore-equivalent", type.getEquivalents().get(0).getDescription());
        assertNotNull(type.getDocumentation());

        assertNotNull(type.getRoleEnumeration().getRoles());
        assertEquals(2, type.getRoleEnumeration().getRoles().size());
        assertEquals("Role1", type.getRoleEnumeration().getRoles().get(0).getName());
        assertEquals("Role2", type.getRoleEnumeration().getRoles().get(1).getName());

        assertTrue(type.getSimpleFacet().declaresContent());
        assertNotNull(type.getSimpleFacet().getExamples());
        assertEquals(1, type.getSimpleFacet().getExamples().size());
        assertEquals("test-simple", type.getSimpleFacet().getExamples().get(0).getContext());
        assertEquals("SimpleFacet-ex", type.getSimpleFacet().getExamples().get(0).getValue());
        assertNotNull(type.getSimpleFacet().getEquivalents());
        assertEquals(1, type.getSimpleFacet().getEquivalents().size());
        assertEquals("test-simple", type.getSimpleFacet().getEquivalents().get(0).getContext());
        assertEquals("SimpleFacet-equivalent", type.getSimpleFacet().getEquivalents().get(0)
                .getDescription());
        assertNotNull(type.getSimpleFacet().getDocumentation());
        assertEquals(XSDSimpleType.class, type.getSimpleFacet().getSimpleType().getClass());
        assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, type.getSimpleFacet().getSimpleType()
                .getNamespace());
        assertEquals("string", type.getSimpleFacet().getSimpleType().getLocalName());
        assertEquals("xsd:string", type.getSimpleFacet().getSimpleTypeName());
    }

    @Test
    public void testCoreObjectTransformerSimpleBaseType() throws Exception {
        TLCoreObject type = getCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SimpleCore");

        assertNotNull(type);
        assertEquals("SimpleCore", type.getName());
        assertNotNull(type.getSimpleFacet());
        assertEquals(TLSimple.class, type.getSimpleFacet().getSimpleType().getClass());
        assertEquals(PACKAGE_1_NAMESPACE, type.getSimpleFacet().getSimpleType().getNamespace());
        assertEquals("Counter_1", type.getSimpleFacet().getSimpleType().getLocalName());
        assertEquals("pkg1:Counter_1", type.getSimpleFacet().getSimpleTypeName());
    }

    @Test
    public void testCoreObjectTransformerEnumerationBaseType() throws Exception {
        TLCoreObject type = getCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "EnumCore");

        assertNotNull(type);
        assertEquals("EnumCore", type.getName());
        assertNotNull(type.getSimpleFacet());
        assertEquals(TLClosedEnumeration.class, type.getSimpleFacet().getSimpleType().getClass());
        assertEquals(PACKAGE_2_NAMESPACE, type.getSimpleFacet().getSimpleType().getNamespace());
        assertEquals("SampleEnum_Closed", type.getSimpleFacet().getSimpleType().getLocalName());
        assertEquals("SampleEnum_Closed", type.getSimpleFacet().getSimpleTypeName());
    }

    @Test
    public void testTLCoreObjectTransformerNoBaseType() throws Exception {
        CoreObject type = transformCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2",
                "NoSimpleBaseTypeCore");

        assertNotNull(type);
        assertEquals("NoSimpleBaseTypeCore", type.getName());
        assertNotNull(type.getSimple());
    }

    @Test
    public void testTLCoreObjectTransformerBuiltInBaseType() throws Exception {
        CoreObject type = transformCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore");

        assertNotNull(type);
        assertNotNull(type.getSimple());
        assertEquals(1, type.getSimple().getExample().size());
        assertEquals("test-simple", type.getSimple().getExample().get(0).getContext());
        assertEquals("SimpleFacet-ex", type.getSimple().getExample().get(0).getValue());
        assertEquals(1, type.getSimple().getEquivalent().size());
        assertEquals("test-simple", type.getSimple().getEquivalent().get(0).getContext());
        assertEquals("SimpleFacet-equivalent", type.getSimple().getEquivalent().get(0).getValue());
        assertNotNull(type.getSimple().getDocumentation());
        assertEquals("xsd:string", type.getSimple().getType());
    }

    @Test
    public void testTLCoreObjectTransformerSimpleBaseType() throws Exception {
        CoreObject type = transformCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SimpleCore");

        assertNotNull(type);
        assertNotNull(type.getSimple());
        assertEquals("pkg1:Counter_1", type.getSimple().getType());
    }

    @Test
    public void testTLCoreObjectTransformerEnumerationBaseType() throws Exception {
        CoreObject type = transformCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "EnumCore");

        assertNotNull(type);
        assertNotNull(type.getSimple());
        assertEquals("SampleEnum_Closed", type.getSimple().getType());
    }

    private TLCoreObject getCoreObject(String namespace, String libraryName, String typeName)
            throws Exception {
        TLLibrary library = getLibrary(namespace, libraryName);

        return (library == null) ? null : library.getCoreObjectType(typeName);
    }

    private CoreObject transformCoreObject(String namespace, String libraryName, String typeName)
            throws Exception {
        TLCoreObject origType = getCoreObject(namespace, libraryName, typeName);
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(origType.getOwningLibrary()));
        ObjectTransformer<TLCoreObject, CoreObject, SymbolResolverTransformerContext> transformer = factory
                .getTransformer(origType, CoreObject.class);

        return transformer.transform(origType);
    }

}
