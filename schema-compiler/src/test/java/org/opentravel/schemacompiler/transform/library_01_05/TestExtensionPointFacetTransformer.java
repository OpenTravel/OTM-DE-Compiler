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
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_05.ExtensionPointFacet;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

import javax.xml.XMLConstants;

/**
 * Verifies the operation of the transformers that handle conversions to and from <code>TLExtensionPointFacet</code>
 * objects.
 * 
 * @author S. Livezey
 */
public class TestExtensionPointFacetTransformer extends Abstract_1_5_TestTransformers {

    @Test
    public void testExtensionPointFacetTransformer() throws Exception {
        TLExtensionPointFacet facet = getExtensionPointFacet( PACKAGE_EXT_NAMESPACE, "library_3_ext",
            "ExtensionPoint_CompoundBusinessObject_Summary" );

        assertNotNull( facet );
        assertNotNull( facet.getExtension() );
        assertNotNull( facet.getExtension().getExtendsEntity() );
        assertEquals( PACKAGE_2_NAMESPACE, facet.getExtension().getExtendsEntity().getNamespace() );
        assertEquals( "CompoundBusinessObject_Summary", facet.getExtension().getExtendsEntity().getLocalName() );
        assertEquals( 2, facet.getAttributes().size() );
        assertEquals( 1, facet.getElements().size() );
        assertEquals( 1, facet.getIndicators().size() );

        assertEquals( "extAttr1", facet.getAttributes().get( 0 ).getName() );
        assertNull( facet.getAttributes().get( 0 ).getDocumentation() );
        assertNotNull( facet.getAttributes().get( 0 ).getExamples() );
        assertEquals( 0, facet.getAttributes().get( 0 ).getExamples().size() );
        assertNotNull( facet.getAttributes().get( 0 ).getEquivalents() );
        assertEquals( 0, facet.getAttributes().get( 0 ).getEquivalents().size() );
        assertFalse( facet.getAttributes().get( 0 ).isMandatory() );
        assertEquals( "xsd:string", facet.getAttributes().get( 0 ).getTypeName() );
        assertNotNull( facet.getAttributes().get( 0 ).getType() );
        assertEquals( XSDSimpleType.class, facet.getAttributes().get( 0 ).getType().getClass() );
        assertEquals( XMLConstants.W3C_XML_SCHEMA_NS_URI, facet.getAttributes().get( 0 ).getType().getNamespace() );
        assertEquals( "string", facet.getAttributes().get( 0 ).getType().getLocalName() );

        assertEquals( "extAttr2", facet.getAttributes().get( 1 ).getName() );
        assertNull( facet.getAttributes().get( 1 ).getDocumentation() );
        assertNotNull( facet.getAttributes().get( 1 ).getExamples() );
        assertEquals( 0, facet.getAttributes().get( 1 ).getExamples().size() );
        assertNotNull( facet.getAttributes().get( 1 ).getEquivalents() );
        assertEquals( 0, facet.getAttributes().get( 1 ).getEquivalents().size() );
        assertFalse( facet.getAttributes().get( 1 ).isMandatory() );
        assertEquals( "xsd:int", facet.getAttributes().get( 1 ).getTypeName() );
        assertNotNull( facet.getAttributes().get( 1 ).getType() );
        assertEquals( XSDSimpleType.class, facet.getAttributes().get( 1 ).getType().getClass() );
        assertEquals( XMLConstants.W3C_XML_SCHEMA_NS_URI, facet.getAttributes().get( 1 ).getType().getNamespace() );
        assertEquals( "int", facet.getAttributes().get( 1 ).getType().getLocalName() );

        assertEquals( "SampleCore_Detail", facet.getElements().get( 0 ).getName() );
        assertNull( facet.getElements().get( 0 ).getDocumentation() );
        assertNotNull( facet.getAttributes().get( 0 ).getExamples() );
        assertEquals( 0, facet.getAttributes().get( 0 ).getExamples().size() );
        assertNotNull( facet.getElements().get( 0 ).getEquivalents() );
        assertEquals( 0, facet.getElements().get( 0 ).getEquivalents().size() );
        assertFalse( facet.getElements().get( 0 ).isMandatory() );
        assertEquals( 0, facet.getElements().get( 0 ).getRepeat() );
        assertEquals( "pkg2:SampleCore_Detail", facet.getElements().get( 0 ).getTypeName() );
        assertNotNull( facet.getElements().get( 0 ).getType() );
        assertEquals( TLFacet.class, facet.getElements().get( 0 ).getType().getClass() );
        assertEquals( PACKAGE_2_NAMESPACE, facet.getElements().get( 0 ).getType().getNamespace() );
        assertEquals( "SampleCore_Detail", facet.getElements().get( 0 ).getType().getLocalName() );

        assertEquals( "extIndicator1", facet.getIndicators().get( 0 ).getName() );
        assertNull( facet.getIndicators().get( 0 ).getDocumentation() );
        assertNotNull( facet.getIndicators().get( 0 ).getEquivalents() );
        assertEquals( 0, facet.getIndicators().get( 0 ).getEquivalents().size() );
    }

