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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the resolution of file hints for OTM import declarations. In some cases, the file paths
 * contain white-space characters. This class attempts to resolve the naming conflicts to return a
 * list of valid file-hint paths.
 * 
 * @author S. Livezey
 */
public class FileHintUtils {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private FileHintUtils() {}
	
    /**
     * Attempts to resolve each of the given file hints to resources on the local file system. If
     * any of the hints represent files that do not exist, they are combined with adjacent hints in
     * an attempt to resolve white-space character issues.
     * 
     * @param hints
     *            the space-separated list of file hints to resolve
     * @param libraryUrl
     *            the URL of the owning library
     * @return List<String>
     */
    public static List<String> resolveHints(String hints, URL libraryUrl) {
        return (hints == null) ? new ArrayList<>() : resolveHints(hints.trim().split("\\s+"),
                libraryUrl);
    }

    /**
     * Attempts to resolve each of the given file hints to resources on the local file system. If
     * any of the hints represent files that do not exist, they are combined with adjacent hints in
     * an attempt to resolve white-space character issues.
     * 
     * @param hints
     *            the array of file hints to resolve
     * @param libraryUrl
     *            the URL of the owning library
     * @return List<String>
     */
    public static List<String> resolveHints(String[] hints, URL libraryUrl) {
        return (hints == null) ? new ArrayList<>() : resolveHints(Arrays.asList(hints),
                libraryUrl);
    }

    /**
     * Attempts to resolve each of the given file hints to resources on the local file system. If
     * any of the hints represent files that do not exist, they are combined with adjacent hints in
     * an attempt to resolve white-space character issues.
     * 
     * @param hints
     *            the list of file hints to resolve
     * @param libraryUrl
     *            the URL of the owning library
     * @return List<String>
     */
    public static List<String> resolveHints(List<String> hints, URL libraryUrl) {
        List<String> resolvedHints = new ArrayList<>();

        if ((libraryUrl == null) || !URLUtils.isFileURL(libraryUrl)) {
            resolvedHints.addAll(hints);

        } else {
            List<String> badHints = new ArrayList<>();

            for (String hint : hints) {
                resolveFileHint( hint, libraryUrl, resolvedHints, badHints );
            }
            resolvedHints.addAll(badHints);
        }
        return resolvedHints;
    }

	/**
	 * Resolves the given file hint.
	 * 
	 * @param hint  the file hint to be resolved
	 * @param libraryUrl  the library URL from which the hint's relative path will be resolved
	 * @param resolvedHints  the list of hints resolved so far
	 * @param badHints  the list of hints that could not be resolved
	 */
	private static void resolveFileHint(String hint, URL libraryUrl, List<String> resolvedHints,
			List<String> badHints) {
		try {
		    File hintFile = URLUtils.toFile(URLUtils.getResolvedURL(hint, libraryUrl));

		    if (hintFile.exists() || (badHints.isEmpty() && hasFileExtension(hint))) {
		        resolvedHints.addAll(badHints);
		        resolvedHints.add(hint);
		        badHints.clear();

		    } else if (!badHints.isEmpty()) {
		        StringBuilder sbCompoundHint = new StringBuilder();

		        for (String badHint : badHints) {
		            sbCompoundHint.append(badHint).append(" ");
		        }
		        sbCompoundHint.append(hint);

		        String compoundHint = sbCompoundHint.toString();
		        hintFile = URLUtils.toFile(URLUtils
		                .getResolvedURL(compoundHint, libraryUrl));

		        if (hintFile.exists() || hasFileExtension(compoundHint)) {
		            resolvedHints.add(compoundHint);
		            badHints.clear();

		        } else {
		            badHints.add(hint);
		        }
		    } else {
		        badHints.add(hint);
		    }

		} catch (Exception e) {
		    // Ignore and just return the hint as-is
		    resolvedHints.addAll(badHints);
		    resolvedHints.add(hint);
		    badHints.clear();
		}
	}

    /**
     * Returns true if the given file hint has a file extension component.
     * 
     * @param hint
     *            the file hint string to analyze
     * @return boolean
     */
    private static boolean hasFileExtension(String hint) {
        boolean result = false;

        if (hint != null) {
            int dotIdx = hint.lastIndexOf('.');

            if (dotIdx >= 0) {
                String fileExt = hint.substring(dotIdx + 1);

                result = (fileExt.indexOf('/') < 0);
            }
        }
        return result;
    }

}
