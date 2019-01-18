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
package org.opentravel.schemacompiler.codegen.html.markup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.DirectoryManager;
import org.opentravel.schemacompiler.codegen.html.DocletConstants;
import org.opentravel.schemacompiler.codegen.html.HtmlDoclet;
import org.opentravel.schemacompiler.codegen.html.LinkFactoryImpl;
import org.opentravel.schemacompiler.codegen.html.LinkInfoImpl;
import org.opentravel.schemacompiler.codegen.html.LinkOutputImpl;
import org.opentravel.schemacompiler.codegen.html.Util;
import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.FieldDocumentationBuilder;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Class for the Html format code generation.
 * Initilizes PrintWriter with FileWriter, to enable print
 * related methods to generate the code to the named File through FileWriter.
 *
 * @since 1.2
 * @author Atul M Dambalkar
 * @author Bhavesh Patel (Modified)
 */
public class HtmlWriter extends PrintWriter {
	
	private static final String WIDTH = "\" WIDTH=\"";
	private static final String SUMMARY = "\" SUMMARY=\"\">";
	private static final String CLASS = "\" CLASS=\"";
	private static final String TH_ALIGN = "<TH ALIGN=\"";
	private static final String TABLE_BORDER = "<TABLE BORDER=\"";
	private static final String INDENT_CLOSE_BRACE = "    }";
	private static final String TEXT_JAVASCRIPT = "text/javascript";

	/**
     * Name of the file, to which this writer is writing to.
     */
    protected final String htmlFilename;

    /**
     * The window title of this file
     */
    protected String winTitle;

    /**
     * URL file separator string("/").
     */
    public static final String FILE_SEPARATOR = DirectoryManager.URL_FILE_SEPARATOR;

    /**
     * The configuration
     */
    protected Configuration configuration;

    /**
     * The flag to indicate whether a member details list is printed or not.
     */
    protected boolean memberDetailsListPrinted;

    /**
     * Header for tables displaying packages and description.
     */
    protected final String[] libraryTableHeader;

    /**
     * Summary for use tables displaying class and package use.
     */
    protected final String useTableSummary;

    /**
     * Column header for class docs displaying Modifier and Type header.
     */
    protected final String modifierTypeHeader;

    public final Content overviewLabel;

    public final Content defaultPackageLabel;

    public final Content libraryLabel;
    
    public final Content versionLabel;
    
    public final Content namespaceLabel;

    public final Content useLabel;

    public final Content prevLabel;

    public final Content nextLabel;

    public final Content prevObjectLabel;

    public final Content nextObjectLabel;

    public final Content summaryLabel;

    public final Content detailLabel;

    public final Content framesLabel;

    public final Content noframesLabel;

    public final Content treeLabel;

    public final Content objectLabel;

    public final Content deprecatedLabel;

    public final Content deprecatedPhrase;

    public final Content allMembersLabel;

    public final Content indexLabel;

    public final Content helpLabel;

    public final Content seeLabel;

    public final Content descriptionLabel;

    public final Content prevLibraryLabel;

    public final Content nextLibraryLabel;

    public final Content librariesLabel;

    public final Content methodDetailsLabel;

    public final Content annotationTypeDetailsLabel;

    public final Content fieldDetailsLabel;

    public final Content propertyDetailsLabel;

    public final Content constructorDetailsLabel;

    public final Content enumConstantsDetailsLabel;

    public final Content specifiedByLabel;

    public final Content overridesLabel;

    public final Content descfrmClassLabel;

    public final Content descfrmInterfaceLabel;

	/**
	 * Relative path from the file getting generated to the destination
	 * directory. For EXAMPLE, if the file getting generated is
	 * "java/lang/Object.html", then the relative path string is "../../". This
	 * string can be empty if the file getting generated is in the destination
	 * directory.
	 */
	private String relativePath = "";

	/**
	 * Same as relativepath, but normalized to never be empty or end with a
	 * slash.
	 */
	private String relativepathNoSlash = "";

	/**
	 * Platform-dependent directory path from the current or the destination
	 * directory to the file getting generated. Used when creating the file. For
	 * EXAMPLE, if the file getting generated is "java/lang/Object.html", then
	 * the path string is "java/lang".
	 */
	private String path = "";

	/**
	 * Name of the file getting generated. If the file getting generated is
	 * "java/lang/Object.html", then the filename is "Object.html".
	 */
	private String filename = "";

	/**
	 * The display length used for indentation while generating the class page.
	 */
	private int displayLength = 0;

    /**
     * Constructor.
     *
     * @param path The directory path to be created for this file
     *             or null if none to be created.
     * @param filename File Name to which the PrintWriter will
     *                 do the Output.
     * @param docencoding Encoding to be used for this file.
     * @exception IOException Exception raised by the FileWriter is passed on
     * to next level.
     * OutputStreamWriter is passed on to next level.
     */
    private HtmlWriter(String path, String filename, String docencoding, Configuration configuration)
                      throws IOException {
        super(Util.genWriter(configuration, path, filename, docencoding));
        this.configuration = configuration;
        htmlFilename = filename;
        this.memberDetailsListPrinted = false;
        libraryTableHeader = new String[] {
            configuration.getText("doclet.Library"),
            configuration.getText("doclet.Description")
        };
        useTableSummary = configuration.getText("doclet.Use_Table_Summary",
                configuration.getText("doclet.packages"));
        modifierTypeHeader = configuration.getText("doclet.0_and_1",
                configuration.getText("doclet.Modifier"),
                configuration.getText("doclet.Type"));
        overviewLabel = getResource("doclet.Overview");
        defaultPackageLabel = new RawHtml(DocletConstants.DEFAULT_PACKAGE_NAME);
        libraryLabel = getResource("doclet.Library");
        namespaceLabel = getResource("doclet.Namespace");
        versionLabel = getResource("doclet.Version");
        useLabel = getResource("doclet.navClassUse");
        prevLabel = getResource("doclet.Prev");
        nextLabel = getResource("doclet.Next");
        prevObjectLabel = getResource("doclet.Prev_Object");
        nextObjectLabel = getResource("doclet.Next_Object");
        summaryLabel = getResource("doclet.Summary");
        detailLabel = getResource("doclet.Detail");
        framesLabel = getResource("doclet.Frames");
        noframesLabel = getResource("doclet.No_Frames");
        treeLabel = getResource("doclet.Tree");
        objectLabel = getResource("doclet.Object");
        deprecatedLabel = getResource("doclet.navDeprecated");
        deprecatedPhrase = getResource("doclet.Deprecated");
        allMembersLabel = getResource("doclet.All_Members");
        indexLabel = getResource("doclet.Index");
        helpLabel = getResource("doclet.Help");
        seeLabel = getResource("doclet.See");
        descriptionLabel = getResource("doclet.Description");
        prevLibraryLabel = getResource("doclet.Prev_Library");
        nextLibraryLabel = getResource("doclet.Next_Library");
        librariesLabel = getResource("doclet.Libraries");
        methodDetailsLabel = getResource("doclet.Method_Detail");
        annotationTypeDetailsLabel = getResource("doclet.Annotation_Type_Member_Detail");
        fieldDetailsLabel = getResource("doclet.Field_Detail");
        propertyDetailsLabel = getResource("doclet.Property_Detail");
        constructorDetailsLabel = getResource("doclet.Constructor_Detail");
        enumConstantsDetailsLabel = getResource("doclet.Enum_Constant_Detail");
        specifiedByLabel = getResource("doclet.Specified_By");
        overridesLabel = getResource("doclet.Overrides");
        descfrmClassLabel = getResource("doclet.Description_From_Class");
        descfrmInterfaceLabel = getResource("doclet.Description_From_Interface");
    }

