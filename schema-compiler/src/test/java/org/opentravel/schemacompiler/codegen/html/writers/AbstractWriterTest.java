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

package org.opentravel.schemacompiler.codegen.html.writers;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;

import java.io.File;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AbstractWriterTest {

    protected static Configuration config;

    public static final File DEST_DIR = new File( "target/test" );

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Configuration.reset();
        config = Configuration.getInstance();
        config.setDestDirName( DEST_DIR + File.separator );
        config.setStylesheetfile( "stylesheet.css" );
        config.setModel( TestLibraryProvider.getModel() );
        config.setWindowtitle( "TestProject" );
        config.setDoctitle( "TestProject.otp" );
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        cleanDirectory( DEST_DIR );
        Configuration.reset();
    }

    /**
     * Delete all files starting from the given directory. Then delete the directory.
     * 
     * @param dir
     * @return
     */
    public static void cleanDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                cleanDirectory( f );
            }
        }
        dir.delete();
    }

    protected TLDocumentation getTestDocumentation() {
        TLDocumentation doc = new TLDocumentation();
        doc.setDescription(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. " );
        TLDocumentationItem item = new TLDocumentationItem();
        item.setText( "This is a deprecation." );
        doc.addDeprecation( item );
        item = new TLDocumentationItem();
        item.setText( "This is an implementer." );
        doc.addImplementer( item );
        item = new TLDocumentationItem();
        item.setText( "This is a reference." );
        doc.addReference( item );
        item = new TLDocumentationItem();
        item.setText( "This is a more info." );
        doc.addMoreInfo( item );
        item = new TLAdditionalDocumentationItem();
        item.setText( "This is an other doc." );
        doc.addOtherDoc( (TLAdditionalDocumentationItem) item );
        return doc;
    }
}