    @Test
    public void testTLExtensionPointFacetTransformer() throws Exception {
        ExtensionPointFacet facet = transformExtensionPointFacet( PACKAGE_EXT_NAMESPACE, "library_3_ext",
            "ExtensionPoint_CompoundBusinessObject_Summary" );

        assertNotNull( facet );
        assertNotNull( facet.getExtension() );
        assertEquals( "pkg2:CompoundBusinessObject_Summary", facet.getExtension().getExtends() );
        assertEquals( 2, facet.getAttribute().size() );
        assertEquals( 1, facet.getElement().size() );
        assertEquals( 1, facet.getIndicator().size() );

        assertEquals( "extAttr1", facet.getAttribute().get( 0 ).getName() );
        assertNull( facet.getAttribute().get( 0 ).getDocumentation() );
        assertEquals( 0, facet.getAttribute().get( 0 ).getExample().size() );
        assertEquals( 0, facet.getAttribute().get( 0 ).getEquivalent().size() );
        assertNull( facet.getAttribute().get( 0 ).isMandatory() );
        assertEquals( "xsd:string", facet.getAttribute().get( 0 ).getType() );

        assertEquals( "extAttr2", facet.getAttribute().get( 1 ).getName() );
        assertNull( facet.getAttribute().get( 1 ).getDocumentation() );
        assertEquals( 0, facet.getAttribute().get( 1 ).getExample().size() );
        assertEquals( 0, facet.getAttribute().get( 1 ).getEquivalent().size() );
        assertNull( facet.getAttribute().get( 1 ).isMandatory() );
        assertEquals( "xsd:int", facet.getAttribute().get( 1 ).getType() );

        assertEquals( "SampleCore_Detail", facet.getElement().get( 0 ).getName() );
        assertNull( facet.getElement().get( 0 ).getDocumentation() );
        assertEquals( 0, facet.getElement().get( 0 ).getExample().size() );
        assertEquals( 0, facet.getElement().get( 0 ).getEquivalent().size() );
        assertNull( facet.getElement().get( 0 ).isMandatory() );
        assertEquals( "0", facet.getElement().get( 0 ).getRepeat() );
        assertEquals( "pkg2:SampleCore_Detail", facet.getElement().get( 0 ).getType() );

        assertEquals( "extIndicator1", facet.getIndicator().get( 0 ).getName() );
        assertNull( facet.getIndicator().get( 0 ).getDocumentation() );
        assertEquals( 0, facet.getIndicator().get( 0 ).getEquivalent().size() );
    }

    private TLExtensionPointFacet getExtensionPointFacet(String namespace, String libraryName,
        String extensionPointName) throws Exception {
        TLLibrary library = getLibrary( namespace, libraryName );

        return (library == null) ? null : library.getExtensionPointFacetType( extensionPointName );
    }

    private ExtensionPointFacet transformExtensionPointFacet(String namespace, String libraryName,
        String extensionPointName) throws Exception {
        TLExtensionPointFacet origExtensionPoint = getExtensionPointFacet( namespace, libraryName, extensionPointName );
        TransformerFactory<SymbolResolverTransformerContext> factory =
            TransformerFactory.getInstance( SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                getContextJAXBTransformation( origExtensionPoint.getOwningLibrary() ) );
        ObjectTransformer<TLExtensionPointFacet,ExtensionPointFacet,SymbolResolverTransformerContext> transformer =
            factory.getTransformer( origExtensionPoint, ExtensionPointFacet.class );

        return transformer.transform( origExtensionPoint );
    }

}
