/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLLibrary;

import org.opentravel.schemacompiler.codegen.html.writers.WriterTest;

/**
 * @author Eric.Bronson
 *
 */
public class LibraryIndexWriterTest extends WriterTest {

	private static String content;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WriterTest.setUpBeforeClass();
		String filename = LibraryIndexWriter.DEFAULT_FILENAME;

		File f = new File(config.destDirName);
		f.mkdir();
		LibraryIndexWriter.generate(config);
		byte[] encoded = Files.readAllBytes(Paths.get(config.destDirName
				+ filename));
		content = new String(encoded);
	}

	@Test
	public void testItShouldAddNamespaces() throws Exception {
		List<TLLibrary> ns = config.getLibraries();
		assertTrue(ns.size() > 0);
		for (TLLibrary lib : ns) {
			assertTrue("No namespace.", content.contains(lib.getName()));
		}
	}

	@Test
	public void testItShouldAddTitle() throws Exception {
		assertTrue("No title.", content.contains(config.windowtitle));
	}

	@Test
	public void testItShouldAddProjectTitle() throws Exception {
		assertTrue("No title.", content.contains(config.doctitle));
	}

}