	/**
	 * Constructor to construct the HtmlStandardWriter object.
	 *
	 * @param filename
	 *            File to be generated.
	 */
	public HtmlWriter(Configuration configuration, String filename)
			throws IOException {
        this(null, configuration.getDestDirName() + filename,
                configuration.getDocencoding(), configuration);
		this.configuration = configuration;
		this.setFilename(filename);
	}

	/**
	 * Constructor to construct the HtmlStandardWriter object.
	 *
	 * @param path
	 *            Platform-dependent {@link #path} used when creating file.
	 * @param filename
	 *            Name of file to be generated.
	 * @param relativePath
	 *            Value for the variable {@link #relativePath}.
	 */
	public HtmlWriter(Configuration configuration, String path,
			String filename, String relativePath) throws IOException {
        this(configuration.getDestDirName() + path, filename,
                configuration.getDocencoding(), configuration);
		this.configuration = configuration;
		this.setPath(path);
		this.setRelativePath(relativePath);
		this.setRelativepathNoSlash(DirectoryManager
				.getPathNoTrailingSlash(this.getRelativePath()));
		this.setFilename(filename);
	}

    /**
     * Get the configuration string as a content.
     *
     * @param key the key to look for in the configuration file
     * @return a content tree for the text
     */
    public Content getResource(String key) {
        return new StringContent(configuration.getText(key));
    }

    /**
     * Get the configuration string as a content.
     *
     * @param key the key to look for in the configuration file
     * @param a1 string argument added to configuration text
     * @return a content tree for the text
     */
    public Content getResource(String key, String a1) {
        return new RawHtml(configuration.getText(key, a1));
    }

    /**
     * Print &lt;/HTML&gt; tag. Add a newline character at the end.
     */
    public void htmlEnd() {
        println("</HTML>");
    }

    /**
     * Returns an HtmlTree for the SCRIPT tag.
     *
     * @return an HtmlTree for the SCRIPT tag
     */
    protected HtmlTree getWinTitleScript(){
        HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
        if(winTitle != null && winTitle.length() > 0) {
            script.addAttr(HtmlAttr.TYPE, TEXT_JAVASCRIPT);
            String scriptCode = "<!--" + DocletConstants.NL +
                    "    if (location.href.indexOf('is-external=true') == -1) {" + DocletConstants.NL +
                    "        parent.document.title=\"" + winTitle + "\";" + DocletConstants.NL +
                    INDENT_CLOSE_BRACE + DocletConstants.NL +
                    "//-->" + DocletConstants.NL;
            RawHtml scriptContent = new RawHtml(scriptCode);
            script.addContent(scriptContent);
        }
        return script;
    }

    /**
     * Returns a content tree for the SCRIPT tag for the main page(index.html).
     *
     * @return a content for the SCRIPT tag
     */
    protected Content getFramesetJavaScript(){
        HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
        script.addAttr(HtmlAttr.TYPE, TEXT_JAVASCRIPT);
        String scriptCode = DocletConstants.NL + "    targetPage = \"\" + window.location.search;" + DocletConstants.NL +
                "    if (targetPage != \"\" && targetPage != \"undefined\")" + DocletConstants.NL +
                "        targetPage = targetPage.substring(1);" + DocletConstants.NL +
                "    if (targetPage.indexOf(\":\") != -1 || (targetPage != \"\" && !validURL(targetPage)))" + DocletConstants.NL +
                "        targetPage = \"undefined\";" + DocletConstants.NL +
                "    function validURL(url) {" + DocletConstants.NL +
                "        var pos = url.indexOf(\".html\");" + DocletConstants.NL +
                "        if (pos == -1 || pos != url.length - 5)" + DocletConstants.NL +
                "            return false;" + DocletConstants.NL +
                "        var allowNumber = false;" + DocletConstants.NL +
                "        var allowSep = false;" + DocletConstants.NL +
                "        var seenDot = false;" + DocletConstants.NL +
                "        for (var i = 0; i < url.length - 5; i++) {" + DocletConstants.NL +
                "            var ch = url.charAt(i);" + DocletConstants.NL +
                "            if ('a' <= ch && ch <= 'z' ||" + DocletConstants.NL +
                "                    'A' <= ch && ch <= 'Z' ||" + DocletConstants.NL +
                "                    ch == '$' ||" + DocletConstants.NL +
                "                    ch == '_') {" + DocletConstants.NL +
                "                allowNumber = true;" + DocletConstants.NL +
                "                allowSep = true;" + DocletConstants.NL +
                "            } else if ('0' <= ch && ch <= '9'" + DocletConstants.NL +
                "                    || ch == '-') {" + DocletConstants.NL +
                "                if (!allowNumber)" + DocletConstants.NL +
                "                     return false;" + DocletConstants.NL +
                "            } else if (ch == '/' || ch == '.') {" + DocletConstants.NL +
                "                if (!allowSep)" + DocletConstants.NL +
                "                    return false;" + DocletConstants.NL +
                "                allowNumber = false;" + DocletConstants.NL +
                "                allowSep = false;" + DocletConstants.NL +
                "                if (ch == '.')" + DocletConstants.NL +
                "                     seenDot = true;" + DocletConstants.NL +
                "                if (ch == '/' && seenDot)" + DocletConstants.NL +
                "                     return false;" + DocletConstants.NL +
                "            } else {" + DocletConstants.NL +
                "                return false;"+ DocletConstants.NL +
                "            }" + DocletConstants.NL +
                "        }" + DocletConstants.NL +
                "        return true;" + DocletConstants.NL +
                INDENT_CLOSE_BRACE + DocletConstants.NL +
                "    function loadFrames() {" + DocletConstants.NL +
                "        if (targetPage != \"\" && targetPage != \"undefined\")" + DocletConstants.NL +
                "             top.classFrame.location = top.targetPage;" + DocletConstants.NL +
                INDENT_CLOSE_BRACE + DocletConstants.NL;
        RawHtml scriptContent = new RawHtml(scriptCode);
        script.addContent(scriptContent);
        return script;
    }

    /**
     * Print the Javascript &lt;SCRIPT&gt; start tag with its type
     * attribute.
     */
    public void script() {
        println("<SCRIPT type=\"text/javascript\">");
    }

    /**
     * Print the Javascript &lt;/SCRIPT&gt; end tag.
     */
    public void scriptEnd() {
        println("</SCRIPT>");
    }

    /**
     * Print the Javascript &lt;NOSCRIPT&gt; start tag.
     */
    public void noScript() {
        println("<NOSCRIPT>");
    }

