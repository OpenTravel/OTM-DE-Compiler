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
        if (linkInfo.getBuilder() != null) {
            // Just a class link
            return getObjectLink( linkInfo );
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
