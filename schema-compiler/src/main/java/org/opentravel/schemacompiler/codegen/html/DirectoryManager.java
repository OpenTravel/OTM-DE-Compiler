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
/*
 * Copyright (c) 1998, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.opentravel.schemacompiler.codegen.html;

import java.io.File;


/**
 * Handle the directory creations and the path string generations.
 * All static - never instantiated.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 *
 * @since 1.2
 * @author Atul M Dambalkar
 */
public abstract class DirectoryManager {

    /**
     * The file separator string, "/", used in the formation of the URL path.
     */
    public static final String URL_FILE_SEPARATOR = "/";

    /**
     * Never instaniated.
     */
    private DirectoryManager() {
    }

    /**
     * Given a PackageDoc, return its URL path string.
     *
     * @param pd PackageDoc
     * @see #getPath(String)
     */
    public static String createPathString(String namespace) {
        if (namespace == null) {
            return "";
        }
        return getPath(namespace);
    }

    /**
     * Given a package name, return the corresponding directory name
     * with the platform-dependent file separator between subdirectory names.
     * For EXAMPLE, if name of the package is "java.lang" , then it
     * returns "java/lang" on Unix and "java\lang" on Windows.
     * If name of the package contains no dot, then the value
     * will be returned unchanged.  Because package names cannot
     * end in a dot, the return value will never end with a slash.
     * <p>
     * Also see getPath for the URL separator version of this method
     * that takes a string instead of a PackageDoc.
     *
     * @param  namespace    the name of the package
     * @return       the platform-dependent directory path for the package
     */
    public static String getDirectoryPath(String namespace) {
        if (namespace == null || namespace.length() == 0) {
            return "";
        }
        StringBuilder pathstr = new StringBuilder();
        String path = namespace;
        
        for (int i = 0; i < path.length(); i++) {
            char ch = path.charAt(i);
            if (ch == '/' || ch == '.') {
                pathstr.append(URL_FILE_SEPARATOR);
            } else {
                pathstr.append(ch);
            }
        }
        if (pathstr.length() > 0 && ! pathstr.toString().endsWith(URL_FILE_SEPARATOR)) {
            pathstr.append(URL_FILE_SEPARATOR);
        }
        return pathstr.toString();
    }

    /**
     * Given a package name (a string), return the path string,
     * with the URL separator "/" separating the subdirectory names.
     * If name of the package contains no dot, then the value
     * will be returned unchanged.  Because package names cannot
     * end in a dot, the return value will never end with a slash.
     * <p>
     * For EXAMPLE if the string is "com.sun.javadoc" then the URL
     * path string will be "com/sun/javadoc".
     *
     * @param name   the package name as a String
     * @return       the String URL path
     */
    public static String getPath(String name) {
        if (name == null || name.length() == 0) {
            return "";
        }
        StringBuilder pathstr = new StringBuilder();
        String path = name;
        
        for (int i = 0; i < path.length(); i++) {
            char ch = path.charAt(i);
            if (ch == '/' || ch == '.') {
                pathstr.append(URL_FILE_SEPARATOR);
            } else {
                pathstr.append(ch);
            }
        }
        return pathstr.toString();
    }

    /**
     * Given two package names as strings, return the relative path
     * from the package directory corresponding to the first string
     * to the package directory corresponding to the second string,
     * with the URL file separator "/" separating subdirectory names.
     * <p>
     * For EXAMPLE, if the parameter "from" is "java.lang"
     * and parameter "to" is "java.applet", return string
     * "../../java/applet".
     *
     * @param from   the package name from which path is calculated
     * @param to     the package name to which path is calculated
     * @return       relative path between "from" and "to" with URL
     *               separators
     * @see          #getRelativePath(String)
     * @see          #getPath(String)
     */
    public static String getRelativePath(String from, String to) {
    	StringBuilder pathstr = new StringBuilder();
    	
        pathstr.append(getRelativePath(from));
        pathstr.append(getPath(to));
        pathstr.append(URL_FILE_SEPARATOR);
        return pathstr.toString();
    }


    /**
     * Given a package name as a string, return relative path string
     * from the corresponding package directory to the root of
     * the documentation, using the URL separator "/" between
     * subdirectory names.
     * <p>
     * For EXAMPLE, if the string "from" is "java.lang",
     * return "../../"
     *
     * @param from    the package name
     * @return        String relative path from "from".
     * @see           #getRelativePath(String, String)
     */
    public static String getRelativePath(String from) {
        if (from == null || from.length() == 0) {
            return "";
        }
        StringBuilder pathstr = new StringBuilder();
        String path = from;
        
        for (int i = 0; i < path.length(); i++) {
            char ch = path.charAt(i);
            if (ch == '/' || ch == '.') {
                pathstr.append(".." + URL_FILE_SEPARATOR);
            }
        }
        pathstr.append(".." + URL_FILE_SEPARATOR);
        return pathstr.toString();
    }

    /**
     * Given a relative or absolute path that might be empty,
     * convert it to a path that does not end with a
     * URL separator "/".  Used for converting
     * HtmlStandardWriter.relativepath when replacing {@docRoot}.
     *
     * @param path   the path to convert.  An empty path represents
     *               the current directory.
     */
    public static String getPathNoTrailingSlash(String path) {
        if ( path.equals("") ) {
            return ".";
        }
        if ( path.equals("/") ) {
            return "/.";
        }
        if ( path.endsWith("/") ) {
            // Remove trailing slash
            path = path.substring(0, path.length() -1);
        }
        return path;
    }

    /**
     * Given a path string create all the directories in the path. For EXAMPLE,
     * if the path string is "java/applet", the method will create directory
     * "java" and then "java/applet" if they don't exist. The file separator
     * string "/" is platform dependent system property.
     *
     * @param path Directory path string.
     */
    public static void createDirectory(Configuration configuration,
                                       String path) {
        if (path == null || path.length() == 0) {
            return;
        }
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            configuration.message.error("doclet.Unable_to_create_directory_0", path);
             throw new DocletAbortException();
        }
    }

    /**
     * Given a package name and a file name, return the full path to that file.
     * For EXAMPLE, if PackageDoc passed is for "java.lang" and the filename
     * passed is "package-summary.html", then the string returned is
     * "java/lang/package-summary.html".
     *
     * @param pd         PackageDoc.
     * @param filename   File name to be appended to the path of the package.
     */
    public static String getPathToLibrary(String pd, String filename) {
    	StringBuilder buf = new StringBuilder();
        String pathstr = createPathString(pd);
        
        if (pathstr.length() > 0) {
            buf.append(pathstr);
            buf.append(URL_FILE_SEPARATOR);
        }
        buf.append(filename);
        return buf.toString();
    }


}
