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
