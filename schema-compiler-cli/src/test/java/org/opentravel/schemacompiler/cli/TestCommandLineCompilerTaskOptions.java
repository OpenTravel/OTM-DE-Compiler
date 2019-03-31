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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.junit.Test;
import org.opentravel.schemacompiler.task.CompileAllTaskOptions;

/**
 * Verifies the functions of the <code>CommandLineCompilerTaskOptions</code> class.
 */
public class TestCommandLineCompilerTaskOptions {

    @Test
    public void testTaskOptions() throws Exception {
        String[] args = new String[] {"-b", "OTA2", "-c", "catalog-location", "-d", "output-folder", "-C",
            "example-context", "-D", "5", "-r", "4", "-p", "resource-path", "-s", "service-endpoint", "-M", "-E", "-e",
            "-J", "-S", "-W", "-X", "-H", "-o", "TestLibrary.otm"};
        CommandLine commandLineArgs = new GnuParser().parse( Main.getCommandLineOptions(), args );
        CommandLineCompilerTaskOptions taskOptions = new CommandLineCompilerTaskOptions( commandLineArgs );

        assertEquals( "OTA2", taskOptions.getBindingStyle() );
        assertEquals( "catalog-location", taskOptions.getCatalogLocation() );
        assertEquals( "output-folder", taskOptions.getOutputFolder() );
        assertEquals( "example-context", taskOptions.getExampleContext() );
        assertEquals( true, taskOptions.isGenerateExamples() );
        assertEquals( (Integer) 5, taskOptions.getExampleMaxDepth() );
        assertEquals( (Integer) 4, taskOptions.getExampleMaxRepeat() );
        assertEquals( true, taskOptions.isSuppressOptionalFields() );
        assertEquals( true, taskOptions.isGenerateMaxDetailsForExamples() );
        assertEquals( true, taskOptions.isCompileJsonSchemas() );
        assertEquals( true, taskOptions.isCompileSchemas() );
        assertEquals( true, taskOptions.isCompileServices() );
        assertEquals( true, taskOptions.isCompileSwagger() );
        assertEquals( true, taskOptions.isSuppressOtmExtensions() );
        assertEquals( true, taskOptions.isCompileHtml() );
        assertEquals( "resource-path", taskOptions.getResourceBaseUrl() );
        assertEquals( "service-endpoint", taskOptions.getServiceEndpointUrl() );
    }

    @Test
    public void testServiceLibraryUrl() throws Exception {
        String[] args = new String[] {"-W", "TestLibrary.otm"};
        CommandLine commandLineArgs = new GnuParser().parse( Main.getCommandLineOptions(), args );
        CompileAllTaskOptions taskOptions = new CommandLineCompilerTaskOptions( commandLineArgs );

        assertTrue( taskOptions.getServiceLibraryUrl().toExternalForm().endsWith( "TestLibrary.otm" ) );
    }

    @Test
    public void testImpliedCompilerFunctions() throws Exception {
        String[] args = new String[] {"TestLibrary.otm"};
        CommandLine commandLineArgs = new GnuParser().parse( Main.getCommandLineOptions(), args );
        CompileAllTaskOptions taskOptions = new CommandLineCompilerTaskOptions( commandLineArgs );

        assertEquals( true, taskOptions.isCompileSchemas() );
        assertEquals( true, taskOptions.isCompileServices() );
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testApplyTaskOptions() throws Exception {
        CommandLine commandLineArgs = new GnuParser().parse( Main.getCommandLineOptions(), new String[0] );
        CompileAllTaskOptions taskOptions = new CommandLineCompilerTaskOptions( commandLineArgs );

        taskOptions.applyTaskOptions( null );
    }

}
