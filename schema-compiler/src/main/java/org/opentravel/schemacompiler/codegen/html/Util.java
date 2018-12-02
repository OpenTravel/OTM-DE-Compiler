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
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ResourceBundle;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;

/**
 * Utilities Class for Documentation. Modified from the original JavaDoc Util
 * class.
 *
 * @author Atul M Dambalkar
 * @author Jamie Ho
 * @author Eric.Bronson (modified for OTM)
 */
public class Util {
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private Util() {}
	
	/**
	 * A mapping between characters and their corresponding HTML escape
	 * character.
	 */
	protected static final String[][] HTML_ESCAPE_CHARS = { { "&", "&amp;" },
			{ "<", "&lt;" }, { ">", "&gt;" } };

	/**
	 * Name of the destination resource directory.
	 */
	public static final String RESOURCESDIR = "resources";
	
	/**
	 * Name of the source resource directory.
	 */
	public static final String SOURCE_RESOURCESDIR = "org/opentravel/schemacompiler/codegen/html/" + RESOURCESDIR;
	

	/**
	 * Resource bundle corresponding to the doclets.properties file.
	 */
	public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(Configuration.DOCLETS_RESOURCE);

	/**
	 * Copy source file to destination file.
	 *
	 * @throws SecurityException
	 * @throws IOException
	 */
	public static void copyFile(File destfile, File srcfile) throws IOException {
		byte[] bytearr = new byte[512];
		int len = 0;

		try (FileInputStream input = new FileInputStream(srcfile)) {
			File destDir = destfile.getParentFile();
			destDir.mkdirs();

			try (FileOutputStream output = new FileOutputStream(destfile)) {
				while ((len = input.read(bytearr)) != -1) {
					output.write(bytearr, 0, len);
				}
			}
		}
	}

	/**
	 * Copy a file in the resources directory to the destination directory (if
	 * it is not there already). If <code>overwrite</code> is true and the
	 * destination file already exists, overwrite it.
	 *
	 * @param configuration
	 *            Holds the destination directory and error message
	 * @param resourcefile
	 *            The name of the resource file to copy
	 * @param overwrite
	 *            A flag to indicate whether the file in the destination
	 *            directory will be overwritten if it already exists.
	 */
	public static void copyResourceFile(Configuration configuration,
			String resourcefile, boolean overwrite) {
		String destresourcesdir = configuration.destDirName + RESOURCESDIR;
		copyFile(configuration, resourcefile, RESOURCESDIR, destresourcesdir,
				overwrite, false);
	}

