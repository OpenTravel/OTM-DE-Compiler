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

package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;

/**
 * @author Eric.Bronson
 *
 */
public interface DocumentationBuilder {

    public String getName();

    public String getNamespace();

    public String getQualifiedName();

    public DocumentationBuilderType getDocType();

    public void setNext(DocumentationBuilder next);

    public void setPrevious(DocumentationBuilder prev);

    public void build() throws CodeGenerationException;

    public String getDescription();

    public String getOwningLibrary();

    public enum DocumentationBuilderType {
        BUSINESS_OBJECT("BusinessObject"),
        CORE_OBJECT("CoreObject"),
        CHOICE_OBJECT("ChoiceObject"),
        VWA("ValueWithAttributes"),
        SERVICE("Service"),
        SIMPLE("SimpleType"),
        CLOSED_ENUM("Closed Enumeration"),
        OPEN_ENUM("Open Enumeration"),
        FACET("Facet"),
        OPERATION("Operation"),
        INDICATOR("Indicator"),
        ATTRIBUTE("Attribute"),
        PROPERTY("Property"),
        LIBRARY("Library");

        private String type;

        private DocumentationBuilderType(String type) {
            this.type = type;
        }

        /**
         * @return the type
         */
        @Override
        public String toString() {
            return type;
        }

    }


}
