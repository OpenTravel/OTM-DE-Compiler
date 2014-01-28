package org.opentravel.schemacompiler.transform.library_01_03;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.XMLConstants;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_04.BusinessObject;
import org.opentravel.ns.ota2.librarymodel_v01_04.CoreObject;
import org.opentravel.ns.ota2.librarymodel_v01_04.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_04.FacetContextual;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.jaxb13_2tl.LibraryTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLFacet</code> and <code>TLFacetContextual</code> objects.
 * 
 * @author S. Livezey
 */
public class TestFacetTransformers extends Abstract_1_3_TestTransformers {

    @Test
    public void testFacetTransformer() throws Exception {
        TLFacet facet = getCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore")
                .getSummaryFacet();

        assertNotNull(facet);
        assertNotNull(facet.getDocumentation());
        assertNotNull(facet.getAliases());
        assertEquals(3, facet.getAliases().size());
        assertEquals("SampleCore_Alias1_Summary", facet.getAliases().get(0).getName());
        assertEquals("SampleCore_Alias2_Summary", facet.getAliases().get(1).getName());
        assertEquals("SampleCore_Info_Alias_Summary", facet.getAliases().get(2).getName());
        assertFalse(facet.isNotExtendable());
    }

    @Test
    public void testFacetTransformerIndicators() throws Exception {
        TLFacet facet = getCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore")
                .getSummaryFacet();

        assertNotNull(facet.getIndicators());
        assertEquals(2, facet.getIndicators().size());

        assertEquals("indicator1", facet.getIndicators().get(0).getName());
        assertNotNull(facet.getIndicators().get(0).getDocumentation());
        assertNotNull(facet.getIndicators().get(0).getEquivalents());
        assertEquals(0, facet.getIndicators().get(0).getEquivalents().size());

        assertEquals("indicator2", facet.getIndicators().get(1).getName());
        assertNull(facet.getIndicators().get(1).getDocumentation());
        assertNotNull(facet.getIndicators().get(1).getEquivalents());
        assertEquals(1, facet.getIndicators().get(1).getEquivalents().size());
        assertEquals("test", facet.getIndicators().get(1).getEquivalents().get(0).getContext());
        assertEquals("indicator2-equivalent", facet.getIndicators().get(1).getEquivalents().get(0)
                .getDescription());
    }

