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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>TLModel</code> class.
 */
public class TestTLModel extends AbstractModelTest {

    @Test
    public void testIdentityFunctions() throws Exception {
        assertEquals( "", model.getValidationIdentity() );
    }

    @Test
    public void testLibraryFunctions() throws Exception {
        assertTrue( model.hasNamespace( library1.getNamespace() ) );
        assertFalse( model.hasNamespace( "http://www.opentravel.org/ns/non-existent/v0" ) );

        model.removeLibrary( library1 );
        assertFalse( model.hasNamespace( library1.getNamespace() ) );
    }

    @Test
    public void testNegativeMoveScenarios() throws Exception {
        TLCoreObject entity1 = addCore( "TestObject1", library1 );
        TLService service1 = addService( "TestService1", library1 );

        addService( "TestService2", library2 );

        // Negative case of a non-move (but does not throw an exception)
        model.moveToLibrary( entity1, library1 );
        assertEquals( library1, entity1.getOwningLibrary() );

        testNegativeCase( library1, l -> model.moveToLibrary( null, library2 ), NullPointerException.class );
        testNegativeCase( library1, l -> model.moveToLibrary( entity1, null ), IllegalArgumentException.class );
        testNegativeCase( library1, l -> model.moveToLibrary( service1, library2 ), IllegalArgumentException.class );
    }

    @Test
    public void testMoveEntity() throws Exception {
        TLCoreObject entity = addCore( "TestObject", library1 );

        model.moveToLibrary( entity, library2 );
        assertEquals( library2, entity.getOwningLibrary() );
    }

    @Test
    public void testMoveService() throws Exception {
        TLService service = addService( "TestService", library1 );

        model.moveToLibrary( service, library2 );
        assertEquals( library2, service.getOwningLibrary() );
    }

    @Test
    public void testModelElementListener() throws Exception {
        List<String> events = new ArrayList<>();
        ModelElementListener listener = new ModelElementListener() {
            public void processOwnershipEvent(OwnershipEvent<?,?> event) {}

            public void processValueChangeEvent(ValueChangeEvent<?,?> event) {
                events.add( "value-change" );
            }
        };

        assertEquals( 0, library1.getListeners().size() );
        library1.addListener( listener );
        assertEquals( 1, library1.getListeners().size() );

        library1.setComments( "Test comments..." );
        assertEquals( 1, events.size() );

        library1.removeListener( listener );
        assertEquals( 0, library1.getListeners().size() );
    }

    @Test
    public void testCloneModelElement() throws Exception {
        TLCoreObject origEntity = addCore( "TestObject", library1 );
        TLCoreObject clonedEntity = (TLCoreObject) origEntity.cloneElement();

        assertEquals( null, clonedEntity.getOwningLibrary() );
        assertEquals( origEntity.getLocalName(), clonedEntity.getLocalName() );
        assertNotEquals( origEntity, clonedEntity );
    }

}
