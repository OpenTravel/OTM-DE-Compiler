/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.codegen.html.DirectoryManager;
import org.opentravel.schemacompiler.codegen.html.Util;
import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.writers.WriterTest;

/**
 * @author Eric.Bronson
 *
 */
public class HtmlDocletTest extends WriterTest {

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
		config.stylesheetfile = "";
		doclet.generateOtherFiles(config.getModel());
		File file = new File(config.getDestDirName()
				+ HtmlDoclet.DEFAULT_STYLESHEET);
		assertTrue(file.exists());
		file = new File(config.getDestDirName() + File.separator
				+ Util.RESOURCESDIR + File.separator
				+ HtmlDoclet.TOGGLE_CLOSE_IMAGE);
		assertTrue(file.exists());
		file = new File(config.getDestDirName() + File.separator
				+ Util.RESOURCESDIR + File.separator
				+ HtmlDoclet.TOGGLE_OPEN_IMAGE);
		assertTrue(file.exists());
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
		doclet.generateLibraryFiles(config.getModel());
		for (TLLibrary lib : config.getModel().getUserDefinedLibraries()) {
			String basePath = config.getDestDirName()
					+ DirectoryManager.getPath(AbstractDocumentationBuilder.getLibraryName(lib));
			for(LibraryMember member : lib.getNamedMembers()){
				File file = new File(basePath + File.separator + member.getLocalName() + ".html");
				assertTrue("No member file: " + member.getLocalName(), file.exists());
			}
		}
	}
}
