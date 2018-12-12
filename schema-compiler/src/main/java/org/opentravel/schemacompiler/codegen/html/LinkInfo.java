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
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
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

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.model.LibraryMember;

/**
 * Encapsulates information about a link.
 *
 * @author Jamie Ho
 * @since 1.5
 */
public abstract class LinkInfo {

    /**
     * The ClassDoc we want to link to.  Null if we are not linking
     * to a ClassDoc.
     */
    private DocumentationBuilder builder;

    /**
     * The LibraryMember we want to link to.  Null if we are not linking to a type.
     */
    private LibraryMember member;

    /**
     * True if this is a link to a VarArg.
     */
    private boolean isVarArg = false;

    /**
     * Set this to true to indicate that you are linking to a type parameter.
     */
    private boolean isTypeBound = false;

    /**
     * The label for the link.
     */
    private String label;

    /**
     * True if the link should be STRONG.
     */
    private boolean isStrong = false;

    /**
     * True if we should include the type in the link label.  False otherwise.
     */
    private boolean includeTypeInClassLinkLabel = true;

    /**
     * True if we should include the type as seperate link.  False otherwise.
     */
    private boolean includeTypeAsSepLink = false;

    /**
     * True if we should exclude the type bounds for the type parameter.
     */
    private boolean excludeTypeBounds = false;

    /**
     * True if we should print the type parameters, but not link them.
     */
    private boolean excludeTypeParameterLinks = false;

    /**
     * True if we should print the type bounds, but not link them.
     */
    private boolean excludeTypeBoundsLinks = false;

    /**
     * By default, the link can be to the page it's already on.  However,
     * there are cases where we don't want this (e.g. heading of class page).
     */
    private boolean linkToSelf = true;

    /**
     * The display length for the link.
     */
    private int displayLength = 0;

    /**
     * Return the id indicating where the link appears in the documentation.
     * This is used for special processing of different types of links.
     *
     * @return the id indicating where the link appears in the documentation.
     */
    public abstract int getContext();

    /**
     * Set the context.
     *
     * @param c the context id to set.
     */
    public abstract void setContext(int c);

    /**
     * Return true if this link is linkable and false if we can't link to the
     * desired place.
     *
     * @return true if this link is linkable and false if we can't link to the
     * desired place.
     */
    public abstract boolean isLinkable();

    /**
     * Return the label for this class link.
     *
     * @param configuration the current configuration of the doclet.
     * @return the label for this class link.
     */
    public String getClassLinkLabel(Configuration configuration) {
        if (getLabel() != null && getLabel().length() > 0) {
            return getLabel();
        } else if (isLinkable()) {
            return getBuilder().getName();
        } else {
            return configuration.getQualifiedName(getBuilder());
        }
    }

	/**
	 * Returns the value of the 'builder' field.
	 *
	 * @return DocumentationBuilder
	 */
	public DocumentationBuilder getBuilder() {
		return builder;
	}

	/**
	 * Assigns the value of the 'builder' field.
	 *
	 * @param builder  the field value to assign
	 */
	public void setBuilder(DocumentationBuilder builder) {
		this.builder = builder;
	}

	/**
	 * Returns the value of the 'member' field.
	 *
	 * @return LibraryMember
	 */
	public LibraryMember getMember() {
		return member;
	}

	/**
	 * Assigns the value of the 'member' field.
	 *
	 * @param member  the field value to assign
	 */
	public void setMember(LibraryMember member) {
		this.member = member;
	}

	/**
	 * Returns the value of the 'isVarArg' field.
	 *
	 * @return boolean
	 */
	public boolean isVarArg() {
		return isVarArg;
	}

	/**
	 * Assigns the value of the 'isVarArg' field.
	 *
	 * @param isVarArg  the field value to assign
	 */
	public void setVarArg(boolean isVarArg) {
		this.isVarArg = isVarArg;
	}

	/**
	 * Returns the value of the 'isTypeBound' field.
	 *
	 * @return boolean
	 */
	public boolean isTypeBound() {
		return isTypeBound;
	}

