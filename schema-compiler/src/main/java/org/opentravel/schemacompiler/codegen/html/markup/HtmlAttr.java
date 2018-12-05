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
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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
package org.opentravel.schemacompiler.codegen.html.markup;

/**
 * Enum representing HTML tag attributes.
 *
 */
public enum HtmlAttr {
    ALT,
    BORDER,
    CELLPADDING,
    CELLSPACING,
    CHARSET,
    CLASS,
    CLEAR,
    COLS,
    CONTENT,
    HREF,
    HTTP_EQUIV("http-equiv"),
    ID,
    LANG,
    NAME,
    ONLOAD,
    REL,
    ROWS,
    SCOPE,
    SCROLLING,
    SRC,
    SUMMARY,
    TARGET,
    TITLE,
    TYPE,
    WIDTH, COLSPAN;

    private final String value;

    HtmlAttr() {
        this.value = name().toLowerCase();
    }

    HtmlAttr(String name) {
        this.value = name;
    }

    @Override
    public String toString() {
        return value;
    }
}
