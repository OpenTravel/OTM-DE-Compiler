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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumerationClosed;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumerationOpen;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from enumeration
 * library objects.
 * 
 * @author S. Livezey
 */
public class TestEnumerationTransformers extends Abstract_1_4_TestTransformers {

    @Test
    public void testEnumerationClosedTransformer() throws Exception {
        TLClosedEnumeration enumType = getClosedEnumeration(PACKAGE_2_NAMESPACE, "library_2_p2",
                "SampleEnum_Closed");

        assertNotNull(enumType);
        assertEquals("SampleEnum_Closed", enumType.getName());
        assertNotNull(enumType.getDocumentation());
        assertNotNull(enumType.getValues());
        assertEquals(3, enumType.getValues().size());

        assertEquals("one", enumType.getValues().get(0).getLiteral());
        assertEquals("two", enumType.getValues().get(1).getLiteral());
        assertEquals("three", enumType.getValues().get(2).getLiteral());

        assertNotNull(enumType.getValues().get(0).getDocumentation());
        assertNotNull(enumType.getValues().get(1).getDocumentation());
        assertNotNull(enumType.getValues().get(2).getDocumentation());

        assertNotNull(enumType.getValues().get(0).getEquivalents());
        assertEquals(0, enumType.getValues().get(0).getEquivalents().size());
        assertNotNull(enumType.getValues().get(1).getEquivalents());
        assertEquals(0, enumType.getValues().get(1).getEquivalents().size());
        assertNotNull(enumType.getValues().get(2).getEquivalents());
        assertEquals(1, enumType.getValues().get(2).getEquivalents().size());
        assertEquals("test", enumType.getValues().get(2).getEquivalents().get(0).getContext());
        assertEquals("three-equivalent", enumType.getValues().get(2).getEquivalents().get(0)
                .getDescription());
    }

    @Test
    public void testTLClosedEnumerationTransformer() throws Exception {
        EnumerationClosed enumType = transformClosedEnumeration(PACKAGE_2_NAMESPACE,
                "library_2_p2", "SampleEnum_Closed");

        assertNotNull(enumType);
        assertEquals("SampleEnum_Closed", enumType.getName());
        assertNotNull(enumType.getDocumentation());
        assertNotNull(enumType.getValue());
        assertEquals(3, enumType.getValue().size());

        assertEquals("one", enumType.getValue().get(0).getLiteral());
        assertEquals("two", enumType.getValue().get(1).getLiteral());
        assertEquals("three", enumType.getValue().get(2).getLiteral());

        assertNotNull(enumType.getValue().get(0).getDocumentation());
        assertNotNull(enumType.getValue().get(1).getDocumentation());
        assertNotNull(enumType.getValue().get(2).getDocumentation());

        assertEquals(0, enumType.getValue().get(0).getEquivalent().size());
        assertEquals(0, enumType.getValue().get(1).getEquivalent().size());
        assertEquals(1, enumType.getValue().get(2).getEquivalent().size());
        assertEquals("test", enumType.getValue().get(2).getEquivalent().get(0).getContext());
        assertEquals("three-equivalent", enumType.getValue().get(2).getEquivalent().get(0)
                .getValue());
    }

    @Test
    public void testEnumerationOpenTransformer() throws Exception {
        TLOpenEnumeration enumType = getOpenEnumeration(PACKAGE_2_NAMESPACE, "library_1_p2",
                "SampleEnum_Open");

        assertNotNull(enumType);
        assertEquals("SampleEnum_Open", enumType.getName());
        assertNotNull(enumType.getDocumentation());
        assertNotNull(enumType.getValues());
        assertEquals(3, enumType.getValues().size());

        assertEquals("four", enumType.getValues().get(0).getLiteral());
        assertEquals("five", enumType.getValues().get(1).getLiteral());
        assertEquals("six", enumType.getValues().get(2).getLiteral());

        assertNotNull(enumType.getValues().get(0).getDocumentation());
        assertNotNull(enumType.getValues().get(1).getDocumentation());
        assertNotNull(enumType.getValues().get(2).getDocumentation());

        assertNotNull(enumType.getValues().get(0).getEquivalents());
        assertEquals(0, enumType.getValues().get(0).getEquivalents().size());
        assertNotNull(enumType.getValues().get(1).getEquivalents());
        assertEquals(0, enumType.getValues().get(1).getEquivalents().size());
        assertNotNull(enumType.getValues().get(2).getEquivalents());
        assertEquals(1, enumType.getValues().get(2).getEquivalents().size());
        assertEquals("test", enumType.getValues().get(2).getEquivalents().get(0).getContext());
        assertEquals("six-equivalent", enumType.getValues().get(2).getEquivalents().get(0)
                .getDescription());
    }

    @Test
    public void testTLOpenEnumerationTransformer() throws Exception {
        EnumerationOpen enumType = transformOpenEnumeration(PACKAGE_2_NAMESPACE, "library_1_p2",
                "SampleEnum_Open");

        assertNotNull(enumType);
        assertEquals("SampleEnum_Open", enumType.getName());
        assertNotNull(enumType.getDocumentation());
        assertNotNull(enumType.getValue());
        assertEquals(3, enumType.getValue().size());

        assertEquals("four", enumType.getValue().get(0).getLiteral());
        assertEquals("five", enumType.getValue().get(1).getLiteral());
        assertEquals("six", enumType.getValue().get(2).getLiteral());

        assertNotNull(enumType.getValue().get(0).getDocumentation());
        assertNotNull(enumType.getValue().get(1).getDocumentation());
        assertNotNull(enumType.getValue().get(2).getDocumentation());

        assertEquals(0, enumType.getValue().get(0).getEquivalent().size());
        assertEquals(0, enumType.getValue().get(1).getEquivalent().size());
        assertEquals(1, enumType.getValue().get(2).getEquivalent().size());
        assertEquals("test", enumType.getValue().get(2).getEquivalent().get(0).getContext());
        assertEquals("six-equivalent", enumType.getValue().get(2).getEquivalent().get(0).getValue());
    }

    private TLClosedEnumeration getClosedEnumeration(String namespace, String libraryName,
            String enumName) throws Exception {
        TLLibrary library = getLibrary(namespace, libraryName);

        return (library == null) ? null : library.getClosedEnumerationType(enumName);
    }

    private EnumerationClosed transformClosedEnumeration(String namespace, String libraryName,
            String enumName) throws Exception {
        TLClosedEnumeration origEnum = getClosedEnumeration(namespace, libraryName, enumName);
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(origEnum.getOwningLibrary()));
        ObjectTransformer<TLClosedEnumeration, EnumerationClosed, SymbolResolverTransformerContext> transformer = factory
                .getTransformer(origEnum, EnumerationClosed.class);

        return transformer.transform(origEnum);
    }

    private TLOpenEnumeration getOpenEnumeration(String namespace, String libraryName,
            String enumName) throws Exception {
        TLLibrary library = getLibrary(namespace, libraryName);

        return (library == null) ? null : library.getOpenEnumerationType(enumName);
    }

    private EnumerationOpen transformOpenEnumeration(String namespace, String libraryName,
            String enumName) throws Exception {
        TLOpenEnumeration origEnum = getOpenEnumeration(namespace, libraryName, enumName);
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(origEnum.getOwningLibrary()));
        ObjectTransformer<TLOpenEnumeration, EnumerationOpen, SymbolResolverTransformerContext> transformer = factory
                .getTransformer(origEnum, EnumerationOpen.class);

        return transformer.transform(origEnum);
    }

}
