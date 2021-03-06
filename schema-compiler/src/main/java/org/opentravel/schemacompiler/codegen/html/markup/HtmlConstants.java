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

import org.opentravel.schemacompiler.codegen.html.Content;


/**
 * Stores constants for Html Doclet.
 *
 * @author Bhavesh Patel
 */
public class HtmlConstants {

    /**
     * Marker to identify start of top navigation bar.
     */
    public static final Content START_OF_TOP_NAVBAR = new Comment( "========= START OF TOP NAVBAR =======" );

    /**
     * Marker to identify start of bottom navigation bar.
     */
    public static final Content START_OF_BOTTOM_NAVBAR = new Comment( "======= START OF BOTTOM NAVBAR ======" );

    /**
     * Marker to identify end of top navigation bar.
     */
    public static final Content END_OF_TOP_NAVBAR = new Comment( "========= END OF TOP NAVBAR =========" );

    /**
     * Marker to identify end of bottom navigation bar.
     */
    public static final Content END_OF_BOTTOM_NAVBAR = new Comment( "======== END OF BOTTOM NAVBAR =======" );

    /**
     * Marker to identify start of class data.
     */
    public static final Content START_OF_CLASS_DATA = new Comment( "======== START OF CLASS DATA ========" );

    /**
     * Marker to identify end of class data.
     */
    public static final Content END_OF_CLASS_DATA = new Comment( "========= END OF CLASS DATA =========" );

    /**
     * Marker to identify start of nested class summary.
     */
    public static final Content START_OF_NESTED_CLASS_SUMMARY = new Comment( "======== NESTED CLASS SUMMARY ========" );

    /**
     * Marker to identify start of annotation type optional member summary.
     */
    public static final Content START_OF_ANNOTATION_TYPE_OPTIONAL_MEMBER_SUMMARY =
        new Comment( "=========== ANNOTATION TYPE OPTIONAL MEMBER SUMMARY ===========" );

    /**
     * Marker to identify start of annotation type required member summary.
     */
    public static final Content START_OF_ANNOTATION_TYPE_REQUIRED_MEMBER_SUMMARY =
        new Comment( "=========== ANNOTATION TYPE REQUIRED MEMBER SUMMARY ===========" );

    /**
     * Marker to identify start of constructor summary.
     */
    public static final Content START_OF_CONSTRUCTOR_SUMMARY = new Comment( "======== CONSTRUCTOR SUMMARY ========" );

    /**
     * Marker to identify start of enum constants summary.
     */
    public static final Content START_OF_ENUM_CONSTANT_SUMMARY =
        new Comment( "=========== ENUM CONSTANT SUMMARY ===========" );

    /**
     * Marker to identify start of field summary.
     */
    public static final Content START_OF_FIELD_SUMMARY = new Comment( "=========== FIELD SUMMARY ===========" );

    /**
     * Marker to identify start of properties summary.
     */
    public static final Content START_OF_PROPERTY_SUMMARY = new Comment( "=========== PROPERTY SUMMARY ===========" );

    /**
     * Marker to identify start of method summary.
     */
    public static final Content START_OF_METHOD_SUMMARY = new Comment( "========== METHOD SUMMARY ===========" );

    /**
     * Marker to identify start of annotation type details.
     */
    public static final Content START_OF_ANNOTATION_TYPE_DETAILS =
        new Comment( "============ ANNOTATION TYPE MEMBER DETAIL ===========" );

    /**
     * Marker to identify start of method details.
     */
    public static final Content START_OF_METHOD_DETAILS = new Comment( "============ METHOD DETAIL ==========" );

    /**
     * Marker to identify start of field details.
     */
    public static final Content START_OF_FIELD_DETAILS = new Comment( "============ FIELD DETAIL ===========" );

    /**
     * Marker to identify start of property details.
     */
    public static final Content START_OF_PROPERTY_DETAILS = new Comment( "============ PROPERTY DETAIL ===========" );

    /**
     * Marker to identify start of constructor details.
     */
    public static final Content START_OF_CONSTRUCTOR_DETAILS = new Comment( "========= CONSTRUCTOR DETAIL ========" );

    /**
     * Marker to identify start of enum constants details.
     */
    public static final Content START_OF_ENUM_CONSTANT_DETAILS =
        new Comment( "============ ENUM CONSTANT DETAIL ===========" );

    /**
     * Html tag for the page title heading.
     */
    public static final HtmlTag TITLE_HEADING = HtmlTag.H1;

    /**
     * Html tag for the class page title heading.
     */
    public static final HtmlTag CLASS_PAGE_HEADING = HtmlTag.H2;

    /**
     * Html tag for the content heading.
     */
    public static final HtmlTag CONTENT_HEADING = HtmlTag.H2;

    /**
     * Html tag for the package name heading.
     */
    public static final HtmlTag PACKAGE_HEADING = HtmlTag.H2;

    /**
     * Html tag for the member summary heading.
     */
    public static final HtmlTag SUMMARY_HEADING = HtmlTag.H3;

    /**
     * Html tag for the inherited member summary heading.
     */
    public static final HtmlTag INHERITED_SUMMARY_HEADING = HtmlTag.H4;

    /**
     * Html tag for the member details heading.
     */
    public static final HtmlTag DETAILS_HEADING = HtmlTag.H3;

    /**
     * Html tag for the serialized member heading.
     */
    public static final HtmlTag SERIALIZED_MEMBER_HEADING = HtmlTag.H3;

    /**
     * Html tag for the member heading.
     */
    public static final HtmlTag MEMBER_HEADING = HtmlTag.H4;

    /**
     * Private constructor to prevent instantiation.
     */
    private HtmlConstants() {}

}