    @Test
    public void testFacetTransformerAttributes() throws Exception {
        TLFacet facet = getCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore")
                .getSummaryFacet();

        assertNotNull(facet.getAttributes());
        assertEquals(4, facet.getAttributes().size());

        assertEquals("attr1", facet.getAttributes().get(0).getName());
        assertNotNull(facet.getAttributes().get(0).getDocumentation());
        assertNotNull(facet.getAttributes().get(0).getExamples());
        assertEquals(0, facet.getAttributes().get(0).getExamples().size());
        assertNotNull(facet.getAttributes().get(0).getEquivalents());
        assertEquals(0, facet.getAttributes().get(0).getEquivalents().size());
        assertFalse(facet.getAttributes().get(0).isMandatory());
        assertEquals("Counter_3", facet.getAttributes().get(0).getTypeName());
        assertNotNull(facet.getAttributes().get(0).getType());
        assertEquals(TLSimple.class, facet.getAttributes().get(0).getType().getClass());
        assertEquals(PACKAGE_2_NAMESPACE, facet.getAttributes().get(0).getType().getNamespace());
        assertEquals("Counter_3", facet.getAttributes().get(0).getType().getLocalName());

        assertEquals("attr2", facet.getAttributes().get(1).getName());
        assertNull(facet.getAttributes().get(1).getDocumentation());
        assertNotNull(facet.getAttributes().get(1).getExamples());
        assertEquals(0, facet.getAttributes().get(1).getExamples().size());
        assertNotNull(facet.getAttributes().get(1).getEquivalents());
        assertEquals(0, facet.getAttributes().get(1).getEquivalents().size());
        assertTrue(facet.getAttributes().get(1).isMandatory());
        assertEquals("Counter_4", facet.getAttributes().get(1).getTypeName());
        assertNotNull(facet.getAttributes().get(1).getType());
        assertEquals(TLSimple.class, facet.getAttributes().get(1).getType().getClass());
        assertEquals(PACKAGE_2_NAMESPACE, facet.getAttributes().get(1).getType().getNamespace());
        assertEquals("Counter_4", facet.getAttributes().get(1).getType().getLocalName());

        assertEquals("attr3", facet.getAttributes().get(2).getName());
        assertNull(facet.getAttributes().get(2).getDocumentation());
        assertNotNull(facet.getAttributes().get(2).getExamples());
        assertEquals(1, facet.getAttributes().get(2).getExamples().size());
        assertEquals(LibraryTransformer.DEFAULT_CONTEXT_ID, facet.getAttributes().get(2)
                .getExamples().get(0).getContext());
        assertEquals("attr3-ex", facet.getAttributes().get(2).getExamples().get(0).getValue());
        assertNotNull(facet.getAttributes().get(2).getEquivalents());
        assertEquals(0, facet.getAttributes().get(2).getEquivalents().size());
        assertFalse(facet.getAttributes().get(2).isMandatory());
        assertEquals("pkg1:Counter_1", facet.getAttributes().get(2).getTypeName());
        assertNotNull(facet.getAttributes().get(2).getType());
        assertEquals(TLSimple.class, facet.getAttributes().get(2).getType().getClass());
        assertEquals(PACKAGE_1_NAMESPACE, facet.getAttributes().get(2).getType().getNamespace());
        assertEquals("Counter_1", facet.getAttributes().get(2).getType().getLocalName());

        assertEquals("attr4", facet.getAttributes().get(3).getName());
        assertNull(facet.getAttributes().get(3).getDocumentation());
        assertNotNull(facet.getAttributes().get(3).getExamples());
        assertEquals(0, facet.getAttributes().get(3).getExamples().size());
        assertNotNull(facet.getAttributes().get(3).getEquivalents());
        assertEquals(1, facet.getAttributes().get(3).getEquivalents().size());
        assertEquals("test", facet.getAttributes().get(3).getEquivalents().get(0).getContext());
        assertEquals("attr4-equivalent", facet.getAttributes().get(3).getEquivalents().get(0)
                .getDescription());
        assertFalse(facet.getAttributes().get(3).isMandatory());
        assertEquals("xsd:int", facet.getAttributes().get(3).getTypeName());
        assertNotNull(facet.getAttributes().get(3).getType());
        assertEquals(XSDSimpleType.class, facet.getAttributes().get(3).getType().getClass());
        assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, facet.getAttributes().get(3).getType()
                .getNamespace());
        assertEquals("int", facet.getAttributes().get(3).getType().getLocalName());
    }

    @Test
    public void testFacetTransformerProperties() throws Exception {
        TLFacet facet = getCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore")
                .getSummaryFacet();

        assertNotNull(facet.getElements());
        assertEquals(6, facet.getElements().size());

        assertEquals("element1", facet.getElements().get(0).getName());
        assertNotNull(facet.getElements().get(0).getDocumentation());
        assertNotNull(facet.getAttributes().get(0).getExamples());
        assertEquals(0, facet.getAttributes().get(0).getExamples().size());
        assertNotNull(facet.getElements().get(0).getEquivalents());
        assertEquals(0, facet.getElements().get(0).getEquivalents().size());
        assertFalse(facet.getElements().get(0).isMandatory());
        assertEquals(0, facet.getElements().get(0).getRepeat());
        assertEquals("Counter_3", facet.getElements().get(0).getTypeName());
        assertNotNull(facet.getElements().get(0).getType());
        assertEquals(TLSimple.class, facet.getElements().get(0).getType().getClass());
        assertEquals(PACKAGE_2_NAMESPACE, facet.getElements().get(0).getType().getNamespace());
        assertEquals("Counter_3", facet.getElements().get(0).getType().getLocalName());

        assertEquals("element2", facet.getElements().get(1).getName());
        assertNull(facet.getElements().get(1).getDocumentation());
        assertNotNull(facet.getAttributes().get(1).getExamples());
        assertEquals(0, facet.getAttributes().get(1).getExamples().size());
        assertNotNull(facet.getElements().get(1).getEquivalents());
        assertEquals(0, facet.getElements().get(1).getEquivalents().size());
        assertFalse(facet.getElements().get(1).isMandatory());
        assertEquals(0, facet.getElements().get(1).getRepeat());
        assertEquals("Counter_4", facet.getElements().get(1).getTypeName());
        assertNotNull(facet.getElements().get(1).getType());
        assertEquals(TLSimple.class, facet.getElements().get(1).getType().getClass());
        assertEquals(PACKAGE_2_NAMESPACE, facet.getElements().get(1).getType().getNamespace());
        assertEquals("Counter_4", facet.getElements().get(1).getType().getLocalName());

        assertEquals("element3", facet.getElements().get(2).getName());
        assertNull(facet.getElements().get(2).getDocumentation());
        assertNotNull(facet.getElements().get(2).getExamples());
        assertEquals(1, facet.getElements().get(2).getExamples().size());
        assertEquals(LibraryTransformer.DEFAULT_CONTEXT_ID, facet.getElements().get(2)
                .getExamples().get(0).getContext());
        assertEquals("element3-ex", facet.getElements().get(2).getExamples().get(0).getValue());
        assertNotNull(facet.getElements().get(2).getEquivalents());
        assertEquals(0, facet.getElements().get(2).getEquivalents().size());
        assertTrue(facet.getElements().get(2).isMandatory());
        assertEquals(0, facet.getElements().get(2).getRepeat());
        assertEquals("pkg1:Counter_1", facet.getElements().get(2).getTypeName());
        assertNotNull(facet.getElements().get(2).getType());
        assertEquals(TLSimple.class, facet.getElements().get(2).getType().getClass());
        assertEquals(PACKAGE_1_NAMESPACE, facet.getElements().get(2).getType().getNamespace());
        assertEquals("Counter_1", facet.getElements().get(2).getType().getLocalName());

        assertEquals("element4", facet.getElements().get(3).getName());
        assertNull(facet.getElements().get(3).getDocumentation());
        assertNotNull(facet.getAttributes().get(3).getExamples());
        assertEquals(0, facet.getAttributes().get(3).getExamples().size());
        assertNotNull(facet.getElements().get(3).getEquivalents());
        assertEquals(1, facet.getElements().get(3).getEquivalents().size());
        assertEquals("test", facet.getElements().get(3).getEquivalents().get(0).getContext());
        assertEquals("element4-equivalent", facet.getElements().get(3).getEquivalents().get(0)
                .getDescription());
        assertFalse(facet.getElements().get(3).isMandatory());
        assertEquals(0, facet.getElements().get(3).getRepeat());
        assertEquals("xsd:string", facet.getElements().get(3).getTypeName());
        assertNotNull(facet.getElements().get(3).getType());
        assertEquals(XSDSimpleType.class, facet.getElements().get(3).getType().getClass());
        assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, facet.getElements().get(3).getType()
                .getNamespace());
        assertEquals("string", facet.getElements().get(3).getType().getLocalName());

        assertEquals("element5", facet.getElements().get(4).getName());
        assertNull(facet.getElements().get(4).getDocumentation());
        assertFalse(facet.getElements().get(4).isMandatory());
        assertEquals(5, facet.getElements().get(4).getRepeat());
        assertEquals("EmptyBusinessObject", facet.getElements().get(4).getTypeName());
        assertNotNull(facet.getElements().get(4).getType());
        assertEquals(TLBusinessObject.class, facet.getElements().get(4).getType().getClass());
        assertEquals(PACKAGE_2_NAMESPACE, facet.getElements().get(4).getType().getNamespace());
        assertEquals("EmptyBusinessObject", facet.getElements().get(4).getType().getLocalName());

        assertEquals("element6", facet.getElements().get(5).getName());
        assertNull(facet.getElements().get(5).getDocumentation());
        assertFalse(facet.getElements().get(5).isMandatory());
        assertEquals(-1, facet.getElements().get(5).getRepeat());
        assertEquals("SampleBusinessObject_Detail", facet.getElements().get(5).getTypeName());
        assertNotNull(facet.getElements().get(5).getType());
        assertEquals(TLFacet.class, facet.getElements().get(5).getType().getClass());
        assertEquals(PACKAGE_2_NAMESPACE, facet.getElements().get(5).getType().getNamespace());
        assertEquals("SampleBusinessObject_Detail", facet.getElements().get(5).getType()
                .getLocalName());
    }

    @Test
    public void testFacetContextualTransformer() throws Exception {
        List<TLFacet> facetList = getBusinessObject(PACKAGE_2_NAMESPACE, "library_1_p2",
                "SampleBusinessObject").getCustomFacets();

        assertNotNull(facetList);
        assertEquals(2, facetList.size());

        TLFacet facet = facetList.get(0);

        assertNotNull(facet);
        assertEquals("SampleCustomFacet", facet.getContext());
        assertEquals(4, facet.getAliases().size());
        assertEquals("SampleBusinessObject_Alias1_SampleCustomFacet", facet.getAliases().get(0)
                .getName());
        assertEquals("SampleBusinessObject_Alias2_SampleCustomFacet", facet.getAliases().get(1)
                .getName());
        assertEquals("SampleCustomFacet_Alias_SampleCustomFacet", facet.getAliases().get(2)
                .getName());
        assertEquals("SampleCustomFacet2_Alias_SampleCustomFacet", facet.getAliases().get(3)
                .getName());
        assertEquals(1, facet.getAttributes().size());
        assertEquals(1, facet.getElements().size());
        assertEquals(1, facet.getIndicators().size());
    }

    @Test
    public void testTLFacetTransformer() throws Exception {
        Facet facet = transformCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore")
                .getSummary();

        assertNotNull(facet);
        assertNotNull(facet.getDocumentation());
    }

    @Test
    public void testTLFacetTransformerIndicators() throws Exception {
        Facet facet = transformCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore")
                .getSummary();

        assertNotNull(facet.getIndicator());
        assertEquals(2, facet.getIndicator().size());

        assertEquals("indicator1", facet.getIndicator().get(0).getName());
        assertNotNull(facet.getIndicator().get(0).getDocumentation());
        assertEquals(0, facet.getIndicator().get(0).getEquivalent().size());

        assertEquals("indicator2", facet.getIndicator().get(1).getName());
        assertNull(facet.getIndicator().get(1).getDocumentation());
        assertEquals(1, facet.getIndicator().get(1).getEquivalent().size());
        assertEquals("test", facet.getIndicator().get(1).getEquivalent().get(0).getContext());
        assertEquals("indicator2-equivalent", facet.getIndicator().get(1).getEquivalent().get(0)
                .getValue());
    }

    @Test
    public void testTLFacetTransformerAttributes() throws Exception {
        Facet facet = transformCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore")
                .getSummary();

        assertNotNull(facet.getAttribute());
        assertEquals(4, facet.getAttribute().size());

        assertEquals("attr1", facet.getAttribute().get(0).getName());
        assertNotNull(facet.getAttribute().get(0).getDocumentation());
        assertEquals(0, facet.getAttribute().get(0).getExample().size());
        assertEquals(0, facet.getAttribute().get(0).getEquivalent().size());
        assertNull(facet.getAttribute().get(0).isMandatory());
        assertEquals("Counter_3", facet.getAttribute().get(0).getType());

        assertEquals("attr2", facet.getAttribute().get(1).getName());
        assertNull(facet.getAttribute().get(1).getDocumentation());
        assertEquals(0, facet.getAttribute().get(1).getExample().size());
        assertEquals(0, facet.getAttribute().get(1).getEquivalent().size());
        assertTrue(facet.getAttribute().get(1).isMandatory());
        assertEquals("Counter_4", facet.getAttribute().get(1).getType());

        assertEquals("attr3", facet.getAttribute().get(2).getName());
        assertNull(facet.getAttribute().get(2).getDocumentation());
        assertEquals(1, facet.getAttribute().get(2).getExample().size());
        assertEquals(LibraryTransformer.DEFAULT_CONTEXT_ID, facet.getAttribute().get(2)
                .getExample().get(0).getContext());
        assertEquals("attr3-ex", facet.getAttribute().get(2).getExample().get(0).getValue());
        assertEquals(0, facet.getAttribute().get(2).getEquivalent().size());
        assertNull(facet.getAttribute().get(2).isMandatory());
        assertEquals("pkg1:Counter_1", facet.getAttribute().get(2).getType());

        assertEquals("attr4", facet.getAttribute().get(3).getName());
        assertNull(facet.getAttribute().get(3).getDocumentation());
        assertEquals(0, facet.getAttribute().get(3).getExample().size());
        assertEquals(1, facet.getAttribute().get(3).getEquivalent().size());
        assertEquals("test", facet.getAttribute().get(3).getEquivalent().get(0).getContext());
        assertEquals("attr4-equivalent", facet.getAttribute().get(3).getEquivalent().get(0)
                .getValue());
        assertNull(facet.getAttribute().get(3).isMandatory());
        assertEquals("xsd:int", facet.getAttribute().get(3).getType());
    }

    @Test
    public void testTLFacetTransformerProperties() throws Exception {
        Facet facet = transformCoreObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleCore")
                .getSummary();

        assertNotNull(facet.getElement());
        assertEquals(6, facet.getElement().size());

        assertEquals("element1", facet.getElement().get(0).getName());
        assertNotNull(facet.getElement().get(0).getDocumentation());
        assertEquals(0, facet.getElement().get(0).getExample().size());
        assertEquals(0, facet.getElement().get(0).getEquivalent().size());
        assertNull(facet.getElement().get(0).isMandatory());
        assertEquals("0", facet.getElement().get(0).getRepeat());
        assertEquals("Counter_3", facet.getElement().get(0).getType());

        assertEquals("element2", facet.getElement().get(1).getName());
        assertNull(facet.getElement().get(1).getDocumentation());
        assertEquals(0, facet.getElement().get(1).getExample().size());
        assertEquals(0, facet.getElement().get(1).getEquivalent().size());
        assertNull(facet.getElement().get(1).isMandatory());
        assertEquals("0", facet.getElement().get(1).getRepeat());
        assertEquals("Counter_4", facet.getElement().get(1).getType());

        assertEquals("element3", facet.getElement().get(2).getName());
        assertNull(facet.getElement().get(2).getDocumentation());
        assertEquals(1, facet.getElement().get(2).getExample().size());
        assertEquals(LibraryTransformer.DEFAULT_CONTEXT_ID, facet.getElement().get(2).getExample()
                .get(0).getContext());
        assertEquals("element3-ex", facet.getElement().get(2).getExample().get(0).getValue());
        assertEquals(0, facet.getElement().get(2).getEquivalent().size());
        assertNotNull(facet.getElement().get(2).isMandatory());
        assertEquals("0", facet.getElement().get(2).getRepeat());
        assertEquals("pkg1:Counter_1", facet.getElement().get(2).getType());

        assertEquals("element4", facet.getElement().get(3).getName());
        assertNull(facet.getElement().get(3).getDocumentation());
        assertEquals(0, facet.getElement().get(3).getExample().size());
        assertEquals(1, facet.getElement().get(3).getEquivalent().size());
        assertEquals("test", facet.getElement().get(3).getEquivalent().get(0).getContext());
        assertEquals("element4-equivalent", facet.getElement().get(3).getEquivalent().get(0)
                .getValue());
        assertNull(facet.getElement().get(3).isMandatory());
        assertEquals("0", facet.getElement().get(3).getRepeat());
        assertEquals("xsd:string", facet.getElement().get(3).getType());

        assertEquals("element5", facet.getElement().get(4).getName());
        assertNull(facet.getElement().get(4).getDocumentation());
        assertNull(facet.getElement().get(4).isMandatory());
        assertEquals("5", facet.getElement().get(4).getRepeat());
        assertEquals("EmptyBusinessObject", facet.getElement().get(4).getType());

        assertEquals("element6", facet.getElement().get(5).getName());
        assertNull(facet.getElement().get(5).getDocumentation());
        assertNull(facet.getElement().get(5).isMandatory());
        assertEquals("*", facet.getElement().get(5).getRepeat());
        assertEquals("SampleBusinessObject_Detail", facet.getElement().get(5).getType());
    }

    @Test
    public void testTLFacetContextualTransformer() throws Exception {
        List<FacetContextual> facetList = transformBusinessObject(PACKAGE_2_NAMESPACE,
                "library_1_p2", "SampleBusinessObject").getCustom();

        assertNotNull(facetList);
        assertEquals(2, facetList.size());

        FacetContextual facet = facetList.get(0);

        assertNotNull(facet);
        assertEquals("SampleCustomFacet", facet.getContext());
        assertEquals(1, facet.getAttribute().size());
        assertEquals(1, facet.getElement().size());
        assertEquals(1, facet.getIndicator().size());
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

    private TLBusinessObject getBusinessObject(String namespace, String libraryName, String typeName)
            throws Exception {
        TLLibrary library = getLibrary(namespace, libraryName);

        return (library == null) ? null : library.getBusinessObjectType(typeName);
    }

    private BusinessObject transformBusinessObject(String namespace, String libraryName,
            String typeName) throws Exception {
        TLBusinessObject origType = getBusinessObject(namespace, libraryName, typeName);
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(origType.getOwningLibrary()));
        ObjectTransformer<TLBusinessObject, BusinessObject, SymbolResolverTransformerContext> transformer = factory
                .getTransformer(origType, BusinessObject.class);

        return transformer.transform(origType);
    }

}
