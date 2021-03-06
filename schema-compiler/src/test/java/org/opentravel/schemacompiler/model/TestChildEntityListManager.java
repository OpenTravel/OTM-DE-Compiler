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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.net.URL;
import java.util.Comparator;
import java.util.List;

/**
 * Validates the operation of the <code>ChildEntityListManagerComponent</code> using a list of <code>TLAlias</code>
 * elements as the underlying child implementation.
 * 
 * @author S. Livezey
 */
public class TestChildEntityListManager {

    @Test
    public void testGetChildren() throws Exception {
        TLCoreObject owner = getPopulatedCoreObject();
        List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
        List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();

        assertAliasNames( detailAliases, "CoreAlias_1_Detail", "CoreAlias_2_Detail" );
        assertAliasNames( detailListAliases, "CoreAlias_1_Detail_List", "CoreAlias_2_Detail_List" );
    }

    @Test
    public void testGetChild() throws Exception {
        TLCoreObject owner = getPopulatedCoreObject();
        TLAlias alias1 = owner.getDetailFacet().getAlias( "CoreAlias_1_Detail" );
        TLAlias listAlias1 = owner.getDetailListFacet().getAlias( "CoreAlias_1_Detail_List" );

        assertNotNull( alias1 );
        assertNotNull( listAlias1 );
    }

    @Test
    public void testAddChild() throws Exception {
        TLCoreObject owner = getPopulatedCoreObject();
        TLAlias alias3 = new TLAlias();

        alias3.setName( "CoreAlias_3" );
        owner.addAlias( alias3 );

        List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
        List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();

        assertAliasNames( detailAliases, "CoreAlias_1_Detail", "CoreAlias_2_Detail", "CoreAlias_3_Detail" );
        assertAliasNames( detailListAliases, "CoreAlias_1_Detail_List", "CoreAlias_2_Detail_List",
            "CoreAlias_3_Detail_List" );
    }

    @Test
    public void testAddChildAtIndex() throws Exception {
        TLCoreObject owner = getPopulatedCoreObject();
        TLAlias alias3 = new TLAlias();

        alias3.setName( "CoreAlias_3" );
        owner.addAlias( 1, alias3 );

        List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
        List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();

        assertAliasNames( detailAliases, "CoreAlias_1_Detail", "CoreAlias_3_Detail", "CoreAlias_2_Detail" );
        assertAliasNames( detailListAliases, "CoreAlias_1_Detail_List", "CoreAlias_3_Detail_List",
            "CoreAlias_2_Detail_List" );
    }

    @Test
    public void testRemoveChild() throws Exception {
        TLCoreObject owner = getPopulatedCoreObject();
        TLAlias alias1 = owner.getAlias( "CoreAlias_1" );

        owner.removeAlias( alias1 );

        List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
        List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();

        assertAliasNames( detailAliases, "CoreAlias_2_Detail" );
        assertAliasNames( detailListAliases, "CoreAlias_2_Detail_List" );
    }

    @Test
    public void testMoveUp() throws Exception {
        TLCoreObject owner = getPopulatedCoreObject();
        TLAlias alias2 = owner.getAlias( "CoreAlias_2" );

        owner.moveUp( alias2 );

        List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
        List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();

        assertAliasNames( detailAliases, "CoreAlias_2_Detail", "CoreAlias_1_Detail" );
        assertAliasNames( detailListAliases, "CoreAlias_2_Detail_List", "CoreAlias_1_Detail_List" );
    }

    @Test
    public void testMoveDown() throws Exception {
        TLCoreObject owner = getPopulatedCoreObject();
        TLAlias alias1 = owner.getAlias( "CoreAlias_1" );

        owner.moveDown( alias1 );

        List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
        List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();

        assertAliasNames( detailAliases, "CoreAlias_2_Detail", "CoreAlias_1_Detail" );
        assertAliasNames( detailListAliases, "CoreAlias_2_Detail_List", "CoreAlias_1_Detail_List" );
    }

    @Test
    public void testSortChildren() throws Exception {
        TLCoreObject owner = getPopulatedCoreObject();
        TLAlias alias3 = new TLAlias();

        alias3.setName( "CoreAlias_3" );
        owner.addAlias( 1, alias3 ); // insert out of natural order

        owner.sortAliases( new Comparator<TLAlias>() {
            public int compare(TLAlias alias1, TLAlias alias2) {
                return alias1.getName().compareTo( alias2.getName() );
            }
        } );

        List<TLAlias> detailAliases = owner.getDetailFacet().getAliases();
        List<TLAlias> detailListAliases = owner.getDetailListFacet().getAliases();

        assertAliasNames( detailAliases, "CoreAlias_1_Detail", "CoreAlias_2_Detail", "CoreAlias_3_Detail" );
        assertAliasNames( detailListAliases, "CoreAlias_1_Detail_List", "CoreAlias_2_Detail_List",
            "CoreAlias_3_Detail_List" );
    }

    @Test
    public void testClearFacet() throws Exception {
        TLCoreObject owner = getPopulatedCoreObject();
        TLFacet detailFacet = owner.getDetailFacet();
        TLListFacet detailListFacet = owner.getDetailListFacet();

        detailFacet.clearFacet();

        List<TLAlias> detailAliases = detailFacet.getAliases();
        List<TLAlias> detailListAliases = detailListFacet.getAliases();

        assertEquals( 0, detailFacet.getAttributes().size() );
        assertEquals( 0, detailFacet.getElements().size() );
        assertEquals( 0, detailFacet.getIndicators().size() );
        assertNull( detailFacet.getDocumentation() );
        assertFalse( detailFacet.isNotExtendable() );
        assertEquals( 0, detailAliases.size() );
        assertEquals( 0, detailListAliases.size() );
    }

    private TLCoreObject getPopulatedCoreObject() throws Exception {
        TLModel model = new TLModel();
        TLLibrary library = new TLLibrary();
        TLCoreObject core = new TLCoreObject();
        TLAlias detailAlias1 = new TLAlias();
        TLAlias detailAlias2 = new TLAlias();

        library.setLibraryUrl( new URL( "file:////usr/local/temp/test.xml" ) );
        library.setNamespace( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1" );
        library.setName( "test" );
        model.addLibrary( library );
        library.addNamedMember( core );

        detailAlias1.setName( "CoreAlias_1" );
        detailAlias2.setName( "CoreAlias_2" );
        core.addAlias( detailAlias1 );
        core.addAlias( detailAlias2 );
        return core;
    }

    private void assertAliasNames(List<TLAlias> aliasList, String... aliasNames) {
        assertEquals( aliasNames.length, aliasList.size() );

        for (int i = 0; i < aliasNames.length; i++) {
            assertEquals( aliasNames[i], aliasList.get( i ).getName() );
        }
    }

}
