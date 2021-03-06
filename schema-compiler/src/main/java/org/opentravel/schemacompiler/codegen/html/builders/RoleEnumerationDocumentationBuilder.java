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
import org.opentravel.schemacompiler.model.TLRoleEnumeration;

/**
 * @author Eric.Bronson
 *
 */
public class RoleEnumerationDocumentationBuilder implements DocumentationBuilder {

    TLRoleEnumeration role;


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getQualifiedName() {
        return null;
    }

    @Override
    public DocumentationBuilderType getDocType() {
        return null;
    }

    @Override
    public void setNext(DocumentationBuilder next) {
        // No action required
    }

    @Override
    public void setPrevious(DocumentationBuilder prev) {
        // No action required
    }

    @Override
    public void build() throws CodeGenerationException {
        // No action required
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getOwningLibrary() {
        return null;
    }


}
