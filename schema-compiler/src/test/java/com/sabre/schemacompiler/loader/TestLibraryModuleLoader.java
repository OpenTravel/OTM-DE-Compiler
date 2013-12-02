/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import com.sabre.schemacompiler.loader.impl.LibrarySchema1_4_ModuleLoader;
import com.sabre.schemacompiler.loader.impl.LibraryStreamInputSource;
import com.sabre.schemacompiler.util.SchemaCompilerTestUtils;
import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemacompiler.validate.FindingMessageFormat;
import com.sabre.schemacompiler.validate.ValidationFinding;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Validates the functions of the <code>LibrarySchema1_3_ModuleLoader</code>.
 * 
 * @author S. Livezey
 */
public class TestLibraryModuleLoader {
	
	@Test
	public void testLoadLibrariesByInputSource() throws Exception {
		File libraryFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v1/library_1_p1.xml");
		LibraryModuleLoader<InputStream> moduleLoader = new LibrarySchema1_4_ModuleLoader();
		ValidationFindings findings = new ValidationFindings();
		
		LibraryModuleInfo<Object> libraryInfo = moduleLoader.loadLibrary(new LibraryStreamInputSource(URLUtils.toURL(libraryFile)), findings);
		
		String[] findingMessages = findings.getAllValidationMessages(FindingMessageFormat.DEFAULT);
		for (String message : findingMessages) {
			System.out.println("> " + message);
		}
		assertNotNull(libraryInfo);
		assertEquals("library_1_p1", libraryInfo.getLibraryName());
		assertFalse(findings.hasFinding());
	}
	
	@Test
	public void testLoadLibrariesByInputSourceWithInvalidUrl() throws Exception {
		File libraryFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v1/library_xyz.xml");
		LibraryModuleLoader<InputStream> moduleLoader = new LibrarySchema1_4_ModuleLoader();
		ValidationFindings findings = new ValidationFindings();
		
		LibraryModuleInfo<Object> libraryInfo = moduleLoader.loadLibrary(new LibraryStreamInputSource(URLUtils.toURL(libraryFile)), findings);
		
		assertNull(libraryInfo);
		assertEquals(1, findings.count());
		
		List<ValidationFinding> findingList = findings.getAllFindingsAsList();
		assertEquals(1, findingList.size());
		assertEquals("schemacompiler.loader.WARNING_LIBRARY_NOT_FOUND", findingList.get(0).getMessageKey());
	}
	
	@Test
	public void testLoadLibrariesWithNullInputSource() throws Exception {
		LibraryModuleLoader<InputStream> moduleLoader = new LibrarySchema1_4_ModuleLoader();
		ValidationFindings findings = new ValidationFindings();
		
		LibraryModuleInfo<Object> libraryInfo = moduleLoader.loadLibrary(null, findings);
		
		assertNull(libraryInfo);
		assertEquals(1, findings.count());
		
		List<ValidationFinding> findingList = findings.getAllFindingsAsList();
		assertEquals(1, findingList.size());
		assertEquals("schemacompiler.loader.WARNING_LIBRARY_NOT_FOUND", findingList.get(0).getMessageKey());
	}
	
}
