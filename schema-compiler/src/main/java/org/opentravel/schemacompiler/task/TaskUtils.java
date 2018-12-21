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
package org.opentravel.schemacompiler.task;

import java.io.File;

/**
 * Static utility methods required by the task subsystem.
 * 
 * @author S. Livezey
 */
public class TaskUtils {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private TaskUtils() {}
	
    /**
     * Returns the file represented by the given option value, resolving relative paths from the
     * user's current working directory.
     * 
     * @param optionValue
     *            the option value to resolve
     * @return File
     */
    public static File getPathFromOptionValue(String optionValue) {
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
        boolean isAbsolutePath;
        File argFile;

        if (isWindows) {
            isAbsolutePath = optionValue.contains(":");
        } else {
            isAbsolutePath = optionValue.startsWith("/");
        }
        if (isAbsolutePath) {
            argFile = new File(optionValue);
        } else {
            argFile = new File(System.getProperty("user.dir"), optionValue);
        }
        return argFile;
    }

}