    /**
     * Print the Javascript &lt;/NOSCRIPT&gt; end tag.
     */
    public void noScriptEnd() {
        println("</NOSCRIPT>");
    }

    /**
     * Return the Javascript call to be embedded in the &lt;BODY&gt; tag.
     * Return nothing if winTitle is empty.
     * @return the Javascript call to be embedded in the &lt;BODY&gt; tag.
     */
    protected String getWindowTitleOnload(){
        if(winTitle != null && winTitle.length() > 0) {
            return " onload=\"windowTitle();\"";
        } else {
            return "";
        }
    }

    /**
     * Returns an HtmlTree for the BODY tag.
     *
     * @param includeScript  set true if printing windowtitle script
     * @param title title for the window
     * @return an HtmlTree for the BODY tag
     */
    public HtmlTree getBody(boolean includeScript, String title) {
        HtmlTree body = new HtmlTree(HtmlTag.BODY);
        // Set window title string which is later printed
        this.winTitle = title;
        // Don't print windowtitle script for overview-frame, allclasses-frame
        // and package-frame
        if (includeScript) {
            body.addContent(getWinTitleScript());
            Content noScript = HtmlTree.noscript(
                    HtmlTree.div(getResource("doclet.No_Script_Message")));
            body.addContent(noScript);
        }
        return body;
    }

    /**
     * Print &lt;/BODY&gt; tag. Add a newline character at the end.
     */
    public void bodyEnd() {
        println("</BODY>");
    }

    /**
     * Print &lt;TITLE&gt; tag. Add a newline character at the end.
     */
    public void title() {
        println("<TITLE>");
    }

    /**
     * Print &lt;TITLE&gt; tag. Add a newline character at the end.
     *
     * @param winTitle The TITLE of this document.
     */
    public void title(String winTitle) {
        // Set window title string which is later printed
        this.winTitle = winTitle;
        title();
    }

    /**
     * Returns an HtmlTree for the title tag.
     *
     * @return an HtmlTree for the title tag
     */
    public HtmlTree getTitle() {
        return HtmlTree.title(new StringContent(winTitle));
    }

    /**
     * Print &lt;H1&gt; tag. Add a newline character at the end.
     */
    public void h1() {
        println("<H1>");
    }

    /**
     * Print &lt;/H1&gt; tag. Add a newline character at the end.
     */
    public void h1End() {
        println("</H1>");
    }

    /**
     * Print &lt;H2&gt; tag. Add a newline character at the end.
     */
    public void h2() {
        println("<H2>");
    }

    /**
     * Print &lt;/H2&gt; tag. Add a newline character at the end.
     */
    public void h2End() {
        println("</H2>");
    }

    /**
     * Print &lt;H3&gt; tag. Add a newline character at the end.
     */
    public void h3() {
        println("<H3>");
    }

    /**
     * Print &lt;H4&gt; tag. Add a newline character at the end.
     */
    public void h4() {
        println("<H4>");
    }

    /**
     * Print &lt;/H4&gt; tag. Add a newline character at the end.
     */
    public void h4End() {
        println("</H4>");
    }

    /**
     * Print &lt;STRONG&gt; tag.
     */
    public void strong() {
        print("<STRONG>");
    }

    /**
     * Print &lt;/STRONG&gt; tag.
     */
    public void strongEnd() {
        print("</STRONG>");
    }

    /**
     * Print text passed, in STRONG format using &lt;STRONG&gt; and &lt;/STRONG&gt; tags.
     *
     * @param text String to be printed in between &lt;STRONG&gt; and &lt;/STRONG&gt; tags.
     */
    public void strong(String text) {
        strong();
        print(text);
        strongEnd();
    }
    
    /**
     * Return "&#38;nbsp;", non-breaking space.
     */
    public Content getSpace() {
        return RawHtml.nbsp;
    }

    /**
     * Print &lt;DL&gt; tag. Add a newline character at the end.
     */
    public void dl() {
        println("<DL>");
    }

    /**
     * Print &lt;/DL&gt; tag. Add a newline character at the end.
     */
    public void dlEnd() {
        println("</DL>");
    }

    /**
     * Print &lt;FONT SIZE="size"&gt; tag. Add a newline character at the end.
     *
     * @param size String size.
     */
    public void font(String size) {
        println("<FONT SIZE=\"" + size + "\">");
    }

    /**
     * Print &lt;/FONT&gt; tag.
     */
    public void fontEnd() {
        print("</FONT>");
    }
    
    /**
     * Print anchor &lt;A NAME="name"&gt; tag.
     *
     * @param name Name String.
     */
    public void aName(String name) {
        print("<A NAME=\"" + name + "\">");
    }

    /**
     * Print &lt;/A&gt; tag.
     */
    public void aEnd() {
        print("</A>");
    }

    /**
     * Print contents within anchor &lt;A NAME="name"&gt; tags.
     *
     * @param name String name.
     * @param content String contents.
     */
    public void anchor(String name, String content) {
        aName(name);
        print(content);
        aEnd();
    }

    /**
     * Print anchor &lt;A NAME="name"&gt; and &lt;/A&gt;tags. Print comment string
     * "&lt;!-- --&gt;" within those tags.
     *
     * @param name String name.
     */
    public void anchor(String name) {
        anchor(name, "<!-- -->");
    }
    
    /**
     * Print HTML &lt;TABLE BORDER="border" WIDTH="width"&gt;
     *
     * @param border       Border size.
     * @param width        Width of the table.
     */
    public void table(int border, String width) {
        println(DocletConstants.NL +
                TABLE_BORDER + border +
                WIDTH + width +
                SUMMARY);
    }

    /**
     * Print the HTML table tag with border size 0 and width 100%.
     */
    public void table() {
        table(0, "100%");
    }

    /**
     * Print &lt;/TABLE&gt; tag. Add a newline character at the end.
     */
    public void tableEnd() {
        println("</TABLE>");
    }

    /**
     * Print &lt;TR&gt; tag. Add a newline character at the end.
     */
    public void tr() {
        println("<TR>");
    }

    /**
     * Print &lt;/TR&gt; tag. Add a newline character at the end.
     */
    public void trEnd() {
        println("</TR>");
    }

    /**
     * Print &lt;TD&gt; tag.
     */
    public void td() {
        print("<TD>");
    }

    /**
     * Print &lt;TD WIDTH="width"&gt; tag.
     *
     * @param width String width.
     */
    public void tdWidth(String width) {
        print("<TD WIDTH=\"" + width + "\">");
    }

    /**
     * Print &lt;/TD&gt; tag. Add a newline character at the end.
     */
    public void tdEnd() {
        println("</TD>");
    }

    /**
     * Print &lt;LINK str&gt; tag.
     *
     * @param str String.
     */
    public void link(String str) {
        println("<LINK " + str + ">");
    }

    /**
     * Print &lt;TR BGCOLOR="color" CLASS="stylename"&gt; tag. Adds a newline character
     * at the end.
     *
     * @param color String color.
     * @param stylename String stylename.
     */
    public void trBgcolorStyle(String color, String stylename) {
        println("<TR BGCOLOR=\"" + color + CLASS + stylename + "\">");
    }

