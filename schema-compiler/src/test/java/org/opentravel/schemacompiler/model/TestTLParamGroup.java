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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Verifies the functions of the <code>TLParamGroup</code> class.
 */
public class TestTLParamGroup extends AbstractModelTest {

    @Test
    public void testIdentityFunctions() throws Exception {
        TLResource resource = addResource( "TestResource", library1 );
        TLParamGroup paramGroup =
            addParamGroup( "TestParamGroup", resource.getBusinessObjectRef().getIdFacet(), resource );

        assertEquals( "TestLibrary1.otm : TestResource/TestParamGroup", paramGroup.getValidationIdentity() );
    }

    @Test
    public void testDocumentationFunctions() throws Exception {
        testDocumentationFunctions( new TLParamGroup() );
    }

    @Test
    public void testParameterFunctions() throws Exception {
        TLResource resource = addResource( "TestResource", library1 );
        TLBusinessObject bo = resource.getBusinessObjectRef();
        TLParamGroup paramGroup = addParamGroup( "ParamGroup", resource.getBusinessObjectRef().getIdFacet(), resource );
        TLAttribute attr1 = addAttribute( "attr1", bo.getIdFacet() );
        TLAttribute attr2 = addAttribute( "attr2", bo.getIdFacet() );
        TLParameter param1 = addParameter( attr1, TLParamLocation.QUERY, paramGroup );
        TLParameter param2 = addParameter( attr2, TLParamLocation.QUERY, paramGroup );

        assertArrayEquals( new String[] {"attr1", "attr2"},
            getNames( paramGroup.getParameters(), f -> f.getFieldRef().getName() ) );

        paramGroup.moveDown( param1 );
        assertArrayEquals( new String[] {"attr2", "attr1"},
            getNames( paramGroup.getParameters(), f -> f.getFieldRef().getName() ) );

        paramGroup.sortParameters( (p1, p2) -> p1.getFieldRef().getName().compareTo( p2.getFieldRef().getName() ) );
        assertArrayEquals( new String[] {"attr1", "attr2"},
            getNames( paramGroup.getParameters(), f -> f.getFieldRef().getName() ) );

        paramGroup.moveUp( param2 );
        assertArrayEquals( new String[] {"attr2", "attr1"},
            getNames( paramGroup.getParameters(), f -> f.getFieldRef().getName() ) );

        paramGroup.removeParameter( param1 );
        assertArrayEquals( new String[] {"attr2"},
            getNames( paramGroup.getParameters(), f -> f.getFieldRef().getName() ) );
    }

}
