/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html;

import java.io.File;

/**
 * @author Eric.Bronson
 *
 */
public abstract class TestUtils {

	/**
	 * 
	 */
	private TestUtils() {
	}

	/**
	 * Delete all files starting from the given directory. Then delete the
	 * directory.
	 * 
	 * @param dir
	 * @return
	 */
	public static void cleanDirectory(File dir) {
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				cleanDirectory(f);
			}
		}
		dir.delete();
	}
}
