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

import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlWriter;

import java.io.IOException;



/**
 * Generate the documentation in the Html "frame" format in the browser. The generated documentation will have two or
 * three frames depending upon the number of packages on the command line. In general there will be three frames in the
 * output, a left-hand top frame will have a list of all packages with links to target left-hand bottom frame. The
 * left-hand bottom frame will have the particular package contents or the all-classes list, where as the single
 * right-hand frame will have overview or package summary or class file. Also take care of browsers which do not support
 * Html frames.
 *
 * @author Atul M Dambalkar
 */
public class FrameOutputWriter extends HtmlWriter {

    /**
     * Number of packages specified on the command line.
     */
    int noOfNamespaces;

    private static final String SCROLL_YES = "yes";

    /**
     * Constructor to construct FrameOutputWriter object.
     *
     * @param configuration the configuration used for output generation
     * @param filename File to be generated.
     * @throws IOException thrown if an error occurs during writer initialization
     */
    public FrameOutputWriter(Configuration configuration, String filename) throws IOException {
        super( configuration, filename );
        noOfNamespaces = configuration.getLibraries().size();
    }

    /**
     * Construct FrameOutputWriter object and then use it to generate the Html file which will have the description of
     * all the frames in the documentation. The name of the generated file is "index.html" which is the default first
     * file for Html documents.
     * 
     * @param configuration the configuration used for output generation
     * @throws DocletAbortException thrown if an unrecoverable error occurs
     */
    public static void generate(Configuration configuration) {
        FrameOutputWriter framegen;
        String filename = "";
        try {
            filename = "index.html";
            framegen = new FrameOutputWriter( configuration, filename );
            framegen.generateFrameFile();
            framegen.close();
        } catch (IOException exc) {
            configuration.message.error( "doclet.exception_encountered", exc.toString(), filename );
            throw new DocletAbortException();
        }
    }

    /**
     * Generate the contants in the "index.html" file. Print the frame details as well as warning if browser is not
     * supporting the Html frames.
     */
    protected void generateFrameFile() {
        Content frameset = getFrameDetails();
        if (configuration.getWindowtitle().length() > 0) {
            printFramesetDocument( configuration.getWindowtitle(), frameset );
        } else {
            printFramesetDocument( configuration.getText( "doclet.Generated_Docs_Untitled" ), frameset );
        }
    }

    /**
     * Add the code for issueing the warning for a non-frame capable web client. Also provide links to the non-frame
     * version documentation.
     *
     * @param contentTree the content tree to which the non-frames information will be added
     */
    protected void addFrameWarning(Content contentTree) {
        Content noframes = new HtmlTree( HtmlTag.NOFRAMES );
        Content noScript = HtmlTree.noscript( HtmlTree.div( getResource( "doclet.No_Script_Message" ) ) );
        noframes.addContent( noScript );
        Content noframesHead = HtmlTree.heading( HtmlConstants.CONTENT_HEADING, getResource( "doclet.Frame_Alert" ) );
        noframes.addContent( noframesHead );
        Content p = HtmlTree.paragraph( getResource( "doclet.Frame_Warning_Message",
            getHyperLinkString( configuration.getTopFile(), configuration.getText( "doclet.Non_Frame_Version" ) ) ) );
        noframes.addContent( p );
        contentTree.addContent( noframes );
    }

    /**
     * Get the frame sizes and their contents.
     *
     * @return a content tree for the frame details
     */
    protected Content getFrameDetails() {
        HtmlTree frameset = HtmlTree.frameset( "20%,80%", null, "Documentation frame", "top.loadFrames()" );
        if (noOfNamespaces <= 1) {
            addAllObjectsFrameTag( frameset );
        } else if (noOfNamespaces > 1) {
            HtmlTree leftFrameset = HtmlTree.frameset( null, "30%,70%", "Left frames", "top.loadFrames()" );
            addAllLibrariesFrameTag( leftFrameset );
            addAllObjectsFrameTag( leftFrameset );
            frameset.addContent( leftFrameset );
        }
        addClassFrameTag( frameset );
        addFrameWarning( frameset );
        return frameset;
    }

    /**
     * Add the FRAME tag for the frame that lists all packages.
     *
     * @param contentTree the content tree to which the information will be added
     */
    private void addAllLibrariesFrameTag(Content contentTree) {
        HtmlTree frame = HtmlTree.frame( LibraryIndexFrameWriter.DEFAULT_FILENAME, "libraryListFrame",
            configuration.getText( "doclet.All_Libraries" ) );
        contentTree.addContent( frame );
    }

    /**
     * Add the FRAME tag for the frame that lists all classes.
     *
     * @param contentTree the content tree to which the information will be added
     */
    private void addAllObjectsFrameTag(Content contentTree) {
        HtmlTree frame = HtmlTree.frame( AllObjectsFrameWriter.OUTPUT_FILE_NAME_FRAMES, "libraryFrame",
            configuration.getText( "doclet.All_library_members" ) );
        contentTree.addContent( frame );
    }

    /**
     * Add the FRAME tag for the frame that describes the class in detail.
     *
     * @param contentTree the content tree to which the information will be added
     */
    private void addClassFrameTag(Content contentTree) {
        HtmlTree frame = HtmlTree.frame( configuration.getTopFile(), "classFrame",
            configuration.getText( "doclet.Library_member_descriptions" ), SCROLL_YES );
        contentTree.addContent( frame );
    }
}
