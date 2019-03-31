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


/**
 * A class to create content for javadoc output pages.
 *
 * @author Bhavesh Patel
 */
public abstract class Content {

    /**
     * Returns a string representation of the content.
     *
     * @return string representation of the content
     */
    public String toString() {
        StringBuilder contentBuilder = new StringBuilder();
        write( contentBuilder );
        return contentBuilder.toString();
    }

    /**
     * Adds content to the existing content.
     *
     * @param content content that needs to be added
     */
    public abstract void addContent(Content content);

    /**
     * Adds a string content to the existing content.
     *
     * @param stringContent the string content to be added
     */
    public abstract void addContent(String stringContent);

    /**
     * Writes content to a StringBuilder.
     * 
     * @param contentBuilder the string builder to which content should be written
     */
    public abstract void write(StringBuilder contentBuilder);

    /**
     * Returns true if the content is empty.
     *
     * @return true if no content to be displayed else return false
     */
    public abstract boolean isEmpty();

    /**
     * Returns true if the content is valid.
     *
     * @return true if the content is valid else return false
     */
    public boolean isValid() {
        return !isEmpty();
    }

    /**
     * Checks for null values.
     *
     * @param t reference type to check for null values
     * @param <T> the type of item on which to perform the null check
     * @return the reference type if not null or else throws a null pointer exception
     */
    protected static <T> T nullCheck(T t) {
        t.getClass();
        return t;
    }

    /**
     * Returns true if the content ends with a newline character. Empty content is considered as ending with new line.
     *
     * @param contentBuilder content to test for newline character at the end
     * @return true if the content ends with newline.
     */
    protected boolean endsWithNewLine(StringBuilder contentBuilder) {
        int contentLength = contentBuilder.length();
        if (contentLength == 0) {
            return true;
        }
        int nlLength = DocletConstants.NL.length();
        if (contentLength < nlLength) {
            return false;
        }
        int contentIndex = contentLength - 1;
        int nlIndex = nlLength - 1;
        while (nlIndex >= 0) {
            if (contentBuilder.charAt( contentIndex ) != DocletConstants.NL.charAt( nlIndex )) {
                return false;
            }
            contentIndex--;
            nlIndex--;
        }
        return true;
    }
}
