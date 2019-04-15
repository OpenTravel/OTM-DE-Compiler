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

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.html.writers.AbstractWriterTest;

import java.io.File;

/**
 * @author Eric.Bronson
 *
 */
public class HtmlDocletTest extends AbstractWriterTest {

    private HtmlDoclet doclet;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        doclet = new HtmlDoclet();
    }

    /**
     * Test method for
     * {@link org.opentravel.schemacompiler.codegen.html.HtmlDoclet#generateOtherFiles(org.opentravel.schemacompiler.model.TLModel)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateOtherFiles() throws Exception {
        config.setStylesheetfile( "" );
        doclet.generateOtherFiles( config.getModel() );
        File file = new File( config.getDestDirName() + HtmlDoclet.DEFAULT_STYLESHEET );
        assertTrue( file.exists() );
        file = new File( config.getDestDirName() + File.separator + Util.RESOURCESDIR + File.separator
            + HtmlDoclet.TOGGLE_CLOSE_IMAGE );
        assertTrue( file.exists() );
        file = new File( config.getDestDirName() + File.separator + Util.RESOURCESDIR + File.separator
            + HtmlDoclet.TOGGLE_OPEN_IMAGE );
        assertTrue( file.exists() );
    }

    /**
     * Test method for
     * {@link org.opentravel.schemacompiler.codegen.html.HtmlDoclet#generateLibraryFiles(org.opentravel.schemacompiler.model.TLModel)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateLibraryFiles() throws Exception {
        DEST_DIR.mkdir();
        doclet.generateLibraryFiles( config.getModel() );
    }

}
