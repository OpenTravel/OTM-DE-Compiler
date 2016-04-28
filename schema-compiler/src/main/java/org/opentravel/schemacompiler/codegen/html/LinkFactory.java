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

/**
 * A factory that constructs links from given link information.
 *
 * @author Jamie Ho
 * @since 1.5
 */
public abstract class LinkFactory {

    /**
     * Return an empty instance of the link output object.
     *
     * @return an empty instance of the link output object.
     */
    protected abstract LinkOutput getOutputInstance();

    /**
     * Constructs a link from the given link information.
     *
     * @param linkInfo the information about the link.
     * @return the output of the link.
     */
    public LinkOutput getLinkOutput(LinkInfo linkInfo) {
      if (linkInfo.builder != null) {
            //Just a class link
            LinkOutput linkOutput = getObjectLink(linkInfo);
            return linkOutput;
        } else {
            return null;
        }
    }

    /**
     * Return the link to the given class.
     *
     * @param linkInfo the information about the link to construct.
     *
     * @return the link for the given class.
     */
    protected abstract LinkOutput getObjectLink(LinkInfo linkInfo);


    /**
     * Return &amp;lt;, which is used in type parameters.  Override this
     * if your doclet uses something different.
     *
     * @return return &amp;lt;, which is used in type parameters.
     */
    protected String getLessThanString() {
        return "&lt;";
    }

    /**
     * Return &amp;gt;, which is used in type parameters.  Override this
     * if your doclet uses something different.
     *
     * @return return &amp;gt;, which is used in type parameters.
     */
    protected String getGreaterThanString() {
        return "&gt;";
    }
}
