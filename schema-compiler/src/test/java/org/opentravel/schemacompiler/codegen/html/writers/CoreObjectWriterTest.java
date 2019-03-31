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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.codegen.html.builders.CoreObjectDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLRole;

/**
 * @author Eric.Bronson
 *
 */
public class CoreObjectWriterTest extends AbstractWriterTest {
    private CoreObjectDocumentationBuilder builder;
    private CoreObjectWriter writer;
    private TLCoreObject co;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        co = TestLibraryProvider.getCoreObject( "SampleCore" );
        builder = new CoreObjectDocumentationBuilder( co );
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (writer != null) {
            writer.close();
        }
    }

    @Test
    public void testItShouldRolesToTheContent() throws Exception {
        writer = new CoreObjectWriter( builder, null, null );
        HtmlTree div = new HtmlTree( HtmlTag.DIV );
        writer.addRoleInfo( div );
        String content = div.toString();
        for (TLRole role : co.getRoleEnumeration().getRoles()) {
            assertTrue( "No role", content.contains( role.getName() ) );
        }
        assertTrue( "Incorrect header", content.contains( "Roles" ) );
    }

}
