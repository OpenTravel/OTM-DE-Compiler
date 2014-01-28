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
        return (hints == null) ? new ArrayList<String>() : resolveHints(hints.trim().split("\\s+"),
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
        return (hints == null) ? new ArrayList<String>() : resolveHints(Arrays.asList(hints),
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
        List<String> resolvedHints = new ArrayList<String>();

        if ((libraryUrl == null) || !URLUtils.isFileURL(libraryUrl)) {
            resolvedHints.addAll(hints);

        } else {
            List<String> badHints = new ArrayList<String>();

            for (String hint : hints) {
                try {
                    File hintFile = URLUtils.toFile(URLUtils.getResolvedURL(hint, libraryUrl));

                    if (hintFile.exists() || ((badHints.size() == 0) && hasFileExtension(hint))) {
                        resolvedHints.addAll(badHints);
                        resolvedHints.add(hint);
                        badHints.clear();

                    } else if (badHints.size() > 0) {
                        StringBuilder _compoundHint = new StringBuilder();

                        for (String badHint : badHints) {
                            _compoundHint.append(badHint).append(" ");
                        }
                        _compoundHint.append(hint);

                        String compoundHint = _compoundHint.toString();
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

                } catch (Throwable t) {
                    // Ignore and just return the hint as-is
                    resolvedHints.addAll(badHints);
                    resolvedHints.add(hint);
                    badHints.clear();
                }
            }
            resolvedHints.addAll(badHints);
        }
        return resolvedHints;
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
