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

package org.opentravel.schemacompiler.codegen;


import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;

import java.util.List;

/**
 * Unit tests that verify the function of the contextual facet utility methods in <code>FacetCodegenUtils</code>.
 */
public class TestContextualFacetUtils {

    private static TLBusinessObject bo1 = new TLBusinessObject();
    private static TLBusinessObject bo2 = new TLBusinessObject();
    private static TLBusinessObject bo3 = new TLBusinessObject();
    private static TLContextualFacet cf1a = new TLContextualFacet();
    private static TLContextualFacet cf1b = new TLContextualFacet();
    private static TLContextualFacet cf2a = new TLContextualFacet();
    private static TLContextualFacet cf3a = new TLContextualFacet();
    private static TLContextualFacet cf3b = new TLContextualFacet();
    private static TLAttribute attr1 = new TLAttribute();
    private static TLAttribute attr2 = new TLAttribute();
    private static TLAttribute attr3 = new TLAttribute();
    private static TLAttribute attr4 = new TLAttribute();
    private static TLAttribute attr5 = new TLAttribute();

    @BeforeClass
    public static void setupTestModel() throws Exception {
        TLExtension bo2Ext = new TLExtension();
        TLExtension bo3Ext = new TLExtension();

        attr1.setName( "attr1" );
        attr2.setName( "attr2" );
        attr3.setName( "attr3" );
        attr4.setName( "attr4" );
        attr5.setName( "attr5" );

        bo1.setName( "BO1" );
        cf1a.setName( "CFA" );
        cf1a.setFacetType( TLFacetType.CUSTOM );
        cf1a.addAttribute( attr1 );
        bo1.addCustomFacet( cf1a );
        cf1b.setName( "CFB" );
        cf1b.setFacetType( TLFacetType.CUSTOM );
        cf1b.addAttribute( attr4 );
        cf1a.addChildFacet( cf1b );

        bo2.setName( "BO2" );
        bo2.setExtension( bo2Ext );
        bo2Ext.setExtendsEntity( bo1 );
        cf2a.setName( "CFA" );
        cf2a.setFacetType( TLFacetType.CUSTOM );
        cf2a.addAttribute( attr2 );
        bo2.addCustomFacet( cf2a );

        bo3.setName( "BO3" );
        bo3.setExtension( bo3Ext );
        bo3Ext.setExtendsEntity( bo2 );
        cf3a.setName( "CFA" );
        cf3a.setFacetType( TLFacetType.CUSTOM );
        cf3a.addAttribute( attr3 );
        bo3.addCustomFacet( cf3a );
        cf3b.setName( "CFB" );
        cf3b.setFacetType( TLFacetType.CUSTOM );
        cf3b.addAttribute( attr5 );
        cf3a.addChildFacet( cf3b );
    }

    @Test
    public void testBusinessObjectFacetOwnerExtension() throws Exception {
        assertEquals( bo2, FacetCodegenUtils.getFacetOwnerExtension( bo3 ) );
    }

    @Test
    public void testContextualFacetOwnerExtension() throws Exception {
        assertEquals( cf2a, FacetCodegenUtils.getFacetOwnerExtension( cf3a ) );
    }

    @Test
    public void testContextualFacetOwnerExtensionWithGhostFacet() throws Exception {
        assertEquals( cf1b, FacetCodegenUtils.getFacetOwnerExtension( cf3b ) );
    }

    @Test
    public void testContextualFacetGhostFacet() throws Exception {
        List<TLContextualFacet> ghostFacets = FacetCodegenUtils.findGhostFacets( cf2a, cf2a.getFacetType() );

        assertEquals( 1, ghostFacets.size() );
        assertEquals( "CFB", ghostFacets.get( 0 ).getName() );
        assertEquals( cf2a, ghostFacets.get( 0 ).getOwningEntity() );
    }

    @Test
    public void testContextualFacetHierarchy() throws Exception {
        List<TLFacet> facetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy( cf3a );

        assertEquals( 3, facetHierarchy.size() );
        assertEquals( bo3.getIdFacet(), facetHierarchy.get( 0 ) );
        assertEquals( bo3.getSummaryFacet(), facetHierarchy.get( 1 ) );
        assertEquals( cf3a, facetHierarchy.get( 2 ) );
    }

    @Test
    public void testNestedContextualFacetHierarchy() throws Exception {
        List<TLFacet> facetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy( cf3b );

        assertEquals( 4, facetHierarchy.size() );
        assertEquals( bo3.getIdFacet(), facetHierarchy.get( 0 ) );
        assertEquals( bo3.getSummaryFacet(), facetHierarchy.get( 1 ) );
        assertEquals( cf3a, facetHierarchy.get( 2 ) );
        assertEquals( cf3b, facetHierarchy.get( 3 ) );
    }

    @Test
    public void testContextualFacetInheritance() throws Exception {
        List<TLAttribute> cf3aAttributes = PropertyCodegenUtils.getInheritedFacetAttributes( cf3a );
        List<TLAttribute> cf3bAttributes = PropertyCodegenUtils.getInheritedFacetAttributes( cf3b );

        assertEquals( 3, cf3aAttributes.size() );
        assertEquals( attr1, cf3aAttributes.get( 0 ) );
        assertEquals( attr2, cf3aAttributes.get( 1 ) );
        assertEquals( attr3, cf3aAttributes.get( 2 ) );

        assertEquals( 2, cf3bAttributes.size() );
        assertEquals( attr4, cf3bAttributes.get( 0 ) );
        assertEquals( attr5, cf3bAttributes.get( 1 ) );
    }

}
