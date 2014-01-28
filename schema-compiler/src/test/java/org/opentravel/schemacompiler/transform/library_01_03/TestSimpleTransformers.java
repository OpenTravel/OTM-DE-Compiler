package org.opentravel.schemacompiler.transform.library_01_03;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.XMLConstants;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_04.Simple;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.jaxb13_2tl.LibraryTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLSimple</code> objects.
 * 
 * @author S. Livezey
 */
public class TestSimpleTransformers extends Abstract_1_3_TestTransformers {

    @Test
    public void testSimpleTransformerExtendBuiltInType() throws Exception {
        TLSimple type = getSimpleType(PACKAGE_1_NAMESPACE, "library_1_p1", "TestString");

        assertNotNull(type);
        assertEquals("TestString", type.getName());
        assertEquals("[A-Za-z]*", type.getPattern());
        assertEquals(5, type.getMinLength());
        assertEquals(10, type.getMaxLength());
        assertNotNull(type.getExamples());
        assertEquals(1, type.getExamples().size());
        assertEquals(LibraryTransformer.DEFAULT_CONTEXT_ID, type.getExamples().get(0).getContext());
        assertEquals("TestString-ex", type.getExamples().get(0).getValue());
        assertNotNull(type.getEquivalents());
        assertEquals(1, type.getEquivalents().size());
        assertEquals("test", type.getEquivalents().get(0).getContext());
        assertEquals("TestString-equivalent", type.getEquivalents().get(0).getDescription());
        assertEquals("xsd:string", type.getParentTypeName());
        assertNotNull(type.getParentType());
        assertEquals(XSDSimpleType.class, type.getParentType().getClass());
        assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, type.getParentType().getNamespace());
        assertEquals("string", type.getParentType().getLocalName());
    }

    @Test
    public void testSimpleTransformerExtendFromSameLibrary() throws Exception {
        TLSimple type = getSimpleType(PACKAGE_1_NAMESPACE, "library_1_p1", "TestCounter");

        assertNotNull(type);
        assertEquals("TestCounter", type.getName());
        assertEquals("Counter_1", type.getParentTypeName());
        assertNotNull(type.getParentType());
        assertEquals(TLSimple.class, type.getParentType().getClass());
        assertEquals(PACKAGE_1_NAMESPACE, type.getParentType().getNamespace());
        assertEquals("Counter_1", type.getParentType().getLocalName());
    }

    @Test
    public void testSimpleTransformerExtendFromSameNamespaceDifferentLibrary() throws Exception {
        TLSimple type = getSimpleType(PACKAGE_1_NAMESPACE, "library_2_p1", "TestCounter_2");

        assertNotNull(type);
        assertEquals("TestCounter_2", type.getName());
        assertEquals("Counter_1", type.getParentTypeName());
        assertNotNull(type.getParentType());
        assertEquals(TLSimple.class, type.getParentType().getClass());
        assertEquals(PACKAGE_1_NAMESPACE, type.getParentType().getNamespace());
        assertEquals("Counter_1", type.getParentType().getLocalName());
    }

    @Test
    public void testSimpleTransformerExtendFromDifferentNamespace() throws Exception {
        TLSimple type = getSimpleType(PACKAGE_2_NAMESPACE, "library_1_p2", "Counter_3");

        assertNotNull(type);
        assertEquals("Counter_3", type.getName());
        assertEquals("pkg1:Counter_1", type.getParentTypeName());
        assertNotNull(type.getParentType());
        assertEquals(TLSimple.class, type.getParentType().getClass());
        assertEquals(PACKAGE_1_NAMESPACE, type.getParentType().getNamespace());
        assertEquals("Counter_1", type.getParentType().getLocalName());
    }

    @Test
    public void testTLSimpleTransformerExtendBuiltInType() throws Exception {
        Simple type = transformSimpleType(PACKAGE_1_NAMESPACE, "library_1_p1", "TestString");

        assertNotNull(type);
        assertEquals("TestString", type.getName());
        assertEquals("[A-Za-z]*", type.getPattern());
        assertNotNull(type.getMinLength());
        assertEquals(5, type.getMinLength().intValue());
        assertNotNull(type.getMaxLength());
        assertEquals(10, type.getMaxLength().intValue());
        assertEquals(1, type.getExample().size());
        assertEquals(LibraryTransformer.DEFAULT_CONTEXT_ID, type.getExample().get(0).getContext());
        assertEquals("TestString-ex", type.getExample().get(0).getValue());
        assertEquals(1, type.getEquivalent().size());
        assertEquals("test", type.getEquivalent().get(0).getContext());
        assertEquals("TestString-equivalent", type.getEquivalent().get(0).getValue());
        assertEquals("xsd:string", type.getType());
    }

    @Test
    public void testTLSimpleTransformerExtendFromSameLibrary() throws Exception {
        Simple type = transformSimpleType(PACKAGE_1_NAMESPACE, "library_1_p1", "TestCounter");

        assertNotNull(type);
        assertEquals("TestCounter", type.getName());
        assertEquals("Counter_1", type.getType());
    }

    @Test
    public void testTLSimpleTransformerExtendFromSameNamespaceDifferentLibrary() throws Exception {
        Simple type = transformSimpleType(PACKAGE_1_NAMESPACE, "library_2_p1", "TestCounter_2");

        assertNotNull(type);
        assertEquals("TestCounter_2", type.getName());
        assertEquals("Counter_1", type.getType());
    }

    @Test
    public void testTLSimpleTransformerExtendFromDifferentNamespace() throws Exception {
        Simple type = transformSimpleType(PACKAGE_2_NAMESPACE, "library_1_p2", "Counter_3");

        assertNotNull(type);
        assertEquals("Counter_3", type.getName());
        assertEquals("pkg1:Counter_1", type.getType());
    }

    private TLSimple getSimpleType(String namespace, String libraryName, String typeName)
            throws Exception {
        TLLibrary library = getLibrary(namespace, libraryName);

        return (library == null) ? null : library.getSimpleType(typeName);
    }

    private Simple transformSimpleType(String namespace, String libraryName, String typeName)
            throws Exception {
        TLSimple origType = getSimpleType(namespace, libraryName, typeName);
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(origType.getOwningLibrary()));
        ObjectTransformer<TLSimple, Simple, SymbolResolverTransformerContext> transformer = factory
                .getTransformer(origType, Simple.class);

        return transformer.transform(origType);
    }

}
