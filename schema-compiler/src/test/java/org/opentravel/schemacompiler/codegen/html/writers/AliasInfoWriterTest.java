/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.writers.info.AliasInfoWriter;
import org.opentravel.schemacompiler.codegen.html.builders.AliasOwnerDocumentationBuilder;

/**
 * @author Eric.Bronson
 *
 */
public class AliasInfoWriterTest extends WriterTest{

	@Test
	public void testItShouldAddAliasesToTheContent() throws Exception {
		final List<String> aliases = new ArrayList<String>();
		aliases.add("alias1");
		aliases.add("alias2");
		aliases.add("alias3");
		SubWriterHolderWriter subWriter = new SubWriterHolderWriter(
				config, "", "TestName.html", "");
		AliasInfoWriter writer = new AliasInfoWriter(subWriter,
				new AliasOwnerDocumentationBuilder() {

					@Override
					public List<String> getAliases() {
						return aliases;
					}
				});
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		writer.addInfo(div);
		String content = div.toString();
		assertTrue("Incorrect header", content.contains("Aliases"));
		for (String alias : aliases) {
			assertTrue("No alias.", content.contains(alias));
		}
		subWriter.close();
	}
}
