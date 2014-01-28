package org.opentravel.schemacompiler.transform.library_01_04;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.xml.XMLConstants;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_04.ValueWithAttributes;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLValueWithAttributes</code> objects.
 * 
 * @author S. Livezey
 */
public class TestValueWithAttributesTransformers extends Abstract_1_4_TestTransformers {

    @Test
    public void testValueWithAttributesTransformer() throws Exception {
        TLValueWithAttributes type = getValueWithAttributesType(PACKAGE_2_NAMESPACE,
                "library_1_p2", "SampleValueWithAttributes");

        assertNotNull(type);
        assertEquals("SampleValueWithAttributes", type.getName());
        assertNotNull(type.getExamples());
        assertEquals(1, type.getExamples().size());
        assertEquals("test", type.getExamples().get(0).getContext());
        assertEquals("SampleValueWithAttributes-ex", type.getExamples().get(0).getValue());
        assertNotNull(type.getEquivalents());
        assertEquals(1, type.getEquivalents().size());
        assertEquals("test", type.getEquivalents().get(0).getContext());
        assertEquals("SampleValueWithAttributes-equivalent", type.getEquivalents().get(0)
                .getDescription());
        assertNotNull(type.getDocumentation());

        assertEquals("attr1", type.getAttributes().get(0).getName());
        assertNotNull(type.getAttributes().get(0).getDocumentation());
        assertNotNull(type.getAttributes().get(0).getExamples());
        assertEquals(0, type.getAttributes().get(0).getExamples().size());
        assertNotNull(type.getAttributes().get(0).getEquivalents());
        assertEquals(0, type.getAttributes().get(0).getEquivalents().size());
        assertFalse(type.getAttributes().get(0).isMandatory());
        assertEquals("Counter_3", type.getAttributes().get(0).getTypeName());
        assertNotNull(type.getAttributes().get(0).getType());
        assertEquals(TLSimple.class, type.getAttributes().get(0).getType().getClass());
        assertEquals(PACKAGE_2_NAMESPACE, type.getAttributes().get(0).getType().getNamespace());
        assertEquals("Counter_3", type.getAttributes().get(0).getType().getLocalName());

        assertEquals("attr2", type.getAttributes().get(1).getName());
        assertNull(type.getAttributes().get(1).getDocumentation());
        assertNotNull(type.getAttributes().get(1).getExamples());
        assertEquals(0, type.getAttributes().get(1).getExamples().size());
        assertNotNull(type.getAttributes().get(1).getEquivalents());
        assertEquals(0, type.getAttributes().get(1).getEquivalents().size());
        assertTrue(type.getAttributes().get(1).isMandatory());
        assertEquals("Counter_4", type.getAttributes().get(1).getTypeName());
        assertNotNull(type.getAttributes().get(1).getType());
        assertEquals(TLSimple.class, type.getAttributes().get(1).getType().getClass());
        assertEquals(PACKAGE_2_NAMESPACE, type.getAttributes().get(1).getType().getNamespace());
        assertEquals("Counter_4", type.getAttributes().get(1).getType().getLocalName());

        assertEquals("attr3", type.getAttributes().get(2).getName());
        assertNull(type.getAttributes().get(2).getDocumentation());
        assertNotNull(type.getAttributes().get(2).getExamples());
        assertEquals(1, type.getAttributes().get(2).getExamples().size());
        assertEquals("test", type.getAttributes().get(2).getExamples().get(0).getContext());
        assertEquals("attr3-ex", type.getAttributes().get(2).getExamples().get(0).getValue());
        assertNotNull(type.getAttributes().get(2).getEquivalents());
        assertEquals(0, type.getAttributes().get(2).getEquivalents().size());
        assertFalse(type.getAttributes().get(2).isMandatory());
        assertEquals("pkg1:Counter_1", type.getAttributes().get(2).getTypeName());
        assertNotNull(type.getAttributes().get(2).getType());
        assertEquals(TLSimple.class, type.getAttributes().get(2).getType().getClass());
        assertEquals(PACKAGE_1_NAMESPACE, type.getAttributes().get(2).getType().getNamespace());
        assertEquals("Counter_1", type.getAttributes().get(2).getType().getLocalName());

        assertEquals("attr4", type.getAttributes().get(3).getName());
        assertNull(type.getAttributes().get(3).getDocumentation());
        assertNotNull(type.getAttributes().get(3).getExamples());
        assertEquals(0, type.getAttributes().get(3).getExamples().size());
        assertNotNull(type.getAttributes().get(3).getEquivalents());
        assertEquals(1, type.getAttributes().get(3).getEquivalents().size());
        assertEquals("test", type.getAttributes().get(3).getEquivalents().get(0).getContext());
        assertEquals("attr4-equivalent", type.getAttributes().get(3).getEquivalents().get(0)
                .getDescription());
        assertFalse(type.getAttributes().get(3).isMandatory());
        assertEquals("xsd:int", type.getAttributes().get(3).getTypeName());
        assertNotNull(type.getAttributes().get(3).getType());
        assertEquals(XSDSimpleType.class, type.getAttributes().get(3).getType().getClass());
        assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, type.getAttributes().get(3).getType()
                .getNamespace());
        assertEquals("int", type.getAttributes().get(3).getType().getLocalName());