    /**
     * Print &lt;TH ALIGN="align"&gt; tag.
     *
     * @param align the align attribute.
     */
    public void thAlign(String align) {
        print(TH_ALIGN + align + "\">");
    }

    /**
     * Print &lt;TH align="align" COLSPAN=i&gt; tag.
     *
     * @param align the align attribute.
     * @param i integer.
     */
    public void thAlignColspan(String align, int i) {
        print(TH_ALIGN + align + "\" COLSPAN=\"" + i + "\">");
    }

    /**
     * Print &lt;/TH&gt; tag. Add a newline character at the end.
     */
    public void thEnd() {
        println("</TH>");
    }

    /**
     * Print Html Hyper Link.
     *
     * @param link String name of the file.
     * @param where Position of the link in the file. Character '#' is not
     * needed.
     * @param label Tag for the link.
     * @param STRONG  Boolean that sets label to STRONG.
     */
    public void printHyperLink(String link, String where,
                               String label, boolean strong) {
        print(getHyperLinkString(link, where, label, strong, "", "", ""));
    }

    /**
     * Return Html Hyper Link string.
     *
     * @param link       String name of the file.
     * @param where      Position of the link in the file. Character '#' is not
     * needed.
     * @param label      Tag for the link.
     * @param STRONG       Boolean that sets label to STRONG.
     * @return String    Hyper Link.
     */
    public String getHyperLinkString(String link, String where,
                               String label, boolean strong) {
        return getHyperLinkString(link, where, label, strong, "", "", "");
    }

    /**
     * Get Html Hyper Link string.
     *
     * @param link       String name of the file.
     * @param where      Position of the link in the file. Character '#' is not
     *                   needed.
     * @param label      Tag for the link.
     * @return a content tree for the hyper link
     */
    public Content getHyperLink(String link, String where,
                               Content label) {
        return getHyperLink(link, where, label, "", "");
    }

    /**
     * Get Html Hyper Link string.
     *
     * @param link       String name of the file.
     * @param where      Position of the link in the file. Character '#' is not
     *                   needed.
     * @param label      Tag for the link.
     * @param STRONG       Boolean that sets label to STRONG.
     * @param stylename  String style of text defined in style sheet.
     * @param title      String that describes the link's content for accessibility.
     * @param target     Target frame.
     * @return String    Hyper Link.
     */
    public String getHyperLinkString(String link, String where,
                               String label, boolean strong,
                               String stylename, String title, String target) {
        StringBuilder retlink = new StringBuilder();
        retlink.append("<a href=\"");
        retlink.append(link);
        if (where != null && where.length() != 0) {
            retlink.append("#");
            retlink.append(where);
        }
        retlink.append("\"");
        if (title != null && title.length() != 0) {
            retlink.append(" title=\"" + title + "\"");
        }
        if (target != null && target.length() != 0) {
            retlink.append(" target=\"" + target + "\"");
        }
        retlink.append(">");
        if (stylename != null && stylename.length() != 0) {
            retlink.append("<FONT CLASS=\"");
            retlink.append(stylename);
            retlink.append("\">");
        }
        if (strong) {
            retlink.append("<span class=\"STRONG\">");
        }
        retlink.append(label);
        if (strong) {
            retlink.append("</span>");
        }
        if (stylename != null && stylename.length() != 0) {
            retlink.append("</FONT>");
        }
        retlink.append("</a>");
        return retlink.toString();
    }

    /**
     * Get Html Hyper Link.
     *
     * @param link       String name of the file.
     * @param where      Position of the link in the file. Character '#' is not
     *                   needed.
     * @param label      Tag for the link.
     * @param title      String that describes the link's content for accessibility.
     * @param target     Target frame.
     * @return a content tree for the hyper link.
     */
    public Content getHyperLink(String link, String where,
            Content label, String title, String target) {
        if (where != null && where.length() != 0) {
            link += "#" + where;
        }
        HtmlTree anchor = HtmlTree.a(link, label);
        if (title != null && title.length() != 0) {
            anchor.addAttr(HtmlAttr.TITLE, title);
        }
        if (target != null && target.length() != 0) {
            anchor.addAttr(HtmlAttr.TARGET, target);
        }
        return anchor;
    }

    /**
     * Get a hyperlink to a file.
     *
     * @param link String name of the file
     * @param label Label for the link
     * @return a content for the hyperlink to the file
     */
    public Content getHyperLink(String link, Content label) {
        return getHyperLink(link, "", label);
    }

    /**
     * Get link string without positioning in the file.
     *
     * @param link       String name of the file.
     * @param label      Tag for the link.
     * @return Strign    Hyper link.
     */
    public String getHyperLinkString(String link, String label) {
        return getHyperLinkString(link, "", label, false);
    }

    /**
     * Get the name of the package, this class is in.
     *
     * @param cd    ClassDoc.
     */
    public String getNamespace(LibraryMember cd) {
        String namespace = cd.getNamespace();
        if (namespace.length() > 0) {
            namespace += ":";
            return namespace;
        }
        return "";
    }

    /**
     * Get the name of the package, this class is in.
     *
     * @param cd    ClassDoc.
     */
    public String getNamespace(DocumentationBuilder cd) {
        String namespace = cd.getNamespace();
        if (namespace.length() > 0) {
            namespace += ":";
            return namespace;
        }
        return "";
    }
    public boolean getMemberDetailsListPrinted() {
        return memberDetailsListPrinted;
    }

    /**
     * Print the frameset version of the Html file header.
     * Called only when generating an HTML frameset file.
     *
     * @param title Title of this HTML document
     * @param frameset the frameset to be added to the HTML document
     */
    public void printFramesetDocument(String title, Content frameset) {
        Content htmlDocType = DocType.newFrameset();
        Content htmlComment = new Comment(configuration.getText("doclet.New_Page"));
        Content head = new HtmlTree(HtmlTag.HEAD);
        
            Content headComment = new Comment("Generated by javadoc on " + today());
            head.addContent(headComment);
        
        Content windowTitle = HtmlTree.title(new StringContent(title));
        head.addContent(windowTitle);
        head.addContent(getFramesetJavaScript());
        Content htmlTree = HtmlTree.html(configuration.getLocale().getLanguage(),
                head, frameset);
        Content htmlDocument = new HtmlDocument(htmlDocType,
                htmlComment, htmlTree);
        print(htmlDocument.toString());
    }

    /**
     * Print the closing &lt;/body&gt; and &lt;/html&gt; tags.
     */
    public void printBodyHtmlEnd() {
        println();
        bodyEnd();
        htmlEnd();
    }

    /**
     * Calls {@link #printBodyHtmlEnd()} method.
     */
    public void printFooter() {
        printBodyHtmlEnd();
    }

    /**
     * Get the day and date information for today, depending upon user option.
     *
     * @return String Today.
     * @see java.util.Calendar
     * @see java.util.GregorianCalendar
     * @see java.util.TimeZone
     */
    public String today() {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        return calendar.getTime().toString();
    }
    
