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

package org.opentravel.schemacompiler.ioc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

/**
 * Verifies the functions of the <code>CompilerExtensionRegistry</code> class.
 */
public class TestCompilerExtensionRegistry {

    @Test
    public void testGetAvailableExtensionIds() throws Exception {
        List<String> extensionIds = CompilerExtensionRegistry.getAvailableExtensionIds();

        assertTrue( extensionIds.contains( "OTA2" ) );
        assertTrue( extensionIds.contains( "XYZ" ) );
    }

    @Test
    public void testSetActiveExtension() throws Exception {
        assertEquals( "OTA2", CompilerExtensionRegistry.getActiveExtension() ); // Default value

        CompilerExtensionRegistry.setActiveExtension( "XYZ" );
        assertEquals( "XYZ", CompilerExtensionRegistry.getActiveExtension() );
    }

}
