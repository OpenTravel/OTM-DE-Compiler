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

package org.opentravel.schemacompiler.codegen.html;

import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlAttr;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlWriter;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to generate file for each package contents in the left-hand bottom frame. This will list all the Class Kinds in
 * the package. A click on any class-kind will update the right-hand frame with the clicked class-kind page.
 */
public class LibraryFrameWriter extends HtmlWriter {

    private TLLibrary library;


    /**
     * The name of the output file.
     */
    public static final String OUTPUT_FILE_NAME = "library-frame.html";

    /**
     * Constructor to construct PackageFrameWriter object and to generate "package-frame.html" file in the respective
     * package directory. For EXAMPLE for package "java.lang" this will generate file "package-frame.html" file in the
     * "java/lang" directory. It will also create "java/lang" directory in the current or the destination directory if
     * it doesen't exist.
     *
     * @param configuration the configuration of the doclet.
     * @param library the library for which the HTML frame is being generated
     * @throws IOException thrown if an error occurs during writer initialization
     */
    public LibraryFrameWriter(Configuration configuration, TLLibrary library) throws IOException {
        super( configuration,
            DirectoryManager.getDirectoryPath( AbstractDocumentationBuilder.getLibraryName( library ) ),
            OUTPUT_FILE_NAME,
            DirectoryManager.getRelativePath( AbstractDocumentationBuilder.getLibraryName( library ) ) );
        this.library = library;
    }

    /**
     * Generate a package summary page for the left-hand bottom frame. Construct the PackageFrameWriter object and then
     * uses it generate the file.
     *
     * @param configuration the current configuration of the doclet.
     * @param library The package for which "library-frame.html" is to be generated.
     */
    public static void generate(Configuration configuration, TLLibrary library) {
        try (LibraryFrameWriter packgen = new LibraryFrameWriter( configuration, library )) {
            String name = AbstractDocumentationBuilder.getLibraryName( library );
            Content body = packgen.getBody( false, packgen.getWindowTitle( name ) );
            Content pkgNameContent = new RawHtml( name );
            Content heading = HtmlTree.heading( HtmlConstants.TITLE_HEADING, HtmlStyle.BAR,
                packgen.getTargetLibraryLink( name, "classFrame", pkgNameContent ) );
            body.addContent( heading );
            HtmlTree div = new HtmlTree( HtmlTag.DIV );
            div.setStyle( HtmlStyle.INDEX_CONTAINER );
            packgen.addClassListing( div );
            body.addContent( div );
            packgen.printHtmlDocument( null, false, body );
        } catch (IOException exc) {
            configuration.message.error( "doclet.exception_encountered", exc.toString(), OUTPUT_FILE_NAME );
            throw new DocletAbortException();
        }
    }

    /**
     * Add class listing for all the classes in this package. Divide class listing as per the class kind and generate
     * separate listing for Classes, Interfaces, Exceptions and Errors.
     *
     * @param contentTree the content tree to which the listing will be added
     */
    protected void addClassListing(Content contentTree) {
        addClassKindListing( library.getBusinessObjectTypes(), getResource( "doclet.BusinessObjects" ), contentTree );
        addClassKindListing( library.getCoreObjectTypes(), getResource( "doclet.CoreObjects" ), contentTree );
        addClassKindListing( library.getValueWithAttributesTypes(), getResource( "doclet.VWA" ), contentTree );
        List<TLAbstractEnumeration> enums = new ArrayList<>();
        enums.addAll( library.getClosedEnumerationTypes() );
        enums.addAll( library.getOpenEnumerationTypes() );
        addClassKindListing( enums, getResource( "doclet.Enums" ), contentTree );
        List<TLService> services = new ArrayList<>();
        services.add( library.getService() );
        addClassKindListing( services, getResource( "doclet.Services" ), contentTree );
        addClassKindListing( library.getSimpleTypes(), getResource( "doclet.SimpleTypes" ), contentTree );

    }

    /**
     * Add specific class kind listing. Also add label to the listing.
     *
     * @param members Array of specific class kinds, namely Class or Interface or Exception or Error
     * @param labelContent content tree of the label to be added
     * @param contentTree the content tree to which the class kind listing will be added
     */
    protected void addClassKindListing(List<? extends LibraryMember> members, Content labelContent,
        Content contentTree) {
        if (!members.isEmpty()) {
            boolean printedHeader = false;
            HtmlTree ul = new HtmlTree( HtmlTag.UL );
            ul.addAttr( HtmlAttr.TITLE, labelContent.toString() );
            for (LibraryMember member : members) {
                if (!configuration.isGeneratedDoc( member )) {
                    continue;
                }
                if (!printedHeader) {
                    Content heading = HtmlTree.heading( HtmlConstants.CONTENT_HEADING, true, labelContent );
                    contentTree.addContent( heading );
                    printedHeader = true;
                }
                Content link = new RawHtml( getLink(
                    new LinkInfoImpl( LinkInfoImpl.PACKAGE_FRAME, member, member.getLocalName(), "classFrame" ) ) );
                Content li = HtmlTree.li( link );
                ul.addContent( li );
            }
            contentTree.addContent( ul );
        }
    }
}