	/**
	 * Get the script to show or hide the All classes link.
	 *
	 * @param id
	 *            id of the element to show or hide
	 * @return a content tree for the script
	 */
	public Content getAllClassesLinkScript(String id) {
		HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
		script.addAttr(HtmlAttr.TYPE, TEXT_JAVASCRIPT);
		String scriptCode = "<!--" + DocletConstants.NL
				+ "  allClassesLink = document.getElementById(\"" + id + "\");"
				+ DocletConstants.NL + "  if(window==top) {"
				+ DocletConstants.NL
				+ "    allClassesLink.style.display = \"BLOCK\";"
				+ DocletConstants.NL + "  }" + DocletConstants.NL + "  else {"
				+ DocletConstants.NL
				+ "    allClassesLink.style.display = \"none\";"
				+ DocletConstants.NL + "  }" + DocletConstants.NL + "  //-->"
				+ DocletConstants.NL;
		Content scriptContent = new RawHtml(scriptCode);
		script.addContent(scriptContent);
		return HtmlTree.div(script);
	}

	/**
	 * Get the script to show or hide the All classes link.
	 *
	 * @param id
	 *            id of the element to show or hide
	 * @return a content tree for the script
	 */
	public Content getJQueryScript() {
		HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
		script.addAttr(HtmlAttr.TYPE, TEXT_JAVASCRIPT);
		script.addAttr(HtmlAttr.SRC,
				"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js");
		return script;
	}

	/**
	 * Get the script to show or hide the All classes link.
	 *
	 * @param id
	 *            id of the element to show or hide
	 * @return a content tree for the script
	 */
	public Content getToggleScript() {
		HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
		script.addAttr(HtmlAttr.TYPE, TEXT_JAVASCRIPT);
		String s = // "<!--" + DocletConstants.NL
		"$(document).ready(function(){" + DocletConstants.NL
				+ "$('[data-toggle=\"COLLAPSED\"]').click(function(){"
				+ DocletConstants.NL + "var target = $(this).data('target');"
				+ DocletConstants.NL + "$(target).toggleClass('COLLAPSED');"
				+ DocletConstants.NL
				+ "var imgTarget = $(this).data('imgtarget');"
				+ DocletConstants.NL + "var img = $(imgTarget);"
				+ DocletConstants.NL + "img.addClass('TOGGLE_BUTTON');"
				+ DocletConstants.NL + "$(this).removeClass('TOGGLE_BUTTON');"
				+ DocletConstants.NL + "});" + DocletConstants.NL + "});";
		script.addContent(new RawHtml(s));
		return script;
	}
	
	/**
	 * Get Package link, with target frame.
	 *
	 * @param pd
	 *            The link will be to the "package-summary.html" page for this
	 *            package
	 * @param target
	 *            name of the target frame
	 * @param label
	 *            tag for the link
	 * @return a content for the target package link
	 */
	public Content getTargetLibraryLink(String namespace, String target,
			Content label) {
		return getHyperLink(pathString(namespace, "library-summary.html"), "",
				label, "", target);
	}

	/**
	 * Generates the HTML document tree and prints it out.
	 *
	 * @param metakeywords
	 *            Array of String keywords for META tag. Each element of the
	 *            array is assigned to a separate META tag. Pass in null for no
	 *            array
	 * @param includeScript
	 *            true if printing windowtitle script false for files that
	 *            appear in the left-hand frames
	 * @param body
	 *            the body htmltree to be included in the document
	 */
	public void printHtmlDocument(String[] metakeywords, boolean includeScript,
			Content body) {
		Content htmlDocType = DocType.newHtml5();
		Content htmlComment = new Comment(
				configuration.getText("doclet.New_Page"));
		Content head = new HtmlTree(HtmlTag.HEAD);
		Content headComment = new Comment("Generated by otm (version "
				+ Configuration.VERSION + ") on " + today());
		head.addContent(headComment);
		head.addContent(getTitle());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Content meta = HtmlTree.meta("date", dateFormat.format(new Date()));
		head.addContent(meta);
		head.addContent(getStyleSheetProperties());
		Content htmlTree = HtmlTree.html(configuration.getLocale()
				.getLanguage(), head, body);
		Content script = getJQueryScript();
		htmlTree.addContent(script);
		script = getToggleScript();
		htmlTree.addContent(script);
		Content htmlDocument = new HtmlDocument(htmlDocType, htmlComment,
				htmlTree);
		print(htmlDocument.toString());
	}

	/**
	 * Get the window title.
	 *
	 * @param title
	 *            the title string to construct the complete window title
	 * @return the window title string
	 */
	public String getWindowTitle(String title) {
		if (configuration.getWindowtitle().length() > 0) {
			title += " (" + configuration.getWindowtitle() + ")";
		}
		return title;
	}


	/**
	 * Adds the navigation bar for the Html page at the top and and the bottom.
	 *
	 * @param header
	 *            If true print navigation bar at the top of the page else
	 * @param body
	 *            the HtmlTree to which the nav links will be added
	 */
	protected void addNavLinks(boolean header, Content body) {
		String allClassesId = "allclasses_";
		HtmlTree navDiv = new HtmlTree(HtmlTag.DIV);
		if (header) {
			body.addContent(HtmlConstants.START_OF_TOP_NAVBAR);
			navDiv.setStyle(HtmlStyle.TOP_NAV);
			allClassesId += "navbar_top";
			Content a = getMarkerAnchor("navbar_top");
			navDiv.addContent(a);
			Content skipLinkContent = getHyperLink("", "skip-navbar_top",
					HtmlTree.EMPTY,
					configuration.getText("doclet.Skip_navigation_links"), "");
			navDiv.addContent(skipLinkContent);
		} else {
			body.addContent(HtmlConstants.START_OF_BOTTOM_NAVBAR);
			navDiv.setStyle(HtmlStyle.BOTTOM_NAV);
			allClassesId += "navbar_bottom";
			Content a = getMarkerAnchor("navbar_bottom");
			navDiv.addContent(a);
			Content skipLinkContent = getHyperLink("", "skip-navbar_bottom",
					HtmlTree.EMPTY,
					configuration.getText("doclet.Skip_navigation_links"), "");
			navDiv.addContent(skipLinkContent);
		}
		if (header) {
			navDiv.addContent(getMarkerAnchor("navbar_top_firstrow"));
		} else {
			navDiv.addContent(getMarkerAnchor("navbar_bottom_firstrow"));
		}
		HtmlTree navList = new HtmlTree(HtmlTag.UL);
		navList.setStyle(HtmlStyle.NAV_LIST);
		navList.addAttr(HtmlAttr.TITLE,
				configuration.getText("doclet.Navigation"));
		if (configuration.isCreateoverview()) {
			navList.addContent(getNavLinkContents());
		}
		List<TLLibrary> libraries = configuration.getLibraries();
		int size = libraries.size();
		if (size == 1) {
			navList.addContent(getNavLinkLibrary(libraries.get(0).getName()));
		} else if (size > 1) {
			navList.addContent(getNavLinkLibrary());
		}
		navList.addContent(getNavLinkObject());
		if (configuration.isClassuse()) {
			navList.addContent(getNavLinkClassUse());
		}
		if (configuration.isCreatetree()) {
			navList.addContent(getNavLinkTree());
		}
		if (configuration.isCreateindex()) {
			navList.addContent(getNavLinkIndex());
		}
		if (!configuration.isNohelp()) {
			navList.addContent(getNavLinkHelp());
		}
		navDiv.addContent(navList);
		body.addContent(navDiv);
		Content ulNav = HtmlTree.ul(HtmlStyle.NAV_LIST, getNavLinkPrevious());
		ulNav.addContent(getNavLinkNext());
		Content subDiv = HtmlTree.div(HtmlStyle.SUB_NAV, ulNav);
		Content ulFrames = HtmlTree.ul(HtmlStyle.NAV_LIST, getNavShowLists());
		ulFrames.addContent(getNavHideLists(getFilename()));
		subDiv.addContent(ulFrames);
		HtmlTree ulAllClasses = HtmlTree.ul(HtmlStyle.NAV_LIST,
				getNavLinkClassIndex());
		ulAllClasses.addAttr(HtmlAttr.ID, allClassesId);
		subDiv.addContent(ulAllClasses);
		subDiv.addContent(getAllClassesLinkScript(allClassesId));
		if (header) {
			subDiv.addContent(getMarkerAnchor("skip-navbar_top"));
			body.addContent(subDiv);
			body.addContent(HtmlConstants.END_OF_TOP_NAVBAR);
		} else {
			subDiv.addContent(getMarkerAnchor("skip-navbar_bottom"));
			body.addContent(subDiv);
			body.addContent(HtmlConstants.END_OF_BOTTOM_NAVBAR);
		}
	}

