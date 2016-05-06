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

import static org.junit.Assert.*;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLDocumentation;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.writers.info.DocumentationInfoWriter;

/**
 * @author Eric.Bronson
 *
 */
public class DocumentationInfoWriterTest extends WriterTest{
	

	@Test
	public void testDocumentation() throws Exception {
		TLDocumentation doc = getTestDocumentation();
		SubWriterHolderWriter subWriter = new SubWriterHolderWriter(config,"","TestName.html", "");
		DocumentationInfoWriter writer = new DocumentationInfoWriter(subWriter, doc);
		
		Content classInfoTree = new HtmlTree(HtmlTag.DIV);
		writer.addInfo(classInfoTree);
		String html = classInfoTree.toString();
		assertTrue("No Documentation header.", html.contains("Documentation"));
		assertTrue("No Description.", html.contains(doc.getDescription()));
		assertTrue("No Implementers.", html.contains(doc.getImplementers().get(0).getText()));
		assertTrue("No Deprecations.", html.contains(doc.getDeprecations().get(0).getText()));
		assertTrue("No References.", html.contains(doc.getReferences().get(0).getText()));
		assertTrue("No MoreInfos.", html.contains(doc.getMoreInfos().get(0).getText()));
		assertTrue("No Other docs.", html.contains(doc.getOtherDocs().get(0).getText()));
		subWriter.close();
	}
}
