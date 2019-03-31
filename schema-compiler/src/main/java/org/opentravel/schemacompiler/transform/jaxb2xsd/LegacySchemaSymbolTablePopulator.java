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

package org.opentravel.schemacompiler.transform.jaxb2xsd;

import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;
import org.w3._2001.xmlschema.TopLevelAttribute;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelElement;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Symbol table populator that creates named entries using the members of the JAXB <code>Schema</code> instance provied.
 * 
 * @author S. Livezey
 */
public class LegacySchemaSymbolTablePopulator implements SymbolTablePopulator<Schema> {

    /**
     * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#populateSymbols(java.lang.Object,
     *      org.opentravel.schemacompiler.transform.SymbolTable)
     */
    @Override
    public void populateSymbols(Schema sourceEntity, SymbolTable symbols) {
        String namespace = sourceEntity.getTargetNamespace();

        for (OpenAttrs schemaTerm : sourceEntity.getSimpleTypeOrComplexTypeOrGroup()) {
            String localName = getLocalName( schemaTerm );

            if (localName != null) {
                symbols.addEntity( namespace, localName, schemaTerm );
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#getLocalName(java.lang.Object)
     */
    @Override
    public String getLocalName(Object sourceObject) {
        String localName = null;

        if (sourceObject instanceof TopLevelSimpleType) {
            localName = ((TopLevelSimpleType) sourceObject).getName();

        } else if (sourceObject instanceof TopLevelComplexType) {
            localName = ((TopLevelComplexType) sourceObject).getName();

        } else if (sourceObject instanceof TopLevelElement) {
            localName = ((TopLevelElement) sourceObject).getName();

        } else if (sourceObject instanceof TopLevelAttribute) {
            localName = ((TopLevelAttribute) sourceObject).getName();
        }
        if (localName != null) {
            localName = localName.trim();
        }
        return localName;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#getSourceEntityType()
     */
    @Override
    public Class<Schema> getSourceEntityType() {
        return Schema.class;
    }

}
