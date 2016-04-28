/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html;

import static org.junit.Assert.*;

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
public class LibraryIndexFrameWriterTest extends WriterTest{
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WriterTest.setUpBeforeClass();
		DEST_DIR.mkdir();
	}


	@Test
	public void testItShouldAddNamespacesForLibraries() throws Exception {
		String filename = LibraryIndexFrameWriter.DEFAULT_FILENAME;
		LibraryIndexFrameWriter.generate(config);
		byte[] encoded = Files.readAllBytes(Paths.get(config.destDirName + filename));
		 String content = new String(encoded);
		 List<TLLibrary> ns = config.getLibraries();
		 assertTrue(ns.size() > 0);
		 for(TLLibrary lib : ns){
			 assertTrue("No namespace.", content.contains(lib.getName()));
		 }
	}

}