	/**
	 * Get the word "NEXT" to indicate that no link is available. Override this
	 * method to customize next link.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkNext() {
		return getNavLinkNext(null);
	}

	/**
	 * Get the word "PREV" to indicate that no link is available. Override this
	 * method to customize prev link.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkPrevious() {
		return getNavLinkPrevious(null);
	}


	/**
	 * Get link to the "overview-summary.html" page.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkContents() {
		Content linkContent = getHyperLink(getRelativePath()
				+ "overview-summary.html", "", overviewLabel, "", "");
		return HtmlTree.li(linkContent);
	}

	/**
	 * Get link to the "namespace-summary.html" page for the package passed.
	 *
	 * @param ns
	 *            Namespace to which link will be generated
	 * @return a content tree for the link
	 */
	protected Content getNavLinkLibrary(String ns) {
		Content linkContent = getLibraryLink(ns, libraryLabel);
		return HtmlTree.li(linkContent);
	}

	/**
	 * Get the word "Package" , to indicate that link is not available here.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkLibrary() {
		return HtmlTree.li(libraryLabel);
	}


	/**
	 * Get the word "Use", to indicate that link is not available.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkClassUse() {
		return HtmlTree.li(useLabel);
	}


	/**
	 * Get link for previous file.
	 *
	 * @param prev
	 *            File name for the prev link
	 * @return a content tree for the link
	 */
	public Content getNavLinkPrevious(String prev) {
		Content li;
		if (prev != null) {
			li = HtmlTree.li(getHyperLink(prev, "", prevLabel, "", ""));
		} else {
			li = HtmlTree.li(prevLabel);
		}
		return li;
	}

	/**
	 * Get link for next file. If next is null, just print the label without
	 * linking it anywhere.
	 *
	 * @param next
	 *            File name for the next link
	 * @return a content tree for the link
	 */
	public Content getNavLinkNext(String next) {
		Content li;
		if (next != null) {
			li = HtmlTree.li(getHyperLink(next, "", nextLabel, "", ""));
		} else {
			li = HtmlTree.li(nextLabel);
		}
		return li;
	}

	/**
	 * Get "FRAMES" link, to switch to the frame version of the output.
	 *
	 * @param link
	 *            File to be linked, "index.html"
	 * @return a content tree for the link
	 */
	protected Content getNavShowLists(String link) {
		Content framesContent = getHyperLink(link + "?" + getPath() + getFilename(), "",
				framesLabel, "", "_top");
		return HtmlTree.li(framesContent);
	}


	/**
	 * Get "FRAMES" link, to switch to the frame version of the output.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavShowLists() {
		return getNavShowLists(getRelativePath() + "index.html");
	}


	/**
	 * Get "NO FRAMES" link, to switch to the non-frame version of the output.
	 *
	 * @param link
	 *            File to be linked
	 * @return a content tree for the link
	 */
	protected Content getNavHideLists(String link) {
		Content noFramesContent = getHyperLink(link, "", noframesLabel, "", "_top");
		return HtmlTree.li(noFramesContent);
	}


	/**
	 * Get "Tree" link in the navigation bar. If there is only one package
	 * specified on the command line, then the "Tree" link will be to the only
	 * "package-tree.html" file otherwise it will be to the "overview-tree.html"
	 * file.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkTree() {
		Content treeLinkContent;
		treeLinkContent = getHyperLink(getRelativePath() + "overview-tree.html", "",
				treeLabel, "", "");
		return HtmlTree.li(treeLinkContent);
	}

	/**
	 * Get the overview tree link for the main tree.
	 *
	 * @param label
	 *            the label for the link
	 * @return a content tree for the link
	 */
	protected Content getNavLinkMainTree(String label) {
		Content mainTreeContent = getHyperLink(getRelativePath()
				+ "overview-tree.html", new StringContent(label));
		return HtmlTree.li(mainTreeContent);
	}


	/**
	 * Get the word "Class", to indicate that class link is not available.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkObject() {
		return HtmlTree.li(objectLabel);
	}

	/**
	 * Get link for generated index. If the user has used "-splitindex" command
	 * line option, then link to file "index-files/index-1.html" is generated
	 * otherwise link to file "index-all.html" is generated.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkClassIndex() {
		return HtmlTree.EMPTY;
	}

	/**
	 * Get link for generated class index.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkIndex() {
		return HtmlTree.EMPTY;
	}

	/**
	 * Get help file link. If user has provided a help file, then generate a
	 * link to the user given file, which is already copied to current or
	 * destination directory.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkHelp() {
		String helpfilenm = configuration.getHelpfile();
		if (helpfilenm.equals("")) {
			helpfilenm = "help-doc.html";
		} else {
			int lastsep;
			if ((lastsep = helpfilenm.lastIndexOf(File.separatorChar)) != -1) {
				helpfilenm = helpfilenm.substring(lastsep + 1);
			}
		}
		Content linkContent = getHyperLink(getRelativePath() + helpfilenm, "",
				helpLabel, "", "");
		return HtmlTree.li(linkContent);
	}
	
	/**
	 * Get summary table header.
	 *
	 * @param header
	 *            the header for the table
	 * @param scope
	 *            the scope of the headers
	 * @return a content tree for the header
	 */
	public Content getSummaryTableHeader(String[] header, String scope) {
		Content tr = new HtmlTree(HtmlTag.TR);
		int size = header.length;
		Content tableHeader;
		if (size == 1) {
			tableHeader = new StringContent(header[0]);
			tr.addContent(HtmlTree.th(HtmlStyle.COL_ONE, scope, tableHeader));
			return tr;
		}
		for (int i = 0; i < size; i++) {
			tableHeader = new StringContent(header[i]);
			if (i == 0)
				tr.addContent(HtmlTree.th(HtmlStyle.COL_FIRST, scope,
						tableHeader));
			else if (i == (size - 1))
				tr.addContent(HtmlTree
						.th(HtmlStyle.COL_LAST, scope, tableHeader));
			else
				tr.addContent(HtmlTree.th(scope, tableHeader));
		}
		return tr;
	}

