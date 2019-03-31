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

package org.opentravel.schemacompiler.codegen.html;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.html.writers.AbstractWriterTest;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Eric.Bronson
 *
 */
public class LibraryIndexFrameWriterTest extends AbstractWriterTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        AbstractWriterTest.setUpBeforeClass();
        DEST_DIR.mkdir();
    }


    @Test
    public void testItShouldAddNamespacesForLibraries() throws Exception {
        String filename = LibraryIndexFrameWriter.DEFAULT_FILENAME;
        LibraryIndexFrameWriter.generate( config );
        byte[] encoded = Files.readAllBytes( Paths.get( config.getDestDirName() + filename ) );
        String content = new String( encoded );
        List<TLLibrary> ns = config.getLibraries();
        assertTrue( ns.size() > 0 );
        for (TLLibrary lib : ns) {
            assertTrue( "No namespace.", content.contains( lib.getName() ) );
        }
    }

}
