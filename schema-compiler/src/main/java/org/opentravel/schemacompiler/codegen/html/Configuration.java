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

import java.util.List;
import java.util.Locale;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilderFactory;
import org.opentravel.schemacompiler.codegen.html.MessageRetriever;
import org.opentravel.schemacompiler.codegen.html.Messager;

/**
 * Configure the output based on the command line options.
 * <p>
 * Also determine the length of the command line option. For example, for a
 * option "-header" there will be a string argument associated, then the the
 * length of option "-header" is two. But for option "-nohelp" no argument is
 * needed so it's length is 1.
 * </p>
 * <p>
 * Also do the error checking on the options used. For example it is illegal to
 * use "-helpfile" option when already "-nohelp" option is used.
 * </p>
 *
 * @author Robert Field.
 * @author Atul Dambalkar.
 * @author Jamie Ho
 * @author Bhavesh Patel (Modified)
 */
public class Configuration {
	

	/**
	 * Location of doclet properties file.
	 */
	public static final String DOCLETS_RESOURCE = "org.opentravel.schemacompiler.codegen.html.resources.doclets";
	
	/**
	 * The namespace for XSD schemas.
	 */
	private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

	private static Configuration instance = new Configuration();

	/**
	 * The build date. Note: For now, we will use a version number instead of a
	 * date.
	 */
	public static final String BUILD_DATE = System.getProperty("java.version");
	
	/**
	 * The build date. Note: For now, we will use a version number instead of a
	 * date.
	 */
	public static final String VERSION = "3.0";

	/**
	 * The name of the constant values file.
	 */
	public static final String CONSTANTS_FILE_NAME = "constant-values.html";

	/**
	 * Argument for command line option "-doctitle".
	 */
	public String doctitle = "";

	/**
	 * Argument for command line option "-windowtitle".
	 */
	public String windowtitle = "";

	/**
	 * Argument for command line option "-stylesheetfile".
	 */
	public String stylesheetfile = "";

	/**
	 * This is true if option "-overview" is used or option "-overview" is not
	 * used and number of packages is more than one.
	 */
	public boolean createoverview = true;

	/**
	 * Unique Resource Handler for this package.
	 */
	public final MessageRetriever message;

	/**
	 * First file to appear in the right-hand frame in the generated
	 * documentation.
	 */
	public String topFile = "";

	/**
	 * The classdoc for the class file getting generated.
	 */
	public DocumentationBuilder currentMember = null;

	/**
	 * The Root of the generated Program Structure from the Doclet API.
	 */
	public TLModel model;

	/**
	 * Destination directory name, in which doclet will generate the entire
	 * documentation. Default is current directory.
	 */
	public String destDirName = "";

	/**
	 * Encoding for this document. Default is default encoding for this
	 * platform.
	 */
	public String docencoding = null;

	/**
	 * True if user wants to add member names as meta keywords. Set to false
	 * because meta keywords are ignored in general by most Internet search
	 * engines.
	 */
	public boolean keywords = false;

	public Messager messager = new Messager("OTM Documentation");
	
	/**
     * True if command line option "-nohelp" is used. Default value is false.
     */
    public boolean nohelp = true;
    
    /**
     * False if command line option "-noindex" is used. Default value is true.
     */
    public boolean createindex = false;

    /**
     * True if command line option "-use" is used. Default value is false.
     */
    public boolean classuse = false;

    /**
     * False if command line option "-notree" is used. Default value is true.
     */
    public boolean createtree = false;
    
    /**
     * True if command line option "-splitindex" is used. Default value is
     * false.
     */
    public boolean splitindex = false;
    
    /**
     * Argument for command line option "-helpfile".
     */
    public String helpfile = "";

	/**
	 * Set this to true if you would like to not emit any errors, warnings and
	 * notices.
	 */
	private boolean silent = false;

	private DocumentationBuilderFactory builderFactory;


	private boolean isGenerateBuiltins = false;

	/**
	 * Constructor. Initializes resource for the
	 * {@link com.sun.tools.doclets.MessageRetriever}.
	 */
	private Configuration() {
		message = new MessageRetriever(this, DOCLETS_RESOURCE);
		topFile = "overview-summary.html";
	}

	/**
	 * Reset to a fresh new Configuration, to allow multiple invocations of
	 * javadoc within a single VM. It would be better not to be using static
	 * fields at all, but .... (sigh).
	 */
	public static void reset() {
		instance = new Configuration();
	}

	public static Configuration getInstance() {
		return instance;
	}

	/**
	 * Return the build date for the doclet.
	 */
	public String getDocletSpecificBuildDate() {
		return BUILD_DATE;
	}

	/**
	 * Return the qualified name of the <code>ClassDoc</code> if it's qualifier
	 * is not excluded. Otherwise, return the unqualified <code>ClassDoc</code>
	 * name.
	 * 
	 * @param cd
	 *            the <code>ClassDoc</code> to check.
	 */
	public String getQualifiedName(DocumentationBuilder member) {
		String namespace = member.getNamespace();
		String localName = member.getName();
		return null == namespace ? localName : namespace + ":" + localName;
	}

