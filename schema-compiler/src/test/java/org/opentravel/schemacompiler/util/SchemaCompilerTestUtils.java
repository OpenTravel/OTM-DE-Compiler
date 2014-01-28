/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.util;

import java.io.File;

import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Static utility methods shared by numerous tests.
 * 
 * @author S. Livezey
 */
public class SchemaCompilerTestUtils {
	
	public static boolean DEBUG = false;
	
	/**
	 * Returns the base folder location for test library data to be utilized by the
	 * schema compiler test frameork.
	 * 
	 * @return String
	 */
	public static String getBaseLibraryLocation() {
		return System.getProperty("user.dir") + "/src/test/resources/libraries_1_4";
	}
	
	/**
	 * Returns the base folder location for test project files to be utilized by the
	 * schema compiler test frameork.
	 * 
	 * @return String
	 */
	public static String getBaseProjectLocation() {
		return System.getProperty("user.dir") + "/src/test/resources/projects";
	}
	
	/**
	 * Returns the test folder location for test project files to be utilized by the
	 * schema compiler test frameork when persistent changes to the project will be saved.
	 * 
	 * @return String
	 */
	public static String getTestProjectLocation() {
		File projectTestFolder = new File(System.getProperty("user.dir") + "/target/codegen-output/projects");
		
		if (!projectTestFolder.exists()) {
			projectTestFolder.mkdirs();
		}
		return projectTestFolder.getAbsolutePath();
	}
	
	/**
	 * Displays the validation findings if debugging is enabled.
	 * 
	 * @param findings  the validation findings to display
	 */
	public static void printFindings(ValidationFindings findings) {
		printFindings(findings, null);
	}
	
	/**
	 * Displays the validation findings if one or more findings of the specified type
	 * are present (and debugging is enabled).
	 * 
	 * @param findings  the validation findings to display
	 * @param findingType  the finding type to search for
	 */
	public static void printFindings(ValidationFindings findings, FindingType findingType) {
		if (DEBUG) {
			boolean hasFindings = ((findingType == null) && findings.hasFinding()) || ((findingType != null) && findings.hasFinding(findingType));
			
			if (hasFindings) {
				System.out.println("Validation Findings:");
				
				for (String message : findings.getAllValidationMessages(FindingMessageFormat.DEFAULT)) {
					System.out.println("  " + message);
				}
			}
		}
	}
	
}
