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

import org.junit.Test;
import org.opentravel.schemacompiler.codegen.html.builders.AliasOwnerDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.writers.info.AliasInfoWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric.Bronson
 *
 */
public class AliasInfoWriterTest extends AbstractWriterTest {

    @Test
    public void testItShouldAddAliasesToTheContent() throws Exception {
        final List<String> aliases = new ArrayList<String>();
        aliases.add( "alias1" );
        aliases.add( "alias2" );
        aliases.add( "alias3" );
        SubWriterHolderWriter subWriter = new SubWriterHolderWriter( config, "", "TestName.html", "" );
        AliasInfoWriter writer = new AliasInfoWriter( subWriter, new AliasOwnerDocumentationBuilder() {

            @Override
            public List<String> getAliases() {
                return aliases;
            }
        } );
        HtmlTree div = new HtmlTree( HtmlTag.DIV );
        writer.addInfo( div );
        String content = div.toString();
        assertTrue( "Incorrect header", content.contains( "Aliases" ) );
        for (String alias : aliases) {
            assertTrue( "No alias.", content.contains( alias ) );
        }
        subWriter.close();
    }
}
