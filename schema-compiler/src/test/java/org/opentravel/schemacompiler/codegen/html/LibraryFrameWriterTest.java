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
import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.writers.AbstractWriterTest;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Eric.Bronson
 *
 */
public class LibraryFrameWriterTest extends AbstractWriterTest {

    private static TLLibrary library;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        AbstractWriterTest.setUpBeforeClass();
        library = (TLLibrary) config.getModel().getUserDefinedLibraries().get( 0 );
        LibraryFrameWriter.generate( config, library );
        String filePath = config.getDestDirName()
            + DirectoryManager.getDirectoryPath( AbstractDocumentationBuilder.getLibraryName( library ) )
            + LibraryFrameWriter.OUTPUT_FILE_NAME;
        byte[] encoded = Files.readAllBytes( Paths.get( filePath ) );
        new String( encoded );
    }

    @Test
    public void test() {
        List<LibraryMember> members = library.getNamedMembers();
        assertTrue( members.size() > 0 );
        // for(LibraryMember member : members){
        // TODO uncomment when we can handle resources and extension points
        // assertTrue(content.contains(member.getLocalName()));
        // }
    }

}