        assertEquals("indicator1", type.getIndicators().get(0).getName());
        assertNull(type.getIndicators().get(0).getDocumentation());
        assertNotNull(type.getIndicators().get(0).getEquivalents());
        assertEquals(0, type.getIndicators().get(0).getEquivalents().size());
    }

    @Test
    public void testTLValueWithAttributesTransformer() throws Exception {
        ValueWithAttributes type = transformValueWithAttributesType(PACKAGE_2_NAMESPACE,
                "library_1_p2", "SampleValueWithAttributes");

        assertNotNull(type);
        assertEquals("SampleValueWithAttributes", type.getName());
        assertEquals(1, type.getExample().size());
        assertEquals("test", type.getExample().get(0).getContext());
        assertEquals("SampleValueWithAttributes-ex", type.getExample().get(0).getValue());
        assertEquals(1, type.getEquivalent().size());
        assertEquals("test", type.getEquivalent().get(0).getContext());
        assertEquals("SampleValueWithAttributes-equivalent", type.getEquivalent().get(0).getValue());
        assertNotNull(type.getDocumentation());

        assertEquals("attr1", type.getAttribute().get(0).getName());
        assertNotNull(type.getAttribute().get(0).getDocumentation());
        assertEquals(0, type.getAttribute().get(0).getExample().size());
        assertEquals(0, type.getAttribute().get(0).getEquivalent().size());
        assertNull(type.getAttribute().get(0).isMandatory());
        assertEquals("Counter_3", type.getAttribute().get(0).getType());

        assertEquals("attr2", type.getAttribute().get(1).getName());
        assertNull(type.getAttribute().get(1).getDocumentation());
        assertEquals(0, type.getAttribute().get(1).getExample().size());
        assertEquals(0, type.getAttribute().get(1).getEquivalent().size());
        assertTrue(type.getAttribute().get(1).isMandatory());
        assertEquals("Counter_4", type.getAttribute().get(1).getType());

        assertEquals("attr3", type.getAttribute().get(2).getName());
        assertNull(type.getAttribute().get(2).getDocumentation());
        assertEquals(1, type.getAttribute().get(2).getExample().size());
        assertEquals("test", type.getAttribute().get(2).getExample().get(0).getContext());
        assertEquals("attr3-ex", type.getAttribute().get(2).getExample().get(0).getValue());
        assertEquals(0, type.getAttribute().get(2).getEquivalent().size());
        assertNull(type.getAttribute().get(2).isMandatory());
        assertEquals("pkg1:Counter_1", type.getAttribute().get(2).getType());

        assertEquals("attr4", type.getAttribute().get(3).getName());
        assertNull(type.getAttribute().get(3).getDocumentation());
        assertEquals(0, type.getAttribute().get(3).getExample().size());
        assertEquals(1, type.getAttribute().get(3).getEquivalent().size());
        assertEquals("test", type.getAttribute().get(3).getEquivalent().get(0).getContext());
        assertEquals("attr4-equivalent", type.getAttribute().get(3).getEquivalent().get(0)
                .getValue());
        assertNull(type.getAttribute().get(3).isMandatory());
        assertEquals("xsd:int", type.getAttribute().get(3).getType());

        assertEquals("indicator1", type.getIndicator().get(0).getName());
        assertNull(type.getIndicator().get(0).getDocumentation());
        assertEquals(0, type.getIndicator().get(0).getEquivalent().size());
    }

    private TLValueWithAttributes getValueWithAttributesType(String namespace, String libraryName,
            String typeName) throws Exception {
        TLLibrary library = getLibrary(namespace, libraryName);

        return (library == null) ? null : library.getValueWithAttributesType(typeName);
    }

    private ValueWithAttributes transformValueWithAttributesType(String namespace,
            String libraryName, String typeName) throws Exception {
        TLValueWithAttributes origType = getValueWithAttributesType(namespace, libraryName,
                typeName);
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(origType.getOwningLibrary()));
        ObjectTransformer<TLValueWithAttributes, ValueWithAttributes, SymbolResolverTransformerContext> transformer = factory
                .getTransformer(origType, ValueWithAttributes.class);

        return transformer.transform(origType);
    }

}
