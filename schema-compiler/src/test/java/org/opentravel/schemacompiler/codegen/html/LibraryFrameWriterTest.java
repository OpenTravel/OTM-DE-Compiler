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
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.codegen.html.DirectoryManager;
import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.writers.WriterTest;

/**
 * @author Eric.Bronson
 *
 */
public class LibraryFrameWriterTest extends WriterTest {

	private static String content;
	
	private static TLLibrary library;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WriterTest.setUpBeforeClass();
		library = (TLLibrary) config.model.getLibrary("http://www.travelport.com/otm/test/v0", "TestLibrary");
		LibraryFrameWriter.generate(config, library);
		String filePath = config.destDirName + DirectoryManager.getDirectoryPath(AbstractDocumentationBuilder.getLibraryName(library)) + LibraryFrameWriter.OUTPUT_FILE_NAME;
		byte[] encoded = Files.readAllBytes(Paths.get(filePath));
		content = new String(encoded);
	}

	@Test
	public void test() {
		List<LibraryMember> members = library.getNamedMembers();
		assertTrue(members.size() > 0);
		for(LibraryMember member : members){
			assertTrue(content.contains(member.getLocalName()));
		}
	}

}