	/**
	 * Copy a file from a source directory to a destination directory (if it is
	 * not there already). If <code>overwrite</code> is true and the destination
	 * file already exists, overwrite it.
	 *
	 * @param configuration
	 *            Holds the error message
	 * @param file
	 *            The name of the file to copy
	 * @param source
	 *            The source directory
	 * @param destination
	 *            The destination directory where the file needs to be copied
	 * @param overwrite
	 *            A flag to indicate whether the file in the destination
	 *            directory will be overwritten if it already exists.
	 * @param replaceNewLine
	 *            true if the newline needs to be replaced with platform-
	 *            specific newline.
	 */
	public static void copyFile(Configuration configuration, String file,
			String source, String destination, boolean overwrite,
			boolean replaceNewLine) {
		DirectoryManager.createDirectory(configuration, destination);
		File destfile = new File(destination, file);
		if (destfile.exists() && (!overwrite)) {
			return;
		}
		try (InputStream in = Configuration.class.getResourceAsStream(source
				+ DirectoryManager.URL_FILE_SEPARATOR + file)) {
			if (in == null) {
				return;
			}

			try (OutputStream out = new FileOutputStream(destfile)) {
				if (!replaceNewLine) {
					byte[] buf = new byte[2048];
					int n;
					while ((n = in.read(buf)) > 0)
						out.write(buf, 0, n);
				} else {
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
						try (BufferedWriter writer = newWriter(out, configuration)) {
							String line;
							
							while ((line = reader.readLine()) != null) {
								writer.write(line);
								writer.write(DocletConstants.NL);
							}
						}
					}
				}
			}
		} catch (IOException ie) {
			throw new DocletAbortException();
		}
	}
	
	/**
	 * Constructs a new writer for the given output stream using the proper
	 * encoding configuration.
	 * 
	 * @param out  the output stream for which to construct a writer
	 * @param configuration  the configuration to use for writer encoding
	 * @return BufferedWriter
	 * @throws IOException  thrown if the writer cannot be created
	 */
	private static BufferedWriter newWriter(OutputStream out, Configuration configuration) throws IOException {
		BufferedWriter writer = null;
		
		if (configuration.docencoding == null) {
			writer = new BufferedWriter(new OutputStreamWriter(out));
		} else {
			writer = new BufferedWriter(new OutputStreamWriter(out, configuration.docencoding));
		}
		return writer;
	}

	/**
	 * Enclose in quotes, used for paths and filenames that contains spaces
	 */
	public static String quote(String filepath) {
		return ("\"" + filepath + "\"");
	}

	/**
	 * Given a string, replace all occurraces of 'newStr' with 'oldStr'.
	 * 
	 * @param originalStr
	 *            the string to modify.
	 * @param oldStr
	 *            the string to replace.
	 * @param newStr
	 *            the string to insert in place of the old string.
	 */
	public static String replaceText(String originalStr, String oldStr,
			String newStr) {
		if (oldStr == null || newStr == null || oldStr.equals(newStr)) {
			return originalStr;
		}
		return originalStr.replace(oldStr, newStr);
	}

	/**
	 * Given a string, escape all special html characters and return the result.
	 *
	 * @param s
	 *            The string to check.
	 * @return the original string with all of the HTML characters escaped.
	 *
	 * @see #HTML_ESCAPE_CHARS
	 */
	public static String escapeHtmlChars(String s) {
		String result = s;
		for (int i = 0; i < HTML_ESCAPE_CHARS.length; i++) {
			result = Util.replaceText(result, HTML_ESCAPE_CHARS[i][0],
					HTML_ESCAPE_CHARS[i][1]);
		}
		return result;
	}

	/**
	 * Given a string, strips all html characters and return the result.
	 *
	 * @param rawString
	 *            The string to check.
	 * @return the original string with all of the HTML characters stripped.
	 *
	 */
	public static String stripHtml(String rawString) {
		// remove HTML tags
		rawString = rawString.replaceAll("\\<.*?>", " ");
		// consolidate multiple spaces between a word to a single space
		rawString = rawString.replaceAll("\\b\\s{2,}\\b", " ");
		// remove extra whitespaces
		return rawString.trim();
	}

	/**
	 * Create the directory path for the file to be generated, construct
	 * FileOutputStream and OutputStreamWriter depending upon docencoding.
	 *
	 * @param path
	 *            The directory path to be created for this file.
	 * @param filename
	 *            File Name to which the PrintWriter will do the Output.
	 * @param docencoding
	 *            Encoding to be used for this file.
	 * @exception IOException
	 *                Exception raised by the FileWriter is passed on to next
	 *                level.
	 * @return Writer Writer for the file getting generated.
	 * @see java.io.FileOutputStream
	 * @see java.io.OutputStreamWriter
	 */
	public static Writer genWriter(Configuration configuration, String path,
			String filename, String docencoding) throws IOException {
		FileOutputStream fos;
		if (path != null) {
			DirectoryManager.createDirectory(configuration, path);
			fos = new FileOutputStream(((path.length() > 0) ? path
					+ File.separator : "")
					+ filename);
		} else {
			fos = new FileOutputStream(filename);
		}
		if (docencoding == null) {
			return new OutputStreamWriter(fos);
		} else {
			try (OutputStreamWriter os = new OutputStreamWriter(fos,
					docencoding)) {
				return os;
			} catch (IOException ex) {
				throw new IOException(ex);
			}

		}
	}

	/**
	 * Return true if this class is linkable and false if we can't link to the
	 * desired class. <br>
	 * <b>NOTE:</b> You can only link to external classes if they are public or
	 * protected.
	 *
	 * @param classDoc
	 *            the class to check.
	 * @param configuration
	 *            the current configuration of the doclet.
	 *
	 * @return true if this class is linkable and false if we can't link to the
	 *         desired class.
	 */
	public static boolean isLinkable(DocumentationBuilder builder,
			Configuration configuration) {
		return configuration.isGeneratedDoc(builder);
	}

}
