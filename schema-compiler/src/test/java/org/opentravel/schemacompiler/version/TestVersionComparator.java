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

package org.opentravel.schemacompiler.version;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Verifies the operation of the <code>VersionComparator</code> class for the OTA2 versioning scheme.
 * 
 * @author S. Livezey
 */
public class TestVersionComparator {

    private static final String TEST_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/test/v1_0";

    private VersionScheme vScheme = new OTA2VersionScheme();

    @Test
    public void testSortAscending() throws Exception {
        List<Versioned> versionedItems = new ArrayList<Versioned>();

        versionedItems.add( new VersionedItem( "2.0.0" ) );
        versionedItems.add( new VersionedItem( "3.5.1" ) );
        versionedItems.add( new VersionedItem( "3.0.0" ) );
        versionedItems.add( new VersionedItem( "1.0.0" ) );
        versionedItems.add( new VersionedItem( "2.2.0" ) );
        versionedItems.add( new VersionedItem( "2.0.1" ) );
        versionedItems.add( new VersionedItem( "2.0.1" ) );
        Collections.sort( versionedItems, vScheme.getComparator( true ) );

        assertEquals( 7, versionedItems.size() );
        assertEquals( "1.0.0", versionedItems.get( 0 ).getVersion() );
        assertEquals( "2.0.0", versionedItems.get( 1 ).getVersion() );
        assertEquals( "2.0.1", versionedItems.get( 2 ).getVersion() );
        assertEquals( "2.0.1", versionedItems.get( 3 ).getVersion() );
        assertEquals( "2.2.0", versionedItems.get( 4 ).getVersion() );
        assertEquals( "3.0.0", versionedItems.get( 5 ).getVersion() );
        assertEquals( "3.5.1", versionedItems.get( 6 ).getVersion() );
    }

    @Test
    public void testSortDescending() throws Exception {
        List<Versioned> versionedItems = new ArrayList<Versioned>();

        versionedItems.add( new VersionedItem( "2.0.0" ) );
        versionedItems.add( new VersionedItem( "3.5.1" ) );
        versionedItems.add( new VersionedItem( "3.0.0" ) );
        versionedItems.add( new VersionedItem( "1.0.0" ) );
        versionedItems.add( new VersionedItem( "2.2.0" ) );
        versionedItems.add( new VersionedItem( "2.0.1" ) );
        versionedItems.add( new VersionedItem( "2.0.1" ) );
        Collections.sort( versionedItems, vScheme.getComparator( false ) );

        assertEquals( 7, versionedItems.size() );
        assertEquals( "3.5.1", versionedItems.get( 0 ).getVersion() );
        assertEquals( "3.0.0", versionedItems.get( 1 ).getVersion() );
        assertEquals( "2.2.0", versionedItems.get( 2 ).getVersion() );
        assertEquals( "2.0.1", versionedItems.get( 3 ).getVersion() );
        assertEquals( "2.0.1", versionedItems.get( 4 ).getVersion() );
        assertEquals( "2.0.0", versionedItems.get( 5 ).getVersion() );
        assertEquals( "1.0.0", versionedItems.get( 6 ).getVersion() );
    }

    private class VersionedItem implements Versioned {

        private String namespace;

        public VersionedItem(String version) {
            this.namespace = vScheme.setVersionIdentifier( TEST_NAMESPACE, version );
        }

        @Override
        public String getNamespace() {
            return null;
        }

        @Override
        public String getBaseNamespace() {
            return vScheme.getBaseNamespace( namespace );
        }

        @Override
        public String getVersion() {
            return vScheme.getVersionIdentifier( namespace );
        }

        @Override
        public String getVersionScheme() {
            return VersionSchemeFactory.getInstance().getDefaultVersionScheme();
        }

        @Override
        public boolean isLaterVersion(Versioned otherVersionedItem) {
            return false;
        }

        @Override
        public String getValidationIdentity() {
            return null;
        }

        @Override
        public String getLocalName() {
            return null;
        }

        @Override
        public AbstractLibrary getOwningLibrary() {
            return null;
        }

        @Override
        public TLModel getOwningModel() {
            return null;
        }

        @Override
        public LibraryElement cloneElement() {
            return null;
        }

        @Override
        public LibraryElement cloneElement(AbstractLibrary namingContext) {
            return null;
        }

        @Override
        public void addListener(ModelElementListener listener) {}

        @Override
        public void removeListener(ModelElementListener listener) {}

        @Override
        public Collection<ModelElementListener> getListeners() {
            return Collections.emptyList();
        }

    }

}
