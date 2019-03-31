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

/**
 * Enum representing HTML tags.
 *
 * @author Bhavesh Patel
 */
public enum HtmlTag {
    A(BlockType.INLINE, EndTag.END),
    BLOCKQUOTE,
    BODY(BlockType.OTHER, EndTag.END),
    BR(BlockType.INLINE, EndTag.NOEND),
    CAPTION,
    CENTER,
    CODE(BlockType.INLINE, EndTag.END),
    DD,
    DIV,
    DL,
    DT,
    EM(BlockType.INLINE, EndTag.END),
    FONT(BlockType.INLINE, EndTag.END),
    FRAME(BlockType.OTHER, EndTag.NOEND),
    FRAMESET(BlockType.OTHER, EndTag.END),
    H1,
    H2,
    H3,
    H4,
    H5,
    H6,
    HEAD(BlockType.OTHER, EndTag.END),
    HR(BlockType.BLOCK, EndTag.NOEND),
    HTML(BlockType.OTHER, EndTag.END),
    I(BlockType.INLINE, EndTag.END),
    IMG(BlockType.INLINE, EndTag.NOEND),
    LI,
    LINK(BlockType.OTHER, EndTag.NOEND),
    MENU,
    META(BlockType.OTHER, EndTag.NOEND),
    NOFRAMES(BlockType.OTHER, EndTag.END),
    NOSCRIPT(BlockType.OTHER, EndTag.END),
    OL,
    P,
    PRE,
    SCRIPT(BlockType.OTHER, EndTag.END),
    SMALL(BlockType.INLINE, EndTag.END),
    SPAN(BlockType.INLINE, EndTag.END),
    STRONG(BlockType.INLINE, EndTag.END),
    TABLE,
    TBODY,
    TD,
    TH,
    TITLE(BlockType.OTHER, EndTag.END),
    TR,
    TT(BlockType.INLINE, EndTag.END),
    UL;

    protected final BlockType blockType;
    protected final EndTag endTag;
    private final String value;

    /**
     * Enum representing the type of HTML element.
     */
    protected enum BlockType {
        BLOCK, INLINE, OTHER;
    }

    /**
     * Enum representing HTML end tag requirement.
     */
    protected enum EndTag {
        END, NOEND;
    }

    HtmlTag() {
        this( BlockType.BLOCK, EndTag.END );
    }

    HtmlTag(BlockType blockType, EndTag endTag) {
        this.blockType = blockType;
        this.endTag = endTag;
        this.value = name().toLowerCase();
    }

    /**
     * Returns true if the end tag is required. This is specific to the standard doclet and does not exactly resemble
     * the W3C specifications.
     *
     * @return true if end tag needs to be displayed else return false
     */
    public boolean endTagRequired() {
        return (endTag == EndTag.END);
    }

    @Override
    public String toString() {
        return value;
    }
}
