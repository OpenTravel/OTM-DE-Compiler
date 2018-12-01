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


import java.io.IOException;
import java.util.List;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.codegen.html.LinkInfoImpl;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.markup.StringContent;
import org.opentravel.schemacompiler.codegen.html.DocletAbortException;
import org.opentravel.schemacompiler.codegen.html.IndexBuilder;

/**
 * Generate the file with list of all the classes in this run. This page will be
 * used in the left-hand bottom frame, when "All Classes" link is clicked in
 * the left-hand top frame. The name of the generated file is
 * "allclasses-frame.html".
 *
 * @author Atul M Dambalkar
 * @author Doug Kramer
 * @author Bhavesh Patel (Modified)
 */
public class AllObjectsFrameWriter extends HtmlDocletWriter {

    /**
     * The name of the output file with frames
     */
    public static final String OUTPUT_FILE_NAME_FRAMES = "allmembers-frame.html";

    /**
     * The name of the output file without frames
     */
    public static final String OUTPUT_FILE_NAME_NOFRAMES = "allmembers-noframe.html";

    /**
     * Index of all the classes.
     */
    protected IndexBuilder indexbuilder;

    /**
     * BR tag to be used within a document tree.
     */
    final HtmlTree BR = new HtmlTree(HtmlTag.BR);

    /**
     * Construct AllClassesFrameWriter object. Also initilises the indexbuilder
     * variable in this class.
     * @throws IOException
     * @throws DocletAbortException
     */
    public AllObjectsFrameWriter(Configuration configuration,
                                 String filename, IndexBuilder indexbuilder)
                              throws IOException {
        super(configuration, filename);
        this.indexbuilder = indexbuilder;
    }

    /**
     * Create AllClassesFrameWriter object. Then use it to generate the
     * "allclasses-frame.html" file. Generate the file in the current or the
     * destination directory.
     *
     * @param indexbuilder IndexBuilder object for all classes index.
     * @throws DocletAbortException
     */
    public static void generate(Configuration configuration,
                                IndexBuilder indexbuilder) {        
        try (AllObjectsFrameWriter allclassgen = 
        		new AllObjectsFrameWriter(configuration,
        			OUTPUT_FILE_NAME_FRAMES, indexbuilder)) {
            allclassgen.buildAllMembersFile(true);
        }  catch (IOException exc) {
            configuration.message.
            	error("doclet.exception_encountered",
                  exc.toString(), OUTPUT_FILE_NAME_FRAMES);
            throw new DocletAbortException();
        }    
            
        try (AllObjectsFrameWriter allclassgen = 
        		new AllObjectsFrameWriter(configuration,
            		OUTPUT_FILE_NAME_NOFRAMES, indexbuilder)) {
            allclassgen.buildAllMembersFile(false);
        } catch (IOException exc) {
            configuration.message.
                error("doclet.exception_encountered",
                  exc.toString(), OUTPUT_FILE_NAME_NOFRAMES);
            throw new DocletAbortException();
        } 
    }

    /**
     * Print all the classes in the file.
     * @param wantFrames True if we want frames.
     */
    protected void buildAllMembersFile(boolean wantFrames) throws IOException {
        String label = configuration.getText("doclet.All_Members");
        Content body = getBody(false, getWindowTitle(label));
        Content heading = HtmlTree.heading(HtmlConstants.TITLE_HEADING,
                HtmlStyle.bar, allMembersLabel);
        body.addContent(heading);
        Content ul = new HtmlTree(HtmlTag.UL);
        // Generate the class links and add it to the tdFont tree.
        addAllClasses(ul, wantFrames);
        Content div = HtmlTree.div(HtmlStyle.indexContainer, ul);
        body.addContent(div);
        printHtmlDocument(null, false, body);
    }

    /**
     * Use the sorted index of all the classes and add all the classes to the
     * content list.
     *
     * @param content HtmlTree content to which all classes information will be added
     * @param wantFrames True if we want frames.
     */
    protected void addAllClasses(Content content, boolean wantFrames) {
        for (int i = 0; i < indexbuilder.elements().length; i++) {
            Character unicode = (Character)((indexbuilder.elements())[i]);
            addContents(indexbuilder.getMemberList(unicode), wantFrames, content);
        }
    }

    /**
     * Given a list of classes, generate links for each class or interface.
     * If the class kind is interface, print it in the italics font. Also all
     * links should target the right-hand frame. If clicked on any class name
     * in this page, appropriate class page should get opened in the right-hand
     * frame.
     *
     * @param classlist Sorted list of classes.
     * @param wantFrames True if we want frames.
     * @param content HtmlTree content to which the links will be added
     */
    protected void addContents(List<LibraryMember> memberList, boolean wantFrames,
            Content content) {
        for (LibraryMember member : memberList) {
            String label = italicsObjectName(member, false);
            Content linkContent = null;
        	String link;
            if(wantFrames){
            	link = getLink(new LinkInfoImpl(
                        LinkInfoImpl.ALL_CLASSES_FRAME, member, label, "classFrame"));              
            } else {
            	link = getLink(new LinkInfoImpl(member, label));
            }
            if(link != null){
            	linkContent = new RawHtml(link);
            }else{
            	linkContent = new StringContent();
            }
            Content li = HtmlTree.li(linkContent);
            content.addContent(li);
        }
    }
}