	/**
	 * Return the qualified name of the <code>ClassDoc</code> if it's qualifier
	 * is not excluded. Otherwise, return the unqualified <code>ClassDoc</code>
	 * name.
	 * 
	 * @param cd
	 *            the <code>ClassDoc</code> to check.
	 */
	public String getQualifiedName(LibraryMember member) {
		String namespace = member.getNamespace();
		String localName = member.getLocalName();
		return null == namespace ? localName : namespace + ":" + localName;
	}


	public String getText(String key) {
		try {
			// Check the doclet specific properties file.
			return getDocletSpecificMsg().getText(key);
		} catch (Exception e) {
			// Check the shared properties file.
			return message.getText(key);
		}
	}

	public String getText(String key, String a1) {
		try {
			// Check the doclet specific properties file.
			return getDocletSpecificMsg().getText(key, a1);
		} catch (Exception e) {
			// Check the shared properties file.
			return message.getText(key, a1);
		}
	}

	public String getText(String key, String a1, String a2) {
		try {
			// Check the doclet specific properties file.
			return getDocletSpecificMsg().getText(key, a1, a2);
		} catch (Exception e) {
			// Check the shared properties file.
			return message.getText(key, a1, a2);
		}
	}

	public Locale getLocale() {
		return Locale.getDefault();
	}

	
	/**
	 * {@inheritDoc}
	 */
	public MessageRetriever getDocletSpecificMsg() {
		return message;
	}

	/**
	 * Return true if the ClassDoc element is getting documented, depending upon
	 * -nodeprecated option and the deprecation information. Return true if
	 * -nodeprecated is not used. Return false if -nodeprecated is used and if
	 * either ClassDoc element is deprecated or the containing package is
	 * deprecated.
	 *
	 * @param builder
	 *            the ClassDoc for which the page generation is checked
	 */
	public boolean isGeneratedDoc(DocumentationBuilder builder) {
		boolean isGenerated = true;
		String namespace = builder.getNamespace();
		if(XSD_NAMESPACE.equals(namespace)){
			isGenerated = false;
		}else if(!isGenerateBuiltins){
			List<AbstractLibrary> libs = model.getLibrariesForNamespace(namespace);
			//only need one library to determine if its a builtin
			// is this faster than traversing getBuiltinLibraries?
			if(libs.size() > 0 && libs.get(0) instanceof BuiltInLibrary){
				isGenerated = false;
			}
		}
		
		return isGenerated;
	}
	
	/**
	 * Return true if the ClassDoc element is getting documented, depending upon
	 * -nodeprecated option and the deprecation information. Return true if
	 * -nodeprecated is not used. Return false if -nodeprecated is used and if
	 * either ClassDoc element is deprecated or the containing package is
	 * deprecated.
	 *
	 * @param builder
	 *            the ClassDoc for which the page generation is checked
	 */
	public boolean isGeneratedDoc(LibraryMember member) {
		return member != null;
	}

	public List<TLLibrary> getLibraries() {
		return model.getUserDefinedLibraries();
	}

	/**
	 * Print error message, increment error count.
	 *
	 * @param msg
	 *            message to print.
	 */
	public void printError(String msg) {
		if (silent)
			return;
		messager.printError(msg);
	}

	/**
	 * Print error message, increment error count.
	 *
	 * @param msg
	 *            message to print.
	 */
	public void printError(SourcePosition pos, String msg) {
		if (silent)
			return;
		messager.printError(pos, msg);
	}

	/**
	 * Print warning message, increment warning count.
	 *
	 * @param msg
	 *            message to print.
	 */
	public void printWarning(String msg) {
		if (silent)
			return;
		messager.printWarning(msg);
	}

	/**
	 * Print warning message, increment warning count.
	 *
	 * @param msg
	 *            message to print.
	 */
	public void printWarning(SourcePosition pos, String msg) {
		if (silent)
			return;
		messager.printWarning(pos, msg);
	}

	/**
	 * Print a message.
	 *
	 * @param msg
	 *            message to print.
	 */
	public void printNotice(String msg) {
		if (silent)
			return;
		messager.printNotice(msg);
	}

	/**
	 * Print a message.
	 *
	 * @param msg
	 *            message to print.
	 */
	public void printNotice(SourcePosition pos, String msg) {
		if (silent)
			return;
		messager.printNotice(pos, msg);
	}

	/**
	 * Return the builder factory for this doclet.
	 *
	 * @return the builder factory for this doclet.
	 */
	public DocumentationBuilderFactory getBuilderFactory() {
		if (builderFactory == null) {
			builderFactory = DocumentationBuilderFactory.getInstance();
		}
		return builderFactory;
	}

	public void setModel(TLModel model) {
//		List<String> ns = new ArrayList<String>();
//		for (TLLibrary lib : model.getUserDefinedLibraries()) {
//			ns.add(lib.getNamespace());
//		}
//		namespaces = ns;
		this.model = model;
	}

	/**
	 * @param destDirName
	 *            the destDirName to set
	 */
	public void setDestDirName(String destDirName) {
		this.destDirName = destDirName;
	}

	/**
	 * @return the model
	 */
	public TLModel getModel() {
		return model;
	}

	/**
	 * @return the destDirName
	 */
	public String getDestDirName() {
		return destDirName;
	}

	/**
	 * @param windowtitle the windowtitle to set
	 */
	public void setWindowtitle(String windowtitle) {
		this.windowtitle = windowtitle;
	}

}
