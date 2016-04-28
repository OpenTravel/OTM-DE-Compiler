package org.opentravel.schemacompiler.codegen.html;

import java.util.*;
import java.io.*;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.codegen.html.DocletAbortException;
import org.opentravel.schemacompiler.codegen.html.IndexBuilder;
import org.opentravel.schemacompiler.codegen.html.Util;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;

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
		configuration = configuration();
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
	public Configuration configuration() {
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
		if (configuration.topFile.length() == 0) {
			configuration.message
					.error("doclet.No_Non_Deprecated_Classes_To_Document");
			return;
		}
		String configdestdir = configuration.destDirName;
		String configstylefile = configuration.stylesheetfile;
		performCopy(configdestdir, configstylefile);
		Util.copyResourceFile(configuration, "expand_normal_closed_16x16.png", false);
		Util.copyResourceFile(configuration, "expand_normal_open_16x16.png", false);
		
		AllObjectsFrameWriter.generate(configuration, new IndexBuilder(
				configuration, false, true));

		FrameOutputWriter.generate(configuration);

		if (configuration.createoverview) {
			LibraryIndexWriter.generate(configuration);
		}
		// If a stylesheet file is not specified, copy the default stylesheet
		// and replace newline with platform-specific newline.
		if (configuration.stylesheetfile.length() == 0) {
			if(configdestdir.isEmpty()){
				throw new RuntimeException("Style sheet output Directory not specified");
			}
			Util.copyFile(configuration, DEFAULT_STYLESHEET, Util.RESOURCESDIR,
					configdestdir, false, true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void generateLibraryFiles(TLModel model) throws Exception {
		List<TLLibrary> libraries = model.getUserDefinedLibraries();
		if (libraries.size() > 1) {
			LibraryIndexFrameWriter.generate(configuration);
		}
		TLLibrary prev, next;
		ListIterator<TLLibrary> libIter = libraries.listIterator();
		while (libIter.hasNext()) {
			prev = libIter.hasPrevious() ? libraries.get(libIter.previousIndex()) : null;
			TLLibrary lib = libIter.next();
			next = libIter.hasNext() ? libraries.get(libIter.nextIndex()) : null;
			LibraryFrameWriter.generate(configuration, lib);
			DocumentationBuilder libraryBuilder = configuration
					.getBuilderFactory().getLibraryDocumentationBuilder(lib, prev,
							next);
			libraryBuilder.build();
		}

	}

	private void performCopy(String configdestdir, String filename) {
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
					configuration.message.notice((SourcePosition) null,
							"doclet.Copying_File_0_To_File_1",
							helpstylefile.toString(), desthelpfile.toString());
					Util.copyFile(desthelpfile, helpstylefile);
				}
			}
		} catch (IOException exc) {
			configuration.message
					.error((SourcePosition) null,
							"doclet.perform_copy_exception_encountered",
							exc.toString());
			throw new DocletAbortException();
		}
	}
}