	/**
	 * Get table caption.
	 *
	 * @param rawText
	 *            the caption for the table which could be raw Html
	 * @return a content tree for the caption
	 */
	public Content getTableCaption(String rawText) {
		Content title = new RawHtml(rawText);
		Content captionSpan = HtmlTree.span(title);
		Content space = getSpace();
		Content tabSpan = HtmlTree.span(HtmlStyle.TAB_END, space);
		Content caption = HtmlTree.caption(captionSpan);
		caption.addContent(tabSpan);
		return caption;
	}

	/**
	 * Get the marker anchor which will be added to the documentation tree.
	 *
	 * @param anchorName
	 *            the anchor name attribute
	 * @return a content tree for the marker anchor
	 */
	public Content getMarkerAnchor(String anchorName) {
		return getMarkerAnchor(anchorName, null);
	}

	/**
	 * Get the marker anchor which will be added to the documentation tree.
	 *
	 * @param anchorName
	 *            the anchor name attribute
	 * @param anchorContent
	 *            the content that should be added to the anchor
	 * @return a content tree for the marker anchor
	 */
	public Content getMarkerAnchor(String anchorName, Content anchorContent) {
		if (anchorContent == null)
			anchorContent = new Comment(" ");
		return HtmlTree.aName(anchorName, anchorContent);
	}

	/**
	 * Returns a packagename content.
	 *
	 * @param namespace
	 *            the package to check
	 * @return package name content
	 */
	public Content getNamespaceName(String namespace) {
		return namespace == null || namespace.length() == 0 ? defaultPackageLabel
				: getNamespaceLabel(namespace);
	}

	/**
	 * Returns a package name label.
	 *
	 * @param parsedName
	 *            the package name
	 * @return the package name content
	 */
	public Content getNamespaceLabel(String packageName) {
		return new StringContent(packageName);
	}

	/**
	 * Prine table header information about color, column span and the font.
	 *
	 * @param color
	 *            Background color.
	 * @param span
	 *            Column span.
	 */
	public void tableHeaderStart(String color, int span) {
		trBgcolorStyle(color, "TableHeadingColor");
		thAlignColspan("left", span);
		font("+2");
	}

	/**
	 * Return path to the class page for a classdoc. For EXAMPLE, the class name
	 * is "java.lang.Object" and if the current file getting generated is
	 * "java/io/File.html", then the path string to the class, returned is
	 * "../../java/lang.Object.html".
	 *
	 * @param cd
	 *            Class to which the path is requested.
	 */
	protected String pathToObject(DocumentationBuilder cd) {
		return pathString(cd.getOwningLibrary(), cd.getName() + ".html");
	}

	/**
	 * Return path to the given file name in the given package. So if the name
	 * passed is "Object.html" and the name of the package is "java.lang", and
	 * if the relative path is "../.." then returned string will be
	 * "../../java/lang/Object.html"
	 *
	 * @param pd
	 *            Package in which the file name is assumed to be.
	 * @param name
	 *            File name, to which path string is.
	 */
	protected String pathString(String pd, String name) {
		StringBuilder buf = new StringBuilder(getRelativePath());
		
		buf.append(DirectoryManager.getPathToLibrary(pd, name));
		return buf.toString();
	}

	/**
	 * Print the link to the given package.
	 *
	 * @param pkg
	 *            the package to link to.
	 * @param label
	 *            the label for the link.
	 * @param isStrong
	 *            true if the label should be STRONG.
	 */
	public void printNamespaceLink(String pkg, String label, boolean isStrong) {
		print(getNamespaceLinkString(pkg, label, isStrong));
	}

	/**
	 * Return the link to the given package.
	 *
	 * @param pkg
	 *            the package to link to.
	 * @param label
	 *            the label for the link.
	 * @param isStrong
	 *            true if the label should be STRONG.
	 * @return the link to the given package.
	 */
	public String getNamespaceLinkString(String pkg, String label,
			boolean isStrong) {
		return getNamespaceLinkString(pkg, label, isStrong, "");
	}

	/**
	 * Return the link to the given package.
	 *
	 * @param pkg
	 *            the package to link to.
	 * @param label
	 *            the label for the link.
	 * @param isStrong
	 *            true if the label should be STRONG.
	 * @param style
	 *            the font of the package link label.
	 * @return the link to the given package.
	 */
	public String getNamespaceLinkString(String pkg, String label,
			boolean isStrong, String style) {
		return label;
	}

	/**
	 * Return the link to the given package.
	 *
	 * @param pkg
	 *            the package to link to.
	 * @param label
	 *            the label for the link.
	 * @return a content tree for the package link.
	 */
	public Content getLibraryLink(String namespace, Content label) {
		if (namespace != null) {
			return getHyperLink(pathString(namespace, "library-summary.html"),
					"", label);
		} else {
			return label;
		}
	}

	public String italicsObjectName(LibraryMember member, boolean qual) {
		return (qual) ? configuration.getQualifiedName(member) : member.getLocalName();
	}

	/**
	 * Return the link to the given class.
	 *
	 * @param linkInfo
	 *            the information about the link.
	 *
	 * @return the link for the given class.
	 */
	public String getLink(LinkInfoImpl linkInfo) {
		LinkFactoryImpl factory = new LinkFactoryImpl(this);
		String link = null;
		try {
			link = ((LinkOutputImpl) factory.getLinkOutput(linkInfo))
					.toString();
		} catch (NullPointerException npe) {
			// TODO: This occurs numerous times during document generation
			configuration.printNotice("Missing link for: " + linkInfo.getLabel());
		}
		setDisplayLength(getDisplayLength() + linkInfo.getDisplayLength());
		return link;
	}

	/**
	 * Retrieve the class link with the package portion of the label in plain
	 * text. If the qualifier is excluded, it willnot be included in the link
	 * label.
	 *
	 * @param cd
	 *            the class to link to.
	 * @param isStrong
	 *            true if the link should be STRONG.
	 * @return the link with the package portion of the label in plain text.
	 */
	public String getPreQualifiedMemberLink(int context,
			AbstractDocumentationBuilder<?> cd, boolean isStrong) {
		String classlink = "";
		String pd = cd.getNamespace();
		if (pd != null) {
			classlink = getNamespace(cd);
		}
		classlink += getLink(new LinkInfoImpl(context, cd, cd.getName(),
				isStrong));
		return classlink;
	}

	/**
	 * Get the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be added
	 * @param doc
	 *            the member being linked to
	 * @param label
	 *            the label for the link
	 * @return a content tree for the doc link
	 */
	public Content getDocLink(int context, FieldDocumentationBuilder<?> doc,
			String label) {
		return getDocLink(context, doc.getOwner(), doc, label);
	}

