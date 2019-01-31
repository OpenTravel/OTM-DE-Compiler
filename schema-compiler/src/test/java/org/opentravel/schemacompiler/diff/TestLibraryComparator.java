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
package org.opentravel.schemacompiler.diff;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.util.ModelComparator;
import org.opentravel.schemacompiler.version.AbstractVersionHelperTests;

/**
 * Verifies the operation of the <code>LibraryComparator</code> and its
 * associated utility classes.
 */
public class TestLibraryComparator extends AbstractVersionHelperTests {
	
	@Test
	public void testLibraryComparison() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary oldLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
        TLLibrary newLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        
        ModelComparator comparator = new ModelComparator( ModelCompareOptions.getDefaultOptions() );
        LibraryChangeSet changeSet;
        
        changeSet = comparator.compareLibraries( oldLibrary, newLibrary );
        comparator.compareLibraries( oldLibrary, newLibrary, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() > 0 );
        
        changeSet = comparator.compareLibraries( newLibrary, oldLibrary );
        comparator.compareLibraries( newLibrary, oldLibrary, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() > 0 );
	}
	
	@Test
	public void testLibraryMinorVersionComparison() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary oldLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1, TEST_LIBRARY_NAME);
        TLLibrary newLibrary = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        ModelCompareOptions options = ModelCompareOptions.getDefaultOptions();
        
        options.setSuppressFieldVersionChanges( true );
        ModelComparator comparator = new ModelComparator( options );
        LibraryChangeSet changeSet;
        
        changeSet = comparator.compareLibraries( oldLibrary, newLibrary );
        comparator.compareLibraries( newLibrary, oldLibrary, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() > 0 );
	}
	
	@Test
	public void testLibraryNoChangeComparison() throws Exception {
        TLModel model = loadTestModel(FILE_VERSION_1_2_2);
        TLLibrary library = (TLLibrary) model.getLibrary(NS_VERSION_1_2, TEST_LIBRARY_NAME);
        
        ModelComparator comparator = new ModelComparator( ModelCompareOptions.getDefaultOptions() );
        LibraryChangeSet changeSet;
        
        changeSet = comparator.compareLibraries( library, library );
        comparator.compareLibraries( library, library, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() == 0 );
	}
	
}
