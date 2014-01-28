package org.opentravel.schemacompiler.version;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.version.OTA2VersionScheme;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Verifies the operation of the <code>VersionComparator</code> class for the OTA2 versioning
 * scheme.
 * 
 * @author S. Livezey
 */
public class TestVersionComparator {

    private static final String TEST_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/test/v1_0";

    private VersionScheme vScheme = new OTA2VersionScheme();

    @Test
    public void testSortAscending() throws Exception {
        List<Versioned> versionedItems = new ArrayList<Versioned>();

        versionedItems.add(new VersionedItem("2.0.0"));
        versionedItems.add(new VersionedItem("3.5.1"));
        versionedItems.add(new VersionedItem("3.0.0"));
        versionedItems.add(new VersionedItem("1.0.0"));
        versionedItems.add(new VersionedItem("2.2.0"));
        versionedItems.add(new VersionedItem("2.0.1"));
        versionedItems.add(new VersionedItem("2.0.1"));
        Collections.sort(versionedItems, vScheme.getComparator(true));

        assertEquals(7, versionedItems.size());
        assertEquals("1.0.0", versionedItems.get(0).getVersion());
        assertEquals("2.0.0", versionedItems.get(1).getVersion());
        assertEquals("2.0.1", versionedItems.get(2).getVersion());
        assertEquals("2.0.1", versionedItems.get(3).getVersion());
        assertEquals("2.2.0", versionedItems.get(4).getVersion());
        assertEquals("3.0.0", versionedItems.get(5).getVersion());
        assertEquals("3.5.1", versionedItems.get(6).getVersion());
    }

    @Test
    public void testSortDescending() throws Exception {
        List<Versioned> versionedItems = new ArrayList<Versioned>();

        versionedItems.add(new VersionedItem("2.0.0"));
        versionedItems.add(new VersionedItem("3.5.1"));
        versionedItems.add(new VersionedItem("3.0.0"));
        versionedItems.add(new VersionedItem("1.0.0"));
        versionedItems.add(new VersionedItem("2.2.0"));
        versionedItems.add(new VersionedItem("2.0.1"));
        versionedItems.add(new VersionedItem("2.0.1"));
        Collections.sort(versionedItems, vScheme.getComparator(false));

        assertEquals(7, versionedItems.size());
        assertEquals("3.5.1", versionedItems.get(0).getVersion());
        assertEquals("3.0.0", versionedItems.get(1).getVersion());
        assertEquals("2.2.0", versionedItems.get(2).getVersion());
        assertEquals("2.0.1", versionedItems.get(3).getVersion());
        assertEquals("2.0.1", versionedItems.get(4).getVersion());
        assertEquals("2.0.0", versionedItems.get(5).getVersion());
        assertEquals("1.0.0", versionedItems.get(6).getVersion());
    }

    private class VersionedItem implements Versioned {

        private String namespace;

        public VersionedItem(String version) {
            this.namespace = vScheme.setVersionIdentifier(TEST_NAMESPACE, version);
        }

        @Override
        public String getNamespace() {
            return null;
        }

        @Override
        public String getBaseNamespace() {
            return vScheme.getBaseNamespace(namespace);
        }

        @Override
        public String getVersion() {
            return vScheme.getVersionIdentifier(namespace);
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

    }

}
