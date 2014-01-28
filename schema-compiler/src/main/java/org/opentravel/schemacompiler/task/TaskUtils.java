
package org.opentravel.schemacompiler.task;

import java.io.File;

/**
 * Static utility methods required by the task subsystem.
 * 
 * @author S. Livezey
 */
public class TaskUtils {
	
	/**
	 * Returns the file represented by the given option value, resolving relative paths from the
	 * user's current working directory.
	 * 
	 * @param optionValue  the option value to resolve
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
