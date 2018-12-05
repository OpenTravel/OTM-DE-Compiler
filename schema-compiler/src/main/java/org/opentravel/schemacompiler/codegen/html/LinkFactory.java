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
            return getObjectLink(linkInfo);
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

}