	/**
	 * Print the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be printed.
	 * @param classDoc
	 *            the classDoc that we should link to. This is not necessarily
	 *            equal to doc.containingClass(). We may be inheriting comments.
	 * @param doc
	 *            the member being linked to.
	 * @param label
	 *            the label for the link.
	 * @param STRONG
	 *            true if the link should be STRONG.
	 */
	public void printDocLink(int context,
			AbstractDocumentationBuilder<?> classDoc,
			FieldDocumentationBuilder<?> doc, String label, boolean strong) {
		print(getDocLink(context, classDoc, doc, label, strong));
	}

	/**
	 * Return the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be printed.
	 * @param classDoc
	 *            the classDoc that we should link to. This is not necessarily
	 *            equal to doc.containingClass(). We may be inheriting comments.
	 * @param doc
	 *            the member being linked to.
	 * @param label
	 *            the label for the link.
	 * @param STRONG
	 *            true if the link should be STRONG.
	 * @return the link for the given member.
	 */
	public String getDocLink(int context,
			DocumentationBuilder classDoc, DocumentationBuilder doc,
			String label, boolean strong) {
		if (!(Util.isLinkable(classDoc, newConfiguration()))) {
			return label;
		} else{
			return getLink(new LinkInfoImpl(context, classDoc, doc.getName(),
					label, strong));
		}
	}

	/**
	 * Return the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be added
	 * @param classDoc
	 *            the classDoc that we should link to. This is not necessarily
	 *            equal to doc.containingClass(). We may be inheriting comments
	 * @param doc
	 *            the member being linked to
	 * @param label
	 *            the label for the link
	 * @return the link for the given member
	 */
	public Content getDocLink(int context,
			AbstractDocumentationBuilder<?> classDoc,
			FieldDocumentationBuilder<?> doc, String label) {
		if (!(Util.isLinkable(classDoc, newConfiguration()))) {
			return new StringContent(label);
		} else if (doc instanceof FieldDocumentationBuilder<?>) {
			return new RawHtml(getLink(new LinkInfoImpl(context, classDoc,
					doc.getName(), label, false)));
		} else {
			return new StringContent(label);
		}
	}

	public String getAnchor(AbstractDocumentationBuilder<?> emd) {
		return getAnchor(emd, false);
	}

	public String getAnchor(AbstractDocumentationBuilder<?> emd,
			boolean isProperty) {
		if (isProperty) {
			return emd.getName();
		}
		return emd.getName();
	}


	/**
	 * Adds the summary content.
	 *
	 * @param doc
	 *            the doc for which the summary will be generated
	 * @param htmltree
	 *            the documentation tree to which the summary will be added
	 */
	public void addSummaryComment(DocumentationBuilder doc, Content htmltree) {
		String desc = doc.getDescription();
		addSummaryComment(desc == null ? "" : desc, htmltree);
	}

	/**
	 * Adds the summary content.
	 *
	 * @param doc
	 *            the doc for which the summary will be generated
	 * @param htmltree
	 *            the documentation tree to which the summary will be added
	 */
	public void addSummaryComment(TLDocumentationOwner owner, Content htmltree) {
		TLDocumentation doc = owner.getDocumentation();
		String desc = "";
		if (doc != null) {
			desc = doc.getDescription();
		}
		addSummaryComment(desc, htmltree);
	}


	/**
	 * Adds the summary content.
	 *
	 * @param doc
	 *            the doc for which the summary will be generated
	 * @param firstSentenceTags
	 *            the first sentence tags for the doc
	 * @param htmltree
	 *            the documentation tree to which the summary will be added
	 */
	public void addSummaryComment(String firstSentenceTags, Content htmltree) {
		addCommentTags(firstSentenceTags, false, htmltree);
	}

	/**
	 * Adds the inline comment.
	 *
	 * @param doc
	 *            the doc for which the inline comments will be generated
	 * @param htmltree
	 *            the documentation tree to which the inline comments will be
	 *            added
	 */
	public void addInlineComment(String comment, Content htmltree) {
		addCommentTags(comment, false, htmltree);
	}


	/**
	 * Adds the comment tags.
	 *
	 * @param doc
	 *            the doc for which the comment tags will be generated
	 * @param comment
	 *            the first sentence tags for the doc
	 * @param depr
	 *            true if it is deprecated
	 * @param htmltree
	 *            the documentation tree to which the comment tags will be added
	 */
	private void addCommentTags(String comment, boolean depr, Content htmltree) {
		Content div;
		Content result = new RawHtml(comment);
		if (depr) {
			Content italic = HtmlTree.i(result);
			div = HtmlTree.div(HtmlStyle.BLOCK, italic);
			htmltree.addContent(div);
		} else {
			div = HtmlTree.div(HtmlStyle.BLOCK, result);
			htmltree.addContent(div);
		}
		if (comment.length() == 0) {
			htmltree.addContent(getSpace());
		}
	}

	/**
	 * Returns a link to the stylesheet file.
	 *
	 * @return an HtmlTree for the lINK tag which provides the stylesheet
	 *         location
	 */
	public HtmlTree getStyleSheetProperties() {
		String fName = configuration.getStylesheetfile();
		
		if (fName.length() > 0) {
			File stylefile = new File(fName);
			String parent = stylefile.getParent();
			
			fName = (parent == null) ? fName : fName.substring(parent.length() + 1);
		} else {
			fName = HtmlDoclet.DEFAULT_STYLESHEET;
		}
		fName = getRelativePath() + fName;
		return HtmlTree.link("stylesheet", "text/css", fName, "Style");
	}

	/**
	 * Return the configuation for this doclet.
	 *
	 * @return the configuration for this doclet.
	 */
	public Configuration newConfiguration() {
		return configuration;
	}

	/**
	 * Returns the value of the 'relativePath' field.
	 *
	 * @return String
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * Assigns the value of the 'relativePath' field.
	 *
	 * @param relativePath  the field value to assign
	 */
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	/**
	 * Returns the value of the 'relativepathNoSlash' field.
	 *
	 * @return String
	 */
	public String getRelativepathNoSlash() {
		return relativepathNoSlash;
	}

	/**
	 * Assigns the value of the 'relativepathNoSlash' field.
	 *
	 * @param relativepathNoSlash  the field value to assign
	 */
	public void setRelativepathNoSlash(String relativepathNoSlash) {
		this.relativepathNoSlash = relativepathNoSlash;
	}

	/**
	 * Returns the value of the 'path' field.
	 *
	 * @return String
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Assigns the value of the 'path' field.
	 *
	 * @param path  the field value to assign
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns the value of the 'filename' field.
	 *
	 * @return String
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Assigns the value of the 'filename' field.
	 *
	 * @param filename  the field value to assign
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Returns the value of the 'displayLength' field.
	 *
	 * @return int
	 */
	public int getDisplayLength() {
		return displayLength;
	}

	/**
	 * Assigns the value of the 'displayLength' field.
	 *
	 * @param displayLength  the field value to assign
	 */
	public void setDisplayLength(int displayLength) {
		this.displayLength = displayLength;
	}
	
}
