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
package org.opentravel.schemacompiler.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies the functions of the OTM command-line compiler utility application.
 */
public class TestCliMain {
    
    private static final File testModelsFolder = new File(
            System.getProperty( "user.dir" ), "/src/test/resources/test-models" );
    private static final File baseOutputFolder = new File(
            System.getProperty( "user.dir" ), "/target/test-output" );
    
    @Test
    public void testCompileLibrary() throws Exception {
        File libraryFile = new File( testModelsFolder, "/TestLibrary.otm" );
        File outputFolder = new File( baseOutputFolder, "/testCompileLibrary" );
        
        executeCli( libraryFile, outputFolder, "OTA2" );
    }
    
    @Test
    public void testPrintUsage() throws Exception {
        ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
        Main cliMain = new Main();
        String output;
        
        cliMain.setOutput( streamOut );
        cliMain.execute( new String[0] );
        output = streamOut.toString( "UTF-8" );
        Assert.assertTrue( output.contains( "usage:" ) );
    }
    
    @Test( expected = IOException.class )
    public void testInvalidLibraryLocation() throws Exception {
        File libraryFile = new File( testModelsFolder, "/NonExistentLibrary.otm" );
        File outputFolder = new File( baseOutputFolder, "/testInvalidLibraryLocation" );
        
        executeCli( libraryFile, outputFolder, "OTA2" );
    }
    
    @Test
    public void testInvalidBindingStyle() throws Exception {
        File libraryFile = new File( testModelsFolder, "/TestLibrary.otm" );
        File outputFolder = new File( baseOutputFolder, "/testInvalidLibraryLocation" );
        String output = executeCli( libraryFile, outputFolder, "XYZ" );
        
        Assert.assertTrue( output.contains( "Invalid binding style specified" ) );
    }
    
    protected String executeCli(File libraryFile, File outputFolder, String bindingStyle) throws Exception {
        ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
        Main cliMain = new Main();
        
        cliMain.setOutput( streamOut );
        cliMain.execute( new String[] {
                "-d", outputFolder.getAbsolutePath(),
                "-b", bindingStyle,
                libraryFile.getAbsolutePath()
            } );
        streamOut.flush();
        return streamOut.toString( "UTF-8" );
    }
}