	/**
	 * Assigns the value of the 'isTypeBound' field.
	 *
	 * @param isTypeBound  the field value to assign
	 */
	public void setTypeBound(boolean isTypeBound) {
		this.isTypeBound = isTypeBound;
	}

	/**
	 * Returns the value of the 'label' field.
	 *
	 * @return String
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Assigns the value of the 'label' field.
	 *
	 * @param label  the field value to assign
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the value of the 'isStrong' field.
	 *
	 * @return boolean
	 */
	public boolean isStrong() {
		return isStrong;
	}

	/**
	 * Assigns the value of the 'isStrong' field.
	 *
	 * @param isStrong  the field value to assign
	 */
	public void setStrong(boolean isStrong) {
		this.isStrong = isStrong;
	}

	/**
	 * Returns the value of the 'includeTypeInClassLinkLabel' field.
	 *
	 * @return boolean
	 */
	public boolean isIncludeTypeInClassLinkLabel() {
		return includeTypeInClassLinkLabel;
	}

	/**
	 * Assigns the value of the 'includeTypeInClassLinkLabel' field.
	 *
	 * @param includeTypeInClassLinkLabel  the field value to assign
	 */
	public void setIncludeTypeInClassLinkLabel(boolean includeTypeInClassLinkLabel) {
		this.includeTypeInClassLinkLabel = includeTypeInClassLinkLabel;
	}

	/**
	 * Returns the value of the 'includeTypeAsSepLink' field.
	 *
	 * @return boolean
	 */
	public boolean isIncludeTypeAsSepLink() {
		return includeTypeAsSepLink;
	}

	/**
	 * Assigns the value of the 'includeTypeAsSepLink' field.
	 *
	 * @param includeTypeAsSepLink  the field value to assign
	 */
	public void setIncludeTypeAsSepLink(boolean includeTypeAsSepLink) {
		this.includeTypeAsSepLink = includeTypeAsSepLink;
	}

	/**
	 * Returns the value of the 'excludeTypeBounds' field.
	 *
	 * @return boolean
	 */
	public boolean isExcludeTypeBounds() {
		return excludeTypeBounds;
	}

	/**
	 * Assigns the value of the 'excludeTypeBounds' field.
	 *
	 * @param excludeTypeBounds  the field value to assign
	 */
	public void setExcludeTypeBounds(boolean excludeTypeBounds) {
		this.excludeTypeBounds = excludeTypeBounds;
	}

	/**
	 * Returns the value of the 'excludeTypeParameterLinks' field.
	 *
	 * @return boolean
	 */
	public boolean isExcludeTypeParameterLinks() {
		return excludeTypeParameterLinks;
	}

	/**
	 * Assigns the value of the 'excludeTypeParameterLinks' field.
	 *
	 * @param excludeTypeParameterLinks  the field value to assign
	 */
	public void setExcludeTypeParameterLinks(boolean excludeTypeParameterLinks) {
		this.excludeTypeParameterLinks = excludeTypeParameterLinks;
	}

	/**
	 * Returns the value of the 'excludeTypeBoundsLinks' field.
	 *
	 * @return boolean
	 */
	public boolean isExcludeTypeBoundsLinks() {
		return excludeTypeBoundsLinks;
	}

	/**
	 * Assigns the value of the 'excludeTypeBoundsLinks' field.
	 *
	 * @param excludeTypeBoundsLinks  the field value to assign
	 */
	public void setExcludeTypeBoundsLinks(boolean excludeTypeBoundsLinks) {
		this.excludeTypeBoundsLinks = excludeTypeBoundsLinks;
	}

	/**
	 * Returns the value of the 'linkToSelf' field.
	 *
	 * @return boolean
	 */
	public boolean isLinkToSelf() {
		return linkToSelf;
	}

	/**
	 * Assigns the value of the 'linkToSelf' field.
	 *
	 * @param linkToSelf  the field value to assign
	 */
	public void setLinkToSelf(boolean linkToSelf) {
		this.linkToSelf = linkToSelf;
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
