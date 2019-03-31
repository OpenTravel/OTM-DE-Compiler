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

package org.opentravel.schemacompiler.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;

/**
 * Validates the functions of the <code>ServiceAssemblyManager</code>.
 */
public class TestServiceAssemblyManager {

    private ServiceAssemblyManager assemblyManager;

    @Before
    public void setup() throws Exception {
        assemblyManager = new ServiceAssemblyManager();
    }

    @Test
    public void testCreateAssembly() throws Exception {
        ServiceAssembly assembly = newAssembly();

        assertNotNull( assembly.getAssemblyUrl() );
        assertNotNull( assembly.getBaseNamespace() );
        assertNotNull( assembly.getName() );
        assertNotNull( assembly.getVersion() );
    }

    @Test
    public void testValidateAssembly_NoFindings() throws Exception {
        ServiceAssembly assembly = newAssembly();
        ValidationFindings findings = assemblyManager.validateAssembly( assembly );

        assertFalse( findings.hasFinding() );
    }

    @Test
    public void testValidateAssembly_NullValues() throws Exception {
        ServiceAssembly assembly = new ServiceAssembly();
        ValidationFindings findings = assemblyManager.validateAssembly( assembly );

        assertTrue( findings.hasFinding() );
        assertEquals( 4, findings.getAllFindingsAsList().size() );
    }

    private ServiceAssembly newAssembly() throws Exception {
        File tempFolder = new File( System.getProperty( "java.io.tmpdir" ) );

        return assemblyManager.newAssembly( "http://www.opentravel.org", "TestAssembly", "1.0.0",
            new File( tempFolder, "TestAssembly.osm" ) );
    }

}
