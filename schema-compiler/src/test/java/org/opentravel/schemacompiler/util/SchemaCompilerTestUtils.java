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
    public static FindingType PRINT_FINDINGS = FindingType.ERROR;

    /**
     * Returns the base folder location for test library data to be utilized by the schema compiler
     * test frameork.
     * 
     * @return String
     */
    public static String getBaseLibraryLocation() {
    	if (OTM16Upgrade.otm16Enabled) {
            return System.getProperty("user.dir") + "/src/test/resources/libraries_1_6";
    	} else {
            return System.getProperty("user.dir") + "/src/test/resources/libraries_1_5";
    	}
    }

    /**
     * Returns the base folder location for test project files to be utilized by the schema compiler
     * test frameork.
     * 
     * @return String
     */
    public static String getBaseProjectLocation() {
        return System.getProperty("user.dir") + "/src/test/resources/projects";
    }

    /**
     * Returns the test folder location for test project files to be utilized by the schema compiler
     * test frameork when persistent changes to the project will be saved.
     * 
     * @return String
     */
    public static String getTestProjectLocation() {
        File projectTestFolder = new File(System.getProperty("user.dir")
                + "/target/codegen-output/projects");

        if (!projectTestFolder.exists()) {
            projectTestFolder.mkdirs();
        }
        return projectTestFolder.getAbsolutePath();
    }

    /**
     * Displays the validation findings if debugging is enabled.
     * 
     * @param findings
     *            the validation findings to display
     */
    public static void printFindings(ValidationFindings findings) {
        printFindings(findings, null);
    }

    /**
     * Displays the validation findings if one or more findings of the specified type are present
     * (and debugging is enabled).
     * 
     * @param findings
     *            the validation findings to display
     * @param findingType
     *            the finding type to search for
     */
    public static void printFindings(ValidationFindings findings, FindingType findingType) {
        if (DEBUG) {
            boolean hasFindings = ((findingType == null) && findings.hasFinding())
                    || ((findingType != null) && findings.hasFinding(findingType));

            if (hasFindings) {
            	String[] findingMessages;
                
                if (PRINT_FINDINGS == FindingType.ERROR) {
                	findingMessages = findings.getValidationMessages(PRINT_FINDINGS, FindingMessageFormat.DEFAULT);
                } else {
                	findingMessages = findings.getAllValidationMessages(FindingMessageFormat.DEFAULT);
                }
                
                if (findingMessages.length > 0) {
                    System.out.println("Validation Findings:");
                	
                    for (String message : findingMessages) {
                        System.out.println("  " + message);
                    }
                }
            }
        }
    }

}
