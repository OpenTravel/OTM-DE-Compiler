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
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;

/**
 * The class with "start" method, calls individual Writers.
 *
 * @author Atul M Dambalkar
 * @author Robert Field
 * @author Jamie Ho
 * @author eric.bronson(modified for otm)
 *
 */
public class HtmlDoclet extends AbstractDoclet {
	
	public static final String DEFAULT_STYLESHEET = "stylesheet.css";
	
	public static final String TOGGLE_CLOSE_IMAGE = "expand_normal_closed_16x16.png";
	
	public static final String TOGGLE_OPEN_IMAGE = "expand_normal_open_16x16.png";
	
	public HtmlDoclet() {
		setConfiguration( newConfiguration() );
	}
	
	/**
	 * The "start" method as required by Javadoc.
	 *
	 * @param manager
	 *            the root of the documentation tree.
	 * @see com.sun.javadoc.RootDoc
	 * @return true if the doclet ran without encountering any errors.
	 */
	public static boolean start(TLModel model) {
		try {
			HtmlDoclet doclet = new HtmlDoclet();
			return doclet.start(doclet, model);
		} finally {
			Configuration.reset();
		}
	}

	/**
	 * Create the configuration instance. Override this method to use a
	 * different configuration.
	 */
	public Configuration newConfiguration() {
		return Configuration.getInstance();
	}

	/**
	 * Start the generation of files. Call generate methods in the individual
	 * writers, which will in turn genrate the documentation files. Call the
	 * TreeWriter generation first to ensure the Class Hierarchy is built first
	 * and then can be used in the later generation.
	 *
	 * For new format.
	 *
	 * @see com.sun.javadoc.RootDoc
	 */
	protected void generateOtherFiles(TLModel model) throws Exception {
		Configuration conf = getConfiguration();
		
		if (conf.getTopFile().length() == 0) {
			conf.message
					.error("doclet.No_Non_Deprecated_Classes_To_Document");
			return;
		}
		String configdestdir = conf.getDestDirName();
		String configstylefile = conf.getStylesheetfile();
		performCopy(configdestdir, configstylefile);
		Util.copyResourceFile(conf, TOGGLE_CLOSE_IMAGE, false);
		Util.copyResourceFile(conf, TOGGLE_OPEN_IMAGE, false);
		Util.copyResourceFile(conf, "background.gif", false);
		Util.copyResourceFile(conf, "tab.gif", false);
		Util.copyResourceFile(conf, "titlebar.gif", false);
		Util.copyResourceFile(conf, "titlebar_end.gif", false);
		
		AllObjectsFrameWriter.generate(conf, new IndexBuilder(
				conf, false, true));

		FrameOutputWriter.generate(conf);

		if (conf.isCreateoverview()) {
			LibraryIndexWriter.generate(conf);
		}
		// If a stylesheet file is not specified, copy the default stylesheet
		// and replace newline with platform-specific newline.
		if (conf.getStylesheetfile().length() == 0) {
			if(configdestdir.isEmpty()){
				throw new RuntimeException("Style sheet output Directory not specified");
			}
			Util.copyFile(conf, DEFAULT_STYLESHEET, Util.RESOURCESDIR,
					configdestdir, false, true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void generateLibraryFiles(TLModel model) throws Exception {
		List<TLLibrary> libraries = model.getUserDefinedLibraries();
		Configuration conf = getConfiguration();
		
		if (libraries.size() > 1) {
			LibraryIndexFrameWriter.generate(conf);
		}
		TLLibrary prev;
		TLLibrary next;
		ListIterator<TLLibrary> libIter = libraries.listIterator();
		
		while (libIter.hasNext()) {
			prev = libIter.hasPrevious() ? libraries.get(libIter.previousIndex()) : null;
			TLLibrary lib = libIter.next();
			next = libIter.hasNext() ? libraries.get(libIter.nextIndex()) : null;
			LibraryFrameWriter.generate(conf, lib);
			DocumentationBuilder libraryBuilder = conf
					.getBuilderFactory().getLibraryDocumentationBuilder(lib, prev, next);
			libraryBuilder.build();
		}

	}

	private void performCopy(String configdestdir, String filename) {
		Configuration conf = getConfiguration();
		
		try {
			String destdir = (configdestdir.length() > 0) ? configdestdir
					+ File.separatorChar : "";
			if (filename.length() > 0) {
				File helpstylefile = new File(filename);
				String parent = helpstylefile.getParent();
				String helpstylefilename = (parent == null) ? filename
						: filename.substring(parent.length() + 1);
				File desthelpfile = new File(destdir + helpstylefilename);
				if (!desthelpfile.getCanonicalPath().equals(
						helpstylefile.getCanonicalPath())) {
					conf.message.notice((SourcePosition) null,
							"doclet.Copying_File_0_To_File_1",
							helpstylefile.toString(), desthelpfile.toString());
					Util.copyFile(desthelpfile, helpstylefile);
				}
			}
		} catch (IOException exc) {
			conf.message.error((SourcePosition) null,
					"doclet.perform_copy_exception_encountered",
					exc.toString());
			throw new DocletAbortException();
		}
	}
}
