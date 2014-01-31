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
package org.opentravel.schemacompiler.transform;

/**
 * A forward declaration is returned by a <code>SymbolResolver</code> when a named entity cannot be
 * immediately resolved because it is part of a re-entrant call. These forward declarations can be
 * added to the symbol resolver's list of forward declarations for processing at the end of the
 * transformation process.
 * 
 * @author S. Livezey
 */
public final class ForwardDeclaration {

    private String namespace;
    private String localName;

    /**
     * Constructor that specifies the namespace and local name to be resolved after the declarative
     * transformation process has been completed.
     * 
     * @param namespace
     *            the namespace of the forward-declared entity to be resolved
     * @param localName
     *            the local name of the forward-declared entity to be resolved
     */
    public ForwardDeclaration(String namespace, String localName) {
        this.namespace = namespace;
        this.localName = localName;
    }

    /**
     * Resolves this forward declaration by returning the matching entity from the symbol table
     * provided (or null, if no matching entity was found to exist).
     * 
     * @param symbolTable
     *            the symbol table to use for entity resolution
     * @return Object
     */
    public Object resolveEntity(SymbolTable symbolTable) {
        return symbolTable.getEntity(namespace, localName);
    }

}
